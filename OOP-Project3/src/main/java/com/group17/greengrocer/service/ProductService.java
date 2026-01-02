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
    
    public ProductService() {
        this.productRepository = new ProductRepository();
    }
    
    /**
     * Get all available products (stock > 0), sorted alphabetically
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
     * Get products grouped by type
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
     * Get products by type (only with stock > 0)
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
     * Search products by name (case-insensitive)
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
     * Get product by ID
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
     * Rule: If stock <= threshold, price doubles.
     * 
     * @param product The product to get display price for
     * @return The display price (doubled if stock <= threshold, otherwise base price)
     */
    public BigDecimal getDisplayPrice(Product product) {
        if (product == null || product.getPricePerKg() == null) {
            return BigDecimal.ZERO;
        }
        
        BigDecimal basePrice = product.getPricePerKg();
        BigDecimal stock = product.getStock();
        BigDecimal threshold = product.getThreshold();
        
        // If threshold is null, use default threshold of 5.0
        if (threshold == null) {
            threshold = new BigDecimal("5.0");
        }
        
        // If stock is null or zero, return base price (or could double, but typically no stock = not available)
        if (stock == null || stock.compareTo(BigDecimal.ZERO) <= 0) {
            return basePrice;
        }
        
        // If stock <= threshold, price doubles
        if (stock.compareTo(threshold) <= 0) {
            return basePrice.multiply(new BigDecimal("2"));
        }
        
        return basePrice;
    }
    
    /**
     * Get all products (for owner)
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
     * Create a new product
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
     * Update product
     */
    public boolean updateProduct(Product product) {
        try {
            return productRepository.update(product);
        } catch (SQLException e) {
            System.err.println("Error updating product: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Update product stock
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
     * Delete product
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

