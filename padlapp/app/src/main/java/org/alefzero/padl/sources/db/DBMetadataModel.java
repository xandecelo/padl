package org.alefzero.padl.sources.db;

public class DBMetadataModel {

    private String columnName;
    private String columnLabel;
    private int columnIndex;
    private int columnType;
    private int columnScale;

    public DBMetadataModel(int columnIndex, String columnName, String columnLabel, int columnType, int columnScale) {
        this.columnIndex = columnIndex;
        this.columnName = columnName;
        this.columnLabel = columnLabel;
        this.columnType = columnType;
        this.columnScale = columnScale;
    }

    public String getColumnName() {
        return columnName;
    }

    public void setColumnName(String columnName) {
        this.columnName = columnName;
    }

    public String getColumnLabel() {
        return columnLabel;
    }

    public void setColumnLabel(String columnLabel) {
        this.columnLabel = columnLabel;
    }

    public int getColumnIndex() {
        return columnIndex;
    }

    public void setColumnIndex(int columnIndex) {
        this.columnIndex = columnIndex;
    }

    public int getColumnType() {
        return columnType;
    }

    public void setColumnType(int columnType) {
        this.columnType = columnType;
    }

    public int getColumnScale() {
        return columnScale;
    }

    public void setColumnScale(int columnScale) {
        this.columnScale = columnScale;
    }

}
