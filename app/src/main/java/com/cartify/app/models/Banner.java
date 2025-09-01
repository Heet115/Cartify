package com.cartify.app.models;

/**
 * Banner model class representing banner images in the app
 */
public class Banner {
    private String url;

    // Default constructor required for Firebase
    public Banner() {}

    public Banner(String url) {
        this.url = url;
    }

    // Getters and Setters
    public String getUrl() { 
        return url; 
    }
    
    public void setUrl(String url) { 
        this.url = url; 
    }
}