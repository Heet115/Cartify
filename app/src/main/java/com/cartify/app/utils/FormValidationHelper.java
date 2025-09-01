package com.cartify.app.utils;

import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;

import java.util.ArrayList;
import java.util.List;

/**
 * Helper class for real-time form validation and user experience improvements
 */
public class FormValidationHelper {

    private final List<ValidationField> fields;
    private ValidationListener validationListener;
    private boolean isRealTimeValidationEnabled = true;

    public FormValidationHelper() {
        this.fields = new ArrayList<>();
    }

    /**
     * Interface for validation state changes
     */
    public interface ValidationListener {
        void onValidationStateChanged(boolean isFormValid);
        void onFieldValidated(EditText field, boolean isValid, String errorMessage);
    }

    /**
     * Represents a field with its validation rules
     */
    public static class ValidationField {
        private final EditText editText;
        private final InputValidator.ValidationFunction validationFunction;
        private final boolean isRequired;
        private boolean isValid = false;

        public ValidationField(EditText editText, InputValidator.ValidationFunction validationFunction, boolean isRequired) {
            this.editText = editText;
            this.validationFunction = validationFunction;
            this.isRequired = isRequired;
        }

        public EditText getEditText() {
            return editText;
        }

        public InputValidator.ValidationFunction getValidationFunction() {
            return validationFunction;
        }

        public boolean isRequired() {
            return isRequired;
        }

        public boolean isValid() {
            return isValid;
        }

        public void setValid(boolean valid) {
            isValid = valid;
        }
    }

    /**
     * Adds a field to be validated
     */
    public FormValidationHelper addField(EditText editText, InputValidator.ValidationFunction validationFunction, boolean isRequired) {
        ValidationField field = new ValidationField(editText, validationFunction, isRequired);
        fields.add(field);
        
        if (isRealTimeValidationEnabled) {
            setupRealTimeValidation(field);
        }
        
        return this;
    }

    /**
     * Convenience method for required fields
     */
    public FormValidationHelper addRequiredField(EditText editText, InputValidator.ValidationFunction validationFunction) {
        return addField(editText, validationFunction, true);
    }

    /**
     * Convenience method for optional fields
     */
    public FormValidationHelper addOptionalField(EditText editText, InputValidator.ValidationFunction validationFunction) {
        return addField(editText, validationFunction, false);
    }

    /**
     * Sets up real-time validation for a field
     */
    private void setupRealTimeValidation(ValidationField field) {
        field.getEditText().addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                validateField(field, false); // Don't show errors while typing
            }
        });

        // Validate on focus lost
        field.getEditText().setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                validateField(field, true); // Show errors when field loses focus
            }
        });
    }

    /**
     * Validates a single field
     */
    private void validateField(ValidationField field, boolean showError) {
        String input = field.getEditText().getText().toString();
        
        // Skip validation for optional empty fields
        if (!field.isRequired() && input.trim().isEmpty()) {
            field.setValid(true);
            field.getEditText().setError(null);
            notifyValidationStateChanged();
            return;
        }

        InputValidator.ValidationResult result = field.getValidationFunction().validate(input);
        field.setValid(result.isValid());

        if (showError && !result.isValid()) {
            field.getEditText().setError(result.getErrorMessage());
        } else if (result.isValid()) {
            field.getEditText().setError(null);
        }

        if (validationListener != null) {
            validationListener.onFieldValidated(field.getEditText(), result.isValid(), result.getErrorMessage());
        }

        notifyValidationStateChanged();
    }

    /**
     * Validates all fields
     */
    public boolean validateAllFields() {
        boolean allValid = true;

        for (ValidationField field : fields) {
            validateField(field, true);
            if (!field.isValid()) {
                allValid = false;
            }
        }

        return allValid;
    }

    /**
     * Checks if the form is currently valid
     */
    public boolean isFormValid() {
        for (ValidationField field : fields) {
            if (!field.isValid()) {
                return false;
            }
        }
        return true;
    }

    /**
     * Clears all validation errors
     */
    public void clearErrors() {
        for (ValidationField field : fields) {
            field.getEditText().setError(null);
        }
    }

    /**
     * Enables or disables real-time validation
     */
    public void setRealTimeValidationEnabled(boolean enabled) {
        this.isRealTimeValidationEnabled = enabled;
    }

    /**
     * Sets the validation listener
     */
    public void setValidationListener(ValidationListener listener) {
        this.validationListener = listener;
    }

    /**
     * Notifies listener about validation state changes
     */
    private void notifyValidationStateChanged() {
        if (validationListener != null) {
            validationListener.onValidationStateChanged(isFormValid());
        }
    }

    /**
     * Gets the first invalid field (for focusing)
     */
    public EditText getFirstInvalidField() {
        for (ValidationField field : fields) {
            if (!field.isValid()) {
                return field.getEditText();
            }
        }
        return null;
    }

    /**
     * Focuses on the first invalid field
     */
    public void focusFirstInvalidField() {
        EditText firstInvalid = getFirstInvalidField();
        if (firstInvalid != null) {
            firstInvalid.requestFocus();
        }
    }

    /**
     * Gets validation summary
     */
    public ValidationSummary getValidationSummary() {
        List<String> errors = new ArrayList<>();
        int validCount = 0;
        int totalCount = fields.size();

        for (ValidationField field : fields) {
            if (field.isValid()) {
                validCount++;
            } else {
                String input = field.getEditText().getText().toString();
                InputValidator.ValidationResult result = field.getValidationFunction().validate(input);
                if (!result.isValid()) {
                    errors.add(result.getErrorMessage());
                }
            }
        }

        return new ValidationSummary(validCount == totalCount, errors, validCount, totalCount);
    }

    /**
     * Validation summary class
     */
    public static class ValidationSummary {
        private final boolean isValid;
        private final List<String> errors;
        private final int validFieldCount;
        private final int totalFieldCount;

        public ValidationSummary(boolean isValid, List<String> errors, int validFieldCount, int totalFieldCount) {
            this.isValid = isValid;
            this.errors = errors;
            this.validFieldCount = validFieldCount;
            this.totalFieldCount = totalFieldCount;
        }

        public boolean isValid() {
            return isValid;
        }

        public List<String> getErrors() {
            return errors;
        }

        public int getValidFieldCount() {
            return validFieldCount;
        }

        public int getTotalFieldCount() {
            return totalFieldCount;
        }

        public String getErrorSummary() {
            if (errors.isEmpty()) {
                return "All fields are valid";
            }
            return String.join("\n", errors);
        }
    }
}