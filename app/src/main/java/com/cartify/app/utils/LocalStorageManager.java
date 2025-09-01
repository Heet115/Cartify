package com.cartify.app.utils;

import android.content.Context;
import android.content.SharedPreferences;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * LocalStorageManager - Handles all local storage operations using SharedPreferences
 * Provides methods to save and retrieve various types of user data
 */
public class LocalStorageManager {
    
    private static final String PREF_NAME = "CartifyUserData";
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_USER_EMAIL = "user_email";
    private static final String KEY_USER_NAME = "user_name";
    private static final String KEY_USER_PHONE = "user_phone";
    private static final String KEY_USER_ADDRESS = "user_address";
    private static final String KEY_IS_LOGGED_IN = "is_logged_in";
    private static final String KEY_CART_ITEMS = "cart_items";
    private static final String KEY_FAVORITE_PRODUCTS = "favorite_products";
    private static final String KEY_RECENT_SEARCHES = "recent_searches";
    private static final String KEY_APP_THEME = "app_theme";
    private static final String KEY_NOTIFICATIONS_ENABLED = "notifications_enabled";
    private static final String KEY_LANGUAGE = "language";
    private static final String KEY_CURRENCY = "currency";
    
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    private Gson gson;
    private static LocalStorageManager instance;
    
    private LocalStorageManager(Context context) {
        sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
        gson = new Gson();
    }
    
    public static synchronized LocalStorageManager getInstance(Context context) {
        if (instance == null) {
            instance = new LocalStorageManager(context.getApplicationContext());
        }
        return instance;
    }
    
    // User Authentication Data
    public void saveUserLoginData(String userId, String email, String name) {
        editor.putString(KEY_USER_ID, userId);
        editor.putString(KEY_USER_EMAIL, email);
        editor.putString(KEY_USER_NAME, name);
        editor.putBoolean(KEY_IS_LOGGED_IN, true);
        editor.apply();
    }
    
    public void saveUserProfile(String name, String phone, String address) {
        editor.putString(KEY_USER_NAME, name);
        editor.putString(KEY_USER_PHONE, phone);
        editor.putString(KEY_USER_ADDRESS, address);
        editor.apply();
    }
    
    public String getUserId() {
        return sharedPreferences.getString(KEY_USER_ID, "");
    }
    
    public String getUserEmail() {
        return sharedPreferences.getString(KEY_USER_EMAIL, "");
    }
    
    public String getUserName() {
        return sharedPreferences.getString(KEY_USER_NAME, "");
    }
    
    public String getUserPhone() {
        return sharedPreferences.getString(KEY_USER_PHONE, "");
    }
    
    public String getUserAddress() {
        return sharedPreferences.getString(KEY_USER_ADDRESS, "");
    }
    
    public boolean isUserLoggedIn() {
        return sharedPreferences.getBoolean(KEY_IS_LOGGED_IN, false);
    }
    
    public void logout() {
        editor.putBoolean(KEY_IS_LOGGED_IN, false);
        editor.remove(KEY_USER_ID);
        editor.remove(KEY_USER_EMAIL);
        // Keep user profile data for next login
        editor.apply();
    }
    
    // Cart Data
    public void saveCartItems(List<String> cartItemsJson) {
        String json = gson.toJson(cartItemsJson);
        editor.putString(KEY_CART_ITEMS, json);
        editor.apply();
    }
    
    public List<String> getCartItems() {
        String json = sharedPreferences.getString(KEY_CART_ITEMS, "");
        if (json.isEmpty()) {
            return new ArrayList<>();
        }
        Type type = new TypeToken<List<String>>(){}.getType();
        return gson.fromJson(json, type);
    }
    
    public void clearCart() {
        editor.remove(KEY_CART_ITEMS);
        editor.apply();
    }    

    // Favorite Products
    public void addFavoriteProduct(String productId) {
        Set<String> favorites = sharedPreferences.getStringSet(KEY_FAVORITE_PRODUCTS, new java.util.HashSet<>());
        favorites.add(productId);
        editor.putStringSet(KEY_FAVORITE_PRODUCTS, favorites);
        editor.apply();
    }
    
    public void removeFavoriteProduct(String productId) {
        Set<String> favorites = sharedPreferences.getStringSet(KEY_FAVORITE_PRODUCTS, new java.util.HashSet<>());
        favorites.remove(productId);
        editor.putStringSet(KEY_FAVORITE_PRODUCTS, favorites);
        editor.apply();
    }
    
