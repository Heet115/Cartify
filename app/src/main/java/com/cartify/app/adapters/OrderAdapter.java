package com.cartify.app.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.cartify.app.R;
import com.cartify.app.models.Order;

import java.util.List;

/**
 * Adapter for displaying orders in RecyclerView
 */
public class OrderAdapter extends RecyclerView.Adapter<OrderAdapter.OrderViewHolder> {

    private Context context;
    private List<Order> orderList;

    public OrderAdapter(Context context, List<Order> orderList) {
        this.context = context;
        this.orderList = orderList;
    }

    @NonNull
    @Override
    public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_order, parent, false);
        return new OrderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderViewHolder holder, int position) {
        Order order = orderList.get(position);
        
        holder.tvOrderId.setText("Order #" + (order.getOrderId() != null ? 
            order.getOrderId().substring(0, Math.min(8, order.getOrderId().length())) : "N/A"));
        holder.tvOrderDate.setText(order.getOrderDate());
        holder.tvTotalAmount.setText("$" + String.format("%.2f", order.getTotalAmount()));
        holder.tvStatus.setText(order.getStatus());
        holder.tvItemCount.setText(order.getItems().size() + " items");
        
        // Set status color
        int statusColor;
        switch (order.getStatus().toLowerCase()) {
            case "pending":
                statusColor = context.getResources().getColor(android.R.color.holo_orange_dark);
                break;
            case "delivered":
                statusColor = context.getResources().getColor(android.R.color.holo_green_dark);
                break;
            case "cancelled":
                statusColor = context.getResources().getColor(android.R.color.holo_red_dark);
                break;
            default:
                statusColor = context.getResources().getColor(android.R.color.darker_gray);
                break;
        }
        holder.tvStatus.setTextColor(statusColor);
    }

    @Override
    public int getItemCount() {
        return orderList.size();
    }

    public void updateOrders(List<Order> newOrders) {
        this.orderList = newOrders;
        notifyDataSetChanged();
    }

    static class OrderViewHolder extends RecyclerView.ViewHolder {
        CardView cardView;
        TextView tvOrderId, tvOrderDate, tvTotalAmount, tvStatus, tvItemCount;

        public OrderViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.cardView);
            tvOrderId = itemView.findViewById(R.id.tvOrderId);
            tvOrderDate = itemView.findViewById(R.id.tvOrderDate);
            tvTotalAmount = itemView.findViewById(R.id.tvTotalAmount);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            tvItemCount = itemView.findViewById(R.id.tvItemCount);
        }
    }
}