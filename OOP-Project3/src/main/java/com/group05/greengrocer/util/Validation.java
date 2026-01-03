package com.group05.greengrocer.util;

import java.math.BigDecimal;
import java.util.regex.Pattern;

/**
 * Validation utility class for input validation.
 * Provides methods to validate various types of user input.
 */
public class Validation {
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    /**
     * Validate email format.
     * 
     * @param email The email address to validate
     * @return true if valid email format, false otherwise
     */
    public static boolean isValidEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }
        return EMAIL_PATTERN.matcher(email).matches();
    }

    /**
     * Validate that a string is not null or empty.
     * 
     * @param value The string to validate
     * @return true if not null and not empty, false otherwise
     */
    public static boolean isNotEmpty(String value) {
        return value != null && !value.trim().isEmpty();
    }

    /**
     * Validate that a number is positive.
     * 
     * @param value The BigDecimal value to validate
     * @return true if positive, false otherwise
     */
    public static boolean isPositive(BigDecimal value) {
        return value != null && value.compareTo(BigDecimal.ZERO) > 0;
    }

    /**
     * Validate that a number is positive.
     * 
     * @param value The double value to validate
     * @return true if positive, false otherwise
     */
    public static boolean isPositive(double value) {
        return value > 0;
    }

    /**
     * Validate that a number is non-negative.
     * 
     * @param value The BigDecimal value to validate
     * @return true if non-negative, false otherwise
     */
    public static boolean isNonNegative(BigDecimal value) {
        return value != null && value.compareTo(BigDecimal.ZERO) >= 0;
    }

    /**
     * Validate quantity input (must be positive).
     * 
     * @param quantityStr The quantity string to validate
     * @return true if valid positive quantity, false otherwise
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
     * Validate price input (must be non-negative).
     * 
     * @param priceStr The price string to validate
     * @return true if valid non-negative price, false otherwise
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
     * Validate stock input (must be non-negative).
     * 
     * @param stockStr The stock string to validate
     * @return true if valid non-negative stock, false otherwise
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
     * Validate username (alphanumeric and underscore, 3-50 characters).
     * 
     * @param username The username to validate
     * @return true if valid username, false otherwise
     */
    public static boolean isValidUsername(String username) {
        if (username == null || username.trim().isEmpty()) {
            return false;
        }
        return username.matches("^[a-zA-Z0-9_]{3,50}$");
    }

    /**
     * Validate password (at least 6 characters) - basic validation.
     * 
     * @param password The password to validate
     * @return true if valid password, false otherwise
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
     * 
     * @param password The password to validate
     * @return true if meets strong password requirements, false otherwise
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
     * Validate full name (only letters and spaces, no numbers, at least 2
     * characters).
     * Allows Turkish characters (ç, ğ, ı, ö, ş, ü).
     * 
     * @param fullName The full name to validate
     * @return true if valid full name, false otherwise
     */
    public static boolean isValidFullName(String fullName) {
        if (fullName == null || fullName.trim().isEmpty()) {
            return false;
        }
        String trimmed = fullName.trim();
        if (trimmed.length() > 100) {
            return false;
        }
        return trimmed.matches("^[a-zA-ZçğıöşüÇĞIİÖŞÜ\\s-]+$") &&
                trimmed.matches(".*[a-zA-ZçğıöşüÇĞIİÖŞÜ].*") &&
                trimmed.length() >= 2;
    }

    /**
     * Validate email format (must contain @ and .com or similar domain).
     * Prevents overflow by checking length (max 100 characters as per database).
     * 
     * @param email The email address to validate
     * @return true if valid email format, false otherwise
     */
    public static boolean isValidEmailFormat(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }
        String trimmed = email.trim();
        if (trimmed.length() > 100) {
            return false;
        }
        return trimmed.contains("@") &&
                trimmed.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$") &&
                trimmed.indexOf("@") > 0 &&
                trimmed.indexOf("@") < trimmed.length() - 1;
    }

    /**
     * Validate phone number (exactly 10 digits, Turkish format: 5XXXXXXXXX).
     * Prevents overflow by ensuring exactly 10 digits.
     * 
     * @param phone The phone number to validate
     * @return true if valid phone number, false otherwise
     */
    public static boolean isValidPhone(String phone) {
        if (phone == null || phone.trim().isEmpty()) {
            return false;
        }
        String trimmed = phone.trim();
        return trimmed.matches("^5[0-9]{9}$");
    }
}