    public Set<String> getFavoriteProducts() {
        return sharedPreferences.getStringSet(KEY_FAVORITE_PRODUCTS, new java.util.HashSet<>());
    }
    
    public boolean isFavoriteProduct(String productId) {
        Set<String> favorites = getFavoriteProducts();
        return favorites.contains(productId);
    }
    
    // Recent Searches
    public void addRecentSearch(String searchQuery) {
        List<String> recentSearches = getRecentSearches();
        
        // Remove if already exists to avoid duplicates
        recentSearches.remove(searchQuery);
        
        // Add to beginning of list
        recentSearches.add(0, searchQuery);
        
        // Keep only last 10 searches
        if (recentSearches.size() > 10) {
            recentSearches = recentSearches.subList(0, 10);
        }
        
        String json = gson.toJson(recentSearches);
        editor.putString(KEY_RECENT_SEARCHES, json);
        editor.apply();
    }
    
    public List<String> getRecentSearches() {
        String json = sharedPreferences.getString(KEY_RECENT_SEARCHES, "");
        if (json.isEmpty()) {
            return new ArrayList<>();
        }
        Type type = new TypeToken<List<String>>(){}.getType();
        return gson.fromJson(json, type);
    }
    
    public void clearRecentSearches() {
        editor.remove(KEY_RECENT_SEARCHES);
        editor.apply();
    }
    
    // App Settings
    public void setAppTheme(String theme) {
        editor.putString(KEY_APP_THEME, theme);
        editor.apply();
    }
    
    public String getAppTheme() {
        return sharedPreferences.getString(KEY_APP_THEME, "light");
    }
    
    public void setNotificationsEnabled(boolean enabled) {
        editor.putBoolean(KEY_NOTIFICATIONS_ENABLED, enabled);
        editor.apply();
    }
    
    public boolean areNotificationsEnabled() {
        return sharedPreferences.getBoolean(KEY_NOTIFICATIONS_ENABLED, true);
    }
    
    public void setLanguage(String language) {
        editor.putString(KEY_LANGUAGE, language);
        editor.apply();
    }
    
    public String getLanguage() {
        return sharedPreferences.getString(KEY_LANGUAGE, "en");
    }
    
    public void setCurrency(String currency) {
        editor.putString(KEY_CURRENCY, currency);
        editor.apply();
    }
    
    public String getCurrency() {
        return sharedPreferences.getString(KEY_CURRENCY, "USD");
    }
    
    // Generic methods for custom data
    public void saveString(String key, String value) {
        editor.putString(key, value);
        editor.apply();
    }
    
    public String getString(String key, String defaultValue) {
        return sharedPreferences.getString(key, defaultValue);
    }
    
    public void saveInt(String key, int value) {
        editor.putInt(key, value);
        editor.apply();
    }
    
    public int getInt(String key, int defaultValue) {
        return sharedPreferences.getInt(key, defaultValue);
    }
    
    public void saveBoolean(String key, boolean value) {
        editor.putBoolean(key, value);
        editor.apply();
    }
    
    public boolean getBoolean(String key, boolean defaultValue) {
        return sharedPreferences.getBoolean(key, defaultValue);
    }
    
    public void saveFloat(String key, float value) {
        editor.putFloat(key, value);
        editor.apply();
    }
    
    public float getFloat(String key, float defaultValue) {
        return sharedPreferences.getFloat(key, defaultValue);
    }
    
    public void saveLong(String key, long value) {
        editor.putLong(key, value);
        editor.apply();
    }
    
    public long getLong(String key, long defaultValue) {
        return sharedPreferences.getLong(key, defaultValue);
    }
    
    // Save complex objects as JSON
    public void saveObject(String key, Object object) {
        String json = gson.toJson(object);
        editor.putString(key, json);
        editor.apply();
    }
    
    public <T> T getObject(String key, Class<T> classType) {
        String json = sharedPreferences.getString(key, "");
        if (json.isEmpty()) {
            return null;
        }
        return gson.fromJson(json, classType);
    }
    
    // Clear all data
    public void clearAllData() {
        editor.clear();
        editor.apply();
    }
    
    // Remove specific key
    public void removeKey(String key) {
        editor.remove(key);
        editor.apply();
    }
    
    // Check if key exists
    public boolean containsKey(String key) {
        return sharedPreferences.contains(key);
    }
}