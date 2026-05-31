package com.dreamers.h2d.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.dreamers.h2d.R;
import com.dreamers.h2d.dao.DirectMessageDAO;
import com.dreamers.h2d.dao.UserDAO;
import com.dreamers.h2d.dao.model.DirectMessage;
import com.dreamers.h2d.dao.model.User;

import java.util.List;
import java.util.UUID;

/**
 * RecyclerView adapter for the inbox screen. Each row represents one conversation
 * partner and shows their username, a preview of the most recent message,
 * and a badge with the count of unread incoming messages.
 */
public class ConversationListAdapter extends RecyclerView.Adapter<ConversationListAdapter.ViewHolder> {

    /**
     * Click callback for inbox rows. Implemented by InboxActivity to launch
     * ConversationActivity with the tapped partner's UUID.
     */
    public interface OnClickListener {
        /**
         * Invoked when an inbox row is tapped.
         * @param partnerId the UUID of the partner whose row was tapped
         */
        void onClick(UUID partnerId);
    }

    private final List<UUID> partners;
    private final UUID viewer;
    private OnClickListener onClickListener;

    /**
     * Constructs the adapter with a sorted list of partner UUIDs and the viewer.
     * @param partners the partner UUIDs to display, in the desired sort order
     * @param viewer the UUID of the user viewing the inbox, used to determine
     *               unread counts and previews
     */
    public ConversationListAdapter(List<UUID> partners, UUID viewer) {
        this.partners = partners;
        this.viewer = viewer;
    }

    /**
     * Registers the callback invoked when an inbox row is tapped.
     * @param listener the click handler
     */
    public void setOnClickListener(OnClickListener listener) {
        this.onClickListener = listener;
    }

    /**
     * Inflates the fragment_conversation_summary row layout for a new inbox row.
     * @param parent the parent ViewGroup the new row will attach to
     * @param viewType unused; all rows share the same layout
     * @return a new ViewHolder wrapping the inflated row
     */
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_conversation_summary, parent, false);
        return new ViewHolder(view);
    }

    /**
     * Binds the partner at the given position to the supplied ViewHolder and
     * attaches the click listener.
     * @param holder the recycled ViewHolder to populate
     * @param position the index of the partner in the backing list
     */
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        UUID partner = partners.get(position);
        holder.display(viewer, partner);
        holder.itemView.setOnClickListener(v -> {
            if (onClickListener != null) onClickListener.onClick(partner);
        });
    }

    /**
     * @return the number of conversation partners in the inbox
     */
    @Override
    public int getItemCount() {
        return partners.size();
    }

    /**
     * Holds references to the username, preview, and unread-badge views of a single
     * inbox row, and populates them with data fetched from the DAOs on bind.
     */
    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView partnerUsername;
        private final TextView lastMessagePreview;
        private final TextView unreadBadge;

        /**
         * Captures the three text views from the inflated row.
         * @param view the inflated fragment_conversation_summary row
         */
        public ViewHolder(View view) {
            super(view);
            partnerUsername = view.findViewById(R.id.partnerUsername);
            lastMessagePreview = view.findViewById(R.id.lastMessagePreview);
            unreadBadge = view.findViewById(R.id.unreadBadge);
        }

        /**
         * Populates the row with the partner's username, the latest message
         * preview, and an unread count badge if there are unread incoming messages.
         * The badge is hidden when there are no unread messages.
         * @param viewer the UUID of the user viewing the inbox
         * @param partner the UUID of the conversation partner for this row
         */
        public void display(UUID viewer, UUID partner) {
            User partnerUser = UserDAO.getInstance().getByUUID(partner);
            String name = partnerUser != null ? partnerUser.username() : "?";
            partnerUsername.setText(name);

            TextView tvInitial = itemView.findViewById(R.id.tvConvInitial);
            if (tvInitial != null) {
                tvInitial.setText(name.substring(0, 1).toUpperCase());
            }


            List<DirectMessage> conv = DirectMessageDAO.getInstance().getConversation(viewer, partner);
            if (!conv.isEmpty()) {
                DirectMessage last = conv.get(conv.size() - 1);
                lastMessagePreview.setText(last.content());
            } else {
                lastMessagePreview.setText("");
            }

            int unread = 0;
            for (DirectMessage dm : conv) {
                if (dm.recipient().equals(viewer) && !dm.isRead()) unread++;
            }
            if (unread > 0) {
                unreadBadge.setText(String.valueOf(unread));
                unreadBadge.setVisibility(View.VISIBLE);
            } else {
                unreadBadge.setVisibility(View.GONE);
            }
        }
    }
}