package ru.demoexam.shoestore.model;

public record User(int id, String fullName, String login, String password, Role role) {
}
