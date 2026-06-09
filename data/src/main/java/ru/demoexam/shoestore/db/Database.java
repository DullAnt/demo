package ru.demoexam.shoestore.db;

import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class Database {
    private final Path appDirectory;
    private final Path dataDirectory;
    private final Path imageDirectory;
    private final String jdbcUrl;
    private final Properties properties;

    public Database() {
        this.appDirectory = Path.of(System.getProperty("user.dir"));
        this.dataDirectory = appDirectory.resolve("data");
        this.imageDirectory = dataDirectory.resolve("images");
        this.jdbcUrl = buildJdbcUrl();
        this.properties = buildProperties();

        try {
            Files.createDirectories(dataDirectory);
            Files.createDirectories(imageDirectory);
        } catch (Exception exception) {
            throw new IllegalStateException("Не удалось создать директории приложения.", exception);
        }
    }

    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(jdbcUrl, properties);
    }

    public Path getImageDirectory() {
        return imageDirectory;
    }

    private String buildJdbcUrl() {
        String host = readConfig("PGHOST", "localhost");
        String port = readConfig("PGPORT", "5432");
        String databaseName = readConfig("PGDATABASE", "building_materials_store");
        return "jdbc:postgresql://" + host + ":" + port + "/" + databaseName;
    }

    private Properties buildProperties() {
        Properties config = new Properties();
        config.setProperty("user", readConfig("PGUSER", "postgres"));
        config.setProperty("password", readConfig("PGPASSWORD", "postgres"));
        return config;
    }

    private String readConfig(String key, String defaultValue) {
        String environmentValue = System.getenv(key);
        if (environmentValue != null && !environmentValue.isBlank()) {
            return environmentValue;
        }

        String systemValue = System.getProperty(key);
        if (systemValue != null && !systemValue.isBlank()) {
            return systemValue;
        }

        return defaultValue;
    }
}
