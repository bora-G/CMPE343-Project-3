package com.group17.greengrocer.model;

import java.math.BigDecimal;

/**
 * Product model class representing a product in the greengrocer store.
 */
public class Product {
    private int productId;
    private String productName;
    private String productType;
    private BigDecimal pricePerKg;
    private BigDecimal stock;
    private BigDecimal threshold;
    private String description;
    private String imagePath;
    
    // Constructors
    public Product() {
    }
    
    public Product(String productName, String productType, BigDecimal pricePerKg, BigDecimal stock, BigDecimal threshold) {
        this.productName = productName;
        this.productType = productType;
        this.pricePerKg = pricePerKg;
        this.stock = stock;
        this.threshold = threshold;
    }
    
    // Getters and Setters
    public int getProductId() {
        return productId;
    }
    
    public void setProductId(int productId) {
        this.productId = productId;
    }
    
    public String getProductName() {
        return productName;
    }
    
    public void setProductName(String productName) {
        this.productName = productName;
    }
    
    public String getProductType() {
        return productType;
    }
    
    public void setProductType(String productType) {
        this.productType = productType;
    }
    
    public BigDecimal getPricePerKg() {
        return pricePerKg;
    }
    
    public void setPricePerKg(BigDecimal pricePerKg) {
        this.pricePerKg = pricePerKg;
    }
    
    public BigDecimal getStock() {
        return stock;
    }
    
    public void setStock(BigDecimal stock) {
        this.stock = stock;
    }
    
    public BigDecimal getThreshold() {
        return threshold;
    }
    
    public void setThreshold(BigDecimal threshold) {
        this.threshold = threshold;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public String getImagePath() {
        return imagePath;
    }
    
    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }
    
    /**
     * Check if product has stock available
     */
    public boolean hasStock() {
        return stock != null && stock.compareTo(BigDecimal.ZERO) > 0;
    }
    
    @Override
    public String toString() {
        return "Product{" +
                "productId=" + productId +
                ", productName='" + productName + '\'' +
                ", productType='" + productType + '\'' +
                ", pricePerKg=" + pricePerKg +
                ", stock=" + stock +
                '}';
    }
}




