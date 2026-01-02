package com.group17.greengrocer.controller;

import com.group17.greengrocer.model.OrderItem;
import com.group17.greengrocer.model.Product;
import com.group17.greengrocer.service.AuthService;
import com.group17.greengrocer.service.ProductService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.util.Duration;

/**
 * Controller for the customer view.
 */
public class CustomerController {
    @FXML
    private Label usernameLabel;
    
    @FXML
    private Label welcomeLabel;
    
    @FXML
    private TextField searchField;
    
    @FXML
    private Button searchButton;
    
    @FXML
    private Button profileButton;
    
    @FXML
    private Button refreshButton;
    
    @FXML
    private Button cartButton;
    
    @FXML
    private Button logoutButton;
    
    @FXML
    private Button orderHistoryButton;
    
    @FXML
    private Button messageOwnerButton;
    
    @FXML
    private VBox productsContainer;
    
    private ProductService productService;
    private AuthService authService;
    private com.group17.greengrocer.service.OrderService orderService;
    private Map<Integer, OrderItem> cart; // productId -> OrderItem
    private Timeline autoRefreshTimeline;
    
    @FXML
    public void initialize() {
        productService = new ProductService();
        authService = new AuthService();
        orderService = new com.group17.greengrocer.service.OrderService();
        cart = new HashMap<>();
        
        // Set username in top right corner
        if (authService.getCurrentUser() != null) {
            usernameLabel.setText(authService.getCurrentUser().getUsername());
            welcomeLabel.setText("Welcome, " + authService.getCurrentUser().getFullName());
        }
        
        // Load products grouped by type
        loadProductsByType();
        
        // Setup auto-refresh every 5 seconds
        setupAutoRefresh();
    }
    
    /**
     * Setup automatic refresh of product list every 5 seconds
     */
    private void setupAutoRefresh() {
        autoRefreshTimeline = new Timeline(
            new KeyFrame(Duration.seconds(5), e -> {
                // Only refresh if not searching
                if (searchField.getText().trim().isEmpty()) {
                    loadProductsByType();
                } else {
                    // If searching, refresh search results
                    handleSearch();
                }
            })
        );
        autoRefreshTimeline.setCycleCount(Timeline.INDEFINITE);
        autoRefreshTimeline.play();
    }
    
    /**
     * Handle refresh button action
     */
    @FXML
    private void handleRefresh() {
        if (searchField.getText().trim().isEmpty()) {
            loadProductsByType();
        } else {
            handleSearch();
        }
    }
    
    /**
     * Load products grouped by type using TitledPane
     */
    private void loadProductsByType() {
        productsContainer.getChildren().clear();
        
        List<String> types = productService.getProductTypes();
        
        for (String type : types) {
            // Get fresh products from database
            List<Product> products = productService.getProductsByType(type);
            if (!products.isEmpty()) {
                TitledPane titledPane = createProductTypePane(type, products);
                productsContainer.getChildren().add(titledPane);
            }
        }
    }
    
    /**
     * Create a TitledPane for a product type
     */
    private TitledPane createProductTypePane(String type, List<Product> products) {
        VBox content = new VBox(10);
        content.setPadding(new javafx.geometry.Insets(10));
        
        for (Product product : products) {
            HBox productRow = createProductRow(product);
            content.getChildren().add(productRow);
        }
        
        TitledPane titledPane = new TitledPane(type, content);
        titledPane.setExpanded(true);
        return titledPane;
    }
    
    /**
     * Create a row for a product with add to cart functionality
     */
    private HBox createProductRow(Product product) {
        HBox row = new HBox(10);
        row.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        
        // Store product reference in row's userData for later refresh
        row.setUserData(product);
        
        Label nameLabel = new Label(product.getProductName());
        nameLabel.setPrefWidth(200);
        
        // Get display price based on threshold rule
        BigDecimal displayPrice = productService.getDisplayPrice(product);
        String priceText = "₺" + displayPrice + "/kg";
        // If price is doubled, add indicator
        if (displayPrice.compareTo(product.getPricePerKg()) > 0) {
            priceText += " (2x)";
        }
        Label priceLabel = new Label(priceText);
        priceLabel.setPrefWidth(120);
        priceLabel.setUserData("price"); // Mark as price label
        
        Label stockLabel = new Label("Stock: " + product.getStock() + " kg");
        stockLabel.setPrefWidth(120);
        stockLabel.setUserData("stock"); // Mark as stock label
        
        TextField quantityField = new TextField();
        quantityField.setPromptText("kg");
        quantityField.setPrefWidth(80);
        
        Button addButton = new Button("Add to Cart");
        addButton.setOnAction(e -> handleAddToCart(product, quantityField, row));
        
        row.getChildren().addAll(nameLabel, priceLabel, stockLabel, quantityField, addButton);
        return row;
    }
    
