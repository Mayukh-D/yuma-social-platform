import dao.MessageDAO;
import dao.ReportDAO;
import dao.UserDAO;
import dao.model.Message;
import dao.model.User;
import moderation.ModerationTools;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import static org.junit.Assert.*;

/**
 * Black-box tests for ModerationTools.getReportedMessages.
 */
public class ModerationToolsGetReportsTests {

    private final MessageDAO messageDAO = MessageDAO.getInstance();
    private final UserDAO userDAO = UserDAO.getInstance();
    private final ReportDAO reportDAO = ReportDAO.getInstance();
    private final UUID MSG_A = UUID.fromString("aaaaaaaa-0000-0000-0000-000000000001");
    private final UUID MSG_B = UUID.fromString("bbbbbbbb-0000-0000-0000-000000000002");
    private final UUID MSG_C = UUID.fromString("cccccccc-0000-0000-0000-000000000003");
    private final UUID USER_1 = UUID.fromString("00000000-0000-0000-0001-000000000001");
    private final UUID USER_2 = UUID.fromString("00000000-0000-0000-0001-000000000002");
    private final UUID USER_3 = UUID.fromString("00000000-0000-0000-0001-000000000003");
    private final UUID USER_4 = UUID.fromString("00000000-0000-0000-0001-000000000004");
    private final UUID USER_5 = UUID.fromString("00000000-0000-0000-0001-000000000005");

    @Before
    public void setUp() {
        userDAO.clear();
        reportDAO.clear();
        messageDAO.remove(MSG_A);
        messageDAO.remove(MSG_B);
        messageDAO.remove(MSG_C);
        messageDAO.add(new Message(MSG_A, USER_1, UUID.randomUUID(), 100L, "alpha"));
        messageDAO.add(new Message(MSG_B, USER_1, UUID.randomUUID(), 200L, "beta"));
        messageDAO.add(new Message(MSG_C, USER_1, UUID.randomUUID(), 300L, "gamma"));
        userDAO.add(new User(USER_1, User.Role.Member, "user11111", "pass"));
        userDAO.add(new User(USER_2, User.Role.Member, "user22222", "pass"));
        userDAO.add(new User(USER_3, User.Role.Member, "user33333", "pass"));
        userDAO.add(new User(USER_4, User.Role.Member, "user44444", "pass"));
        userDAO.add(new User(USER_5, User.Role.Member, "user55555", "pass"));
    }

    @After
    public void tearDown() {
        userDAO.clear();
        reportDAO.clear();
        messageDAO.remove(MSG_A);
        messageDAO.remove(MSG_B);
        messageDAO.remove(MSG_C);
    }

    @Test(expected = IllegalArgumentException.class)
    public void getReportedMessages_unknownStrategy_throwsException() {
        ModerationTools.getReportedMessages("NEWEST", 5);
    }

    @Test(expected = IllegalArgumentException.class)
    public void getReportedMessages_emptyStrategy_throwsException() {
        ModerationTools.getReportedMessages("", 5);
    }

    @Test(expected = IllegalArgumentException.class)
    public void getReportedMessages_lowercaseStrategy_throwsException() {
        ModerationTools.getReportedMessages("oldest", 5);
    }

