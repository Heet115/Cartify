package com.cartify.app.utils;

import android.text.TextUtils;
import android.util.Patterns;
import android.widget.EditText;

import java.util.regex.Pattern;

/**
 * Comprehensive input validation utility class for Cartify app
 * Provides centralized validation methods for all user inputs
 */
public class InputValidator {

    // Regex patterns for validation
    private static final Pattern NAME_PATTERN = Pattern.compile("^[a-zA-Z\\s]{2,50}$");
    private static final Pattern PHONE_PATTERN = Pattern.compile("^[+]?[0-9]{10,15}$");
    private static final Pattern PASSWORD_PATTERN = Pattern.compile("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)[a-zA-Z\\d@$!%*?&]{8,}$");
    private static final Pattern SEARCH_PATTERN = Pattern.compile("^[a-zA-Z0-9\\s\\-_.,]{1,100}$");
    private static final Pattern ADDRESS_PATTERN = Pattern.compile("^[a-zA-Z0-9\\s\\-_.,#/]{5,200}$");
    
    // Validation result class
    public static class ValidationResult {
        private final boolean isValid;
        private final String errorMessage;
        
        public ValidationResult(boolean isValid, String errorMessage) {
            this.isValid = isValid;
            this.errorMessage = errorMessage;
        }
        
        public boolean isValid() {
            return isValid;
        }
        
        public String getErrorMessage() {
            return errorMessage;
        }
    }

    /**
     * Validates email address
     */
    public static ValidationResult validateEmail(String email) {
        if (TextUtils.isEmpty(email)) {
            return new ValidationResult(false, "Email is required");
        }
        
        email = email.trim();
        
        if (email.length() > 254) {
            return new ValidationResult(false, "Email is too long");
        }
        
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            return new ValidationResult(false, "Please enter a valid email address");
        }
        
