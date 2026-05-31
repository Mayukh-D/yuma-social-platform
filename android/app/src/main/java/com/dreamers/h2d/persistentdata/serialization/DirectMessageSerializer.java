package com.dreamers.h2d.persistentdata.serialization;

import com.dreamers.h2d.dao.model.DirectMessage;
import java.util.UUID;

/**
 * Serialises DirectMessage objects to and from CSV rows for persistence.
 * Schema (6 columns): id, sender, recipient, timestamp, content, read.
 */
public class DirectMessageSerializer implements Serializer<DirectMessage, String[]> {

    /**
     * Converts a DirectMessage into its CSV row representation.
     * @param dm the message to serialise
     * @return a 6-element array: id, sender, recipient, timestamp, content, read
     */
    @Override
    public String[] serialize(DirectMessage dm) {
        return new String[]{
                dm.id().toString(),
                dm.sender().toString(),
                dm.recipient().toString(),
                String.valueOf(dm.timestamp()),
                dm.content(),
                String.valueOf(dm.isRead())
        };
    }

    /**
     * Reconstructs a DirectMessage from its CSV row representation.
     * @param data a 6-element array in the same order as serialize() produces
     * @return the reconstructed DirectMessage with its read flag restored
     */
    @Override
    public DirectMessage deserialize(String[] data) {
        DirectMessage dm = new DirectMessage(
                UUID.fromString(data[0]),
                UUID.fromString(data[1]),
                UUID.fromString(data[2]),
                Long.parseLong(data[3]),
                data[4]
        );
        dm.setRead(Boolean.parseBoolean(data[5]));
        return dm;
    }
}