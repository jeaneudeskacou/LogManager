package oprlogmanager;

import java.time.LocalDateTime;
import java.util.*;

public class OPRreport {
    final static List<String> VALID_INFO_KEYS = Arrays.asList(
            "reportingGroupLocation",
            "ReportTimeUnit",
            "ReportGranularity",
            "ReportingGroupRef",
            "outputUri",
            "computeUri",
            "splitCriteriaName",
            "additionalSplitCriteriaName",
            "type",
            "endUnit",
            "startUnit"
    );
    Map<String, String> reportInfo;
    List<LogTable> tables;
    private LocalDateTime startTimestamp;
    private LocalDateTime endTimestamp;

    public OPRreport() {
        reportInfo = new HashMap<>();
        tables = new ArrayList<>();
    }

    public LocalDateTime getStartTimestamp() {
        return startTimestamp;
    }

    public void setStartTimestamp(LocalDateTime startTimeStamp) {
        this.startTimestamp = startTimeStamp;
    }

    public LocalDateTime getEndTimestamp() {
        return endTimestamp;
    }

    public void setEndTimestamp(LocalDateTime endTimeStamp) {
        this.endTimestamp = endTimeStamp;
    }

    boolean tableTimestampMatch(LogTable table) {
        return this.getStartTimestamp().isBefore(table.getStartTimestamp()) &&
                this.getEndTimestamp().isAfter(table.getStartTimestamp());
    }

    @Override
    public String toString() {
        return "startTimestamp: " + startTimestamp +
                " endTimestamp: " + endTimestamp +
                " reportInfo: " + reportInfo;
    }
}
