package moderation;

import dao.MessageDAO;
import dao.ReportDAO;
import dao.UserDAO;
import dao.model.Message;
import dao.model.User;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

/**
 * Provides moderation operations available to Admin users.
 */
public class ModerationTools {

    private static final ReportDAO reportDAO = ReportDAO.getInstance();

    /**
     * Records a report against a message by a user.
     * @return false if the message or user does not exist (or if already reported)
     */
    public static boolean addReport(UUID message, UUID user, long timestamp) {
        if (MessageDAO.getInstance().get(message) == null) return false;
        if (UserDAO.getInstance().getByUUID(user) == null) return false;
        return reportDAO.addReport(message, user, timestamp);
    }

    /**
     * Retracts a report against a message by a user.
     * @return false if the message or user does not exist, or if no such report exists
     */
    public static boolean removeReport(UUID message, UUID user, long timestamp) {
        if (MessageDAO.getInstance().get(message) == null) return false;
        if (UserDAO.getInstance().getByUUID(user) == null) return false;
        return reportDAO.removeReport(message, user);
    }

    /** Given user has reported a given message? */
    public static boolean hasReported(UUID message, UUID user) {
        return reportDAO.hasReported(message, user);
    }

    /**
     * Allows an Admin to hide or unhide a message.
     * @return false if the message or user does not exist, or if the user is not an Admin
     */
    public static boolean setHidden(UUID message, UUID user, boolean hidden) {
        Message msg = MessageDAO.getInstance().get(message);
        if (msg == null) return false;

        User u = UserDAO.getInstance().getByUUID(user);
        if (u == null || u.role() != User.Role.Admin) return false;

        msg.setHidden(hidden);
        return true;
    }

    /**
     * Returns an iterator over reported Messages ordered by the given strategy.
     *
     * @param strategy "OLDEST" (oldest report first) or "MOST" (most reports first)
     * @param amount   maximum number of messages to return; must be positive
     * @return an Iterator over the matching Messages in strategy order
     * @throws IllegalArgumentException if strategy is unrecognised or amount is not positive
     */
    public static Iterator<Message> getReportedMessages(String strategy, int amount) {
        if (amount <= 0) throw new IllegalArgumentException("amount must be positive, got: " + amount);

        ReportSortStrategy sortStrategy = ReportSortStrategy.Factory.create(strategy);
        Comparator<Message> comparator = sortStrategy.comparator(reportDAO);

        MessageDAO messageDAO = MessageDAO.getInstance();

        List<Message> reported = new ArrayList<>();
        for (UUID id : reportDAO.getReportedMessageIds()) {
            Message msg = messageDAO.get(id);
            if (msg != null) reported.add(msg);
        }
        reported.sort(comparator);
        List<Message> result = reported.subList(0, Math.min(amount, reported.size()));

        return result.iterator();
    }
}