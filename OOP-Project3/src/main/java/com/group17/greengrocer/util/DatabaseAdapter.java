package com.group17.greengrocer.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * DatabaseAdapter class for managing database connections.
 * Handles JDBC connection to MySQL database.
 * Supports both localhost and local network connections.
 * 
 * For local network usage:
 * - Change DB_HOST to the IP address of the MySQL server (e.g., "192.168.1.100")
 * - Ensure MySQL server allows remote connections
 * - Update MySQL user permissions: GRANT ALL ON greengrocer_db.* TO 'root'@'%';
 */
public class DatabaseAdapter {
    // Database configuration - supports localhost and network IP addresses
    // For local network: change to IP address (e.g., "192.168.1.100" or "192.168.0.50")
    private static final String DB_HOST = "localhost"; // Change to network IP for local network usage
    private static final String DB_PORT = "3306";
    private static final String DB_NAME = "greengrocer_db";
    private static final String DB_URL = "jdbc:mysql://" + DB_HOST + ":" + DB_PORT + "/" + DB_NAME + "?useSSL=false&allowPublicKeyRetrieval=true";
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
    
    /**
     * Run all database migrations
     */
    public void runMigrations() {
        System.out.println("Checking database schema...");
        migrateProductInfoTable();
        migrateOrderInfoTable();
        migrateOrderInfoColumns();
    }
    
