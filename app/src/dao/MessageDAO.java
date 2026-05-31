package dao;

import dao.model.Message;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class MessageDAO {
    private static MessageDAO instance;
    private final Map<UUID, Message> messages = new HashMap<>();

    /**
     * Returns a instance of MessageDAO if it exists and nothing if
     * it doesn't. Follows Singleton
     */
    private MessageDAO() {}
    public static MessageDAO getInstance() {
        if (instance == null) {
            instance = new MessageDAO();
        }
        return instance;
    }

    /**
     * Adds a message to the system.
     */
    public void add(Message message) {
        messages.put(message.getUUID(), message);
    }

    /**
     * Retrieves a message by its UUID.
     * Returns null if the message does not exist.
     */
    public Message get(UUID id) {
        return messages.get(id);
    }

    /**
     * Helper for existence checks of Message.
     */
    public boolean exists(UUID id) {
        return messages.containsKey(id);
    }

    /**
     * Removes a message from the system if needed for moderation.
     */
    public void remove(UUID id) {
        messages.remove(id);
    }
}