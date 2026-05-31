package com.dreamers.h2d.dao.model;

import java.util.UUID;

/**
 * A direct message between two users. Identified by a unique UUID,
 * with sender, recipient, timestamp, content, and a mutable read flag
 * that flips to true once the recipient views the message.
 */
public class DirectMessage implements HasUUID {
    private final UUID id;
    private final UUID sender;
    private final UUID recipient;
    private final long timestamp;
    private final String content;
    private boolean read;

    /**
     * Constructs a new direct message. The read flag is initialised to false.
     * @param id unique identifier for this message
     * @param sender UUID of the user sending the message
     * @param recipient UUID of the user receiving the message
     * @param timestamp send time in milliseconds since epoch
     * @param content the text of the message
     */
    public DirectMessage(UUID id, UUID sender, UUID recipient, long timestamp, String content) {
        this.id = id;
        this.sender = sender;
        this.recipient = recipient;
        this.timestamp = timestamp;
        this.content = content;
        this.read = false;
    }

    /** @return the message UUID */
    public UUID id() { return id; }

    /** @return the sender's UUID */
    public UUID sender() { return sender; }

    /** @return the recipient's UUID */
    public UUID recipient() { return recipient; }

    /** @return the send time in milliseconds since epoch */
    public long timestamp() { return timestamp; }

    /** @return the text content of the message */
    public String content() { return content; }

    /** @return true if the recipient has viewed this message */
    public boolean isRead() { return read; }

    /**
     * Marks this message as read or unread.
     * @param read the new read state
     */
    public void setRead(boolean read) { this.read = read; }

    /**
     * Returns this message's UUID, satisfying the HasUUID contract.
     * @return the message UUID
     */
    @Override
    public UUID getUUID() { return id; }
}