package com.cartify.app.utils;

/**
 * Utility class for price and discount calculations
 */
public class PriceUtils {

    /**
     * Calculate discount percentage between old price and current price
     * @param oldPrice Original price
     * @param currentPrice Current discounted price
     * @return Discount percentage as integer (0-100)
     */
    public static int calculateDiscountPercentage(double oldPrice, double currentPrice) {
        if (oldPrice <= 0 || currentPrice < 0 || currentPrice >= oldPrice) {
            return 0;
        }
        
        double discountPercent = ((oldPrice - currentPrice) / oldPrice) * 100;
        return (int) Math.round(discountPercent);
    }

    /**
     * Format discount percentage for display
     * @param oldPrice Original price
     * @param currentPrice Current discounted price
     * @return Formatted discount string (e.g., "30% OFF") or null if no discount
     */
    public static String getDiscountText(double oldPrice, double currentPrice) {
        int discountPercent = calculateDiscountPercentage(oldPrice, currentPrice);
        if (discountPercent > 0) {
            return discountPercent + "% OFF";
        }
        return null;
    }

    /**
     * Check if product has a discount
     * @param oldPrice Original price
     * @param currentPrice Current discounted price
     * @return true if there's a discount, false otherwise
     */
    public static boolean hasDiscount(double oldPrice, double currentPrice) {
        return calculateDiscountPercentage(oldPrice, currentPrice) > 0;
    }

    /**
     * Format price for display
     * @param price Price value
     * @return Formatted price string (e.g., "$35.00")
     */
    public static String formatPrice(double price) {
        return "$" + String.format("%.2f", price);
    }

    /**
     * Calculate savings amount
     * @param oldPrice Original price
     * @param currentPrice Current discounted price
     * @return Savings amount
     */
    public static double calculateSavings(double oldPrice, double currentPrice) {
        if (oldPrice <= currentPrice) {
            return 0;
        }
        return oldPrice - currentPrice;
    }
}