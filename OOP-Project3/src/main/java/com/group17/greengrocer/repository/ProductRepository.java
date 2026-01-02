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
        String sql = "INSERT INTO ProductInfo (productName, productType, pricePerKg, stock, threshold, description, imagePath, productImage, imageMimeType) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = dbAdapter.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            stmt.setString(1, product.getProductName());
            stmt.setString(2, product.getProductType());
            stmt.setBigDecimal(3, product.getPricePerKg());
            stmt.setBigDecimal(4, product.getStock());
            stmt.setBigDecimal(5, product.getThreshold());
            stmt.setString(6, product.getDescription());
            stmt.setString(7, product.getImagePath());
            if (product.getProductImage() != null) {
                stmt.setBytes(8, product.getProductImage());
            } else {
                stmt.setNull(8, java.sql.Types.BLOB);
            }
            stmt.setString(9, product.getImageMimeType());
            
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
        String sql = "UPDATE ProductInfo SET productName = ?, productType = ?, pricePerKg = ?, " +
                     "stock = ?, threshold = ?, description = ?, imagePath = ?, productImage = ?, imageMimeType = ? WHERE productId = ?";
        
        System.out.println("=== ProductRepository.update() called ===");
        System.out.println("Product ID: " + product.getProductId());
        System.out.println("Product Name: " + product.getProductName());
        System.out.println("Product Type: " + product.getProductType());
        System.out.println("Price: " + product.getPricePerKg());
        System.out.println("Stock: " + product.getStock());
        System.out.println("Threshold: " + product.getThreshold());
        System.out.println("Description: " + (product.getDescription() != null ? product.getDescription() : "null"));
        System.out.println("Image Path: " + (product.getImagePath() != null ? product.getImagePath() : "null"));
        System.out.println("Image Bytes: " + (product.getProductImage() != null ? product.getProductImage().length + " bytes" : "null"));
        System.out.println("Image MIME Type: " + (product.getImageMimeType() != null ? product.getImageMimeType() : "null"));
        
        try (Connection conn = dbAdapter.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            // Set parameters with null checks
            stmt.setString(1, product.getProductName());
            stmt.setString(2, product.getProductType());
            stmt.setBigDecimal(3, product.getPricePerKg());
            stmt.setBigDecimal(4, product.getStock());
            stmt.setBigDecimal(5, product.getThreshold());
            
            if (product.getDescription() != null && !product.getDescription().trim().isEmpty()) {
                stmt.setString(6, product.getDescription());
            } else {
                stmt.setNull(6, java.sql.Types.VARCHAR);
            }
            
            if (product.getImagePath() != null && !product.getImagePath().trim().isEmpty()) {
                stmt.setString(7, product.getImagePath());
            } else {
                stmt.setNull(7, java.sql.Types.VARCHAR);
            }
            
            if (product.getProductImage() != null && product.getProductImage().length > 0) {
                stmt.setBytes(8, product.getProductImage());
                System.out.println("Setting BLOB: " + product.getProductImage().length + " bytes, MIME: " + product.getImageMimeType());
            } else {
                stmt.setNull(8, java.sql.Types.BLOB);
                System.out.println("Setting BLOB to NULL");
            }
            
            if (product.getImageMimeType() != null && !product.getImageMimeType().trim().isEmpty()) {
                stmt.setString(9, product.getImageMimeType());
            } else {
                stmt.setNull(9, java.sql.Types.VARCHAR);
            }
            
            stmt.setInt(10, product.getProductId());
            
            System.out.println("Executing UPDATE query...");
            int rowsAffected = stmt.executeUpdate();
            System.out.println("Update query executed. Rows affected: " + rowsAffected);
            
            if (rowsAffected == 0) {
                System.err.println("WARNING: No rows were updated. Product ID: " + product.getProductId() + " may not exist.");
                // Verify if product exists
                Product existing = findById(product.getProductId());
                if (existing == null) {
                    System.err.println("ERROR: Product with ID " + product.getProductId() + " does not exist in database!");
                } else {
                    System.err.println("Product exists but update failed. Check for constraint violations or data type mismatches.");
                }
            } else {
                System.out.println("SUCCESS: Product updated successfully!");
            }
            
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("SQLException in ProductRepository.update():");
            System.err.println("  Message: " + e.getMessage());
            System.err.println("  SQL State: " + e.getSQLState());
            System.err.println("  Error Code: " + e.getErrorCode());
            e.printStackTrace();
            throw e; // Re-throw to be handled by service layer
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
        
        // Read BLOB image if available
        try {
            java.sql.Blob imageBlob = rs.getBlob("productImage");
            if (imageBlob != null && imageBlob.length() > 0) {
                product.setProductImage(imageBlob.getBytes(1, (int) imageBlob.length()));
            }
        } catch (SQLException e) {
            // Column might not exist in old schema, ignore
        }
        
        try {
            product.setImageMimeType(rs.getString("imageMimeType"));
        } catch (SQLException e) {
            // Column might not exist in old schema, ignore
        }
        
        return product;
    }
}




