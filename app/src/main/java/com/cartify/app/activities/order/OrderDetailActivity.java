package com.cartify.app.activities.order;

import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.cartify.app.R;
import com.cartify.app.adapters.OrderItemAdapter;
import com.cartify.app.models.CartItem;
import com.cartify.app.models.Order;
import com.cartify.app.utils.FirebaseHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * Order Detail Activity for displaying detailed order information
 */
public class OrderDetailActivity extends AppCompatActivity {

    private TextView tvOrderId, tvOrderDate, tvOrderStatus, tvDeliveryAddress, tvTotalAmount;
    private RecyclerView recyclerViewItems;
    private OrderItemAdapter itemAdapter;
    private ProgressBar progressBar;
    
    private String orderId;
    private Order currentOrder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_detail);

        orderId = getIntent().getStringExtra("order_id");
        if (orderId == null) {
            finish();
            return;
        }

        initViews();
        setupToolbar();
        setupRecyclerView();
        loadOrderDetails();
    }

    private void initViews() {
        tvOrderId = findViewById(R.id.tvOrderId);
        tvOrderDate = findViewById(R.id.tvOrderDate);
        tvOrderStatus = findViewById(R.id.tvOrderStatus);
        tvDeliveryAddress = findViewById(R.id.tvDeliveryAddress);
        tvTotalAmount = findViewById(R.id.tvTotalAmount);
        recyclerViewItems = findViewById(R.id.recyclerViewItems);
        progressBar = findViewById(R.id.progressBar);
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Order Details");
        
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }

    @Override
    public boolean onCreateOptionsMenu(android.view.Menu menu) {
        getMenuInflater().inflate(R.menu.orders_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@androidx.annotation.NonNull android.view.MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == R.id.action_track_order) {
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
        itemAdapter = new OrderItemAdapter(this, new ArrayList<>());
        recyclerViewItems.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewItems.setAdapter(itemAdapter);
    }

    private void loadOrderDetails() {
        String userId = FirebaseHelper.getCurrentUserId();
        if (userId == null) {
            finish();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        
        FirebaseHelper.getUserOrdersCollection(userId)
            .document(orderId)
            .get()
            .addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists()) {
                    currentOrder = documentSnapshot.toObject(Order.class);
                    if (currentOrder != null) {
                        displayOrderDetails();
                    }
                } else {
                    Toast.makeText(this, "Order not found", Toast.LENGTH_SHORT).show();
                    finish();
                }
                progressBar.setVisibility(View.GONE);
            })
            .addOnFailureListener(e -> {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(this, "Failed to load order details: " + e.getMessage(), 
                    Toast.LENGTH_SHORT).show();
                finish();
            });
    }

    private void displayOrderDetails() {
        if (currentOrder == null) return;

        tvOrderId.setText("Order #" + (orderId.length() > 8 ? orderId.substring(0, 8) : orderId));
        tvOrderDate.setText(currentOrder.getOrderDate());
        tvOrderStatus.setText(currentOrder.getStatus());
        tvDeliveryAddress.setText(currentOrder.getDeliveryAddress());
        tvTotalAmount.setText("$" + String.format("%.2f", currentOrder.getTotalAmount()));

        // Set status color
        int statusColor;
        switch (currentOrder.getStatus().toLowerCase()) {
            case "pending":
                statusColor = getResources().getColor(android.R.color.holo_orange_dark);
                break;
            case "delivered":
                statusColor = getResources().getColor(android.R.color.holo_green_dark);
                break;
            case "cancelled":
                statusColor = getResources().getColor(android.R.color.holo_red_dark);
                break;
            default:
                statusColor = getResources().getColor(android.R.color.darker_gray);
                break;
        }
        tvOrderStatus.setTextColor(statusColor);

        // Display order items
        if (currentOrder.getItems() != null) {
            itemAdapter.updateItems(currentOrder.getItems());
        }
    }
}