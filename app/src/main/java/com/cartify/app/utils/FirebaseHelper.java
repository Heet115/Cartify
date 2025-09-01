package com.cartify.app.utils;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

/**
 * Firebase helper class for managing Firebase instances and common operations
 */
public class FirebaseHelper {
    private static FirebaseAuth mAuth;
    private static DatabaseReference mDatabase;

    public static FirebaseAuth getAuth() {
        if (mAuth == null) {
            mAuth = FirebaseAuth.getInstance();
        }
        return mAuth;
    }

    public static DatabaseReference getDatabase() {
        if (mDatabase == null) {
            mDatabase = FirebaseDatabase.getInstance().getReference();
        }
        return mDatabase;
    }

    public static FirebaseUser getCurrentUser() {
        return getAuth().getCurrentUser();
    }

    public static String getCurrentUserId() {
        FirebaseUser user = getCurrentUser();
        return user != null ? user.getUid() : null;
    }

    public static boolean isUserLoggedIn() {
        return getCurrentUser() != null;
    }

    // Database reference paths
    public static DatabaseReference getProductsRef() {
        return getDatabase().child("Items");
    }

    public static DatabaseReference getUserCartRef(String userId) {
        return getDatabase().child("users").child(userId).child("cart");
    }

    public static DatabaseReference getUserOrdersRef(String userId) {
        return getDatabase().child("users").child(userId).child("orders");
    }

    public static DatabaseReference getOrdersRef() {
        return getDatabase().child("orders");
    }
}