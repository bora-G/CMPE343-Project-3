package com.group17.greengrocer.model;

import java.time.LocalDateTime;

/**
 * CarrierRating model class representing a rating given to a carrier.
 */
public class CarrierRating {
    private int ratingId;
    private int orderId;
    private int carrierId;
    private int customerId;
    private int rating; // 1-5
    private String comment;
    private LocalDateTime createdAt;
    
    // Constructors
    public CarrierRating() {
    }
    
    // Getters and Setters
    public int getRatingId() {
        return ratingId;
    }
    
    public void setRatingId(int ratingId) {
        this.ratingId = ratingId;
    }
    
    public int getOrderId() {
        return orderId;
    }
    
    public void setOrderId(int orderId) {
        this.orderId = orderId;
    }
    
    public int getCarrierId() {
        return carrierId;
    }
    
    public void setCarrierId(int carrierId) {
        this.carrierId = carrierId;
    }
    
    public int getCustomerId() {
        return customerId;
    }
    
    public void setCustomerId(int customerId) {
        this.customerId = customerId;
    }
    
    public int getRating() {
        return rating;
    }
    
    public void setRating(int rating) {
        this.rating = rating;
    }
    
    public String getComment() {
        return comment;
    }
    
    public void setComment(String comment) {
        this.comment = comment;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}

