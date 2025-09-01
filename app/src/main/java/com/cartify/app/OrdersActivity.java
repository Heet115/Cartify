package com.cartify.app;

import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.cartify.app.adapters.OrderAdapter;
import com.cartify.app.models.Order;
import com.cartify.app.utils.FirebaseHelper;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.List;
import android.content.Intent;

/**
 * Orders Activity for displaying user's order history from Firestore
 */
public class OrdersActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private OrderAdapter orderAdapter;
    private List<Order> orderList;
    private TextView tvEmptyOrders;
    private ProgressBar progressBar;
    private BottomNavigationView bottomNavigation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_orders);

        initViews();
        setupToolbar();
        setupRecyclerView();
        setupBottomNavigation();
        loadOrders();
    }

    private void initViews() {
        recyclerView = findViewById(R.id.recyclerView);
        tvEmptyOrders = findViewById(R.id.tvEmptyOrders);
        progressBar = findViewById(R.id.progressBar);
        bottomNavigation = findViewById(R.id.bottomNavigation);
        
        orderList = new ArrayList<>();
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        getSupportActionBar().setTitle("My Orders");
        
        toolbar.setNavigationOnClickListener(v -> {
            startActivity(new Intent(OrdersActivity.this, MainActivity.class));
            finish();
        });
    }

    @Override
    public boolean onCreateOptionsMenu(android.view.Menu menu) {
        getMenuInflater().inflate(R.menu.orders_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@androidx.annotation.NonNull android.view.MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == android.R.id.home) {
            startActivity(new Intent(OrdersActivity.this, MainActivity.class));
            finish();
            return true;
        } else if (itemId == R.id.action_search_orders) {
            Toast.makeText(this, "Search orders feature coming soon!", Toast.LENGTH_SHORT).show();
            return true;
        } else if (itemId == R.id.action_order_history) {
            Toast.makeText(this, "Order history feature coming soon!", Toast.LENGTH_SHORT).show();
            return true;
        } else if (itemId == R.id.action_track_order) {
            Toast.makeText(this, "Track order feature coming soon!", Toast.LENGTH_SHORT).show();
            return true;
        } else if (itemId == R.id.action_reorder) {
            Toast.makeText(this, "Reorder feature coming soon!", Toast.LENGTH_SHORT).show();
            return true;
        } else if (itemId == R.id.action_download_invoice) {
            Toast.makeText(this, "Download invoice feature coming soon!", Toast.LENGTH_SHORT).show();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void setupRecyclerView() {
        orderAdapter = new OrderAdapter(this, orderList);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(orderAdapter);
    }

    private void setupBottomNavigation() {
        bottomNavigation.setSelectedItemId(R.id.nav_orders);
        bottomNavigation.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_home) {
                startActivity(new Intent(OrdersActivity.this, MainActivity.class));
                finish();
                return true;
            } else if (itemId == R.id.nav_cart) {
                startActivity(new Intent(OrdersActivity.this, CartActivity.class));
                finish();
                return true;
            } else if (itemId == R.id.nav_orders) {
                return true; // Already on orders
            } else if (itemId == R.id.nav_profile) {
                startActivity(new Intent(OrdersActivity.this, ProfileActivity.class));
                finish();
                return true;
            }
            return false;
        });
    }

    private void loadOrders() {
        String userId = FirebaseHelper.getCurrentUserId();
        if (userId == null) {
            finish();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        
        FirebaseHelper.getUserOrdersCollection(userId)
            .orderBy("orderDate", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .addSnapshotListener((querySnapshot, error) -> {
                if (error != null) {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(OrdersActivity.this, 
                        "Failed to load orders: " + error.getMessage(), 
                        Toast.LENGTH_SHORT).show();
                    return;
                }

                orderList.clear();
                if (querySnapshot != null) {
                    for (com.google.firebase.firestore.QueryDocumentSnapshot document : querySnapshot) {
                        Order order = document.toObject(Order.class);
                        if (order != null) {
                            orderList.add(order);
                        }
                    }
                }
                
                orderAdapter.updateOrders(orderList);
                updateEmptyState();
                progressBar.setVisibility(View.GONE);
            });
    }

    private void updateEmptyState() {
        if (orderList.isEmpty()) {
            tvEmptyOrders.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        } else {
            tvEmptyOrders.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        }
    }
}