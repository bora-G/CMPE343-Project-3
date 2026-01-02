package com.group17.greengrocer.service;

import com.group17.greengrocer.model.User;
import com.group17.greengrocer.repository.UserRepository;
import com.group17.greengrocer.util.Session;
import com.group17.greengrocer.util.Validation;

import java.sql.SQLException;

/**
 * Service class for authentication business logic.
 */
public class AuthService {
    private final UserRepository userRepository;
    private final Session session;
    
    public AuthService() {
        this.userRepository = new UserRepository();
        this.session = Session.getInstance();
    }
    
    /**
     * Authenticate user login
     * @param username Username
     * @param password Password
     * @return true if authentication successful
     */
    public boolean login(String username, String password) {
        if (!Validation.isNotEmpty(username) || !Validation.isNotEmpty(password)) {
            return false;
        }
        
        try {
            User user = userRepository.authenticate(username, password);
            if (user != null) {
                session.setCurrentUser(user);
                return true;
            }
        } catch (SQLException e) {
            System.err.println("Error during authentication: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }
    
    /**
     * Logout current user
     */
    public void logout() {
        session.clear();
    }
    
    /**
     * Get current logged-in user
     */
    public User getCurrentUser() {
        return session.getCurrentUser();
    }
    
    /**
     * Check if user is logged in
     */
    public boolean isLoggedIn() {
        return session.isLoggedIn();
    }
    
    /**
     * Check if current user has specific role
     */
    public boolean hasRole(String role) {
        return session.hasRole(role);
    }
    
    /**
     * Register a new customer
     * Validates unique username and strong password requirements
     * @param username Username (must be unique)
     * @param password Password (must be strong: 8+ chars, uppercase, lowercase, digit)
     * @param fullName Full name
     * @param email Email (optional)
     * @param phone Phone (optional)
     * @param address Address (optional)
     * @return Registration result message (success or error)
     */
    public String registerCustomer(String username, String password, String fullName, 
                                   String email, String phone, String address) {
        // Validate username
        if (!Validation.isValidUsername(username)) {
            return "Username must be 3-50 characters and contain only letters, numbers, and underscores.";
        }
        
        // Check if username already exists
        try {
            User existingUser = userRepository.findByUsername(username);
            if (existingUser != null) {
                return "Username already exists. Please choose a different username.";
            }
        } catch (SQLException e) {
            System.err.println("Error checking username: " + e.getMessage());
            return "Error checking username availability.";
        }
        
        // Validate strong password
        if (!Validation.isStrongPassword(password)) {
            return "Password must be at least 8 characters and contain uppercase, lowercase, and a digit.";
        }
        
        // Validate full name
        if (!Validation.isValidFullName(fullName)) {
            return "Full name must contain only letters and spaces (no numbers).";
        }
        
        // Validate email if provided
        if (email != null && !email.trim().isEmpty()) {
            if (!Validation.isValidEmailFormat(email)) {
                return "Invalid email format.";
            }
        }
        
        // Validate phone if provided
        if (phone != null && !phone.trim().isEmpty()) {
            if (!Validation.isValidPhone(phone)) {
                return "Phone must be exactly 10 digits starting with 5 (e.g., 5372440233).";
            }
        }
        
        // Create new customer user
        // Note: Password will be automatically hashed by UserRepository.create()
        User newUser = new User();
        newUser.setUsername(username.trim());
        newUser.setPassword(password); // Will be hashed in repository
        newUser.setRole("Customer");
        newUser.setFullName(fullName.trim());
        newUser.setEmail(email != null ? email.trim() : null);
        newUser.setPhone(phone != null ? phone.trim() : null);
        newUser.setAddress(address != null ? address.trim() : null);
        newUser.setActive(true);
        
        // Save to database (password will be hashed automatically)
        try {
            if (userRepository.create(newUser)) {
                return "SUCCESS"; // Special success marker
            } else {
                return "Failed to create account. Please try again.";
            }
        } catch (SQLException e) {
            System.err.println("Error creating user: " + e.getMessage());
            e.printStackTrace();
            return "Database error. Please try again.";
        }
    }
    
    /**
     * Update customer profile information
     * @param fullName Full name
     * @param email Email
     * @param phone Phone
     * @param address Address
     * @return Update result message (success or error)
     */
    public String updateProfile(String fullName, String email, String phone, String address) {
        User currentUser = session.getCurrentUser();
        if (currentUser == null) {
            return "No user logged in.";
        }
        
        // Validate full name
        if (!Validation.isValidFullName(fullName)) {
            return "Full name must contain only letters and spaces (no numbers).";
        }
        
        // Validate email if provided
        if (email != null && !email.trim().isEmpty()) {
            if (!Validation.isValidEmailFormat(email)) {
                return "Invalid email format.";
            }
        }
        
        // Validate phone if provided
        if (phone != null && !phone.trim().isEmpty()) {
            if (!Validation.isValidPhone(phone)) {
                return "Phone must be exactly 10 digits starting with 5 (e.g., 5372440233).";
            }
        }
        
        // Update user information
        currentUser.setFullName(fullName.trim());
        currentUser.setEmail(email != null ? email.trim() : null);
        currentUser.setPhone(phone != null ? phone.trim() : null);
        currentUser.setAddress(address != null ? address.trim() : null);
        
        try {
            if (userRepository.update(currentUser)) {
                session.setCurrentUser(currentUser); // Update session
                return "SUCCESS"; // Special success marker
            } else {
                return "Failed to update profile. Please try again.";
            }
        } catch (SQLException e) {
            System.err.println("Error updating profile: " + e.getMessage());
            e.printStackTrace();
            return "Database error. Please try again.";
        }
    }
}




