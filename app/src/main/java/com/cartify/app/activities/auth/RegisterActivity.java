package com.cartify.app.activities.auth;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.cartify.app.R;
import com.cartify.app.activities.main.MainActivity;
import com.cartify.app.utils.FirebaseHelper;
import com.cartify.app.utils.InputValidator;
import com.cartify.app.utils.FormValidationHelper;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

/**
 * Register Activity for new user registration
 */
public class RegisterActivity extends AppCompatActivity {

    private EditText etEmail, etPassword, etConfirmPassword;
    private Button btnRegister;
    private TextView tvLogin;
    private ProgressBar progressBar;
    private FirebaseAuth mAuth;
    private FormValidationHelper formValidator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Initialize Firebase Auth
        mAuth = FirebaseHelper.getAuth();

        initViews();
        setupValidation();
        setupClickListeners();
    }

    private void initViews() {
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        btnRegister = findViewById(R.id.btnRegister);
        tvLogin = findViewById(R.id.tvLogin);
        progressBar = findViewById(R.id.progressBar);
    }

    private void setupValidation() {
        formValidator = new FormValidationHelper();
        
        // Add validation for all fields
        formValidator.addRequiredField(etEmail, InputValidator::validateEmail)
                   .addRequiredField(etPassword, InputValidator::validatePassword)
                   .addRequiredField(etConfirmPassword, input -> 
                       InputValidator.validatePasswordConfirmation(
                           etPassword.getText().toString(), input));

        // Set validation listener to enable/disable register button
        formValidator.setValidationListener(new FormValidationHelper.ValidationListener() {
            @Override
            public void onValidationStateChanged(boolean isFormValid) {
                btnRegister.setEnabled(isFormValid && !isLoading());
            }

            @Override
            public void onFieldValidated(EditText field, boolean isValid, String errorMessage) {
                // Optional: Handle individual field validation
            }
        });
    }

    private void setupClickListeners() {
        btnRegister.setOnClickListener(v -> registerUser());
        
        tvLogin.setOnClickListener(v -> {
            startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
            finish();
        });
    }

    private void registerUser() {
        // Validate all fields before proceeding
        if (!formValidator.validateAllFields()) {
            formValidator.focusFirstInvalidField();
            return;
        }

        String email = InputValidator.sanitizeInput(etEmail.getText().toString().trim());
        String password = etPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();

        // Additional security checks
        if (email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        // Final password confirmation check
        if (!password.equals(confirmPassword)) {
            etConfirmPassword.setError("Passwords do not match");
            etConfirmPassword.requestFocus();
            return;
        }

        // Show progress bar
        setLoading(true);

        // Create user with Firebase
        mAuth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this, task -> {
                setLoading(false);

                if (task.isSuccessful()) {
                    // Registration successful
                    FirebaseUser user = mAuth.getCurrentUser();
                    if (user != null) {
                        Toast.makeText(RegisterActivity.this, 
                            "Welcome to Cartify!", Toast.LENGTH_SHORT).show();
                        
                        // Initialize user data in database
                        initializeUserData(user.getUid(), email);
                        
                        startActivity(new Intent(RegisterActivity.this, MainActivity.class));
                        finish();
                    }
                } else {
                    // Registration failed - provide user-friendly error messages
                    handleRegistrationError(task.getException());
                }
            });
    }

    private void handleRegistrationError(Exception exception) {
        String errorMessage = "Registration failed. Please try again.";
        
        if (exception != null) {
            String firebaseError = exception.getMessage();
            if (firebaseError != null) {
                if (firebaseError.contains("email address is already in use") || 
                    firebaseError.contains("email-already-in-use")) {
                    errorMessage = "An account with this email already exists. Please sign in instead.";
                    etEmail.setError("Email already registered");
                    etEmail.requestFocus();
                } else if (firebaseError.contains("invalid email") || 
                          firebaseError.contains("invalid-email")) {
                    errorMessage = "Invalid email address format.";
                    etEmail.setError("Invalid email format");
                    etEmail.requestFocus();
                } else if (firebaseError.contains("weak password") || 
                          firebaseError.contains("weak-password")) {
                    errorMessage = "Password is too weak. Please choose a stronger password.";
                    etPassword.setError("Password too weak");
                    etPassword.requestFocus();
                } else if (firebaseError.contains("network error") || 
                          firebaseError.contains("network")) {
                    errorMessage = "Network error. Please check your connection.";
                } else if (firebaseError.contains("too-many-requests")) {
                    errorMessage = "Too many attempts. Please try again later.";
                }
            }
        }
        
        Toast.makeText(RegisterActivity.this, errorMessage, Toast.LENGTH_LONG).show();
    }

    private void setLoading(boolean loading) {
        progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        btnRegister.setEnabled(!loading && formValidator.isFormValid());
        etEmail.setEnabled(!loading);
        etPassword.setEnabled(!loading);
        etConfirmPassword.setEnabled(!loading);
    }

    private boolean isLoading() {
        return progressBar.getVisibility() == View.VISIBLE;
    }

    private void initializeUserData(String userId, String email) {
        // Create user profile in Firestore
        String currentTime = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", 
            java.util.Locale.getDefault()).format(new java.util.Date());
        
        // Sanitize email before storing
        String sanitizedEmail = InputValidator.sanitizeInput(email);
        
        com.cartify.app.models.UserProfile userProfile = new com.cartify.app.models.UserProfile(
            userId, sanitizedEmail, "", "", "", currentTime, currentTime
        );
        
        FirebaseHelper.getUserProfileRef(userId).set(userProfile)
            .addOnSuccessListener(aVoid -> {
                // User profile created successfully
            })
            .addOnFailureListener(e -> {
                // Handle error silently or log it
                // Registration was successful, profile creation failure is not critical
            });
    }
}