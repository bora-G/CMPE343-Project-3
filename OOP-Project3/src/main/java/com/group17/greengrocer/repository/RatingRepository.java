package com.group17.greengrocer.repository;

import com.group17.greengrocer.model.CarrierRating;
import com.group17.greengrocer.util.DatabaseAdapter;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Repository class for CarrierRating database operations.
 */
public class RatingRepository {
    private final DatabaseAdapter dbAdapter;
    
    /**
     * Constructor for RatingRepository.
     */
    public RatingRepository() {
        this.dbAdapter = DatabaseAdapter.getInstance();
    }
    
    /**
     * Create a new rating.
     * @param orderId The order ID associated with the rating
     * @param carrierId The carrier ID being rated
     * @param customerId The customer ID giving the rating
     * @param rating The rating value (1-5)
     * @param comment The optional comment
     * @return true if rating was created successfully, false otherwise
     * @throws SQLException if database access error occurs
     */
    public boolean create(int orderId, int carrierId, int customerId, int rating, String comment) throws SQLException {
        String sql = "INSERT INTO CarrierRating (orderId, carrierId, customerId, rating, comment) " +
                     "VALUES (?, ?, ?, ?, ?)";
        
        try (Connection conn = dbAdapter.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, orderId);
            stmt.setInt(2, carrierId);
            stmt.setInt(3, customerId);
            stmt.setInt(4, rating);
            stmt.setString(5, comment);
            
            return stmt.executeUpdate() > 0;
        }
    }
    
    /**
     * Get all ratings.
     * @return List of all carrier ratings, sorted by creation date descending
     * @throws SQLException if database access error occurs
     */
    public List<CarrierRating> findAll() throws SQLException {
        String sql = "SELECT * FROM CarrierRating ORDER BY createdAt DESC";
        List<CarrierRating> ratings = new ArrayList<>();
        
        try (Connection conn = dbAdapter.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                ratings.add(mapResultSetToRating(rs));
            }
        }
        return ratings;
    }
    
    /**
     * Map ResultSet to CarrierRating object.
     * @param rs The ResultSet containing rating data
     * @return The mapped CarrierRating object
     * @throws SQLException if database access error occurs
     */
    private CarrierRating mapResultSetToRating(ResultSet rs) throws SQLException {
        CarrierRating rating = new CarrierRating();
        rating.setRatingId(rs.getInt("ratingId"));
        rating.setOrderId(rs.getInt("orderId"));
        rating.setCarrierId(rs.getInt("carrierId"));
        rating.setCustomerId(rs.getInt("customerId"));
        rating.setRating(rs.getInt("rating"));
        rating.setComment(rs.getString("comment"));
        Timestamp createdAt = rs.getTimestamp("createdAt");
        if (createdAt != null) {
            rating.setCreatedAt(createdAt.toLocalDateTime());
        }
        return rating;
    }
}

