package com.dreamers.h2d.pinning;

import com.dreamers.h2d.dao.MessageDAO;
import com.dreamers.h2d.dao.PinDAO;
import com.dreamers.h2d.dao.model.Message;


import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

/**
 * Facade for pinning operations. UI calls this, never PinDAO directly.
 * Follows the same pattern as ModerationTools from H1.
 */
public class PinTools {

    private static final PinDAO pinDAO = PinDAO.getInstance();
    private static final MessageDAO messageDAO = MessageDAO.getInstance();

    /** Pins a message. Returns false if message doesn't exist. Admin should check before calling. */
    public static boolean pin(UUID thread, UUID messageId) {
        if (messageDAO.get(messageId) == null) return false;
        return pinDAO.pin(thread, messageId);
    }

    /** Unpins a message. Returns false if it wasn't pinned. */
    public static boolean unpin(UUID thread, UUID messageId) {
        return pinDAO.unpin(thread, messageId);
    }

    /** Returns true if a message is currently pinned. */
    public static boolean isPinned(UUID thread, UUID messageId) {
        return pinDAO.isPinned(thread, messageId);
    }

    /**
     * Returns all pinned messages for a thread, in insertion order.
     * Used by PostViewerActivity to show pinned section at top.
     */
    public static Iterator<Message> getPinnedMessages(UUID thread) {
        List<Message> pinned = new ArrayList<>();
        for (UUID msgId : pinDAO.getPinnedMessages(thread)) {
            Message m = messageDAO.get(msgId);
            if (m != null) pinned.add(m);
        }
        return pinned.iterator();
    }
}