package com.cartify.app.activities.product;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.cartify.app.R;
import com.cartify.app.activities.cart.CartActivity;
import com.cartify.app.adapters.ProductAdapter;
import com.cartify.app.models.Product;
import com.cartify.app.utils.FirebaseHelper;
import com.cartify.app.utils.SearchSuggestionsHelper;
import com.cartify.app.utils.InputValidator;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

/**
 * Search Activity for searching products
 */
public class SearchActivity extends AppCompatActivity {

    private EditText searchEditText;
    private RecyclerView searchResultsRecyclerView;
    private TextView noResultsTextView;
    private TextView resultsCountText;
    private TextView sortButton;
    private LinearLayout resultsHeader;
    private LinearLayout emptyStateLayout;
    private ProgressBar progressBar;
    
    private ProductAdapter searchAdapter;
    private List<Product> allProducts;
    private List<Product> filteredProducts;
    private SearchSuggestionsHelper suggestionsHelper;
    
    private String currentQuery = "";
    
    // Filter parameters
    private double maxPrice = Double.MAX_VALUE;
    private float minRating = 0.0f;
    private List<Integer> selectedCategoryIds = new ArrayList<>();
    
    private static final int FILTER_REQUEST_CODE = 1001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        initViews();
        setupRecyclerView();
        setupSearch();
        loadProducts();
    }

    private void initViews() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Search Products");
        }

        searchEditText = findViewById(R.id.searchEditText);
        searchResultsRecyclerView = findViewById(R.id.searchResultsRecyclerView);
        noResultsTextView = findViewById(R.id.noResultsTextView);
        resultsCountText = findViewById(R.id.resultsCountText);
        sortButton = findViewById(R.id.sortButton);
        resultsHeader = findViewById(R.id.resultsHeader);
        emptyStateLayout = findViewById(R.id.emptyStateLayout);
        progressBar = findViewById(R.id.progressBar);

        allProducts = new ArrayList<>();
        filteredProducts = new ArrayList<>();
        suggestionsHelper = new SearchSuggestionsHelper(this);
        
        // Auto-focus on search field
        searchEditText.requestFocus();
    }

    private void setupRecyclerView() {
        searchAdapter = new ProductAdapter(this, filteredProducts);
        searchResultsRecyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        searchResultsRecyclerView.setAdapter(searchAdapter);
    }

    private void setupSearch() {
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String rawQuery = s.toString();
                
                // Validate and sanitize search input
                if (rawQuery.length() > 100) {
                    searchEditText.setError("Search query too long (max 100 characters)");
                    return;
                }
                
                // Sanitize the input
                currentQuery = InputValidator.sanitizeInput(rawQuery.trim());
                
                // Clear any previous errors
                searchEditText.setError(null);
                
                // Perform search
                performSearch(currentQuery);
                
                // Save to search history when user stops typing (with validation)
                if (!currentQuery.isEmpty() && currentQuery.length() > 2) {
                    InputValidator.ValidationResult validation = InputValidator.validateSearchQuery(currentQuery);
                    if (validation.isValid()) {
                        suggestionsHelper.addToHistory(currentQuery);
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void loadProducts() {
        progressBar.setVisibility(View.VISIBLE);
        
        FirebaseHelper.getProductsRef().addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                allProducts.clear();
                int index = 0;
                
                for (DataSnapshot productSnapshot : snapshot.getChildren()) {
                    Product product = productSnapshot.getValue(Product.class);
                    if (product != null) {
                        product.setId(String.valueOf(index++));
                        allProducts.add(product);
                    }
                }
                
                progressBar.setVisibility(View.GONE);
                
                // Perform search with current query if any
                if (!currentQuery.isEmpty()) {
                    performSearch(currentQuery);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(SearchActivity.this, 
                    "Failed to load products: " + error.getMessage(), 
                    Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void performSearch(String query) {
        filteredProducts.clear();
        
        if (query.isEmpty()) {
            // Show all products when search is empty
            filteredProducts.addAll(allProducts);
        } else {
            // Validate search query before processing
            InputValidator.ValidationResult validation = InputValidator.validateSearchQuery(query);
            if (!validation.isValid()) {
                // Show error but don't crash - just show no results
                updateUI();
                return;
            }
            
            // Filter products based on search query
            String lowerCaseQuery = query.toLowerCase();
            
            for (Product product : allProducts) {
                if (matchesSearchQuery(product, lowerCaseQuery)) {
                    filteredProducts.add(product);
                }
            }
        }
        
        updateUI();
    }

    private boolean matchesSearchQuery(Product product, String query) {
        // Apply filters first
        if (!matchesFilters(product)) {
            return false;
        }
        
        // If no search query, show all products that match filters
        if (query.isEmpty()) {
            return true;
        }
        
        // Search in product title
        if (product.getTitle() != null && 
            product.getTitle().toLowerCase().contains(query)) {
            return true;
        }
        
        // Search in product description
        if (product.getDescription() != null && 
            product.getDescription().toLowerCase().contains(query)) {
            return true;
        }
        
        // Search by price range (if query is numeric)
        try {
            double searchPrice = Double.parseDouble(query);
            
            // Validate price input
            InputValidator.ValidationResult priceValidation = InputValidator.validatePrice(query);
            if (priceValidation.isValid() && Math.abs(product.getPrice() - searchPrice) < 10) {
                return true; // Within $10 range
            }
        } catch (NumberFormatException e) {
            // Not a number, continue with other searches
        }
        
        return false;
    }

    private boolean matchesFilters(Product product) {
        // Price filter
        if (product.getPrice() > maxPrice) {
            return false;
        }
        
        // Rating filter
        if (product.getRating() < minRating) {
            return false;
        }
        
        // Category filter (if any categories are selected)
        if (!selectedCategoryIds.isEmpty()) {
            // For now, we don't have category IDs in products
            // This would need to be implemented based on your product-category relationship
            // return selectedCategoryIds.contains(product.getCategoryId());
        }
        
        return true;
    }

    private void updateUI() {
        searchAdapter.updateProducts(filteredProducts);
        
        if (filteredProducts.isEmpty()) {
            if (!currentQuery.isEmpty()) {
                // Show no results state
                emptyStateLayout.setVisibility(View.VISIBLE);
                noResultsTextView.setText("No products found for \"" + currentQuery + "\"");
                searchResultsRecyclerView.setVisibility(View.GONE);
                resultsHeader.setVisibility(View.GONE);
            } else {
                // Show initial state
                emptyStateLayout.setVisibility(View.GONE);
                searchResultsRecyclerView.setVisibility(View.GONE);
                resultsHeader.setVisibility(View.GONE);
            }
        } else {
            // Show results
            emptyStateLayout.setVisibility(View.GONE);
            searchResultsRecyclerView.setVisibility(View.VISIBLE);
            resultsHeader.setVisibility(View.VISIBLE);
            
            // Update results count
            int count = filteredProducts.size();
            String countText = count == 1 ? "Found 1 product" : "Found " + count + " products";
            if (!currentQuery.isEmpty()) {
                countText += " for \"" + currentQuery + "\"";
            }
            resultsCountText.setText(countText);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.search_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == android.R.id.home) {
            onBackPressed();
            return true;
        } else if (itemId == R.id.action_filter) {
            Toast.makeText(this, "Filter feature coming soon!", Toast.LENGTH_SHORT).show();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        
        if (requestCode == FILTER_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            // Get filter parameters
            maxPrice = data.getDoubleExtra("maxPrice", Double.MAX_VALUE);
            minRating = data.getFloatExtra("minRating", 0.0f);
            selectedCategoryIds = data.getIntegerArrayListExtra("selectedCategories");
            if (selectedCategoryIds == null) {
                selectedCategoryIds = new ArrayList<>();
            }
            
            // Reapply search with new filters
            performSearch(currentQuery);
            
            Toast.makeText(this, "Filters applied", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}