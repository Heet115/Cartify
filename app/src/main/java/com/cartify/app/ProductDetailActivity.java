package com.cartify.app;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
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
    private TextView tvTitle, tvPrice, tvOldPrice, tvDescription, tvRating, tvReviews, tvQuantity;
    private Button btnAddToCart;
    private ImageButton btnIncrease, btnDecrease;
    private ProgressBar progressBar;
    
    private Product currentProduct;
    private String productId;
    private int quantity = 1;

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
        
        // Quantity controls
        tvQuantity = findViewById(R.id.tvQuantity);
        btnIncrease = findViewById(R.id.btnIncrease);
        btnDecrease = findViewById(R.id.btnDecrease);

        // Set click listeners
        btnAddToCart.setOnClickListener(v -> addToCart());
        btnIncrease.setOnClickListener(v -> increaseQuantity());
        btnDecrease.setOnClickListener(v -> decreaseQuantity());
        
        // Initialize quantity display
        updateQuantityDisplay();
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Product Details");
        
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }

    @Override
    public boolean onCreateOptionsMenu(android.view.Menu menu) {
        getMenuInflater().inflate(R.menu.product_detail_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull android.view.MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == R.id.action_favorite) {
            Toast.makeText(this, "Added to favorites!", Toast.LENGTH_SHORT).show();
            return true;
        } else if (itemId == R.id.action_share) {
            shareProduct();
            return true;
        } else if (itemId == R.id.action_compare) {
            Toast.makeText(this, "Compare feature coming soon!", Toast.LENGTH_SHORT).show();
            return true;
        } else if (itemId == R.id.action_add_to_wishlist) {
            Toast.makeText(this, "Added to wishlist!", Toast.LENGTH_SHORT).show();
            return true;
        } else if (itemId == R.id.action_report_issue) {
            Toast.makeText(this, "Report issue feature coming soon!", Toast.LENGTH_SHORT).show();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void shareProduct() {
        if (currentProduct != null) {
            android.content.Intent shareIntent = new android.content.Intent(android.content.Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Check out this product!");
            shareIntent.putExtra(android.content.Intent.EXTRA_TEXT, 
                "Check out " + currentProduct.getTitle() + " for only $" + 
                String.format("%.2f", currentProduct.getPrice()) + " on Cartify!");
            startActivity(android.content.Intent.createChooser(shareIntent, "Share Product"));
        }
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

    private void increaseQuantity() {
        if (quantity < 99) { // Set a reasonable maximum
            quantity++;
            updateQuantityDisplay();
        }
    }

    private void decreaseQuantity() {
        if (quantity > 1) {
            quantity--;
            updateQuantityDisplay();
        }
    }

    private void updateQuantityDisplay() {
        tvQuantity.setText(String.valueOf(quantity));
    }

    private void addToCart() {
        if (currentProduct == null) return;

        String userId = FirebaseHelper.getCurrentUserId();
        if (userId == null) {
            Toast.makeText(this, "Please login first", Toast.LENGTH_SHORT).show();
            return;
        }

        // Check if item already exists in cart
        FirebaseHelper.getUserCartCollection(userId)
            .whereEqualTo("productId", currentProduct.getId())
            .get()
            .addOnSuccessListener(querySnapshot -> {
                if (!querySnapshot.isEmpty()) {
                    // Item exists, update quantity
                    for (com.google.firebase.firestore.QueryDocumentSnapshot document : querySnapshot) {
                        CartItem existingItem = document.toObject(CartItem.class);
                        int newQuantity = existingItem.getQuantity() + quantity;
                        document.getReference().update("quantity", newQuantity)
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(ProductDetailActivity.this, 
                                    "Added " + quantity + " item(s) to cart", Toast.LENGTH_SHORT).show();
                                // Reset quantity to 1 after adding to cart
                                quantity = 1;
                                updateQuantityDisplay();
                            })
                            .addOnFailureListener(e -> 
                                Toast.makeText(ProductDetailActivity.this, 
                                    "Failed to update cart", Toast.LENGTH_SHORT).show());
                        break;
                    }
                } else {
                    // Item doesn't exist, create new cart item
                    CartItem cartItem = new CartItem(
                        currentProduct.getId(),
                        currentProduct.getTitle(),
                        currentProduct.getPrice(),
                        currentProduct.getPicUrl() != null && !currentProduct.getPicUrl().isEmpty() 
                            ? currentProduct.getPicUrl().get(0) : "",
                        quantity,
                        currentProduct.getSize() != null && !currentProduct.getSize().isEmpty() 
                            ? currentProduct.getSize().get(0) : null,
                        currentProduct.getColor() != null && !currentProduct.getColor().isEmpty() 
                            ? currentProduct.getColor().get(0) : null
                    );

                    FirebaseHelper.getUserCartCollection(userId)
                        .add(cartItem)
                        .addOnSuccessListener(documentReference -> {
                            Toast.makeText(ProductDetailActivity.this, 
                                "Added " + quantity + " item(s) to cart", Toast.LENGTH_SHORT).show();
                            // Reset quantity to 1 after adding to cart
                            quantity = 1;
                            updateQuantityDisplay();
                        })
                        .addOnFailureListener(e -> 
                            Toast.makeText(ProductDetailActivity.this, 
                                "Failed to add to cart", Toast.LENGTH_SHORT).show());
                }
            })
            .addOnFailureListener(e -> 
                Toast.makeText(ProductDetailActivity.this, 
                    "Failed to check cart", Toast.LENGTH_SHORT).show());
    }
}