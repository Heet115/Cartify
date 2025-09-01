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
import androidx.recyclerview.widget.RecyclerView;

import com.cartify.app.adapters.ProductAdapter;
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

    private RecyclerView recyclerView;
    private ProductAdapter productAdapter;
    private List<Product> productList;
    private ProgressBar progressBar;
    private BottomNavigationView bottomNavigation;
    private UserDataHelper userDataHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize UserDataHelper
        userDataHelper = new UserDataHelper(this);

        // Handle system window insets for proper padding
        getWindow().setStatusBarColor(getResources().getColor(R.color.primary_color));
        
        initViews();
        setupRecyclerView();
        setupBottomNavigation();
        loadProducts();
    }

    private void initViews() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        
        recyclerView = findViewById(R.id.recyclerView);
        progressBar = findViewById(R.id.progressBar);
        bottomNavigation = findViewById(R.id.bottomNavigation);
        
        productList = new ArrayList<>();
    }

    private void setupRecyclerView() {
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

    private void loadProducts() {
        progressBar.setVisibility(View.VISIBLE);
        
        FirebaseHelper.getProductsRef().addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                productList.clear();
                int index = 0;
                
                for (DataSnapshot productSnapshot : snapshot.getChildren()) {
                    Product product = productSnapshot.getValue(Product.class);
                    if (product != null) {
                        product.setId(String.valueOf(index++));
                        productList.add(product);
                    }
                }
                
                productAdapter.updateProducts(productList);
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == R.id.action_search) {
            // TODO: Implement search functionality
            Toast.makeText(this, "Search feature coming soon!", Toast.LENGTH_SHORT).show();
            return true;
        } else if (itemId == R.id.action_cart) {
            startActivity(new Intent(this, CartActivity.class));
            return true;
        } else if (itemId == R.id.action_favorites) {
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