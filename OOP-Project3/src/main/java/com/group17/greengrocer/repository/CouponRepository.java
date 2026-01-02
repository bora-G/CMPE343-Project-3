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
    
    /**
     * Constructor for CouponRepository.
     */
    public CouponRepository() {
        this.dbAdapter = DatabaseAdapter.getInstance();
    }
    
    /**
     * Apply a coupon code and return discount amount.
     * @param couponCode The coupon code to apply
     * @param customerId The customer ID to verify ownership
     * @param subtotal The order subtotal to calculate percentage discount
     * @return The discount amount, or null if coupon is invalid or expired
     * @throws SQLException if database access error occurs
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
     * Mark coupon as used.
     * @param couponCode The coupon code to mark as used
     * @param customerId The customer ID to verify ownership
     * @return true if coupon was marked as used successfully, false otherwise
     * @throws SQLException if database access error occurs
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
     * Create a new coupon (legacy method without couponName).
     * @param customerId The customer ID to assign the coupon to
     * @param couponCode The unique coupon code
     * @param discountAmount The fixed discount amount
     * @param discountPercent The percentage discount
     * @return true if coupon was created successfully, false otherwise
     * @throws SQLException if database access error occurs
     */
    public boolean create(int customerId, String couponCode, BigDecimal discountAmount, BigDecimal discountPercent) throws SQLException {
        return create(customerId, couponCode, discountAmount, discountPercent, null);
    }
    
    /**
     * Create a new coupon with optional name.
     * @param customerId The customer ID to assign the coupon to
     * @param couponCode The unique coupon code
     * @param discountAmount The fixed discount amount
     * @param discountPercent The percentage discount
     * @param couponName The optional coupon name
     * @return true if coupon was created successfully, false otherwise
     * @throws SQLException if database access error occurs
     */
    public boolean create(int customerId, String couponCode, BigDecimal discountAmount, BigDecimal discountPercent, String couponName) throws SQLException {
        boolean hasCouponName = false;
        try {
            try (Connection checkConn = dbAdapter.getConnection();
                 Statement checkStmt = checkConn.createStatement()) {
                checkStmt.executeQuery("SELECT couponName FROM Coupon LIMIT 1");
                hasCouponName = true;
            }
        } catch (SQLException e) {
            hasCouponName = false;
        }
        
        String sql;
        if (hasCouponName) {
            sql = "INSERT INTO Coupon (customerId, couponCode, discountAmount, discountPercent, couponName, isUsed) " +
                 "VALUES (?, ?, ?, ?, ?, FALSE)";
        } else {
            sql = "INSERT INTO Coupon (customerId, couponCode, discountAmount, discountPercent, isUsed) " +
                 "VALUES (?, ?, ?, ?, FALSE)";
        }
        
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
            
            if (hasCouponName) {
                stmt.setString(5, couponName);
            }
            
            return stmt.executeUpdate() > 0;
        }
    }
    
    /**
     * Get all coupons.
     * @return List of all coupons, sorted by creation date descending
     * @throws SQLException if database access error occurs
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
     * Check if coupon code is unique.
     * @param couponCode The coupon code to check
     * @return true if coupon code is unique, false otherwise
     * @throws SQLException if database access error occurs
     */
    public boolean isCodeUnique(String couponCode) throws SQLException {
        String sql = "SELECT COUNT(*) FROM Coupon WHERE couponCode = ?";
        
        try (Connection conn = dbAdapter.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, couponCode);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) == 0;
                }
            }
        }
        return true;
    }
    
    /**
     * Map ResultSet to Coupon object.
     * @param rs The ResultSet containing coupon data
     * @return The mapped Coupon object
     * @throws SQLException if database access error occurs
     */
    private Coupon mapResultSetToCoupon(ResultSet rs) throws SQLException {
        Coupon coupon = new Coupon();
        coupon.setCouponId(rs.getInt("couponId"));
        coupon.setCustomerId(rs.getInt("customerId"));
        coupon.setCouponCode(rs.getString("couponCode"));
        coupon.setDiscountAmount(rs.getBigDecimal("discountAmount"));
        coupon.setDiscountPercent(rs.getBigDecimal("discountPercent"));
        coupon.setUsed(rs.getBoolean("isUsed"));
        
        try {
            coupon.setCouponName(rs.getString("couponName"));
        } catch (SQLException e) {
            coupon.setCouponName(null);
        }
        
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

