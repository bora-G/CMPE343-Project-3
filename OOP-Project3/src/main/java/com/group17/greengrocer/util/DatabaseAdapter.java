package com.group17.greengrocer.util;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

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
            if (conn != null && !conn.isClosed()) {
                // Run migrations after successful connection
                runMigrations(conn);
                return true;
            }
            return false;
        } catch (SQLException e) {
            System.err.println("Database connection test failed: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Run database migrations to add missing columns
     */
    private void runMigrations(Connection conn) {
        // Check and add imageUrl column to ProductInfo
        if (!columnExists(conn, "ProductInfo", "imageUrl")) {
            try (Statement stmt = conn.createStatement()) {
                stmt.execute("ALTER TABLE ProductInfo ADD COLUMN imageUrl VARCHAR(500) AFTER imagePath");
                System.out.println("✓ Added imageUrl column to ProductInfo");
            } catch (SQLException e) {
                System.err.println("Warning: Could not add imageUrl column: " + e.getMessage());
            }
        }
        
        // Check and add imageData column to ProductInfo
        if (!columnExists(conn, "ProductInfo", "imageData")) {
            try (Statement stmt = conn.createStatement()) {
                stmt.execute("ALTER TABLE ProductInfo ADD COLUMN imageData LONGBLOB AFTER imageUrl");
                System.out.println("✓ Added imageData column to ProductInfo");
            } catch (SQLException e) {
                System.err.println("Warning: Could not add imageData column: " + e.getMessage());
            }
        }
        
        // Check and add invoicePdf column to OrderInfo
        if (!columnExists(conn, "OrderInfo", "invoicePdf")) {
            try (Statement stmt = conn.createStatement()) {
                stmt.execute("ALTER TABLE OrderInfo ADD COLUMN invoicePdf LONGBLOB AFTER invoicePath");
                System.out.println("✓ Added invoicePdf column to OrderInfo");
            } catch (SQLException e) {
                System.err.println("Warning: Could not add invoicePdf column: " + e.getMessage());
            }
        }
        
        // Check and add originalPrice column to ProductInfo
        if (!columnExists(conn, "ProductInfo", "originalPrice")) {
            try (Statement stmt = conn.createStatement()) {
                stmt.execute("ALTER TABLE ProductInfo ADD COLUMN originalPrice DECIMAL(10, 2) AFTER pricePerKg");
                System.out.println("✓ Added originalPrice column to ProductInfo");
            } catch (SQLException e) {
                System.err.println("Warning: Could not add originalPrice column: " + e.getMessage());
            }
        }
        
        // Check and add discountPercent column to ProductInfo
        if (!columnExists(conn, "ProductInfo", "discountPercent")) {
            try (Statement stmt = conn.createStatement()) {
                stmt.execute("ALTER TABLE ProductInfo ADD COLUMN discountPercent DECIMAL(5, 2) DEFAULT 0.00 AFTER originalPrice");
                System.out.println("✓ Added discountPercent column to ProductInfo");
            } catch (SQLException e) {
                System.err.println("Warning: Could not add discountPercent column: " + e.getMessage());
            }
        }
        
        // Check and create Coupon table if it doesn't exist
        if (!tableExists(conn, "Coupon")) {
            try (Statement stmt = conn.createStatement()) {
                stmt.execute("CREATE TABLE IF NOT EXISTS Coupon (" +
                    "couponId INT PRIMARY KEY AUTO_INCREMENT, " +
                    "customerId INT NOT NULL, " +
                    "couponCode VARCHAR(20) UNIQUE NOT NULL, " +
                    "discountAmount DECIMAL(10, 2) NOT NULL, " +
                    "discountPercent DECIMAL(5, 2), " +
                    "couponName VARCHAR(100), " +
                    "isUsed BOOLEAN DEFAULT FALSE, " +
                    "expiryDate TIMESTAMP, " +
                    "createdAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                    "FOREIGN KEY (customerId) REFERENCES UserInfo(userId) ON DELETE CASCADE" +
                    ")");
                System.out.println("✓ Created Coupon table");
            } catch (SQLException e) {
                System.err.println("Warning: Could not create Coupon table: " + e.getMessage());
            }
        } else {
            // Check if couponName column exists, add if missing
            if (!columnExists(conn, "Coupon", "couponName")) {
                try (Statement stmt = conn.createStatement()) {
                    stmt.execute("ALTER TABLE Coupon ADD COLUMN couponName VARCHAR(100) AFTER discountPercent");
                    System.out.println("✓ Added couponName column to Coupon");
                } catch (SQLException e) {
                    System.err.println("Warning: Could not add couponName column: " + e.getMessage());
                }
            }
        }
        
        // Check and create CarrierRating table if it doesn't exist
        if (!tableExists(conn, "CarrierRating")) {
            try (Statement stmt = conn.createStatement()) {
                stmt.execute("CREATE TABLE IF NOT EXISTS CarrierRating (" +
                    "ratingId INT PRIMARY KEY AUTO_INCREMENT, " +
                    "orderId INT NOT NULL, " +
                    "carrierId INT NOT NULL, " +
                    "customerId INT NOT NULL, " +
                    "rating INT NOT NULL CHECK (rating >= 1 AND rating <= 5), " +
                    "comment TEXT, " +
                    "createdAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                    "FOREIGN KEY (orderId) REFERENCES OrderInfo(orderId) ON DELETE CASCADE, " +
                    "FOREIGN KEY (carrierId) REFERENCES UserInfo(userId) ON DELETE CASCADE, " +
                    "FOREIGN KEY (customerId) REFERENCES UserInfo(userId) ON DELETE CASCADE" +
                    ")");
                System.out.println("✓ Created CarrierRating table");
            } catch (SQLException e) {
                System.err.println("Warning: Could not create CarrierRating table: " + e.getMessage());
            }
        }
        
        // Check and create Message table if it doesn't exist
        if (!tableExists(conn, "Message")) {
            try (Statement stmt = conn.createStatement()) {
                stmt.execute("CREATE TABLE IF NOT EXISTS Message (" +
                    "messageId INT PRIMARY KEY AUTO_INCREMENT, " +
                    "customerId INT NOT NULL, " +
                    "ownerId INT, " +
                    "subject VARCHAR(200), " +
                    "message TEXT NOT NULL, " +
                    "isRead BOOLEAN DEFAULT FALSE, " +
                    "createdAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                    "FOREIGN KEY (customerId) REFERENCES UserInfo(userId) ON DELETE CASCADE, " +
                    "FOREIGN KEY (ownerId) REFERENCES UserInfo(userId) ON DELETE SET NULL" +
                    ")");
                System.out.println("✓ Created Message table");
            } catch (SQLException e) {
                System.err.println("Warning: Could not create Message table: " + e.getMessage());
            }
        }
    }
    
    /**
     * Check if a table exists
     */
    private boolean tableExists(Connection conn, String tableName) {
        try {
            DatabaseMetaData metaData = conn.getMetaData();
            try (ResultSet rs = metaData.getTables(null, null, tableName, null)) {
                return rs.next();
            }
        } catch (SQLException e) {
            System.err.println("Error checking table existence: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Check if a column exists in a table
     */
    private boolean columnExists(Connection conn, String tableName, String columnName) {
        try {
            DatabaseMetaData metaData = conn.getMetaData();
            try (ResultSet rs = metaData.getColumns(null, null, tableName, columnName)) {
                return rs.next();
            }
        } catch (SQLException e) {
            System.err.println("Error checking column existence: " + e.getMessage());
            return false;
        }
    }
}








