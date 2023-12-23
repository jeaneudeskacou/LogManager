package oprlogmanager;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class LogTable {
    private String tableName;
    private String columns;
    private List<String> lines;
    private LocalDateTime startTimestamp;

    public LogTable(String tableName) {
        this.tableName = tableName;
        lines = new ArrayList<>();
    }

    public LocalDateTime getStartTimestamp() {
        return startTimestamp;
    }

    public void setStartTimestamp(LocalDateTime startTimestamp) {
        this.startTimestamp = startTimestamp;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public String getColumns() {
        return columns;
    }

    public void setColumns(String columns) {
        this.columns = columns;
    }

    public List<String> getLines() {
        return lines;
    }

    public void setLines(List<String> lines) {
        this.lines = lines;
    }

    public void addLine(String line) {
        lines.add(line);
    }

    @Override
    public String toString() {
        return tableName;
    }
}
