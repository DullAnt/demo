package ru.demoexam.shoestore.ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import ru.demoexam.shoestore.repository.UserRepository;

public class LoginView extends VBox {
    public LoginView(AppNavigator navigator) {
        setSpacing(12);
        setPadding(new Insets(30));
        setAlignment(Pos.CENTER);
        setStyle("-fx-background-color: linear-gradient(to bottom, #eef6ff, #ffffff);");

        Label title = new Label("Магазин стройматериалов");
        title.setStyle("-fx-font-size: 28px; -fx-font-weight: bold;");

        Label subtitle = new Label("Авторизация пользователя");
        subtitle.setStyle("-fx-text-fill: #56657a;");

        TextField loginField = new TextField();
        loginField.setPromptText("Логин");
        loginField.setMaxWidth(260);

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Пароль");
        passwordField.setMaxWidth(260);

        Button signInButton = new Button("Войти");
        signInButton.setDefaultButton(true);
        signInButton.setMaxWidth(260);

        Button guestButton = new Button("Продолжить как гость");
        guestButton.setMaxWidth(260);

        UserRepository userRepository = new UserRepository(navigator.getDatabase());

        signInButton.setOnAction(event -> {
            String login = loginField.getText().trim();
            String password = passwordField.getText().trim();

            if (login.isBlank() || password.isBlank()) {
                AlertUtil.showWarning("Ошибка входа", "Введите логин и пароль.");
                return;
            }

            userRepository.findByCredentials(login, password).ifPresentOrElse(user -> {
                navigator.getSession().login(user);
                navigator.showProducts();
            }, () -> AlertUtil.showError(
                "Ошибка входа",
                "Пользователь с указанным логином и паролем не найден. Проверьте введенные данные."
            ));
        });

        guestButton.setOnAction(event -> {
            navigator.getSession().loginAsGuest();
            navigator.showProducts();
        });

        getChildren().addAll(title, subtitle, loginField, passwordField, signInButton, guestButton);
    }
}
