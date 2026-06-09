package ru.demoexam.shoestore.ui;

import javafx.scene.control.ListCell;
import ru.demoexam.shoestore.model.Product;

public class ProductListCell extends ListCell<Product> {
    @Override
    protected void updateItem(Product item, boolean empty) {
        super.updateItem(item, empty);
        if (empty || item == null) {
            setText(null);
        } else {
            setText(item.getArticle() + " - " + item.getName());
        }
    }
}
