package com.group05.greengrocer.model;

import java.math.BigDecimal;

/**
 * Product model class representing a product in the greengrocer store.
 * Contains product information including name, type, price, stock, and image
 * data.
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
    private String imageUrl;
    private byte[] imageData;
    private BigDecimal originalPrice;
    private BigDecimal discountPercent;

    /**
     * Default constructor for Product.
     */
    public Product() {
    }

    /**
     * Constructor for Product with basic information.
     * 
     * @param productName The name of the product
     * @param productType The type/category of the product
     * @param pricePerKg  The price per kilogram
     * @param stock       The current stock amount
     * @param threshold   The minimum stock threshold
     */
    public Product(String productName, String productType, BigDecimal pricePerKg, BigDecimal stock,
            BigDecimal threshold) {
        this.productName = productName;
        this.productType = productType;
        this.pricePerKg = pricePerKg;
        this.stock = stock;
        this.threshold = threshold;
    }

    /**
     * Gets the product ID.
     * 
     * @return The product ID
     */
    public int getProductId() {
        return productId;
    }

    /**
     * Sets the product ID.
     * 
     * @param productId The product ID to set
     */
    public void setProductId(int productId) {
        this.productId = productId;
    }

    /**
     * Gets the product name.
     * 
     * @return The product name
     */
    public String getProductName() {
        return productName;
    }

    /**
     * Sets the product name.
     * 
     * @param productName The product name to set
     */
    public void setProductName(String productName) {
        this.productName = productName;
    }

    /**
     * Gets the product type/category.
     * 
     * @return The product type
     */
    public String getProductType() {
        return productType;
    }

    /**
     * Sets the product type/category.
     * 
     * @param productType The product type to set
     */
    public void setProductType(String productType) {
        this.productType = productType;
    }

    /**
     * Gets the price per kilogram.
     * 
     * @return The price per kilogram
     */
    public BigDecimal getPricePerKg() {
        return pricePerKg;
    }

    /**
     * Sets the price per kilogram.
     * 
     * @param pricePerKg The price per kilogram to set
     */
    public void setPricePerKg(BigDecimal pricePerKg) {
        this.pricePerKg = pricePerKg;
    }

    /**
     * Gets the current stock amount.
     * 
     * @return The stock amount
     */
    public BigDecimal getStock() {
        return stock;
    }

    /**
     * Sets the stock amount.
     * 
     * @param stock The stock amount to set
     */
    public void setStock(BigDecimal stock) {
        this.stock = stock;
    }

    /**
     * Gets the minimum stock threshold.
     * 
     * @return The threshold value
     */
    public BigDecimal getThreshold() {
        return threshold;
    }

    /**
     * Sets the minimum stock threshold.
     * 
     * @param threshold The threshold value to set
     */
    public void setThreshold(BigDecimal threshold) {
        this.threshold = threshold;
    }

    /**
     * Gets the product description.
     * 
     * @return The description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Sets the product description.
     * 
     * @param description The description to set
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Gets the image file path.
     * 
     * @return The image path
     */
    public String getImagePath() {
        return imagePath;
    }

    /**
     * Sets the image file path.
     * 
     * @param imagePath The image path to set
     */
    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    /**
     * Gets the image URL.
     * 
     * @return The image URL
     */
    public String getImageUrl() {
        return imageUrl;
    }

    /**
     * Sets the image URL.
     * 
     * @param imageUrl The image URL to set
     */
    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    /**
     * Gets the image data as byte array.
     * 
     * @return The image data
     */
    public byte[] getImageData() {
        return imageData;
    }

    /**
     * Sets the image data as byte array.
     * 
     * @param imageData The image data to set
     */
    public void setImageData(byte[] imageData) {
        this.imageData = imageData;
    }

    /**
     * Gets the original price before discount.
     * 
     * @return The original price
     */
    public BigDecimal getOriginalPrice() {
        return originalPrice;
    }

    /**
     * Sets the original price before discount.
     * 
     * @param originalPrice The original price to set
     */
    public void setOriginalPrice(BigDecimal originalPrice) {
        this.originalPrice = originalPrice;
    }

    /**
     * Gets the discount percentage.
     * 
     * @return The discount percentage
     */
    public BigDecimal getDiscountPercent() {
        return discountPercent;
    }

    /**
     * Sets the discount percentage.
     * 
     * @param discountPercent The discount percentage to set
     */
    public void setDiscountPercent(BigDecimal discountPercent) {
        this.discountPercent = discountPercent;
    }

    /**
     * Checks if the product has stock available.
     * 
     * @return true if stock is greater than zero, false otherwise
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
