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
     * Private constructor for singleton pattern
     */
    private Session() {
    }
    
    /**
     * Get singleton instance of Session
     */
    public static synchronized Session getInstance() {
        if (instance == null) {
            instance = new Session();
        }
        return instance;
    }
    
    /**
     * Set current logged-in user
     */
    public void setCurrentUser(User user) {
        this.currentUser = user;
    }
    
    /**
     * Get current logged-in user
     */
    public User getCurrentUser() {
        return currentUser;
    }
    
    /**
     * Check if user is logged in
     */
    public boolean isLoggedIn() {
        return currentUser != null;
    }
    
    /**
     * Check if current user has a specific role
     */
    public boolean hasRole(String role) {
        return currentUser != null && currentUser.getRole().equals(role);
    }
    
    /**
     * Clear current session (logout)
     */
    public void clear() {
        this.currentUser = null;
    }
    
    /**
     * Get current user ID
     */
    public int getCurrentUserId() {
        return currentUser != null ? currentUser.getUserId() : -1;
    }
}








