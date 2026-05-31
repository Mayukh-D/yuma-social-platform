package com.dreamers.h2d.common;

import android.content.Context;
import com.dreamers.h2d.dao.RandomContentGenerator;
import com.dreamers.h2d.dao.UserDAO;
import com.dreamers.h2d.dao.PostDAO;
import com.dreamers.h2d.persistentdata.DataManager;
import com.dreamers.h2d.session.SessionManager;
import com.dreamers.h2d.dao.model.User;

import java.util.UUID;

public class AppInitializer {
    private static boolean initialized = false;

    public static void init(Context ctx) {
        if (initialized) return;

        com.dreamers.h2d.persistentdata.io.ComputerIOFactory.init(ctx);
        DataManager.getInstance().readAll();


        // Ensure demo admin always exists
        if (UserDAO.getInstance().login("demoadmin", "demo1234") == null) {
            UserDAO.getInstance().add(new User(
                    UUID.randomUUID(),
                    User.Role.Admin,
                    "demoadmin",
                    "demo1234"
            ));
            DataManager.getInstance().writeAll();
        }

        // Seed data if no posts exist
        if (!PostDAO.getInstance().getAll().hasNext()) {
            RandomContentGenerator.populateRandomData();
            DataManager.getInstance().writeAll();
        }

        // Re-validate session after reload
        // The User object in SessionManager may be stale after readAll() clears UserDAO
        User currentUser = SessionManager.getInstance().getCurrentUser();
        if (currentUser != null) {
            User reloaded = UserDAO.getInstance().getByUUID(currentUser.id());
            if (reloaded != null) {
                SessionManager.getInstance().setCurrentUser(reloaded);
            } else {
                SessionManager.getInstance().logout();
            }
        }

        initialized = true;
    }
}
