package ru.demoexam.shoestore.ui;

import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import ru.demoexam.shoestore.db.Database;
import ru.demoexam.shoestore.model.Order;
import ru.demoexam.shoestore.model.Product;
import ru.demoexam.shoestore.service.AppSession;

public class AppNavigator {
    private final Stage primaryStage;
    private final Database database;
    private final AppSession session;
    private Stage productFormStage;
    private Stage orderFormStage;

    public AppNavigator(Stage primaryStage, Database database) {
        this.primaryStage = primaryStage;
        this.database = database;
        this.session = new AppSession();
        this.session.loginAsGuest();
    }

    public Database getDatabase() {
        return database;
    }

    public AppSession getSession() {
        return session;
    }

    public void showLogin() {
        primaryStage.setTitle("Вход в систему");
        primaryStage.setScene(new Scene(new LoginView(this), 500, 320));
        primaryStage.show();
    }

    public void showProducts() {
        primaryStage.setTitle("Список товаров");
        primaryStage.setScene(new Scene(new ProductListView(this), 1280, 760));
        primaryStage.show();
    }

    public void showOrders() {
        primaryStage.setTitle("Список заказов");
        primaryStage.setScene(new Scene(new OrdersView(this), 1050, 680));
        primaryStage.show();
    }

    public void openProductForm(Product product, Runnable onSave) {
        if (productFormStage != null && productFormStage.isShowing()) {
            productFormStage.toFront();
            AlertUtil.showWarning(
                "Окно уже открыто",
                "Форма редактирования товара уже открыта. Сначала завершите текущую операцию."
            );
            return;
        }

        productFormStage = new Stage();
        productFormStage.initOwner(primaryStage);
        productFormStage.initModality(Modality.WINDOW_MODAL);
        productFormStage.setTitle(product == null ? "Добавление товара" : "Редактирование товара");
        productFormStage.setScene(new Scene(new ProductFormView(this, product, onSave, productFormStage), 720, 700));
        productFormStage.show();
    }

    public void openOrderForm(Order order, Runnable onSave) {
        if (orderFormStage != null && orderFormStage.isShowing()) {
            orderFormStage.toFront();
            AlertUtil.showWarning(
                "Окно уже открыто",
                "Форма редактирования заказа уже открыта. Сначала завершите текущую операцию."
            );
            return;
        }

        orderFormStage = new Stage();
        orderFormStage.initOwner(primaryStage);
        orderFormStage.initModality(Modality.WINDOW_MODAL);
        orderFormStage.setTitle(order == null ? "Добавление заказа" : "Редактирование заказа");
        orderFormStage.setScene(new Scene(new OrderFormView(this, order, onSave, orderFormStage), 700, 520));
        orderFormStage.show();
    }
}
