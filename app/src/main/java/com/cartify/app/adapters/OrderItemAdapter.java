package com.cartify.app.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.cartify.app.R;
import com.cartify.app.models.CartItem;

import java.util.List;

/**
 * Adapter for displaying order items in RecyclerView
 */
public class OrderItemAdapter extends RecyclerView.Adapter<OrderItemAdapter.OrderItemViewHolder> {

    private Context context;
    private List<CartItem> items;

    public OrderItemAdapter(Context context, List<CartItem> items) {
        this.context = context;
        this.items = items;
    }

    @NonNull
    @Override
    public OrderItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_order_item, parent, false);
        return new OrderItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderItemViewHolder holder, int position) {
        CartItem item = items.get(position);
        
        holder.tvTitle.setText(item.getTitle());
        holder.tvPrice.setText("$" + String.format("%.2f", item.getPrice()));
        holder.tvQuantity.setText("Qty: " + item.getQuantity());
        holder.tvTotalPrice.setText("$" + String.format("%.2f", item.getTotalPrice()));
        
        if (item.getSelectedSize() != null && !item.getSelectedSize().isEmpty()) {
            holder.tvSize.setText("Size: " + item.getSelectedSize());
            holder.tvSize.setVisibility(View.VISIBLE);
        } else {
            holder.tvSize.setVisibility(View.GONE);
        }

        // Load product image
        Glide.with(context)
            .load(item.getImageUrl())
            .placeholder(R.drawable.placeholder_image)
            .error(R.drawable.placeholder_image)
            .into(holder.ivProduct);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public void updateItems(List<CartItem> newItems) {
        this.items = newItems;
        notifyDataSetChanged();
    }

    static class OrderItemViewHolder extends RecyclerView.ViewHolder {
        ImageView ivProduct;
        TextView tvTitle, tvPrice, tvQuantity, tvTotalPrice, tvSize;

        public OrderItemViewHolder(@NonNull View itemView) {
            super(itemView);
            ivProduct = itemView.findViewById(R.id.ivProduct);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvPrice = itemView.findViewById(R.id.tvPrice);
            tvQuantity = itemView.findViewById(R.id.tvQuantity);
            tvTotalPrice = itemView.findViewById(R.id.tvTotalPrice);
            tvSize = itemView.findViewById(R.id.tvSize);
        }
    }
}