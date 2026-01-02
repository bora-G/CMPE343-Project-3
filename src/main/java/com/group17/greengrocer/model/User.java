package com.group17.greengrocer.model;

/**
 * User model class representing a user in the system.
 * Can be a Customer, Carrier, or Owner.
 */
public class User {
    private int userId;
    private String username;
    private String password;
    private String role;
    private String fullName;
    private String email;
    private String phone;
    private String address;
    private boolean isActive;
    
    /**
     * Default constructor for User.
     */
    public User() {
    }
    
    /**
     * Constructor for User with basic information.
     * @param username The username for login
     * @param password The hashed password
     * @param role The user role (Customer, Carrier, or Owner)
     * @param fullName The full name of the user
     */
    public User(String username, String password, String role, String fullName) {
        this.username = username;
        this.password = password;
        this.role = role;
        this.fullName = fullName;
        this.isActive = true;
    }
    
    /**
     * Gets the user ID.
     * @return The user ID
     */
    public int getUserId() {
        return userId;
    }
    
    /**
     * Sets the user ID.
     * @param userId The user ID to set
     */
    public void setUserId(int userId) {
        this.userId = userId;
    }
    
    /**
     * Gets the username.
     * @return The username
     */
    public String getUsername() {
        return username;
    }
    
    /**
     * Sets the username.
     * @param username The username to set
     */
    public void setUsername(String username) {
        this.username = username;
    }
    
    /**
     * Gets the password hash.
     * @return The hashed password
     */
    public String getPassword() {
        return password;
    }
    
    /**
     * Sets the password hash.
     * @param password The hashed password to set
     */
    public void setPassword(String password) {
        this.password = password;
    }
    
    /**
     * Gets the user role.
     * @return The role (Customer, Carrier, or Owner)
     */
    public String getRole() {
        return role;
    }
    
    /**
     * Sets the user role.
     * @param role The role to set
     */
    public void setRole(String role) {
        this.role = role;
    }
    
    /**
     * Gets the full name.
     * @return The full name
     */
    public String getFullName() {
        return fullName;
    }
    
    /**
     * Sets the full name.
     * @param fullName The full name to set
     */
    public void setFullName(String fullName) {
        this.fullName = fullName;
    }
    
    /**
     * Gets the email address.
     * @return The email address
     */
    public String getEmail() {
        return email;
    }
    
    /**
     * Sets the email address.
     * @param email The email address to set
     */
    public void setEmail(String email) {
        this.email = email;
    }
    
    /**
     * Gets the phone number.
     * @return The phone number
     */
    public String getPhone() {
        return phone;
    }
    
    /**
     * Sets the phone number.
     * @param phone The phone number to set
     */
    public void setPhone(String phone) {
        this.phone = phone;
    }
    
    /**
     * Gets the address.
     * @return The address
     */
    public String getAddress() {
        return address;
    }
    
    /**
     * Sets the address.
     * @param address The address to set
     */
    public void setAddress(String address) {
        this.address = address;
    }
    
    /**
     * Checks if the user is active.
     * @return true if active, false otherwise
     */
    public boolean isActive() {
        return isActive;
    }
    
    /**
     * Sets the active status.
     * @param active The active status to set
     */
    public void setActive(boolean active) {
        isActive = active;
    }
    
    @Override
    public String toString() {
        return "User{" +
                "userId=" + userId +
                ", username='" + username + '\'' +
                ", role='" + role + '\'' +
                ", fullName='" + fullName + '\'' +
                '}';
    }
}
