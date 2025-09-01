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
import com.cartify.app.utils.UserDataHelper;
import com.cartify.app.utils.InputValidator;
import com.cartify.app.utils.FormValidationHelper;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

/**
 * Login Activity for user authentication
 */
public class LoginActivity extends AppCompatActivity {

    private EditText etEmail, etPassword;
    private Button btnLogin;
    private TextView tvRegister, tvForgotPassword;
    private ProgressBar progressBar;
    private FirebaseAuth mAuth;
    private UserDataHelper userDataHelper;
    private FormValidationHelper formValidator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Initialize Firebase Auth and UserDataHelper
        mAuth = FirebaseHelper.getAuth();
        userDataHelper = new UserDataHelper(this);

        // Check if user is already logged in (check both Firebase and local storage)
        if (FirebaseHelper.isUserLoggedIn() && userDataHelper.isUserLoggedIn()) {
            startActivity(new Intent(LoginActivity.this, MainActivity.class));
            finish();
            return;
        }

        initViews();
        setupValidation();
        setupClickListeners();
    }

    private void initViews() {
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        tvRegister = findViewById(R.id.tvRegister);
        tvForgotPassword = findViewById(R.id.tvForgotPassword);
        progressBar = findViewById(R.id.progressBar);
    }

    private void setupValidation() {
        formValidator = new FormValidationHelper();
        
        // Add validation for email and password fields
        formValidator.addRequiredField(etEmail, InputValidator::validateEmail)
                   .addRequiredField(etPassword, input -> {
                       if (TextUtils.isEmpty(input)) {
                           return new InputValidator.ValidationResult(false, "Password is required");
                       }
                       return new InputValidator.ValidationResult(true, null);
                   });

        // Set validation listener to enable/disable login button
        formValidator.setValidationListener(new FormValidationHelper.ValidationListener() {
            @Override
            public void onValidationStateChanged(boolean isFormValid) {
                btnLogin.setEnabled(isFormValid && !isLoading());
            }

            @Override
            public void onFieldValidated(EditText field, boolean isValid, String errorMessage) {
                // Optional: Handle individual field validation
            }
        });
    }

    private void setupClickListeners() {
        btnLogin.setOnClickListener(v -> loginUser());
        
        tvRegister.setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
        });

        tvForgotPassword.setOnClickListener(v -> sendPasswordResetEmail());
    }

    private void sendPasswordResetEmail() {
        String email = etEmail.getText().toString().trim();
        
        // Validate email before sending reset
        InputValidator.ValidationResult emailValidation = InputValidator.validateEmail(email);
        if (!emailValidation.isValid()) {
            etEmail.setError(emailValidation.getErrorMessage());
            etEmail.requestFocus();
            return;
        }

        // Show progress
        progressBar.setVisibility(View.VISIBLE);
        tvForgotPassword.setEnabled(false);

        mAuth.sendPasswordResetEmail(email)
            .addOnCompleteListener(task -> {
                progressBar.setVisibility(View.GONE);
                tvForgotPassword.setEnabled(true);

                if (task.isSuccessful()) {
                    Toast.makeText(LoginActivity.this, 
                        "Password reset email sent to " + email, Toast.LENGTH_LONG).show();
                } else {
                    String errorMessage = "Failed to send reset email";
                    if (task.getException() != null) {
                        String firebaseError = task.getException().getMessage();
                        if (firebaseError != null) {
                            if (firebaseError.contains("no user record")) {
                                errorMessage = "No account found with this email address";
                            } else if (firebaseError.contains("invalid email")) {
                                errorMessage = "Invalid email address";
                            } else {
                                errorMessage = firebaseError;
                            }
                        }
                    }
                    Toast.makeText(LoginActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                }
            });
    }

    private void loginUser() {
        // Validate all fields before proceeding
        if (!formValidator.validateAllFields()) {
            formValidator.focusFirstInvalidField();
            return;
        }

        String email = InputValidator.sanitizeInput(etEmail.getText().toString().trim());
        String password = etPassword.getText().toString().trim();

        // Additional security check
        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        // Show progress bar
        setLoading(true);

        // Sign in with Firebase
        mAuth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this, task -> {
                setLoading(false);

                if (task.isSuccessful()) {
                    // Login successful - save user data locally
                    FirebaseUser user = mAuth.getCurrentUser();
                    if (user != null) {
                        saveUserDataLocally(user, email);
                    }
                    
                    updateLastLoginTime();
                    Toast.makeText(LoginActivity.this, "Welcome back!", 
                        Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(LoginActivity.this, MainActivity.class));
                    finish();
                } else {
                    // Login failed - provide user-friendly error messages
                    handleLoginError(task.getException());
                }
            });
    }

    private void handleLoginError(Exception exception) {
        String errorMessage = "Login failed. Please try again.";
        
        if (exception != null) {
            String firebaseError = exception.getMessage();
            if (firebaseError != null) {
                if (firebaseError.contains("password is invalid") || 
                    firebaseError.contains("wrong-password")) {
                    errorMessage = "Incorrect password. Please try again.";
                    etPassword.setError("Incorrect password");
                    etPassword.requestFocus();
                } else if (firebaseError.contains("no user record") || 
                          firebaseError.contains("user-not-found")) {
                    errorMessage = "No account found with this email address.";
                    etEmail.setError("Account not found");
                    etEmail.requestFocus();
                } else if (firebaseError.contains("invalid email") || 
                          firebaseError.contains("invalid-email")) {
                    errorMessage = "Invalid email address format.";
                    etEmail.setError("Invalid email format");
                    etEmail.requestFocus();
                } else if (firebaseError.contains("too-many-requests")) {
                    errorMessage = "Too many failed attempts. Please try again later.";
                } else if (firebaseError.contains("network error") || 
                          firebaseError.contains("network")) {
                    errorMessage = "Network error. Please check your connection.";
                } else if (firebaseError.contains("user-disabled")) {
                    errorMessage = "This account has been disabled. Please contact support.";
                }
            }
        }
        
        Toast.makeText(LoginActivity.this, errorMessage, Toast.LENGTH_LONG).show();
    }

    private void setLoading(boolean loading) {
        progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        btnLogin.setEnabled(!loading && formValidator.isFormValid());
        tvForgotPassword.setEnabled(!loading);
        etEmail.setEnabled(!loading);
        etPassword.setEnabled(!loading);
    }

    private boolean isLoading() {
        return progressBar.getVisibility() == View.VISIBLE;
    }

    private void saveUserDataLocally(FirebaseUser user, String email) {
        // Save user session data to local storage
        String userId = user.getUid();
        String displayName = user.getDisplayName() != null ? user.getDisplayName() : "";
        
        userDataHelper.saveUserSession(userId, email, displayName);
        
        // Mark as not first time user if they're logging in
        userDataHelper.setFirstTimeUser(false);
    }
    
    private void updateLastLoginTime() {
        // Update last login time in local storage
        userDataHelper.updateLastLoginTime();
        
        // Also update in Firebase
        String userId = FirebaseHelper.getCurrentUserId();
        if (userId != null) {
            String currentTime = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", 
                java.util.Locale.getDefault()).format(new java.util.Date());
            
            FirebaseHelper.getUserProfileRef(userId)
                .update("lastLoginAt", currentTime)
                .addOnFailureListener(e -> {
                    // Handle error silently or log it
                });
        }
    }
}