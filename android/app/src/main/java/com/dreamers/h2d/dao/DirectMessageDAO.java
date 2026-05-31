package com.dreamers.h2d.dao;

import com.dreamers.h2d.dao.model.DirectMessage;
import java.util.*;

/**
 * Singleton store of all direct messages exchanged between users.
 * Maintains two indices: one keyed by message UUID for direct lookup,
 * and one keyed by user UUID for efficient conversation retrieval.
 */
public class DirectMessageDAO {
    private static DirectMessageDAO instance;
    private DirectMessageDAO() {}

    /**
     * Returns the singleton instance, creating it on first access.
     * @return the shared DirectMessageDAO
     */
    public static DirectMessageDAO getInstance() {
        if (instance == null) instance = new DirectMessageDAO();
        return instance;
    }

    private final Map<UUID, DirectMessage> byId = new HashMap<>();
    private final Map<UUID, List<DirectMessage>> byUser = new HashMap<>();

    /**
     * Adds a direct message to the store and indexes it under both sender and recipient.
     * @param dm the direct message to add
     */
    public void add(DirectMessage dm) {
        byId.put(dm.id(), dm);
        byUser.computeIfAbsent(dm.sender(), k -> new ArrayList<>()).add(dm);
        byUser.computeIfAbsent(dm.recipient(), k -> new ArrayList<>()).add(dm);
    }

    /**
     * Looks up a direct message by its UUID.
     * @param id the message UUID
     * @return the matching DirectMessage, or null if not found
     */
    public DirectMessage get(UUID id) { return byId.get(id); }

    /**
     * Returns the set of UUIDs of all users the given user has exchanged messages with.
     * @param user the viewer's UUID
     * @return an unmodifiable set of partner UUIDs, empty if none
     */
    public Set<UUID> getConversationPartners(UUID user) {
        List<DirectMessage> dms = byUser.get(user);
        if (dms == null) return Set.of();
        Set<UUID> partners = new HashSet<>();
        for (DirectMessage dm : dms) {
            partners.add(dm.sender().equals(user) ? dm.recipient() : dm.sender());
        }
        return partners;
    }

    /**
     * Returns all messages exchanged between two users in chronological order.
     * @param userA one participant's UUID
     * @param userB the other participant's UUID
     * @return the list of messages between userA and userB, sorted by timestamp ascending
     */
    public List<DirectMessage> getConversation(UUID userA, UUID userB) {
        List<DirectMessage> dms = byUser.get(userA);
        if (dms == null) return List.of();
        List<DirectMessage> conv = new ArrayList<>();
        for (DirectMessage dm : dms) {
            if (dm.sender().equals(userB) || dm.recipient().equals(userB)) {
                conv.add(dm);
            }
        }
        conv.sort(Comparator.comparingLong(DirectMessage::timestamp));
        return conv;
    }

    /**
     * Returns an iterator over all direct messages in the store, used by the
     * persistence layer to serialise the DAO contents.
     * @return an Iterator of all stored DirectMessages
     */
    public Iterator<DirectMessage> getAll() { return byId.values().iterator(); }

    /**
     * Removes all direct messages from the store. Called by DataManager
     * before reloading from disk.
     */
    public void clear() {
        byId.clear();
        byUser.clear();
    }
}