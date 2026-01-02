package com.group17.greengrocer.controller;

import com.group17.greengrocer.model.Order;
import com.group17.greengrocer.model.User;
import com.group17.greengrocer.repository.UserRepository;
import com.group17.greengrocer.service.AuthService;
import com.group17.greengrocer.service.OrderService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ResourceBundle;

/**
 * Controller for the carrier view.
 */
public class CarrierController implements Initializable {
    @FXML
    private Label welcomeLabel;

    @FXML
    private Button logoutButton;

    @FXML
    private TabPane mainTabPane;

    // Available Orders Table
    @FXML
    private TableView<Order> availableOrdersTable;

    @FXML
    private TableColumn<Order, Integer> orderIdColumn1;

    @FXML
    private TableColumn<Order, String> customerNameColumn1;

    @FXML
    private TableColumn<Order, Double> totalCostColumn1;

    @FXML
    private TableColumn<Order, String> deliveryAddressColumn1;

    @FXML
    private TableColumn<Order, String> orderDateColumn1;

    @FXML
    private TableColumn<Order, String> requestedDeliveryDateColumn;

    @FXML
    private TableColumn<Order, Void> selectColumn;

    @FXML
    private Button selectMultipleButton;

    // Current Orders Table
    @FXML
    private TableView<Order> currentOrdersTable;

    @FXML
    private TableColumn<Order, Integer> orderIdColumn2;

    @FXML
    private TableColumn<Order, String> customerNameColumn2;

    @FXML
    private TableColumn<Order, Double> totalCostColumn2;

    @FXML
    private TableColumn<Order, String> deliveryAddressColumn2;

    @FXML
    private TableColumn<Order, String> orderDateColumn2;

    @FXML
    private TableColumn<Order, Void> completeColumn;

    // Completed Orders Table
    @FXML
    private TableView<Order> completedOrdersTable;

    @FXML
    private TableColumn<Order, Integer> orderIdColumn3;

    @FXML
    private TableColumn<Order, String> customerNameColumn3;

    @FXML
    private TableColumn<Order, Double> totalCostColumn3;

    @FXML
    private TableColumn<Order, String> deliveryAddressColumn3;

    @FXML
    private TableColumn<Order, String> deliveryDateColumn;

    private OrderService orderService;
    private AuthService authService;
    private UserRepository userRepository;

    private ObservableList<Order> availableOrders;
    private ObservableList<Order> currentOrders;
    private ObservableList<Order> completedOrders;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        orderService = new OrderService();
        authService = new AuthService();
        userRepository = new UserRepository();

        availableOrders = FXCollections.observableArrayList();
        currentOrders = FXCollections.observableArrayList();
        completedOrders = FXCollections.observableArrayList();

        // Set welcome message
        if (authService.getCurrentUser() != null) {
            welcomeLabel.setText("Welcome, " + authService.getCurrentUser().getFullName());
        }

        setupTables();
        loadData();