    @Test(expected = IllegalArgumentException.class)
    public void getReportedMessages_zeroAmount_throwsException() {
        ModerationTools.getReportedMessages("OLDEST", 0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void getReportedMessages_negativeAmount_throwsException() {
        ModerationTools.getReportedMessages("MOST", -1);
    }

    @Test
    public void getReportedMessages_noReports_returnsEmptyIterator() {
        Iterator<Message> it = ModerationTools.getReportedMessages("OLDEST", 10);
        assertFalse(it.hasNext());
    }

    @Test
    public void getReportedMessages_allReportsRemoved_returnsEmpty() {
        ModerationTools.addReport(MSG_A, USER_1, 100L);
        ModerationTools.removeReport(MSG_A, USER_1, 100L);

        Iterator<Message> it = ModerationTools.getReportedMessages("OLDEST", 10);
        assertFalse(it.hasNext());
    }

    @Test
    public void getReportedMessages_oldest_singleReport_returnsThatMessage() {
        ModerationTools.addReport(MSG_B, USER_1, 500L);

        List<Message> results = toList(ModerationTools.getReportedMessages("OLDEST", 10));
        assertEquals(1, results.size());
        assertEquals(MSG_B, results.get(0).id());
    }

    @Test
    public void getReportedMessages_oldest_orderedByOldestReportTimestamp() {

        ModerationTools.addReport(MSG_A, USER_1, 300L);
        ModerationTools.addReport(MSG_C, USER_2,  50L);

        List<Message> results = toList(ModerationTools.getReportedMessages("OLDEST", 10));
        assertEquals(2, results.size());
        assertEquals(MSG_C, results.get(0).id());
        assertEquals(MSG_A, results.get(1).id());
    }

    @Test
    public void getReportedMessages_oldest_usesOldestReportNotLatest() {

        ModerationTools.addReport(MSG_A, USER_1,  10L);
        ModerationTools.addReport(MSG_A, USER_2, 999L);
        ModerationTools.addReport(MSG_B, USER_3,  20L);

        List<Message> results = toList(ModerationTools.getReportedMessages("OLDEST", 10));
        assertEquals(2, results.size());
        assertEquals(MSG_A, results.get(0).id());
        assertEquals(MSG_B, results.get(1).id());
    }

    @Test
    public void getReportedMessages_oldest_removingOldestReportChangesOrder() {

        ModerationTools.addReport(MSG_A, USER_1,  10L);
        ModerationTools.addReport(MSG_A, USER_2, 500L);
        ModerationTools.addReport(MSG_B, USER_3,  20L);
        ModerationTools.removeReport(MSG_A, USER_1, 0L);

        List<Message> results = toList(ModerationTools.getReportedMessages("OLDEST", 10));
        assertEquals(2, results.size());
        assertEquals(MSG_B, results.get(0).id());
        assertEquals(MSG_A, results.get(1).id());
    }

    @Test
    public void getReportedMessages_most_singleReport_returnsThatMessage() {
        ModerationTools.addReport(MSG_A, USER_1, 100L);

        List<Message> results = toList(ModerationTools.getReportedMessages("MOST", 10));
        assertEquals(1, results.size());
        assertEquals(MSG_A, results.get(0).id());
    }

    @Test
    public void getReportedMessages_most_orderedByReportCountDescending() {

        ModerationTools.addReport(MSG_A, USER_1, 100L);

        ModerationTools.addReport(MSG_B, USER_1, 100L);
        ModerationTools.addReport(MSG_B, USER_2, 200L);
        ModerationTools.addReport(MSG_B, USER_3, 300L);

        ModerationTools.addReport(MSG_C, USER_1, 100L);
        ModerationTools.addReport(MSG_C, USER_2, 200L);

        List<Message> results = toList(ModerationTools.getReportedMessages("MOST", 10));
        assertEquals(3, results.size());
        assertEquals(MSG_B, results.get(0).id());
        assertEquals(MSG_C, results.get(1).id());
        assertEquals(MSG_A, results.get(2).id());
    }

    @Test
    public void getReportedMessages_most_removingReportAffectsOrder() {

        ModerationTools.addReport(MSG_A, USER_1, 100L);
        ModerationTools.addReport(MSG_A, USER_2, 200L);
        ModerationTools.addReport(MSG_B, USER_3, 100L);
        ModerationTools.addReport(MSG_B, USER_4, 200L);
        ModerationTools.removeReport(MSG_A, USER_1, 0L);

        List<Message> results = toList(ModerationTools.getReportedMessages("MOST", 10));
        assertEquals(2, results.size());
        assertEquals(MSG_B, results.get(0).id());
    }

    @Test
    public void getReportedMessages_amountCapsResults() {
        ModerationTools.addReport(MSG_A, USER_1, 100L);
        ModerationTools.addReport(MSG_B, USER_2, 200L);
        ModerationTools.addReport(MSG_C, USER_3, 300L);

        List<Message> results = toList(ModerationTools.getReportedMessages("OLDEST", 2));
        assertEquals(2, results.size());
    }

    @Test
    public void getReportedMessages_amountLargerThanResults_returnsAll() {
        ModerationTools.addReport(MSG_A, USER_1, 100L);
        ModerationTools.addReport(MSG_B, USER_2, 200L);

        List<Message> results = toList(ModerationTools.getReportedMessages("MOST", 100));
        assertEquals(2, results.size());
    }

    @Test
    public void getReportedMessages_amountOne_returnsOnlyTopResult() {

        ModerationTools.addReport(MSG_A, USER_1, 100L);
        ModerationTools.addReport(MSG_B, USER_1, 100L);
        ModerationTools.addReport(MSG_B, USER_2, 200L);

        List<Message> results = toList(ModerationTools.getReportedMessages("MOST", 1));
        assertEquals(1, results.size());
        assertEquals(MSG_B, results.get(0).id());
    }

    @Test
    public void getReportedMessages_multipleReportsOnOneMessage_appearsOnce() {
        ModerationTools.addReport(MSG_A, USER_1, 100L);
        ModerationTools.addReport(MSG_A, USER_2, 200L);
        ModerationTools.addReport(MSG_A, USER_3, 300L);

        List<Message> results = toList(ModerationTools.getReportedMessages("MOST", 10));
        assertEquals(1, results.size());
        assertEquals(MSG_A, results.get(0).id());
    }

    private static List<Message> toList(Iterator<Message> iterator) {
        List<Message> list = new ArrayList<>();
        iterator.forEachRemaining(list::add);
        return list;
    }
}