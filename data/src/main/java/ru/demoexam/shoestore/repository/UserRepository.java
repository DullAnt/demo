package ru.demoexam.shoestore.repository;

import ru.demoexam.shoestore.db.Database;
import ru.demoexam.shoestore.model.Role;
import ru.demoexam.shoestore.model.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Optional;

public class UserRepository {
    private final Database database;

    public UserRepository(Database database) {
        this.database = database;
    }

    public Optional<User> findByCredentials(String login, String password) {
        try (Connection connection = database.getConnection();
             PreparedStatement statement = connection.prepareStatement("""
                 SELECT id, full_name, login, password, role
                 FROM users
                 WHERE login = ? AND password = ?
                 """)) {
            statement.setString(1, login);
            statement.setString(2, password);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    return Optional.empty();
                }

                return Optional.of(new User(
                    resultSet.getInt("id"),
                    resultSet.getString("full_name"),
                    resultSet.getString("login"),
                    resultSet.getString("password"),
                    Role.valueOf(resultSet.getString("role"))
                ));
            }
        } catch (Exception exception) {
            throw new IllegalStateException("Не удалось выполнить авторизацию.", exception);
        }
    }
}
