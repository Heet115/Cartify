package com.cartify.app;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.appcompat.app.AppCompatActivity;

import com.cartify.app.utils.FirebaseHelper;
import com.cartify.app.utils.UserDataHelper;

/**
 * Splash Screen Activity - Shows app logo and checks login status
 */
public class SplashActivity extends AppCompatActivity {

    private static final int SPLASH_DELAY = 2000; // 2 seconds
    private UserDataHelper userDataHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        // Initialize UserDataHelper
        userDataHelper = new UserDataHelper(this);

        // Delay and check login status
        new Handler(Looper.getMainLooper()).postDelayed(this::checkLoginStatus, SPLASH_DELAY);
    }

    private void checkLoginStatus() {
        // Check if user is logged in (both Firebase and local storage)
        if (FirebaseHelper.isUserLoggedIn() && userDataHelper.isUserLoggedIn()) {
            // User is logged in, go to MainActivity
            startActivity(new Intent(this, MainActivity.class));
        } else {
            // User is not logged in, go to LoginActivity
            startActivity(new Intent(this, LoginActivity.class));
        }
        finish();
    }
}