    /**
     * Check and add missing columns to ProductInfo table if they don't exist
     * This is a migration method to add productImage and imageMimeType columns
     */
    public void migrateProductInfoTable() {
        try (Connection conn = getConnection()) {
            // Check if productImage column exists
            boolean hasProductImage = columnExists(conn, "ProductInfo", "productImage");
            boolean hasImageMimeType = columnExists(conn, "ProductInfo", "imageMimeType");
            
            if (!hasProductImage || !hasImageMimeType) {
                System.out.println("Migrating ProductInfo table: Adding missing columns...");
                
                if (!hasProductImage) {
                    try (java.sql.Statement stmt = conn.createStatement()) {
                        String sql = "ALTER TABLE ProductInfo ADD COLUMN productImage LONGBLOB AFTER imagePath";
                        stmt.executeUpdate(sql);
                        System.out.println("✓ Added productImage column");
                    } catch (SQLException e) {
                        // Column might already exist, ignore duplicate column error
                        if (!e.getMessage().contains("Duplicate column name")) {
                            System.err.println("Error adding productImage column: " + e.getMessage());
                            e.printStackTrace();
                        }
                    }
                }
                
                if (!hasImageMimeType) {
                    try (java.sql.Statement stmt = conn.createStatement()) {
                        String sql = "ALTER TABLE ProductInfo ADD COLUMN imageMimeType VARCHAR(50) AFTER productImage";
                        stmt.executeUpdate(sql);
                        System.out.println("✓ Added imageMimeType column");
                    } catch (SQLException e) {
                        // Column might already exist, ignore duplicate column error
                        if (!e.getMessage().contains("Duplicate column name")) {
                            System.err.println("Error adding imageMimeType column: " + e.getMessage());
                            e.printStackTrace();
                        }
                    }
                }
                
                System.out.println("Migration completed successfully!");
            } else {
                System.out.println("ProductInfo table is up to date (all columns exist)");
            }
        } catch (SQLException e) {
            System.err.println("Error during database migration: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Migrate OrderInfo table to change invoiceContent from LONGTEXT to LONGBLOB for PDF storage
     */
    public void migrateOrderInfoTable() {
        try (Connection conn = getConnection()) {
            // Check current column type
            String currentType = getColumnType(conn, "OrderInfo", "invoiceContent");
            
            if (currentType != null && currentType.toUpperCase().contains("TEXT")) {
                System.out.println("Migrating OrderInfo table: Converting invoiceContent from TEXT to BLOB for PDF storage...");
                
                try (java.sql.Statement stmt = conn.createStatement()) {
                    // MySQL doesn't support direct ALTER from TEXT to BLOB, so we need to:
                    // 1. Add a temporary column
                    // 2. Copy data (if any) - but since it's PDF, we'll just change the type
                    // 3. Drop old column
                    // 4. Rename new column
                    
                    // For simplicity, we'll just alter the column type directly
                    // Note: This will clear existing text data, but new orders will have PDF
                    String sql = "ALTER TABLE OrderInfo MODIFY COLUMN invoiceContent LONGBLOB";
                    stmt.executeUpdate(sql);
                    System.out.println("✓ Converted invoiceContent to LONGBLOB for PDF storage");
                } catch (SQLException e) {
                    System.err.println("Error migrating invoiceContent column: " + e.getMessage());
                    e.printStackTrace();
                }
            } else if (currentType != null && currentType.toUpperCase().contains("BLOB")) {
                System.out.println("OrderInfo table already has invoiceContent as BLOB (PDF format)");
            } else {
                // Column might not exist, add it
                try (java.sql.Statement stmt = conn.createStatement()) {
                    String sql = "ALTER TABLE OrderInfo ADD COLUMN invoiceContent LONGBLOB AFTER invoicePath";
                    stmt.executeUpdate(sql);
                    System.out.println("✓ Added invoiceContent column as LONGBLOB for PDF storage");
                } catch (SQLException e) {
                    if (!e.getMessage().contains("Duplicate column name")) {
                        System.err.println("Error adding invoiceContent column: " + e.getMessage());
                        e.printStackTrace();
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Error during OrderInfo migration: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Get the data type of a column
     * @param conn Database connection
     * @param tableName Table name
     * @param columnName Column name
     * @return Column type as string, or null if column doesn't exist
     */
    private String getColumnType(Connection conn, String tableName, String columnName) {
        try (java.sql.Statement stmt = conn.createStatement();
             java.sql.ResultSet rs = stmt.executeQuery("SHOW COLUMNS FROM " + tableName + " LIKE '" + columnName + "'")) {
            if (rs.next()) {
                return rs.getString("Type");
            }
        } catch (SQLException e) {
            System.err.println("Error getting column type: " + e.getMessage());
        }
        return null;
    }
    
    /**
     * Migrate OrderInfo table to add missing columns (subtotal, vatAmount, discountAmount, loyaltyDiscount, etc.)
     */
    public void migrateOrderInfoColumns() {
        try (Connection conn = getConnection()) {
            System.out.println("Migrating OrderInfo table: Checking for missing columns...");
            
            // List of required columns with their definitions
            String[][] requiredColumns = {
                {"subtotal", "DECIMAL(10, 2) NOT NULL DEFAULT 0.00"},
                {"vatAmount", "DECIMAL(10, 2) NOT NULL DEFAULT 0.00"},
                {"discountAmount", "DECIMAL(10, 2) NOT NULL DEFAULT 0.00"},
                {"loyaltyDiscount", "DECIMAL(10, 2) NOT NULL DEFAULT 0.00"},
                {"canCancelUntil", "TIMESTAMP NULL"},
                {"couponCode", "VARCHAR(20) NULL"}
            };
            
            for (String[] columnDef : requiredColumns) {
                String columnName = columnDef[0];
                String columnType = columnDef[1];
                
                if (!columnExists(conn, "OrderInfo", columnName)) {
                    try (java.sql.Statement stmt = conn.createStatement()) {
                        String sql = "ALTER TABLE OrderInfo ADD COLUMN " + columnName + " " + columnType;
                        stmt.executeUpdate(sql);
                        System.out.println("✓ Added " + columnName + " column to OrderInfo");
                    } catch (SQLException e) {
                        if (!e.getMessage().contains("Duplicate column name")) {
                            System.err.println("Error adding " + columnName + " column: " + e.getMessage());
                            e.printStackTrace();
                        }
                    }
                } else {
                    System.out.println("OrderInfo table already has " + columnName + " column.");
                }
            }
            
            System.out.println("OrderInfo column migration completed!");
        } catch (SQLException e) {
            System.err.println("Error during OrderInfo column migration: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Check if a column exists in a table
     * @param conn Database connection
     * @param tableName Table name
     * @param columnName Column name
     * @return true if column exists, false otherwise
     */
    private boolean columnExists(Connection conn, String tableName, String columnName) {
        try (java.sql.Statement stmt = conn.createStatement();
             java.sql.ResultSet rs = stmt.executeQuery("SHOW COLUMNS FROM " + tableName + " LIKE '" + columnName + "'")) {
            return rs.next();
        } catch (SQLException e) {
            System.err.println("Error checking column existence: " + e.getMessage());
            return false;
        }
    }
}








