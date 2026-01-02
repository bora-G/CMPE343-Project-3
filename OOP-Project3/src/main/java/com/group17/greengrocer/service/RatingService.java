package com.group17.greengrocer.service;

import com.group17.greengrocer.repository.RatingRepository;
import java.sql.SQLException;

/**
 * Service class for carrier rating business logic.
 */
public class RatingService {
    private final RatingRepository ratingRepository;
    
    public RatingService() {
        this.ratingRepository = new RatingRepository();
    }
    
    /**
     * Rate a carrier for an order
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

