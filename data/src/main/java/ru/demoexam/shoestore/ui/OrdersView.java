package ru.demoexam.shoestore.ui;

import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import ru.demoexam.shoestore.model.Order;
import ru.demoexam.shoestore.model.Role;
import ru.demoexam.shoestore.repository.OrderRepository;

public class OrdersView extends BorderPane {
    private final AppNavigator navigator;
    private final OrderRepository orderRepository;
    private final TableView<Order> tableView = new TableView<>();

    public OrdersView(AppNavigator navigator) {
        this.navigator = navigator;
        this.orderRepository = new OrderRepository(navigator.getDatabase());

        setPadding(new Insets(16));
        setTop(buildHeader());
        setCenter(buildTable());
        refreshTable();
    }

    private Node buildHeader() {
        Label title = new Label("Список заказов");
        title.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");

        Label userLabel = new Label("Пользователь: " + navigator.getSession().getCurrentDisplayName());
        Button backButton = new Button("Назад к товарам");
        backButton.setOnAction(event -> navigator.showProducts());

        HBox header = new HBox(15, title, new Label("|"), userLabel, backButton);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(0, 0, 16, 0));
        return header;
    }

    private Node buildTable() {
        TableColumn<Order, Number> idColumn = new TableColumn<>("Номер");
        idColumn.setCellValueFactory(value -> new SimpleObjectProperty<>(value.getValue().getId()));

        TableColumn<Order, String> itemsColumn = new TableColumn<>("Артикулы заказа");
        itemsColumn.setCellValueFactory(value -> new SimpleStringProperty(value.getValue().getItemsSummary()));

        TableColumn<Order, String> clientColumn = new TableColumn<>("Клиент");
        clientColumn.setCellValueFactory(value -> new SimpleStringProperty(value.getValue().getClientFullName()));

        TableColumn<Order, String> pickupColumn = new TableColumn<>("Пункт выдачи");
        pickupColumn.setCellValueFactory(value -> new SimpleStringProperty(value.getValue().getPickupPoint().name()));

        TableColumn<Order, Number> codeColumn = new TableColumn<>("Код");
        codeColumn.setCellValueFactory(value -> new SimpleObjectProperty<>(value.getValue().getPickupCode()));

        TableColumn<Order, String> statusColumn = new TableColumn<>("Статус");
        statusColumn.setCellValueFactory(value -> new SimpleStringProperty(value.getValue().getStatus()));

        TableColumn<Order, String> orderDateColumn = new TableColumn<>("Дата заказа");
        orderDateColumn.setCellValueFactory(value -> new SimpleStringProperty(value.getValue().getOrderDate().toString()));

        TableColumn<Order, String> deliveryDateColumn = new TableColumn<>("Дата доставки");
        deliveryDateColumn.setCellValueFactory(value -> new SimpleStringProperty(value.getValue().getDeliveryDate().toString()));

        tableView.getColumns().addAll(idColumn, itemsColumn, clientColumn, pickupColumn, codeColumn, statusColumn, orderDateColumn, deliveryDateColumn);
        tableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);

        if (navigator.getSession().getCurrentRole() == Role.ADMIN) {
            tableView.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && tableView.getSelectionModel().getSelectedItem() != null) {
                    navigator.openOrderForm(tableView.getSelectionModel().getSelectedItem(), this::refreshTable);
                }
            });
        }

        Button addButton = new Button("Добавить заказ");
        addButton.setVisible(navigator.getSession().getCurrentRole() == Role.ADMIN);
        addButton.setOnAction(event -> navigator.openOrderForm(null, this::refreshTable));

        Button deleteButton = new Button("Удалить заказ");
        deleteButton.setVisible(navigator.getSession().getCurrentRole() == Role.ADMIN);
        deleteButton.setOnAction(event -> deleteSelectedOrder());

        return new VBox(10, tableView, new HBox(10, addButton, deleteButton));
    }

    private void refreshTable() {
        tableView.setItems(FXCollections.observableArrayList(orderRepository.findAll()));
    }

    private void deleteSelectedOrder() {
        Order order = tableView.getSelectionModel().getSelectedItem();
        if (order == null) {
            AlertUtil.showWarning("Удаление заказа", "Выберите заказ для удаления.");
            return;
        }
        orderRepository.delete(order.getId());
        refreshTable();
        AlertUtil.showInfo("Удаление заказа", "Заказ удален.");
    }
}