        return new ValidationResult(true, null);
    }

    /**
     * Validates password with strength requirements
     */
    public static ValidationResult validatePassword(String password) {
        if (TextUtils.isEmpty(password)) {
            return new ValidationResult(false, "Password is required");
        }
        
        if (password.length() < 8) {
            return new ValidationResult(false, "Password must be at least 8 characters long");
        }
        
        if (password.length() > 128) {
            return new ValidationResult(false, "Password is too long (max 128 characters)");
        }
        
        if (!PASSWORD_PATTERN.matcher(password).matches()) {
            return new ValidationResult(false, "Password must contain at least one uppercase letter, one lowercase letter, and one number");
        }
        
        // Check for common weak passwords
        String lowerPassword = password.toLowerCase();
        String[] weakPasswords = {"password", "12345678", "qwerty123", "admin123", "welcome123"};
        for (String weak : weakPasswords) {
            if (lowerPassword.contains(weak)) {
                return new ValidationResult(false, "Password is too common. Please choose a stronger password");
            }
        }
        
        return new ValidationResult(true, null);
    }

    /**
     * Validates password confirmation
     */
    public static ValidationResult validatePasswordConfirmation(String password, String confirmPassword) {
        if (TextUtils.isEmpty(confirmPassword)) {
            return new ValidationResult(false, "Please confirm your password");
        }
        
        if (!password.equals(confirmPassword)) {
            return new ValidationResult(false, "Passwords do not match");
        }
        
        return new ValidationResult(true, null);
    }

    /**
     * Validates full name
     */
    public static ValidationResult validateName(String name) {
        if (TextUtils.isEmpty(name)) {
            return new ValidationResult(false, "Name is required");
        }
        
        name = name.trim();
        
        if (name.length() < 2) {
            return new ValidationResult(false, "Name must be at least 2 characters long");
        }
        
        if (name.length() > 50) {
            return new ValidationResult(false, "Name is too long (max 50 characters)");
        }
        
        if (!NAME_PATTERN.matcher(name).matches()) {
            return new ValidationResult(false, "Name can only contain letters and spaces");
        }
        
        return new ValidationResult(true, null);
    }

    /**
     * Validates phone number
     */
    public static ValidationResult validatePhone(String phone) {
        if (TextUtils.isEmpty(phone)) {
            return new ValidationResult(false, "Phone number is required");
        }
        
        phone = phone.trim().replaceAll("\\s+", ""); // Remove spaces
        
        if (!PHONE_PATTERN.matcher(phone).matches()) {
            return new ValidationResult(false, "Please enter a valid phone number (10-15 digits)");
        }
        
        return new ValidationResult(true, null);
    }

    /**
     * Validates address
     */
    public static ValidationResult validateAddress(String address) {
        if (TextUtils.isEmpty(address)) {
            return new ValidationResult(false, "Address is required");
        }
        
        address = address.trim();
        
        if (address.length() < 5) {
            return new ValidationResult(false, "Address must be at least 5 characters long");
        }
        
        if (address.length() > 200) {
            return new ValidationResult(false, "Address is too long (max 200 characters)");
        }
        
        if (!ADDRESS_PATTERN.matcher(address).matches()) {
            return new ValidationResult(false, "Address contains invalid characters");
        }
        
        return new ValidationResult(true, null);
    }

    /**
     * Validates search query
     */
    public static ValidationResult validateSearchQuery(String query) {
        if (TextUtils.isEmpty(query)) {
            return new ValidationResult(false, "Search query cannot be empty");
        }
        
        query = query.trim();
        
        if (query.length() < 1) {
            return new ValidationResult(false, "Search query is too short");
        }
        
        if (query.length() > 100) {
            return new ValidationResult(false, "Search query is too long (max 100 characters)");
        }
        
        if (!SEARCH_PATTERN.matcher(query).matches()) {
            return new ValidationResult(false, "Search query contains invalid characters");
        }
        
        return new ValidationResult(true, null);
    }

    /**
     * Validates price input
     */
    public static ValidationResult validatePrice(String priceStr) {
        if (TextUtils.isEmpty(priceStr)) {
            return new ValidationResult(false, "Price is required");
        }
        
        try {
            double price = Double.parseDouble(priceStr.trim());
            
            if (price < 0) {
                return new ValidationResult(false, "Price cannot be negative");
            }
            
            if (price > 999999.99) {
                return new ValidationResult(false, "Price is too high (max $999,999.99)");
            }
            
            // Check for reasonable decimal places
            String[] parts = priceStr.split("\\.");
            if (parts.length > 1 && parts[1].length() > 2) {
                return new ValidationResult(false, "Price can have at most 2 decimal places");
            }
            
            return new ValidationResult(true, null);
            
        } catch (NumberFormatException e) {
            return new ValidationResult(false, "Please enter a valid price");
        }
    }

    /**
     * Validates quantity input
     */
    public static ValidationResult validateQuantity(String quantityStr) {
        if (TextUtils.isEmpty(quantityStr)) {
            return new ValidationResult(false, "Quantity is required");
        }
        
        try {
            int quantity = Integer.parseInt(quantityStr.trim());
            
            if (quantity < 1) {
                return new ValidationResult(false, "Quantity must be at least 1");
            }
            
            if (quantity > 999) {
                return new ValidationResult(false, "Quantity cannot exceed 999");
            }
            
            return new ValidationResult(true, null);
            
        } catch (NumberFormatException e) {
            return new ValidationResult(false, "Please enter a valid quantity");
        }
    }

    /**
     * Validates rating input
     */
    public static ValidationResult validateRating(String ratingStr) {
        if (TextUtils.isEmpty(ratingStr)) {
            return new ValidationResult(false, "Rating is required");
        }
        
        try {
            float rating = Float.parseFloat(ratingStr.trim());
            
            if (rating < 0.0f) {
                return new ValidationResult(false, "Rating cannot be negative");
            }
            
            if (rating > 5.0f) {
                return new ValidationResult(false, "Rating cannot exceed 5.0");
            }
            
            return new ValidationResult(true, null);
            
        } catch (NumberFormatException e) {
            return new ValidationResult(false, "Please enter a valid rating");
        }
    }

    /**
     * Helper method to validate EditText and show error
     */
    public static boolean validateEditText(EditText editText, ValidationResult result) {
        if (!result.isValid()) {
            editText.setError(result.getErrorMessage());
            editText.requestFocus();
            return false;
        } else {
            editText.setError(null);
            return true;
        }
    }

    /**
     * Sanitizes input by removing potentially harmful characters
     */
    public static String sanitizeInput(String input) {
        if (TextUtils.isEmpty(input)) {
            return "";
        }
        
        return input.trim()
                .replaceAll("<", "&lt;")
                .replaceAll(">", "&gt;")
                .replaceAll("\"", "&quot;")
                .replaceAll("'", "&#x27;")
                .replaceAll("/", "&#x2F;");
    }

    /**
     * Validates multiple fields at once
     */
    public static boolean validateFields(ValidationField... fields) {
        boolean allValid = true;
        
        for (ValidationField field : fields) {
            ValidationResult result = field.validate();
            if (!validateEditText(field.getEditText(), result)) {
                allValid = false;
            }
        }
        
        return allValid;
    }

    /**
     * Helper class for batch validation
     */
    public static class ValidationField {
        private final EditText editText;
        private final ValidationFunction validationFunction;
        
        public ValidationField(EditText editText, ValidationFunction validationFunction) {
            this.editText = editText;
            this.validationFunction = validationFunction;
        }
        
        public EditText getEditText() {
            return editText;
        }
        
        public ValidationResult validate() {
            return validationFunction.validate(editText.getText().toString());
        }
    }

    /**
     * Functional interface for validation functions
     */
    public interface ValidationFunction {
        ValidationResult validate(String input);
    }
}