package ru.demoexam.shoestore.ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import ru.demoexam.shoestore.model.LookupValue;
import ru.demoexam.shoestore.model.Product;
import ru.demoexam.shoestore.repository.LookupRepository;
import ru.demoexam.shoestore.repository.ProductRepository;
import ru.demoexam.shoestore.service.ImageStorageService;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;

public class ProductFormView extends BorderPane {
    private final ProductRepository productRepository;
    private final ImageStorageService imageStorageService;
    private String imagePath;

    public ProductFormView(AppNavigator navigator, Product sourceProduct, Runnable onSave, Stage stage) {
        this.productRepository = new ProductRepository(navigator.getDatabase());
        this.imageStorageService = new ImageStorageService(navigator.getDatabase());
        this.imagePath = sourceProduct == null ? null : sourceProduct.getImagePath();

        LookupRepository lookupRepository = new LookupRepository(navigator.getDatabase());

        setPadding(new Insets(18));

        Label header = new Label(sourceProduct == null ? "Добавление товара" : "Редактирование товара");
        header.setStyle("-fx-font-size: 22px; -fx-font-weight: bold;");
        setTop(header);
        BorderPane.setMargin(header, new Insets(0, 0, 14, 0));

        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(10);

        TextField articleField = new TextField();
        articleField.setEditable(sourceProduct == null);
        articleField.setPromptText("Например PMEZMH");

        TextField nameField = new TextField();
        ComboBox<LookupValue> categoryBox = new ComboBox<>();
        categoryBox.getItems().addAll(lookupRepository.findAll("categories"));
        TextArea descriptionArea = new TextArea();
        descriptionArea.setPrefRowCount(3);
        ComboBox<LookupValue> manufacturerBox = new ComboBox<>();
        manufacturerBox.getItems().addAll(lookupRepository.findAll("manufacturers"));
        ComboBox<LookupValue> supplierBox = new ComboBox<>();
        supplierBox.getItems().addAll(lookupRepository.findAll("suppliers"));
        TextField priceField = new TextField();
        TextField unitField = new TextField();
        TextField stockField = new TextField();
        TextField discountField = new TextField();

        ImageView imageView = new ImageView();
        imageView.setFitWidth(300);
        imageView.setFitHeight(200);
        imageView.setPreserveRatio(true);
        imageView.setStyle("-fx-border-color: #cdd8e6; -fx-background-color: #f3f7fb;");
        refreshImage(imageView, imagePath);

        Button chooseImageButton = new Button("Выбрать изображение");
        chooseImageButton.setOnAction(event -> {
            FileChooser chooser = new FileChooser();
            chooser.setTitle("Выберите изображение");
            chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Изображения", "*.png", "*.jpg", "*.jpeg"));
            File selectedFile = chooser.showOpenDialog(stage);
            if (selectedFile != null) {
                imagePath = imageStorageService.saveImage(selectedFile, imagePath);
                refreshImage(imageView, imagePath);
            }
        });

        fillIfEdit(sourceProduct, articleField, nameField, categoryBox, descriptionArea, manufacturerBox, supplierBox, priceField, unitField, stockField, discountField);

        int row = 0;
        addRow(grid, row++, "Артикул", articleField);
        addRow(grid, row++, "Наименование", nameField);
        addRow(grid, row++, "Категория", categoryBox);
        addRow(grid, row++, "Описание", descriptionArea);
        addRow(grid, row++, "Производитель", manufacturerBox);
        addRow(grid, row++, "Поставщик", supplierBox);
        addRow(grid, row++, "Цена", priceField);
        addRow(grid, row++, "Единица измерения", unitField);
        addRow(grid, row++, "Количество на складе", stockField);
        addRow(grid, row++, "Скидка, %", discountField);

        VBox imageBox = new VBox(8, new Label("Изображение товара"), imageView, chooseImageButton);
        imageBox.setAlignment(Pos.TOP_CENTER);

        setCenter(new HBox(20, grid, imageBox));

        Button saveButton = new Button("Сохранить");
        Button cancelButton = new Button("Отмена");

        saveButton.setOnAction(event -> {
            try {
                Product product = sourceProduct == null ? new Product() : sourceProduct;
                product.setArticle(required(articleField.getText(), "Введите артикул товара."));
                product.setName(required(nameField.getText(), "Введите наименование товара."));
                product.setCategory(required(categoryBox.getValue(), "Выберите категорию."));
                product.setDescription(required(descriptionArea.getText(), "Введите описание товара."));
                product.setManufacturer(required(manufacturerBox.getValue(), "Выберите производителя."));
                product.setSupplier(required(supplierBox.getValue(), "Выберите поставщика."));
                product.setPrice(parsePrice(priceField.getText()));
                product.setUnit(required(unitField.getText(), "Введите единицу измерения."));
                product.setStockQuantity(parseInt(stockField.getText(), "Количество на складе должно быть целым и неотрицательным."));
                product.setDiscountPercent(parseInt(discountField.getText(), "Скидка должна быть целым и неотрицательным числом."));
                product.setImagePath(imagePath);

                productRepository.save(product);
                AlertUtil.showInfo("Успешно", "Товар сохранен.");
                onSave.run();
                stage.close();
            } catch (Exception exception) {
                AlertUtil.showError("Ошибка сохранения", exception.getMessage());
            }
        });

        cancelButton.setOnAction(event -> stage.close());
        HBox buttons = new HBox(10, saveButton, cancelButton);
        buttons.setAlignment(Pos.CENTER_RIGHT);
        buttons.setPadding(new Insets(15, 0, 0, 0));
        setBottom(buttons);
    }

