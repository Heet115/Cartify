package com.cartify.app.utils;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

/**
 * Firebase helper class for managing Firebase instances and common operations
 * Uses Realtime Database for products and Firestore for user data, cart, and orders
 */
public class FirebaseHelper {
    private static FirebaseAuth mAuth;
    private static DatabaseReference mRealtimeDatabase;
    private static FirebaseFirestore mFirestore;

    public static FirebaseAuth getAuth() {
        if (mAuth == null) {
            mAuth = FirebaseAuth.getInstance();
        }
        return mAuth;
    }

    public static DatabaseReference getRealtimeDatabase() {
        if (mRealtimeDatabase == null) {
            mRealtimeDatabase = FirebaseDatabase.getInstance().getReference();
        }
        return mRealtimeDatabase;
    }

    public static FirebaseFirestore getFirestore() {
        if (mFirestore == null) {
            mFirestore = FirebaseFirestore.getInstance();
        }
        return mFirestore;
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

    // Realtime Database reference paths
    public static DatabaseReference getProductsRef() {
        return getRealtimeDatabase().child("Items");
    }

    public static DatabaseReference getCategoriesRef() {
        return getRealtimeDatabase().child("Category");
    }

    public static DatabaseReference getBannersRef() {
        return getRealtimeDatabase().child("Banner");
    }

    // Firestore collection references (for user data, cart, orders)
    public static CollectionReference getUsersCollection() {
        return getFirestore().collection("users");
    }

    public static DocumentReference getUserDocument(String userId) {
        return getUsersCollection().document(userId);
    }

    public static CollectionReference getUserCartCollection(String userId) {
        return getUserDocument(userId).collection("cart");
    }

    public static CollectionReference getUserOrdersCollection(String userId) {
        return getUserDocument(userId).collection("orders");
    }

    public static CollectionReference getOrdersCollection() {
        return getFirestore().collection("orders");
    }

    // User profile methods
    public static DocumentReference getUserProfileRef(String userId) {
        return getUserDocument(userId);
    }
}