    /**
     * Handle add to cart action
     */
    @FXML
    private void handleAddToCart(Product product, TextField quantityField, HBox productRow) {
        String quantityStr = quantityField.getText().trim();
        
        if (!com.group17.greengrocer.util.Validation.isValidQuantity(quantityStr)) {
            showAlert(Alert.AlertType.ERROR, "Invalid Input", "Please enter a valid positive quantity.");
            return;
        }
        
        BigDecimal quantity = new BigDecimal(quantityStr);
        
        // Check stock availability
        if (quantity.compareTo(product.getStock()) > 0) {
            showAlert(Alert.AlertType.ERROR, "Insufficient Stock", 
                "Available stock: " + product.getStock() + " kg");
            return;
        }
        
        // Get display price based on threshold rule (stock <= threshold means doubled price)
        BigDecimal displayPrice = productService.getDisplayPrice(product);
        
        // Add or merge with existing cart item
        if (cart.containsKey(product.getProductId())) {
            OrderItem existingItem = cart.get(product.getProductId());
            BigDecimal newQuantity = existingItem.getQuantity().add(quantity);
            existingItem.setQuantity(newQuantity);
            // Recalculate price based on current stock
            BigDecimal currentDisplayPrice = productService.getDisplayPrice(product);
            existingItem.setUnitPrice(currentDisplayPrice);
            existingItem.setSubtotal(currentDisplayPrice.multiply(newQuantity));
        } else {
            OrderItem item = new OrderItem();
            item.setProductId(product.getProductId());
            item.setQuantity(quantity);
            item.setUnitPrice(displayPrice);
            item.setSubtotal(displayPrice.multiply(quantity));
            item.setProduct(product);
            cart.put(product.getProductId(), item);
        }
        
        quantityField.clear();
        showAlert(Alert.AlertType.INFORMATION, "Success", "Product added to cart!");
        
        // Refresh the product row to update price and stock display
        refreshProductRow(product, productRow);
    }
    
    /**
     * Refresh a single product row with updated price and stock
     */
    private void refreshProductRow(Product product, HBox row) {
        // Get fresh product data from database
        Product freshProduct = productService.getProductById(product.getProductId());
        if (freshProduct == null) {
            return;
        }
        
        // Update product object
        product.setStock(freshProduct.getStock());
        product.setThreshold(freshProduct.getThreshold());
        product.setPricePerKg(freshProduct.getPricePerKg());
        
        // Find and update price and stock labels
        for (javafx.scene.Node node : row.getChildren()) {
            if (node instanceof Label) {
                Label label = (Label) node;
                Object userData = label.getUserData();
                
                if ("price".equals(userData)) {
                    // Update price label
                    BigDecimal displayPrice = productService.getDisplayPrice(product);
                    String priceText = "₺" + displayPrice + "/kg";
                    if (displayPrice.compareTo(product.getPricePerKg()) > 0) {
                        priceText += " (2x)";
                    }
                    label.setText(priceText);
                } else if ("stock".equals(userData)) {
                    // Update stock label
                    label.setText("Stock: " + product.getStock() + " kg");
                }
            }
        }
    }
    
    /**
     * Handle search action
     */
    @FXML
    private void handleSearch() {
        String searchTerm = searchField.getText().trim();
        
        productsContainer.getChildren().clear();
        
        if (searchTerm.isEmpty()) {
            loadProductsByType();
            return;
        }
        
        List<Product> products = productService.searchProducts(searchTerm);
        
        if (products.isEmpty()) {
            Label noResultsLabel = new Label("No products found.");
            productsContainer.getChildren().add(noResultsLabel);
        } else {
            // Group search results by type
            Map<String, List<Product>> groupedProducts = new HashMap<>();
            for (Product product : products) {
                groupedProducts.computeIfAbsent(product.getProductType(), k -> new ArrayList<>()).add(product);
            }
            
            for (Map.Entry<String, List<Product>> entry : groupedProducts.entrySet()) {
                TitledPane titledPane = createProductTypePane(entry.getKey(), entry.getValue());
                productsContainer.getChildren().add(titledPane);
            }
        }
    }
    
