package com.group17.greengrocer.app;

import com.group17.greengrocer.util.DatabaseAdapter;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Main application class for Local Greengrocer Project.
 * Entry point for the JavaFX application.
 */
public class Main extends Application {
    
    /**
     * Start method called by JavaFX application launcher.
     * Initializes database connection, runs migrations, and loads the login view.
     * @param primaryStage The primary stage for the application
     * @throws Exception if FXML loading or database connection fails
     */
    @Override
    public void start(Stage primaryStage) throws Exception {
        DatabaseAdapter dbAdapter = DatabaseAdapter.getInstance();
        if (!dbAdapter.testConnection()) {
            System.err.println("Warning: Database connection test failed. " +
                "Please ensure MySQL is running and the database is created.");
            System.err.println("You may need to:");
            System.err.println("1. Create the database: CREATE DATABASE greengrocer_db;");
            System.err.println("2. Run the schema.sql file to create tables and insert sample data");
            System.err.println("3. Update DatabaseAdapter.java with your MySQL credentials");
        } else {
            System.out.println("Database connection successful. Migrations completed if needed.");
        }
        
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/LoginView.fxml"));
        Parent root = loader.load();
        
        Scene scene = new Scene(root);
        scene.getStylesheets().add(getClass().getResource("/css/styles.css").toExternalForm());
        
        primaryStage.setTitle("Local Greengrocer - Login");
        primaryStage.setScene(scene);
        primaryStage.setMaximized(true);
        primaryStage.setResizable(true);
        primaryStage.show();
    }
    
    /**
     * Stop method called when application is closing.
     * Closes database connection on application exit.
     * @throws Exception if closing connection fails
     */
    @Override
    public void stop() throws Exception {
        DatabaseAdapter.getInstance().closeConnection();
        super.stop();
    }
    
    /**
     * Main method to launch the application.
     * @param args Command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }
}
