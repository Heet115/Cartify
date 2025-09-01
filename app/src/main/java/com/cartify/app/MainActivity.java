package com.cartify.app;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.cartify.app.adapters.BannerAdapter;
import com.cartify.app.adapters.CategoryAdapter;
import com.cartify.app.adapters.ProductAdapter;
import com.cartify.app.models.Banner;
import com.cartify.app.models.Category;
import com.cartify.app.models.Product;
import com.cartify.app.utils.FirebaseHelper;
import com.cartify.app.utils.UserDataHelper;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

/**
 * Main Activity displaying product catalog
 */
public class MainActivity extends AppCompatActivity {

    private RecyclerView recyclerView, recyclerViewCategories;
    private ViewPager2 viewPagerBanners;
    private ProductAdapter productAdapter;
    private CategoryAdapter categoryAdapter;
    private BannerAdapter bannerAdapter;
    private List<Product> productList, allProductsList;
    private List<Category> categoryList;
    private List<Banner> bannerList;
    private ProgressBar progressBar;
    private BottomNavigationView bottomNavigation;
    private UserDataHelper userDataHelper;
    private int selectedCategoryId = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize UserDataHelper
        userDataHelper = new UserDataHelper(this);

        // Handle system window insets for proper padding
        getWindow().setStatusBarColor(getResources().getColor(R.color.primary_color));
        
        initViews();
        setupRecyclerViews();
        setupBottomNavigation();
        loadData();
    }

    private void initViews() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        
        recyclerView = findViewById(R.id.recyclerView);
        recyclerViewCategories = findViewById(R.id.recyclerViewCategories);
        viewPagerBanners = findViewById(R.id.viewPagerBanners);
        progressBar = findViewById(R.id.progressBar);
        bottomNavigation = findViewById(R.id.bottomNavigation);
        
        // Setup search card click
        findViewById(R.id.searchCard).setOnClickListener(v -> 
            startActivity(new Intent(MainActivity.this, SearchActivity.class)));
        
        productList = new ArrayList<>();
        allProductsList = new ArrayList<>();
        categoryList = new ArrayList<>();
        bannerList = new ArrayList<>();
    }

    private void setupRecyclerViews() {
        // Setup categories RecyclerView
        categoryAdapter = new CategoryAdapter(this, categoryList);
        recyclerViewCategories.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        recyclerViewCategories.setAdapter(categoryAdapter);
        
        categoryAdapter.setOnCategoryClickListener((category, position) -> {
            selectedCategoryId = category.getId();
            filterProductsByCategory();
        });

        // Setup banners ViewPager2
        bannerAdapter = new BannerAdapter(this, bannerList);
        viewPagerBanners.setAdapter(bannerAdapter);

        // Setup products RecyclerView
        productAdapter = new ProductAdapter(this, productList);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        recyclerView.setAdapter(productAdapter);
    }

    private void setupBottomNavigation() {
        bottomNavigation.setSelectedItemId(R.id.nav_home);
        bottomNavigation.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_home) {
                return true;
            } else if (itemId == R.id.nav_cart) {
                startActivity(new Intent(MainActivity.this, CartActivity.class));
                finish();
                return true;
            } else if (itemId == R.id.nav_orders) {
                startActivity(new Intent(MainActivity.this, OrdersActivity.class));
                finish();
                return true;
            } else if (itemId == R.id.nav_profile) {
                startActivity(new Intent(MainActivity.this, ProfileActivity.class));
                finish();
                return true;
            }
            return false;
        });
    }

    private void loadData() {
        progressBar.setVisibility(View.VISIBLE);
        loadCategories();
        loadBanners();
        loadProducts();
    }

    private void loadCategories() {
        FirebaseHelper.getCategoriesRef().addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                categoryList.clear();
                
                for (DataSnapshot categorySnapshot : snapshot.getChildren()) {
                    Category category = categorySnapshot.getValue(Category.class);
                    if (category != null) {
                        categoryList.add(category);
                    }
                }
                
                categoryAdapter.updateCategories(categoryList);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(MainActivity.this, 
                    "Failed to load categories: " + error.getMessage(), 
                    Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadBanners() {
        FirebaseHelper.getBannersRef().addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                bannerList.clear();
                
                for (DataSnapshot bannerSnapshot : snapshot.getChildren()) {
                    Banner banner = bannerSnapshot.getValue(Banner.class);
                    if (banner != null) {
                        bannerList.add(banner);
                    }
                }
                
                bannerAdapter.updateBanners(bannerList);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(MainActivity.this, 
                    "Failed to load banners: " + error.getMessage(), 
                    Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadProducts() {
        FirebaseHelper.getProductsRef().addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                allProductsList.clear();
                int index = 0;
                
                for (DataSnapshot productSnapshot : snapshot.getChildren()) {
                    Product product = productSnapshot.getValue(Product.class);
                    if (product != null) {
                        product.setId(String.valueOf(index++));
                        allProductsList.add(product);
                    }
                }
                
                filterProductsByCategory();
                progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(MainActivity.this, 
                    "Failed to load products: " + error.getMessage(), 
                    Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void filterProductsByCategory() {
        productList.clear();
        
        if (selectedCategoryId == 0) { // "All" category
            productList.addAll(allProductsList);
        } else {
            // For now, show all products since we don't have category filtering logic
            // You can implement category-based filtering here based on your business logic
            productList.addAll(allProductsList);
        }
        
        productAdapter.updateProducts(productList);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == R.id.action_favorites) {
            // TODO: Implement favorites functionality
            Toast.makeText(this, "Favorites feature coming soon!", Toast.LENGTH_SHORT).show();
            return true;
        } else if (itemId == R.id.action_orders) {
            startActivity(new Intent(this, OrdersActivity.class));
            return true;
        } else if (itemId == R.id.action_profile) {
            startActivity(new Intent(this, ProfileActivity.class));
            return true;
        } else if (itemId == R.id.action_settings) {
            // TODO: Implement settings functionality
            Toast.makeText(this, "Settings feature coming soon!", Toast.LENGTH_SHORT).show();
            return true;
        } else if (itemId == R.id.action_help) {
            // TODO: Implement help functionality
            Toast.makeText(this, "Help & Support feature coming soon!", Toast.LENGTH_SHORT).show();
            return true;
        } else if (itemId == R.id.action_about) {
            // TODO: Implement about functionality
            Toast.makeText(this, "About feature coming soon!", Toast.LENGTH_SHORT).show();
            return true;
        } else if (itemId == R.id.action_logout) {
            // Logout from both Firebase and local storage
            FirebaseHelper.getAuth().signOut();
            userDataHelper.logoutUser();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}