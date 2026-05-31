package com.dreamers.h2d.messaging;

import com.dreamers.h2d.dao.DirectMessageDAO;
import com.dreamers.h2d.dao.UserDAO;
import com.dreamers.h2d.dao.model.DirectMessage;

import java.util.Iterator;
import java.util.UUID;

/**
 * Facade for direct messaging operations. Validates inputs against UserDAO
 * and exposes query results as Iterators, mirroring the pattern established
 * by ModerationTools in Hackathon 1.
 */
public class DirectMessageTools {
    private static final DirectMessageDAO dmDAO = DirectMessageDAO.getInstance();
    private static final UserDAO userDAO = UserDAO.getInstance();

    /**
     * Sends a direct message from sender to recipient. Both users must exist
     * in UserDAO and the content must be non-blank.
     * @param sender UUID of the sending user
     * @param recipient UUID of the receiving user
     * @param content the text of the message
     * @param timestamp send time in milliseconds since epoch
     * @return true if the message was sent successfully, false if either user
     *         is unknown or the content is blank
     */
    public static boolean send(UUID sender, UUID recipient, String content, long timestamp) {
        if (userDAO.getByUUID(sender) == null) return false;
        if (userDAO.getByUUID(recipient) == null) return false;
        if (content == null || content.isBlank()) return false;
        dmDAO.add(new DirectMessage(UUID.randomUUID(), sender, recipient, timestamp, content));
        return true;
    }

    /**
     * Returns an iterator over the UUIDs of users that the given user has
     * exchanged messages with.
     * @param user the viewer's UUID
     * @return an Iterator of partner UUIDs, empty if none exist
     */
    public static Iterator<UUID> getConversationsFor(UUID user) {
        return dmDAO.getConversationPartners(user).iterator();
    }

    /**
     * Returns an iterator over all messages between two users in chronological order.
     * @param userA one participant's UUID
     * @param userB the other participant's UUID
     * @return an Iterator of DirectMessages between the two users, sorted by timestamp ascending
     */
    public static Iterator<DirectMessage> getConversation(UUID userA, UUID userB) {
        return dmDAO.getConversation(userA, userB).iterator();
    }

    /**
     * Counts the total number of unread incoming messages across all of the
     * user's conversations.
     * @param user the viewer's UUID
     * @return the number of messages addressed to the user that have not been marked read
     */
    public static int unreadCount(UUID user) {
        int count = 0;
        for (UUID partner : dmDAO.getConversationPartners(user)) {
            for (DirectMessage dm : dmDAO.getConversation(user, partner)) {
                if (dm.recipient().equals(user) && !dm.isRead()) count++;
            }
        }
        return count;
    }

    /**
     * Marks the given message as read. No effect if the message does not exist.
     * @param dmId the UUID of the message to mark as read
     */
    public static void markRead(UUID dmId) {
        DirectMessage dm = dmDAO.get(dmId);
        if (dm != null) dm.setRead(true);
    }
}