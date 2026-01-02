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
    
    @Override
    public void start(Stage primaryStage) throws Exception {
        // Test database connection and run migrations
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
        
        // Load login view
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
    
    @Override
    public void stop() throws Exception {
        // Close database connection on application exit
        DatabaseAdapter.getInstance().closeConnection();
        super.stop();
    }
    
    /**
     * Main method to launch the application
     */
    public static void main(String[] args) {
        launch(args);
    }
}






