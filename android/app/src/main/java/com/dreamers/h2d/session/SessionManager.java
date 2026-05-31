package com.dreamers.h2d.session;

import com.dreamers.h2d.dao.model.User;

public class SessionManager {
    private static SessionManager instance;
    private User currentUser;

    private SessionManager() {}

    public static SessionManager getInstance() {
        if (instance == null) instance = new SessionManager();
        return instance;
    }

    public User getCurrentUser() { return currentUser; }
    public void setCurrentUser(User user) { this.currentUser = user; }
    public void logout() { this.currentUser = null; }
    public boolean isLoggedIn() { return currentUser != null; }
    public boolean isAdmin() {
        return currentUser != null && currentUser.role() == User.Role.Admin;
    }
}