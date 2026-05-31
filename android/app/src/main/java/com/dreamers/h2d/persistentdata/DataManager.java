package com.dreamers.h2d.persistentdata;

import com.dreamers.h2d.dao.DirectMessageDAO;
import com.dreamers.h2d.dao.MessageDAO;
import com.dreamers.h2d.dao.PinDAO;
import com.dreamers.h2d.dao.PostDAO;
import com.dreamers.h2d.dao.ReactionDAO;
import com.dreamers.h2d.dao.ReportDAO;
import com.dreamers.h2d.dao.UserDAO;
import com.dreamers.h2d.dao.model.DirectMessage;
import com.dreamers.h2d.dao.model.Message;
import com.dreamers.h2d.dao.model.Post;
import com.dreamers.h2d.dao.model.ReactionType;
import com.dreamers.h2d.dao.model.User;
import com.dreamers.h2d.persistentdata.formatted.CSVFormat;
import com.dreamers.h2d.persistentdata.formatted.CSVFormattedFactory;
import com.dreamers.h2d.persistentdata.io.ComputerIOFactory;
import com.dreamers.h2d.persistentdata.io.IOFactory;
import com.dreamers.h2d.persistentdata.serialization.DirectMessageSerializer;
import com.dreamers.h2d.persistentdata.serialization.MessageSerializer;
import com.dreamers.h2d.persistentdata.serialization.PinSerializer;
import com.dreamers.h2d.persistentdata.serialization.PostSerializer;
import com.dreamers.h2d.persistentdata.serialization.ReactionSerializer;
import com.dreamers.h2d.persistentdata.serialization.ReportSerializer;
import com.dreamers.h2d.persistentdata.serialization.UserSerializer;

import java.util.UUID;

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

    // 3 columns: message_uuid, user_uuid, reaction_type
    private final DataPipeline<String[], String[]> reactionPipeline = new DataPipeline<>(
            io, new CSVFormattedFactory(new CSVFormat(3)), new ReactionSerializer(), "reactions");

    // 6 columns: id, sender, recipient, timestamp, content, read
    private final DataPipeline<DirectMessage, String[]> directMessagePipeline = new DataPipeline<>(
            io, new CSVFormattedFactory(new CSVFormat(6)), new DirectMessageSerializer(), "dms");

    // 2 columns: thread_uuid, message_uuid
    private final DataPipeline<String[], String[]> pinPipeline = new DataPipeline<>(
            io, new CSVFormattedFactory(new CSVFormat(2)), new PinSerializer(), "pins");

    private final UserDAO   users   = UserDAO.getInstance();
    private final PostDAO   posts   = PostDAO.getInstance();
    private final ReportDAO reports = ReportDAO.getInstance();

    public void readAll() {
        users.clear();
        posts.clear();
        reports.clear();
        MessageDAO.getInstance().clear();
        ReactionDAO.getInstance().clear();
        DirectMessageDAO.getInstance().clear();
        PinDAO.getInstance().clear();

        userPipeline.readTo(users::add);
        postPipeline.readTo(posts::add);
        messagePipeline.readTo(message -> {
            posts.get(new Post(message.thread())).messages.insert(message);
            MessageDAO.getInstance().add(message); // H1 BUG FIX
        });
        reportPipeline.readTo(row ->
                reports.addReport(UUID.fromString(row[0]), UUID.fromString(row[1]), Long.parseLong(row[2])));
        reactionPipeline.readTo(row ->
                ReactionDAO.getInstance().addReaction(
                        UUID.fromString(row[0]),
                        UUID.fromString(row[1]),
                        ReactionType.valueOf(row[2])));
        directMessagePipeline.readTo(DirectMessageDAO.getInstance()::add);
        pinPipeline.readTo(row ->
                PinDAO.getInstance().pin(UUID.fromString(row[0]), UUID.fromString(row[1])));
    }

    public void writeAll() {
        userPipeline.writeFrom(users.getAll());
        postPipeline.writeFrom(posts.getAll());
        messagePipeline.writeFrom(posts.getAllMessages());
        reportPipeline.writeFrom(reports.getAllReports());
        reactionPipeline.writeFrom(ReactionDAO.getInstance().getAllReactions());
        directMessagePipeline.writeFrom(DirectMessageDAO.getInstance().getAll());
        pinPipeline.writeFrom(PinDAO.getInstance().getAll());
    }
}