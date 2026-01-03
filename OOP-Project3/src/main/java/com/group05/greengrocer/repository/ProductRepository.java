package com.group05.greengrocer.repository;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import com.group05.greengrocer.model.Product;
import com.group05.greengrocer.util.DatabaseAdapter;

/**
 * Repository class for Product database operations.
 * Handles all database access related to products.
 */
public class ProductRepository {
    private final DatabaseAdapter dbAdapter;

    /**
     * Constructor for ProductRepository.
     */
    public ProductRepository() {
        this.dbAdapter = DatabaseAdapter.getInstance();
    }

    /**
     * Find product by ID.
     * 
     * @param productId The product ID to search for
     * @return The Product object if found, null otherwise
     * @throws SQLException if database access error occurs
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
     * Get all products.
     * 
     * @return List of all products, sorted by name
     * @throws SQLException if database access error occurs
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
     * Get all products with stock > 0, sorted alphabetically.
     * 
     * @return List of available products with stock > 0
     * @throws SQLException if database access error occurs
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
     * Get products by type.
     * 
     * @param productType The product type to filter by
     * @return List of products with the specified type and stock > 0
     * @throws SQLException if database access error occurs
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
     * Search products by name (case-insensitive).
     * 
     * @param searchTerm The search term to match against product names
     * @return List of products matching the search term with stock > 0
     * @throws SQLException if database access error occurs
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
     * Get all unique product types.
     * 
     * @return List of all distinct product types, sorted alphabetically
     * @throws SQLException if database access error occurs
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
     * Create a new product.
     * 
     * @param product The Product object to create
     * @return true if product was created successfully, false otherwise
     * @throws SQLException if database access error occurs
     */
    public boolean create(Product product) throws SQLException {
        String sql;
        boolean hasImageColumns = false;
        try {
            try (Connection conn = dbAdapter.getConnection();
                    Statement checkStmt = conn.createStatement()) {
                checkStmt.executeQuery("SELECT imageUrl FROM ProductInfo LIMIT 1");
                hasImageColumns = true;
                sql = "INSERT INTO ProductInfo (productName, productType, pricePerKg, stock, threshold, description, imagePath, imageUrl, imageData) "
                        +
                        "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
            }
        } catch (SQLException e) {
            hasImageColumns = false;
            sql = "INSERT INTO ProductInfo (productName, productType, pricePerKg, stock, threshold, description, imagePath) "
                    +
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
     * Update product information.
     * 
     * @param product The Product object with updated information
     * @return true if product was updated successfully, false otherwise
     * @throws SQLException if database access error occurs
     */
    public boolean update(Product product) throws SQLException {
        String sql;
        try {
            try (Connection conn = dbAdapter.getConnection();
                    Statement checkStmt = conn.createStatement()) {
                checkStmt.executeQuery("SELECT imageUrl FROM ProductInfo LIMIT 1");
                sql = "UPDATE ProductInfo SET productName = ?, productType = ?, pricePerKg = ?, " +
                        "stock = ?, threshold = ?, description = ?, imagePath = ?, imageUrl = ?, imageData = ? WHERE productId = ?";
            }
        } catch (SQLException e) {
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
     * Update product stock.
     * 
     * @param productId The ID of the product to update
     * @param newStock  The new stock amount
     * @return true if stock was updated successfully, false otherwise
     * @throws SQLException if database access error occurs
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
     * Delete product.
     * 
     * @param productId The ID of the product to delete
     * @return true if product was deleted successfully, false otherwise
     * @throws SQLException if database access error occurs
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
     * Map ResultSet to Product object.
     * 
     * @param rs The ResultSet containing product data
     * @return The mapped Product object
     * @throws SQLException if database access error occurs
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
