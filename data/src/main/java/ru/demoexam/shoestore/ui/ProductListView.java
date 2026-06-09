package ru.demoexam.shoestore.ui;

import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import ru.demoexam.shoestore.model.LookupValue;
import ru.demoexam.shoestore.model.Product;
import ru.demoexam.shoestore.model.Role;
import ru.demoexam.shoestore.repository.LookupRepository;
import ru.demoexam.shoestore.repository.ProductRepository;

import java.nio.file.Files;
import java.nio.file.Path;
import java.text.DecimalFormat;
import java.util.Locale;
import java.util.function.Predicate;

public class ProductListView extends BorderPane {
    private final AppNavigator navigator;
    private final ProductRepository productRepository;
    private final LookupRepository lookupRepository;
    private final TableView<Product> tableView = new TableView<>();
    private final TextField searchField = new TextField();
    private final ComboBox<String> sortBox = new ComboBox<>();
    private final ComboBox<String> manufacturerFilterBox = new ComboBox<>();

    public ProductListView(AppNavigator navigator) {
        this.navigator = navigator;
        this.productRepository = new ProductRepository(navigator.getDatabase());
        this.lookupRepository = new LookupRepository(navigator.getDatabase());

        setPadding(new Insets(16));
        setTop(buildHeader());
        setCenter(buildTable());

        searchField.textProperty().addListener((observable, oldValue, newValue) -> applyFilters());
        sortBox.valueProperty().addListener((observable, oldValue, newValue) -> applyFilters());
        manufacturerFilterBox.valueProperty().addListener((observable, oldValue, newValue) -> applyFilters());
        refreshTable();
    }

    private Node buildHeader() {
        Label title = new Label("Список товаров");
        title.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");

        Label userLabel = new Label("Пользователь: " + navigator.getSession().getCurrentDisplayName());
        userLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #44556b;");

        Button logoutButton = new Button("Выход");
        logoutButton.setOnAction(event -> {
            navigator.getSession().logout();
            navigator.showLogin();
        });

        HBox userBox = new HBox(10, userLabel, logoutButton);
        userBox.setAlignment(Pos.CENTER_RIGHT);

        BorderPane topLine = new BorderPane();
        topLine.setLeft(title);
        topLine.setRight(userBox);

        searchField.setPromptText("Поиск по товарам");

        sortBox.getItems().addAll(
            "Без сортировки",
            "Остаток по возрастанию",
            "Остаток по убыванию",
            "Цена по возрастанию",
            "Цена по убыванию",
            "Скидка по возрастанию",
            "Скидка по убыванию"
        );
        sortBox.setValue("Без сортировки");

        manufacturerFilterBox.getItems().add("Все производители");
        for (LookupValue manufacturer : lookupRepository.findAll("manufacturers")) {
            manufacturerFilterBox.getItems().add(manufacturer.name());
        }
        manufacturerFilterBox.setValue("Все производители");

        Button ordersButton = new Button("Заказы");
        ordersButton.setVisible(navigator.getSession().getCurrentRole() == Role.MANAGER
            || navigator.getSession().getCurrentRole() == Role.ADMIN);
        ordersButton.setOnAction(event -> navigator.showOrders());

        Button addButton = new Button("Добавить товар");
        addButton.setVisible(navigator.getSession().getCurrentRole() == Role.ADMIN);
        addButton.setOnAction(event -> navigator.openProductForm(null, this::refreshTable));

        HBox filterBox = new HBox(10, searchField, sortBox, manufacturerFilterBox, ordersButton, addButton);
        filterBox.setAlignment(Pos.CENTER_LEFT);
        filterBox.setPadding(new Insets(15, 0, 15, 0));

        if (navigator.getSession().getCurrentRole() == Role.GUEST
            || navigator.getSession().getCurrentRole() == Role.CLIENT) {
            searchField.setDisable(true);
            sortBox.setDisable(true);
            manufacturerFilterBox.setDisable(true);
        }

        return new VBox(8, topLine, filterBox);
    }

