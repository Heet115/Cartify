package com.cartify.app;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.cartify.app.models.CartItem;
import com.cartify.app.models.Order;
import com.cartify.app.utils.FirebaseHelper;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Order Confirmation Activity for placing orders
 */
public class OrderConfirmationActivity extends AppCompatActivity {

    private TextView tvOrderSummary, tvTotalAmount;
    private EditText etDeliveryAddress;
    private Button btnPlaceOrder;
    private ProgressBar progressBar;
    
    private List<CartItem> cartItems;
    private double totalAmount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_confirmation);

        initViews();
        setupToolbar();
        loadCartItems();
    }

    private void initViews() {
        tvOrderSummary = findViewById(R.id.tvOrderSummary);
        tvTotalAmount = findViewById(R.id.tvTotalAmount);
        etDeliveryAddress = findViewById(R.id.etDeliveryAddress);
        btnPlaceOrder = findViewById(R.id.btnPlaceOrder);
        progressBar = findViewById(R.id.progressBar);
        
        cartItems = new ArrayList<>();
        
        btnPlaceOrder.setOnClickListener(v -> placeOrder());
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Order Confirmation");
        
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }

    private void loadCartItems() {
        String userId = FirebaseHelper.getCurrentUserId();
        if (userId == null) {
            finish();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        
        FirebaseHelper.getUserCartRef(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                cartItems.clear();
                totalAmount = 0;
                
                for (DataSnapshot itemSnapshot : snapshot.getChildren()) {
                    CartItem item = itemSnapshot.getValue(CartItem.class);
                    if (item != null) {
                        cartItems.add(item);
                        totalAmount += item.getTotalPrice();
                    }
                }
                
                displayOrderSummary();
                progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(OrderConfirmationActivity.this, 
                    "Failed to load cart items", Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    private void displayOrderSummary() {
        StringBuilder summary = new StringBuilder();
        summary.append("Order Summary:\n\n");
        
        for (CartItem item : cartItems) {
            summary.append(item.getTitle())
                   .append("\nQuantity: ").append(item.getQuantity())
                   .append("\nPrice: $").append(String.format("%.2f", item.getTotalPrice()))
                   .append("\n\n");
        }
        
        tvOrderSummary.setText(summary.toString());
        tvTotalAmount.setText("Total Amount: $" + String.format("%.2f", totalAmount));
    }

    private void placeOrder() {
        String deliveryAddress = etDeliveryAddress.getText().toString().trim();
        
        if (deliveryAddress.isEmpty()) {
            etDeliveryAddress.setError("Please enter delivery address");
            return;
        }

        if (cartItems.isEmpty()) {
            Toast.makeText(this, "No items in cart", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = FirebaseHelper.getCurrentUserId();
        if (userId == null) return;

        progressBar.setVisibility(View.VISIBLE);
        btnPlaceOrder.setEnabled(false);

        // Create order
        String orderId = FirebaseHelper.getOrdersRef().push().getKey();
        String orderDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());
        
        Order order = new Order(
            orderId,
            userId,
            new ArrayList<>(cartItems),
            totalAmount,
            orderDate,
            "Pending",
            deliveryAddress
        );

        // Save order to Firebase
        if (orderId != null) {
            FirebaseHelper.getOrdersRef().child(orderId).setValue(order)
                .addOnSuccessListener(aVoid -> {
                    // Also save to user's orders
                    FirebaseHelper.getUserOrdersRef(userId).child(orderId).setValue(order)
                        .addOnSuccessListener(aVoid1 -> {
                            // Clear cart after successful order
                            clearCart(userId);
                        })
                        .addOnFailureListener(e -> {
                            progressBar.setVisibility(View.GONE);
                            btnPlaceOrder.setEnabled(true);
                            Toast.makeText(OrderConfirmationActivity.this, 
                                "Failed to save order", Toast.LENGTH_SHORT).show();
                        });
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    btnPlaceOrder.setEnabled(true);
                    Toast.makeText(OrderConfirmationActivity.this, 
                        "Failed to place order", Toast.LENGTH_SHORT).show();
                });
        }
    }

    private void clearCart(String userId) {
        FirebaseHelper.getUserCartRef(userId).removeValue()
            .addOnSuccessListener(aVoid -> {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(OrderConfirmationActivity.this, 
                    "Order placed successfully!", Toast.LENGTH_LONG).show();
                
                // Navigate back to main activity
                Intent intent = new Intent(OrderConfirmationActivity.this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
            })
            .addOnFailureListener(e -> {
                progressBar.setVisibility(View.GONE);
                btnPlaceOrder.setEnabled(true);
                Toast.makeText(OrderConfirmationActivity.this, 
                    "Order placed but failed to clear cart", Toast.LENGTH_SHORT).show();
            });
    }
}