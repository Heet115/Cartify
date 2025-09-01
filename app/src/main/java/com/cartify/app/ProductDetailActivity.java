package com.cartify.app;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.bumptech.glide.Glide;
import com.cartify.app.models.CartItem;
import com.cartify.app.models.Product;
import com.cartify.app.utils.FirebaseHelper;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

/**
 * Product Detail Activity for displaying individual product information
 */
public class ProductDetailActivity extends AppCompatActivity {

    private ImageView ivProduct;
    private TextView tvTitle, tvPrice, tvOldPrice, tvDescription, tvRating, tvReviews;
    private Button btnAddToCart;
    private ProgressBar progressBar;
    
    private Product currentProduct;
    private String productId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_detail);

        productId = getIntent().getStringExtra("product_id");
        if (productId == null) {
            finish();
            return;
        }

        initViews();
        setupToolbar();
        loadProductDetails();
    }

    private void initViews() {
        ivProduct = findViewById(R.id.ivProduct);
        tvTitle = findViewById(R.id.tvTitle);
        tvPrice = findViewById(R.id.tvPrice);
        tvOldPrice = findViewById(R.id.tvOldPrice);
        tvDescription = findViewById(R.id.tvDescription);
        tvRating = findViewById(R.id.tvRating);
        tvReviews = findViewById(R.id.tvReviews);
        btnAddToCart = findViewById(R.id.btnAddToCart);
        progressBar = findViewById(R.id.progressBar);

        btnAddToCart.setOnClickListener(v -> addToCart());
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Product Details");
        
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }

    private void loadProductDetails() {
        progressBar.setVisibility(View.VISIBLE);
        
        FirebaseHelper.getProductsRef().addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int index = 0;
                for (DataSnapshot productSnapshot : snapshot.getChildren()) {
                    if (String.valueOf(index).equals(productId)) {
                        currentProduct = productSnapshot.getValue(Product.class);
                        if (currentProduct != null) {
                            currentProduct.setId(productId);
                            displayProductDetails();
                        }
                        break;
                    }
                    index++;
                }
                progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(ProductDetailActivity.this, 
                    "Failed to load product details", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void displayProductDetails() {
        tvTitle.setText(currentProduct.getTitle());
        tvPrice.setText("$" + String.format("%.2f", currentProduct.getPrice()));
        tvOldPrice.setText("$" + String.format("%.2f", currentProduct.getOldPrice()));
        tvDescription.setText(currentProduct.getDescription());
        tvRating.setText(String.valueOf(currentProduct.getRating()));
        tvReviews.setText("(" + currentProduct.getReview() + " reviews)");

        // Load product image
        if (currentProduct.getPicUrl() != null && !currentProduct.getPicUrl().isEmpty()) {
            Glide.with(this)
                .load(currentProduct.getPicUrl().get(0))
                .placeholder(R.drawable.placeholder_image)
                .error(R.drawable.placeholder_image)
                .into(ivProduct);
        }
    }

    private void addToCart() {
        if (currentProduct == null) return;

        String userId = FirebaseHelper.getCurrentUserId();
        if (userId == null) {
            Toast.makeText(this, "Please login first", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create cart item
        CartItem cartItem = new CartItem(
            currentProduct.getId(),
            currentProduct.getTitle(),
            currentProduct.getPrice(),
            currentProduct.getPicUrl() != null && !currentProduct.getPicUrl().isEmpty() 
                ? currentProduct.getPicUrl().get(0) : "",
            1,
            currentProduct.getSize() != null && !currentProduct.getSize().isEmpty() 
                ? currentProduct.getSize().get(0) : null,
            currentProduct.getColor() != null && !currentProduct.getColor().isEmpty() 
                ? currentProduct.getColor().get(0) : null
        );

        // Add to Firebase
        String cartItemId = FirebaseHelper.getUserCartRef(userId).push().getKey();
        if (cartItemId != null) {
            FirebaseHelper.getUserCartRef(userId).child(cartItemId).setValue(cartItem)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(ProductDetailActivity.this, 
                        "Added to cart", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(ProductDetailActivity.this, 
                        "Failed to add to cart", Toast.LENGTH_SHORT).show();
                });
        }
    }
}