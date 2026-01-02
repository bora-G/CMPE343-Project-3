package com.group17.greengrocer.util;

import com.group17.greengrocer.model.User;

/**
 * Session class for managing current user session.
 * Singleton pattern to maintain user state throughout the application.
 */
public class Session {
    private static Session instance;
    private User currentUser;
    
    /**
     * Private constructor for singleton pattern.
     */
    private Session() {
    }
    
    /**
     * Get singleton instance of Session.
     * @return The Session instance
     */
    public static synchronized Session getInstance() {
        if (instance == null) {
            instance = new Session();
        }
        return instance;
    }
    
    /**
     * Set current logged-in user.
     * @param user The user to set as current user
     */
    public void setCurrentUser(User user) {
        this.currentUser = user;
    }
    
    /**
     * Get current logged-in user.
     * @return The current user, or null if not logged in
     */
    public User getCurrentUser() {
        return currentUser;
    }
    
    /**
     * Check if user is logged in.
     * @return true if user is logged in, false otherwise
     */
    public boolean isLoggedIn() {
        return currentUser != null;
    }
    
    /**
     * Check if current user has a specific role.
     * @param role The role to check (Customer, Carrier, or Owner)
     * @return true if user has the specified role, false otherwise
     */
    public boolean hasRole(String role) {
        return currentUser != null && currentUser.getRole().equals(role);
    }
    
    /**
     * Clear current session (logout).
     */
    public void clear() {
        this.currentUser = null;
    }
    
    /**
     * Get current user ID.
     * @return The user ID, or -1 if not logged in
     */
    public int getCurrentUserId() {
        return currentUser != null ? currentUser.getUserId() : -1;
    }
}
