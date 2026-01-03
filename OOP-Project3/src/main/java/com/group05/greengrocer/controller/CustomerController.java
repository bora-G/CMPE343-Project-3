package com.group05.greengrocer.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.group05.greengrocer.model.OrderItem;
import com.group05.greengrocer.model.Product;
import com.group05.greengrocer.service.AuthService;
import com.group05.greengrocer.service.ProductService;

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
    private com.group05.greengrocer.service.OrderService orderService;
    private Map<Integer, OrderItem> cart; // productId -> OrderItem
    private Timeline autoRefreshTimeline;
    private static final Map<String, javafx.scene.image.Image> imageCache = new HashMap<>();

    /**
     * Initialize the controller.
     */
    @FXML
    public void initialize() {
        productService = new ProductService();
        authService = new AuthService();
        orderService = new com.group05.greengrocer.service.OrderService();
        cart = new HashMap<>();

        if (authService.getCurrentUser() != null) {
            usernameLabel.setText(authService.getCurrentUser().getUsername());
            welcomeLabel.setText("Welcome, " + authService.getCurrentUser().getFullName());
        }

        sortComboBox.getItems().addAll("Name (A-Z)", "Name (Z-A)", "Price (Low-High)", "Price (High-Low)");
        sortComboBox.setValue("Name (A-Z)");
        sortComboBox.setOnAction(e -> handleSortChange());

        loadProductsByType();

        setupAutoRefresh();
    }

    /**
     * Setup automatic refresh of product prices and stocks every 5 seconds.
     * This avoids recreating the entire UI, which improves performance.
     */
    private void setupAutoRefresh() {
        autoRefreshTimeline = new Timeline(
                new KeyFrame(Duration.seconds(5), e -> {
                    updateProductDataOnly();
                }));
        autoRefreshTimeline.setCycleCount(Timeline.INDEFINITE);
        autoRefreshTimeline.play();
    }

    /**
     * Update only the data (price and stock) in existing product rows.
     * This prevents the "heavy" feeling and flickering.
     */
    private void updateProductDataOnly() {
        List<Product> freshProducts = productService.getAvailableProducts();
        Map<Integer, Product> freshMap = new HashMap<>();
        for (Product p : freshProducts) {
            freshMap.put(p.getProductId(), p);
        }

        for (javafx.scene.Node titledNode : productsContainer.getChildren()) {
            if (titledNode instanceof TitledPane) {
                TitledPane tp = (TitledPane) titledNode;
                if (tp.getContent() instanceof VBox) {
                    VBox content = (VBox) tp.getContent();
                    for (javafx.scene.Node rowNode : content.getChildren()) {
                        if (rowNode instanceof HBox) {
                            HBox row = (HBox) rowNode;
                            if (row.getUserData() instanceof Product) {
                                Product p = (Product) row.getUserData();
                                Product fresh = freshMap.get(p.getProductId());
                                if (fresh != null) {
                                    updateRowLabels(p, fresh, row);
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Helper to update labels in a row without DB calls.
     */
    private void updateRowLabels(Product local, Product fresh, HBox row) {
        local.setStock(fresh.getStock());
        local.setThreshold(fresh.getThreshold());
        local.setPricePerKg(fresh.getPricePerKg());

        for (javafx.scene.Node node : row.getChildren()) {
            if (node instanceof Label) {
                Label label = (Label) node;
                Object userData = label.getUserData();
                if ("price".equals(userData)) {
                    BigDecimal displayPrice = productService.getDisplayPrice(local);
                    String priceText = "₺" + displayPrice + "/kg";
                    if (displayPrice.compareTo(local.getPricePerKg()) > 0) {
                        priceText += " (2x)";
                    }
                    label.setText(priceText);
                } else if ("stock".equals(userData)) {
                    label.setText("Stock: " + local.getStock() + " kg");
                }
            }
        }
    }

    /**
     * Handle refresh button action.
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
     * Load products grouped by type using TitledPane.
     */
    private void loadProductsByType() {
        productsContainer.getChildren().clear();

        List<String> types = productService.getProductTypes();

        for (String type : types) {
            List<Product> products = productService.getProductsByType(type);
            if (!products.isEmpty()) {
                TitledPane titledPane = createProductTypePane(type, products);
                productsContainer.getChildren().add(titledPane);
            }
        }
    }

    /**
     * Create a TitledPane for a product type.
     * 
     * @param type     The product type
     * @param products The list of products for this type
     * @return The TitledPane containing products
     */
    private TitledPane createProductTypePane(String type, List<Product> products) {
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
     * Sort products based on selected sort option.
     * 
     * @param products The list of products to sort
     * @return The sorted list of products
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
     * Handle sort change.
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
     * Create a row for a product with add to cart functionality.
     * 
     * @param product The product to create a row for
     * @return The HBox containing the product row
     */
    private HBox createProductRow(Product product) {
        HBox row = new HBox(10);
        row.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        row.setUserData(product);

        javafx.scene.image.ImageView imageView = null;
        try {
            javafx.scene.image.Image image = null;
            // 1. Try BLOB (Priority)
            if (product.getImageData() != null && product.getImageData().length > 0) {
                image = new javafx.scene.image.Image(new ByteArrayInputStream(product.getImageData()), 100, 100, true,
                        true);
            }
            // 2. Try URL (Fallback & Migration)
            else if (product.getImageUrl() != null && !product.getImageUrl().isEmpty()) {
                if (imageCache.containsKey(product.getImageUrl())) {
                    image = imageCache.get(product.getImageUrl());
                } else {
                    image = new javafx.scene.image.Image(product.getImageUrl(), 100, 100, true, true, true);
                    imageCache.put(product.getImageUrl(), image);

                    // Migration: Download and Save as BLOB (Background)
                    final String url = product.getImageUrl();
                    final Product p = product;
                    new Thread(() -> {
                        try {
                            java.net.URL imageUrl = new java.net.URL(url);
                            try (java.io.InputStream in = imageUrl.openStream()) {
                                byte[] bytes = in.readAllBytes(); // Java 9+
                                p.setImageData(bytes);
                                // Update DB
                                new com.group05.greengrocer.service.ProductService().updateProduct(p);
                                System.out.println("Migrated image to BLOB for: " + p.getProductName());
                            }
                        } catch (Exception ex) {
                            // Silent fail for migration
                        }
                    }).start();
                }
            }

            if (image != null) {
                imageView = new javafx.scene.image.ImageView(image);
                imageView.setFitWidth(100);
                imageView.setFitHeight(100);
                imageView.setPreserveRatio(true);
                imageView.setSmooth(true);
                imageView.setCache(true);
            }
        } catch (Exception e) {
            System.err.println("Error loading image: " + e.getMessage());
        }

        Label nameLabel = new Label(product.getProductName());
        nameLabel.setPrefWidth(200);

        BigDecimal displayPrice = productService.getDisplayPrice(product);
        String priceText = "₺" + displayPrice + "/kg";
        if (displayPrice.compareTo(product.getPricePerKg()) > 0) {
            priceText += " (2x)";
        }
        Label priceLabel = new Label(priceText);
        priceLabel.setPrefWidth(120);
        priceLabel.setUserData("price");

        Label stockLabel = new Label("Stock: " + product.getStock() + " kg");
        stockLabel.setPrefWidth(120);
        stockLabel.setUserData("stock");

        TextField quantityField = new TextField();
        quantityField.setPromptText("kg");
        quantityField.setPrefWidth(80);

        Button addButton = new Button("Add to Cart");
        addButton.setOnAction(e -> handleAddToCart(product, quantityField, row));

        if (imageView != null) {
            imageView.setStyle("-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.3), 5, 0, 0, 2);");
            row.getChildren().addAll(imageView, nameLabel, priceLabel, stockLabel, quantityField, addButton);
        } else {
            Label placeholderLabel = new Label("📦");
            placeholderLabel.setStyle("-fx-font-size: 30px; -fx-padding: 10px;");
            row.getChildren().addAll(placeholderLabel, nameLabel, priceLabel, stockLabel, quantityField, addButton);
        }
        return row;
    }

    /**
     * Handle add to cart action.
     * 
     * @param product       The product to add to cart
     * @param quantityField The quantity input field
     * @param productRow    The product row HBox for refreshing
     */
    @FXML
    private void handleAddToCart(Product product, TextField quantityField, HBox productRow) {
        String quantityStr = quantityField.getText().trim();

        if (!com.group05.greengrocer.util.Validation.isValidQuantity(quantityStr)) {
            showAlert(Alert.AlertType.ERROR, "Invalid Input", "Please enter a valid positive quantity.");
            return;
        }

        BigDecimal quantity = new BigDecimal(quantityStr);

        if (quantity.compareTo(product.getStock()) > 0) {
            showAlert(Alert.AlertType.ERROR, "Insufficient Stock",
                    "Available stock: " + product.getStock() + " kg");
            return;
        }

        BigDecimal displayPrice = productService.getDisplayPrice(product);

        if (cart.containsKey(product.getProductId())) {
            OrderItem existingItem = cart.get(product.getProductId());
            BigDecimal newQuantity = existingItem.getQuantity().add(quantity);
            existingItem.setQuantity(newQuantity);
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

        refreshProductRow(product, productRow);
    }

    /**
     * Refresh a single product row with updated price and stock.
     * 
     * @param product The product to refresh
     * @param row     The product row HBox to update
     */
    private void refreshProductRow(Product product, HBox row) {
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
     * Handle search action.
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
     * Handle view cart action.
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

            cartStage.setOnCloseRequest(e -> {
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
     * Handle edit profile action.
     */
    @FXML
    private void handleEditProfile() {
        Dialog<Void> dialog = createProfileEditDialog();
        dialog.showAndWait();
    }

    /**
     * Create profile edit dialog.
     * 
     * @return The profile edit dialog
     */
    private Dialog<Void> createProfileEditDialog() {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Edit Profile");

        com.group05.greengrocer.model.User currentUser = authService.getCurrentUser();

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

            if (fullName.isEmpty()) {
                showAlert(Alert.AlertType.ERROR, "Missing Field",
                        "Full name is required.");
                e.consume();
                return;
            }

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
                if (authService.getCurrentUser() != null) {
                    welcomeLabel.setText("Welcome, " + authService.getCurrentUser().getFullName());
                }
            }
        });

        dialog.setResultConverter(buttonType -> null);

        return dialog;
    }

    /**
     * Handle logout action.
     */
    @FXML
    private void handleLogout() {
        authService.logout();
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/LoginView.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root, 960, 540);
            Stage stage = (Stage) logoutButton.getScene().getWindow();
            stage.setFullScreen(false); // Exit fullscreen before changing scene
            stage.setScene(scene);
            stage.setTitle("Login");
            stage.setResizable(true);
            stage.centerOnScreen();
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Show alert dialog.
     * 
     * @param type    The alert type
     * @param title   The alert title
     * @param message The alert message
     */
    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Get cart items (for cart controller).
     * 
     * @return List of cart items
     */
    public List<OrderItem> getCartItems() {
        return new ArrayList<>(cart.values());
    }

    /**
     * Clear cart.
     */
    public void clearCart() {
        cart.clear();
    }

    /**
     * Handle view order history action.
     */
    @FXML
    private void handleViewOrderHistory() {
        try {
            List<com.group05.greengrocer.model.Order> orders = orderService.getCustomerOrders();

            Dialog<Void> dialog = new Dialog<>();
            dialog.setTitle("My Orders");
            dialog.setHeaderText("Order History");

            TableView<com.group05.greengrocer.model.Order> orderTable = new TableView<>();
            TableColumn<com.group05.greengrocer.model.Order, Integer> orderIdColumn = new TableColumn<>("Order ID");
            TableColumn<com.group05.greengrocer.model.Order, String> statusColumn = new TableColumn<>("Status");
            TableColumn<com.group05.greengrocer.model.Order, java.math.BigDecimal> totalColumn = new TableColumn<>(
                    "Total");
            TableColumn<com.group05.greengrocer.model.Order, String> orderDateColumn = new TableColumn<>("Order Date");
            TableColumn<com.group05.greengrocer.model.Order, String> deliveryDateColumn = new TableColumn<>(
                    "Delivery Date");
            TableColumn<com.group05.greengrocer.model.Order, Void> actionsColumn = new TableColumn<>("Actions");

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

            actionsColumn.setCellFactory(
                    param -> new javafx.scene.control.TableCell<com.group05.greengrocer.model.Order, Void>() {
                        private final javafx.scene.control.Button cancelButton = new javafx.scene.control.Button(
                                "Cancel");
                        private final javafx.scene.control.Button rateButton = new javafx.scene.control.Button("Rate");
                        private final javafx.scene.control.Button downloadInvoiceButton = new javafx.scene.control.Button(
                                "Invoice");
                        private final javafx.scene.layout.HBox buttonBox = new javafx.scene.layout.HBox(5);

                        {
                            cancelButton.setOnAction(event -> {
                                com.group05.greengrocer.model.Order order = getTableView().getItems().get(getIndex());
                                handleCancelOrder(order);
                            });
                            rateButton.setOnAction(event -> {
                                com.group05.greengrocer.model.Order order = getTableView().getItems().get(getIndex());
                                if (order.getStatus().equals("Delivered") && order.getCarrierId() != null) {
                                    handleRateCarrier(order);
                                }
                            });
                            downloadInvoiceButton.setOnAction(event -> {
                                com.group05.greengrocer.model.Order order = getTableView().getItems().get(getIndex());
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
                                com.group05.greengrocer.model.Order order = getTableView().getItems().get(getIndex());
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
     * Handle cancel order action.
     * 
     * @param order The order to cancel
     */
    private void handleCancelOrder(com.group05.greengrocer.model.Order order) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Cancel Order");
        confirm.setHeaderText("Cancel Order " + order.getOrderId());
        confirm.setContentText("Are you sure you want to cancel this order?");

        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                if (orderService.cancelOrderByCustomer(order.getOrderId())) {
                    showAlert(Alert.AlertType.INFORMATION, "Success", "Order cancelled successfully.");
                    handleViewOrderHistory();
                } else {
                    showAlert(Alert.AlertType.ERROR, "Error", "Failed to cancel order. It may be too late to cancel.");
                }
            }
        });
    }

    /**
     * Handle rate carrier action.
     * 
     * @param order The order to rate the carrier for
     */
    private void handleRateCarrier(com.group05.greengrocer.model.Order order) {
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
            com.group05.greengrocer.service.RatingService ratingService = new com.group05.greengrocer.service.RatingService();
            if (ratingService.rateCarrier(order.getOrderId(), order.getCarrierId(),
                    com.group05.greengrocer.util.Session.getInstance().getCurrentUserId(),
                    result.getKey(), result.getValue())) {
                showAlert(Alert.AlertType.INFORMATION, "Success", "Thank you for your rating!");
            } else {
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to submit rating.");
            }
        });
    }

    /**
     * Handle download invoice action.
     * 
     * @param order The order to download invoice for
     */
    private void handleDownloadInvoice(com.group05.greengrocer.model.Order order) {
        java.io.File invoiceFile = null;

        // 1. Try to find existing file from path
        if (order.getInvoicePath() != null && !order.getInvoicePath().isEmpty()) {
            invoiceFile = new java.io.File(order.getInvoicePath());
        }

        // 2. If file doesn't exist locally, try to recover
        if (invoiceFile == null || !invoiceFile.exists()) {
            try {
                // A. Check if PDF exists in DB (BLOB)
                byte[] pdfBytes = orderService.getInvoicePDF(order.getOrderId());

                // B. If not in DB, Regenerate it on the fly
                if (pdfBytes == null) {
                    // Ensure items are loaded
                    if (order.getItems() == null || order.getItems().isEmpty()) {
                        com.group05.greengrocer.model.Order fullOrder = orderService.getOrderById(order.getOrderId());
                        if (fullOrder != null) {
                            order.setItems(fullOrder.getItems());
                            // Sync other fields if necessary
                            order.setDeliveryAddress(fullOrder.getDeliveryAddress());
                            order.setOrderDate(fullOrder.getOrderDate());
                            order.setDeliveryDate(fullOrder.getDeliveryDate());
                        }
                    }
                    pdfBytes = orderService.generateInvoicePDF(order);

                    // Save to DB for future
                    if (pdfBytes != null) {
                        orderService.saveInvoicePDF(order.getOrderId(), pdfBytes);
                    }
                }

                // C. Write bytes to local file
                if (pdfBytes != null) {
                    String fileName = "invoices/order_" + order.getOrderId() + ".pdf";
                    java.io.File dir = new java.io.File("invoices");
                    if (!dir.exists())
                        dir.mkdirs();

                    try (java.io.FileOutputStream fos = new java.io.FileOutputStream(fileName)) {
                        fos.write(pdfBytes);
                    }

                    invoiceFile = new java.io.File(fileName);

                    // Update path in DB if it was missing
                    if (order.getInvoicePath() == null || order.getInvoicePath().isEmpty()) {
                        orderService.saveInvoicePath(order.getOrderId(), fileName);
                        order.setInvoicePath(fileName);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to recover invoice: " + e.getMessage());
                return;
            }
        }

        // 3. Inform the user (Do not auto-open to prevent crashes)
        if (invoiceFile != null && invoiceFile.exists()) {
            showAlert(Alert.AlertType.INFORMATION, "Invoice Saved",
                    "Invoice generated successfully.\nLocation: " + invoiceFile.getAbsolutePath());
        } else {
            showAlert(Alert.AlertType.WARNING, "Not Found", "Could not generate invoice file.");
        }
    }

    /**
     * Handle message owner action.
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
            com.group05.greengrocer.service.MessageService messageService = new com.group05.greengrocer.service.MessageService();
            if (messageService.sendMessageToOwner(
                    com.group05.greengrocer.util.Session.getInstance().getCurrentUserId(),
                    result.getKey(), result.getValue())) {
                showAlert(Alert.AlertType.INFORMATION, "Success", "Message sent successfully!");
            } else {
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to send message.");
            }
        });
    }
}
