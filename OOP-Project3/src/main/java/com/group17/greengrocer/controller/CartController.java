package com.group17.greengrocer.controller;

import com.group17.greengrocer.model.Order;
import com.group17.greengrocer.model.OrderItem;
import com.group17.greengrocer.model.Product;
import com.group17.greengrocer.repository.ProductRepository;
import com.group17.greengrocer.service.OrderService;
import com.group17.greengrocer.service.ProductService;
import com.group17.greengrocer.service.CouponService;
import com.group17.greengrocer.service.LoyaltyService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.math.BigDecimal;
import java.net.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Controller for the cart view.
 */
public class CartController implements Initializable {
    @FXML
    private TableView<OrderItem> cartTable;
    
    @FXML
    private TableColumn<OrderItem, String> productNameColumn;
    
    @FXML
    private TableColumn<OrderItem, BigDecimal> quantityColumn;
    
    @FXML
    private TableColumn<OrderItem, BigDecimal> unitPriceColumn;
    
    @FXML
    private TableColumn<OrderItem, BigDecimal> subtotalColumn;
    
    @FXML
    private TableColumn<OrderItem, Void> removeColumn;
    
    @FXML
    private Label subtotalLabel;
    
    @FXML
    private Label vatLabel;
    
    @FXML
    private Label couponDiscountLabel;
    
    @FXML
    private Label loyaltyDiscountLabel;
    
    @FXML
    private Label totalLabel;
    
    @FXML
    private TextField couponCodeField;
    
    @FXML
    private Button applyCouponButton;
    
    @FXML
    private TextField deliveryAddressField;
    
    @FXML
    private DatePicker deliveryDatePicker;
    
    @FXML
    private ComboBox<Integer> deliveryHourComboBox;
    
    @FXML
    private ComboBox<Integer> deliveryMinuteComboBox;
    
    @FXML
    private Label errorLabel;
    
    @FXML
    private Button checkoutButton;
    
    @FXML
    private Button closeButton;
    
    private ObservableList<OrderItem> cartItems;
    private OrderService orderService;
    private ProductRepository productRepository;
    private ProductService productService;
    private CouponService couponService;
    private LoyaltyService loyaltyService;
    
    // Cart calculation values
    private BigDecimal subtotal = BigDecimal.ZERO;
    private BigDecimal vatAmount = BigDecimal.ZERO;
    private BigDecimal couponDiscount = BigDecimal.ZERO;
    private BigDecimal loyaltyDiscount = BigDecimal.ZERO;
    private BigDecimal total = BigDecimal.ZERO;
    private String appliedCouponCode = null;
    
    // Business rules
    private static final BigDecimal MIN_CART_VALUE = new BigDecimal("200.00"); // Minimum 200 TL
    private static final BigDecimal VAT_RATE = new BigDecimal("0.20"); // 20% VAT
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        orderService = new OrderService();
        productRepository = new ProductRepository();
        productService = new ProductService();
        couponService = new CouponService();
        loyaltyService = new LoyaltyService();
        cartItems = FXCollections.observableArrayList();
        
        // Initialize delivery time comboboxes
        for (int i = 0; i < 24; i++) {
            deliveryHourComboBox.getItems().add(i);
        }
        for (int i = 0; i < 60; i += 15) {
            deliveryMinuteComboBox.getItems().add(i);
        }
        
        // Set default values
        deliveryDatePicker.setValue(LocalDate.now().plusDays(1));
        deliveryHourComboBox.setValue(10);
        deliveryMinuteComboBox.setValue(0);
        
