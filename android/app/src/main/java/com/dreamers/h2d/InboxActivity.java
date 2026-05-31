package com.dreamers.h2d;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.dreamers.h2d.adapters.ConversationListAdapter;
import com.dreamers.h2d.dao.DirectMessageDAO;
import com.dreamers.h2d.messaging.ConversationSortStrategy;
import com.dreamers.h2d.session.SessionManager;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

public class InboxActivity extends AppCompatActivity {

    private UUID currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inbox);

        if (SessionManager.getInstance().getCurrentUser() == null) {
            finish();
            return;
        }
        currentUserId = SessionManager.getInstance().getCurrentUser().id();
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        DirectMessageDAO dmDAO = DirectMessageDAO.getInstance();
        RecyclerView recycler = findViewById(R.id.conversationsList);
        recycler.setLayoutManager(new LinearLayoutManager(this));

        View sortButton = findViewById(R.id.sortSpinner);
        sortButton.setOnClickListener(v -> {
            String[] options = {"Most Recent", "Unread First"};
            new AlertDialog.Builder(this)
                    .setTitle("Sort by")
                    .setItems(options, (dialog, which) -> {
                        String strategy = which == 0 ? "RECENT" : "UNREAD";
                        refreshInbox(strategy, dmDAO, recycler);
                    })
                    .show();
        });

        refreshInbox("RECENT", dmDAO, recycler);

        Button newDM = findViewById(R.id.newDMButton);
        newDM.setOnClickListener(v -> {
            Intent intent = new Intent(this, UserListActivity.class);
            intent.putExtra("currentUserId", currentUserId.toString());
            startActivity(intent);
        });
    }

    private void refreshInbox(String strategy, DirectMessageDAO dmDAO, RecyclerView recycler) {
        List<UUID> partners = new ArrayList<>(dmDAO.getConversationPartners(currentUserId));
        ConversationSortStrategy sort = ConversationSortStrategy.Factory.create(strategy);
        Comparator<UUID> cmp = sort.comparator(currentUserId, dmDAO);
        partners.sort(cmp);

        ConversationListAdapter adapter = new ConversationListAdapter(partners, currentUserId);
        adapter.setOnClickListener(partnerId -> {
            Intent intent = new Intent(this, ConversationActivity.class);
            intent.putExtra("partnerId", partnerId.toString());
            startActivity(intent);
        });
        recycler.setAdapter(adapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        RecyclerView recycler = findViewById(R.id.conversationsList);
        if (recycler != null) {
            refreshInbox("RECENT", DirectMessageDAO.getInstance(), recycler);
        }
    }
}