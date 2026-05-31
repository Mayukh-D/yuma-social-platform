package com.dreamers.h2d;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.dreamers.h2d.adapters.DMAdapter;
import com.dreamers.h2d.dao.DirectMessageDAO;
import com.dreamers.h2d.dao.UserDAO;
import com.dreamers.h2d.dao.model.DirectMessage;
import com.dreamers.h2d.dao.model.User;
import com.dreamers.h2d.messaging.DirectMessageTools;
import com.dreamers.h2d.persistentdata.DataManager;
import com.dreamers.h2d.session.SessionManager;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ConversationActivity extends AppCompatActivity {

    private UUID currentUserId;
    private UUID partnerId;
    private List<DirectMessage> messages;
    private DMAdapter adapter;
    private RecyclerView recycler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_conversation);

        ImageButton backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(v -> finish());

        if (SessionManager.getInstance().getCurrentUser() == null) {
            finish();
            return;
        }
        currentUserId = SessionManager.getInstance().getCurrentUser().id();

        String partnerIdStr = getIntent().getStringExtra("partnerId");
        if (partnerIdStr == null) { finish(); return; }
        partnerId = UUID.fromString(partnerIdStr);

        User partner = UserDAO.getInstance().getByUUID(partnerId);
        TextView header = findViewById(R.id.conversationHeader);
        header.setText(partner != null ? partner.username() : "Unknown");

        for (DirectMessage dm : DirectMessageDAO.getInstance().getConversation(currentUserId, partnerId)) {
            if (dm.recipient().equals(currentUserId) && !dm.isRead()) {
                DirectMessageTools.markRead(dm.id());
            }
        }

        messages = new ArrayList<>(DirectMessageDAO.getInstance().getConversation(currentUserId, partnerId));
        adapter = new DMAdapter(messages, currentUserId);
        recycler = findViewById(R.id.dmList);
        recycler.setLayoutManager(new LinearLayoutManager(this));
        recycler.setAdapter(adapter);
        scrollToBottom();

        EditText input = findViewById(R.id.messageInput);
        Button send = findViewById(R.id.sendButton);
        send.setOnClickListener(v -> sendMessage(input));
    }

    private void sendMessage(EditText input) {
        String text = input.getText().toString().trim();
        if (text.isEmpty()) return;

        boolean ok = DirectMessageTools.send(
                currentUserId, partnerId, text, System.currentTimeMillis());
        if (!ok) {
            Toast.makeText(this, "Failed to send", Toast.LENGTH_SHORT).show();
            return;
        }

        DataManager.getInstance().writeAll();
        input.setText("");
        messages.clear();
        messages.addAll(DirectMessageDAO.getInstance().getConversation(currentUserId, partnerId));
        adapter.notifyDataSetChanged();
        scrollToBottom();
    }

    private void scrollToBottom() {
        if (!messages.isEmpty()) {
            recycler.scrollToPosition(messages.size() - 1);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        DataManager.getInstance().writeAll();
    }
}