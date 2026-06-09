package ru.demoexam.shoestore.ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import ru.demoexam.shoestore.model.LookupValue;
import ru.demoexam.shoestore.model.Order;
import ru.demoexam.shoestore.model.OrderItem;
import ru.demoexam.shoestore.model.Product;
import ru.demoexam.shoestore.repository.LookupRepository;
import ru.demoexam.shoestore.repository.OrderRepository;
import ru.demoexam.shoestore.repository.ProductRepository;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class OrderFormView extends BorderPane {
    public OrderFormView(AppNavigator navigator, Order sourceOrder, Runnable onSave, Stage stage) {
        setPadding(new Insets(16));

        OrderRepository orderRepository = new OrderRepository(navigator.getDatabase());
        ProductRepository productRepository = new ProductRepository(navigator.getDatabase());
        LookupRepository lookupRepository = new LookupRepository(navigator.getDatabase());

        Label header = new Label(sourceOrder == null ? "Добавление заказа" : "Редактирование заказа");
        header.setStyle("-fx-font-size: 22px; -fx-font-weight: bold;");
        setTop(header);
        BorderPane.setMargin(header, new Insets(0, 0, 16, 0));

        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(10);

        TextField idField = new TextField();
        idField.setEditable(false);
        idField.setText(sourceOrder == null ? String.valueOf(orderRepository.getNextId()) : String.valueOf(sourceOrder.getId()));

        TextArea itemsArea = new TextArea();
        itemsArea.setPromptText("Формат: PMEZMH, 2, BPV4MM, 1");
        itemsArea.setPrefRowCount(3);

        ComboBox<LookupValue> pickupPointBox = new ComboBox<>();
        pickupPointBox.getItems().addAll(lookupRepository.findAll("pickup_points", "address"));

        TextField clientField = new TextField();
        TextField codeField = new TextField();

        ComboBox<String> statusBox = new ComboBox<>();
        statusBox.getItems().addAll("Новый", "Ожидает выдачи", "Завершен", "Отменен");

        DatePicker orderDatePicker = new DatePicker(LocalDate.now());
        DatePicker deliveryDatePicker = new DatePicker(LocalDate.now().plusDays(3));

        if (sourceOrder != null) {
            itemsArea.setText(sourceOrder.getItemsSummary());
            pickupPointBox.setValue(sourceOrder.getPickupPoint());
            clientField.setText(sourceOrder.getClientFullName());
            codeField.setText(String.valueOf(sourceOrder.getPickupCode()));
            statusBox.setValue(sourceOrder.getStatus());
            orderDatePicker.setValue(sourceOrder.getOrderDate());
            deliveryDatePicker.setValue(sourceOrder.getDeliveryDate());
        }

        addRow(grid, 0, "Номер заказа", idField);
        addRow(grid, 1, "Артикул заказа", itemsArea);
        addRow(grid, 2, "Пункт выдачи", pickupPointBox);
        addRow(grid, 3, "ФИО клиента", clientField);
        addRow(grid, 4, "Код получения", codeField);
        addRow(grid, 5, "Статус заказа", statusBox);
        addRow(grid, 6, "Дата заказа", orderDatePicker);
        addRow(grid, 7, "Дата доставки", deliveryDatePicker);
        setCenter(grid);

        Button saveButton = new Button("Сохранить");
        Button cancelButton = new Button("Отмена");

        saveButton.setOnAction(event -> {
            try {
                Order order = sourceOrder == null ? new Order() : sourceOrder;
                order.setId(Integer.parseInt(idField.getText()));
                order.setPickupPoint(required(pickupPointBox.getValue(), "Выберите пункт выдачи."));
                order.setClientFullName(required(clientField.getText(), "Введите ФИО клиента."));
                order.setPickupCode(parseInt(codeField.getText(), "Введите корректный код получения."));
                order.setStatus(required(statusBox.getValue(), "Выберите статус заказа."));
                order.setOrderDate(required(orderDatePicker.getValue(), "Укажите дату заказа."));
                order.setDeliveryDate(required(deliveryDatePicker.getValue(), "Укажите дату доставки."));
                order.replaceItems(parseItems(itemsArea.getText(), productRepository));

                orderRepository.save(order);
                AlertUtil.showInfo("Успешно", "Заказ сохранен.");
                onSave.run();
                stage.close();
            } catch (Exception exception) {
                AlertUtil.showError("Ошибка сохранения", exception.getMessage());
            }
        });

        cancelButton.setOnAction(event -> stage.close());
        HBox buttons = new HBox(10, saveButton, cancelButton);
        buttons.setAlignment(Pos.CENTER_RIGHT);
        buttons.setPadding(new Insets(16, 0, 0, 0));
        setBottom(buttons);
    }

    private void addRow(GridPane grid, int row, String labelText, javafx.scene.Node node) {
        Label label = new Label(labelText);
        label.setMinWidth(170);
        grid.add(label, 0, row);
        grid.add(node, 1, row);
        if (node instanceof TextField textField) {
            textField.setPrefWidth(320);
        } else if (node instanceof ComboBox<?> comboBox) {
            comboBox.setPrefWidth(320);
        } else if (node instanceof TextArea textArea) {
            textArea.setPrefWidth(320);
        }
    }

    private List<OrderItem> parseItems(String rawValue, ProductRepository productRepository) {
        String value = required(rawValue, "Заполните состав заказа.");
        String[] parts = value.split(",");
        if (parts.length < 2 || parts.length % 2 != 0) {
            throw new IllegalArgumentException("Состав заказа должен быть в формате: АРТИКУЛ, количество, АРТИКУЛ, количество.");
        }

        List<OrderItem> items = new ArrayList<>();
        for (int index = 0; index < parts.length; index += 2) {
            String article = parts[index].trim();
            int quantity = parseInt(parts[index + 1].trim(), "Количество товара в заказе должно быть положительным числом.");
            Product product = productRepository.findByArticle(article)
                .orElseThrow(() -> new IllegalArgumentException("Товар с артикулом " + article + " не найден."));

            OrderItem item = new OrderItem();
            item.setProduct(product);
            item.setQuantity(quantity);
            items.add(item);
        }
        return items;
    }

    private String required(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(message);
        }
        return value.trim();
    }

    private LookupValue required(LookupValue value, String message) {
        if (value == null) {
            throw new IllegalArgumentException(message);
        }
        return value;
    }

    private LocalDate required(LocalDate value, String message) {
        if (value == null) {
            throw new IllegalArgumentException(message);
        }
        return value;
    }

    private int parseInt(String value, String message) {
        try {
            int parsed = Integer.parseInt(value.trim());
            if (parsed <= 0) {
                throw new IllegalArgumentException(message);
            }
            return parsed;
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException(message);
        }
    }
}
