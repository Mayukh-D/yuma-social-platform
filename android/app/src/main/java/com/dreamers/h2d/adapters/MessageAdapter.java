package com.dreamers.h2d.adapters;

import android.app.AlertDialog;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.dreamers.h2d.ProfileActivity;
import com.dreamers.h2d.moderation.ModerationTools;
import com.dreamers.h2d.persistentdata.DataManager;
import com.dreamers.h2d.pinning.PinTools;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.dreamers.h2d.R;
import com.dreamers.h2d.dao.ReportDAO;
import com.dreamers.h2d.dao.UserDAO;
import com.dreamers.h2d.dao.model.Message;
import com.dreamers.h2d.dao.model.ReactionType;
import com.dreamers.h2d.dao.model.User;
import com.dreamers.h2d.reactions.ReactionTools;
import com.dreamers.h2d.session.SessionManager;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {
    private final List<Message> messages;
    private final boolean isAdmin;
    private final UUID threadId;

    public MessageAdapter(List<Message> messages, boolean isAdmin, UUID threadId) {
        this.messages = messages;
        this.isAdmin  = isAdmin;
        this.threadId = threadId;
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_message, parent, false);
        return new MessageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {
        holder.display(messages.get(position));
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    class MessageViewHolder extends RecyclerView.ViewHolder {
        private final TextView authorText;
        private final TextView timestampText;
        private final TextView contentText;
        private final TextView reactionSummaryText;
        private final Button   reactButton;
        private final Button   hideButton;
        private final Button   pinButton;
        private final Button   reportButton;
        private final Button   clearReportButton;

        MessageViewHolder(@NonNull View itemView) {
            super(itemView);
            authorText          = itemView.findViewById(R.id.messageAuthorText);
            timestampText       = itemView.findViewById(R.id.messageTimestampText);
            contentText         = itemView.findViewById(R.id.messageContentText);
            reactionSummaryText = itemView.findViewById(R.id.reactionSummaryText);
            reactButton         = itemView.findViewById(R.id.reactButton);
            hideButton          = itemView.findViewById(R.id.hideButton);
            pinButton           = itemView.findViewById(R.id.pinButton);
            reportButton        = itemView.findViewById(R.id.reportButton);
            clearReportButton   = itemView.findViewById(R.id.clearReportButton);
        }

        void display(Message message) {
            if (!isAdmin && message.isHidden()) {
                itemView.setVisibility(View.GONE);
                itemView.getLayoutParams().height = 0;
                return;
            }

            itemView.setVisibility(View.VISIBLE);
            itemView.getLayoutParams().height = ViewGroup.LayoutParams.WRAP_CONTENT;

            User poster = UserDAO.getInstance().getByUUID(message.poster());
            authorText.setText(poster != null ? poster.username() : "Unknown user");
            if (poster != null) {
                authorText.setOnClickListener(v -> {
                    Intent intent = new Intent(itemView.getContext(), ProfileActivity.class);
                    intent.putExtra("userId", poster.id().toString());
                    itemView.getContext().startActivity(intent);
                });
            }

            timestampText.setText(formatTime(message.timestamp()));
            contentText.setText(message.message());
            updateReactionSummary(message);
            reactButton.setOnClickListener(v -> showReactionDialog(message));

            if (isAdmin) {
                hideButton.setVisibility(View.VISIBLE);
                hideButton.setText(message.isHidden() ? "Unhide" : "Hide");
                hideButton.setOnClickListener(v -> {
                    ModerationTools.setHidden(message.id(),
                            SessionManager.getInstance().getCurrentUser().id(),
                            !message.isHidden());
                    DataManager.getInstance().writeAll();
                    int pos = getBindingAdapterPosition();
                    if (pos != RecyclerView.NO_ID) notifyItemChanged(pos);
                });

                if (threadId != null) {
                    pinButton.setVisibility(View.VISIBLE);
                    boolean pinned = PinTools.isPinned(threadId, message.id());
                    pinButton.setText(pinned ? "Unpin" : "Pin");
                    pinButton.setOnClickListener(v -> {
                        if (PinTools.isPinned(threadId, message.id()))
                            PinTools.unpin(threadId, message.id());
                        else
                            PinTools.pin(threadId, message.id());
                        DataManager.getInstance().writeAll();
                        int pos = getBindingAdapterPosition();
                        if (pos != RecyclerView.NO_ID) notifyItemChanged(pos);
                    });
                } else {
                    pinButton.setVisibility(View.GONE);
                }

                int reportCount = ReportDAO.getInstance().getReportCount(message.id());
                if (reportCount > 0) {
                    clearReportButton.setVisibility(View.VISIBLE);
                    clearReportButton.setText("Clear (" + reportCount + ")");
                    clearReportButton.setOnClickListener(v -> {
                        ReportDAO.getInstance().clearReports(message.id());
                        DataManager.getInstance().writeAll();
                        Toast.makeText(itemView.getContext(), "Reports cleared", Toast.LENGTH_SHORT).show();
                        int pos = getBindingAdapterPosition();
                        if (pos != RecyclerView.NO_ID) {
                            messages.remove(pos);
                            notifyItemRemoved(pos);
                        }
                    });
                } else {
                    clearReportButton.setVisibility(View.GONE);
                }

                reportButton.setVisibility(View.GONE);

            } else {
                hideButton.setVisibility(View.GONE);
                pinButton.setVisibility(View.GONE);
                clearReportButton.setVisibility(View.GONE);

                reportButton.setVisibility(View.VISIBLE);
                reportButton.setOnClickListener(v -> {
                    User currentUser = SessionManager.getInstance().getCurrentUser();
                    if (currentUser == null) return;
                    if (ModerationTools.hasReported(message.id(), currentUser.id())) {
                        Toast.makeText(itemView.getContext(), "Already reported", Toast.LENGTH_SHORT).show();
                    } else {
                        ModerationTools.addReport(message.id(), currentUser.id(), System.currentTimeMillis());
                        DataManager.getInstance().writeAll();
                        Toast.makeText(itemView.getContext(), "Message reported", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }

        private void showReactionDialog(Message message) {
            User currentUser = SessionManager.getInstance().getCurrentUser();
            if (currentUser == null) return;

            String[] options = {"👍 Like", "❤️ Love", "😂 Laugh", "😡 Angry", "😢 Sad", "Remove reaction"};

            new AlertDialog.Builder(itemView.getContext())
                    .setTitle("React to message")
                    .setItems(options, (dialog, which) -> {
                        UUID messageId = message.id();
                        UUID userId    = currentUser.getUUID();
                        switch (which) {
                            case 0 -> ReactionTools.react(messageId, userId, ReactionType.LIKE);
                            case 1 -> ReactionTools.react(messageId, userId, ReactionType.LOVE);
                            case 2 -> ReactionTools.react(messageId, userId, ReactionType.LAUGH);
                            case 3 -> ReactionTools.react(messageId, userId, ReactionType.ANGRY);
                            case 4 -> ReactionTools.react(messageId, userId, ReactionType.SAD);
                            case 5 -> ReactionTools.unreact(messageId, userId);
                        }
                        DataManager.getInstance().writeAll();
                        int pos = getBindingAdapterPosition();
                        if (pos != RecyclerView.NO_ID) notifyItemChanged(pos);
                    })
                    .show();
        }

        private void updateReactionSummary(Message message) {
            Map<ReactionType, Integer> counts = ReactionTools.getReactionCounts(message.id());
            if (counts.isEmpty()) {
                reactionSummaryText.setText("No reactions yet!");
                return;
            }
            StringBuilder summary = new StringBuilder();
            appendReaction(summary, counts, ReactionType.LIKE,  "👍");
            appendReaction(summary, counts, ReactionType.LOVE,  "❤️");
            appendReaction(summary, counts, ReactionType.LAUGH, "😂");
            appendReaction(summary, counts, ReactionType.ANGRY, "😡");
            appendReaction(summary, counts, ReactionType.SAD,   "😢");
            reactionSummaryText.setText(summary.toString().trim());
        }

        private void appendReaction(StringBuilder summary, Map<ReactionType, Integer> counts,
                                    ReactionType type, String emoji) {
            int count = counts.getOrDefault(type, 0);
            if (count > 0) summary.append(emoji).append(" ").append(count).append("  ");
        }

        private String formatTime(long timestamp) {
            return new SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())
                    .format(new Date(timestamp));
        }
    }
}