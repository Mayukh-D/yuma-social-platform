package com.dreamers.h2d;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.dreamers.h2d.adapters.PostAdapter;
import com.dreamers.h2d.common.AppInitializer;
import com.dreamers.h2d.dao.PostDAO;
import com.dreamers.h2d.dao.model.Post;
import com.dreamers.h2d.persistentdata.DataManager;
import com.dreamers.h2d.session.SessionManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {
    private List<Post> posts = new ArrayList<>();
    private List<Post> allPosts = new ArrayList<>();
    private PostAdapter adapter;
    private RecyclerView rvPosts;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppInitializer.init(this);
        setContentView(R.layout.activity_main);

        // Reports
        View btnReports = findViewById(R.id.btnReports);
        if (SessionManager.getInstance().isAdmin()) {
            btnReports.setVisibility(View.VISIBLE);
            btnReports.setOnClickListener(v ->
                    startActivity(new Intent(this, ReportsActivity.class)));
        }

        findViewById(R.id.btnProfile).setOnClickListener(v -> {
            if (SessionManager.getInstance().getCurrentUser() == null) return;
            Intent intent = new Intent(this, ProfileActivity.class);
            intent.putExtra("userId", SessionManager.getInstance().getCurrentUser().id().toString());
            startActivity(intent);
        });

        findViewById(R.id.btnDMs).setOnClickListener(v -> {
            if (SessionManager.getInstance().getCurrentUser() == null) return;
            Intent intent = new Intent(this, InboxActivity.class);
            intent.putExtra("currentUserId", SessionManager.getInstance().getCurrentUser().id().toString());
            startActivity(intent);
        });

        findViewById(R.id.btnNewPost).setOnClickListener(v -> {
            if (SessionManager.getInstance().getCurrentUser() == null) return;
            EditText input = new EditText(this);
            input.setHint("Post topic");
            new AlertDialog.Builder(this)
                    .setTitle("Create Post")
                    .setView(input)
                    .setPositiveButton("Post", (dialog, which) -> {
                        String topic = input.getText().toString().trim();
                        if (topic.isEmpty()) return;
                        Post newPost = new Post(UUID.randomUUID(),
                                SessionManager.getInstance().getCurrentUser().id(), topic);
                        PostDAO.getInstance().add(newPost);
                        DataManager.getInstance().writeAll();
                        loadPosts();
                        adapter.notifyDataSetChanged();
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        });

        findViewById(R.id.btnLogout).setOnClickListener(v -> {
            DataManager.getInstance().writeAll();
            SessionManager.getInstance().logout();
            Intent intent = new Intent(this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });

        findViewById(R.id.btnLogout).setOnLongClickListener(v -> {
            if (SessionManager.getInstance().isAdmin()) {
                startActivity(new Intent(this, ReportsActivity.class));
                return true;
            }
            return false;
        });

        rvPosts = findViewById(R.id.rvPosts);
        rvPosts.setLayoutManager(new LinearLayoutManager(this));
        loadPosts();
        adapter = new PostAdapter(posts, post -> {
            Intent intent = new Intent(this, PostViewerActivity.class);
            intent.putExtra("postId", post.id.toString());
            startActivity(intent);
        });
        rvPosts.setAdapter(adapter);

        EditText etSearch = findViewById(R.id.etSearch);
        etSearch.addTextChangedListener(new android.text.TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                String query = s.toString().toLowerCase().trim();
                posts.clear();
                if (query.isEmpty()) {
                    posts.addAll(allPosts);
                } else {
                    for (Post p : allPosts) {
                        if (p.topic.toLowerCase().contains(query)) posts.add(p);
                    }
                }
                adapter.notifyDataSetChanged();
            }
            @Override public void afterTextChanged(android.text.Editable s) {}
        });
    }

    private void loadPosts() {
        posts.clear();
        allPosts.clear();
        Iterator<Post> it = PostDAO.getInstance().getAll();
        while (it.hasNext()) allPosts.add(it.next());
        Collections.sort(allPosts, (a, b) -> a.topic.compareToIgnoreCase(b.topic));
        posts.addAll(allPosts);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (SessionManager.getInstance().getCurrentUser() == null) {
            Intent intent = new Intent(this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            return;
        }
        if (adapter != null) {
            loadPosts();
            adapter.notifyDataSetChanged();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        DataManager.getInstance().writeAll();
    }
}