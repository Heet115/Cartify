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

import java.util.ArrayList;
import java.util.List;

/**
 * Orders Activity for displaying user's order history from Firestore
 */
public class OrdersActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private OrderAdapter orderAdapter;
    private List<Order> orderList;
    private TextView tvEmptyOrders;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_orders);

        initViews();
        setupToolbar();
        setupRecyclerView();
        loadOrders();
    }

    private void initViews() {
        recyclerView = findViewById(R.id.recyclerView);
        tvEmptyOrders = findViewById(R.id.tvEmptyOrders);
        progressBar = findViewById(R.id.progressBar);
        
        orderList = new ArrayList<>();
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("My Orders");
        
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }

    private void setupRecyclerView() {
        orderAdapter = new OrderAdapter(this, orderList);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(orderAdapter);
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