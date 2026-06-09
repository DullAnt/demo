package ru.demoexam.shoestore;

import javafx.application.Application;
import javafx.stage.Stage;
import ru.demoexam.shoestore.db.Database;
import ru.demoexam.shoestore.db.SchemaInitializer;
import ru.demoexam.shoestore.ui.AppNavigator;

public class MainApp extends Application {

    @Override
    public void start(Stage stage) {
        Database database = new Database();
        SchemaInitializer initializer = new SchemaInitializer(database);
        initializer.initialize();

        AppNavigator navigator = new AppNavigator(stage, database);
        navigator.showLogin();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