        // Disable tab closing - make all tabs non-closable
        if (mainTabPane != null) {
            for (Tab tab : mainTabPane.getTabs()) {
                tab.setClosable(false);
            }
        }

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
     * Setup all tables
     */
    private void setupTables() {
        // Available Orders Table
        orderIdColumn1.setCellValueFactory(new PropertyValueFactory<>("orderId"));
        customerNameColumn1.setCellValueFactory(cellData -> {
            try {
                User customer = userRepository.findById(cellData.getValue().getCustomerId());
                return new javafx.beans.property.SimpleStringProperty(
                        customer != null ? customer.getFullName() : "Unknown");
            } catch (Exception e) {
                return new javafx.beans.property.SimpleStringProperty("Unknown");
            }
        });
        totalCostColumn1.setCellValueFactory(cellData -> new javafx.beans.property.SimpleDoubleProperty(
                cellData.getValue().getTotalCost().doubleValue()).asObject());
        deliveryAddressColumn1.setCellValueFactory(new PropertyValueFactory<>("deliveryAddress"));
        orderDateColumn1.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(
                cellData.getValue().getOrderDate() != null ? cellData.getValue().getOrderDate().toString() : ""));
        requestedDeliveryDateColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(
                cellData.getValue().getDeliveryDate() != null ? cellData.getValue().getDeliveryDate().toString()
                        : "Not set"));

        selectColumn.setCellFactory(param -> new TableCell<Order, Void>() {
            private final HBox buttonBox = new HBox(5);
            private final Button viewDetailsButton = new Button("View Details");
            private final Button selectButton = new Button("Select");

            {
                viewDetailsButton.setOnAction(event -> {
                    Order order = getTableView().getItems().get(getIndex());
                    handleViewOrderDetails(order);
                });
                selectButton.setOnAction(event -> {
                    Order order = getTableView().getItems().get(getIndex());
                    handleSelectOrder(order);
                });
                buttonBox.getChildren().addAll(viewDetailsButton, selectButton);
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(buttonBox);
                }
            }
        });

        // Enable multiple selection for available orders
        availableOrdersTable.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        availableOrdersTable.setItems(availableOrders);

        // Current Orders Table
        orderIdColumn2.setCellValueFactory(new PropertyValueFactory<>("orderId"));
        customerNameColumn2.setCellValueFactory(cellData -> {
            try {
                User customer = userRepository.findById(cellData.getValue().getCustomerId());
                return new javafx.beans.property.SimpleStringProperty(
                        customer != null ? customer.getFullName() : "Unknown");
            } catch (Exception e) {
                return new javafx.beans.property.SimpleStringProperty("Unknown");
            }
        });
        totalCostColumn2.setCellValueFactory(cellData -> new javafx.beans.property.SimpleDoubleProperty(
                cellData.getValue().getTotalCost().doubleValue()).asObject());
        deliveryAddressColumn2.setCellValueFactory(new PropertyValueFactory<>("deliveryAddress"));
        orderDateColumn2.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(
                cellData.getValue().getOrderDate() != null ? cellData.getValue().getOrderDate().toString() : ""));

        completeColumn.setCellFactory(param -> new TableCell<Order, Void>() {
            private final HBox buttonBox = new HBox(5);
            private final Button completeButton = new Button("Complete");
            private final Button cancelButton = new Button("Cancel");

            {
                // Set button sizes
                completeButton.setPrefWidth(80);
                completeButton.setMinWidth(80);
                cancelButton.setPrefWidth(80);
                cancelButton.setMinWidth(80);

                // Set button box alignment
                buttonBox.setAlignment(javafx.geometry.Pos.CENTER);

                buttonBox.getChildren().addAll(completeButton, cancelButton);
                completeButton.setOnAction(event -> {
                    Order order = getTableView().getItems().get(getIndex());
                    handleCompleteOrder(order);
                });
                cancelButton.setOnAction(event -> {
                    Order order = getTableView().getItems().get(getIndex());
                    handleCancelOrder(order);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(buttonBox);
                }
            }
        });

        currentOrdersTable.setItems(currentOrders);

        // Completed Orders Table
        orderIdColumn3.setCellValueFactory(new PropertyValueFactory<>("orderId"));
        customerNameColumn3.setCellValueFactory(cellData -> {
            try {
                User customer = userRepository.findById(cellData.getValue().getCustomerId());
                return new javafx.beans.property.SimpleStringProperty(
                        customer != null ? customer.getFullName() : "Unknown");
            } catch (Exception e) {
                return new javafx.beans.property.SimpleStringProperty("Unknown");
            }
        });
        totalCostColumn3.setCellValueFactory(cellData -> new javafx.beans.property.SimpleDoubleProperty(
                cellData.getValue().getTotalCost().doubleValue()).asObject());
        deliveryAddressColumn3.setCellValueFactory(new PropertyValueFactory<>("deliveryAddress"));
        deliveryDateColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(
                cellData.getValue().getDeliveryDate() != null ? cellData.getValue().getDeliveryDate().toString() : ""));

        completedOrdersTable.setItems(completedOrders);
    }

    /**
     * Load data for all tables
     */
    private void loadData() {
        availableOrders.setAll(orderService.getAvailableOrders());
        currentOrders.setAll(orderService.getCarrierCurrentOrders());
        completedOrders.setAll(orderService.getCarrierCompletedOrders());
    }

    /**
     * Handle select order action
     */
    private void handleSelectOrder(Order order) {
        // Check if delivery date is in the past
        if (order.getDeliveryDate() != null && order.getDeliveryDate().isBefore(java.time.LocalDateTime.now())) {
            showAlert(Alert.AlertType.ERROR, "Cannot Accept Order",
                    "Cannot accept this order because the requested delivery date is in the past.");
            return;
        }

        if (orderService.assignOrderToCarrier(order.getOrderId())) {
            showAlert(Alert.AlertType.INFORMATION, "Success",
                    "Order " + order.getOrderId() + " assigned to you successfully!");
            loadData(); // Refresh data
        } else {
            showAlert(Alert.AlertType.ERROR, "Error",
                    "Failed to assign order. It may have been assigned to another carrier.");
            loadData(); // Refresh to show updated status
        }
    }

    /**
     * Handle view order details action
     */
    private void handleViewOrderDetails(Order order) {
        try {
            // Load order items
            Order fullOrder = orderService.getOrderById(order.getOrderId());
            if (fullOrder == null) {
                showAlert(Alert.AlertType.ERROR, "Error", "Order not found.");
                return;
            }

            Dialog<Void> dialog = new Dialog<>();
            dialog.setTitle("Order Details");
            dialog.setHeaderText("Order ID: " + fullOrder.getOrderId());

            StringBuilder details = new StringBuilder();
            details.append("Customer: ");
            try {
                User customer = userRepository.findById(fullOrder.getCustomerId());
                details.append(customer != null ? customer.getFullName() : "Unknown");
            } catch (Exception e) {
                details.append("Unknown");
            }
            details.append("\n\nDelivery Address: ").append(fullOrder.getDeliveryAddress());
            details.append("\nOrder Date: ").append(fullOrder.getOrderDate());
            details.append("\nRequested Delivery Date: ")
                    .append(fullOrder.getDeliveryDate() != null ? fullOrder.getDeliveryDate() : "Not set");
            details.append("\n\nProducts:\n");
            details.append("--------------------------------\n");

            for (com.group17.greengrocer.model.OrderItem item : fullOrder.getItems()) {
                details.append(item.getProduct().getProductName())
                        .append(" - ").append(item.getQuantity()).append(" kg x ₺")
                        .append(item.getUnitPrice()).append(" = ₺").append(item.getSubtotal()).append("\n");
            }

            details.append("\n--------------------------------\n");
            details.append("Subtotal: ₺")
                    .append(fullOrder.getSubtotal() != null ? fullOrder.getSubtotal() : fullOrder.getTotalCost());
            if (fullOrder.getVatAmount() != null && fullOrder.getVatAmount().compareTo(java.math.BigDecimal.ZERO) > 0) {
                details.append("\nVAT (20%): ₺").append(fullOrder.getVatAmount());
            }
            details.append("\nTotal (with VAT): ₺").append(fullOrder.getTotalCost());

            Label contentLabel = new Label(details.toString());
            contentLabel.setWrapText(true);
            contentLabel.setPrefWidth(500);

            VBox content = new VBox(10);
            content.setPadding(new javafx.geometry.Insets(20));
            content.getChildren().add(contentLabel);

            dialog.getDialogPane().setContent(content);
            dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
            dialog.showAndWait();
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to load order details: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Handle select multiple orders action
     */
    @FXML
    private void handleSelectMultipleOrders() {
        ObservableList<Order> selectedOrders = availableOrdersTable.getSelectionModel().getSelectedItems();
        if (selectedOrders.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "No Selection", "Please select one or more orders to assign.");
            return;
        }

        int successCount = 0;
        int skippedCount = 0;
        for (Order order : selectedOrders) {
            // Check if delivery date is in the past
            if (order.getDeliveryDate() != null && order.getDeliveryDate().isBefore(java.time.LocalDateTime.now())) {
                skippedCount++;
                continue;
            }

            if (orderService.assignOrderToCarrier(order.getOrderId())) {
                successCount++;
            }
        }

        if (skippedCount > 0) {
            showAlert(Alert.AlertType.WARNING, "Some Orders Skipped",
                    skippedCount + " order(s) were skipped because their delivery dates are in the past.");
        }

        if (successCount > 0) {
            showAlert(Alert.AlertType.INFORMATION, "Success",
                    successCount + " order(s) assigned successfully!");
            loadData(); // Refresh data
        } else {
            showAlert(Alert.AlertType.ERROR, "Error",
                    "Failed to assign orders. They may have been assigned to other carriers.");
            loadData(); // Refresh to show updated status
        }
    }

    /**
     * Handle complete order action
     */
    private void handleCompleteOrder(Order order) {
        // Show dialog to enter delivery date
        Dialog<java.time.LocalDateTime> dialog = new Dialog<>();
        dialog.setTitle("Complete Order");
        dialog.setHeaderText("Enter delivery date and time for Order " + order.getOrderId());

        DatePicker datePicker = new DatePicker();
        datePicker.setValue(java.time.LocalDate.now());

        ComboBox<Integer> hourComboBox = new ComboBox<>();
        ComboBox<Integer> minuteComboBox = new ComboBox<>();
        for (int i = 0; i < 24; i++) {
            hourComboBox.getItems().add(i);
        }
        for (int i = 0; i < 60; i += 15) {
            minuteComboBox.getItems().add(i);
        }
        hourComboBox.setValue(java.time.LocalTime.now().getHour());
        minuteComboBox.setValue((java.time.LocalTime.now().getMinute() / 15) * 15);

        VBox content = new VBox(10);
        content.setPadding(new javafx.geometry.Insets(20));
        content.getChildren().addAll(
                new Label("Delivery Date:"), datePicker,
                new Label("Delivery Time:"),
                new HBox(10, new Label("Hour:"), hourComboBox, new Label("Minute:"), minuteComboBox));

        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        // Get OK button and add validation
        javafx.scene.control.Button okButton = (javafx.scene.control.Button) dialog.getDialogPane()
                .lookupButton(ButtonType.OK);
        okButton.addEventFilter(javafx.event.ActionEvent.ACTION, e -> {
            LocalDate date = datePicker.getValue();
            Integer hour = hourComboBox.getValue();
            Integer minute = minuteComboBox.getValue();

            if (date == null || hour == null || minute == null) {
                showAlert(Alert.AlertType.ERROR, "Invalid Input", "Please select date and time.");
                e.consume();
                return;
            }

            LocalDateTime deliveryDateTime = LocalDateTime.of(date, LocalTime.of(hour, minute));

            // Check if delivery date is in the past
            if (deliveryDateTime.isBefore(LocalDateTime.now())) {
                showAlert(Alert.AlertType.ERROR, "Invalid Date",
                        "Delivery date cannot be in the past.");
                e.consume();
                return;
            }
        });

        dialog.setResultConverter(buttonType -> {
            if (buttonType == ButtonType.OK) {
                LocalDate date = datePicker.getValue();
                Integer hour = hourComboBox.getValue();
                Integer minute = minuteComboBox.getValue();

                if (date == null || hour == null || minute == null) {
                    return null;
                }

                LocalDateTime deliveryDateTime = LocalDateTime.of(date, LocalTime.of(hour, minute));

                // Double check (should not happen due to validation above)
                if (deliveryDateTime.isBefore(LocalDateTime.now())) {
                    return null;
                }

                return deliveryDateTime;
            }
            return null;
        });

        dialog.showAndWait().ifPresent(deliveryDateTime -> {
            if (orderService.markOrderAsCompletedWithDate(order.getOrderId(), deliveryDateTime)) {
                showAlert(Alert.AlertType.INFORMATION, "Success",
                        "Order " + order.getOrderId() + " marked as completed!");
                loadData(); // Refresh data
            } else {
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to complete order.");
            }
        });
    }

    /**
     * Handle cancel order action
     */
    private void handleCancelOrder(Order order) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirm Cancel");
        confirm.setHeaderText("Cancel Order");
        confirm.setContentText("Are you sure you want to cancel order " + order.getOrderId() + "?\n" +
                "The order will be returned to available orders and can be picked up by other carriers.");

        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                if (orderService.cancelOrderByCarrier(order.getOrderId())) {
                    showAlert(Alert.AlertType.INFORMATION, "Success",
                            "Order " + order.getOrderId() + " has been cancelled and returned to available orders.");
                    loadData(); // Refresh data
                } else {
                    showAlert(Alert.AlertType.ERROR, "Error",
                            "Failed to cancel order. It may have already been completed or assigned to another carrier.");
                    loadData(); // Refresh to show updated status
                }
            }
        });
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
}
