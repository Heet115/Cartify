package com.cartify.app.models;

/**
 * User profile model class for storing user information in Firestore
 */
public class UserProfile {
    private String userId;
    private String email;
    private String name;
    private String phone;
    private String address;
    private String createdAt;
    private String lastLoginAt;

    // Default constructor required for Firestore
    public UserProfile() {}

    public UserProfile(String userId, String email, String name, String phone, 
                      String address, String createdAt, String lastLoginAt) {
        this.userId = userId;
        this.email = email;
        this.name = name;
        this.phone = phone;
        this.address = address;
        this.createdAt = createdAt;
        this.lastLoginAt = lastLoginAt;
    }

    // Getters and Setters
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    public String getLastLoginAt() { return lastLoginAt; }
    public void setLastLoginAt(String lastLoginAt) { this.lastLoginAt = lastLoginAt; }
}