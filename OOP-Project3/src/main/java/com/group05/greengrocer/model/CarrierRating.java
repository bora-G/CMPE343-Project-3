package com.group05.greengrocer.model;

import java.time.LocalDateTime;

/**
 * CarrierRating model class representing a rating given to a carrier.
 * Contains rating information including score (1-5), comment, and related IDs.
 */
public class CarrierRating {
    private int ratingId;
    private int orderId;
    private int carrierId;
    private int customerId;
    private int rating;
    private String comment;
    private LocalDateTime createdAt;

    /**
     * Default constructor for CarrierRating.
     */
    public CarrierRating() {
    }

    /**
     * Gets the rating ID.
     * 
     * @return The rating ID
     */
    public int getRatingId() {
        return ratingId;
    }

    /**
     * Sets the rating ID.
     * 
     * @param ratingId The rating ID to set
     */
    public void setRatingId(int ratingId) {
        this.ratingId = ratingId;
    }

    /**
     * Gets the order ID.
     * 
     * @return The order ID
     */
    public int getOrderId() {
        return orderId;
    }

    /**
     * Sets the order ID.
     * 
     * @param orderId The order ID to set
     */
    public void setOrderId(int orderId) {
        this.orderId = orderId;
    }

    /**
     * Gets the carrier ID.
     * 
     * @return The carrier ID
     */
    public int getCarrierId() {
        return carrierId;
    }

    /**
     * Sets the carrier ID.
     * 
     * @param carrierId The carrier ID to set
     */
    public void setCarrierId(int carrierId) {
        this.carrierId = carrierId;
    }

    /**
     * Gets the customer ID.
     * 
     * @return The customer ID
     */
    public int getCustomerId() {
        return customerId;
    }

    /**
     * Sets the customer ID.
     * 
     * @param customerId The customer ID to set
     */
    public void setCustomerId(int customerId) {
        this.customerId = customerId;
    }

    /**
     * Gets the rating value (1-5).
     * 
     * @return The rating value
     */
    public int getRating() {
        return rating;
    }

    /**
     * Sets the rating value (1-5).
     * 
     * @param rating The rating value to set
     */
    public void setRating(int rating) {
        this.rating = rating;
    }

    /**
     * Gets the comment.
     * 
     * @return The comment
     */
    public String getComment() {
        return comment;
    }

    /**
     * Sets the comment.
     * 
     * @param comment The comment to set
     */
    public void setComment(String comment) {
        this.comment = comment;
    }

    /**
     * Gets the creation date.
     * 
     * @return The creation date
     */
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    /**
     * Sets the creation date.
     * 
     * @param createdAt The creation date to set
     */
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
