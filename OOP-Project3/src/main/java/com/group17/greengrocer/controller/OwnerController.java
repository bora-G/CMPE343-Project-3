package com.group17.greengrocer.controller;

import com.group17.greengrocer.model.Order;
import com.group17.greengrocer.model.Product;
import com.group17.greengrocer.model.User;
import com.group17.greengrocer.service.AuthService;
import com.group17.greengrocer.service.OwnerService;
import com.group17.greengrocer.service.ProductService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URL;
import java.util.Map;
import java.util.ResourceBundle;

/**
 * Controller for the owner view.
 */
public class OwnerController implements Initializable {
    @FXML
    private Label welcomeLabel;
    
    @FXML
    private Button logoutButton;
    
    @FXML
    private TabPane mainTabPane;
    
    // Products Tab
    @FXML
    private TableView<Product> productsTable;
    
    @FXML
    private TableColumn<Product, Integer> productIdColumn;
    
    @FXML
    private TableColumn<Product, String> productNameColumn;
    
    @FXML
    private TableColumn<Product, String> productTypeColumn;
    
    @FXML
    private TableColumn<Product, BigDecimal> priceColumn;
    
    @FXML
    private TableColumn<Product, BigDecimal> stockColumn;
    
    @FXML
    private TableColumn<Product, BigDecimal> thresholdColumn;
    
    @FXML
    private Button addProductButton;
    
    @FXML
    private Button updateProductButton;
    
    @FXML
    private Button deleteProductButton;
    
    // Carriers Tab
    @FXML
    private TableView<User> carriersTable;
    
    @FXML
    private TableColumn<User, Integer> carrierIdColumn;
    
    @FXML
    private TableColumn<User, String> carrierNameColumn;
    
    @FXML
    private TableColumn<User, String> carrierEmailColumn;
    
    @FXML
    private TableColumn<User, String> carrierPhoneColumn;
    
    @FXML
    private Button hireCarrierButton;
    
    @FXML
    private Button fireCarrierButton;
    
    // Reports Tab
    @FXML
    private VBox reportsContainer;
    
    @FXML
    private Button profitReportButton;
    
    @FXML
    private Button deliveredOrdersButton;
    
    @FXML
    private Button carrierPerformanceButton;
    
    // All Orders Tab
    @FXML
    private TableView<Order> allOrdersTable;
    @FXML
    private TableColumn<Order, Integer> allOrderIdColumn;
    @FXML
    private TableColumn<Order, String> allCustomerColumn;
    @FXML
    private TableColumn<Order, String> allCarrierColumn;
    @FXML
    private TableColumn<Order, String> allStatusColumn;
    @FXML
    private TableColumn<Order, Double> allTotalColumn;
    @FXML
    private TableColumn<Order, String> allOrderDateColumn;
    @FXML
    private TableColumn<Order, String> allDeliveryDateColumn;
    
    // Messages Tab
    @FXML
    private TableView<com.group17.greengrocer.model.Message> messagesTable;
    @FXML
    private TableColumn<com.group17.greengrocer.model.Message, Integer> messageIdColumn;
    @FXML
    private TableColumn<com.group17.greengrocer.model.Message, String> messageCustomerColumn;
    @FXML
    private TableColumn<com.group17.greengrocer.model.Message, String> messageSubjectColumn;
    @FXML
    private TableColumn<com.group17.greengrocer.model.Message, String> messageDateColumn;
    @FXML
    private TableColumn<com.group17.greengrocer.model.Message, Boolean> messageReadColumn;
    @FXML
    private TableColumn<com.group17.greengrocer.model.Message, Void> messageActionColumn;
    
    // Coupons & Loyalty Tab
    @FXML
    private Button createCouponButton;
    @FXML
    private Button adjustLoyaltyButton;
    @FXML
    private TableView<com.group17.greengrocer.model.Coupon> couponsTable;
    @FXML
    private TableColumn<com.group17.greengrocer.model.Coupon, Integer> couponIdColumn;
    @FXML
    private TableColumn<com.group17.greengrocer.model.Coupon, String> couponCustomerColumn;
    @FXML
    private TableColumn<com.group17.greengrocer.model.Coupon, String> couponCodeColumn;
    @FXML
    private TableColumn<com.group17.greengrocer.model.Coupon, Double> couponDiscountColumn;
    @FXML
    private TableColumn<com.group17.greengrocer.model.Coupon, Boolean> couponUsedColumn;
    @FXML
    private VBox loyaltyStandardsContainer;
    
