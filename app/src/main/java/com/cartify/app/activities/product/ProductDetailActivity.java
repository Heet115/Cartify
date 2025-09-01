package com.cartify.app.activities.product;

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
import com.cartify.app.R;
import com.cartify.app.models.CartItem;
import com.cartify.app.models.Product;
import com.cartify.app.utils.FirebaseHelper;
import com.cartify.app.utils.InputValidator;
import com.cartify.app.utils.PriceUtils;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

/**
 * Product Detail Activity for displaying individual product information
 */
public class ProductDetailActivity extends AppCompatActivity {

    private ImageView ivProduct;
    private TextView tvTitle, tvPrice, tvOldPrice, tvDescription, tvRating, tvReviews, tvQuantity, tvDiscount;
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
        tvDiscount = findViewById(R.id.tvDiscount);
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
        tvPrice.setText(PriceUtils.formatPrice(currentProduct.getPrice()));
        tvOldPrice.setText(PriceUtils.formatPrice(currentProduct.getOldPrice()));
        tvDescription.setText(currentProduct.getDescription());
        tvRating.setText(String.valueOf(currentProduct.getRating()));
        tvReviews.setText("(" + currentProduct.getReview() + " reviews)");

        // Display dynamic discount percentage using utility
        String discountText = PriceUtils.getDiscountText(currentProduct.getOldPrice(), currentProduct.getPrice());
        if (discountText != null) {
            tvDiscount.setText(discountText);
            tvDiscount.setVisibility(View.VISIBLE);
        } else {
            tvDiscount.setVisibility(View.GONE);
        }

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
        // Validate current quantity before increasing
        InputValidator.ValidationResult validation = 
            InputValidator.validateQuantity(String.valueOf(quantity + 1));
        
        if (!validation.isValid()) {
            Toast.makeText(this, validation.getErrorMessage(), Toast.LENGTH_SHORT).show();
            return;
        }
        
        if (quantity < 99) { // Set a reasonable maximum
            quantity++;
            updateQuantityDisplay();
        } else {
            Toast.makeText(this, "Maximum quantity is 99", Toast.LENGTH_SHORT).show();
        }
    }

    private void decreaseQuantity() {
        if (quantity > 1) {
            quantity--;
            updateQuantityDisplay();
        } else {
            Toast.makeText(this, "Minimum quantity is 1", Toast.LENGTH_SHORT).show();
        }
    }

    private void updateQuantityDisplay() {
        tvQuantity.setText(String.valueOf(quantity));
    }

    private void addToCart() {
        if (currentProduct == null) {
            Toast.makeText(this, "Product information not available", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = FirebaseHelper.getCurrentUserId();
        if (userId == null) {
            Toast.makeText(this, "Please log in to add items to cart", Toast.LENGTH_LONG).show();
            return;
        }

        // Validate quantity before adding to cart
        InputValidator.ValidationResult quantityValidation = 
            InputValidator.validateQuantity(String.valueOf(quantity));
        
        if (!quantityValidation.isValid()) {
            Toast.makeText(this, quantityValidation.getErrorMessage(), Toast.LENGTH_SHORT).show();
            return;
        }

        // Validate product data
        if (currentProduct.getTitle() == null || currentProduct.getTitle().trim().isEmpty()) {
            Toast.makeText(this, "Invalid product title", Toast.LENGTH_SHORT).show();
            return;
        }

        InputValidator.ValidationResult priceValidation = 
            InputValidator.validatePrice(String.valueOf(currentProduct.getPrice()));
        
        if (!priceValidation.isValid()) {
            Toast.makeText(this, "Invalid product price", Toast.LENGTH_SHORT).show();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        btnAddToCart.setEnabled(false);

        // Check if item already exists in cart
        FirebaseHelper.getUserCartCollection(userId)
            .whereEqualTo("productId", currentProduct.getId())
            .get()
            .addOnSuccessListener(querySnapshot -> {
                progressBar.setVisibility(View.GONE);
                btnAddToCart.setEnabled(true);
                
                if (!querySnapshot.isEmpty()) {
                    // Item exists, update quantity
                    for (com.google.firebase.firestore.QueryDocumentSnapshot document : querySnapshot) {
                        CartItem existingItem = document.toObject(CartItem.class);
                        int newQuantity = existingItem.getQuantity() + quantity;
                        
                        // Validate new total quantity
                        InputValidator.ValidationResult newQuantityValidation = 
                            InputValidator.validateQuantity(String.valueOf(newQuantity));
                        
                        if (!newQuantityValidation.isValid()) {
                            Toast.makeText(ProductDetailActivity.this, 
                                "Cannot add more items: " + newQuantityValidation.getErrorMessage(), 
                                Toast.LENGTH_LONG).show();
                            return;
                        }

                        if (newQuantity > 99) {
                            Toast.makeText(ProductDetailActivity.this, 
                                "Cannot add more items. Maximum 99 per product in cart.", 
                                Toast.LENGTH_LONG).show();
                            return;
                        }

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
                    // Item doesn't exist, create new cart item with sanitized data
                    String sanitizedTitle = InputValidator.sanitizeInput(currentProduct.getTitle());
                    String sanitizedImageUrl = currentProduct.getPicUrl() != null && !currentProduct.getPicUrl().isEmpty() 
                        ? InputValidator.sanitizeInput(currentProduct.getPicUrl().get(0)) : "";
                    
                    CartItem cartItem = new CartItem(
                        currentProduct.getId(),
                        sanitizedTitle,
                        currentProduct.getPrice(),
                        sanitizedImageUrl,
                        quantity,
                        currentProduct.getSize() != null && !currentProduct.getSize().isEmpty() 
                            ? InputValidator.sanitizeInput(currentProduct.getSize().get(0)) : null,
                        currentProduct.getColor() != null && !currentProduct.getColor().isEmpty() 
                            ? InputValidator.sanitizeInput(currentProduct.getColor().get(0)) : null
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
            .addOnFailureListener(e -> {
                progressBar.setVisibility(View.GONE);
                btnAddToCart.setEnabled(true);
                Toast.makeText(ProductDetailActivity.this, 
                    "Failed to check cart", Toast.LENGTH_SHORT).show();
            });
    }
}