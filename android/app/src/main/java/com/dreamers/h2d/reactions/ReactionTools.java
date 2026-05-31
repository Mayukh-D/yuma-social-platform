package com.dreamers.h2d.reactions;

import com.dreamers.h2d.dao.MessageDAO;
import com.dreamers.h2d.dao.ReactionDAO;
import com.dreamers.h2d.dao.UserDAO;
import com.dreamers.h2d.dao.model.Message;
import com.dreamers.h2d.dao.model.ReactionType;

import java.util.*;

public class ReactionTools {

    /**
     * Provides reaction operations available to all users who are logged-in.
     * Acts as the public facade over ReactionDAO.
     * UI code must call these methods rather than accessing ReactionDAO directly.
     */
    private static final ReactionDAO reactionDAO = ReactionDAO.getInstance();

    /**
     * Records a reaction by a user on a message.
     * @return false if the message/user does not exist
     */
    public static boolean react(UUID message, UUID user, ReactionType type) {
        if (UserDAO.getInstance().getByUUID(user) == null) return false;

        return reactionDAO.addReaction(message, user, type);
    }

    /**
     * Removes a user's reaction from a message.
     * @return false if the message or user does not exist, or if no reaction existed
     */
    public static boolean unreact(UUID message, UUID user) {
        if (UserDAO.getInstance().getByUUID(user) == null) return false;

        return reactionDAO.removeReaction(message, user);
    }


    /**
     * Returns the reaction type a specific user has placed on a message.
     * @return the ReactionType, or null if the user has not reacted
     */
    public static ReactionType getMyReaction(UUID message, UUID user) {
        return reactionDAO.getReaction(message, user);
    }

    /**
     * Returns a count of each reaction type on a message.
     * Returns an empty map if the message has no reactions.
     */
    public static Map<ReactionType, Integer> getReactionCounts(UUID message) {
        return reactionDAO.getReactionCounts(message);
    }

    /** Returns the total number of users who have reacted to a message. */
    public static int getTotalReactions(UUID message) {
        return reactionDAO.getTotalReactions(message);
    }

    /**
     * Returns an iterator over the most-reacted messages ordered by the given strategy.
     *
     * @param strategy "MOST" (most total reactions first) or "LIKED" (most LIKE reactions first)
     * @param amount maximum number of messages to return; amount >0
     * @return Iterator over the matching Messages by  strategy order
     * @throws IllegalArgumentException if strategy is unrecognised/amount is not positive
     */
    public static Iterator<Message> getTopReactedMessages(String strategy, int amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("amount must be positive");
        }

        ReactionSortStrategy sortStrategy = ReactionSortStrategy.Factory.create(strategy);

        List<Message> messages = new ArrayList<>();
        MessageDAO messageDAO = MessageDAO.getInstance();

        for (UUID messageId : reactionDAO.getReactedMessageIds()) {
            Message message = messageDAO.get(messageId);
            if (message != null) {
                messages.add(message);
            }
        }

        messages.sort(sortStrategy.comparator(reactionDAO));

        return messages.subList(0, Math.min(amount, messages.size())).iterator();
    }
}