    // Carrier Ratings Tab
    @FXML
    private TableView<com.group17.greengrocer.model.CarrierRating> ratingsTable;
    @FXML
    private TableColumn<com.group17.greengrocer.model.CarrierRating, Integer> ratingIdColumn;
    @FXML
    private TableColumn<com.group17.greengrocer.model.CarrierRating, String> ratingCarrierColumn;
    @FXML
    private TableColumn<com.group17.greengrocer.model.CarrierRating, String> ratingCustomerColumn;
    @FXML
    private TableColumn<com.group17.greengrocer.model.CarrierRating, Integer> ratingValueColumn;
    @FXML
    private TableColumn<com.group17.greengrocer.model.CarrierRating, String> ratingCommentColumn;
    @FXML
    private TableColumn<com.group17.greengrocer.model.CarrierRating, String> ratingDateColumn;
    
    private ProductService productService;
    private OwnerService ownerService;
    private AuthService authService;
    private com.group17.greengrocer.service.OrderService orderService;
    private com.group17.greengrocer.service.MessageService messageService;
    private com.group17.greengrocer.service.CouponService couponService;
    private com.group17.greengrocer.service.RatingService ratingService;
    
    private ObservableList<Product> products;
    private ObservableList<User> carriers;
    private ObservableList<Order> allOrders;
    private ObservableList<com.group17.greengrocer.model.Message> messages;
    private ObservableList<com.group17.greengrocer.model.Coupon> coupons;
    private ObservableList<com.group17.greengrocer.model.CarrierRating> ratings;
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        productService = new ProductService();
        ownerService = new OwnerService();
        authService = new AuthService();
        orderService = new com.group17.greengrocer.service.OrderService();
        messageService = new com.group17.greengrocer.service.MessageService();
        couponService = new com.group17.greengrocer.service.CouponService();
        ratingService = new com.group17.greengrocer.service.RatingService();
        
        products = FXCollections.observableArrayList();
        carriers = FXCollections.observableArrayList();
        allOrders = FXCollections.observableArrayList();
        messages = FXCollections.observableArrayList();
        coupons = FXCollections.observableArrayList();
        ratings = FXCollections.observableArrayList();
        
        // Set welcome message
        if (authService.getCurrentUser() != null) {
            welcomeLabel.setText("Welcome, " + authService.getCurrentUser().getFullName());
        }
        
