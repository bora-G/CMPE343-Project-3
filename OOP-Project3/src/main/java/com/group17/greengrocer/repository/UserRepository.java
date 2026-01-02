package com.group17.greengrocer.repository;

import com.group17.greengrocer.model.User;
import com.group17.greengrocer.util.DatabaseAdapter;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Repository class for User database operations.
 * Handles all database access related to users.
 */
public class UserRepository {
    private final DatabaseAdapter dbAdapter;
    
    public UserRepository() {
        this.dbAdapter = DatabaseAdapter.getInstance();
    }
    
    /**
     * Authenticate user by username and password
     */
    public User authenticate(String username, String password) throws SQLException {
        String sql = "SELECT * FROM UserInfo WHERE username = ? AND password = ? AND isActive = TRUE";
        
        try (Connection conn = dbAdapter.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, username);
            stmt.setString(2, password);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToUser(rs);
                }
            }
        }
        return null;
    }
    
    /**
     * Find user by username
     */
    public User findByUsername(String username) throws SQLException {
        String sql = "SELECT * FROM UserInfo WHERE username = ?";
        
        try (Connection conn = dbAdapter.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, username);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToUser(rs);
                }
            }
        }
        return null;
    }
    
    /**
     * Find user by ID
     */
    public User findById(int userId) throws SQLException {
        String sql = "SELECT * FROM UserInfo WHERE userId = ?";
        
        try (Connection conn = dbAdapter.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, userId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToUser(rs);
                }
            }
        }
        return null;
    }
    
    /**
     * Get all users by role
     */
    public List<User> findByRole(String role) throws SQLException {
        String sql = "SELECT * FROM UserInfo WHERE role = ? AND isActive = TRUE";
        List<User> users = new ArrayList<>();
        
        try (Connection conn = dbAdapter.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, role);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    users.add(mapResultSetToUser(rs));
                }
            }
        }
        return users;
    }
    
    /**
     * Get all carriers
     */
    public List<User> getAllCarriers() throws SQLException {
        return findByRole("Carrier");
    }
    
    /**
     * Get all customers
     */
    public List<User> getAllCustomers() throws SQLException {
        return findByRole("Customer");
    }
    
    /**
     * Create a new user
     */
    public boolean create(User user) throws SQLException {
        String sql = "INSERT INTO UserInfo (username, password, role, fullName, email, phone, address, isActive) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = dbAdapter.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            stmt.setString(1, user.getUsername());
            stmt.setString(2, user.getPassword());
            stmt.setString(3, user.getRole());
            stmt.setString(4, user.getFullName());
            stmt.setString(5, user.getEmail());
            stmt.setString(6, user.getPhone());
            stmt.setString(7, user.getAddress());
            stmt.setBoolean(8, user.isActive());
            
            int rowsAffected = stmt.executeUpdate();
            
            if (rowsAffected > 0) {
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        user.setUserId(generatedKeys.getInt(1));
                    }
                }
                return true;
            }
        }
        return false;
    }
    
    /**
     * Update user information
     */
    public boolean update(User user) throws SQLException {
        String sql = "UPDATE UserInfo SET username = ?, password = ?, role = ?, fullName = ?, " +
                     "email = ?, phone = ?, address = ?, isActive = ? WHERE userId = ?";
        
        try (Connection conn = dbAdapter.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, user.getUsername());
            stmt.setString(2, user.getPassword());
            stmt.setString(3, user.getRole());
            stmt.setString(4, user.getFullName());
            stmt.setString(5, user.getEmail());
            stmt.setString(6, user.getPhone());
            stmt.setString(7, user.getAddress());
            stmt.setBoolean(8, user.isActive());
            stmt.setInt(9, user.getUserId());
            
            return stmt.executeUpdate() > 0;
        }
    }
    
    /**
     * Delete user (soft delete by setting isActive to false)
     */
    public boolean delete(int userId) throws SQLException {
        String sql = "UPDATE UserInfo SET isActive = FALSE WHERE userId = ?";
        
        try (Connection conn = dbAdapter.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, userId);
            return stmt.executeUpdate() > 0;
        }
    }
    
    /**
     * Map ResultSet to User object
     */
    private User mapResultSetToUser(ResultSet rs) throws SQLException {
        User user = new User();
        user.setUserId(rs.getInt("userId"));
        user.setUsername(rs.getString("username"));
        user.setPassword(rs.getString("password"));
        user.setRole(rs.getString("role"));
        user.setFullName(rs.getString("fullName"));
        user.setEmail(rs.getString("email"));
        user.setPhone(rs.getString("phone"));
        user.setAddress(rs.getString("address"));
        user.setActive(rs.getBoolean("isActive"));
        return user;
    }
}








