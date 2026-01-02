package com.group17.greengrocer.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Utility class for password hashing and verification using SHA-256.
 * Provides secure password hashing and verification functionality.
 */
public class PasswordUtil {
    
    /**
     * Hash a plain text password using SHA-256.
     * @param plainPassword The plain text password to hash
     * @return The hashed password (SHA-256 hash as hexadecimal string, 64 characters)
     */
    public static String hashPassword(String plainPassword) {
        if (plainPassword == null || plainPassword.isEmpty()) {
            throw new IllegalArgumentException("Password cannot be null or empty");
        }
        
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(plainPassword.getBytes());
            
            // Convert byte array to hexadecimal string
            StringBuilder hexString = new StringBuilder();
            for (byte b : hashBytes) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not available", e);
        }
    }
    
    /**
     * Verify a plain text password against a SHA-256 hash.
     * @param plainPassword The plain text password to verify
     * @param hashedPassword The SHA-256 hash to verify against (hexadecimal string)
     * @return true if the password matches the hash, false otherwise
     */
    public static boolean verifyPassword(String plainPassword, String hashedPassword) {
        if (plainPassword == null || hashedPassword == null) {
            return false;
        }
        
        try {
            String computedHash = hashPassword(plainPassword);
            return computedHash.equalsIgnoreCase(hashedPassword);
        } catch (Exception e) {
            // If hashing fails, return false
            return false;
        }
    }
    
    /**
     * Check if a string is already a SHA-256 hash.
     * SHA-256 hashes are 64 characters long hexadecimal strings.
     * @param password The string to check
     * @return true if the string appears to be a SHA-256 hash
     */
    public static boolean isHashed(String password) {
        if (password == null || password.length() != 64) {
            return false;
        }
        // Check if it's a valid hexadecimal string (64 characters)
        return password.matches("^[0-9a-fA-F]{64}$");
    }
}