    private void addRow(GridPane grid, int row, String labelText, javafx.scene.Node node) {
        Label label = new Label(labelText);
        label.setMinWidth(170);
        grid.add(label, 0, row);
        grid.add(node, 1, row);
        if (node instanceof TextField textField) {
            textField.setPrefWidth(260);
        } else if (node instanceof ComboBox<?> comboBox) {
            comboBox.setPrefWidth(260);
        } else if (node instanceof TextArea textArea) {
            textArea.setPrefWidth(260);
        }
    }

    private void fillIfEdit(
        Product product,
        TextField articleField,
        TextField nameField,
        ComboBox<LookupValue> categoryBox,
        TextArea descriptionArea,
        ComboBox<LookupValue> manufacturerBox,
        ComboBox<LookupValue> supplierBox,
        TextField priceField,
        TextField unitField,
        TextField stockField,
        TextField discountField
    ) {
        if (product == null) {
            return;
        }
        articleField.setText(product.getArticle());
        nameField.setText(product.getName());
        categoryBox.setValue(product.getCategory());
        descriptionArea.setText(product.getDescription());
        manufacturerBox.setValue(product.getManufacturer());
        supplierBox.setValue(product.getSupplier());
        priceField.setText(String.valueOf(product.getPrice()));
        unitField.setText(product.getUnit());
        stockField.setText(String.valueOf(product.getStockQuantity()));
        discountField.setText(String.valueOf(product.getDiscountPercent()));
    }

    private void refreshImage(ImageView imageView, String path) {
        try {
            if (path != null && Files.exists(Path.of(path))) {
                imageView.setImage(new Image(Path.of(path).toUri().toString(), 300, 200, true, true));
            } else {
                imageView.setImage(null);
            }
        } catch (Exception exception) {
            imageView.setImage(null);
        }
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

    private double parsePrice(String value) {
        try {
            double parsed = Double.parseDouble(value.replace(',', '.'));
            if (parsed < 0) {
                throw new IllegalArgumentException("Цена не может быть отрицательной.");
            }
            return parsed;
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException("Введите корректную цену.");
        }
    }

    private int parseInt(String value, String message) {
        try {
            int parsed = Integer.parseInt(value.trim());
            if (parsed < 0) {
                throw new IllegalArgumentException(message);
            }
            return parsed;
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException(message);
        }
    }
}