    /**
     * Handle view cart action
     */
    @FXML
    private void handleViewCart() {
        if (cart.isEmpty()) {
            showAlert(Alert.AlertType.INFORMATION, "Cart Empty", "Your cart is empty.");
            return;
        }
        
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/CartView.fxml"));
            Parent root = loader.load();
            CartController cartController = loader.getController();
            cartController.setCartItems(new ArrayList<>(cart.values()));
            
            Stage cartStage = new Stage();
            cartStage.setTitle("Shopping Cart");
            cartStage.setScene(new Scene(root));
            
            // Refresh product list when cart window is closed
            cartStage.setOnCloseRequest(e -> {
                // Refresh product list to update prices and stock
                if (searchField.getText().trim().isEmpty()) {
                    loadProductsByType();
                } else {
                    handleSearch();
                }
            });
            
            cartStage.show();
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to open cart: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Handle edit profile action
     */
    @FXML
    private void handleEditProfile() {
        Dialog<Void> dialog = createProfileEditDialog();
        dialog.showAndWait();
    }
    
    /**
     * Create profile edit dialog
     */
    private Dialog<Void> createProfileEditDialog() {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Edit Profile");
        
        com.group17.greengrocer.model.User currentUser = authService.getCurrentUser();
        
        TextField fullNameField = new TextField(currentUser != null ? currentUser.getFullName() : "");
        fullNameField.setPromptText("Full Name (letters and spaces only)");
        TextField emailField = new TextField(currentUser != null && currentUser.getEmail() != null ? currentUser.getEmail() : "");
        emailField.setPromptText("Email (optional)");
        TextField phoneField = new TextField(currentUser != null && currentUser.getPhone() != null ? currentUser.getPhone() : "");
        phoneField.setPromptText("Phone (optional, format: 5XXXXXXXXX)");
        TextField addressField = new TextField(currentUser != null && currentUser.getAddress() != null ? currentUser.getAddress() : "");
        addressField.setPromptText("Address (optional)");
        
        // Add input length restrictions
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
        
        VBox content = new VBox(10);
        content.setPadding(new Insets(20));
        content.getChildren().addAll(
            new Label("Full Name*:"), fullNameField,
            new Label("Email:"), emailField,
            new Label("Phone:"), phoneField,
            new Label("Address:"), addressField,
            new Label("* Required field")
        );
        
        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        
        javafx.scene.control.Button okButton = (javafx.scene.control.Button) dialog.getDialogPane().lookupButton(ButtonType.OK);
        
        okButton.addEventFilter(javafx.event.ActionEvent.ACTION, e -> {
            String fullName = fullNameField.getText().trim();
            String email = emailField.getText().trim();
            String phone = phoneField.getText().trim();
            String address = addressField.getText().trim();
            
            // Validate required fields
            if (fullName.isEmpty()) {
                showAlert(Alert.AlertType.ERROR, "Missing Field", 
                    "Full name is required.");
                e.consume();
                return;
            }
            
            // Update profile
            String result = authService.updateProfile(fullName, 
                email.isEmpty() ? null : email, 
                phone.isEmpty() ? null : phone, 
                address.isEmpty() ? null : address);
            
            if (!"SUCCESS".equals(result)) {
                showAlert(Alert.AlertType.ERROR, "Update Failed", result);
                e.consume();
            } else {
                showAlert(Alert.AlertType.INFORMATION, "Success", 
                    "Profile updated successfully!");
                // Update welcome label
                if (authService.getCurrentUser() != null) {
                    welcomeLabel.setText("Welcome, " + authService.getCurrentUser().getFullName());
                }
            }
        });
        
        dialog.setResultConverter(buttonType -> null);
        
        return dialog;
    }
    
    /**
     * Handle logout action
     */
    @FXML
    private void handleLogout() {
        authService.logout();
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/LoginView.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root);
            Stage stage = (Stage) logoutButton.getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("Login");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
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
     * Get cart items (for cart controller)
     */
    public List<OrderItem> getCartItems() {
        return new ArrayList<>(cart.values());
    }
    
    /**
     * Clear cart
     */
    public void clearCart() {
        cart.clear();
    }
    
    /**
     * Handle view order history action
     */
    @FXML
    private void handleViewOrderHistory() {
        try {
            List<com.group17.greengrocer.model.Order> orders = orderService.getCustomerOrders();
            
            // Create order history dialog
            Dialog<Void> dialog = new Dialog<>();
            dialog.setTitle("My Orders");
            dialog.setHeaderText("Order History");
            
            TableView<com.group17.greengrocer.model.Order> orderTable = new TableView<>();
            TableColumn<com.group17.greengrocer.model.Order, Integer> orderIdColumn = new TableColumn<>("Order ID");
            TableColumn<com.group17.greengrocer.model.Order, String> statusColumn = new TableColumn<>("Status");
            TableColumn<com.group17.greengrocer.model.Order, java.math.BigDecimal> totalColumn = new TableColumn<>("Total");
            TableColumn<com.group17.greengrocer.model.Order, String> orderDateColumn = new TableColumn<>("Order Date");
            TableColumn<com.group17.greengrocer.model.Order, String> deliveryDateColumn = new TableColumn<>("Delivery Date");
            TableColumn<com.group17.greengrocer.model.Order, Void> actionsColumn = new TableColumn<>("Actions");
            
            orderIdColumn.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("orderId"));
            statusColumn.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("status"));
            totalColumn.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("totalCost"));
            orderDateColumn.setCellValueFactory(cellData -> {
                java.time.LocalDateTime date = cellData.getValue().getOrderDate();
                return new javafx.beans.property.SimpleStringProperty(date != null ? date.toString() : "");
            });
            deliveryDateColumn.setCellValueFactory(cellData -> {
                java.time.LocalDateTime date = cellData.getValue().getDeliveryDate();
                return new javafx.beans.property.SimpleStringProperty(date != null ? date.toString() : "");
            });
            
