package org.alefzero.padl.sources.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public abstract class BaseDBConnection {

    private String jdbcURL;
    private String username;
    private String password;

    public BaseDBConnection(String jdbcURL, String username, String password) {
        this.jdbcURL = jdbcURL;
        this.username = username;
        this.password = password;
    }

    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(jdbcURL, username, password);
    }

}
