package com.group17.greengrocer.service;

import com.group17.greengrocer.model.Product;
import com.group17.greengrocer.repository.ProductRepository;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;

/**
 * Service class for product business logic.
 */
public class ProductService {
    private final ProductRepository productRepository;
    
    /**
     * Constructor for ProductService.
     */
    public ProductService() {
        this.productRepository = new ProductRepository();
    }
    
    /**
     * Get all available products (stock > 0), sorted alphabetically.
     * @return List of available products with stock > 0
     */
    public List<Product> getAvailableProducts() {
        try {
            return productRepository.findAvailableProducts();
        } catch (SQLException e) {
            System.err.println("Error fetching available products: " + e.getMessage());
            e.printStackTrace();
            return List.of();
        }
    }
    
    /**
     * Get products grouped by type.
     * @return List of all unique product types
     */
    public List<String> getProductTypes() {
        try {
            return productRepository.getAllTypes();
        } catch (SQLException e) {
            System.err.println("Error fetching product types: " + e.getMessage());
            e.printStackTrace();
            return List.of();
        }
    }
    
    /**
     * Get products by type (only with stock > 0).
     * @param type The product type to filter by
     * @return List of products with the specified type and stock > 0
     */
    public List<Product> getProductsByType(String type) {
        try {
            return productRepository.findByType(type);
        } catch (SQLException e) {
            System.err.println("Error fetching products by type: " + e.getMessage());
            e.printStackTrace();
            return List.of();
        }
    }
    
    /**
     * Search products by name (case-insensitive).
     * @param searchTerm The search term to match against product names
     * @return List of products matching the search term with stock > 0
     */
    public List<Product> searchProducts(String searchTerm) {
        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            return getAvailableProducts();
        }
        
        try {
            return productRepository.searchByName(searchTerm);
        } catch (SQLException e) {
            System.err.println("Error searching products: " + e.getMessage());
            e.printStackTrace();
            return List.of();
        }
    }
    
    /**
     * Get product by ID.
     * @param productId The product ID to search for
     * @return The Product object if found, null otherwise
     */
    public Product getProductById(int productId) {
        try {
            return productRepository.findById(productId);
        } catch (SQLException e) {
            System.err.println("Error fetching product: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * Get display price for a product based on threshold rule.
     * Rule: If stock &lt;= threshold, price doubles.
     * 
     * @param product The product to get display price for
     * @return The display price (doubled if stock &lt;= threshold, otherwise base price)
     */
    public BigDecimal getDisplayPrice(Product product) {
        if (product == null || product.getPricePerKg() == null) {
            return BigDecimal.ZERO;
        }
        
        BigDecimal basePrice = product.getPricePerKg();
        BigDecimal stock = product.getStock();
        BigDecimal threshold = product.getThreshold();
        
        if (threshold == null) {
            threshold = new BigDecimal("5.0");
        }
        
        if (stock == null || stock.compareTo(BigDecimal.ZERO) <= 0) {
            return basePrice;
        }
        
        if (stock.compareTo(threshold) <= 0) {
            return basePrice.multiply(new BigDecimal("2"));
        }
        
        return basePrice;
    }
    
    /**
     * Get all products (for owner).
     * @return List of all products, sorted by name
     */
    public List<Product> getAllProducts() {
        try {
            return productRepository.findAll();
        } catch (SQLException e) {
            System.err.println("Error fetching all products: " + e.getMessage());
            e.printStackTrace();
            return List.of();
        }
    }
    
    /**
     * Create a new product.
     * @param product The Product object to create
     * @return true if product was created successfully, false otherwise
     */
    public boolean createProduct(Product product) {
        try {
            return productRepository.create(product);
        } catch (SQLException e) {
            System.err.println("Error creating product: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Update product.
     * @param product The Product object with updated information
     * @return true if product was updated successfully, false otherwise
     */
    public boolean updateProduct(Product product) {
        try {
            if (product.getProductId() <= 0) {
                System.err.println("Error: Invalid product ID: " + product.getProductId());
                return false;
            }
            return productRepository.update(product);
        } catch (SQLException e) {
            System.err.println("Error updating product: " + e.getMessage());
            System.err.println("SQL State: " + e.getSQLState());
            System.err.println("Error Code: " + e.getErrorCode());
            e.printStackTrace();
            return false;
        } catch (Exception e) {
            System.err.println("Unexpected error updating product: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Update product stock.
     * @param productId The ID of the product to update
     * @param newStock The new stock amount
     * @return true if stock was updated successfully, false otherwise
     */
    public boolean updateStock(int productId, BigDecimal newStock) {
        try {
            return productRepository.updateStock(productId, newStock);
        } catch (SQLException e) {
            System.err.println("Error updating stock: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Delete product.
     * @param productId The ID of the product to delete
     * @return true if product was deleted successfully, false otherwise
     */
    public boolean deleteProduct(int productId) {
        try {
            return productRepository.delete(productId);
        } catch (SQLException e) {
            System.err.println("Error deleting product: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
}

