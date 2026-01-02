package com.group17.greengrocer.repository;

import com.group17.greengrocer.model.Product;
import com.group17.greengrocer.util.DatabaseAdapter;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Repository class for Product database operations.
 * Handles all database access related to products.
 */
public class ProductRepository {
    private final DatabaseAdapter dbAdapter;
    
    public ProductRepository() {
        this.dbAdapter = DatabaseAdapter.getInstance();
    }
    
    /**
     * Find product by ID
     */
    public Product findById(int productId) throws SQLException {
        String sql = "SELECT * FROM ProductInfo WHERE productId = ?";
        
        try (Connection conn = dbAdapter.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, productId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToProduct(rs);
                }
            }
        }
        return null;
    }
    
    /**
     * Get all products
     */
    public List<Product> findAll() throws SQLException {
        String sql = "SELECT * FROM ProductInfo ORDER BY productName";
        List<Product> products = new ArrayList<>();
        
        try (Connection conn = dbAdapter.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                products.add(mapResultSetToProduct(rs));
            }
        }
        return products;
    }
    
    /**
     * Get all products with stock > 0, sorted alphabetically
     */
    public List<Product> findAvailableProducts() throws SQLException {
        String sql = "SELECT * FROM ProductInfo WHERE stock > 0 ORDER BY productName";
        List<Product> products = new ArrayList<>();
        
        try (Connection conn = dbAdapter.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                products.add(mapResultSetToProduct(rs));
            }
        }
        return products;
    }
    
    /**
     * Get products by type
     */
    public List<Product> findByType(String productType) throws SQLException {
        String sql = "SELECT * FROM ProductInfo WHERE productType = ? AND stock > 0 ORDER BY productName";
        List<Product> products = new ArrayList<>();
        
        try (Connection conn = dbAdapter.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, productType);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    products.add(mapResultSetToProduct(rs));
                }
            }
        }
        return products;
    }
    
    /**
     * Search products by name (case-insensitive)
     */
    public List<Product> searchByName(String searchTerm) throws SQLException {
        String sql = "SELECT * FROM ProductInfo WHERE LOWER(productName) LIKE ? AND stock > 0 ORDER BY productName";
        List<Product> products = new ArrayList<>();
        
        try (Connection conn = dbAdapter.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, "%" + searchTerm.toLowerCase() + "%");
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    products.add(mapResultSetToProduct(rs));
                }
            }
        }
        return products;
    }
    
    /**
     * Get all unique product types
     */
    public List<String> getAllTypes() throws SQLException {
        String sql = "SELECT DISTINCT productType FROM ProductInfo ORDER BY productType";
        List<String> types = new ArrayList<>();
        
        try (Connection conn = dbAdapter.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                types.add(rs.getString("productType"));
            }
        }
        return types;
    }
    
    /**
     * Create a new product
     */
    public boolean create(Product product) throws SQLException {
        // Check if imageUrl column exists
        String sql;
        boolean hasImageColumns = false;
        try {
            try (Connection conn = dbAdapter.getConnection();
                 Statement checkStmt = conn.createStatement()) {
                checkStmt.executeQuery("SELECT imageUrl FROM ProductInfo LIMIT 1");
                hasImageColumns = true;
                sql = "INSERT INTO ProductInfo (productName, productType, pricePerKg, stock, threshold, description, imagePath, imageUrl, imageData) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
            }
        } catch (SQLException e) {
            hasImageColumns = false;
            sql = "INSERT INTO ProductInfo (productName, productType, pricePerKg, stock, threshold, description, imagePath) " +
                 "VALUES (?, ?, ?, ?, ?, ?, ?)";
        }
        
        try (Connection conn = dbAdapter.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            stmt.setString(1, product.getProductName());
            stmt.setString(2, product.getProductType());
            stmt.setBigDecimal(3, product.getPricePerKg());
            stmt.setBigDecimal(4, product.getStock());
            stmt.setBigDecimal(5, product.getThreshold());
            stmt.setString(6, product.getDescription() != null ? product.getDescription() : "");
            stmt.setString(7, product.getImagePath() != null ? product.getImagePath() : "");
            
            if (hasImageColumns) {
                stmt.setString(8, product.getImageUrl() != null ? product.getImageUrl() : "");
                if (product.getImageData() != null) {
                    stmt.setBytes(9, product.getImageData());
                } else {
                    stmt.setNull(9, Types.BLOB);
                }
            }
            
            int rowsAffected = stmt.executeUpdate();
            
            if (rowsAffected > 0) {
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        product.setProductId(generatedKeys.getInt(1));
                    }
                }
                return true;
            }
        }
        return false;
    }
    
    /**
     * Update product information
     */
    public boolean update(Product product) throws SQLException {
        // Check if imageUrl column exists, use appropriate SQL
        String sql;
        try {
            // Try to check if column exists
            try (Connection conn = dbAdapter.getConnection();
                 Statement checkStmt = conn.createStatement()) {
                checkStmt.executeQuery("SELECT imageUrl FROM ProductInfo LIMIT 1");
                // Column exists, use full SQL
                sql = "UPDATE ProductInfo SET productName = ?, productType = ?, pricePerKg = ?, " +
                     "stock = ?, threshold = ?, description = ?, imagePath = ?, imageUrl = ?, imageData = ? WHERE productId = ?";
            }
        } catch (SQLException e) {
            // Column doesn't exist, use SQL without imageUrl/imageData
            sql = "UPDATE ProductInfo SET productName = ?, productType = ?, pricePerKg = ?, " +
                 "stock = ?, threshold = ?, description = ?, imagePath = ? WHERE productId = ?";
        }
        
        try (Connection conn = dbAdapter.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, product.getProductName());
            stmt.setString(2, product.getProductType());
            stmt.setBigDecimal(3, product.getPricePerKg());
            stmt.setBigDecimal(4, product.getStock());
            stmt.setBigDecimal(5, product.getThreshold());
            stmt.setString(6, product.getDescription() != null ? product.getDescription() : "");
            stmt.setString(7, product.getImagePath() != null ? product.getImagePath() : "");
            
            // Only set imageUrl and imageData if column exists
            if (sql.contains("imageUrl")) {
                stmt.setString(8, product.getImageUrl() != null ? product.getImageUrl() : "");
                if (product.getImageData() != null) {
                    stmt.setBytes(9, product.getImageData());
                } else {
                    stmt.setNull(9, Types.BLOB);
                }
                stmt.setInt(10, product.getProductId());
            } else {
                stmt.setInt(8, product.getProductId());
            }
            
            return stmt.executeUpdate() > 0;
        }
    }
    
    /**
     * Update product stock
     */
    public boolean updateStock(int productId, BigDecimal newStock) throws SQLException {
        String sql = "UPDATE ProductInfo SET stock = ? WHERE productId = ?";
        
        try (Connection conn = dbAdapter.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setBigDecimal(1, newStock);
            stmt.setInt(2, productId);
            
            return stmt.executeUpdate() > 0;
        }
    }
    
    /**
     * Delete product
     */
    public boolean delete(int productId) throws SQLException {
        String sql = "DELETE FROM ProductInfo WHERE productId = ?";
        
        try (Connection conn = dbAdapter.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, productId);
            return stmt.executeUpdate() > 0;
        }
    }
    
    /**
     * Map ResultSet to Product object
     */
    private Product mapResultSetToProduct(ResultSet rs) throws SQLException {
        Product product = new Product();
        product.setProductId(rs.getInt("productId"));
        product.setProductName(rs.getString("productName"));
        product.setProductType(rs.getString("productType"));
        product.setPricePerKg(rs.getBigDecimal("pricePerKg"));
        product.setStock(rs.getBigDecimal("stock"));
        product.setThreshold(rs.getBigDecimal("threshold"));
        product.setDescription(rs.getString("description"));
        product.setImagePath(rs.getString("imagePath"));
        
        // Handle new fields (with backward compatibility)
        try {
            product.setImageUrl(rs.getString("imageUrl"));
        } catch (SQLException e) {
            product.setImageUrl(null);
        }
        
        try {
            product.setImageData(rs.getBytes("imageData"));
        } catch (SQLException e) {
            product.setImageData(null);
        }
        
        return product;
    }
}




