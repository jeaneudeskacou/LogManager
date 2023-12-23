package oprlogmanager;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class LogManager {
    private final static String TRACE_PATH = "C:\\Workspaces\\logs\\opr205\\arbopr_openreport_trace.log";
    private final static String TABLE_NAME_MARKER = "Start log content for table";
    private final static String SPACE_SEPARATOR = " ";
    private final static String PIPE_SEPARATOR = "\\|";
    private final static List<String> IGNORE_MARKERS = Arrays.asList("TRACE", "DEBUG", "Message");
    private final static String LOG_PATH = "C:\\Workspaces\\logs\\opr205\\arbopr_openreport.log";
    private final static String START_REPORT_MARKER = "[generateReport";
    private final static String END_REPORT_MARKER = "End : generateReport";
    private final static char OPEN_BRACKET = '[';
    private final static char CLOSE_BRACKET = ']';
    private final static char COMMA = ',';
    private final static String TIME_FORMATTER = "yyyy-MM-dd HH:mm:ss,SSS";

    private BufferedReader bufferedReader;
    private String line_ptr;

    public static void main(String[] args) {
        LogManager lm = new LogManager();
        lm.getTablesByReport();
        System.out.println(lm);
    }

    List<LogTable> getTablesByReport() {
        List<OPRreport> reports = getReports();
        List<LogTable> tables = getTables();
        mapTablesByReports(tables, reports);
        return new ArrayList<>();
    }

    List<OPRreport> getReports() {
        List<OPRreport> reports = new ArrayList<>();
        bufferedReader = openFile(LOG_PATH);
        line_ptr = nextLine();
        while (line_ptr != null) {
            if (line_ptr.contains(START_REPORT_MARKER)) {
                reports.add(getReport());
            }
            line_ptr = nextLine();
        }
        return reports;
    }

    private BufferedReader openFile(String path) {
        try {
            return new BufferedReader(new FileReader(path));
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    private String nextLine() {
        try {
            return bufferedReader.readLine();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private OPRreport getReport() {
        OPRreport report = new OPRreport();
        line_ptr = nextLine();
        report.setStartTimestamp(getAndParseTimestamp(line_ptr));
        while (line_ptr != null && !line_ptr.contains(END_REPORT_MARKER)) {
            line_ptr = nextLine();
        }
        if (line_ptr != null) {
            setReportInfo(report, line_ptr);
            line_ptr = nextLine();
            report.setEndTimestamp(getAndParseTimestamp(line_ptr));
        }
        return report;
    }

    private void setReportInfo(OPRreport report, String line) {
        report.reportInfo.putAll(getFilteredReportInfo(line));
    }

    private Map<String, String> getFilteredReportInfo(String line) {
        line = line.substring(line.indexOf(OPEN_BRACKET), line.lastIndexOf(CLOSE_BRACKET) + 1);
        Map<String, String> reportInfo = parseReportInfo(line);
        return filterMapKeys(reportInfo);
    }

    Map<String, String> parseReportInfo(String rawParams) {
        Map<String, String> reportInfo = new HashMap<>();
        int start = -1;
        String key = "";
        String val;
        for (int ptr = 0; ptr < rawParams.length(); ptr++) {
            if (rawParams.charAt(ptr) != OPEN_BRACKET) {
                if (start == -1)
                    start = ptr;
                else if (rawParams.charAt(ptr) == '=') {
                    key = rawParams.substring(start, ptr).trim();
                    start = -1;
                } else if (isValueEnd(rawParams.charAt(ptr))) {
                    val = rawParams.substring(start, ptr).trim();
                    reportInfo.put(key, val);
                    start = -1;
                }
            }
        }
        return reportInfo;
    }

    boolean isValueEnd(char c) {
        return c == COMMA || c == CLOSE_BRACKET;
    }

    Map<String, String> filterMapKeys(Map<String, String> baseMap) {
        return baseMap.keySet().stream()
                .filter(candidateKey ->
                        OPRreport.VALID_INFO_KEYS.stream().anyMatch(candidateKey::equalsIgnoreCase))
                .collect(Collectors.toMap(Function.identity(), baseMap::get));
    }

    List<LogTable> getTables() {
        List<LogTable> result = new ArrayList<>();
        bufferedReader = openFile(TRACE_PATH);
        line_ptr = nextLine();
        while (line_ptr != null) {
            if (line_ptr.contains(TABLE_NAME_MARKER)) {
                LogTable logTable = getTable();
                result.add(logTable);
            }
        }
        return result;
    }

    LogTable getTable() {
        String tableName = getTableName(line_ptr);
        LogTable logTable = new LogTable(tableName);
        logTable.setStartTimestamp(getAndParseTimestamp(line_ptr));
        nextLine();
        String header = line_ptr = nextLine();
        logTable.setColumns(header);
        line_ptr = nextLine();
        while (isTableContinue()) {
            if (lineShouldBeIgnored(line_ptr)) {
                line_ptr = nextLine();
                continue;
            }
            logTable.addLine(line_ptr);
            line_ptr = nextLine();
        }

        return logTable;
    }

    private void mapTablesByReports(List<LogTable> tables, List<OPRreport> reports) {
        reports.stream().forEach(report -> {
            tables.stream().filter(report::tableTimestampMatch).forEach(report.tables::add);
        });
    }

    String getTableName(String line) {
        String[] spitted = line.split(SPACE_SEPARATOR);
        return spitted[spitted.length - 1];
    }

    private boolean isTableContinue() {
        return line_ptr != null && !line_ptr.contains(TABLE_NAME_MARKER);
    }

    LocalDateTime getAndParseTimestamp(String line) {
        return parseTime(line.split(PIPE_SEPARATOR)[0]);
    }

    LocalDateTime parseTime(String timestamp) {
        return LocalDateTime.parse(timestamp, DateTimeFormatter.ofPattern(TIME_FORMATTER));
    }

    private boolean lineShouldBeIgnored(String line) {
        return IGNORE_MARKERS.stream().anyMatch(line::contains);
    }
}
