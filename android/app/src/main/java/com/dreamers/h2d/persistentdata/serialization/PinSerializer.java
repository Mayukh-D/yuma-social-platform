package com.dreamers.h2d.persistentdata.serialization;

/**
 * Serializes pin records to/from CSV rows.
 * Each row is: thread_uuid, message_uuid
 * Simple pass-through since we store as String arrays.
 */
public class PinSerializer implements Serializer<String[], String[]> {

    @Override
    public String[] serialize(String[] row) {
        return row;
    }

    @Override
    public String[] deserialize(String[] data) {
        return data;
    }
}