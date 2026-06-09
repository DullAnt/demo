package ru.demoexam.shoestore.model;

public enum Role {
    GUEST("Гость"),
    CLIENT("Клиент"),
    MANAGER("Менеджер"),
    ADMIN("Администратор");

    private final String title;

    Role(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }
}
