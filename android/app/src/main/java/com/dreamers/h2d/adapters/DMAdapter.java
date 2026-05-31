package com.dreamers.h2d.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.dreamers.h2d.R;
import com.dreamers.h2d.dao.model.DirectMessage;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

public class DMAdapter extends RecyclerView.Adapter<DMAdapter.ViewHolder> {

    private final List<DirectMessage> messages;
    private final UUID viewer;
    private static final SimpleDateFormat TIME_FMT =
            new SimpleDateFormat("HH:mm", Locale.getDefault());

    public DMAdapter(List<DirectMessage> messages, UUID viewer) {
        this.messages = messages;
        this.viewer = viewer;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_dm, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.display(messages.get(position), viewer);
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView bubble;
        private final TextView timestamp;

        public ViewHolder(View view) {
            super(view);
            bubble    = view.findViewById(R.id.dmBubble);
            timestamp = view.findViewById(R.id.dmTimestamp);
        }

        public void display(DirectMessage dm, UUID viewer) {
            bubble.setText(dm.content());
            timestamp.setText(TIME_FMT.format(new Date(dm.timestamp())));

            if (dm.sender().equals(viewer)) {
                bubble.setBackgroundResource(R.drawable.bubble_sent);
                bubble.setTextColor(itemView.getContext().getColor(R.color.bubble_sent_text));
                ((LinearLayout) itemView).setGravity(android.view.Gravity.END);
                timestamp.setGravity(android.view.Gravity.END);
            } else {
                bubble.setBackgroundResource(R.drawable.bubble_received);
                bubble.setTextColor(itemView.getContext().getColor(R.color.bubble_received_text));
                ((LinearLayout) itemView).setGravity(android.view.Gravity.START);
                timestamp.setGravity(android.view.Gravity.START);
            }
        }
    }
}