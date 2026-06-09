package ru.demoexam.shoestore.service;

import ru.demoexam.shoestore.model.Role;
import ru.demoexam.shoestore.model.User;

public class AppSession {
    private User currentUser;
    private Role currentRole = Role.GUEST;
    private String currentDisplayName = "Гость";

    public void login(User user) {
        this.currentUser = user;
        this.currentRole = user.role();
        this.currentDisplayName = user.fullName();
    }

    public void loginAsGuest() {
        this.currentUser = null;
        this.currentRole = Role.GUEST;
        this.currentDisplayName = "Гость";
    }

    public void logout() {
        loginAsGuest();
    }

    public User getCurrentUser() {
        return currentUser;
    }

    public Role getCurrentRole() {
        return currentRole;
    }

    public String getCurrentDisplayName() {
        return currentDisplayName;
    }
}
