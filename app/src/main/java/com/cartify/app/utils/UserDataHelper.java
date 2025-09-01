package com.cartify.app.utils;

import android.content.Context;
import com.cartify.app.models.CartItem;
import com.cartify.app.models.UserProfile;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * UserDataHelper - Helper class that provides convenient methods for common user data operations
 * This class acts as a bridge between your models and LocalStorageManager
 */
public class UserDataHelper {
    
    private LocalStorageManager storageManager;
    private Gson gson;
    
    public UserDataHelper(Context context) {
        storageManager = LocalStorageManager.getInstance(context);
        gson = new Gson();
    }
    
    // User Profile Operations
    public void saveUserProfile(UserProfile userProfile) {
        storageManager.saveObject("user_profile", userProfile);
        
        // Also save individual fields for quick access
        storageManager.saveUserProfile(
            userProfile.getName(),
            userProfile.getPhone(),
            userProfile.getAddress()
        );
    }
    
    public UserProfile getUserProfile() {
        return storageManager.getObject("user_profile", UserProfile.class);
    }
    
    // Cart Operations
    public void saveCartItems(List<CartItem> cartItems) {
        List<String> cartItemsJson = new ArrayList<>();
        for (CartItem item : cartItems) {
            cartItemsJson.add(gson.toJson(item));
        }
        storageManager.saveCartItems(cartItemsJson);
    }
    
    public List<CartItem> getCartItems() {
        List<String> cartItemsJson = storageManager.getCartItems();
        List<CartItem> cartItems = new ArrayList<>();
        
        for (String json : cartItemsJson) {
            CartItem item = gson.fromJson(json, CartItem.class);
            cartItems.add(item);
        }
        
        return cartItems;
    }
    
    public void addToCart(CartItem cartItem) {
        List<CartItem> cartItems = getCartItems();
        
        // Check if item already exists in cart
        boolean itemExists = false;
        for (int i = 0; i < cartItems.size(); i++) {
            CartItem existingItem = cartItems.get(i);
            if (existingItem.getProductId().equals(cartItem.getProductId())) {
                // Update quantity
                existingItem.setQuantity(existingItem.getQuantity() + cartItem.getQuantity());
                cartItems.set(i, existingItem);
                itemExists = true;
                break;
            }
        }
        
        if (!itemExists) {
            cartItems.add(cartItem);
        }
        
        saveCartItems(cartItems);
    }
    
    public void removeFromCart(String productId) {
        List<CartItem> cartItems = getCartItems();
        cartItems.removeIf(item -> item.getProductId().equals(productId));
        saveCartItems(cartItems);
    }
    
    public void updateCartItemQuantity(String productId, int newQuantity) {
        List<CartItem> cartItems = getCartItems();
        
        for (CartItem item : cartItems) {
            if (item.getProductId().equals(productId)) {
                if (newQuantity <= 0) {
                    cartItems.remove(item);
                } else {
                    item.setQuantity(newQuantity);
                }
                break;
            }
        }
        
        saveCartItems(cartItems);
    }
    
    public int getCartItemCount() {
        List<CartItem> cartItems = getCartItems();
        int totalCount = 0;
        for (CartItem item : cartItems) {
            totalCount += item.getQuantity();
        }
        return totalCount;
    }
    
    public double getCartTotal() {
        List<CartItem> cartItems = getCartItems();
        double total = 0.0;
        for (CartItem item : cartItems) {
            total += item.getPrice() * item.getQuantity();
        }
        return total;
    }
    
    public void clearCart() {
        storageManager.clearCart();
    }
    
    // User Session Management
    public void saveUserSession(String userId, String email, String name) {
        storageManager.saveUserLoginData(userId, email, name);
    }
    
    public boolean isUserLoggedIn() {
        return storageManager.isUserLoggedIn();
    }
    
    public void logoutUser() {
        storageManager.logout();
        // Optionally clear cart on logout
        // clearCart();
    }
    
    // App Preferences
    public void saveAppPreferences(String theme, boolean notificationsEnabled, String language) {
        storageManager.setAppTheme(theme);
        storageManager.setNotificationsEnabled(notificationsEnabled);
        storageManager.setLanguage(language);
    }
    
    public String getAppTheme() {
        return storageManager.getAppTheme();
    }
    
    public boolean areNotificationsEnabled() {
        return storageManager.areNotificationsEnabled();
    }
    
    public String getLanguage() {
        return storageManager.getLanguage();
    }
    
    // Search History
    public void addSearchQuery(String query) {
        if (query != null && !query.trim().isEmpty()) {
            storageManager.addRecentSearch(query.trim());
        }
    }
    
    public List<String> getRecentSearches() {
        return storageManager.getRecentSearches();
    }
    
    // Favorites
    public void toggleFavorite(String productId) {
        if (storageManager.isFavoriteProduct(productId)) {
            storageManager.removeFavoriteProduct(productId);
        } else {
            storageManager.addFavoriteProduct(productId);
        }
    }
    
    public boolean isFavorite(String productId) {
        return storageManager.isFavoriteProduct(productId);
    }
    
    // First Time User
    public void setFirstTimeUser(boolean isFirstTime) {
        storageManager.saveBoolean("is_first_time_user", isFirstTime);
    }
    
    public boolean isFirstTimeUser() {
        return storageManager.getBoolean("is_first_time_user", true);
    }
    
    // Last Login Time
    public void updateLastLoginTime() {
        storageManager.saveLong("last_login_time", System.currentTimeMillis());
    }
    
    public long getLastLoginTime() {
        return storageManager.getLong("last_login_time", 0);
    }
}