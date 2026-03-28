package com.hash.bookmyseat.adapter;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.hash.bookmyseat.R;
import com.hash.bookmyseat.model.Event;

import java.util.List;

public class EventAdapter extends RecyclerView.Adapter<EventAdapter.EventViewHolder> {

    private List<Event> events;
    private OnItemClickListener listener;
    private static final String TAG = "EventAdapter";

    public interface OnItemClickListener {
        void onItemClick(Event event);
    }

    public EventAdapter(List<Event> events, OnItemClickListener listener) {
        this.events = events;
        this.listener = listener;
    }

    public void updateList(List<Event> newList) {
        this.events = newList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public EventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_event, parent, false);
        return new EventViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EventViewHolder holder, int position) {
        Event event = events.get(position);

        holder.tvTitle.setText(event.getTitle());
        holder.tvMovie.setText(event.getMovieTitle());
        holder.tvDate.setText(event.getDate());
        holder.tvTime.setText(event.getTime());
        holder.tvVenue.setText(event.getVenue());
        holder.tvPrice.setText("LKR " + event.getPricePerSeat());

        // Load Base64 image
        String posterBase64 = event.getPosterBase64();
        if (posterBase64 != null && !posterBase64.isEmpty()) {
            try {
                byte[] decodedString = Base64.decode(posterBase64, Base64.DEFAULT);
                Bitmap bitmap = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                holder.ivPoster.setImageBitmap(bitmap);
            } catch (Exception e) {
                Log.e(TAG, "Error loading image: " + e.getMessage());
                holder.ivPoster.setImageResource(R.drawable.ic_image_placeholder);
            }
        } else {
            holder.ivPoster.setImageResource(R.drawable.ic_image_placeholder);
        }

        holder.cardView.setOnClickListener(v -> listener.onItemClick(event));
    }

    @Override
    public int getItemCount() {
        return events != null ? events.size() : 0;
    }

    static class EventViewHolder extends RecyclerView.ViewHolder {
        CardView cardView;
        ImageView ivPoster;
        TextView tvTitle, tvMovie, tvDate, tvTime, tvVenue, tvPrice;

        EventViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.cardView);
            ivPoster = itemView.findViewById(R.id.ivPoster);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvMovie = itemView.findViewById(R.id.tvMovie);
            tvDate = itemView.findViewById(R.id.tvDate);
            tvTime = itemView.findViewById(R.id.tvTime);
            tvVenue = itemView.findViewById(R.id.tvVenue);
            tvPrice = itemView.findViewById(R.id.tvPrice);
        }
    }
}