package com.dreamers.h2d;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Toast;
import android.widget.Spinner;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.dreamers.h2d.adapters.MessageAdapter;
import com.dreamers.h2d.dao.model.Message;
import com.dreamers.h2d.moderation.ModerationTools;
import com.dreamers.h2d.session.SessionManager;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class ReportsActivity extends AppCompatActivity {

    private RecyclerView rvReports;
    private String currentStrategy = "MOST";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!SessionManager.getInstance().isAdmin()) {
            Toast.makeText(this, "Admins only.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        setContentView(R.layout.activity_reports);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        rvReports = findViewById(R.id.rvReports);
        rvReports.setLayoutManager(new LinearLayoutManager(this));

        Spinner spinner = findViewById(R.id.spinnerStrategy);
        String[] strategies = {"MOST", "OLDEST"};
        spinner.setAdapter(new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item, strategies));

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> p, View v, int pos, long id) {
                currentStrategy = strategies[pos];
                loadReports();
            }
            @Override
            public void onNothingSelected(AdapterView<?> p) {}
        });

        loadReports();
    }

    private void loadReports() {
        List<Message> reported = new ArrayList<>();
        try {
            Iterator<Message> it = ModerationTools.getReportedMessages(currentStrategy, 50);
            while (it.hasNext()) reported.add(it.next());
        } catch (Exception e) {
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
        rvReports.setAdapter(new MessageAdapter(reported, true, null));
    }
}