            // Actions column with Cancel and Rate buttons
            actionsColumn.setCellFactory(param -> new javafx.scene.control.TableCell<com.group17.greengrocer.model.Order, Void>() {
                private final javafx.scene.control.Button cancelButton = new javafx.scene.control.Button("Cancel");
                private final javafx.scene.control.Button rateButton = new javafx.scene.control.Button("Rate");
                private final javafx.scene.control.Button downloadInvoiceButton = new javafx.scene.control.Button("Invoice");
                private final javafx.scene.layout.HBox buttonBox = new javafx.scene.layout.HBox(5);
                
                {
                    cancelButton.setOnAction(event -> {
                        com.group17.greengrocer.model.Order order = getTableView().getItems().get(getIndex());
                        handleCancelOrder(order);
                    });
                    rateButton.setOnAction(event -> {
                        com.group17.greengrocer.model.Order order = getTableView().getItems().get(getIndex());
                        if (order.getStatus().equals("Delivered") && order.getCarrierId() != null) {
                            handleRateCarrier(order);
                        }
                    });
                    downloadInvoiceButton.setOnAction(event -> {
                        com.group17.greengrocer.model.Order order = getTableView().getItems().get(getIndex());
                        handleDownloadInvoice(order);
                    });
                    buttonBox.getChildren().addAll(cancelButton, rateButton, downloadInvoiceButton);
                }
                
                @Override
                protected void updateItem(Void item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty) {
                        setGraphic(null);
                    } else {
                        com.group17.greengrocer.model.Order order = getTableView().getItems().get(getIndex());
                        cancelButton.setVisible(order.getStatus().equals("Pending") && 
                            (order.getCanCancelUntil() == null || order.getCanCancelUntil().isAfter(java.time.LocalDateTime.now())));
                        rateButton.setVisible(order.getStatus().equals("Delivered") && order.getCarrierId() != null);
                        setGraphic(buttonBox);
                    }
                }
            });
            
            orderTable.getColumns().add(orderIdColumn);
            orderTable.getColumns().add(statusColumn);
            orderTable.getColumns().add(totalColumn);
            orderTable.getColumns().add(orderDateColumn);
            orderTable.getColumns().add(deliveryDateColumn);
            orderTable.getColumns().add(actionsColumn);
            orderTable.setItems(javafx.collections.FXCollections.observableArrayList(orders));
            
            VBox content = new VBox(10);
            content.setPadding(new javafx.geometry.Insets(20));
            content.getChildren().add(orderTable);
            
            dialog.getDialogPane().setContent(content);
            dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
            dialog.showAndWait();
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to load order history: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Handle cancel order action
     */
    private void handleCancelOrder(com.group17.greengrocer.model.Order order) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Cancel Order");
        confirm.setHeaderText("Cancel Order " + order.getOrderId());
        confirm.setContentText("Are you sure you want to cancel this order?");
        
        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                if (orderService.cancelOrderByCustomer(order.getOrderId())) {
                    showAlert(Alert.AlertType.INFORMATION, "Success", "Order cancelled successfully.");
                    handleViewOrderHistory(); // Refresh
                } else {
                    showAlert(Alert.AlertType.ERROR, "Error", "Failed to cancel order. It may be too late to cancel.");
                }
            }
        });
    }
    
    /**
     * Handle rate carrier action
     */
    private void handleRateCarrier(com.group17.greengrocer.model.Order order) {
        Dialog<javafx.util.Pair<Integer, String>> dialog = new Dialog<>();
        dialog.setTitle("Rate Carrier");
        dialog.setHeaderText("Rate your delivery experience");
        
        ComboBox<Integer> ratingComboBox = new ComboBox<>();
        for (int i = 1; i <= 5; i++) {
            ratingComboBox.getItems().add(i);
        }
        ratingComboBox.setValue(5);
        
        TextArea commentArea = new TextArea();
        commentArea.setPromptText("Optional comment...");
        commentArea.setPrefRowCount(3);
        
        VBox content = new VBox(10);
        content.setPadding(new javafx.geometry.Insets(20));
        content.getChildren().addAll(
            new Label("Rating (1-5):"), ratingComboBox,
            new Label("Comment:"), commentArea
        );
        
        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        
        dialog.setResultConverter(buttonType -> {
            if (buttonType == ButtonType.OK) {
                return new javafx.util.Pair<>(ratingComboBox.getValue(), commentArea.getText());
            }
            return null;
        });
        
        dialog.showAndWait().ifPresent(result -> {
            com.group17.greengrocer.service.RatingService ratingService = new com.group17.greengrocer.service.RatingService();
            if (ratingService.rateCarrier(order.getOrderId(), order.getCarrierId(), 
                com.group17.greengrocer.util.Session.getInstance().getCurrentUserId(), 
                result.getKey(), result.getValue())) {
                showAlert(Alert.AlertType.INFORMATION, "Success", "Thank you for your rating!");
            } else {
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to submit rating.");
            }
        });
    }
    
    /**
     * Handle download invoice action
     */
    private void handleDownloadInvoice(com.group17.greengrocer.model.Order order) {
        if (order.getInvoicePath() == null || order.getInvoicePath().isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "No Invoice", "Invoice not available for this order.");
            return;
        }
        
        java.io.File invoiceFile = new java.io.File(order.getInvoicePath());
        if (invoiceFile.exists()) {
            try {
                java.awt.Desktop.getDesktop().open(invoiceFile);
            } catch (Exception e) {
                showAlert(Alert.AlertType.INFORMATION, "Invoice", 
                    "Invoice location: " + invoiceFile.getAbsolutePath());
            }
        } else {
            showAlert(Alert.AlertType.WARNING, "File Not Found", "Invoice file not found at: " + order.getInvoicePath());
        }
    }
    
    /**
     * Handle message owner action
     */
    @FXML
    private void handleMessageOwner() {
        Dialog<javafx.util.Pair<String, String>> dialog = new Dialog<>();
        dialog.setTitle("Message Owner");
        dialog.setHeaderText("Send a message to the owner");
        
        TextField subjectField = new TextField();
        subjectField.setPromptText("Subject");
        
        TextArea messageArea = new TextArea();
        messageArea.setPromptText("Your message...");
        messageArea.setPrefRowCount(5);
        
        VBox content = new VBox(10);
        content.setPadding(new javafx.geometry.Insets(20));
        content.getChildren().addAll(
            new Label("Subject:"), subjectField,
            new Label("Message:"), messageArea
        );
        
        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        
        dialog.setResultConverter(buttonType -> {
            if (buttonType == ButtonType.OK) {
                return new javafx.util.Pair<>(subjectField.getText(), messageArea.getText());
            }
            return null;
        });
        
        dialog.showAndWait().ifPresent(result -> {
            com.group17.greengrocer.service.MessageService messageService = new com.group17.greengrocer.service.MessageService();
            if (messageService.sendMessageToOwner(
                com.group17.greengrocer.util.Session.getInstance().getCurrentUserId(),
                result.getKey(), result.getValue())) {
                showAlert(Alert.AlertType.INFORMATION, "Success", "Message sent successfully!");
            } else {
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to send message.");
            }
        });
    }
}

