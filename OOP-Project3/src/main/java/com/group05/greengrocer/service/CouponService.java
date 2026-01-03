package com.group05.greengrocer.service;

import java.math.BigDecimal;
import java.sql.SQLException;

import com.group05.greengrocer.repository.CouponRepository;

/**
 * Service class for coupon business logic.
 */
public class CouponService {
    private final CouponRepository couponRepository;

    /**
     * Constructor for CouponService.
     */
    public CouponService() {
        this.couponRepository = new CouponRepository();
    }

    /**
     * Apply a coupon code and return discount amount
     * 
     * @param couponCode The coupon code
     * @param customerId The customer ID
     * @param subtotal   The order subtotal
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
     * Mark coupon as used.
     * 
     * @param couponCode The coupon code to mark as used
     * @param customerId The customer ID to verify ownership
     * @return true if coupon was marked as used successfully, false otherwise
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
     * Create a coupon for a customer (e.g., after purchase).
     * 
     * @param customerId      The customer ID to assign the coupon to
     * @param couponCode      The unique coupon code
     * @param discountAmount  The fixed discount amount
     * @param discountPercent The percentage discount
     * @return true if coupon was created successfully, false otherwise
     */
    public boolean createCoupon(int customerId, String couponCode, BigDecimal discountAmount,
            BigDecimal discountPercent) {
        return createCoupon(customerId, couponCode, discountAmount, discountPercent, null);
    }

    /**
     * Create a coupon for a customer with optional name.
     * 
     * @param customerId      The customer ID to assign the coupon to
     * @param couponCode      The unique coupon code
     * @param discountAmount  The fixed discount amount
     * @param discountPercent The percentage discount
     * @param couponName      The optional coupon name
     * @return true if coupon was created successfully, false otherwise
     */
    public boolean createCoupon(int customerId, String couponCode, BigDecimal discountAmount,
            BigDecimal discountPercent, String couponName) {
        try {
            return couponRepository.create(customerId, couponCode, discountAmount, discountPercent, couponName);
        } catch (SQLException e) {
            System.err.println("Error creating coupon: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
}
