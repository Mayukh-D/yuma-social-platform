package com.dreamers.h2d.adapters;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.dreamers.h2d.ProfileActivity;
import com.dreamers.h2d.R;
import com.dreamers.h2d.dao.UserDAO;
import com.dreamers.h2d.dao.model.Post;
import com.dreamers.h2d.dao.model.User;

import java.util.List;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.ViewHolder> {

    public interface OnPostClickListener {
        void onPostClick(Post post);
    }

    private final List<Post> posts;
    private final OnPostClickListener listener;

    public PostAdapter(List<Post> posts, OnPostClickListener listener) {
        this.posts = posts;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_post, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Post post = posts.get(position);
        holder.tvTitle.setText(post.topic);

        User poster = UserDAO.getInstance().getByUUID(post.poster);
        holder.tvAuthor.setText("by " + (poster != null ? poster.username() : "unknown"));
        if (poster != null) {
            final User finalPoster = poster;
            holder.tvAuthor.setOnClickListener(v -> {
                Intent intent = new Intent(holder.itemView.getContext(), ProfileActivity.class);
                intent.putExtra("userId", finalPoster.id().toString());
                holder.itemView.getContext().startActivity(intent);
            });
        }

        holder.itemView.setOnClickListener(v -> listener.onPostClick(post));
    }

    @Override
    public int getItemCount() { return posts.size(); }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvAuthor;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvPostTitle);
            tvAuthor = itemView.findViewById(R.id.tvPostAuthor);
        }
    }
}