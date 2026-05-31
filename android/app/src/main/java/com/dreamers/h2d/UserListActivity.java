package com.dreamers.h2d;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.dreamers.h2d.adapters.UserAdapter;
import com.dreamers.h2d.dao.UserDAO;
import com.dreamers.h2d.dao.model.User;
import com.dreamers.h2d.session.SessionManager;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

public class UserListActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_list);

        RecyclerView rvUsers = findViewById(R.id.rvUsers);
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        rvUsers.setLayoutManager(new LinearLayoutManager(this));

        UUID currentId = SessionManager.getInstance().getCurrentUser().id();
        List<User> users = new ArrayList<>();
        Iterator<User> it = UserDAO.getInstance().getAll();
        while (it.hasNext()) {
            User u = it.next();
            if (!u.id().equals(currentId)) users.add(u);
        }

        UserAdapter adapter = new UserAdapter(users, user -> {
            Intent intent = new Intent(this, ConversationActivity.class);
            intent.putExtra("partnerId", user.id().toString());
            finish();
            startActivity(intent);
        });
        rvUsers.setAdapter(adapter);
    }
}