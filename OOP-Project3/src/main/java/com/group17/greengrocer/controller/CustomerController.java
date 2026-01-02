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
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
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
    private Button couponsButton;
    
    @FXML
    private VBox productsContainer;
    
    private ProductService productService;
    private AuthService authService;
    private com.group17.greengrocer.service.OrderService orderService;
    private com.group17.greengrocer.service.CouponService couponService;
    private Map<Integer, OrderItem> cart; // productId -> OrderItem
    private Timeline autoRefreshTimeline;
    private Map<String, Boolean> titledPaneExpandedStates; // Store expanded state for each type
    
    @FXML
    public void initialize() {
        productService = new ProductService();
        authService = new AuthService();
        orderService = new com.group17.greengrocer.service.OrderService();
        couponService = new com.group17.greengrocer.service.CouponService();
        cart = new HashMap<>();
        titledPaneExpandedStates = new HashMap<>();
        
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
        // Save current expanded states before clearing
        saveTitledPaneStates();
        
        productsContainer.getChildren().clear();
        
        List<String> types = productService.getProductTypes();
        
        for (String type : types) {
            // Get fresh products from database
            List<Product> products = productService.getProductsByType(type);
            if (!products.isEmpty()) {
                TitledPane titledPane = createProductTypePane(type, products);
                // Restore expanded state if it was saved
                if (titledPaneExpandedStates.containsKey(type)) {
                    titledPane.setExpanded(titledPaneExpandedStates.get(type));
                } else {
                    // Default: expanded for new types
                    titledPane.setExpanded(true);
                    titledPaneExpandedStates.put(type, true);
                }
                productsContainer.getChildren().add(titledPane);
            }
        }
    }
    
    /**
     * Save current expanded states of all TitledPanes
     */
    private void saveTitledPaneStates() {
        for (javafx.scene.Node node : productsContainer.getChildren()) {
            if (node instanceof TitledPane) {
                TitledPane pane = (TitledPane) node;
                titledPaneExpandedStates.put(pane.getText(), pane.isExpanded());
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
        
        // Create ImageView for product image
        ImageView imageView = new ImageView();
        imageView.setFitWidth(60);
        imageView.setFitHeight(60);
        imageView.setPreserveRatio(true);
        imageView.setSmooth(true);
        imageView.setCache(true);
        
        // Load product image if available
        // Priority: 1. BLOB from database, 2. File path, 3. URL
        if (product.getProductImage() != null && product.getProductImage().length > 0) {
            // Load from BLOB (database)
            try {
                java.io.ByteArrayInputStream bais = new java.io.ByteArrayInputStream(product.getProductImage());
                Image image = new Image(bais);
                imageView.setImage(image);
            } catch (Exception e) {
                System.err.println("Failed to load image from BLOB for product: " + product.getProductName() + " - " + e.getMessage());
            }
        } else if (product.getImagePath() != null && !product.getImagePath().trim().isEmpty()) {
            try {
                String imagePath = product.getImagePath();
                // Handle both file paths and URLs
                Image image;
                if (imagePath.startsWith("http://") || imagePath.startsWith("https://")) {
                    // Load from URL
                    image = new Image(imagePath);
                } else {
                    // Try as file path (relative or absolute)
                    java.io.File imageFile = new java.io.File(imagePath);
                    if (imageFile.exists()) {
                        image = new Image(imageFile.toURI().toString());
                    } else {
                        // Try as resource path (for images in resources folder)
                        try {
                            image = new Image(getClass().getResourceAsStream("/" + imagePath));
                        } catch (Exception ex) {
                            // If resource path fails, try without leading slash
                            try {
                                image = new Image(getClass().getResourceAsStream(imagePath));
                            } catch (Exception ex2) {
                                // If all fails, try direct file path again
                                imageFile = new java.io.File(imagePath);
                                if (imageFile.exists()) {
                                    image = new Image(imageFile.toURI().toString());
                                } else {
                                    throw new Exception("Image file not found: " + imagePath);
                                }
                            }
                        }
                    }
                }
                imageView.setImage(image);
            } catch (Exception e) {
                // If image loading fails, use placeholder or leave empty
                System.err.println("Failed to load image for product: " + product.getProductName() + " - " + e.getMessage());
                // Optionally set a placeholder image
                // imageView.setImage(new Image(getClass().getResourceAsStream("/images/placeholder.png")));
            }
        }
        
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
        
        row.getChildren().addAll(imageView, nameLabel, priceLabel, stockLabel, quantityField, addButton);
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
        
        // Save current expanded states before clearing (if not searching)
        if (searchTerm.isEmpty()) {
            saveTitledPaneStates();
        }
        
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
                // Restore expanded state for search results too
                if (titledPaneExpandedStates.containsKey(entry.getKey())) {
                    titledPane.setExpanded(titledPaneExpandedStates.get(entry.getKey()));
                } else {
                    titledPane.setExpanded(true);
                }
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
            Scene cartScene = new Scene(root, 960, 540);
            cartScene.getStylesheets().add(getClass().getResource("/css/styles.css").toExternalForm());
            cartStage.setScene(cartScene);
            cartStage.centerOnScreen();
            
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
            Scene scene = new Scene(root, 960, 540);
            scene.getStylesheets().add(getClass().getResource("/css/styles.css").toExternalForm());
            Stage stage = (Stage) logoutButton.getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("Login");
            stage.centerOnScreen();
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
     * Handle download invoice action - Downloads PDF from database (BLOB) and shares with customer
     */
    private void handleDownloadInvoice(com.group17.greengrocer.model.Order order) {
        // Priority: 1. PDF from database (BLOB), 2. File path (backup)
        byte[] invoicePdfBytes = order.getInvoiceContent();
        
        if (invoicePdfBytes != null && invoicePdfBytes.length > 0) {
            // Invoice PDF is stored in database (BLOB) - save to file and open
            try {
                // Create invoices directory if it doesn't exist
                java.io.File invoiceDir = new java.io.File("invoices");
                if (!invoiceDir.exists()) {
                    invoiceDir.mkdirs();
                }
                
                // Save PDF from database to file
                String invoiceFileName = "order_" + order.getOrderId() + "_invoice.pdf";
                java.io.File invoiceFile = new java.io.File(invoiceDir, invoiceFileName);
                
                try (java.io.FileOutputStream fos = new java.io.FileOutputStream(invoiceFile)) {
                    fos.write(invoicePdfBytes);
                }
                
                // Open PDF file for customer
                try {
                    java.awt.Desktop.getDesktop().open(invoiceFile);
                    showAlert(Alert.AlertType.INFORMATION, "Invoice Downloaded", 
                        "Invoice has been downloaded and opened.\nLocation: " + invoiceFile.getAbsolutePath());
                } catch (Exception e) {
                    showAlert(Alert.AlertType.INFORMATION, "Invoice Downloaded", 
                        "Invoice has been saved to:\n" + invoiceFile.getAbsolutePath());
                }
            } catch (Exception e) {
                System.err.println("Error saving invoice from database: " + e.getMessage());
                e.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to download invoice: " + e.getMessage());
            }
        } else if (order.getInvoicePath() != null && !order.getInvoicePath().isEmpty()) {
            // Fallback: Try file path (backup)
            java.io.File invoiceFile = new java.io.File(order.getInvoicePath());
            if (invoiceFile.exists()) {
                try {
                    java.awt.Desktop.getDesktop().open(invoiceFile);
                } catch (Exception e) {
                    showAlert(Alert.AlertType.INFORMATION, "Invoice", 
                        "Invoice location: " + invoiceFile.getAbsolutePath());
                }
            } else {
                showAlert(Alert.AlertType.WARNING, "Invoice Not Found", 
                    "Invoice PDF is not available in database or file system.");
            }
        } else {
            showAlert(Alert.AlertType.WARNING, "No Invoice", "Invoice not available for this order.");
        }
    }
    
    /**
     * Handle view coupons action - Show all customer coupons
     */
    @FXML
    private void handleViewCoupons() {
        int customerId = com.group17.greengrocer.util.Session.getInstance().getCurrentUserId();
        List<com.group17.greengrocer.model.Coupon> allCoupons = couponService.getCustomerCoupons(customerId);
        List<com.group17.greengrocer.model.Coupon> availableCoupons = couponService.getAvailableCustomerCoupons(customerId);
        
        // Create dialog to show coupons
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("My Coupons");
        dialog.setHeaderText("Your Coupons");
        
        // Create table to display coupons
        TableView<com.group17.greengrocer.model.Coupon> couponTable = new TableView<>();
        
        TableColumn<com.group17.greengrocer.model.Coupon, String> codeColumn = new TableColumn<>("Coupon Code");
        codeColumn.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("couponCode"));
        codeColumn.setPrefWidth(150);
        
        TableColumn<com.group17.greengrocer.model.Coupon, String> discountColumn = new TableColumn<>("Discount");
        discountColumn.setCellValueFactory(cellData -> {
            com.group17.greengrocer.model.Coupon coupon = cellData.getValue();
            String discount = "";
            if (coupon.getDiscountAmount() != null && coupon.getDiscountAmount().compareTo(BigDecimal.ZERO) > 0) {
                discount = "₺" + coupon.getDiscountAmount().setScale(2, java.math.RoundingMode.HALF_UP);
            } else if (coupon.getDiscountPercent() != null && coupon.getDiscountPercent().compareTo(BigDecimal.ZERO) > 0) {
                discount = coupon.getDiscountPercent() + "%";
            }
            return new javafx.beans.property.SimpleStringProperty(discount);
        });
        discountColumn.setPrefWidth(100);
        
        TableColumn<com.group17.greengrocer.model.Coupon, String> statusColumn = new TableColumn<>("Status");
        statusColumn.setCellValueFactory(cellData -> {
            com.group17.greengrocer.model.Coupon coupon = cellData.getValue();
            String status;
            if (coupon.isUsed()) {
                status = "Used";
            } else if (coupon.getExpiryDate() != null && coupon.getExpiryDate().isBefore(java.time.LocalDateTime.now())) {
                status = "Expired";
            } else {
                status = "Available";
            }
            return new javafx.beans.property.SimpleStringProperty(status);
        });
        statusColumn.setPrefWidth(100);
        
        TableColumn<com.group17.greengrocer.model.Coupon, String> expiryColumn = new TableColumn<>("Expiry Date");
        expiryColumn.setCellValueFactory(cellData -> {
            com.group17.greengrocer.model.Coupon coupon = cellData.getValue();
            String expiry = coupon.getExpiryDate() != null ? coupon.getExpiryDate().toString() : "No expiry";
            return new javafx.beans.property.SimpleStringProperty(expiry);
        });
        expiryColumn.setPrefWidth(180);
        
        couponTable.getColumns().addAll(codeColumn, discountColumn, statusColumn, expiryColumn);
        couponTable.setItems(javafx.collections.FXCollections.observableArrayList(allCoupons));
        couponTable.setPrefHeight(400);
        
        VBox content = new VBox(10);
        content.setPadding(new Insets(10));
        
        Label infoLabel = new Label("Available Coupons: " + availableCoupons.size() + " | Total Coupons: " + allCoupons.size());
        infoLabel.setStyle("-fx-font-weight: bold;");
        
        content.getChildren().addAll(infoLabel, couponTable);
        
        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        
        dialog.showAndWait();
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

