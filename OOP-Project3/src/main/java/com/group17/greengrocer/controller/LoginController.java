package com.group17.greengrocer.controller;

import com.group17.greengrocer.model.User;
import com.group17.greengrocer.service.AuthService;
import com.group17.greengrocer.util.Validation;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * Controller for the login view.
 */
public class LoginController {
    @FXML
    private TextField usernameField;
    
    @FXML
    private PasswordField passwordField;
    
    @FXML
    private Label errorLabel;
    
    @FXML
    private javafx.scene.control.Button loginButton;
    
    @FXML
    private javafx.scene.control.Button registerButton;
    
    private AuthService authService;
    
    @FXML
    public void initialize() {
        authService = new AuthService();
    }
    
    @FXML
    private void handleLogin() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText();
        
        // Clear previous error
        errorLabel.setVisible(false);
        errorLabel.setText("");
        
        // Validate input
        if (!Validation.isNotEmpty(username) || !Validation.isNotEmpty(password)) {
            showError("Please enter both username and password.");
            return;
        }
        
        // Attempt login
        if (authService.login(username, password)) {
            // Navigate based on role
            String role = authService.getCurrentUser().getRole();
            try {
                navigateToRoleView(role);
            } catch (IOException e) {
                showError("Error loading dashboard: " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            showError("Invalid username or password.");
        }
    }
    
    /**
     * Navigate to the appropriate view based on user role
     */
    private void navigateToRoleView(String role) throws IOException {
        String fxmlFile;
        switch (role) {
            case "Customer":
                fxmlFile = "/fxml/CustomerView.fxml";
                break;
            case "Carrier":
                fxmlFile = "/fxml/CarrierView.fxml";
                break;
            case "Owner":
                fxmlFile = "/fxml/OwnerView.fxml";
                break;
            default:
                showError("Unknown user role: " + role);
                return;
        }
        
        FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFile));
        Parent root = loader.load();
        Scene scene = new Scene(root);
        scene.getStylesheets().add(getClass().getResource("/css/styles.css").toExternalForm());
        Stage stage = (Stage) loginButton.getScene().getWindow();
        stage.setScene(scene);
        stage.setTitle("Group17 GreenGrocer");
        // Set full screen - use both setMaximized and setFullScreen for better compatibility
        stage.setMaximized(true);
        stage.setWidth(javafx.stage.Screen.getPrimary().getVisualBounds().getWidth());
        stage.setHeight(javafx.stage.Screen.getPrimary().getVisualBounds().getHeight());
        stage.show();
    }
    
    /**
     * Handle register action
     */
    @FXML
    private void handleRegister() {
        Dialog<User> dialog = createRegistrationDialog();
        dialog.showAndWait().ifPresent(user -> {
            // User registration is handled in the dialog's result converter
        });
    }
    
    /**
     * Create registration dialog
     */
    private Dialog<User> createRegistrationDialog() {
        Dialog<User> dialog = new Dialog<>();
        dialog.setTitle("Register as Customer");
        
        // Set dialog size
        dialog.setResizable(true);
        dialog.getDialogPane().setPrefWidth(500);
        dialog.getDialogPane().setPrefHeight(600);
        
        TextField usernameField = new TextField();
        PasswordField passwordField = new PasswordField();
        TextField fullNameField = new TextField();
        TextField emailField = new TextField();
        TextField phoneField = new TextField();
        TextField addressField = new TextField();
        
        // Add input length restrictions
        usernameField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null && newValue.length() > 50) {
                usernameField.setText(oldValue);
            }
        });
        
        fullNameField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null && newValue.length() > 100) {
                fullNameField.setText(oldValue);
            }
        });
        
        emailField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null && newValue.length() > 100) {
                emailField.setText(oldValue);
            }
        });
        
        phoneField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null && newValue.length() > 10) {
                phoneField.setText(oldValue);
            }
            // Only allow digits
            if (newValue != null && !newValue.matches("^[0-9]*$")) {
                phoneField.setText(oldValue);
            }
        });
        
        // Create requirement labels with better formatting
        Label usernameReq = new Label("Requirements: 3-50 characters, letters, numbers, underscore only");
        usernameReq.setStyle("-fx-font-size: 10px; -fx-text-fill: #666;");
        usernameReq.setWrapText(true);
        
        Label passwordReq = new Label("Requirements: At least 8 characters, must contain uppercase, lowercase, and a digit");
        passwordReq.setStyle("-fx-font-size: 10px; -fx-text-fill: #666;");
        passwordReq.setWrapText(true);
        
        Label fullNameReq = new Label("Requirements: Letters and spaces only, no numbers");
        fullNameReq.setStyle("-fx-font-size: 10px; -fx-text-fill: #666;");
        fullNameReq.setWrapText(true);
        
        Label emailReq = new Label("Format: user@example.com (optional)");
        emailReq.setStyle("-fx-font-size: 10px; -fx-text-fill: #666;");
        emailReq.setWrapText(true);
        
        Label phoneReq = new Label("Format: 5XXXXXXXXX (10 digits, starts with 5) (optional)");
        phoneReq.setStyle("-fx-font-size: 10px; -fx-text-fill: #666;");
        phoneReq.setWrapText(true);
        
        VBox content = new VBox(8);
        content.setPadding(new Insets(20));
        
        // Username section
        Label usernameLabel = new Label("Username*:");
        usernameLabel.setStyle("-fx-font-weight: bold;");
        VBox usernameBox = new VBox(3);
        usernameBox.getChildren().addAll(usernameLabel, usernameField, usernameReq);
        
        // Password section
        Label passwordLabel = new Label("Password*:");
        passwordLabel.setStyle("-fx-font-weight: bold;");
        VBox passwordBox = new VBox(3);
        passwordBox.getChildren().addAll(passwordLabel, passwordField, passwordReq);
        
        // Full Name section
        Label fullNameLabel = new Label("Full Name*:");
        fullNameLabel.setStyle("-fx-font-weight: bold;");
        VBox fullNameBox = new VBox(3);
        fullNameBox.getChildren().addAll(fullNameLabel, fullNameField, fullNameReq);
        
        // Email section
        Label emailLabel = new Label("Email:");
        emailLabel.setStyle("-fx-font-weight: bold;");
        VBox emailBox = new VBox(3);
        emailBox.getChildren().addAll(emailLabel, emailField, emailReq);
        
        // Phone section
        Label phoneLabel = new Label("Phone:");
        phoneLabel.setStyle("-fx-font-weight: bold;");
        VBox phoneBox = new VBox(3);
        phoneBox.getChildren().addAll(phoneLabel, phoneField, phoneReq);
        
        // Address section
        Label addressLabel = new Label("Address:");
        addressLabel.setStyle("-fx-font-weight: bold;");
        VBox addressBox = new VBox(3);
        addressBox.getChildren().addAll(addressLabel, addressField);
        
        // Required fields note
        Label requiredNote = new Label("* Required fields");
        requiredNote.setStyle("-fx-font-weight: bold; -fx-text-fill: #e74c3c; -fx-padding: 10 0 0 0;");
        
        content.getChildren().addAll(
            usernameBox,
            passwordBox,
            fullNameBox,
            emailBox,
            phoneBox,
            addressBox,
            requiredNote
        );
        
        // Wrap content in ScrollPane to ensure everything is visible
        ScrollPane scrollPane = new ScrollPane(content);
        scrollPane.setFitToWidth(true);
        scrollPane.setPrefHeight(550);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        
        dialog.getDialogPane().setContent(scrollPane);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        
        javafx.scene.control.Button okButton = (javafx.scene.control.Button) dialog.getDialogPane().lookupButton(ButtonType.OK);
        
        okButton.addEventFilter(javafx.event.ActionEvent.ACTION, e -> {
            String username = usernameField.getText().trim();
            String password = passwordField.getText();
            String fullName = fullNameField.getText().trim();
            String email = emailField.getText().trim();
            String phone = phoneField.getText().trim();
            String address = addressField.getText().trim();
            
            // Validate required fields
            if (username.isEmpty() || password.isEmpty() || fullName.isEmpty()) {
                showAlert(Alert.AlertType.ERROR, "Missing Fields", 
                    "Please fill in all required fields (Username, Password, Full Name).");
                e.consume();
                return;
            }
            
            // Register user
            String result = authService.registerCustomer(username, password, fullName, 
                email.isEmpty() ? null : email, 
                phone.isEmpty() ? null : phone, 
                address.isEmpty() ? null : address);
            
            if (!"SUCCESS".equals(result)) {
                showAlert(Alert.AlertType.ERROR, "Registration Failed", result);
                e.consume();
            } else {
                showAlert(Alert.AlertType.INFORMATION, "Success", 
                    "Registration successful! You can now login with your credentials.");
            }
        });
        
        dialog.setResultConverter(buttonType -> {
            if (buttonType == ButtonType.OK) {
                // Registration is handled in event filter
                return new User(); // Return dummy user to close dialog
            }
            return null;
        });
        
        return dialog;
    }
    
    /**
     * Show alert dialog
     */
    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    /**
     * Show error message
     */
    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
    }
}

