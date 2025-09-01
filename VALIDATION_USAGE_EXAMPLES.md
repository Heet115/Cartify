# Input Validation Usage Examples

This document provides practical examples of how to use the input validation system in the Cartify app.

## Basic Validation Examples

### 1. Simple Field Validation

```java
// Validate email field
EditText emailField = findViewById(R.id.etEmail);
String email = emailField.getText().toString();

InputValidator.ValidationResult result = InputValidator.validateEmail(email);
if (!result.isValid()) {
    emailField.setError(result.getErrorMessage());
    return;
}
```

### 2. Using ValidationResult Helper

```java
// Validate and show error in one line
EditText passwordField = findViewById(R.id.etPassword);
InputValidator.ValidationResult passwordResult = 
    InputValidator.validatePassword(passwordField.getText().toString());

if (!InputValidator.validateEditText(passwordField, passwordResult)) {
    return; // Validation failed, error shown automatically
}
```

## Form Validation Examples

### 1. Basic Form Setup

```java
public class MyActivity extends AppCompatActivity {
    private FormValidationHelper formValidator;
    private EditText etName, etEmail, etPhone;
    private Button btnSubmit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my);
        
        initViews();
        setupValidation();
    }

    private void setupValidation() {
        formValidator = new FormValidationHelper();
        
        // Add fields with validation rules
        formValidator.addRequiredField(etEmail, InputValidator::validateEmail)
                   .addRequiredField(etName, InputValidator::validateName)
                   .addOptionalField(etPhone, InputValidator::validatePhone);

        // Enable/disable submit button based on validation state
        formValidator.setValidationListener(new FormValidationHelper.ValidationListener() {
            @Override
            public void onValidationStateChanged(boolean isFormValid) {
                btnSubmit.setEnabled(isFormValid);
            }

            @Override
            public void onFieldValidated(EditText field, boolean isValid, String errorMessage) {
                // Optional: Handle individual field validation
                if (field == etEmail && !isValid) {
                    // Custom handling for email field
                }
            }
        });
    }

    private void submitForm() {
        if (formValidator.validateAllFields()) {
            // All fields are valid, proceed with submission
            String name = InputValidator.sanitizeInput(etName.getText().toString());
            String email = InputValidator.sanitizeInput(etEmail.getText().toString());
            // ... submit data
        } else {
            formValidator.focusFirstInvalidField();
        }
    }
}
```

### 2. Advanced Form with Custom Validation

```java
private void setupAdvancedValidation() {
    formValidator = new FormValidationHelper();
    
    // Custom validation function
    InputValidator.ValidationFunction customEmailValidation = input -> {
        // First check basic email validation
        InputValidator.ValidationResult basicResult = InputValidator.validateEmail(input);
        if (!basicResult.isValid()) {
            return basicResult;
        }
        
        // Additional business rule: must be company email
        if (!input.endsWith("@company.com")) {
            return new InputValidator.ValidationResult(false, "Must use company email");
        }
        
        return new InputValidator.ValidationResult(true, null);
    };
    
    formValidator.addRequiredField(etEmail, customEmailValidation);
}
```

## Real-time Validation Examples

### 1. Enable Real-time Validation

```java
// Enable real-time validation (default)
formValidator.setRealTimeValidationEnabled(true);

// Disable for performance-sensitive forms
formValidator.setRealTimeValidationEnabled(false);
```

### 2. Manual Field Validation

```java
// Validate specific field manually
etEmail.setOnFocusChangeListener((v, hasFocus) -> {
    if (!hasFocus) {
        String email = etEmail.getText().toString();
        InputValidator.ValidationResult result = InputValidator.validateEmail(email);
        InputValidator.validateEditText(etEmail, result);
    }
});
```

## Batch Validation Examples

### 1. Validate Multiple Fields at Once

```java
// Using FormValidationHelper batch validation
boolean allValid = InputValidator.validateFields(
    new InputValidator.ValidationField(etEmail, InputValidator::validateEmail),
    new InputValidator.ValidationField(etPassword, InputValidator::validatePassword),
    new InputValidator.ValidationField(etName, InputValidator::validateName)
);

if (!allValid) {
    Toast.makeText(this, "Please fix the errors above", Toast.LENGTH_SHORT).show();
}
```

### 2. Get Validation Summary

```java
FormValidationHelper.ValidationSummary summary = formValidator.getValidationSummary();

if (!summary.isValid()) {
    String errorMessage = "Validation failed:\n" + summary.getErrorSummary();
    Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show();
}

// Show progress: "2 of 3 fields completed"
String progress = summary.getValidFieldCount() + " of " + 
                 summary.getTotalFieldCount() + " fields completed";
```

## E-commerce Specific Examples

### 1. Product Quantity Validation

```java
private void updateQuantity(int newQuantity) {
    InputValidator.ValidationResult result = 
        InputValidator.validateQuantity(String.valueOf(newQuantity));
    
    if (!result.isValid()) {
        Toast.makeText(this, result.getErrorMessage(), Toast.LENGTH_SHORT).show();
        return;
    }
    
    // Additional business rule
    if (newQuantity > 99) {
        Toast.makeText(this, "Maximum 99 items per product", Toast.LENGTH_SHORT).show();
        return;
    }
    
    // Update quantity
    this.quantity = newQuantity;
    updateUI();
}
```

