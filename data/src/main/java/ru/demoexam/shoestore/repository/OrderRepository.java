package ru.demoexam.shoestore.repository;

import ru.demoexam.shoestore.db.Database;
import ru.demoexam.shoestore.model.LookupValue;
import ru.demoexam.shoestore.model.Order;
import ru.demoexam.shoestore.model.OrderItem;
import ru.demoexam.shoestore.model.Product;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class OrderRepository {
    private final Database database;

    public OrderRepository(Database database) {
        this.database = database;
    }

    public List<Order> findAll() {
        try (Connection connection = database.getConnection();
             PreparedStatement statement = connection.prepareStatement("""
                 SELECT
                     o.id,
                     o.client_full_name,
                     o.pickup_code,
                     o.status,
                     o.order_date,
                     o.delivery_date,
                     pp.id AS pickup_point_id,
                     pp.address AS pickup_point_address,
                     oi.quantity,
                     p.article,
                     p.name AS product_name,
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
                 FROM orders o
                 JOIN pickup_points pp ON pp.id = o.pickup_point_id
                 LEFT JOIN order_items oi ON oi.order_id = o.id
                 LEFT JOIN products p ON p.article = oi.product_article
                 LEFT JOIN categories c ON c.id = p.category_id
                 LEFT JOIN manufacturers m ON m.id = p.manufacturer_id
                 LEFT JOIN suppliers s ON s.id = p.supplier_id
                 ORDER BY o.id, p.article
                 """);
             ResultSet resultSet = statement.executeQuery()) {
            Map<Integer, Order> orderMap = new LinkedHashMap<>();
            while (resultSet.next()) {
                int orderId = resultSet.getInt("id");
                Order order = orderMap.computeIfAbsent(orderId, ignored -> mapOrder(resultSet));
                if (resultSet.getString("article") != null) {
                    OrderItem item = new OrderItem();
                    item.setProduct(mapProduct(resultSet));
                    item.setQuantity(resultSet.getInt("quantity"));
                    order.getItems().add(item);
                }
            }
            return new ArrayList<>(orderMap.values());
        } catch (Exception exception) {
            throw new IllegalStateException("Не удалось получить список заказов.", exception);
        }
    }

    public int getNextId() {
        try (Connection connection = database.getConnection();
             PreparedStatement statement = connection.prepareStatement("SELECT COALESCE(MAX(id), 0) + 1 FROM orders");
             ResultSet resultSet = statement.executeQuery()) {
            return resultSet.next() ? resultSet.getInt(1) : 1;
        } catch (Exception exception) {
            throw new IllegalStateException("Не удалось вычислить новый номер заказа.", exception);
        }
    }

    public void save(Order order) {
        if (exists(order.getId())) {
            update(order);
        } else {
            insert(order);
        }
    }

    public void delete(int orderId) {
        try (Connection connection = database.getConnection();
             PreparedStatement deleteItems = connection.prepareStatement("DELETE FROM order_items WHERE order_id = ?");
             PreparedStatement deleteOrder = connection.prepareStatement("DELETE FROM orders WHERE id = ?")) {
            deleteItems.setInt(1, orderId);
            deleteItems.executeUpdate();
            deleteOrder.setInt(1, orderId);
            deleteOrder.executeUpdate();
        } catch (Exception exception) {
            throw new IllegalStateException("Не удалось удалить заказ.", exception);
        }
    }

    private void insert(Order order) {
        try (Connection connection = database.getConnection();
             PreparedStatement statement = connection.prepareStatement("""
                 INSERT INTO orders(id, pickup_point_id, client_full_name, pickup_code, status, order_date, delivery_date)
                 VALUES (?, ?, ?, ?, ?, ?, ?)
                 """)) {
            fillOrderStatement(order, statement, true);
            statement.executeUpdate();
            replaceItems(connection, order);
        } catch (Exception exception) {
            throw new IllegalStateException("Не удалось добавить заказ.", exception);
        }
    }

    private void update(Order order) {
        try (Connection connection = database.getConnection();
             PreparedStatement statement = connection.prepareStatement("""
                 UPDATE orders
                 SET pickup_point_id = ?, client_full_name = ?, pickup_code = ?, status = ?, order_date = ?, delivery_date = ?
                 WHERE id = ?
                 """)) {
            fillOrderStatement(order, statement, false);
            statement.executeUpdate();
            replaceItems(connection, order);
        } catch (Exception exception) {
            throw new IllegalStateException("Не удалось обновить заказ.", exception);
        }
    }

    private boolean exists(int orderId) {
        try (Connection connection = database.getConnection();
             PreparedStatement statement = connection.prepareStatement("SELECT COUNT(*) FROM orders WHERE id = ?")) {
            statement.setInt(1, orderId);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() && resultSet.getInt(1) > 0;
            }
        } catch (Exception exception) {
            throw new IllegalStateException("Не удалось проверить существование заказа.", exception);
        }
    }

    private void replaceItems(Connection connection, Order order) throws Exception {
        try (PreparedStatement deleteStatement = connection.prepareStatement("DELETE FROM order_items WHERE order_id = ?");
             PreparedStatement insertStatement = connection.prepareStatement("""
                 INSERT INTO order_items(order_id, product_article, quantity)
                 VALUES (?, ?, ?)
                 """)) {
            deleteStatement.setInt(1, order.getId());
            deleteStatement.executeUpdate();

            for (OrderItem item : order.getItems()) {
                insertStatement.setInt(1, order.getId());
                insertStatement.setString(2, item.getProduct().getArticle());
                insertStatement.setInt(3, item.getQuantity());
                insertStatement.addBatch();
            }
            insertStatement.executeBatch();
        }
    }

    private void fillOrderStatement(Order order, PreparedStatement statement, boolean includeId) throws Exception {
        int index = 1;
        if (includeId) {
            statement.setInt(index++, order.getId());
        }
        statement.setInt(index++, order.getPickupPoint().id());
        statement.setString(index++, order.getClientFullName());
        statement.setInt(index++, order.getPickupCode());
        statement.setString(index++, order.getStatus());
        statement.setString(index++, order.getOrderDate().toString());
        statement.setString(index++, order.getDeliveryDate().toString());
        if (!includeId) {
            statement.setInt(index, order.getId());
        }
    }

    private Order mapOrder(ResultSet resultSet) {
        try {
            Order order = new Order();
            order.setId(resultSet.getInt("id"));
            order.setPickupPoint(new LookupValue(resultSet.getInt("pickup_point_id"), resultSet.getString("pickup_point_address")));
            order.setClientFullName(resultSet.getString("client_full_name"));
            order.setPickupCode(resultSet.getInt("pickup_code"));
            order.setStatus(resultSet.getString("status"));
            order.setOrderDate(LocalDate.parse(resultSet.getString("order_date")));
            order.setDeliveryDate(LocalDate.parse(resultSet.getString("delivery_date")));
            return order;
        } catch (Exception exception) {
            throw new IllegalStateException("Не удалось прочитать заказ.", exception);
        }
    }

    private Product mapProduct(ResultSet resultSet) throws Exception {
        Product product = new Product();
        product.setArticle(resultSet.getString("article"));
        product.setName(resultSet.getString("product_name"));
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
