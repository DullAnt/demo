package ru.demoexam.shoestore.repository;

import ru.demoexam.shoestore.db.Database;
import ru.demoexam.shoestore.model.LookupValue;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class LookupRepository {
    private final Database database;

    public LookupRepository(Database database) {
        this.database = database;
    }

    public List<LookupValue> findAll(String tableName) {
        return findAll(tableName, "name");
    }

    public List<LookupValue> findAll(String tableName, String valueColumn) {
        try (Connection connection = database.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                 "SELECT id, " + valueColumn + " AS display_value FROM " + tableName + " ORDER BY " + valueColumn
             );
             ResultSet resultSet = statement.executeQuery()) {
            List<LookupValue> result = new ArrayList<>();
            while (resultSet.next()) {
                result.add(new LookupValue(resultSet.getInt("id"), resultSet.getString("display_value")));
            }
            return result;
        } catch (Exception exception) {
            throw new IllegalStateException("Не удалось получить справочные данные: " + tableName, exception);
        }
    }
}
