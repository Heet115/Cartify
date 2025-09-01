package com.cartify.app;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.cartify.app.adapters.CartAdapter;
import com.cartify.app.models.CartItem;
import com.cartify.app.utils.FirebaseHelper;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.List;

/**
 * Cart Activity for displaying and managing cart items
 */
public class CartActivity extends AppCompatActivity implements CartAdapter.OnCartItemListener {

    private RecyclerView recyclerView;
    private CartAdapter cartAdapter;
    private List<CartItem> cartItems;
    private TextView tvTotalAmount, tvEmptyCart;
    private Button btnCheckout;
    private ProgressBar progressBar;
    private BottomNavigationView bottomNavigation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);

        initViews();
        setupToolbar();
        setupRecyclerView();
        setupBottomNavigation();
        loadCartItems();
    }

    private void initViews() {
        recyclerView = findViewById(R.id.recyclerView);
        tvTotalAmount = findViewById(R.id.tvTotalAmount);
        tvEmptyCart = findViewById(R.id.tvEmptyCart);
        btnCheckout = findViewById(R.id.btnCheckout);
        progressBar = findViewById(R.id.progressBar);
        bottomNavigation = findViewById(R.id.bottomNavigation);
        
        cartItems = new ArrayList<>();
        
        btnCheckout.setOnClickListener(v -> proceedToCheckout());
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        getSupportActionBar().setTitle("Shopping Cart");
        
        toolbar.setNavigationOnClickListener(v -> {
            startActivity(new Intent(CartActivity.this, MainActivity.class));
            finish();
        });
    }

    @Override
    public boolean onCreateOptionsMenu(android.view.Menu menu) {
        getMenuInflater().inflate(R.menu.cart_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull android.view.MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == android.R.id.home) {
            startActivity(new Intent(CartActivity.this, MainActivity.class));
            finish();
            return true;
        } else if (itemId == R.id.action_clear_cart) {
            clearCart();
            return true;
        } else if (itemId == R.id.action_save_for_later) {
            Toast.makeText(this, "Save for later feature coming soon!", Toast.LENGTH_SHORT).show();
            return true;
        } else if (itemId == R.id.action_share_cart) {
            Toast.makeText(this, "Share cart feature coming soon!", Toast.LENGTH_SHORT).show();
            return true;
        } else if (itemId == R.id.action_continue_shopping) {
            startActivity(new Intent(this, MainActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void clearCart() {
        String userId = FirebaseHelper.getCurrentUserId();
        if (userId == null) return;

        FirebaseHelper.getUserCartCollection(userId)
            .get()
            .addOnSuccessListener(querySnapshot -> {
                for (com.google.firebase.firestore.QueryDocumentSnapshot document : querySnapshot) {
                    document.getReference().delete();
                }
                Toast.makeText(this, "Cart cleared successfully", Toast.LENGTH_SHORT).show();
            })
            .addOnFailureListener(e -> 
                Toast.makeText(this, "Failed to clear cart", Toast.LENGTH_SHORT).show());
    }

    private void setupRecyclerView() {
        cartAdapter = new CartAdapter(this, cartItems, this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(cartAdapter);
    }

    private void setupBottomNavigation() {
        bottomNavigation.setSelectedItemId(R.id.nav_cart);
        bottomNavigation.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_home) {
                startActivity(new Intent(CartActivity.this, MainActivity.class));
                finish();
                return true;
            } else if (itemId == R.id.nav_cart) {
                return true; // Already on cart
            } else if (itemId == R.id.nav_orders) {
                startActivity(new Intent(CartActivity.this, OrdersActivity.class));
                finish();
                return true;
            } else if (itemId == R.id.nav_profile) {
                startActivity(new Intent(CartActivity.this, ProfileActivity.class));
                finish();
                return true;
            }
            return false;
        });
    }

    private void loadCartItems() {
        String userId = FirebaseHelper.getCurrentUserId();
        if (userId == null) return;

        progressBar.setVisibility(View.VISIBLE);
        
        FirebaseHelper.getUserCartCollection(userId)
            .addSnapshotListener((querySnapshot, error) -> {
                if (error != null) {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(CartActivity.this, 
                        "Failed to load cart items: " + error.getMessage(), 
                        Toast.LENGTH_SHORT).show();
                    return;
                }

                cartItems.clear();
                if (querySnapshot != null) {
                    for (com.google.firebase.firestore.QueryDocumentSnapshot document : querySnapshot) {
                        CartItem item = document.toObject(CartItem.class);
                        if (item != null) {
                            cartItems.add(item);
                        }
                    }
                }
                
                cartAdapter.updateCartItems(cartItems);
                updateTotalAmount();
                updateEmptyState();
                progressBar.setVisibility(View.GONE);
            });
    }

    private void updateTotalAmount() {
        double total = 0;
        for (CartItem item : cartItems) {
            total += item.getTotalPrice();
        }
        tvTotalAmount.setText("Total: $" + String.format("%.2f", total));
    }

    private void updateEmptyState() {
        if (cartItems.isEmpty()) {
            tvEmptyCart.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
            btnCheckout.setVisibility(View.GONE);
        } else {
            tvEmptyCart.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
            btnCheckout.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onQuantityChanged(CartItem item, int newQuantity) {
        String userId = FirebaseHelper.getCurrentUserId();
        if (userId == null) return;

        // Find and update the item in Firestore
        FirebaseHelper.getUserCartCollection(userId)
            .whereEqualTo("productId", item.getProductId())
            .get()
            .addOnSuccessListener(querySnapshot -> {
                for (com.google.firebase.firestore.QueryDocumentSnapshot document : querySnapshot) {
                    document.getReference().update("quantity", newQuantity)
                        .addOnFailureListener(e -> 
                            Toast.makeText(CartActivity.this, "Failed to update quantity", Toast.LENGTH_SHORT).show());
                    break;
                }
            })
            .addOnFailureListener(e -> 
                Toast.makeText(CartActivity.this, "Failed to update quantity", Toast.LENGTH_SHORT).show());
    }

    @Override
    public void onItemRemoved(CartItem item) {
        String userId = FirebaseHelper.getCurrentUserId();
        if (userId == null) return;

        // Remove item from Firestore
        FirebaseHelper.getUserCartCollection(userId)
            .whereEqualTo("productId", item.getProductId())
            .get()
            .addOnSuccessListener(querySnapshot -> {
                for (com.google.firebase.firestore.QueryDocumentSnapshot document : querySnapshot) {
                    document.getReference().delete()
                        .addOnSuccessListener(aVoid -> 
                            Toast.makeText(CartActivity.this, "Item removed from cart", Toast.LENGTH_SHORT).show())
                        .addOnFailureListener(e -> 
                            Toast.makeText(CartActivity.this, "Failed to remove item", Toast.LENGTH_SHORT).show());
                    break;
                }
            })
            .addOnFailureListener(e -> 
                Toast.makeText(CartActivity.this, "Failed to remove item", Toast.LENGTH_SHORT).show());
    }

    private void proceedToCheckout() {
        if (cartItems.isEmpty()) {
            Toast.makeText(this, "Cart is empty", Toast.LENGTH_SHORT).show();
            return;
        }

        Intent intent = new Intent(this, OrderConfirmationActivity.class);
        startActivity(intent);
    }
}