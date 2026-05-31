package com.dreamers.h2d.dao;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.Iterator;
import java.util.ArrayList;
import java.util.List;

/**
 * Stores which messages are pinned, per post (thread).
 * Keyed by thread UUID → set of pinned message UUIDs.
 * SOLID: Message model is untouched; pin state lives here.
 */
public class PinDAO {
    private static PinDAO instance;

    private PinDAO() {}

    public static PinDAO getInstance() {
        if (instance == null) instance = new PinDAO();
        return instance;
    }

    // thread UUID -> set of pinned message UUIDs
    private final Map<UUID, Set<UUID>> pins = new HashMap<>();

    /** Pins a message in its thread. Returns true if state changed. */
    public boolean pin(UUID thread, UUID message) {
        return pins.computeIfAbsent(thread, k -> new HashSet<>()).add(message);
    }

    /** Unpins a message. Returns true if it was pinned. */
    public boolean unpin(UUID thread, UUID message) {
        Set<UUID> threadPins = pins.get(thread);
        if (threadPins == null) return false;
        boolean removed = threadPins.remove(message);
        if (threadPins.isEmpty()) pins.remove(thread);
        return removed;
    }

    /** Returns true if a message is pinned. */
    public boolean isPinned(UUID thread, UUID message) {
        Set<UUID> threadPins = pins.get(thread);
        return threadPins != null && threadPins.contains(message);
    }

    /** Returns all pinned message UUIDs for a thread. */
    public Set<UUID> getPinnedMessages(UUID thread) {
        return Collections.unmodifiableSet(
                pins.getOrDefault(thread, Collections.emptySet()));
    }

    /** For persistence — rows: thread_uuid, message_uuid */
    public Iterator<String[]> getAll() {
        List<String[]> rows = new ArrayList<>();
        for (Map.Entry<UUID, Set<UUID>> e : pins.entrySet()) {
            for (UUID msgId : e.getValue()) {
                rows.add(new String[]{e.getKey().toString(), msgId.toString()});
            }
        }
        return rows.iterator();
    }

    public void clear() { pins.clear(); }
}