        setupTables();
        loadData();
    }
    
    /**
     * Setup all tables
     */
    private void setupTables() {
        // Products Table
        productIdColumn.setCellValueFactory(new PropertyValueFactory<>("productId"));
        productNameColumn.setCellValueFactory(new PropertyValueFactory<>("productName"));
        productTypeColumn.setCellValueFactory(new PropertyValueFactory<>("productType"));
        priceColumn.setCellValueFactory(new PropertyValueFactory<>("pricePerKg"));
        stockColumn.setCellValueFactory(new PropertyValueFactory<>("stock"));
        thresholdColumn.setCellValueFactory(new PropertyValueFactory<>("threshold"));
        productsTable.setItems(products);
        
        // Carriers Table
        carrierIdColumn.setCellValueFactory(new PropertyValueFactory<>("userId"));
        carrierNameColumn.setCellValueFactory(new PropertyValueFactory<>("fullName"));
        carrierEmailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));
        carrierPhoneColumn.setCellValueFactory(new PropertyValueFactory<>("phone"));
        carriersTable.setItems(carriers);
        
        // All Orders Table
        setupAllOrdersTable();
        
        // Messages Table
        setupMessagesTable();
        
        // Coupons Table
        setupCouponsTable();
        
        // Ratings Table
        setupRatingsTable();
    }
    
    /**
     * Setup all orders table
     */
    private void setupAllOrdersTable() {
        allOrderIdColumn.setCellValueFactory(new PropertyValueFactory<>("orderId"));
        allCustomerColumn.setCellValueFactory(cellData -> {
            try {
                com.group17.greengrocer.model.User customer = ownerService.getUserById(cellData.getValue().getCustomerId());
                return new javafx.beans.property.SimpleStringProperty(
                    customer != null ? customer.getFullName() : "Unknown");
            } catch (Exception e) {
                return new javafx.beans.property.SimpleStringProperty("Unknown");
            }
        });
        allCarrierColumn.setCellValueFactory(cellData -> {
            try {
                if (cellData.getValue().getCarrierId() != null) {
                    com.group17.greengrocer.model.User carrier = ownerService.getUserById(cellData.getValue().getCarrierId());
                    return new javafx.beans.property.SimpleStringProperty(
                        carrier != null ? carrier.getFullName() : "Unknown");
                }
                return new javafx.beans.property.SimpleStringProperty("Not assigned");
            } catch (Exception e) {
                return new javafx.beans.property.SimpleStringProperty("Unknown");
            }
        });
        allStatusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        allTotalColumn.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleDoubleProperty(
                cellData.getValue().getTotalCost().doubleValue()).asObject());
        allOrderDateColumn.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleStringProperty(
                cellData.getValue().getOrderDate() != null ? 
                    cellData.getValue().getOrderDate().toString() : ""));
        allDeliveryDateColumn.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleStringProperty(
                cellData.getValue().getDeliveryDate() != null ? 
                    cellData.getValue().getDeliveryDate().toString() : ""));
        allOrdersTable.setItems(allOrders);
    }
    
    /**
     * Setup messages table
     */
    private void setupMessagesTable() {
        messageIdColumn.setCellValueFactory(new PropertyValueFactory<>("messageId"));
        messageCustomerColumn.setCellValueFactory(cellData -> {
            try {
                com.group17.greengrocer.model.User customer = ownerService.getUserById(cellData.getValue().getCustomerId());
                return new javafx.beans.property.SimpleStringProperty(
                    customer != null ? customer.getFullName() : "Unknown");
            } catch (Exception e) {
                return new javafx.beans.property.SimpleStringProperty("Unknown");
            }
        });
        messageSubjectColumn.setCellValueFactory(new PropertyValueFactory<>("subject"));
        messageDateColumn.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleStringProperty(
                cellData.getValue().getCreatedAt() != null ? 
                    cellData.getValue().getCreatedAt().toString() : ""));
        messageReadColumn.setCellValueFactory(new PropertyValueFactory<>("read"));
        
        messageActionColumn.setCellFactory(param -> new TableCell<com.group17.greengrocer.model.Message, Void>() {
            private final Button viewButton = new Button("View/Reply");
            
            {
                viewButton.setOnAction(event -> {
                    com.group17.greengrocer.model.Message message = getTableView().getItems().get(getIndex());
                    handleViewMessage(message);
                });
            }
            
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(viewButton);
                }
            }
        });
        
        messagesTable.setItems(messages);
    }
    
    /**
     * Setup coupons table
     */
    private void setupCouponsTable() {
        couponIdColumn.setCellValueFactory(new PropertyValueFactory<>("couponId"));
        couponCustomerColumn.setCellValueFactory(cellData -> {
            try {
                com.group17.greengrocer.model.User customer = ownerService.getUserById(cellData.getValue().getCustomerId());
                return new javafx.beans.property.SimpleStringProperty(
                    customer != null ? customer.getFullName() : "Unknown");
            } catch (Exception e) {
                return new javafx.beans.property.SimpleStringProperty("Unknown");
            }
        });
        couponCodeColumn.setCellValueFactory(new PropertyValueFactory<>("couponCode"));
        couponDiscountColumn.setCellValueFactory(cellData -> {
            java.math.BigDecimal discount = cellData.getValue().getDiscountAmount();
            if (discount == null && cellData.getValue().getDiscountPercent() != null) {
                discount = cellData.getValue().getDiscountPercent();
            }
            return new javafx.beans.property.SimpleDoubleProperty(
                discount != null ? discount.doubleValue() : 0.0).asObject();
        });
        couponUsedColumn.setCellValueFactory(new PropertyValueFactory<>("used"));
        couponsTable.setItems(coupons);
    }
    
    /**
     * Setup ratings table
     */
    private void setupRatingsTable() {
        ratingIdColumn.setCellValueFactory(new PropertyValueFactory<>("ratingId"));
        ratingCarrierColumn.setCellValueFactory(cellData -> {
            try {
                com.group17.greengrocer.model.User carrier = ownerService.getUserById(cellData.getValue().getCarrierId());
                return new javafx.beans.property.SimpleStringProperty(
                    carrier != null ? carrier.getFullName() : "Unknown");
            } catch (Exception e) {
                return new javafx.beans.property.SimpleStringProperty("Unknown");
            }
        });
        ratingCustomerColumn.setCellValueFactory(cellData -> {
            try {
                com.group17.greengrocer.model.User customer = ownerService.getUserById(cellData.getValue().getCustomerId());
                return new javafx.beans.property.SimpleStringProperty(
                    customer != null ? customer.getFullName() : "Unknown");
            } catch (Exception e) {
                return new javafx.beans.property.SimpleStringProperty("Unknown");
            }
        });
        ratingValueColumn.setCellValueFactory(new PropertyValueFactory<>("rating"));
        ratingCommentColumn.setCellValueFactory(new PropertyValueFactory<>("comment"));
        ratingDateColumn.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleStringProperty(
                cellData.getValue().getCreatedAt() != null ? 
                    cellData.getValue().getCreatedAt().toString() : ""));
        ratingsTable.setItems(ratings);
    }
    
    /**
     * Load data for all tables
     */
    private void loadData() {
        products.setAll(productService.getAllProducts());
        carriers.setAll(ownerService.getAllCarriers());
        loadAllOrders();
        loadMessages();
        loadCoupons();
        loadRatings();
        loadLoyaltyStandards();
    }
    
    /**
     * Load all orders
     */
    private void loadAllOrders() {
        try {
            allOrders.setAll(orderService.getAllOrders());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Load messages
     */
    private void loadMessages() {
        try {
            messages.setAll(ownerService.getAllMessages());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Load coupons
     */
    private void loadCoupons() {
        try {
            coupons.setAll(ownerService.getAllCoupons());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Load ratings
     */
    private void loadRatings() {
        try {
            ratings.setAll(ownerService.getAllRatings());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Load loyalty standards
     */
    private void loadLoyaltyStandards() {
        loyaltyStandardsContainer.getChildren().clear();
        
        Label titleLabel = new Label("Current Loyalty Standards:");
        titleLabel.setStyle("-fx-font-weight: bold;");
        
        Label standardLabel = new Label("Customers with 5 or more completed orders receive a 5% loyalty discount on all purchases.");
        
        loyaltyStandardsContainer.getChildren().addAll(titleLabel, standardLabel);
    }
    
    /**
     * Handle add product action
     */
    @FXML
    private void handleAddProduct() {
        Dialog<Product> dialog = createProductDialog(null);
        dialog.showAndWait().ifPresent(product -> {
            if (productService.createProduct(product)) {
                showAlert(Alert.AlertType.INFORMATION, "Success", "Product added successfully!");
                loadData();
            } else {
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to add product.");
            }
        });
    }
    
    /**
     * Handle update product action
     */
    @FXML
    private void handleUpdateProduct() {
        Product selected = productsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "No Selection", "Please select a product to update.");
            return;
        }
        
        Dialog<Product> dialog = createProductDialog(selected);
        dialog.showAndWait().ifPresent(product -> {
            product.setProductId(selected.getProductId());
            if (productService.updateProduct(product)) {
                showAlert(Alert.AlertType.INFORMATION, "Success", "Product updated successfully!");
                loadData();
            } else {
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to update product.");
            }
        });
    }
    
    /**
     * Handle delete product action
     */
    @FXML
    private void handleDeleteProduct() {
        Product selected = productsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "No Selection", "Please select a product to delete.");
            return;
        }
        
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirm Delete");
        confirm.setHeaderText("Delete Product");
        confirm.setContentText("Are you sure you want to delete " + selected.getProductName() + "?");
        
        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                if (productService.deleteProduct(selected.getProductId())) {
                    showAlert(Alert.AlertType.INFORMATION, "Success", "Product deleted successfully!");
                    loadData();
                } else {
                    showAlert(Alert.AlertType.ERROR, "Error", "Failed to delete product.");
                }
            }
        });
    }
    
    /**
     * Create product dialog
     */
    private Dialog<Product> createProductDialog(Product existing) {
        Dialog<Product> dialog = new Dialog<>();
        dialog.setTitle(existing == null ? "Add Product" : "Update Product");
        
        TextField nameField = new TextField(existing != null ? existing.getProductName() : "");
        TextField typeField = new TextField(existing != null ? existing.getProductType() : "");
        TextField priceField = new TextField(existing != null ? existing.getPricePerKg().toString() : "");
        TextField stockField = new TextField(existing != null ? existing.getStock().toString() : "");
        TextField thresholdField = new TextField(existing != null ? existing.getThreshold().toString() : "5.0");
        
        VBox content = new VBox(10);
        content.setPadding(new Insets(20));
        content.getChildren().addAll(
            new Label("Product Name:"), nameField,
            new Label("Product Type:"), typeField,
            new Label("Price per Kg:"), priceField,
            new Label("Stock:"), stockField,
            new Label("Threshold:"), thresholdField
        );
        
        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        
        dialog.setResultConverter(buttonType -> {
            if (buttonType == ButtonType.OK) {
                Product product = new Product();
                product.setProductName(nameField.getText());
                product.setProductType(typeField.getText());
                product.setPricePerKg(new BigDecimal(priceField.getText()));
                product.setStock(new BigDecimal(stockField.getText()));
                product.setThreshold(new BigDecimal(thresholdField.getText()));
                return product;
            }
            return null;
        });
        
        return dialog;
    }
    
    /**
     * Handle hire carrier action
     */
    @FXML
    private void handleHireCarrier() {
        Dialog<User> dialog = createCarrierDialog(null);
        dialog.showAndWait().ifPresent(carrier -> {
            if (ownerService.hireCarrier(carrier)) {
                showAlert(Alert.AlertType.INFORMATION, "Success", "Carrier hired successfully!");
                loadData();
            } else {
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to hire carrier.");
            }
        });
    }
    
    /**
     * Handle fire carrier action
     */
    @FXML
    private void handleFireCarrier() {
        User selected = carriersTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "No Selection", "Please select a carrier to fire.");
            return;
        }
        
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirm Fire");
        confirm.setHeaderText("Fire Carrier");
        confirm.setContentText("Are you sure you want to fire " + selected.getFullName() + "?");
        
        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                if (ownerService.fireCarrier(selected.getUserId())) {
                    showAlert(Alert.AlertType.INFORMATION, "Success", "Carrier fired successfully!");
                    loadData();
                } else {
                    showAlert(Alert.AlertType.ERROR, "Error", "Failed to fire carrier.");
                }
            }
        });
    }
    
    /**
     * Create carrier dialog
     */
    private Dialog<User> createCarrierDialog(User existing) {
        Dialog<User> dialog = new Dialog<>();
        dialog.setTitle(existing == null ? "Hire Carrier" : "Update Carrier");
        
        TextField usernameField = new TextField(existing != null ? existing.getUsername() : "");
        // Don't show existing password (it's hashed anyway) - leave empty for security
        TextField passwordField = new TextField("");
        TextField nameField = new TextField(existing != null ? existing.getFullName() : "");
        TextField emailField = new TextField(existing != null ? existing.getEmail() : "");
        TextField phoneField = new TextField(existing != null ? existing.getPhone() : "");
        
        // Add input length restrictions to prevent overflow
        nameField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null && newValue.length() > 100) {
                nameField.setText(oldValue);
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
            new Label("Username:"), usernameField,
            new Label("Password:"), passwordField,
            new Label("Full Name (required, letters and spaces only):"), nameField,
            new Label("Email (required, must contain @ and domain):"), emailField,
            new Label("Phone (required, format: 5XXXXXXXXX):"), phoneField
        );
        
        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        
        // Get OK button and add validation before dialog closes
        javafx.scene.control.Button okButton = (javafx.scene.control.Button) dialog.getDialogPane().lookupButton(ButtonType.OK);
        okButton.addEventFilter(javafx.event.ActionEvent.ACTION, e -> {
            String name = nameField.getText().trim();
            String email = emailField.getText().trim();
            String phone = phoneField.getText().trim();
            
            // Validate full name
            if (!com.group17.greengrocer.util.Validation.isValidFullName(name)) {
                showAlert(Alert.AlertType.ERROR, "Invalid Full Name", 
                    "Full name is required and must contain only letters and spaces (no numbers).");
                e.consume(); // Prevent dialog from closing
                return;
            }
            
            // Validate email
            if (!com.group17.greengrocer.util.Validation.isValidEmailFormat(email)) {
                showAlert(Alert.AlertType.ERROR, "Invalid Email", 
                    "Email is required and must be in valid format (e.g., user@example.com).");
                e.consume(); // Prevent dialog from closing
                return;
            }
            
            // Validate phone
            if (!com.group17.greengrocer.util.Validation.isValidPhone(phone)) {
                showAlert(Alert.AlertType.ERROR, "Invalid Phone", 
                    "Phone is required and must be exactly 10 digits starting with 5 (e.g., 5372440233).");
                e.consume(); // Prevent dialog from closing
                return;
            }
        });
        
        dialog.setResultConverter(buttonType -> {
            if (buttonType == ButtonType.OK) {
                String name = nameField.getText().trim();
                String email = emailField.getText().trim();
                String phone = phoneField.getText().trim();
                
                User carrier = new User();
                carrier.setUsername(usernameField.getText().trim());
                carrier.setPassword(passwordField.getText().trim());
                carrier.setFullName(name);
                carrier.setEmail(email);
                carrier.setPhone(phone);
                return carrier;
            }
            return null;
        });
        
        return dialog;
    }
    
    /**
     * Handle profit report action
     */
    @FXML
    private void handleProfitReport() {
        reportsContainer.getChildren().clear();
        
        Map<String, Object> report = ownerService.getProfitReport();
        BigDecimal totalProfit = (BigDecimal) report.get("totalProfit");
        Integer totalOrders = (Integer) report.get("totalOrders");
        
        Label titleLabel = new Label("Profit Report");
        titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");
        
        Label profitLabel = new Label("Total Profit: ₺" + totalProfit);
        Label ordersLabel = new Label("Total Delivered Orders: " + totalOrders);
        
        reportsContainer.getChildren().addAll(titleLabel, profitLabel, ordersLabel);
    }
    
    /**
     * Handle delivered orders report action
     */
    @FXML
    private void handleDeliveredOrders() {
        reportsContainer.getChildren().clear();
        
        Label titleLabel = new Label("Delivered Orders Report");
        titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");
        
        TableView<Order> table = new TableView<>();
        TableColumn<Order, Integer> orderIdCol = new TableColumn<>("Order ID");
        TableColumn<Order, Double> totalCol = new TableColumn<>("Total Cost");
        TableColumn<Order, String> dateCol = new TableColumn<>("Delivery Date");
        
        orderIdCol.setCellValueFactory(new PropertyValueFactory<>("orderId"));
        totalCol.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleDoubleProperty(
                cellData.getValue().getTotalCost().doubleValue()).asObject());
        dateCol.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleStringProperty(
                cellData.getValue().getDeliveryDate() != null ? 
                    cellData.getValue().getDeliveryDate().toString() : ""));
        
        table.getColumns().add(orderIdCol);
        table.getColumns().add(totalCol);
        table.getColumns().add(dateCol);
        table.setItems(FXCollections.observableArrayList(ownerService.getDeliveredOrdersReport()));
        
        reportsContainer.getChildren().addAll(titleLabel, table);
    }
    
    /**
     * Handle carrier performance report action
     */
    @FXML
    private void handleCarrierPerformance() {
        reportsContainer.getChildren().clear();
        
        Label titleLabel = new Label("Carrier Performance Report");
        titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");
        
        Map<Integer, Integer> performance = ownerService.getCarrierPerformanceReport();
        
        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();
        BarChart<String, Number> chart = new BarChart<>(xAxis, yAxis);
        chart.setTitle("Orders Completed by Carrier");
        
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        
        for (Map.Entry<Integer, Integer> entry : performance.entrySet()) {
            try {
                User carrier = ownerService.getAllCarriers().stream()
                    .filter(c -> c.getUserId() == entry.getKey())
                    .findFirst()
                    .orElse(null);
                String carrierName = carrier != null ? carrier.getFullName() : "Carrier " + entry.getKey();
                series.getData().add(new XYChart.Data<>(carrierName, entry.getValue()));
            } catch (Exception e) {
                series.getData().add(new XYChart.Data<>("Carrier " + entry.getKey(), entry.getValue()));
            }
        }
        
        chart.getData().add(series);
        chart.setPrefHeight(400);
        
        reportsContainer.getChildren().addAll(titleLabel, chart);
    }
    
    /**
     * Handle view message action
     */
    private void handleViewMessage(com.group17.greengrocer.model.Message message) {
        Dialog<javafx.util.Pair<Boolean, String>> dialog = new Dialog<>();
        dialog.setTitle("View/Reply Message");
        dialog.setHeaderText("Message from Customer");
        
        TextArea messageArea = new TextArea();
        messageArea.setText("Subject: " + message.getSubject() + "\n\n" + message.getMessage());
        messageArea.setEditable(false);
        messageArea.setPrefRowCount(10);
        
        TextArea replyArea = new TextArea();
        replyArea.setPromptText("Enter your reply...");
        replyArea.setPrefRowCount(5);
        
        VBox content = new VBox(10);
        content.setPadding(new Insets(20));
        content.getChildren().addAll(
            new Label("Message:"), messageArea,
            new Label("Reply:"), replyArea
        );
        
        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        
        dialog.setResultConverter(buttonType -> {
            if (buttonType == ButtonType.OK) {
                return new javafx.util.Pair<>(true, replyArea.getText());
            }
            return null;
        });
        
        dialog.showAndWait().ifPresent(result -> {
            if (result.getKey() && !result.getValue().trim().isEmpty()) {
                if (ownerService.replyToMessage(message.getMessageId(), result.getValue())) {
                    showAlert(Alert.AlertType.INFORMATION, "Success", "Reply sent successfully!");
                    loadMessages();
                } else {
                    showAlert(Alert.AlertType.ERROR, "Error", "Failed to send reply.");
                }
            }
        });
    }
    
    /**
     * Handle create coupon action
     */
    @FXML
    private void handleCreateCoupon() {
        Dialog<javafx.util.Pair<Integer, javafx.util.Pair<String, java.math.BigDecimal>>> dialog = new Dialog<>();
        dialog.setTitle("Create Coupon");
        dialog.setHeaderText("Create a new discount coupon");
        
        ComboBox<com.group17.greengrocer.model.User> customerComboBox = new ComboBox<>();
        customerComboBox.getItems().addAll(ownerService.getAllCustomers());
        
        // Set cell factory for dropdown items
        customerComboBox.setCellFactory(param -> new ListCell<com.group17.greengrocer.model.User>() {
            @Override
            protected void updateItem(com.group17.greengrocer.model.User item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.getFullName() + " (" + item.getUsername() + ")");
                }
            }
        });
        
        // Set button cell for selected item display
        customerComboBox.setButtonCell(new ListCell<com.group17.greengrocer.model.User>() {
            @Override
            protected void updateItem(com.group17.greengrocer.model.User item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.getFullName() + " (" + item.getUsername() + ")");
                }
            }
        });
        
        // Generate random coupon code (6-8 digits)
        String generatedCode = generateUniqueCouponCode();
        
        TextField codeField = new TextField(generatedCode);
        codeField.setEditable(false); // Read-only, auto-generated
        codeField.setStyle("-fx-background-color: #f0f0f0;");
        
        Button regenerateButton = new Button("Regenerate");
        regenerateButton.setOnAction(e -> {
            codeField.setText(generateUniqueCouponCode());
        });
        
        HBox codeBox = new HBox(10);
        codeBox.getChildren().addAll(codeField, regenerateButton);
        
        TextField discountField = new TextField();
        discountField.setPromptText("Discount amount (e.g., 10.00)");
        
        VBox content = new VBox(10);
        content.setPadding(new Insets(20));
        content.getChildren().addAll(
            new Label("Customer:"), customerComboBox,
            new Label("Coupon Code (auto-generated):"), codeBox,
            new Label("Discount Amount (₺):"), discountField
        );
        
        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        
        dialog.setResultConverter(buttonType -> {
            if (buttonType == ButtonType.OK) {
                com.group17.greengrocer.model.User customer = customerComboBox.getValue();
                String couponCode = codeField.getText().trim();
                if (customer != null && !couponCode.isEmpty() && !discountField.getText().trim().isEmpty()) {
                    try {
                        java.math.BigDecimal discount = new java.math.BigDecimal(discountField.getText());
                        if (discount.compareTo(java.math.BigDecimal.ZERO) <= 0) {
                            showAlert(Alert.AlertType.ERROR, "Invalid Input", "Discount amount must be greater than 0.");
                            return null;
                        }
                        return new javafx.util.Pair<>(customer.getUserId(), 
                            new javafx.util.Pair<>(couponCode, discount));
                    } catch (NumberFormatException e) {
                        showAlert(Alert.AlertType.ERROR, "Invalid Input", "Please enter a valid discount amount.");
                    }
                } else {
                    if (customer == null) {
                        showAlert(Alert.AlertType.ERROR, "Missing Field", "Please select a customer.");
                    } else if (discountField.getText().trim().isEmpty()) {
                        showAlert(Alert.AlertType.ERROR, "Missing Field", "Please enter discount amount.");
                    }
                }
            }
            return null;
        });
        
        dialog.showAndWait().ifPresent(result -> {
            if (ownerService.createCoupon(result.getKey(), result.getValue().getKey(), 
                result.getValue().getValue(), null)) {
                showAlert(Alert.AlertType.INFORMATION, "Success", "Coupon created successfully!");
                loadCoupons();
            } else {
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to create coupon.");
            }
        });
    }
    
    /**
     * Generate unique coupon code (6-8 digits)
     */
    private String generateUniqueCouponCode() {
        java.util.Random random = new java.util.Random();
        int maxAttempts = 100; // Prevent infinite loop
        int attempts = 0;
        
        while (attempts < maxAttempts) {
            // Generate 6-8 digit code
            int codeLength = 6 + random.nextInt(3); // 6, 7, or 8 digits
            int min = (int) Math.pow(10, codeLength - 1);
            int max = (int) Math.pow(10, codeLength) - 1;
            int codeNumber = min + random.nextInt(max - min + 1);
            String code = String.valueOf(codeNumber);
            
            // Check if code already exists
            if (isCouponCodeUnique(code)) {
                return code;
            }
            attempts++;
        }
        
        // Fallback: use timestamp-based code if all attempts fail
        return String.valueOf(System.currentTimeMillis() % 100000000); // Last 8 digits
    }
    
    /**
     * Check if coupon code is unique
     */
    private boolean isCouponCodeUnique(String code) {
        try {
            return ownerService.isCouponCodeUnique(code);
        } catch (Exception e) {
            e.printStackTrace();
            return true; // Assume unique if check fails
        }
    }
    
    /**
     * Handle adjust loyalty standards action
     */
    @FXML
    private void handleAdjustLoyalty() {
        Alert infoAlert = new Alert(Alert.AlertType.INFORMATION);
        infoAlert.setTitle("Loyalty Standards");
        infoAlert.setHeaderText("Current Loyalty Standards");
        infoAlert.setContentText("Customers with 5 or more completed orders receive a 5% loyalty discount on all purchases.\n\n" +
            "This standard is currently fixed. In a production system, you could adjust this threshold and discount percentage.");
        infoAlert.showAndWait();
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
}

