package com.group17.greengrocer.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Coupon model class representing a discount coupon.
 */
public class Coupon {
    private int couponId;
    private int customerId;
    private String couponCode;
    private BigDecimal discountAmount;
    private BigDecimal discountPercent;
    private boolean isUsed;
    private LocalDateTime expiryDate;
    private LocalDateTime createdAt;
    
    // Constructors
    public Coupon() {
    }
    
    // Getters and Setters
    public int getCouponId() {
        return couponId;
    }
    
    public void setCouponId(int couponId) {
        this.couponId = couponId;
    }
    
    public int getCustomerId() {
        return customerId;
    }
    
    public void setCustomerId(int customerId) {
        this.customerId = customerId;
    }
    
    public String getCouponCode() {
        return couponCode;
    }
    
    public void setCouponCode(String couponCode) {
        this.couponCode = couponCode;
    }
    
    public BigDecimal getDiscountAmount() {
        return discountAmount;
    }
    
    public void setDiscountAmount(BigDecimal discountAmount) {
        this.discountAmount = discountAmount;
    }
    
    public BigDecimal getDiscountPercent() {
        return discountPercent;
    }
    
    public void setDiscountPercent(BigDecimal discountPercent) {
        this.discountPercent = discountPercent;
    }
    
    public boolean isUsed() {
        return isUsed;
    }
    
    public void setUsed(boolean used) {
        isUsed = used;
    }
    
    public LocalDateTime getExpiryDate() {
        return expiryDate;
    }
    
    public void setExpiryDate(LocalDateTime expiryDate) {
        this.expiryDate = expiryDate;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}

