package com.cartify.app.utils;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Helper class for managing search suggestions and history
 */
public class SearchSuggestionsHelper {
    
    private static final String PREFS_NAME = "search_suggestions";
    private static final String KEY_SEARCH_HISTORY = "search_history";
    private static final String KEY_POPULAR_SEARCHES = "popular_searches";
    private static final int MAX_HISTORY_SIZE = 10;
    
    private SharedPreferences prefs;
    
    public SearchSuggestionsHelper(Context context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }
    
    /**
     * Add a search query to history
     */
    public void addToHistory(String query) {
        if (query == null || query.trim().isEmpty()) {
            return;
        }
        
        query = query.trim().toLowerCase();
        Set<String> history = getSearchHistory();
        
        // Remove if already exists to avoid duplicates
        history.remove(query);
        
        // Add to beginning
        List<String> historyList = new ArrayList<>(history);
        historyList.add(0, query);
        
        // Keep only recent searches
        if (historyList.size() > MAX_HISTORY_SIZE) {
            historyList = historyList.subList(0, MAX_HISTORY_SIZE);
        }
        
        // Save back to preferences
        prefs.edit()
            .putStringSet(KEY_SEARCH_HISTORY, new HashSet<>(historyList))
            .apply();
    }
    
    /**
     * Get search history
     */
    public Set<String> getSearchHistory() {
        return prefs.getStringSet(KEY_SEARCH_HISTORY, new HashSet<>());
    }
    
    /**
     * Clear search history
     */
    public void clearHistory() {
        prefs.edit()
            .remove(KEY_SEARCH_HISTORY)
            .apply();
    }
    
    /**
     * Get popular search suggestions
     */
    public List<String> getPopularSearches() {
        List<String> popular = new ArrayList<>();
        popular.add("shoes");
        popular.add("t-shirt");
        popular.add("blazer");
        popular.add("men");
        popular.add("women");
        popular.add("casual");
        popular.add("formal");
        popular.add("winter");
        return popular;
    }
    
    /**
     * Get search suggestions based on query
     */
    public List<String> getSuggestions(String query) {
        List<String> suggestions = new ArrayList<>();
        
        if (query == null || query.trim().isEmpty()) {
            // Return recent searches and popular searches
            suggestions.addAll(getSearchHistory());
            suggestions.addAll(getPopularSearches());
        } else {
            query = query.toLowerCase();
            
            // Add matching history items
            for (String historyItem : getSearchHistory()) {
                if (historyItem.contains(query)) {
                    suggestions.add(historyItem);
                }
            }
            
            // Add matching popular searches
            for (String popular : getPopularSearches()) {
                if (popular.contains(query) && !suggestions.contains(popular)) {
                    suggestions.add(popular);
                }
            }
        }
        
        return suggestions;
    }
}