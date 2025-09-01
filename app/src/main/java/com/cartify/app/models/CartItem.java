package com.cartify.app.models;

/**
 * CartItem model class representing items in user's shopping cart
 */
public class CartItem {
    private String productId;
    private String title;
    private double price;
    private String imageUrl;
    private int quantity;
    private String selectedSize;
    private String selectedColor;

    // Default constructor required for Firebase
    public CartItem() {}

    public CartItem(String productId, String title, double price, String imageUrl, 
                    int quantity, String selectedSize, String selectedColor) {
        this.productId = productId;
        this.title = title;
        this.price = price;
        this.imageUrl = imageUrl;
        this.quantity = quantity;
        this.selectedSize = selectedSize;
        this.selectedColor = selectedColor;
    }

    // Getters and Setters
    public String getProductId() { return productId; }
    public void setProductId(String productId) { this.productId = productId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public String getSelectedSize() { return selectedSize; }
    public void setSelectedSize(String selectedSize) { this.selectedSize = selectedSize; }

    public String getSelectedColor() { return selectedColor; }
    public void setSelectedColor(String selectedColor) { this.selectedColor = selectedColor; }

    public double getTotalPrice() {
        return price * quantity;
    }
}