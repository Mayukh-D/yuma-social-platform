package com.dreamers.h2d.persistentdata.serialization;

import com.dreamers.h2d.dao.model.Message;

import java.util.UUID;

/**
 * Helps in Serialisation and Deserialisation
 */
public class MessageSerializer implements Serializer<Message, String[]> {
    /**
     * Serializes Messages to CSV rows.
     * Schema (6 columns): id, poster, thread, timestamp, message, hidden
     * @param Message object that is the object to be serialised
     */
    @Override
    public String[] serialize(Message object) {
        return new String[]{
                object.id().toString(),
                object.poster().toString(),
                object.thread().toString(),
                String.valueOf(object.timestamp()),
                object.message(),
                String.valueOf(object.isHidden())
        };
    }

    /**
     * Deserializes Messages from CSV rows.
     * Schema (6 columns): id, poster, thread, timestamp, message, hidden
     * @param Array of String that is to be deserialised
     */
    @Override
    public Message deserialize(String[] data) {
        Message message = new Message(
                UUID.fromString(data[0]),
                UUID.fromString(data[1]),
                UUID.fromString(data[2]),
                Long.parseLong(data[3]),
                data[4]
        );
        message.setHidden(Boolean.parseBoolean(data[5]));
        return message;
    }
}