    private Node buildTable() {
        TableColumn<Product, Product> imageColumn = new TableColumn<>("Фото");
        imageColumn.setCellValueFactory(value -> new SimpleObjectProperty<>(value.getValue()));
        imageColumn.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(Product item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                    setText(null);
                    return;
                }
                if (item.getImagePath() != null && Files.exists(Path.of(item.getImagePath()))) {
                    ImageView imageView = new ImageView(new Image(Path.of(item.getImagePath()).toUri().toString(), 90, 60, true, true));
                    imageView.setFitWidth(90);
                    imageView.setFitHeight(60);
                    imageView.setPreserveRatio(true);
                    setGraphic(imageView);
                    setText(null);
                } else {
                    setGraphic(null);
                    setText("Нет фото");
                }
            }
        });

        TableColumn<Product, String> articleColumn = new TableColumn<>("Артикул");
        articleColumn.setCellValueFactory(value -> new SimpleStringProperty(value.getValue().getArticle()));

        TableColumn<Product, String> nameColumn = new TableColumn<>("Наименование");
        nameColumn.setCellValueFactory(value -> new SimpleStringProperty(value.getValue().getName()));

        TableColumn<Product, String> categoryColumn = new TableColumn<>("Категория");
        categoryColumn.setCellValueFactory(value -> new SimpleStringProperty(value.getValue().getCategory().name()));

        TableColumn<Product, String> descriptionColumn = new TableColumn<>("Описание");
        descriptionColumn.setCellValueFactory(value -> new SimpleStringProperty(value.getValue().getDescription()));

        TableColumn<Product, String> manufacturerColumn = new TableColumn<>("Производитель");
        manufacturerColumn.setCellValueFactory(value -> new SimpleStringProperty(value.getValue().getManufacturer().name()));

        TableColumn<Product, String> supplierColumn = new TableColumn<>("Поставщик");
        supplierColumn.setCellValueFactory(value -> new SimpleStringProperty(value.getValue().getSupplier().name()));

        TableColumn<Product, Number> priceColumn = new TableColumn<>("Цена");
        priceColumn.setCellValueFactory(value -> new SimpleObjectProperty<>(value.getValue().getPrice()));
        priceColumn.setCellFactory(column -> new TableCell<>() {
            private final DecimalFormat format = (DecimalFormat) DecimalFormat.getNumberInstance(new Locale("ru", "RU"));

            @Override
            protected void updateItem(Number item, boolean empty) {
                super.updateItem(item, empty);
                Product product = getTableRow() == null ? null : getTableRow().getItem();
                if (empty || product == null || item == null) {
                    setGraphic(null);
                    setText(null);
                    return;
                }

                if (product.getDiscountPercent() > 0) {
                    Label basePrice = new Label(format.format(product.getPrice()) + " ₽");
                    basePrice.setStyle("-fx-text-fill: red; -fx-strikethrough: true;");
                    Label finalPrice = new Label(format.format(product.getDiscountedPrice()) + " ₽");
                    setGraphic(new HBox(6, basePrice, finalPrice));
                    setText(null);
                } else {
                    setGraphic(null);
                    setText(format.format(item.doubleValue()) + " ₽");
                }
            }
        });

        TableColumn<Product, String> unitColumn = new TableColumn<>("Ед.");
        unitColumn.setCellValueFactory(value -> new SimpleStringProperty(value.getValue().getUnit()));

        TableColumn<Product, Number> stockColumn = new TableColumn<>("Остаток");
        stockColumn.setCellValueFactory(value -> new SimpleObjectProperty<>(value.getValue().getStockQuantity()));

        TableColumn<Product, Number> discountColumn = new TableColumn<>("Скидка");
        discountColumn.setCellValueFactory(value -> new SimpleObjectProperty<>(value.getValue().getDiscountPercent()));
        discountColumn.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(Number item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.intValue() + "%");
            }
        });

        tableView.getColumns().addAll(
            imageColumn, articleColumn, nameColumn, categoryColumn, descriptionColumn,
            manufacturerColumn, supplierColumn, priceColumn, unitColumn, stockColumn, discountColumn
        );
        tableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
        tableView.setRowFactory(table -> new TableRow<>() {
            @Override
            protected void updateItem(Product item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setStyle("");
                    return;
                }
                if (item.getStockQuantity() == 0) {
                    setStyle("-fx-background-color: #ccecff;");
                } else if (item.getDiscountPercent() > 12) {
                    setStyle("-fx-background-color: #F4A460;");
                } else {
                    setStyle("");
                }
            }
        });

        if (navigator.getSession().getCurrentRole() == Role.ADMIN) {
            tableView.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && tableView.getSelectionModel().getSelectedItem() != null) {
                    navigator.openProductForm(tableView.getSelectionModel().getSelectedItem(), this::refreshTable);
                }
            });
        }

        Button deleteButton = new Button("Удалить товар");
        deleteButton.setVisible(navigator.getSession().getCurrentRole() == Role.ADMIN);
        deleteButton.setOnAction(event -> deleteSelectedProduct());

        return new VBox(10, tableView, deleteButton);
    }

    private void refreshTable() {
        FilteredList<Product> filteredList = new FilteredList<>(FXCollections.observableArrayList(productRepository.findAll()));
        tableView.setUserData(filteredList);
        applyFilters();

        SortedList<Product> sortedList = new SortedList<>(filteredList);
        sortedList.comparatorProperty().bind(tableView.comparatorProperty());
        tableView.setItems(sortedList);
    }

    @SuppressWarnings("unchecked")
    private void applyFilters() {
        FilteredList<Product> filteredList = (FilteredList<Product>) tableView.getUserData();
        if (filteredList == null) {
            return;
        }

        String query = searchField.getText() == null ? "" : searchField.getText().trim().toLowerCase();
        String manufacturer = manufacturerFilterBox.getValue();

        Predicate<Product> predicate = product -> {
            boolean matchesManufacturer = manufacturer == null
                || manufacturer.equals("Все производители")
                || product.getManufacturer().name().equalsIgnoreCase(manufacturer);

            boolean matchesQuery = query.isBlank()
                || product.getArticle().toLowerCase().contains(query)
                || product.getName().toLowerCase().contains(query)
                || product.getCategory().name().toLowerCase().contains(query)
                || product.getDescription().toLowerCase().contains(query)
                || product.getManufacturer().name().toLowerCase().contains(query)
                || product.getSupplier().name().toLowerCase().contains(query)
                || product.getUnit().toLowerCase().contains(query);

            return matchesManufacturer && matchesQuery;
        };
        filteredList.setPredicate(predicate);

        tableView.getSortOrder().clear();
        switch (sortBox.getValue()) {
            case "Остаток по возрастанию" -> applySort(9, TableColumn.SortType.ASCENDING);
            case "Остаток по убыванию" -> applySort(9, TableColumn.SortType.DESCENDING);
            case "Цена по возрастанию" -> applySort(7, TableColumn.SortType.ASCENDING);
            case "Цена по убыванию" -> applySort(7, TableColumn.SortType.DESCENDING);
            case "Скидка по возрастанию" -> applySort(10, TableColumn.SortType.ASCENDING);
            case "Скидка по убыванию" -> applySort(10, TableColumn.SortType.DESCENDING);
            default -> {
            }
        }
    }

    private void applySort(int columnIndex, TableColumn.SortType sortType) {
        TableColumn<Product, ?> column = tableView.getColumns().get(columnIndex);
        column.setSortType(sortType);
        tableView.getSortOrder().add(column);
    }

    private void deleteSelectedProduct() {
        Product product = tableView.getSelectionModel().getSelectedItem();
        if (product == null) {
            AlertUtil.showWarning("Удаление товара", "Выберите товар для удаления.");
            return;
        }

        if (productRepository.isUsedInOrders(product.getArticle())) {
            AlertUtil.showWarning("Удаление запрещено", "Товар нельзя удалить, потому что он уже используется в заказе.");
            return;
        }

        productRepository.delete(product.getArticle());
        refreshTable();
        AlertUtil.showInfo("Удаление товара", "Товар удален.");
    }
}
