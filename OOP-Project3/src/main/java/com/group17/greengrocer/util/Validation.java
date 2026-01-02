package com.group17.greengrocer.util;

import java.math.BigDecimal;
import java.util.regex.Pattern;

/**
 * Validation utility class for input validation.
 */
public class Validation {
    private static final Pattern EMAIL_PATTERN = 
        Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    
    /**
     * Validate email format
     */
    public static boolean isValidEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }
        return EMAIL_PATTERN.matcher(email).matches();
    }
    
    /**
     * Validate that a string is not null or empty
     */
    public static boolean isNotEmpty(String value) {
        return value != null && !value.trim().isEmpty();
    }
    
    /**
     * Validate that a number is positive
     */
    public static boolean isPositive(BigDecimal value) {
        return value != null && value.compareTo(BigDecimal.ZERO) > 0;
    }
    
    /**
     * Validate that a number is positive (double)
     */
    public static boolean isPositive(double value) {
        return value > 0;
    }
    
    /**
     * Validate that a number is non-negative
     */
    public static boolean isNonNegative(BigDecimal value) {
        return value != null && value.compareTo(BigDecimal.ZERO) >= 0;
    }
    
    /**
     * Validate quantity input (must be positive)
     */
    public static boolean isValidQuantity(String quantityStr) {
        try {
            double quantity = Double.parseDouble(quantityStr);
            return isPositive(quantity);
        } catch (NumberFormatException e) {
            return false;
        }
    }
    
    /**
     * Validate price input (must be non-negative)
     */
    public static boolean isValidPrice(String priceStr) {
        try {
            BigDecimal price = new BigDecimal(priceStr);
            return isNonNegative(price);
        } catch (NumberFormatException e) {
            return false;
        }
    }
    
    /**
     * Validate stock input (must be non-negative)
     */
    public static boolean isValidStock(String stockStr) {
        try {
            BigDecimal stock = new BigDecimal(stockStr);
            return isNonNegative(stock);
        } catch (NumberFormatException e) {
            return false;
        }
    }
    
    /**
     * Validate username (alphanumeric and underscore, 3-50 characters)
     */
    public static boolean isValidUsername(String username) {
        if (username == null || username.trim().isEmpty()) {
            return false;
        }
        return username.matches("^[a-zA-Z0-9_]{3,50}$");
    }
    
    /**
     * Validate password (at least 6 characters) - basic validation
     */
    public static boolean isValidPassword(String password) {
        return password != null && password.length() >= 6;
    }
    
    /**
     * Validate strong password requirements:
     * - At least 8 characters
     * - Contains at least one uppercase letter
     * - Contains at least one lowercase letter
     * - Contains at least one digit
     */
    public static boolean isStrongPassword(String password) {
        if (password == null || password.length() < 8) {
            return false;
        }
        
        boolean hasUpperCase = false;
        boolean hasLowerCase = false;
        boolean hasDigit = false;
        
        for (char c : password.toCharArray()) {
            if (Character.isUpperCase(c)) {
                hasUpperCase = true;
            } else if (Character.isLowerCase(c)) {
                hasLowerCase = true;
            } else if (Character.isDigit(c)) {
                hasDigit = true;
            }
        }
        
        return hasUpperCase && hasLowerCase && hasDigit;
    }
    
    /**
     * Validate full name (only letters and spaces, no numbers, at least 2 characters)
     * Allows Turkish characters (ç, ğ, ı, ö, ş, ü)
     */
    public static boolean isValidFullName(String fullName) {
        if (fullName == null || fullName.trim().isEmpty()) {
            return false;
        }
        String trimmed = fullName.trim();
        // Check length to prevent overflow (max 100 characters as per database)
        if (trimmed.length() > 100) {
            return false;
        }
        // Only letters (including Turkish characters), spaces, and hyphens allowed
        // Must have at least one letter
        return trimmed.matches("^[a-zA-ZçğıöşüÇĞIİÖŞÜ\\s-]+$") && 
               trimmed.matches(".*[a-zA-ZçğıöşüÇĞIİÖŞÜ].*") &&
               trimmed.length() >= 2;
    }
    
    /**
     * Validate email format (must contain @ and .com or similar domain)
     * Prevents overflow by checking length (max 100 characters as per database)
     */
    public static boolean isValidEmailFormat(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }
        String trimmed = email.trim();
        // Check length to prevent overflow (max 100 characters as per database)
        if (trimmed.length() > 100) {
            return false;
        }
        // Must contain @ and a domain with at least .com or similar
        return trimmed.contains("@") && 
               trimmed.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$") &&
               trimmed.indexOf("@") > 0 && 
               trimmed.indexOf("@") < trimmed.length() - 1;
    }
    
    /**
     * Validate phone number (exactly 10 digits, Turkish format: 5XXXXXXXXX)
     * Prevents overflow by ensuring exactly 10 digits
     */
    public static boolean isValidPhone(String phone) {
        if (phone == null || phone.trim().isEmpty()) {
            return false;
        }
        String trimmed = phone.trim();
        // Must be exactly 10 digits, no other characters
        // Turkish phone format: starts with 5, followed by 9 digits
        return trimmed.matches("^5[0-9]{9}$");
    }
}








