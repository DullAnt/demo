package ru.demoexam.shoestore.repository;

import ru.demoexam.shoestore.db.Database;
import ru.demoexam.shoestore.model.LookupValue;
import ru.demoexam.shoestore.model.Product;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ProductRepository {
    private final Database database;

    public ProductRepository(Database database) {
        this.database = database;
    }

    public List<Product> findAll() {
        try (Connection connection = database.getConnection();
             PreparedStatement statement = connection.prepareStatement("""
                 SELECT
                     p.article,
                     p.name,
                     p.description,
                     p.price,
                     p.unit,
                     p.stock_quantity,
                     p.discount_percent,
                     p.image_path,
                     c.id AS category_id,
                     c.name AS category_name,
                     m.id AS manufacturer_id,
                     m.name AS manufacturer_name,
                     s.id AS supplier_id,
                     s.name AS supplier_name
                 FROM products p
                 JOIN categories c ON c.id = p.category_id
                 JOIN manufacturers m ON m.id = p.manufacturer_id
                 JOIN suppliers s ON s.id = p.supplier_id
                 ORDER BY p.article
                 """);
             ResultSet resultSet = statement.executeQuery()) {
            List<Product> products = new ArrayList<>();
            while (resultSet.next()) {
                products.add(mapProduct(resultSet));
            }
            return products;
        } catch (Exception exception) {
            throw new IllegalStateException("Не удалось получить список товаров.", exception);
        }
    }

    public Optional<Product> findByArticle(String article) {
        try (Connection connection = database.getConnection();
             PreparedStatement statement = connection.prepareStatement("""
                 SELECT
                     p.article,
                     p.name,
                     p.description,
                     p.price,
                     p.unit,
                     p.stock_quantity,
                     p.discount_percent,
                     p.image_path,
                     c.id AS category_id,
                     c.name AS category_name,
                     m.id AS manufacturer_id,
                     m.name AS manufacturer_name,
                     s.id AS supplier_id,
                     s.name AS supplier_name
                 FROM products p
                 JOIN categories c ON c.id = p.category_id
                 JOIN manufacturers m ON m.id = p.manufacturer_id
                 JOIN suppliers s ON s.id = p.supplier_id
                 WHERE p.article = ?
                 """)) {
            statement.setString(1, article);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return Optional.of(mapProduct(resultSet));
                }
                return Optional.empty();
            }
        } catch (Exception exception) {
            throw new IllegalStateException("Не удалось получить товар по артикулу.", exception);
        }
    }

    public void save(Product product) {
        if (exists(product.getArticle())) {
            update(product);
        } else {
            insert(product);
        }
    }

    public boolean isUsedInOrders(String article) {
        try (Connection connection = database.getConnection();
             PreparedStatement statement = connection.prepareStatement("SELECT COUNT(*) FROM order_items WHERE product_article = ?")) {
            statement.setString(1, article);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() && resultSet.getInt(1) > 0;
            }
        } catch (Exception exception) {
            throw new IllegalStateException("Не удалось проверить использование товара в заказах.", exception);
        }
    }

    public void delete(String article) {
        try (Connection connection = database.getConnection();
             PreparedStatement statement = connection.prepareStatement("DELETE FROM products WHERE article = ?")) {
            statement.setString(1, article);
            statement.executeUpdate();
        } catch (Exception exception) {
            throw new IllegalStateException("Не удалось удалить товар.", exception);
        }
    }

    private boolean exists(String article) {
        try (Connection connection = database.getConnection();
             PreparedStatement statement = connection.prepareStatement("SELECT COUNT(*) FROM products WHERE article = ?")) {
            statement.setString(1, article);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() && resultSet.getInt(1) > 0;
            }
        } catch (Exception exception) {
            throw new IllegalStateException("Не удалось проверить существование товара.", exception);
        }
    }

    private void insert(Product product) {
        try (Connection connection = database.getConnection();
             PreparedStatement statement = connection.prepareStatement("""
                 INSERT INTO products(article, name, category_id, description, manufacturer_id, supplier_id, price, unit, stock_quantity, discount_percent, image_path)
                 VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                 """)) {
            fillInsertStatement(product, statement);
            statement.executeUpdate();
        } catch (Exception exception) {
            throw new IllegalStateException("Не удалось добавить товар.", exception);
        }
    }

    private void update(Product product) {
        try (Connection connection = database.getConnection();
             PreparedStatement statement = connection.prepareStatement("""
                 UPDATE products
                 SET name = ?, category_id = ?, description = ?, manufacturer_id = ?, supplier_id = ?, price = ?, unit = ?, stock_quantity = ?, discount_percent = ?, image_path = ?
                 WHERE article = ?
                 """)) {
            statement.setString(1, product.getName());
            statement.setInt(2, product.getCategory().id());
            statement.setString(3, product.getDescription());
            statement.setInt(4, product.getManufacturer().id());
            statement.setInt(5, product.getSupplier().id());
            statement.setDouble(6, product.getPrice());
            statement.setString(7, product.getUnit());
            statement.setInt(8, product.getStockQuantity());
            statement.setInt(9, product.getDiscountPercent());
            statement.setString(10, product.getImagePath());
            statement.setString(11, product.getArticle());
            statement.executeUpdate();
        } catch (Exception exception) {
            throw new IllegalStateException("Не удалось обновить товар.", exception);
        }
    }

    private void fillInsertStatement(Product product, PreparedStatement statement) throws Exception {
        statement.setString(1, product.getArticle());
        statement.setString(2, product.getName());
        statement.setInt(3, product.getCategory().id());
        statement.setString(4, product.getDescription());
        statement.setInt(5, product.getManufacturer().id());
        statement.setInt(6, product.getSupplier().id());
        statement.setDouble(7, product.getPrice());
        statement.setString(8, product.getUnit());
        statement.setInt(9, product.getStockQuantity());
        statement.setInt(10, product.getDiscountPercent());
        statement.setString(11, product.getImagePath());
    }

    private Product mapProduct(ResultSet resultSet) throws Exception {
        Product product = new Product();
        product.setArticle(resultSet.getString("article"));
        product.setName(resultSet.getString("name"));
        product.setDescription(resultSet.getString("description"));
        product.setPrice(resultSet.getDouble("price"));
        product.setUnit(resultSet.getString("unit"));
        product.setStockQuantity(resultSet.getInt("stock_quantity"));
        product.setDiscountPercent(resultSet.getInt("discount_percent"));
        product.setImagePath(resultSet.getString("image_path"));
        product.setCategory(new LookupValue(resultSet.getInt("category_id"), resultSet.getString("category_name")));
        product.setManufacturer(new LookupValue(resultSet.getInt("manufacturer_id"), resultSet.getString("manufacturer_name")));
        product.setSupplier(new LookupValue(resultSet.getInt("supplier_id"), resultSet.getString("supplier_name")));
        return product;
    }
}
