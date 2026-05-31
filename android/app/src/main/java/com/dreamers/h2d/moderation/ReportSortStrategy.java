package com.dreamers.h2d.moderation;

import com.dreamers.h2d.dao.ReportDAO;
import com.dreamers.h2d.dao.model.Message;

import java.util.Comparator;

/**
 * Defines a strategy for ordering reported Messages.
 * Implementations wrap a Comparator that can be applied to Message UUIDs based on data from the ReportDAO.
 */
public interface ReportSortStrategy {

    /** Returns a Comparator that orders Messages according to this strategy. */
    Comparator<Message> comparator(ReportDAO reportDAO);

    /**
     * Factory that produces a ReportSortStrategy from a strategy name string.
     * Implements the Factory design pattern.
     */
    final class Factory {

        private Factory() {}

        /**
         * Creates the appropriate ReportSortStrategy for the given name.
         * @param strategy "OLDEST" or "MOST"
         * @return the corresponding strategy
         * @throws IllegalArgumentException if the strategy name is not recognised
         */
        public static ReportSortStrategy create(String strategy) {
            return switch (strategy) {
                case "OLDEST" -> new OldestFirst();
                case "MOST"   -> new MostFirst();
                default -> throw new IllegalArgumentException(
                        String.format("Unknown report strategy: '%s'. Expected \"OLDEST\" or \"MOST\".", strategy));
            };
        }
    }

    /**
     * Orders messages so that the one with the oldest (smallest timestamp)
     * active report appears first.
     */
    final class OldestFirst implements ReportSortStrategy {
        @Override
        public Comparator<Message> comparator(ReportDAO reportDAO) {
            return Comparator.comparingLong(msg -> reportDAO.getOldestReportTimestamp(msg.id()));
        }
    }

    /**
     * Orders messages so that the one with the most active reports appears first.
     * The comparator is reversed so that higher counts rank first.
     */
    final class MostFirst implements ReportSortStrategy {
        @Override
        public Comparator<Message> comparator(ReportDAO reportDAO) {
            return Comparator.comparingInt((Message msg) -> reportDAO.getReportCount(msg.id())).reversed();
        }
    }
}