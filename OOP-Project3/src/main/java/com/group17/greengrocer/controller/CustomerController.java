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

    @FXML
    private ComboBox<String> sortComboBox;

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

        // Setup sorting ComboBox
        sortComboBox.getItems().addAll("Name (A-Z)", "Name (Z-A)", "Price (Low-High)", "Price (High-Low)");
        sortComboBox.setValue("Name (A-Z)");
        sortComboBox.setOnAction(e -> handleSortChange());

        // Load products grouped by type
        loadProductsByType();

        // Setup auto-refresh every 5 seconds
        setupAutoRefresh();

        // Disable close button (X) - user must use logout button
        // Use Platform.runLater because scene is not yet attached during initialize()
        javafx.application.Platform.runLater(() -> {
            try {
                Stage stage = (Stage) welcomeLabel.getScene().getWindow();
                if (stage != null) {
                    stage.setOnCloseRequest(e -> {
                        e.consume(); // Prevent window from closing - user must use logout button
                    });
                }
            } catch (Exception e) {
                System.err.println("Warning: Could not set close request handler: " + e.getMessage());
            }
        });
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
                }));
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
        // Sort products based on selected sort option
        List<Product> sortedProducts = sortProducts(new ArrayList<>(products));

        VBox content = new VBox(10);
        content.setPadding(new javafx.geometry.Insets(10));

        for (Product product : sortedProducts) {
            HBox productRow = createProductRow(product);
            content.getChildren().add(productRow);
        }

        TitledPane titledPane = new TitledPane(type, content);
        titledPane.setExpanded(true);
        return titledPane;
    }

    /**
     * Sort products based on selected sort option
     */
    private List<Product> sortProducts(List<Product> products) {
        String sortOption = sortComboBox.getValue();
        if (sortOption == null) {
            sortOption = "Name (A-Z)";
        }

        switch (sortOption) {
            case "Name (A-Z)":
                products.sort((p1, p2) -> p1.getProductName().compareToIgnoreCase(p2.getProductName()));
                break;
            case "Name (Z-A)":
                products.sort((p1, p2) -> p2.getProductName().compareToIgnoreCase(p1.getProductName()));
                break;
            case "Price (Low-High)":
                products.sort((p1, p2) -> {
                    BigDecimal price1 = productService.getDisplayPrice(p1);
                    BigDecimal price2 = productService.getDisplayPrice(p2);
                    return price1.compareTo(price2);
                });
                break;
            case "Price (High-Low)":
                products.sort((p1, p2) -> {
                    BigDecimal price1 = productService.getDisplayPrice(p1);
                    BigDecimal price2 = productService.getDisplayPrice(p2);
                    return price2.compareTo(price1);
                });
                break;
        }
        return products;
    }

    /**
     * Handle sort change
     */
    @FXML
    private void handleSortChange() {
        if (searchField.getText().trim().isEmpty()) {
            loadProductsByType();
        } else {
            handleSearch();
        }
    }

    /**
     * Create a row for a product with add to cart functionality
     */
    /**
     * Create a row for a product with add to cart functionality - Amazon Style
     */
    private HBox createProductRow(Product product) {
        HBox row = new HBox(15);
        row.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        row.setStyle("-fx-background-color: white; -fx-padding: 15; -fx-background-radius: 8; " +
                "-fx-border-color: #e7e7e7; -fx-border-radius: 8; -fx-cursor: hand;");

        // Store product reference in row's userData for later refresh
        row.setUserData(product);

        // Hover effect
        row.setOnMouseEntered(
                e -> row.setStyle("-fx-background-color: white; -fx-padding: 15; -fx-background-radius: 8; " +
                        "-fx-border-color: #ff9900; -fx-border-radius: 8; -fx-cursor: hand; " +
                        "-fx-effect: dropshadow(gaussian, rgba(255,153,0,0.2), 15, 0, 0, 3);"));
        row.setOnMouseExited(
                e -> row.setStyle("-fx-background-color: white; -fx-padding: 15; -fx-background-radius: 8; " +
                        "-fx-border-color: #e7e7e7; -fx-border-radius: 8; -fx-cursor: hand;"));

        // Product image (if available - only URL)
        javafx.scene.image.ImageView imageView = new javafx.scene.image.ImageView();
        imageView.setFitWidth(80);
        imageView.setFitHeight(80);
        imageView.setPreserveRatio(true);
        imageView.setStyle("-fx-background-color: #f7f7f7; -fx-background-radius: 5;");

        if (product.getImageUrl() != null && !product.getImageUrl().isEmpty()) {
            try {
                javafx.scene.image.Image image = new javafx.scene.image.Image(product.getImageUrl(), true);
                imageView.setImage(image);
            } catch (Exception e) {
                System.err.println("Error loading image from URL: " + e.getMessage());
            }
        }

        // Product info container
        VBox infoBox = new VBox(5);
        infoBox.setPrefWidth(200);

        Label nameLabel = new Label(product.getProductName());
        nameLabel.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: #007185;");
        nameLabel.setWrapText(true);

        Label typeLabel = new Label(product.getProductType());
        typeLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #565959;");

        infoBox.getChildren().addAll(nameLabel, typeLabel);

        // Price section
        VBox priceBox = new VBox(2);
        priceBox.setPrefWidth(130);

        BigDecimal displayPrice = productService.getDisplayPrice(product);
        boolean isPriceDoubled = displayPrice.compareTo(product.getPricePerKg()) > 0;

        if (isPriceDoubled) {
            // Show original price with strikethrough
            Label originalPriceLabel = new Label("₺" + product.getPricePerKg() + "/kg");
            originalPriceLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #565959; -fx-strikethrough: true;");

            Label discountLabel = new Label("Limited Stock!");
            discountLabel.setStyle("-fx-font-size: 10px; -fx-text-fill: #cc0c39; -fx-font-weight: bold;");

            Label priceLabel = new Label("₺" + displayPrice + "/kg");
            priceLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #b12704;");
            priceLabel.setUserData("price");

            priceBox.getChildren().addAll(discountLabel, originalPriceLabel, priceLabel);
        } else {
            Label priceLabel = new Label("₺" + displayPrice + "/kg");
            priceLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #0f1111;");
            priceLabel.setUserData("price");
            priceBox.getChildren().add(priceLabel);
        }

        // Stock section
        VBox stockBox = new VBox(2);
        stockBox.setPrefWidth(100);

        Label stockLabel = new Label(product.getStock() + " kg");
        stockLabel.setUserData("stock");

        if (product.getStock().compareTo(product.getThreshold()) <= 0) {
            stockLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #b12704; -fx-font-weight: bold;");
            Label lowStockLabel = new Label("Low Stock!");
            lowStockLabel.setStyle("-fx-font-size: 10px; -fx-text-fill: #b12704;");
            stockBox.getChildren().addAll(lowStockLabel, stockLabel);
        } else {
            stockLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #067d62; -fx-font-weight: bold;");
            Label inStockLabel = new Label("In Stock");
            inStockLabel.setStyle("-fx-font-size: 10px; -fx-text-fill: #067d62;");
            stockBox.getChildren().addAll(inStockLabel, stockLabel);
        }

        // Quantity input
        VBox qtyBox = new VBox(3);
        Label qtyLabel = new Label("Qty (kg):");
        qtyLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #565959;");

        TextField quantityField = new TextField();
        quantityField.setPromptText("0.5");
        quantityField.setPrefWidth(70);
        quantityField
                .setStyle("-fx-padding: 8; -fx-background-radius: 4; -fx-border-radius: 4; -fx-border-color: #ddd;");

        qtyBox.getChildren().addAll(qtyLabel, quantityField);

        // Add to cart button - Amazon style
        Button addButton = new Button("🛒 Add");
        addButton.setStyle("-fx-background-color: linear-gradient(to bottom, #f7dfa5, #f0c14b); " +
                "-fx-text-fill: #111; -fx-font-weight: bold; -fx-padding: 10 20; " +
                "-fx-background-radius: 20; -fx-border-radius: 20; " +
                "-fx-border-color: #a88734 #9c7e31 #846a29; -fx-cursor: hand;");
        addButton.setOnMouseEntered(
                e -> addButton.setStyle("-fx-background-color: linear-gradient(to bottom, #f5d78e, #eeb933); " +
                        "-fx-text-fill: #111; -fx-font-weight: bold; -fx-padding: 10 20; " +
                        "-fx-background-radius: 20; -fx-border-radius: 20; " +
                        "-fx-border-color: #a88734 #9c7e31 #846a29; -fx-cursor: hand;"));
        addButton.setOnMouseExited(
                e -> addButton.setStyle("-fx-background-color: linear-gradient(to bottom, #f7dfa5, #f0c14b); " +
                        "-fx-text-fill: #111; -fx-font-weight: bold; -fx-padding: 10 20; " +
                        "-fx-background-radius: 20; -fx-border-radius: 20; " +
                        "-fx-border-color: #a88734 #9c7e31 #846a29; -fx-cursor: hand;"));
        addButton.setOnAction(e -> handleAddToCart(product, quantityField, row));

        // Add spacer
        javafx.scene.layout.Region spacer = new javafx.scene.layout.Region();
        HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);

        row.getChildren().addAll(imageView, infoBox, priceBox, stockBox, spacer, qtyBox, addButton);
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

        // Get display price based on threshold rule (stock <= threshold means doubled
        // price)
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
            cartStage.setMaximized(true);

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
        TextField emailField = new TextField(
                currentUser != null && currentUser.getEmail() != null ? currentUser.getEmail() : "");
        emailField.setPromptText("Email (optional)");
        TextField phoneField = new TextField(
                currentUser != null && currentUser.getPhone() != null ? currentUser.getPhone() : "");
        phoneField.setPromptText("Phone (optional, format: 5XXXXXXXXX)");
        TextField addressField = new TextField(
                currentUser != null && currentUser.getAddress() != null ? currentUser.getAddress() : "");
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
                new Label("* Required field"));

        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        javafx.scene.control.Button okButton = (javafx.scene.control.Button) dialog.getDialogPane()
                .lookupButton(ButtonType.OK);

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

            // Load CSS
            java.net.URL cssUrl = getClass().getResource("/css/styles.css");
            if (cssUrl != null) {
                scene.getStylesheets().add(cssUrl.toExternalForm());
            }

            Stage stage = (Stage) logoutButton.getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("Local Greengrocer - Login");

            // Set initial size to 960x540 as per project requirements
            stage.setWidth(960);
            stage.setHeight(540);
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
            TableColumn<com.group17.greengrocer.model.Order, java.math.BigDecimal> totalColumn = new TableColumn<>(
                    "Total");
            TableColumn<com.group17.greengrocer.model.Order, String> orderDateColumn = new TableColumn<>("Order Date");
            TableColumn<com.group17.greengrocer.model.Order, String> deliveryDateColumn = new TableColumn<>(
                    "Delivery Date");
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
            actionsColumn.setCellFactory(
                    param -> new javafx.scene.control.TableCell<com.group17.greengrocer.model.Order, Void>() {
                        private final javafx.scene.control.Button cancelButton = new javafx.scene.control.Button(
                                "Cancel");
                        private final javafx.scene.control.Button rateButton = new javafx.scene.control.Button("Rate");
                        private final javafx.scene.control.Button downloadInvoiceButton = new javafx.scene.control.Button(
                                "Invoice");
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
                                        (order.getCanCancelUntil() == null
                                                || order.getCanCancelUntil().isAfter(java.time.LocalDateTime.now())));
                                rateButton.setVisible(
                                        order.getStatus().equals("Delivered") && order.getCarrierId() != null);
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
                new Label("Comment:"), commentArea);

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
            showAlert(Alert.AlertType.WARNING, "File Not Found",
                    "Invoice file not found at: " + order.getInvoicePath());
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
                new Label("Message:"), messageArea);

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
