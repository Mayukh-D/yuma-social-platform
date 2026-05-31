package com.dreamers.h2d.dao;

import java.util.*;

/**
 * Stores which users have reported which messages, including the timestamp of each report.
 * Uses a HashMap of HashMaps for O(1) average-case performance on all operations.
 */
public class ReportDAO {

    private static ReportDAO instance;

    private ReportDAO() {}

    public static ReportDAO getInstance() {
        if (instance == null) instance = new ReportDAO();
        return instance;
    }

    /** Maps message UUID -> (user UUID -> report timestamp). */
    private final Map<UUID, Map<UUID, Long>> reports = new HashMap<>();

    /**
     * Records a report of a message by a user at the given timestamp.
     * @return true if added, false if the user had already reported this message
     */
    public boolean addReport(UUID message, UUID user, long timestamp) {
        Map<UUID, Long> reporters = reports.computeIfAbsent(message, k -> new HashMap<>());
        if (reporters.containsKey(user)) return false;
        reporters.put(user, timestamp);
        return true;
    }

    /**
     * Retracts a report of a message by a user.
     * @return true if removed, false if no such report existed
     */
    public boolean removeReport(UUID message, UUID user) {
        Map<UUID, Long> reporters = reports.get(message);
        if (reporters == null) return false;
        boolean removed = reporters.remove(user) != null;
        if (reporters.isEmpty()) reports.remove(message);
        return removed;
    }

    /** Checks whether a given user has reported a given message. */
    public boolean hasReported(UUID message, UUID user) {
        Map<UUID, Long> reporters = reports.get(message);
        return reporters != null && reporters.containsKey(user);
    }

    /** Returns the number of active reports on a message, or 0 if none. */
    public int getReportCount(UUID message) {
        Map<UUID, Long> reporters = reports.get(message);
        return reporters == null ? 0 : reporters.size();
    }

    /**
     * Returns the timestamp of the oldest active report on a message.
     * Returns Long.MAX_VALUE if there are no reports.
     */
    public long getOldestReportTimestamp(UUID message) {
        Map<UUID, Long> reporters = reports.get(message);
        if (reporters == null || reporters.isEmpty()) return Long.MAX_VALUE;
        return Collections.min(reporters.values());
    }

    /** Returns UUIDs of all messages that have at least one active report. */
    public Set<UUID> getReportedMessageIds() {
        return Collections.unmodifiableSet(reports.keySet());
    }

    public Set<UUID> getReporters(UUID message) {
        Map<UUID, Long> reporters = reports.get(message);
        if (reporters == null) return Set.of();
        return Collections.unmodifiableSet(reporters.keySet());
    }

    public void clearReports(UUID message) {
        reports.remove(message);
    }

    /** Clears all stored reports. Used when reloading from persistent storage. */
    public void clear() {
        reports.clear();
    }

    /**
     * Returns an iterator over all (message_uuid, user_uuid, timestamp) rows.
     * Used for persistence.
     */
    public Iterator<String[]> getAllReports() {
        List<String[]> rows = new ArrayList<>();
        for (Map.Entry<UUID, Map<UUID, Long>> entry : reports.entrySet()) {
            for (Map.Entry<UUID, Long> report : entry.getValue().entrySet()) {
                rows.add(new String[]{
                        entry.getKey().toString(),
                        report.getKey().toString(),
                        String.valueOf(report.getValue())
                });
            }
        }
        return rows.iterator();
    }
}