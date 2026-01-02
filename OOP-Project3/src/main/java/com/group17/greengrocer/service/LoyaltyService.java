package com.group17.greengrocer.service;

import com.group17.greengrocer.repository.OrderRepository;
import java.sql.SQLException;

/**
 * Service class for loyalty discount business logic.
 * Loyalty discount: 5% discount for customers with 5+ completed orders
 */
public class LoyaltyService {
    private final OrderRepository orderRepository;
    
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
     * Rule: Customer must have 5 or more completed orders
     * @param customerId The customer ID
     * @return true if eligible
     */
    public boolean isEligibleForLoyaltyDiscount(int customerId) {
        return getCompletedOrdersCount(customerId) >= 5;
    }
}

