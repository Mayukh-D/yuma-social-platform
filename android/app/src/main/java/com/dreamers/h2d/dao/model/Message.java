package com.dreamers.h2d.dao.model;

import java.util.UUID;


public class Message implements HasUUID {
    private final UUID id;
    private final UUID poster;
    private final UUID thread;
    private final long timestamp;
    private final String message;
    private boolean hidden;

    /** Message class object that handles all message data*/
    /** If user is not admin, it needs to filter out hidden messages.
     * */

    public Message(UUID id, UUID poster, UUID thread, long timestamp, String message) {
        this.id = id;
        this.poster = poster;
        this.thread = thread;
        this.timestamp = timestamp;
        this.message = message;
        this.hidden = false;
    }

    public UUID id()        { return id; }
    public UUID poster()    { return poster; }
    public UUID thread()    { return thread; }
    public long timestamp() { return timestamp; }
    public String message() { return message; }
    public boolean isHidden() { return hidden; }

    public void setHidden(boolean hidden) { this.hidden = hidden; }

    @Override
    public UUID getUUID() { return id; }
}