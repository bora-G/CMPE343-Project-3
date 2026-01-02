package com.group17.greengrocer.repository;

import com.group17.greengrocer.model.Coupon;
import com.group17.greengrocer.util.DatabaseAdapter;
import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Repository class for Coupon database operations.
 */
public class CouponRepository {
    private final DatabaseAdapter dbAdapter;
    
    public CouponRepository() {
        this.dbAdapter = DatabaseAdapter.getInstance();
    }
    
    /**
     * Apply a coupon code and return discount amount
     */
    public BigDecimal applyCoupon(String couponCode, int customerId, BigDecimal subtotal) throws SQLException {
        String sql = "SELECT * FROM Coupon WHERE couponCode = ? AND customerId = ? AND isUsed = FALSE " +
                     "AND (expiryDate IS NULL OR expiryDate > NOW())";
        
        try (Connection conn = dbAdapter.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, couponCode);
            stmt.setInt(2, customerId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    BigDecimal discountAmount = rs.getBigDecimal("discountAmount");
                    BigDecimal discountPercent = rs.getBigDecimal("discountPercent");
                    
                    if (discountAmount != null && discountAmount.compareTo(BigDecimal.ZERO) > 0) {
                        return discountAmount;
                    } else if (discountPercent != null && discountPercent.compareTo(BigDecimal.ZERO) > 0) {
                        return subtotal.multiply(discountPercent).divide(new BigDecimal("100"), 2, java.math.RoundingMode.HALF_UP);
                    }
                }
            }
        }
        return null;
    }
    
    /**
     * Mark coupon as used
     */
    public boolean markAsUsed(String couponCode, int customerId) throws SQLException {
        String sql = "UPDATE Coupon SET isUsed = TRUE WHERE couponCode = ? AND customerId = ?";
        
        try (Connection conn = dbAdapter.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, couponCode);
            stmt.setInt(2, customerId);
            
            return stmt.executeUpdate() > 0;
        }
    }
    
    /**
     * Create a new coupon
     */
    public boolean create(int customerId, String couponCode, BigDecimal discountAmount, BigDecimal discountPercent) throws SQLException {
        String sql = "INSERT INTO Coupon (customerId, couponCode, discountAmount, discountPercent, isUsed) " +
                     "VALUES (?, ?, ?, ?, FALSE)";
        
        try (Connection conn = dbAdapter.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, customerId);
            stmt.setString(2, couponCode);
            stmt.setBigDecimal(3, discountAmount);
            if (discountPercent != null) {
                stmt.setBigDecimal(4, discountPercent);
            } else {
                stmt.setNull(4, Types.DECIMAL);
            }
            
            return stmt.executeUpdate() > 0;
        }
    }
    
    /**
     * Get all coupons
     */
    public List<Coupon> findAll() throws SQLException {
        String sql = "SELECT * FROM Coupon ORDER BY createdAt DESC";
        List<Coupon> coupons = new ArrayList<>();
        
        try (Connection conn = dbAdapter.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                coupons.add(mapResultSetToCoupon(rs));
            }
        }
        return coupons;
    }
    
    /**
     * Check if coupon code is unique
     */
    public boolean isCodeUnique(String couponCode) throws SQLException {
        String sql = "SELECT COUNT(*) FROM Coupon WHERE couponCode = ?";
        
        try (Connection conn = dbAdapter.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, couponCode);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) == 0; // Return true if count is 0 (unique)
                }
            }
        }
        return true; // Assume unique if check fails
    }
    
    /**
     * Map ResultSet to Coupon object
     */
    private Coupon mapResultSetToCoupon(ResultSet rs) throws SQLException {
        Coupon coupon = new Coupon();
        coupon.setCouponId(rs.getInt("couponId"));
        coupon.setCustomerId(rs.getInt("customerId"));
        coupon.setCouponCode(rs.getString("couponCode"));
        coupon.setDiscountAmount(rs.getBigDecimal("discountAmount"));
        coupon.setDiscountPercent(rs.getBigDecimal("discountPercent"));
        coupon.setUsed(rs.getBoolean("isUsed"));
        Timestamp expiryDate = rs.getTimestamp("expiryDate");
        if (expiryDate != null) {
            coupon.setExpiryDate(expiryDate.toLocalDateTime());
        }
        Timestamp createdAt = rs.getTimestamp("createdAt");
        if (createdAt != null) {
            coupon.setCreatedAt(createdAt.toLocalDateTime());
        }
        return coupon;
    }
}

