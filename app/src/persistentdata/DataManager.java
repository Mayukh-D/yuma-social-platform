package persistentdata;

import dao.PostDAO;
import dao.ReportDAO;
import dao.UserDAO;
import dao.model.Message;
import dao.model.Post;
import dao.model.User;
import persistentdata.formatted.CSVFormat;
import persistentdata.formatted.CSVFormattedFactory;
import persistentdata.io.ComputerIOFactory;
import persistentdata.io.IOFactory;
import persistentdata.serialization.MessageSerializer;
import persistentdata.serialization.PostSerializer;
import persistentdata.serialization.ReportSerializer;
import persistentdata.serialization.UserSerializer;

import java.util.UUID;

/**
 * Coordinates reading and writing of all persistent application data.
 * Each entity type has its own CSV file and serializer.
 */
public class DataManager {

    private static DataManager instance;

    public static DataManager getInstance() {
        if (instance == null) instance = new DataManager();
        return instance;
    }

    private final IOFactory io = new ComputerIOFactory();

    private final DataPipeline<User, String[]> userPipeline = new DataPipeline<>(
            io, new CSVFormattedFactory(new CSVFormat(4)), new UserSerializer(), "users");

    private final DataPipeline<Post, String[]> postPipeline = new DataPipeline<>(
            io, new CSVFormattedFactory(new CSVFormat(3)), new PostSerializer(), "posts");

    // 6 columns: id, poster, thread, timestamp, message, hidden
    private final DataPipeline<Message, String[]> messagePipeline = new DataPipeline<>(
            io, new CSVFormattedFactory(new CSVFormat(6)), new MessageSerializer(), "messages");

    // 3 columns: message_uuid, user_uuid, timestamp
    private final DataPipeline<String[], String[]> reportPipeline = new DataPipeline<>(
            io, new CSVFormattedFactory(new CSVFormat(3)), new ReportSerializer(), "reports");

    private final UserDAO users = UserDAO.getInstance();
    private final PostDAO posts = PostDAO.getInstance();
    private final ReportDAO reports = ReportDAO.getInstance();

    /** Reads all data from disk, replacing any in-memory state. */
    public void readAll() {
        users.clear();
        posts.clear();
        reports.clear();

        userPipeline.readTo(users::add);
        postPipeline.readTo(posts::add);
        messagePipeline.readTo(message ->
                posts.get(new Post(message.thread())).messages.insert(message));
        reportPipeline.readTo(row ->
                reports.addReport(UUID.fromString(row[0]), UUID.fromString(row[1]), Long.parseLong(row[2])));
    }

    /** Writes all in-memory data to disk. */
    public void writeAll() {
        userPipeline.writeFrom(users.getAll());
        postPipeline.writeFrom(posts.getAll());
        messagePipeline.writeFrom(posts.getAllMessages());
        reportPipeline.writeFrom(reports.getAllReports());
    }
}