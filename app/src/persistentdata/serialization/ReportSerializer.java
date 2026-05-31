package persistentdata.serialization;

import java.util.UUID;

/**
 * Serializes user reports to and from CSV rows.
 * Schema (3 columns): message_uuid, user_uuid, timestamp
 */
public class ReportSerializer implements Serializer<String[], String[]> {

    @Override
    public String[] serialize(String[] row) {
        return row;
    }

    @Override
    public String[] deserialize(String[] data) {
        return data;
    }
}