        setupTable();
    }
    
    /**
     * Set cart items from customer controller
     */
    public void setCartItems(List<OrderItem> items) {
        // Load product information for each item and update prices based on threshold
        for (OrderItem item : items) {
            try {
                Product product = productRepository.findById(item.getProductId());
                item.setProduct(product);
                
                // Update price based on current stock and threshold rule
                if (product != null) {
                    BigDecimal displayPrice = productService.getDisplayPrice(product);
                    item.setUnitPrice(displayPrice);
                    item.setSubtotal(displayPrice.multiply(item.getQuantity()));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        cartItems.setAll(items);
        updateTotal();
    }
    
    /**
     * Setup table columns
     */
    private void setupTable() {
        productNameColumn.setCellValueFactory(cellData -> {
            Product product = cellData.getValue().getProduct();
            return new javafx.beans.property.SimpleStringProperty(
                product != null ? product.getProductName() : "Unknown");
        });
        
        quantityColumn.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        unitPriceColumn.setCellValueFactory(new PropertyValueFactory<>("unitPrice"));
        subtotalColumn.setCellValueFactory(new PropertyValueFactory<>("subtotal"));
        
        // Remove column with button
        removeColumn.setCellFactory(param -> new TableCell<OrderItem, Void>() {
            private final Button removeButton = new Button("Remove");
            
            {
                removeButton.setOnAction(event -> {
                    OrderItem item = getTableView().getItems().get(getIndex());
                    cartItems.remove(item);
                    updateTotal();
                });
            }
            
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(removeButton);
                }
            }
        });
        
        cartTable.setItems(cartItems);
    }
    
    /**
     * Update total and all cost breakdown labels
     */
    private void updateTotal() {
        // Calculate subtotal
        subtotal = BigDecimal.ZERO;
        for (OrderItem item : cartItems) {
            subtotal = subtotal.add(item.getSubtotal());
        }
        
        // Calculate VAT (20% of subtotal)
        vatAmount = subtotal.multiply(VAT_RATE);
        
        // Calculate loyalty discount (configurable percentage if customer meets threshold)
        int completedOrdersCount = loyaltyService.getCompletedOrdersCount(
            com.group17.greengrocer.util.Session.getInstance().getCurrentUserId());
        loyaltyDiscount = BigDecimal.ZERO;
        int threshold = com.group17.greengrocer.service.LoyaltyService.getLoyaltyThreshold();
        BigDecimal discountPercent = com.group17.greengrocer.service.LoyaltyService.getLoyaltyDiscountPercent();
        if (completedOrdersCount >= threshold) {
            // Configurable loyalty discount
            loyaltyDiscount = subtotal.multiply(discountPercent).divide(new BigDecimal("100"), 2, java.math.RoundingMode.HALF_UP);
        }
        
        // Calculate total (subtotal + VAT - discounts)
        total = subtotal.add(vatAmount).subtract(couponDiscount).subtract(loyaltyDiscount);
        
        // Update labels
        subtotalLabel.setText("₺" + subtotal.setScale(2, java.math.RoundingMode.HALF_UP));
        vatLabel.setText("₺" + vatAmount.setScale(2, java.math.RoundingMode.HALF_UP));
        couponDiscountLabel.setText(couponDiscount.compareTo(BigDecimal.ZERO) > 0 ? 
            "-₺" + couponDiscount.setScale(2, java.math.RoundingMode.HALF_UP) : "₺0.00");
        loyaltyDiscountLabel.setText(loyaltyDiscount.compareTo(BigDecimal.ZERO) > 0 ? 
            "-₺" + loyaltyDiscount.setScale(2, java.math.RoundingMode.HALF_UP) : "₺0.00");
        totalLabel.setText("₺" + total.setScale(2, java.math.RoundingMode.HALF_UP));
    }
    
    /**
     * Handle apply coupon action
     */
    @FXML
    private void handleApplyCoupon() {
        String couponCode = couponCodeField.getText().trim();
        if (couponCode.isEmpty()) {
            showError("Please enter a coupon code.");
            return;
        }
        
        int customerId = com.group17.greengrocer.util.Session.getInstance().getCurrentUserId();
        BigDecimal discount = couponService.applyCoupon(couponCode, customerId, subtotal);
        
        if (discount != null && discount.compareTo(BigDecimal.ZERO) > 0) {
            couponDiscount = discount;
            appliedCouponCode = couponCode;
            updateTotal();
            showAlert(Alert.AlertType.INFORMATION, "Coupon Applied", 
                "Coupon code applied successfully! Discount: ₺" + discount.setScale(2, java.math.RoundingMode.HALF_UP));
        } else {
            showError("Invalid or expired coupon code.");
        }
    }
    
    /**
     * Handle checkout action
     */
    @FXML
    private void handleCheckout() {
        errorLabel.setVisible(false);
        
        // Validate minimum cart value (200 TL)
        if (total.compareTo(MIN_CART_VALUE) < 0) {
            showError("Minimum sepet tutarı ₺200.00'dir. Lütfen sepetinize daha fazla ürün ekleyin.");
            return;
        }
        
        // Validate delivery address
        String address = deliveryAddressField.getText().trim();
        if (address.isEmpty()) {
            showError("Please enter delivery address.");
            return;
        }
        
        // Validate delivery date and time
        LocalDate date = deliveryDatePicker.getValue();
        Integer hour = deliveryHourComboBox.getValue();
        Integer minute = deliveryMinuteComboBox.getValue();
        
        if (date == null || hour == null || minute == null) {
            showError("Please select delivery date and time.");
            return;
        }
        
        LocalDateTime deliveryDateTime = LocalDateTime.of(date, LocalTime.of(hour, minute));
        
        // Validate delivery is within 48 hours
        if (!orderService.isValidDeliveryDate(deliveryDateTime)) {
            showError("Delivery date must be within 48 hours from now.");
            return;
        }
        
        // Show order summary before finalizing
        if (!showOrderSummary(address, deliveryDateTime)) {
            return; // User cancelled
        }
        
        // Create order
        Order order = new Order();
        order.setCustomerId(com.group17.greengrocer.util.Session.getInstance().getCurrentUserId());
        order.setDeliveryAddress(address);
        order.setStatus("Pending");
        order.setSubtotal(subtotal);
        order.setVatAmount(vatAmount);
        order.setDiscountAmount(couponDiscount);
        order.setLoyaltyDiscount(loyaltyDiscount);
        order.setTotalCost(total);
        order.setCouponCode(appliedCouponCode);
        // Allow cancellation within 2 hours
        order.setCanCancelUntil(LocalDateTime.now().plusHours(2));
        
        List<OrderItem> items = new ArrayList<>(cartItems);
        order.setItems(items); // Set items for invoice generation
        
        try {
            if (orderService.createOrderWithDetails(order, items, deliveryDateTime)) {
                // Mark coupon as used if applied
                if (appliedCouponCode != null) {
                    couponService.markCouponAsUsed(appliedCouponCode, 
                        com.group17.greengrocer.util.Session.getInstance().getCurrentUserId());
                }
                
                // Generate PDF invoice and save to database
                byte[] pdfBytes = orderService.generateInvoicePDF(order);
                if (pdfBytes != null) {
                    orderService.saveInvoicePDF(order.getOrderId(), pdfBytes);
                    // Also save to file for easy access
                    String invoicePath = "invoices/order_" + order.getOrderId() + ".pdf";
                    java.io.File dir = new java.io.File("invoices");
                    if (!dir.exists()) dir.mkdirs();
                    try (java.io.FileOutputStream fos = new java.io.FileOutputStream(invoicePath)) {
                        fos.write(pdfBytes);
                    } catch (java.io.IOException e) {
                        System.err.println("Error saving invoice file: " + e.getMessage());
                        e.printStackTrace();
                    }
                    orderService.saveInvoicePath(order.getOrderId(), invoicePath);
                    showInvoiceDownload(order.getOrderId(), invoicePath);
                } else {
                    showAlert(Alert.AlertType.ERROR, "Error", "Failed to generate invoice PDF.");
                }
                
                showAlert(Alert.AlertType.INFORMATION, "Success", 
                    "Order placed successfully! Order ID: " + order.getOrderId());
                handleClose();
            } else {
                showError("Failed to create order. Please try again.");
            }
        } catch (IllegalArgumentException e) {
            showError(e.getMessage());
        }
    }
    
    /**
     * Show order summary dialog before finalizing purchase
     */
    private boolean showOrderSummary(String address, LocalDateTime deliveryDateTime) {
        Alert summaryAlert = new Alert(Alert.AlertType.CONFIRMATION);
        summaryAlert.setTitle("Order Summary");
        summaryAlert.setHeaderText("Please review your order before finalizing:");
        
        StringBuilder summary = new StringBuilder();
        summary.append("Items:\n");
        for (OrderItem item : cartItems) {
            summary.append("  - ").append(item.getProduct().getProductName())
                   .append(": ").append(item.getQuantity()).append(" kg x ₺")
                   .append(item.getUnitPrice()).append(" = ₺").append(item.getSubtotal()).append("\n");
        }
        summary.append("\nSubtotal: ₺").append(subtotal.setScale(2, java.math.RoundingMode.HALF_UP));
        summary.append("\nVAT (20%): ₺").append(vatAmount.setScale(2, java.math.RoundingMode.HALF_UP));
        if (couponDiscount.compareTo(BigDecimal.ZERO) > 0) {
            summary.append("\nCoupon Discount: -₺").append(couponDiscount.setScale(2, java.math.RoundingMode.HALF_UP));
        }
        if (loyaltyDiscount.compareTo(BigDecimal.ZERO) > 0) {
            summary.append("\nLoyalty Discount (5%): -₺").append(loyaltyDiscount.setScale(2, java.math.RoundingMode.HALF_UP));
        }
        summary.append("\n\nTotal: ₺").append(total.setScale(2, java.math.RoundingMode.HALF_UP));
        summary.append("\n\nDelivery Address: ").append(address);
        summary.append("\nDelivery Date & Time: ").append(deliveryDateTime);
        
        summaryAlert.setContentText(summary.toString());
        
        return summaryAlert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK;
    }
    
    /**
     * Show invoice download dialog
     */
    private void showInvoiceDownload(int orderId, String invoicePath) {
        Alert invoiceAlert = new Alert(Alert.AlertType.INFORMATION);
        invoiceAlert.setTitle("Invoice Generated");
        invoiceAlert.setHeaderText("Your invoice has been generated!");
        invoiceAlert.setContentText("Order ID: " + orderId + "\nInvoice saved at: " + invoicePath + 
            "\n\nYou can download it from your order history.");
        invoiceAlert.showAndWait();
    }
    
    /**
     * Handle close action
     */
    @FXML
    private void handleClose() {
        Stage stage = (Stage) closeButton.getScene().getWindow();
        stage.close();
    }
    
    /**
     * Show error message
     */
    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
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

