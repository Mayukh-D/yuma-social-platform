import dao.MessageDAO;
import dao.ReportDAO;
import dao.UserDAO;
import dao.model.Message;
import dao.model.User;
import moderation.ModerationTools;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.UUID;

import static org.junit.Assert.*;

/**
 * Branches covered:
 *   ModerationTools.addReport:
 *     1. message UUID does not exist -> false
 *     2. message exists, user UUID does not exist -> false
 *     3. message and user exist, user has not yet reported -> true
 *     4. message and user exist, user has already reported -> false
 *
 *   ReportDAO.addReport:
 *     5. message has no prior reports (computeIfAbsent creates new set) -> true
 *     6. message has prior reports from another user (set already exists) -> true
 *     7. user already in the set -> false (duplicate)
 */
public class ModerationToolsAddReportTests {
    private static final UUID MSG_ID  = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private static final UUID USER_ID = UUID.fromString("00000000-0000-0000-0000-000000000002");
    private static final UUID OTHER_USER_ID = UUID.fromString("00000000-0000-0000-0000-000000000003");
    private static final UUID UNKNOWN_UUID  = UUID.fromString("00000000-0000-0000-0000-000000000099");

    private final MessageDAO messageDAO = MessageDAO.getInstance();
    private final UserDAO userDAO = UserDAO.getInstance();
    private final ReportDAO reportDAO = ReportDAO.getInstance();

    /** Fresh user and message before each test reigstered */
    @Before
    public void setUp() {

        userDAO.clear();
        reportDAO.clear();
        messageDAO.remove(MSG_ID);

        messageDAO.add(new Message(MSG_ID, UUID.randomUUID(), UUID.randomUUID(), 1000L, "hello"));

        userDAO.add(new User(USER_ID, User.Role.Member, "userA1234", "pass"));
        userDAO.add(new User(OTHER_USER_ID, User.Role.Member, "userB1234", "pass"));
    }

    @After
    public void tearDown() {
        userDAO.clear();
        reportDAO.clear();
        messageDAO.remove(MSG_ID);
    }

    @Test
    public void addReport_unknownMessage_returnsFalse() {
        boolean result = ModerationTools.addReport(UNKNOWN_UUID, USER_ID, 1000L);
        assertFalse(result);
    }

    @Test
    public void addReport_unknownMessage_doesNotRecordReport() {
        ModerationTools.addReport(UNKNOWN_UUID, USER_ID, 1000L);
        assertFalse(reportDAO.hasReported(UNKNOWN_UUID, USER_ID));
    }

    @Test
    public void addReport_unknownUser_returnsFalse() {
        boolean result = ModerationTools.addReport(MSG_ID, UNKNOWN_UUID, 1000L);
        assertFalse(result);
    }

    @Test
    public void addReport_unknownUser_doesNotRecordReport() {
        ModerationTools.addReport(MSG_ID, UNKNOWN_UUID, 1000L);
        assertFalse(reportDAO.hasReported(MSG_ID, UNKNOWN_UUID));
    }

    @Test
    public void addReport_validFirstReport_returnsTrue() {
        boolean result = ModerationTools.addReport(MSG_ID, USER_ID, 1000L);
        assertTrue(result);
    }

    @Test
    public void addReport_validFirstReport_isRecorded() {
        ModerationTools.addReport(MSG_ID, USER_ID, 1000L);
        assertTrue(reportDAO.hasReported(MSG_ID, USER_ID));
    }

    @Test
    public void addReport_duplicateReport_returnsFalse() {
        ModerationTools.addReport(MSG_ID, USER_ID, 1000L);
        boolean result = ModerationTools.addReport(MSG_ID, USER_ID, 2000L);
        assertFalse(result);
    }

    @Test
    public void addReport_duplicateReport_stillOnlyRecordedOnce() {
        ModerationTools.addReport(MSG_ID, USER_ID, 1000L);
        ModerationTools.addReport(MSG_ID, USER_ID, 2000L);
        // Report count must still be 1
        assertEquals(1, reportDAO.getReportCount(MSG_ID));
    }
    @Test
    public void addReport_secondUserReportsSameMessage_returnsTrue() {
        ModerationTools.addReport(MSG_ID, USER_ID, 1000L);
        boolean result = ModerationTools.addReport(MSG_ID, OTHER_USER_ID, 2000L);
        assertTrue(result);
    }

    @Test
    public void addReport_secondUserReportsSameMessage_bothRecorded() {
        ModerationTools.addReport(MSG_ID, USER_ID, 1000L);
        ModerationTools.addReport(MSG_ID, OTHER_USER_ID, 2000L);
        assertTrue(reportDAO.hasReported(MSG_ID, USER_ID));
        assertTrue(reportDAO.hasReported(MSG_ID, OTHER_USER_ID));
    }

    @Test
    public void addReport_bothUnknown_returnsFalse() {
        boolean result = ModerationTools.addReport(UNKNOWN_UUID, UNKNOWN_UUID, 1000L);
        assertFalse(result);
    }
}