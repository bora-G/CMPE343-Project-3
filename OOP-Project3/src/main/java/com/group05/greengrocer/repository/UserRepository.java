package com.group05.greengrocer.repository;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import com.group05.greengrocer.model.User;
import com.group05.greengrocer.util.DatabaseAdapter;
import com.group05.greengrocer.util.PasswordUtil;

/**
 * Repository class for User database operations.
 * Handles all database access related to users.
 */
public class UserRepository {
    private final DatabaseAdapter dbAdapter;

    /**
     * Constructor for UserRepository.
     */
    public UserRepository() {
        this.dbAdapter = DatabaseAdapter.getInstance();
    }

    /**
     * Authenticate user by username and password.
     * Compares the provided password with the stored hash using SHA-256.
     * Supports both hashed passwords (new) and plain text (legacy migration).
     * 
     * @param username The username to authenticate
     * @param password The plain text password to verify
     * @return The authenticated User object, or null if authentication fails
     * @throws SQLException if database access error occurs
     */
    public User authenticate(String username, String password) throws SQLException {
        String sql = "SELECT * FROM UserInfo WHERE username = ? AND isActive = TRUE";

        try (Connection conn = dbAdapter.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    User user = mapResultSetToUser(rs);
                    String storedHash = user.getPassword();

                    if (PasswordUtil.isHashed(storedHash)) {
                        if (PasswordUtil.verifyPassword(password, storedHash)) {
                            return user;
                        }
                    } else {
                        if (storedHash.equals(password)) {
                            user.setPassword(PasswordUtil.hashPassword(password));
                            update(user);
                            return user;
                        }
                    }
                }
            }
        }
        return null;
    }

    /**
     * Find user by username.
     * 
     * @param username The username to search for
     * @return The User object if found, null otherwise
     * @throws SQLException if database access error occurs
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
     * Find user by ID.
     * 
     * @param userId The user ID to search for
     * @return The User object if found, null otherwise
     * @throws SQLException if database access error occurs
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
     * Get all users by role.
     * 
     * @param role The role to filter by (Customer, Carrier, or Owner)
     * @return List of User objects with the specified role
     * @throws SQLException if database access error occurs
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
     * Get all carriers.
     * 
     * @return List of all active Carrier users
     * @throws SQLException if database access error occurs
     */
    public List<User> getAllCarriers() throws SQLException {
        return findByRole("Carrier");
    }

    /**
     * Get all customers.
     * 
     * @return List of all active Customer users
     * @throws SQLException if database access error occurs
     */
    public List<User> getAllCustomers() throws SQLException {
        return findByRole("Customer");
    }

    /**
     * Create a new user.
     * Automatically hashes the password before storing it.
     * 
     * @param user The User object to create
     * @return true if user was created successfully, false otherwise
     * @throws SQLException if database access error occurs
     */
    public boolean create(User user) throws SQLException {
        String sql = "INSERT INTO UserInfo (username, password, role, fullName, email, phone, address, isActive) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = dbAdapter.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            String passwordToStore = user.getPassword();
            if (!PasswordUtil.isHashed(passwordToStore)) {
                passwordToStore = PasswordUtil.hashPassword(passwordToStore);
            }

            stmt.setString(1, user.getUsername());
            stmt.setString(2, passwordToStore);
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
                user.setPassword(passwordToStore);
                return true;
            }
        }
        return false;
    }

    /**
     * Update user information.
     * Automatically hashes the password if it's a new plain text password.
     * 
     * @param user The User object with updated information
     * @return true if user was updated successfully, false otherwise
     * @throws SQLException if database access error occurs
     */
    public boolean update(User user) throws SQLException {
        String sql = "UPDATE UserInfo SET username = ?, password = ?, role = ?, fullName = ?, " +
                "email = ?, phone = ?, address = ?, isActive = ? WHERE userId = ?";

        try (Connection conn = dbAdapter.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            String passwordToStore = user.getPassword();
            if (!PasswordUtil.isHashed(passwordToStore)) {
                passwordToStore = PasswordUtil.hashPassword(passwordToStore);
                user.setPassword(passwordToStore);
            }

            stmt.setString(1, user.getUsername());
            stmt.setString(2, passwordToStore);
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
     * Delete user (soft delete by setting isActive to false).
     * 
     * @param userId The ID of the user to delete
     * @return true if user was deleted successfully, false otherwise
     * @throws SQLException if database access error occurs
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
     * Map ResultSet to User object.
     * 
     * @param rs The ResultSet containing user data
     * @return The mapped User object
     * @throws SQLException if database access error occurs
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
