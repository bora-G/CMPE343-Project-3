package com.group17.greengrocer.service;

import com.group17.greengrocer.repository.RatingRepository;
import java.sql.SQLException;

/**
 * Service class for carrier rating business logic.
 */
public class RatingService {
    private final RatingRepository ratingRepository;
    
    /**
     * Constructor for RatingService.
     */
    public RatingService() {
        this.ratingRepository = new RatingRepository();
    }
    
    /**
     * Rate a carrier for an order.
     * @param orderId The order ID associated with the rating
     * @param carrierId The carrier ID being rated
     * @param customerId The customer ID giving the rating
     * @param rating The rating value (1-5)
     * @param comment The optional comment
     * @return true if rating was created successfully, false otherwise
     */
    public boolean rateCarrier(int orderId, int carrierId, int customerId, int rating, String comment) {
        try {
            return ratingRepository.create(orderId, carrierId, customerId, rating, comment);
        } catch (SQLException e) {
            System.err.println("Error rating carrier: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
}