### 2. Cart Validation Before Checkout

```java
private boolean validateCartForCheckout() {
    if (cartItems.isEmpty()) {
        Toast.makeText(this, "Cart is empty", Toast.LENGTH_SHORT).show();
        return false;
    }
    
    double total = 0;
    for (CartItem item : cartItems) {
        // Validate each item
        InputValidator.ValidationResult quantityResult = 
            InputValidator.validateQuantity(String.valueOf(item.getQuantity()));
        
        if (!quantityResult.isValid()) {
            Toast.makeText(this, "Invalid quantity for " + item.getProductTitle(), 
                Toast.LENGTH_LONG).show();
            return false;
        }
        
        InputValidator.ValidationResult priceResult = 
            InputValidator.validatePrice(String.valueOf(item.getPrice()));
        
        if (!priceResult.isValid()) {
            Toast.makeText(this, "Invalid price for " + item.getProductTitle(), 
                Toast.LENGTH_LONG).show();
            return false;
        }
        
        total += item.getPrice() * item.getQuantity();
    }
    
    // Validate total
    if (total > 999999.99) {
        Toast.makeText(this, "Cart total exceeds maximum limit", Toast.LENGTH_LONG).show();
        return false;
    }
    
    return true;
}
```

### 3. Search Input Validation

```java
private void performSearch(String query) {
    // Validate search query
    InputValidator.ValidationResult result = InputValidator.validateSearchQuery(query);
    if (!result.isValid()) {
        searchEditText.setError(result.getErrorMessage());
        return;
    }
    
    // Sanitize input before processing
    String sanitizedQuery = InputValidator.sanitizeInput(query);
    
    // Perform search with sanitized query
    searchProducts(sanitizedQuery);
}
```

## Error Handling Examples

### 1. User-Friendly Error Messages

```java
private void handleValidationError(InputValidator.ValidationResult result, EditText field) {
    if (!result.isValid()) {
        field.setError(result.getErrorMessage());
        field.requestFocus();
        
        // Optional: Show toast for critical errors
        if (result.getErrorMessage().contains("required")) {
            Toast.makeText(this, "Please fill in all required fields", 
                Toast.LENGTH_SHORT).show();
        }
    }
}
```

### 2. Validation with Loading States

```java
private void submitWithValidation() {
    // Show loading
    progressBar.setVisibility(View.VISIBLE);
    submitButton.setEnabled(false);
    
    // Validate all fields
    if (!formValidator.validateAllFields()) {
        // Hide loading on validation failure
        progressBar.setVisibility(View.GONE);
        submitButton.setEnabled(true);
        formValidator.focusFirstInvalidField();
        return;
    }
    
    // Proceed with submission
    submitData();
}
```

## Security Examples

### 1. Input Sanitization

```java
// Always sanitize user input before storage
private void saveUserProfile(String name, String address) {
    String sanitizedName = InputValidator.sanitizeInput(name);
    String sanitizedAddress = InputValidator.sanitizeInput(address);
    
    // Save sanitized data
    userProfile.setName(sanitizedName);
    userProfile.setAddress(sanitizedAddress);
}
```

### 2. XSS Prevention

```java
// Sanitize before displaying user-generated content
private void displayUserReview(String reviewText) {
    String safeReviewText = InputValidator.sanitizeInput(reviewText);
    reviewTextView.setText(safeReviewText);
}
```

## Performance Optimization Examples

### 1. Conditional Validation

```java
// Only validate when necessary
private void onTextChanged(String text) {
    // Skip validation for very short inputs
    if (text.length() < 3) {
        return;
    }
    
    // Debounce validation to avoid excessive calls
    handler.removeCallbacks(validationRunnable);
    handler.postDelayed(validationRunnable, 300);
}
```

### 2. Efficient Error Clearing

```java
// Clear errors efficiently
private void clearAllErrors() {
    formValidator.clearErrors();
    // Or manually for specific fields
    etEmail.setError(null);
    etPassword.setError(null);
}
```

## Testing Examples

### 1. Unit Test for Custom Validation

```java
@Test
public void testCustomEmailValidation() {
    // Test valid company email
    InputValidator.ValidationResult result = 
        customEmailValidation.validate("user@company.com");
    assertTrue(result.isValid());
    
    // Test invalid domain
    result = customEmailValidation.validate("user@gmail.com");
    assertFalse(result.isValid());
    assertEquals("Must use company email", result.getErrorMessage());
}
```

### 2. Integration Test for Form Validation

```java
@Test
public void testFormValidation() {
    // Setup form validator
    FormValidationHelper validator = new FormValidationHelper();
    validator.addRequiredField(emailField, InputValidator::validateEmail);
    
    // Test with invalid email
    emailField.setText("invalid-email");
    assertFalse(validator.validateAllFields());
    
    // Test with valid email
    emailField.setText("user@example.com");
    assertTrue(validator.validateAllFields());
}
```

These examples demonstrate the comprehensive input validation system and how to implement it effectively throughout your Android application.