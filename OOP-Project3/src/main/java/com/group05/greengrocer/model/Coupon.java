package com.group05.greengrocer.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Coupon model class representing a discount coupon.
 * Contains coupon information including code, name, discount amount/percentage,
 * and usage status.
 */
public class Coupon {
    private int couponId;
    private int customerId;
    private String couponCode;
    private String couponName;
    private BigDecimal discountAmount;
    private BigDecimal discountPercent;
    private boolean isUsed;
    private LocalDateTime expiryDate;
    private LocalDateTime createdAt;

    /**
     * Default constructor for Coupon.
     */
    public Coupon() {
    }

    /**
     * Gets the coupon ID.
     * 
     * @return The coupon ID
     */
    public int getCouponId() {
        return couponId;
    }

    /**
     * Sets the coupon ID.
     * 
     * @param couponId The coupon ID to set
     */
    public void setCouponId(int couponId) {
        this.couponId = couponId;
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
     * Gets the coupon code.
     * 
     * @return The coupon code
     */
    public String getCouponCode() {
        return couponCode;
    }

    /**
     * Sets the coupon code.
     * 
     * @param couponCode The coupon code to set
     */
    public void setCouponCode(String couponCode) {
        this.couponCode = couponCode;
    }

    /**
     * Gets the coupon name.
     * 
     * @return The coupon name
     */
    public String getCouponName() {
        return couponName;
    }

    /**
     * Sets the coupon name.
     * 
     * @param couponName The coupon name to set
     */
    public void setCouponName(String couponName) {
        this.couponName = couponName;
    }

    /**
     * Gets the discount amount.
     * 
     * @return The discount amount
     */
    public BigDecimal getDiscountAmount() {
        return discountAmount;
    }

    /**
     * Sets the discount amount.
     * 
     * @param discountAmount The discount amount to set
     */
    public void setDiscountAmount(BigDecimal discountAmount) {
        this.discountAmount = discountAmount;
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
     * Checks if the coupon is used.
     * 
     * @return true if used, false otherwise
     */
    public boolean isUsed() {
        return isUsed;
    }

    /**
     * Sets the used status.
     * 
     * @param used The used status to set
     */
    public void setUsed(boolean used) {
        isUsed = used;
    }

    /**
     * Gets the expiry date.
     * 
     * @return The expiry date
     */
    public LocalDateTime getExpiryDate() {
        return expiryDate;
    }

    /**
     * Sets the expiry date.
     * 
     * @param expiryDate The expiry date to set
     */
    public void setExpiryDate(LocalDateTime expiryDate) {
        this.expiryDate = expiryDate;
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
