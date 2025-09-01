package com.cartify.app.models;

import java.util.List;

/**
 * Product model class representing items in the e-commerce app
 */
public class Product {
    private String id;
    private String title;
    private String description;
    private double price;
    private double oldPrice;
    private double rating;
    private int review;
    private List<String> picUrl;
    private List<String> size;
    private List<String> color;

    // Default constructor required for Firebase
    public Product() {}

    public Product(String id, String title, String description, double price, 
                   double oldPrice, double rating, int review,
                   List<String> picUrl, List<String> size, List<String> color) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.price = price;
        this.oldPrice = oldPrice;
        this.rating = rating;
        this.review = review;
        this.picUrl = picUrl;
        this.size = size;
        this.color = color;
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }

    public double getOldPrice() { return oldPrice; }
    public void setOldPrice(double oldPrice) { this.oldPrice = oldPrice; }

    public double getRating() { return rating; }
    public void setRating(double rating) { this.rating = rating; }

    public int getReview() { return review; }
    public void setReview(int review) { this.review = review; }

    public List<String> getPicUrl() { return picUrl; }
    public void setPicUrl(List<String> picUrl) { this.picUrl = picUrl; }

    public List<String> getSize() { return size; }
    public void setSize(List<String> size) { this.size = size; }

    public List<String> getColor() { return color; }
    public void setColor(List<String> color) { this.color = color; }
}