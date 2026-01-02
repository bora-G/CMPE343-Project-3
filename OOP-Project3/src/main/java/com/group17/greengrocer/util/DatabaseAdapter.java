package com.group17.greengrocer.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * DatabaseAdapter class for managing database connections.
 * Handles JDBC connection to MySQL database.
 */
public class DatabaseAdapter {
    private static final String DB_URL = "jdbc:mysql://localhost:3306/greengrocer_db";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "1234"; // Change this to your MySQL password
    
    private static DatabaseAdapter instance;
    private Connection connection;
    
    /**
     * Private constructor for singleton pattern
     */
    private DatabaseAdapter() {
        try {
            // Load MySQL JDBC driver
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            System.err.println("MySQL JDBC Driver not found!");
            e.printStackTrace();
        }
    }
    
    /**
     * Get singleton instance of DatabaseAdapter
     */
    public static synchronized DatabaseAdapter getInstance() {
        if (instance == null) {
            instance = new DatabaseAdapter();
        }
        return instance;
    }
    
    /**
     * Get database connection
     * @return Connection object
     * @throws SQLException if connection fails
     */
    public Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
        }
        return connection;
    }
    
    /**
     * Close database connection
     */
    public void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            System.err.println("Error closing database connection: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Test database connection
     * @return true if connection is successful
     */
    public boolean testConnection() {
        try {
            Connection conn = getConnection();
            return conn != null && !conn.isClosed();
        } catch (SQLException e) {
            System.err.println("Database connection test failed: " + e.getMessage());
            return false;
        }
    }
}








