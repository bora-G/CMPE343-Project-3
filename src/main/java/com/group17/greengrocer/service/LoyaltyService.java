package com.group17.greengrocer.service;

import com.group17.greengrocer.repository.OrderRepository;
import java.math.BigDecimal;
import java.sql.SQLException;

/**
 * Service class for loyalty discount business logic.
 * Loyalty discount: Configurable discount percentage for customers with configurable completed orders threshold
 */
public class LoyaltyService {
    private final OrderRepository orderRepository;
    
    private static int loyaltyThreshold = 5;
    private static BigDecimal loyaltyDiscountPercent = new BigDecimal("5.00");
    
    /**
     * Constructor for LoyaltyService.
     */
    public LoyaltyService() {
        this.orderRepository = new OrderRepository();
    }
    
    /**
     * Get completed orders count for a customer
     * @param customerId The customer ID
     * @return Number of completed orders
     */
    public int getCompletedOrdersCount(int customerId) {
        try {
            return orderRepository.getCompletedOrdersCountByCustomer(customerId);
        } catch (SQLException e) {
            System.err.println("Error fetching completed orders count: " + e.getMessage());
            e.printStackTrace();
            return 0;
        }
    }
    
    /**
     * Check if customer is eligible for loyalty discount
     * Rule: Customer must have threshold or more completed orders
     * @param customerId The customer ID
     * @return true if eligible
     */
    public boolean isEligibleForLoyaltyDiscount(int customerId) {
        return getCompletedOrdersCount(customerId) >= loyaltyThreshold;
    }
    
    /**
     * Get current loyalty threshold.
     * @return The number of completed orders required for loyalty discount
     */
    public static int getLoyaltyThreshold() {
        return loyaltyThreshold;
    }
    
    /**
     * Set loyalty threshold.
     * @param threshold The number of completed orders required for loyalty discount
     * @throws IllegalArgumentException if threshold is negative
     */
    public static void setLoyaltyThreshold(int threshold) {
        if (threshold < 0) {
            throw new IllegalArgumentException("Loyalty threshold cannot be negative");
        }
        loyaltyThreshold = threshold;
    }
    
    /**
     * Get current loyalty discount percentage.
     * @return The loyalty discount percentage
     */
    public static BigDecimal getLoyaltyDiscountPercent() {
        return loyaltyDiscountPercent;
    }
    
    /**
     * Set loyalty discount percentage.
     * @param percent The loyalty discount percentage (0-100)
     * @throws IllegalArgumentException if percent is null, negative, or greater than 100
     */
    public static void setLoyaltyDiscountPercent(BigDecimal percent) {
        if (percent == null || percent.compareTo(BigDecimal.ZERO) < 0 || percent.compareTo(new BigDecimal("100")) > 0) {
            throw new IllegalArgumentException("Loyalty discount percent must be between 0 and 100");
        }
        loyaltyDiscountPercent = percent;
    }
}

