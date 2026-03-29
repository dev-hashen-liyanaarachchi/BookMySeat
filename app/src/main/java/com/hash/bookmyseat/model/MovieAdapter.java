package com.hash.bookmyseat.model; // ඔබ මෙය model එකේ තබා ඇත්නම් package එක වෙනස් කරන්න එපා

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.hash.bookmyseat.R;
import com.hash.bookmyseat.activity.MovieDetailsActivity;
import com.hash.bookmyseat.model.Movie;

import java.util.List;

public class MovieAdapter extends RecyclerView.Adapter<MovieAdapter.ViewHolder> {
    private List<Movie> movieList;

    public MovieAdapter(List<Movie> movieList) {
        this.movieList = movieList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_movie, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Movie movie = movieList.get(position);
        holder.title.setText(movie.getTitle());
        holder.genre.setText(movie.getGenre());


        holder.itemView.setOnClickListener(v -> {
            // MovieDetailsActivity එකට මාරු වීම
            Intent intent = new Intent(v.getContext(), MovieDetailsActivity.class);


            intent.putExtra("movie_title", movie.getTitle());
            intent.putExtra("movie_genre", movie.getGenre());

            v.getContext().startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return movieList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView title, genre;

        public ViewHolder(View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.txtMovieTitle);
            genre = itemView.findViewById(R.id.txtMovieGenre);
        }
    }
}