package com.dreamers.h2d.messaging;

import com.dreamers.h2d.dao.DirectMessageDAO;
import com.dreamers.h2d.dao.model.DirectMessage;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;

/**
 * Strategy for ordering conversation partners in the inbox. Implementations
 * supply a Comparator over partner UUIDs given the viewer and the underlying DAO.
 * Concrete strategies are instantiated via the nested Factory.
 */
public interface ConversationSortStrategy {

    /**
     * Builds a comparator that orders partner UUIDs according to this strategy.
     * @param viewer the UUID of the user viewing the inbox
     * @param dao the data source for looking up conversation history
     * @return a Comparator over partner UUIDs
     */
    Comparator<UUID> comparator(UUID viewer, DirectMessageDAO dao);

    /**
     * Factory for creating ConversationSortStrategy instances by name.
     * Supported strategies: "RECENT" (most recent activity first) and
     * "UNREAD" (most unread incoming messages first).
     */
    final class Factory {
        private Factory() {}

        /**
         * Instantiates the named sort strategy.
         * @param strategy the strategy name, "RECENT" or "UNREAD"
         * @return a new ConversationSortStrategy instance
         * @throws IllegalArgumentException if the strategy name is not recognised
         */
        public static ConversationSortStrategy create(String strategy) {
            switch (strategy) {
                case "RECENT": return new MostRecent();
                case "UNREAD": return new UnreadFirst();
                default: throw new IllegalArgumentException("Unknown: " + strategy);
            }
        }
    }

    /**
     * Sorts partners by the timestamp of the most recent message in each
     * conversation, descending. Partners with no messages sort to the end.
     */
    final class MostRecent implements ConversationSortStrategy {

        /**
         * Returns a comparator that orders partner UUIDs by most recent message timestamp.
         * @param viewer the UUID of the user viewing the inbox
         * @param dao the data source for looking up conversation history
         * @return a Comparator that orders most-recently-active conversations first
         */
        @Override
        public Comparator<UUID> comparator(UUID viewer, DirectMessageDAO dao) {
            return (a, b) -> {
                List<DirectMessage> convA = dao.getConversation(viewer, a);
                List<DirectMessage> convB = dao.getConversation(viewer, b);
                long tA = convA.isEmpty() ? 0L : convA.get(convA.size() - 1).timestamp();
                long tB = convB.isEmpty() ? 0L : convB.get(convB.size() - 1).timestamp();
                return Long.compare(tB, tA);
            };
        }
    }

    /**
     * Sorts partners by the number of unread incoming messages, descending.
     * Useful for surfacing conversations that need the viewer's attention.
     */
    final class UnreadFirst implements ConversationSortStrategy {

        /**
         * Returns a comparator that orders partner UUIDs by unread incoming count.
         * @param viewer the UUID of the user viewing the inbox
         * @param dao the data source for looking up conversation history
         * @return a Comparator that orders conversations with more unread messages first
         */
        @Override
        public Comparator<UUID> comparator(UUID viewer, DirectMessageDAO dao) {
            return (a, b) -> {
                int uA = countUnread(viewer, a, dao);
                int uB = countUnread(viewer, b, dao);
                return Integer.compare(uB, uA);
            };
        }

        /**
         * Counts unread incoming messages from a specific partner.
         * @param viewer the UUID of the user viewing the inbox
         * @param partner the UUID of the conversation partner
         * @param dao the data source for looking up conversation history
         * @return the number of messages addressed to the viewer from the partner
         *         that have not been marked read
         */
        private int countUnread(UUID viewer, UUID partner, DirectMessageDAO dao) {
            int unread = 0;
            for (DirectMessage dm : dao.getConversation(viewer, partner)) {
                if (dm.recipient().equals(viewer) && !dm.isRead()) unread++;
            }
            return unread;
        }
    }
}