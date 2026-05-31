package com.dreamers.h2d.reactions;

import com.dreamers.h2d.dao.ReactionDAO;
import com.dreamers.h2d.dao.model.Message;
import com.dreamers.h2d.dao.model.ReactionType;

import java.util.Comparator;

/**
 * Defines a strategy for ordering Messages by their reactions.
 * Implementations wrap a Comparator that ranks Messages based on reaction data retrieved from the ReactionDAO.
 *
 * Follows Singleton/Factory/Iterator throughout
 * Mirrors the shape of ReportSortStrategy.
 */
public interface ReactionSortStrategy {

    /** Returns a Comparator that orders Messages according to this strategy. */
    Comparator<Message> comparator(ReactionDAO dao);

    final class Factory {

        /**
         * Factory that produces a ReactionSortStrategy from a strategy name string.
         * Implements the Factory design pattern.
         */
        private Factory() {}

        /**
         * Creates the appropriate ReactionSortStrategy for the given name.
         * @param strategy "MOST" or "LIKED"
         * @return the corresponding strategy
         * @throws IllegalArgumentException if the strategy name is not recognised
         */
        public static ReactionSortStrategy create(String strategy) {
            return switch (strategy) {
                case "MOST" -> new MostReacted();
                case "LIKED" -> new MostOfType(ReactionType.LIKE);
                default -> throw new IllegalArgumentException("Unknown strategy: " + strategy);
            };
        }
    }

    /**
     * Orders messages so that the one with the most total reactions
     * (across all types combined) appears first.
     */
    final class MostReacted implements ReactionSortStrategy {
        @Override
        public Comparator<Message> comparator(ReactionDAO dao) {
            return Comparator.comparingInt((Message message) ->
                    dao.getTotalReactions(message.id())).reversed();
        }
    }

    /**
     * Orders messages so that the one with the most reactions of a specific
     * type appears first. The type is fixed at construction time.
     */
    final class MostOfType implements ReactionSortStrategy {
        private final ReactionType type;

        MostOfType(ReactionType type) {
            this.type = type;
        }

        @Override
        public Comparator<Message> comparator(ReactionDAO dao) {
            return Comparator.comparingInt((Message message) ->
                            dao.getReactionCounts(message.id()).getOrDefault(type, 0)).reversed();
        }
    }
}