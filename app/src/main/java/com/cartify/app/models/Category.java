package com.cartify.app.models;

/**
 * Category model class representing product categories
 */
public class Category {
    private int id;
    private String title;

    // Default constructor required for Firebase
    public Category() {}

    public Category(int id, String title) {
        this.id = id;
        this.title = title;
    }

    // Getters and Setters
    public int getId() { 
        return id; 
    }
    
    public void setId(int id) { 
        this.id = id; 
    }

    public String getTitle() { 
        return title; 
    }
    
    public void setTitle(String title) { 
        this.title = title; 
    }
}