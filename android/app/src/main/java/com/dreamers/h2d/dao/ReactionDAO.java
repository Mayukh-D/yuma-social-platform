package com.dreamers.h2d.dao;

import com.dreamers.h2d.dao.model.ReactionType;

import java.util.*;

/**
 * Stores every reaction by user on every message.
 * Mapping: message UUID -> (user UUID -> ReactionType),
 *
 * Singleton design pattern — the same instance is shared across the whole platform.
 */
public class ReactionDAO {
    private static ReactionDAO instance;

    /** Mapping: message UUID -> (user UUID -> reaction type). */
    private final Map<UUID, Map<UUID, ReactionType>> reactions = new HashMap<>();

    private ReactionDAO() {}

    public static ReactionDAO getInstance() {
        if (instance == null) instance = new ReactionDAO();
        return instance;
    }

    /**
     * Records/overwrites a reaction by user on a message.
     * @return true if the reaction was new/changed, false if it was already the same type
     */
    public boolean addReaction(UUID message, UUID user, ReactionType type) {
        Map<UUID, ReactionType> reactors = reactions.computeIfAbsent(message, k -> new HashMap<>());

        ReactionType previous = reactors.put(user, type);
        return previous != type;
    }

    /**
     * Removes a user's reaction from a message.
     * Removes the message entry entirely if it has no remaining reactions.
     * @return true if removed, false if no such reaction existed
     */
    public boolean removeReaction(UUID message, UUID user) {
        Map<UUID, ReactionType> reactors = reactions.get(message);
        if (reactors == null) return false;

        boolean removed = reactors.remove(user) != null;

        if (reactors.isEmpty()) {
            reactions.remove(message);
        }

        return removed;
    }

    /**
     * Returns the reaction type a specific user has placed on a message.
     * @return the ReactionType, or null if no user reaction.
     */
    public ReactionType getReaction(UUID message, UUID user) {
        Map<UUID, ReactionType> reactors = reactions.get(message);
        if (reactors == null) return null;
        return reactors.get(user);
    }

    /**
     * Returns a count of each reaction type on a message.
     * Returns an empty map if message has no reactions.
     */
    public Map<ReactionType, Integer> getReactionCounts(UUID message) {
        Map<ReactionType, Integer> counts = new EnumMap<>(ReactionType.class);

        Map<UUID, ReactionType> reactors = reactions.get(message);
        if (reactors == null) return counts;

        for (ReactionType type : reactors.values()) {
            counts.merge(type, 1, Integer::sum);
        }

        return counts;
    }

    /** Returns the total number of users who have reacted to a message (including 0 if none). */
    public int getTotalReactions(UUID message) {
        Map<UUID, ReactionType> reactors = reactions.get(message);
        if (reactors == null) return 0;
        return reactors.size();
    }

    /** Returns UUIDs of all messages that have at least one reaction which is active. */
    public Set<UUID> getReactedMessageIds() {
        return Collections.unmodifiableSet(reactions.keySet());
    }

    /** Clears all stored reactions. Used when reloading from persistent data. */
    public void clear() {
        reactions.clear();
    }

    /**
     * Returns an iterator over all reactions as rows of the following structure: message_uuid, user_uuid, reaction_type.
     * Used by DataManager for persistence.
     */
    public Iterator<String[]> getAllReactions() {
        List<String[]> rows = new ArrayList<>();

        for (Map.Entry<UUID, Map<UUID, ReactionType>> messageEntry : reactions.entrySet()) {

            UUID messageId = messageEntry.getKey();

            for (Map.Entry<UUID, ReactionType> userEntry : messageEntry.getValue().entrySet()) {
                rows.add(new String[]{
                        messageId.toString(),
                        userEntry.getKey().toString(),
                        userEntry.getValue().name()
                });
            }
        }

        return rows.iterator();
    }
}