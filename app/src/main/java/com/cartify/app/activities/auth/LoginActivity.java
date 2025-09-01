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

    private void setupClickListeners() {
        btnLogin.setOnClickListener(v -> loginUser());
        
        tvRegister.setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
        });

        tvForgotPassword.setOnClickListener(v -> {
            // Implement forgot password functionality
            String email = etEmail.getText().toString().trim();
            if (TextUtils.isEmpty(email)) {
                etEmail.setError("Enter your email");
                return;
            }
            
            mAuth.sendPasswordResetEmail(email)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(LoginActivity.this, 
                            "Password reset email sent", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(LoginActivity.this, 
                            "Error: " + task.getException().getMessage(), 
                            Toast.LENGTH_SHORT).show();
                    }
                });
        });
    }

    private void loginUser() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        // Validate input
        if (TextUtils.isEmpty(email)) {
            etEmail.setError("Enter email");
            return;
        }

        if (TextUtils.isEmpty(password)) {
            etPassword.setError("Enter password");
            return;
        }

        // Show progress bar
        progressBar.setVisibility(View.VISIBLE);
        btnLogin.setEnabled(false);

        // Sign in with Firebase
        mAuth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this, task -> {
                progressBar.setVisibility(View.GONE);
                btnLogin.setEnabled(true);

                if (task.isSuccessful()) {
                    // Login successful - save user data locally
                    FirebaseUser user = mAuth.getCurrentUser();
                    if (user != null) {
                        saveUserDataLocally(user, email);
                    }
                    
                    updateLastLoginTime();
                    Toast.makeText(LoginActivity.this, "Login successful", 
                        Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(LoginActivity.this, MainActivity.class));
                    finish();
                } else {
                    // Login failed
                    Toast.makeText(LoginActivity.this, 
                        "Login failed: " + task.getException().getMessage(), 
                        Toast.LENGTH_SHORT).show();
                }
            });
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