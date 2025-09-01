package com.cartify.app.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.cartify.app.ProductDetailActivity;
import com.cartify.app.R;
import com.cartify.app.models.CartItem;
import com.cartify.app.models.Product;
import com.cartify.app.utils.FirebaseHelper;
import com.google.android.material.button.MaterialButton;

import java.util.List;

/**
 * Adapter for displaying products in RecyclerView
 */
public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ProductViewHolder> {

    private Context context;
    private List<Product> productList;

    public ProductAdapter(Context context, List<Product> productList) {
        this.context = context;
        this.productList = productList;
    }

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_product, parent, false);
        return new ProductViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
        Product product = productList.get(position);
        
        holder.tvTitle.setText(product.getTitle());
        holder.tvPrice.setText("$" + String.format("%.2f", product.getPrice()));
        holder.tvOldPrice.setText("$" + String.format("%.2f", product.getOldPrice()));
        holder.tvRating.setText(String.valueOf(product.getRating()));
        holder.tvReviews.setText("(" + product.getReview() + ")");
        
        // Load product image using Glide
        if (product.getPicUrl() != null && !product.getPicUrl().isEmpty()) {
            Glide.with(context)
                .load(product.getPicUrl().get(0))
                .placeholder(R.drawable.placeholder_image)
                .error(R.drawable.placeholder_image)
                .into(holder.ivProduct);
        }

        // Set click listener to open product details
        holder.cardView.setOnClickListener(v -> {
            Intent intent = new Intent(context, ProductDetailActivity.class);
            intent.putExtra("product_id", product.getId());
            context.startActivity(intent);
        });

        // Add to cart button click listener
        holder.btnAddToCart.setOnClickListener(v -> {
            addToCart(product);
        });
    }

    private void addToCart(Product product) {
        String userId = FirebaseHelper.getCurrentUserId();
        if (userId == null) {
            Toast.makeText(context, "Please login first", Toast.LENGTH_SHORT).show();
            return;
        }

        // Check if item already exists in cart
        FirebaseHelper.getUserCartCollection(userId)
            .whereEqualTo("productId", product.getId())
            .get()
            .addOnSuccessListener(querySnapshot -> {
                if (!querySnapshot.isEmpty()) {
                    // Item exists, update quantity
                    for (com.google.firebase.firestore.QueryDocumentSnapshot document : querySnapshot) {
                        CartItem existingItem = document.toObject(CartItem.class);
                        int newQuantity = existingItem.getQuantity() + 1;
                        document.getReference().update("quantity", newQuantity)
                            .addOnSuccessListener(aVoid -> 
                                Toast.makeText(context, "Updated cart quantity", Toast.LENGTH_SHORT).show())
                            .addOnFailureListener(e -> 
                                Toast.makeText(context, "Failed to update cart", Toast.LENGTH_SHORT).show());
                        break;
                    }
                } else {
                    // Item doesn't exist, create new cart item
                    CartItem cartItem = new CartItem(
                        product.getId(),
                        product.getTitle(),
                        product.getPrice(),
                        product.getPicUrl() != null && !product.getPicUrl().isEmpty() 
                            ? product.getPicUrl().get(0) : "",
                        1,
                        product.getSize() != null && !product.getSize().isEmpty() 
                            ? product.getSize().get(0) : null,
                        product.getColor() != null && !product.getColor().isEmpty() 
                            ? product.getColor().get(0) : null
                    );

                    FirebaseHelper.getUserCartCollection(userId)
                        .add(cartItem)
                        .addOnSuccessListener(documentReference -> 
                            Toast.makeText(context, "Added to cart", Toast.LENGTH_SHORT).show())
                        .addOnFailureListener(e -> 
                            Toast.makeText(context, "Failed to add to cart", Toast.LENGTH_SHORT).show());
                }
            })
            .addOnFailureListener(e -> 
                Toast.makeText(context, "Failed to check cart", Toast.LENGTH_SHORT).show());
    }

    @Override
    public int getItemCount() {
        return productList.size();
    }

    public void updateProducts(List<Product> newProducts) {
        this.productList = newProducts;
        notifyDataSetChanged();
    }

    static class ProductViewHolder extends RecyclerView.ViewHolder {
        CardView cardView;
        ImageView ivProduct;
        TextView tvTitle, tvPrice, tvOldPrice, tvRating, tvReviews;
        MaterialButton btnAddToCart;

        public ProductViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.cardView);
            ivProduct = itemView.findViewById(R.id.ivProduct);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvPrice = itemView.findViewById(R.id.tvPrice);
            tvOldPrice = itemView.findViewById(R.id.tvOldPrice);
            tvRating = itemView.findViewById(R.id.tvRating);
            tvReviews = itemView.findViewById(R.id.tvReviews);
            btnAddToCart = itemView.findViewById(R.id.btnAddToCart);
        }
    }
}