package org.alefzero.padl.sources.impl;

import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;

public class DBSourceMeta {
    public int columnCount;
    public List<Integer> columnType = new LinkedList<Integer>();
    public List<String> columnName = new LinkedList<String>();
    public List<Integer> precision = new LinkedList<Integer>();
    public List<Integer> scale = new LinkedList<Integer>();

    public DBSourceMeta(ResultSetMetaData meta) throws SQLException {
        columnCount = meta.getColumnCount();
        for (int i = 0; i < columnCount; i++) {
            // Follow SQL convention of 1st col index = 1
            scale.add(meta.getScale(i + 1) < 0 ? 0 : meta.getScale(i + 1));
            precision.add(meta.getPrecision(i + 1) < 0 ? 0 : meta.getPrecision(i + 1));
            columnType.add(meta.getColumnType(i + 1));
        }
    }

    public int getColumnType(int i) {
        // Follow SQL convention of 1st col index = 1
        return columnType.get(i - 1);
    }

    public int getPrecision(int i) {
        // Follow SQL convention of 1st col index = 1
        return precision.get(i - 1);
    }

    public int getScale(int i) {
        // Follow SQL convention of 1st col index = 1
        return scale.get(i - 1);
    }

    public String getColumnName(int i) {
        // Follow SQL convention of 1st col index = 1
        return columnName.get(i - 1);
    }

    public int getColumnCount() {
        return columnCount;
    }

}
