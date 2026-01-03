package com.group05.greengrocer.controller;

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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import com.group05.greengrocer.model.Order;
import com.group05.greengrocer.model.Product;
import com.group05.greengrocer.model.User;
import com.group05.greengrocer.service.AuthService;
import com.group05.greengrocer.service.OwnerService;
import com.group05.greengrocer.service.ProductService;

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
    private Button applyDiscountButton;

    @FXML
    private Button deleteProductButton;

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

    @FXML
    private VBox reportsContainer;

    @FXML
    private Button profitReportButton;

    @FXML
    private Button deliveredOrdersButton;

    @FXML
    private Button carrierPerformanceButton;

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

    @FXML
    private TableView<com.group05.greengrocer.model.Message> messagesTable;
    @FXML
    private TableColumn<com.group05.greengrocer.model.Message, Integer> messageIdColumn;
    @FXML
    private TableColumn<com.group05.greengrocer.model.Message, String> messageCustomerColumn;
    @FXML
    private TableColumn<com.group05.greengrocer.model.Message, String> messageSubjectColumn;
    @FXML
    private TableColumn<com.group05.greengrocer.model.Message, String> messageDateColumn;
    @FXML
    private TableColumn<com.group05.greengrocer.model.Message, Boolean> messageReadColumn;
    @FXML
    private TableColumn<com.group05.greengrocer.model.Message, Void> messageActionColumn;

    @FXML
    private Button createCouponButton;
    @FXML
    private Button adjustLoyaltyButton;
    @FXML
    private TableView<com.group05.greengrocer.model.Coupon> couponsTable;
    @FXML
    private TableColumn<com.group05.greengrocer.model.Coupon, Integer> couponIdColumn;
    @FXML
    private TableColumn<com.group05.greengrocer.model.Coupon, String> couponCustomerColumn;
    @FXML
    private TableColumn<com.group05.greengrocer.model.Coupon, String> couponCodeColumn;
    @FXML
    private TableColumn<com.group05.greengrocer.model.Coupon, Double> couponDiscountColumn;
    @FXML
    private TableColumn<com.group05.greengrocer.model.Coupon, Boolean> couponUsedColumn;
    @FXML
    private VBox loyaltyStandardsContainer;

    @FXML
    private TableView<com.group05.greengrocer.model.CarrierRating> ratingsTable;
    @FXML
    private TableColumn<com.group05.greengrocer.model.CarrierRating, Integer> ratingIdColumn;
    @FXML
    private TableColumn<com.group05.greengrocer.model.CarrierRating, String> ratingCarrierColumn;
    @FXML
    private TableColumn<com.group05.greengrocer.model.CarrierRating, String> ratingCustomerColumn;
    @FXML
    private TableColumn<com.group05.greengrocer.model.CarrierRating, Integer> ratingValueColumn;
    @FXML
    private TableColumn<com.group05.greengrocer.model.CarrierRating, String> ratingCommentColumn;
    @FXML
    private TableColumn<com.group05.greengrocer.model.CarrierRating, String> ratingDateColumn;

    private ProductService productService;
    private OwnerService ownerService;
    private AuthService authService;
    private com.group05.greengrocer.service.OrderService orderService;
    private com.group05.greengrocer.service.MessageService messageService;
    private com.group05.greengrocer.service.CouponService couponService;
    private com.group05.greengrocer.service.RatingService ratingService;

    private ObservableList<Product> products;
    private ObservableList<User> carriers;
    private ObservableList<Order> allOrders;
    private ObservableList<com.group05.greengrocer.model.Message> messages;
    private ObservableList<com.group05.greengrocer.model.Coupon> coupons;
    private ObservableList<com.group05.greengrocer.model.CarrierRating> ratings;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        productService = new ProductService();
        ownerService = new OwnerService();
        authService = new AuthService();
        orderService = new com.group05.greengrocer.service.OrderService();
        messageService = new com.group05.greengrocer.service.MessageService();
        couponService = new com.group05.greengrocer.service.CouponService();
        ratingService = new com.group05.greengrocer.service.RatingService();

        products = FXCollections.observableArrayList();
        carriers = FXCollections.observableArrayList();
        allOrders = FXCollections.observableArrayList();
        messages = FXCollections.observableArrayList();
        coupons = FXCollections.observableArrayList();
        ratings = FXCollections.observableArrayList();

        if (authService.getCurrentUser() != null) {
            welcomeLabel.setText("Welcome, " + authService.getCurrentUser().getFullName());
        }

        setupTables();
        loadData();

        checkLowStockProducts();

        if (mainTabPane != null) {
            for (Tab tab : mainTabPane.getTabs()) {
                tab.setClosable(false);
            }
        }
    }

    /**
     * Setup all tables.
     */
    private void setupTables() {
        productIdColumn.setCellValueFactory(new PropertyValueFactory<>("productId"));
        productNameColumn.setCellValueFactory(new PropertyValueFactory<>("productName"));
        productTypeColumn.setCellValueFactory(new PropertyValueFactory<>("productType"));
        priceColumn.setCellValueFactory(new PropertyValueFactory<>("pricePerKg"));
        stockColumn.setCellValueFactory(new PropertyValueFactory<>("stock"));
        thresholdColumn.setCellValueFactory(new PropertyValueFactory<>("threshold"));
        productsTable.setItems(products);

        carrierIdColumn.setCellValueFactory(new PropertyValueFactory<>("userId"));
        carrierNameColumn.setCellValueFactory(new PropertyValueFactory<>("fullName"));
        carrierEmailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));
        carrierPhoneColumn.setCellValueFactory(new PropertyValueFactory<>("phone"));
        carriersTable.setItems(carriers);

        setupAllOrdersTable();

        setupMessagesTable();

        setupCouponsTable();

        setupRatingsTable();
    }

    /**
     * Setup all orders table.
     */
    private void setupAllOrdersTable() {
        allOrderIdColumn.setCellValueFactory(new PropertyValueFactory<>("orderId"));
        allCustomerColumn.setCellValueFactory(cellData -> {
            try {
                com.group05.greengrocer.model.User customer = ownerService
                        .getUserById(cellData.getValue().getCustomerId());
                return new javafx.beans.property.SimpleStringProperty(
                        customer != null ? customer.getFullName() : "Unknown");
            } catch (Exception e) {
                return new javafx.beans.property.SimpleStringProperty("Unknown");
            }
        });
        allCarrierColumn.setCellValueFactory(cellData -> {
            try {
                if (cellData.getValue().getCarrierId() != null) {
                    com.group05.greengrocer.model.User carrier = ownerService
                            .getUserById(cellData.getValue().getCarrierId());
                    return new javafx.beans.property.SimpleStringProperty(
                            carrier != null ? carrier.getFullName() : "Unknown");
                }
                return new javafx.beans.property.SimpleStringProperty("Not assigned");
            } catch (Exception e) {
                return new javafx.beans.property.SimpleStringProperty("Unknown");
            }
        });
        allStatusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        allTotalColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleDoubleProperty(
                cellData.getValue().getTotalCost().doubleValue()).asObject());
        allOrderDateColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(
                cellData.getValue().getOrderDate() != null ? cellData.getValue().getOrderDate().toString() : ""));
        allDeliveryDateColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(
                cellData.getValue().getDeliveryDate() != null ? cellData.getValue().getDeliveryDate().toString() : ""));
        allOrdersTable.setItems(allOrders);
    }

    /**
     * Setup messages table.
     */
    private void setupMessagesTable() {
        messageIdColumn.setCellValueFactory(new PropertyValueFactory<>("messageId"));
        messageCustomerColumn.setCellValueFactory(cellData -> {
            try {
                com.group05.greengrocer.model.User customer = ownerService
                        .getUserById(cellData.getValue().getCustomerId());
                return new javafx.beans.property.SimpleStringProperty(
                        customer != null ? customer.getFullName() : "Unknown");
            } catch (Exception e) {
                return new javafx.beans.property.SimpleStringProperty("Unknown");
            }
        });
        messageSubjectColumn.setCellValueFactory(new PropertyValueFactory<>("subject"));
        messageDateColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(
                cellData.getValue().getCreatedAt() != null ? cellData.getValue().getCreatedAt().toString() : ""));
        messageReadColumn.setCellValueFactory(new PropertyValueFactory<>("read"));

        messageActionColumn.setCellFactory(param -> new TableCell<com.group05.greengrocer.model.Message, Void>() {
            private final Button viewButton = new Button("View/Reply");

            {
                viewButton.setOnAction(event -> {
                    com.group05.greengrocer.model.Message message = getTableView().getItems().get(getIndex());
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
     * Setup coupons table.
     */
    private void setupCouponsTable() {
        couponIdColumn.setCellValueFactory(new PropertyValueFactory<>("couponId"));
        couponCustomerColumn.setCellValueFactory(cellData -> {
            try {
                com.group05.greengrocer.model.User customer = ownerService
                        .getUserById(cellData.getValue().getCustomerId());
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
     * Setup ratings table.
     */
    private void setupRatingsTable() {
        ratingIdColumn.setCellValueFactory(new PropertyValueFactory<>("ratingId"));
        ratingCarrierColumn.setCellValueFactory(cellData -> {
            try {
                com.group05.greengrocer.model.User carrier = ownerService
                        .getUserById(cellData.getValue().getCarrierId());
                return new javafx.beans.property.SimpleStringProperty(
                        carrier != null ? carrier.getFullName() : "Unknown");
            } catch (Exception e) {
                return new javafx.beans.property.SimpleStringProperty("Unknown");
            }
        });
        ratingCustomerColumn.setCellValueFactory(cellData -> {
            try {
                com.group05.greengrocer.model.User customer = ownerService
                        .getUserById(cellData.getValue().getCustomerId());
                return new javafx.beans.property.SimpleStringProperty(
                        customer != null ? customer.getFullName() : "Unknown");
            } catch (Exception e) {
                return new javafx.beans.property.SimpleStringProperty("Unknown");
            }
        });
        ratingValueColumn.setCellValueFactory(new PropertyValueFactory<>("rating"));
        ratingCommentColumn.setCellValueFactory(new PropertyValueFactory<>("comment"));
        ratingDateColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(
                cellData.getValue().getCreatedAt() != null ? cellData.getValue().getCreatedAt().toString() : ""));
        ratingsTable.setItems(ratings);
    }

    /**
     * Load data for all tables.
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
     * Check for products below threshold and show warning.
     */
    private void checkLowStockProducts() {
        List<Product> lowStockProducts = new ArrayList<>();
        for (Product product : products) {
            if (product.getStock().compareTo(product.getThreshold()) < 0) {
                lowStockProducts.add(product);
            }
        }

        if (!lowStockProducts.isEmpty()) {
            StringBuilder message = new StringBuilder("Warning: The following products are below their threshold:\n\n");
            for (Product product : lowStockProducts) {
                message.append("• ").append(product.getProductName())
                        .append(" - Stock: ").append(product.getStock())
                        .append(" kg (Threshold: ").append(product.getThreshold()).append(" kg)\n");
            }
            showAlert(Alert.AlertType.WARNING, "Low Stock Alert", message.toString());
        }
    }

    /**
     * Load all orders.
     */
    private void loadAllOrders() {
        try {
            allOrders.setAll(orderService.getAllOrders());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Load messages.
     */
    private void loadMessages() {
        try {
            messages.setAll(ownerService.getAllMessages());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Load coupons.
     */
    private void loadCoupons() {
        try {
            coupons.setAll(ownerService.getAllCoupons());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Load ratings.
     */
    private void loadRatings() {
        try {
            ratings.setAll(ownerService.getAllRatings());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Load loyalty standards.
     */
    private void loadLoyaltyStandards() {
        loyaltyStandardsContainer.getChildren().clear();

        int threshold = com.group05.greengrocer.service.LoyaltyService.getLoyaltyThreshold();
        java.math.BigDecimal discountPercent = com.group05.greengrocer.service.LoyaltyService
                .getLoyaltyDiscountPercent();

        Label titleLabel = new Label("Current Loyalty Standards:");
        titleLabel.setStyle("-fx-font-weight: bold;");

        Label standardLabel = new Label("Customers with " + threshold + " or more completed orders receive a " +
                discountPercent + "% loyalty discount on all purchases.");

        loyaltyStandardsContainer.getChildren().addAll(titleLabel, standardLabel);
    }

    /**
     * Handle add product action.
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
     * Handle update product action.
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
            if (product == null) {
                return; // User cancelled or validation failed
            }

            product.setProductId(selected.getProductId());

            System.out.println("Updating product ID: " + product.getProductId());
            System.out.println("Product Name: " + product.getProductName());
            System.out.println("Product Type: " + product.getProductType());
            System.out.println("Price: " + product.getPricePerKg());
            System.out.println("Stock: " + product.getStock());
            System.out.println("Threshold: " + product.getThreshold());
            System.out.println("Description: " + product.getDescription());
            System.out.println("Image URL: " + product.getImageUrl());

            try {
                if (productService.updateProduct(product)) {
                    showAlert(Alert.AlertType.INFORMATION, "Success", "Product updated successfully!");
                    loadData();
                } else {
                    showAlert(Alert.AlertType.ERROR, "Error",
                            "Failed to update product. Please check console for details.");
                }
            } catch (Exception e) {
                showAlert(Alert.AlertType.ERROR, "Error",
                        "Failed to update product: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }

    /**
     * Handle apply discount action.
     */
    @FXML
    private void handleApplyDiscount() {
        Product selected = productsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "No Selection", "Please select a product to apply discount.");
            return;
        }

        Dialog<DiscountInfo> dialog = createDiscountDialog(selected);
        dialog.showAndWait().ifPresent(discountInfo -> {
            try {
                BigDecimal originalPrice = selected.getPricePerKg();
                BigDecimal discountPercent = discountInfo.discountPercent;
                BigDecimal discountAmount = originalPrice.multiply(discountPercent.divide(new BigDecimal("100")));
                BigDecimal newPrice = originalPrice.subtract(discountAmount);

                selected.setPricePerKg(newPrice);
                if (productService.updateProduct(selected)) {
                    showAlert(Alert.AlertType.INFORMATION, "Success",
                            String.format("Discount applied! New price: ₺%.2f (%.1f%% off)",
                                    newPrice, discountPercent));
                    loadData();
                } else {
                    showAlert(Alert.AlertType.ERROR, "Error", "Failed to apply discount.");
                }
            } catch (Exception e) {
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to apply discount: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }

    /**
     * Create discount dialog.
     * 
     * @param product The product to apply discount to
     * @return The discount dialog
     */
    private Dialog<DiscountInfo> createDiscountDialog(Product product) {
        Dialog<DiscountInfo> dialog = new Dialog<>();
        dialog.setTitle("Apply Discount");
        dialog.setHeaderText("Apply discount to: " + product.getProductName() +
                "\nCurrent Price: ₺" + product.getPricePerKg());

        TextField discountField = new TextField();
        discountField.setPromptText("Discount percentage (0-100)");

        discountField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null && !newValue.matches("^[0-9]*\\.?[0-9]*$")) {
                discountField.setText(oldValue);
            }
        });

        VBox content = new VBox(10);
        content.setPadding(new Insets(20));
        content.getChildren().addAll(
                new Label("Discount Percentage:"),
                discountField,
                new Label("Example: 10 for 10% discount"));

        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        javafx.scene.control.Button okButton = (javafx.scene.control.Button) dialog.getDialogPane()
                .lookupButton(ButtonType.OK);
        okButton.addEventFilter(javafx.event.ActionEvent.ACTION, e -> {
            try {
                String discountText = discountField.getText().trim();
                if (discountText.isEmpty()) {
                    showAlert(Alert.AlertType.ERROR, "Invalid Input", "Please enter a discount percentage.");
                    e.consume();
                    return;
                }

                BigDecimal discountPercent = new BigDecimal(discountText);
                if (discountPercent.compareTo(BigDecimal.ZERO) < 0
                        || discountPercent.compareTo(new BigDecimal("100")) > 0) {
                    showAlert(Alert.AlertType.ERROR, "Invalid Input",
                            "Discount percentage must be between 0 and 100.");
                    e.consume();
                    return;
                }
            } catch (NumberFormatException ex) {
                showAlert(Alert.AlertType.ERROR, "Invalid Input", "Please enter a valid number.");
                e.consume();
                return;
            }
        });

        dialog.setResultConverter(buttonType -> {
            if (buttonType == ButtonType.OK) {
                try {
                    BigDecimal discountPercent = new BigDecimal(discountField.getText().trim());
                    return new DiscountInfo(discountPercent);
                } catch (NumberFormatException e) {
                    return null;
                }
            }
            return null;
        });

        return dialog;
    }

    /**
     * Helper class for discount information.
     */
    private static class DiscountInfo {
        BigDecimal discountPercent;

        DiscountInfo(BigDecimal discountPercent) {
            this.discountPercent = discountPercent;
        }
    }

    /**
     * Handle delete product action.
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
     * Create product dialog.
     * 
     * @param existing The existing product to edit, or null for new product
     * @return The product dialog
     */
    private Dialog<Product> createProductDialog(Product existing) {
        Dialog<Product> dialog = new Dialog<>();
        dialog.setTitle(existing == null ? "Add Product" : "Update Product");

        TextField nameField = new TextField(existing != null ? existing.getProductName() : "");
        TextField typeField = new TextField(existing != null ? existing.getProductType() : "");
        TextField priceField = new TextField(existing != null ? existing.getPricePerKg().toString() : "");
        TextField stockField = new TextField(existing != null ? existing.getStock().toString() : "");
        TextField thresholdField = new TextField(existing != null ? existing.getThreshold().toString() : "5.0");
        TextField descriptionField = new TextField(
                existing != null && existing.getDescription() != null ? existing.getDescription() : "");
        descriptionField.setPromptText("Product description (optional)");
        TextField imageUrlField = new TextField(
                existing != null && existing.getImageUrl() != null ? existing.getImageUrl() : "");
        imageUrlField.setPromptText("Image URL (e.g., https://example.com/image.jpg)");

        VBox content = new VBox(10);
        content.setPadding(new Insets(20));
        content.getChildren().addAll(
                new Label("Product Name:"), nameField,
                new Label("Product Type:"), typeField,
                new Label("Price per Kg:"), priceField,
                new Label("Stock:"), stockField,
                new Label("Threshold:"), thresholdField,
                new Label("Description:"), descriptionField,
                new Label("Image URL:"), imageUrlField);

        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        javafx.scene.control.Button okButton = (javafx.scene.control.Button) dialog.getDialogPane()
                .lookupButton(ButtonType.OK);
        okButton.addEventFilter(javafx.event.ActionEvent.ACTION, e -> {
            String name = nameField.getText().trim();
            if (name.isEmpty() || !name.matches("^[a-zA-ZçğıöşüÇĞIİÖŞÜ\\s-]+$")) {
                showAlert(Alert.AlertType.ERROR, "Invalid Product Name",
                        "Product name must contain only letters and spaces (no numbers).");
                e.consume();
                return;
            }

            String type = typeField.getText().trim();
            if (type.isEmpty() || !type.matches("^[a-zA-ZçğıöşüÇĞIİÖŞÜ\\s-]+$")) {
                showAlert(Alert.AlertType.ERROR, "Invalid Product Type",
                        "Product type must contain only letters and spaces (no numbers).");
                e.consume();
                return;
            }

            try {
                BigDecimal price = new BigDecimal(priceField.getText().trim());
                if (price.compareTo(BigDecimal.ZERO) <= 0) {
                    showAlert(Alert.AlertType.ERROR, "Invalid Price",
                            "Price must be a positive number.");
                    e.consume();
                    return;
                }
            } catch (NumberFormatException ex) {
                showAlert(Alert.AlertType.ERROR, "Invalid Price",
                        "Price must be a valid number.");
                e.consume();
                return;
            }

            try {
                BigDecimal stock = new BigDecimal(stockField.getText().trim());
                if (stock.compareTo(BigDecimal.ZERO) < 0) {
                    showAlert(Alert.AlertType.ERROR, "Invalid Stock",
                            "Stock cannot be negative.");
                    e.consume();
                    return;
                }
            } catch (NumberFormatException ex) {
                showAlert(Alert.AlertType.ERROR, "Invalid Stock",
                        "Stock must be a valid number.");
                e.consume();
                return;
            }

            try {
                BigDecimal threshold = new BigDecimal(thresholdField.getText().trim());
                if (threshold.compareTo(BigDecimal.ZERO) <= 0) {
                    showAlert(Alert.AlertType.ERROR, "Invalid Threshold",
                            "Threshold must be a positive number.");
                    e.consume();
                    return;
                }
            } catch (NumberFormatException ex) {
                showAlert(Alert.AlertType.ERROR, "Invalid Threshold",
                        "Threshold must be a valid number.");
                e.consume();
                return;
            }
        });

        dialog.setResultConverter(buttonType -> {
            if (buttonType == ButtonType.OK) {
                try {
                    Product product = new Product();
                    product.setProductName(nameField.getText().trim());
                    product.setProductType(typeField.getText().trim());
                    product.setPricePerKg(new BigDecimal(priceField.getText().trim()));
                    product.setStock(new BigDecimal(stockField.getText().trim()));
                    product.setThreshold(new BigDecimal(thresholdField.getText().trim()));
                    product.setDescription(descriptionField.getText().trim());

                    if (existing != null) {
                        product.setImagePath(existing.getImagePath());
                    } else {
                        product.setImagePath(null);
                    }

                    String imageUrl = imageUrlField.getText().trim();
                    if (!imageUrl.isEmpty()) {
                        product.setImageUrl(imageUrl);
                        product.setImageData(null);
                    } else {
                        if (existing != null) {
                            product.setImageUrl(existing.getImageUrl());
                            product.setImageData(null);
                        } else {
                            product.setImageUrl(null);
                            product.setImageData(null);
                        }
                    }

                    return product;
                } catch (Exception e) {
                    System.err.println("Error creating product object in dialog: " + e.getMessage());
                    e.printStackTrace();
                    showAlert(Alert.AlertType.ERROR, "Error",
                            "Failed to create product: " + e.getMessage());
                    return null;
                }
            }
            return null;
        });

        return dialog;
    }

    /**
     * Handle hire carrier action.
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
     * Handle fire carrier action.
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
     * Create carrier dialog.
     * 
     * @param existing The existing carrier to edit, or null for new carrier
     * @return The carrier dialog
     */
    private Dialog<User> createCarrierDialog(User existing) {
        Dialog<User> dialog = new Dialog<>();
        dialog.setTitle(existing == null ? "Hire Carrier" : "Update Carrier");

        TextField usernameField = new TextField(existing != null ? existing.getUsername() : "");
        TextField passwordField = new TextField("");
        TextField nameField = new TextField(existing != null ? existing.getFullName() : "");
        TextField emailField = new TextField(existing != null ? existing.getEmail() : "");
        TextField phoneField = new TextField(existing != null ? existing.getPhone() : "");

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
            if (newValue != null && !newValue.matches("^[0-9]*$")) {
                phoneField.setText(oldValue);
            }
            if (newValue != null && newValue.length() > 0 && !newValue.startsWith("5")) {
                phoneField.setText("5");
            }
        });

        nameField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null && !newValue.matches("^[a-zA-ZçğıöşüÇĞIİÖŞÜ\\s-]*$")) {
                nameField.setText(oldValue);
            }
        });

        VBox content = new VBox(10);
        content.setPadding(new Insets(20));
        content.getChildren().addAll(
                new Label("Username:"), usernameField,
                new Label("Password:"), passwordField,
                new Label("Full Name (required, letters and spaces only):"), nameField,
                new Label("Email (required, must contain @ and domain):"), emailField,
                new Label("Phone (required, format: 5XXXXXXXXX):"), phoneField);

        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        javafx.scene.control.Button okButton = (javafx.scene.control.Button) dialog.getDialogPane()
                .lookupButton(ButtonType.OK);
        okButton.addEventFilter(javafx.event.ActionEvent.ACTION, e -> {
            String name = nameField.getText().trim();
            String email = emailField.getText().trim();
            String phone = phoneField.getText().trim();
            String username = usernameField.getText().trim();
            String password = passwordField.getText().trim();

            if (username.isEmpty() || !com.group05.greengrocer.util.Validation.isValidUsername(username)) {
                showAlert(Alert.AlertType.ERROR, "Invalid Username",
                        "Username must be 3-50 characters and contain only letters, numbers, and underscores.");
                e.consume();
                return;
            }

            if (existing == null || !password.isEmpty()) {
                if (password.isEmpty() || !com.group05.greengrocer.util.Validation.isStrongPassword(password)) {
                    showAlert(Alert.AlertType.ERROR, "Invalid Password",
                            "Password must be at least 8 characters and contain uppercase, lowercase, and a digit.");
                    e.consume();
                    return;
                }
            }

            // Validate full name
            if (!com.group05.greengrocer.util.Validation.isValidFullName(name)) {
                showAlert(Alert.AlertType.ERROR, "Invalid Full Name",
                        "Full name is required and must contain only letters and spaces (no numbers).");
                e.consume();
                return;
            }

            if (!com.group05.greengrocer.util.Validation.isValidEmailFormat(email)) {
                showAlert(Alert.AlertType.ERROR, "Invalid Email",
                        "Email is required and must be in valid format (e.g., user@example.com).");
                e.consume();
                return;
            }

            if (!com.group05.greengrocer.util.Validation.isValidPhone(phone)) {
                showAlert(Alert.AlertType.ERROR, "Invalid Phone",
                        "Phone is required and must be exactly 10 digits starting with 5 (e.g., 5372440233).");
                e.consume();
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
     * Handle profit report action.
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
     * Handle delivered orders report action.
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
        totalCol.setCellValueFactory(cellData -> new javafx.beans.property.SimpleDoubleProperty(
                cellData.getValue().getTotalCost().doubleValue()).asObject());
        dateCol.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(
                cellData.getValue().getDeliveryDate() != null ? cellData.getValue().getDeliveryDate().toString() : ""));

        table.getColumns().add(orderIdCol);
        table.getColumns().add(totalCol);
        table.getColumns().add(dateCol);
        table.setItems(FXCollections.observableArrayList(ownerService.getDeliveredOrdersReport()));

        reportsContainer.getChildren().addAll(titleLabel, table);
    }

    /**
     * Handle carrier performance report action.
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
     * Handle view message action.
     * 
     * @param message The message to view/reply to
     */
    private void handleViewMessage(com.group05.greengrocer.model.Message message) {
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
                new Label("Reply:"), replyArea);

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
        final String[] couponNameRef = new String[1]; // To store couponName outside dialog
        dialog.setTitle("Create Coupon");
        dialog.setHeaderText("Create a new discount coupon");

        ComboBox<Object> customerComboBox = new ComboBox<>();
        customerComboBox.getItems().add("All Customers");
        customerComboBox.getItems().addAll(ownerService.getAllCustomers());

        customerComboBox.setCellFactory(param -> new ListCell<Object>() {
            @Override
            protected void updateItem(Object item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else if (item instanceof String) {
                    setText((String) item);
                } else if (item instanceof com.group05.greengrocer.model.User) {
                    com.group05.greengrocer.model.User user = (com.group05.greengrocer.model.User) item;
                    setText(user.getFullName() + " (" + user.getUsername() + ")");
                }
            }
        });

        customerComboBox.setButtonCell(new ListCell<Object>() {
            @Override
            protected void updateItem(Object item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else if (item instanceof String) {
                    setText((String) item);
                } else if (item instanceof com.group05.greengrocer.model.User) {
                    com.group05.greengrocer.model.User user = (com.group05.greengrocer.model.User) item;
                    setText(user.getFullName() + " (" + user.getUsername() + ")");
                }
            }
        });

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

        TextField couponNameField = new TextField();
        couponNameField.setPromptText("Coupon Name (optional, e.g., Summer Sale)");

        TextField discountField = new TextField();
        discountField.setPromptText("Discount amount (e.g., 10.00)");

        VBox content = new VBox(10);
        content.setPadding(new Insets(20));
        content.getChildren().addAll(
                new Label("Customer:"), customerComboBox,
                new Label("Coupon Name (optional):"), couponNameField,
                new Label("Coupon Code (auto-generated):"), codeBox,
                new Label("Discount Amount (₺):"), discountField);

        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.setResultConverter(buttonType -> {
            if (buttonType == ButtonType.OK) {
                Object selected = customerComboBox.getValue();
                String couponCode = codeField.getText().trim();
                couponNameRef[0] = couponNameField.getText().trim();

                if (selected == null) {
                    showAlert(Alert.AlertType.ERROR, "Missing Field", "Please select a customer or 'All Customers'.");
                    return null;
                }

                if (couponCode.isEmpty() || discountField.getText().trim().isEmpty()) {
                    showAlert(Alert.AlertType.ERROR, "Missing Field", "Please enter coupon code and discount amount.");
                    return null;
                }

                try {
                    java.math.BigDecimal discount = new java.math.BigDecimal(discountField.getText());
                    if (discount.compareTo(java.math.BigDecimal.ZERO) <= 0) {
                        showAlert(Alert.AlertType.ERROR, "Invalid Input", "Discount amount must be greater than 0.");
                        return null;
                    }

                    int customerId = selected instanceof String ? -1
                            : ((com.group05.greengrocer.model.User) selected).getUserId();
                    return new javafx.util.Pair<>(customerId,
                            new javafx.util.Pair<>(couponCode, discount));
                } catch (NumberFormatException e) {
                    showAlert(Alert.AlertType.ERROR, "Invalid Input", "Please enter a valid discount amount.");
                }
            }
            return null;
        });

        dialog.showAndWait().ifPresent(result -> {
            String couponName = couponNameRef[0] != null && !couponNameRef[0].isEmpty() ? couponNameRef[0] : null;
            int customerId = result.getKey();
            String couponCode = result.getValue().getKey();
            java.math.BigDecimal discount = result.getValue().getValue();

            if (customerId == -1) {
                List<com.group05.greengrocer.model.User> allCustomers = ownerService.getAllCustomers();
                int successCount = 0;
                for (com.group05.greengrocer.model.User customer : allCustomers) {
                    String uniqueCode = generateUniqueCouponCode();
                    if (ownerService.createCoupon(customer.getUserId(), uniqueCode, discount, null, couponName)) {
                        successCount++;
                    }
                }
                if (successCount > 0) {
                    showAlert(Alert.AlertType.INFORMATION, "Success",
                            "Coupons created successfully for " + successCount + " customer(s)!");
                    loadCoupons();
                } else {
                    showAlert(Alert.AlertType.ERROR, "Error", "Failed to create coupons.");
                }
            } else {
                if (ownerService.createCoupon(customerId, couponCode, discount, null, couponName)) {
                    showAlert(Alert.AlertType.INFORMATION, "Success", "Coupon created successfully!");
                    loadCoupons();
                } else {
                    showAlert(Alert.AlertType.ERROR, "Error", "Failed to create coupon.");
                }
            }
        });
    }

    /**
     * Generate unique coupon code (6-8 digits).
     * 
     * @return A unique coupon code
     */
    private String generateUniqueCouponCode() {
        java.util.Random random = new java.util.Random();
        int maxAttempts = 100;
        int attempts = 0;

        while (attempts < maxAttempts) {
            int codeLength = 6 + random.nextInt(3);
            int min = (int) Math.pow(10, codeLength - 1);
            int max = (int) Math.pow(10, codeLength) - 1;
            int codeNumber = min + random.nextInt(max - min + 1);
            String code = String.valueOf(codeNumber);

            if (isCouponCodeUnique(code)) {
                return code;
            }
            attempts++;
        }

        return String.valueOf(System.currentTimeMillis() % 100000000);
    }

    /**
     * Check if coupon code is unique.
     * 
     * @param code The coupon code to check
     * @return true if the code is unique, false otherwise
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
        Dialog<javafx.util.Pair<Integer, java.math.BigDecimal>> dialog = new Dialog<>();
        dialog.setTitle("Adjust Loyalty Standards");
        dialog.setHeaderText("Configure Loyalty Discount Settings");

        TextField thresholdField = new TextField(
                String.valueOf(com.group05.greengrocer.service.LoyaltyService.getLoyaltyThreshold()));
        thresholdField.setPromptText("Minimum completed orders (e.g., 5)");

        TextField discountPercentField = new TextField(
                String.valueOf(com.group05.greengrocer.service.LoyaltyService.getLoyaltyDiscountPercent()));
        discountPercentField.setPromptText("Discount percentage (e.g., 5.00)");

        VBox content = new VBox(10);
        content.setPadding(new Insets(20));
        content.getChildren().addAll(
                new Label("Current Settings:"),
                new Label("Threshold: " + com.group05.greengrocer.service.LoyaltyService.getLoyaltyThreshold()
                        + " orders"),
                new Label("Discount: " + com.group05.greengrocer.service.LoyaltyService.getLoyaltyDiscountPercent()
                        + "%"),
                new javafx.scene.control.Separator(),
                new Label("New Threshold (minimum completed orders):"),
                thresholdField,
                new Label("New Discount Percentage:"),
                discountPercentField);

        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.setResultConverter(buttonType -> {
            if (buttonType == ButtonType.OK) {
                try {
                    int threshold = Integer.parseInt(thresholdField.getText().trim());
                    java.math.BigDecimal discountPercent = new java.math.BigDecimal(
                            discountPercentField.getText().trim());

                    if (threshold < 0) {
                        showAlert(Alert.AlertType.ERROR, "Invalid Input", "Threshold cannot be negative.");
                        return null;
                    }
                    if (discountPercent.compareTo(java.math.BigDecimal.ZERO) < 0
                            || discountPercent.compareTo(new java.math.BigDecimal("100")) > 0) {
                        showAlert(Alert.AlertType.ERROR, "Invalid Input",
                                "Discount percentage must be between 0 and 100.");
                        return null;
                    }

                    return new javafx.util.Pair<>(threshold, discountPercent);
                } catch (NumberFormatException e) {
                    showAlert(Alert.AlertType.ERROR, "Invalid Input", "Please enter valid numbers.");
                }
            }
            return null;
        });

        dialog.showAndWait().ifPresent(result -> {
            com.group05.greengrocer.service.LoyaltyService.setLoyaltyThreshold(result.getKey());
            com.group05.greengrocer.service.LoyaltyService.setLoyaltyDiscountPercent(result.getValue());
            showAlert(Alert.AlertType.INFORMATION, "Success",
                    "Loyalty standards updated!\n" +
                            "New threshold: " + result.getKey() + " orders\n" +
                            "New discount: " + result.getValue() + "%");
            loadLoyaltyStandards();
        });
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
}
