package com.dreamers.h2d;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import com.dreamers.h2d.adapters.MessageAdapter;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.dreamers.h2d.dao.MessageDAO;
import com.dreamers.h2d.dao.PostDAO;
import com.dreamers.h2d.dao.UserDAO;
import com.dreamers.h2d.dao.model.Message;
import com.dreamers.h2d.dao.model.Post;
import com.dreamers.h2d.dao.model.User;
import com.dreamers.h2d.persistentdata.DataManager;
import com.dreamers.h2d.pinning.PinTools;
import com.dreamers.h2d.session.SessionManager;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

public class PostViewerActivity extends AppCompatActivity {

    private UUID threadId;
    private Post post;
    private boolean isAdmin;

    private TextView tvPinnedHeader;
    private RecyclerView rvPinnedMessages;
    private View divider;
    private RecyclerView rvMessages;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_viewer);

        String postIdStr = getIntent().getStringExtra("postId");
        if (postIdStr == null) {
            Toast.makeText(this, "No post selected", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        threadId = UUID.fromString(postIdStr);
        post     = PostDAO.getInstance().get(new Post(threadId));
        isAdmin  = SessionManager.getInstance().isAdmin();

        if (post == null) {
            Toast.makeText(this, "Post not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        TextView tvTitle  = findViewById(R.id.tvPostTitle);
        TextView tvAuthor = findViewById(R.id.tvPostAuthor);
        tvPinnedHeader    = findViewById(R.id.tvPinnedHeader);
        rvPinnedMessages  = findViewById(R.id.rvPinnedMessages);
        divider           = findViewById(R.id.divider);
        rvMessages        = findViewById(R.id.rvMessages);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        tvTitle.setText(post.topic);
        User author = UserDAO.getInstance().getByUUID(post.poster);
        tvAuthor.setText("by " + (author != null ? author.username() : "Unknown"));

        loadMessages();

        EditText etNewMessage  = findViewById(R.id.etNewMessage);
        Button btnSendMessage  = findViewById(R.id.btnSendMessage);
        btnSendMessage.setOnClickListener(v -> {
            String text = etNewMessage.getText().toString().trim();
            if (text.isEmpty()) return;
            Message newMessage = new Message(
                    UUID.randomUUID(),
                    SessionManager.getInstance().getCurrentUser().id(),
                    threadId,
                    System.currentTimeMillis(),
                    text
            );
            post.messages.insert(newMessage);
            MessageDAO.getInstance().add(newMessage);
            etNewMessage.setText("");
            DataManager.getInstance().writeAll();
            loadMessages();
        });
    }

    private void loadMessages() {
        List<Message> pinnedList = new ArrayList<>();
        Iterator<Message> pinned = PinTools.getPinnedMessages(threadId);
        while (pinned.hasNext()) pinnedList.add(pinned.next());

        if (!pinnedList.isEmpty()) {
            tvPinnedHeader.setVisibility(View.VISIBLE);
            rvPinnedMessages.setVisibility(View.VISIBLE);
            divider.setVisibility(View.VISIBLE);
            rvPinnedMessages.setLayoutManager(new LinearLayoutManager(this));
            rvPinnedMessages.setAdapter(new MessageAdapter(pinnedList, isAdmin, threadId));
        } else {
            tvPinnedHeader.setVisibility(View.GONE);
            rvPinnedMessages.setVisibility(View.GONE);
            divider.setVisibility(View.GONE);
        }

        List<Message> allMessages = new ArrayList<>();
        Iterator<Message> messages = post.getVisibleMessages(isAdmin).getAll();
        while (messages.hasNext()) allMessages.add(messages.next());

        rvMessages.setLayoutManager(new LinearLayoutManager(this));
        rvMessages.setAdapter(new MessageAdapter(allMessages, isAdmin, threadId));
    }

    @Override
    protected void onStop() {
        super.onStop();
        DataManager.getInstance().writeAll();
    }
}