package com.hash.bookmyseat.model;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.hash.bookmyseat.R;

import java.util.List;

public class SeatAdapter extends RecyclerView.Adapter<SeatAdapter.SeatViewHolder> {

    private List<Seat> seats;
    private OnSeatClickListener listener;

    public interface OnSeatClickListener {
        void onSeatClick(Seat seat);
    }

    public SeatAdapter(List<Seat> seats, OnSeatClickListener listener) {
        this.seats = seats;
        this.listener = listener;
    }

    @NonNull
    @Override
    public SeatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_seat, parent, false);
        return new SeatViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SeatViewHolder holder, int position) {
        Seat seat = seats.get(position);
        holder.tvSeatNumber.setText(seat.getSeatNumber());


        switch (seat.getStatus()) {
            case 0:
                holder.itemView.setBackgroundResource(R.drawable.seat_available);
                holder.tvSeatNumber.setTextColor(0xFF000000);
                break;
            case 1:
                holder.itemView.setBackgroundResource(R.drawable.seat_selected);
                holder.tvSeatNumber.setTextColor(0xFFFFFFFF);
                break;
            case 2:
                holder.itemView.setBackgroundResource(R.drawable.seat_booked);
                holder.tvSeatNumber.setTextColor(0xFFCCCCCC);
                break;
        }

        holder.itemView.setOnClickListener(v -> {

            if (seat.getStatus() != 2) {
                listener.onSeatClick(seat);
            }
        });
    }

    @Override
    public int getItemCount() {
        return seats != null ? seats.size() : 0;
    }

    static class SeatViewHolder extends RecyclerView.ViewHolder {
        TextView tvSeatNumber;

        SeatViewHolder(@NonNull View itemView) {
            super(itemView);
            tvSeatNumber = itemView.findViewById(R.id.tvSeatNumber);
        }
    }
}