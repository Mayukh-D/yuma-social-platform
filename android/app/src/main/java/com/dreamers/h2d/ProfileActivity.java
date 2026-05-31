package com.dreamers.h2d;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.dreamers.h2d.adapters.PostAdapter;
import com.dreamers.h2d.dao.PostDAO;
import com.dreamers.h2d.dao.UserDAO;
import com.dreamers.h2d.dao.model.Message;
import com.dreamers.h2d.dao.model.Post;
import com.dreamers.h2d.dao.model.User;
import com.dreamers.h2d.reactions.ReactionTools;
import com.dreamers.h2d.session.SessionManager;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

public class ProfileActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        String userIdStr = getIntent().getStringExtra("userId");
        UUID userId = UUID.fromString(userIdStr);
        User user = UserDAO.getInstance().getByUUID(userId);
        User currentUser = SessionManager.getInstance().getCurrentUser();

        TextView tvUsername = findViewById(R.id.tvUsername);
        TextView tvRole = findViewById(R.id.tvRole);
        Button btnSendDM = findViewById(R.id.btnSendDM);
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        // their posts
        RecyclerView rvUserPosts = findViewById(R.id.rvUserPosts);
        rvUserPosts.setLayoutManager(new LinearLayoutManager(this));
        List<Post> userPosts = new ArrayList<>();
        Iterator<Post> it = PostDAO.getInstance().getAll();
        while (it.hasNext()) {
            Post p = it.next();
            if (p.poster.equals(userId)) userPosts.add(p);
        }
        rvUserPosts.setAdapter(new PostAdapter(userPosts, post -> {
            Intent intent = new Intent(this, PostViewerActivity.class);
            intent.putExtra("postId", post.id.toString());
            startActivity(intent);
        }));

        tvUsername.setText(user.username());
        tvRole.setText(user.role().toString());

        // count total reactions received on all their messages
        int totalReactions = 0;
        Iterator<Post> postIt = PostDAO.getInstance().getAll();
        while (postIt.hasNext()) {
            Post p = postIt.next();
            Iterator<Message> msgIt = p.messages.getAll();
            while (msgIt.hasNext()) {
                Message m = msgIt.next();
                if (m.poster().equals(userId)) {
                    totalReactions += ReactionTools.getTotalReactions(m.id());
                }
            }
        }
        TextView tvReactions = findViewById(R.id.tvReactions);
        tvReactions.setText("⭐ " + totalReactions + " reactions received");

        if (user.id().equals(currentUser.id())) {
            btnSendDM.setVisibility(View.GONE);
        } else {
            btnSendDM.setVisibility(View.VISIBLE);
            btnSendDM.setOnClickListener(v -> {
                Intent intent = new Intent(this, ConversationActivity.class);
                intent.putExtra("partnerId", user.id().toString());
                startActivity(intent);
            });
        }


    }
}