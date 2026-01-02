package com.group17.greengrocer.service;

import com.group17.greengrocer.repository.CouponRepository;
import java.math.BigDecimal;
import java.sql.SQLException;

/**
 * Service class for coupon business logic.
 */
public class CouponService {
    private final CouponRepository couponRepository;
    
    public CouponService() {
        this.couponRepository = new CouponRepository();
    }
    
    /**
     * Apply a coupon code and return discount amount
     * @param couponCode The coupon code
     * @param customerId The customer ID
     * @param subtotal The order subtotal
     * @return Discount amount, or null if invalid
     */
    public BigDecimal applyCoupon(String couponCode, int customerId, BigDecimal subtotal) {
        try {
            return couponRepository.applyCoupon(couponCode, customerId, subtotal);
        } catch (SQLException e) {
            System.err.println("Error applying coupon: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * Mark coupon as used
     */
    public boolean markCouponAsUsed(String couponCode, int customerId) {
        try {
            return couponRepository.markAsUsed(couponCode, customerId);
        } catch (SQLException e) {
            System.err.println("Error marking coupon as used: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Create a coupon for a customer (e.g., after purchase)
     */
    public boolean createCoupon(int customerId, String couponCode, BigDecimal discountAmount, BigDecimal discountPercent) {
        try {
            return couponRepository.create(customerId, couponCode, discountAmount, discountPercent);
        } catch (SQLException e) {
            System.err.println("Error creating coupon: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
}

