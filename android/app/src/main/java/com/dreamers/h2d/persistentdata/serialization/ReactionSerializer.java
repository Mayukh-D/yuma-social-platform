package com.dreamers.h2d.persistentdata.serialization;

/**
 * Serializes reaction records to and from CSV rows.
 * Schema (3 columns): message_uuid, user_uuid, reaction_type
 *
 * ReactionDAO.getAllReactions() already produces String[] rows in this format.
 * DataManager reads them back as String[] too.
 * Therefore, directions are identity operations.
 */
public class ReactionSerializer implements Serializer<String[], String[]> {
    @Override
    public String[] serialize(String[] row) {
        return row;
    }

    @Override
    public String[] deserialize(String[] data) {
        return data;
    }
}