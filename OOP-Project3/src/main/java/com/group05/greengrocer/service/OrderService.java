package com.group05.greengrocer.service;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;

import com.group05.greengrocer.model.Order;
import com.group05.greengrocer.model.OrderItem;
import com.group05.greengrocer.model.Product;
import com.group05.greengrocer.repository.OrderItemRepository;
import com.group05.greengrocer.repository.OrderRepository;
import com.group05.greengrocer.repository.ProductRepository;
import com.group05.greengrocer.util.Session;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Service class for order business logic.
 * Enforces business rules like threshold pricing, delivery time validation,
 * etc.
 */
public class OrderService {
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final ProductRepository productRepository;
    private final Session session;

    private static final int MAX_DELIVERY_HOURS = 48;

    /**
     * Constructor for OrderService.
     */
    public OrderService() {
        this.orderRepository = new OrderRepository();
        this.orderItemRepository = new OrderItemRepository();
        this.productRepository = new ProductRepository();
        this.session = Session.getInstance();
    }

    /**
     * Calculate price for a product based on stock and threshold rule.
     * Rule: If stock &lt;= threshold, price doubles.
     * 
     * @param product The product to calculate price for
     * @return The unit price (doubled if stock &lt;= threshold, otherwise base
     *         price)
     */
    public BigDecimal calculateItemPrice(Product product) {
        if (product == null || product.getPricePerKg() == null) {
            return BigDecimal.ZERO;
        }

        BigDecimal basePrice = product.getPricePerKg();
        BigDecimal stock = product.getStock();
        BigDecimal threshold = product.getThreshold();

        if (threshold == null) {
            threshold = new BigDecimal("5.0");
        }

        if (stock == null || stock.compareTo(BigDecimal.ZERO) <= 0) {
            return basePrice;
        }

        if (stock.compareTo(threshold) <= 0) {
            return basePrice.multiply(new BigDecimal("2"));
        }

        return basePrice;
    }

    /**
     * Calculate total cost for order items.
     * 
     * @param items The list of order items
     * @return The total cost of all items
     */
    public BigDecimal calculateTotalCost(List<OrderItem> items) {
        BigDecimal total = BigDecimal.ZERO;
        for (OrderItem item : items) {
            total = total.add(item.getSubtotal());
        }
        return total;
    }

    /**
     * Validate delivery date is within 48 hours.
     * 
     * @param deliveryDate The delivery date to validate
     * @return true if delivery date is valid (within 48 hours from now), false
     *         otherwise
     */
    public boolean isValidDeliveryDate(LocalDateTime deliveryDate) {
        if (deliveryDate == null) {
            return false;
        }

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime maxDelivery = now.plusHours(MAX_DELIVERY_HOURS);

        return deliveryDate.isAfter(now) && deliveryDate.isBefore(maxDelivery) || deliveryDate.isEqual(maxDelivery);
    }

    /**
     * Log a transaction event for an order.
     * 
     * @param orderId The order ID
     * @param message The message to log
     */
    private void logTransaction(int orderId, String message) {
        try {
            orderRepository.appendTransactionLog(orderId, message);
        } catch (SQLException e) {
            System.err.println("Failed to log transaction: " + e.getMessage());
        }
    }

    /**
     * Create a new order with items.
     * 
     * @param order        The Order object to create
     * @param items        The list of order items
     * @param deliveryDate The delivery date (must be within 48 hours)
     * @return true if order was created successfully, false otherwise
     * @throws IllegalArgumentException if delivery date is invalid or stock is
     *                                  insufficient
     */
    public boolean createOrder(Order order, List<OrderItem> items, LocalDateTime deliveryDate) {
        if (!isValidDeliveryDate(deliveryDate)) {
            throw new IllegalArgumentException("Delivery date must be within 48 hours from now");
        }

        for (OrderItem item : items) {
            try {
                Product product = productRepository.findById(item.getProductId());
                if (product == null) {
                    throw new IllegalArgumentException("Product not found: " + item.getProductId());
                }

                if (product.getStock().compareTo(item.getQuantity()) < 0) {
                    throw new IllegalArgumentException("Insufficient stock for product: " + product.getProductName());
                }

                BigDecimal unitPrice = calculateItemPrice(product);
                item.setUnitPrice(unitPrice);
                item.setSubtotal(unitPrice.multiply(item.getQuantity()));
            } catch (SQLException e) {
                System.err.println("Error processing order item: " + e.getMessage());
                e.printStackTrace();
                return false;
            }
        }

        BigDecimal totalCost = calculateTotalCost(items);
        order.setTotalCost(totalCost);
        order.setDeliveryDate(deliveryDate);
        order.setOrderDate(LocalDateTime.now());

        try {
            if (orderRepository.create(order)) {
                for (OrderItem item : items) {
                    item.setOrderId(order.getOrderId());
                }

                if (orderItemRepository.createBatch(items)) {
                    for (OrderItem item : items) {
                        try {
                            Product product = productRepository.findById(item.getProductId());
                            BigDecimal newStock = product.getStock().subtract(item.getQuantity());
                            productRepository.updateStock(product.getProductId(), newStock);
                        } catch (SQLException e) {
                            System.err.println("Error updating stock: " + e.getMessage());
                            e.printStackTrace();
                        }
                    }
                    logTransaction(order.getOrderId(), "Order created. Total: " + order.getTotalCost());
                    return true;
                }
            }
        } catch (SQLException e) {
            System.err.println("Error creating order: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Create a new order with all details (subtotal, VAT, discounts).
     * 
     * @param order        The Order object to create with all financial details
     * @param items        The list of order items
     * @param deliveryDate The delivery date (must be within 48 hours)
     * @return true if order was created successfully, false otherwise
     * @throws IllegalArgumentException if delivery date is invalid or stock is
     *                                  insufficient
     */
    public boolean createOrderWithDetails(Order order, List<OrderItem> items, LocalDateTime deliveryDate) {
        if (!isValidDeliveryDate(deliveryDate)) {
            throw new IllegalArgumentException("Delivery date must be within 48 hours from now");
        }

        for (OrderItem item : items) {
            try {
                Product product = productRepository.findById(item.getProductId());
                if (product == null) {
                    throw new IllegalArgumentException("Product not found: " + item.getProductId());
                }

                if (product.getStock().compareTo(item.getQuantity()) < 0) {
                    throw new IllegalArgumentException("Insufficient stock for product: " + product.getProductName());
                }

                BigDecimal unitPrice = calculateItemPrice(product);
                item.setUnitPrice(unitPrice);
                item.setSubtotal(unitPrice.multiply(item.getQuantity()));
            } catch (SQLException e) {
                System.err.println("Error processing order item: " + e.getMessage());
                e.printStackTrace();
                return false;
            }
        }

        order.setDeliveryDate(deliveryDate);
        if (order.getOrderDate() == null) {
            order.setOrderDate(LocalDateTime.now());
        }

        try {
            if (orderRepository.create(order)) {
                for (OrderItem item : items) {
                    item.setOrderId(order.getOrderId());
                }

                if (orderItemRepository.createBatch(items)) {
                    for (OrderItem item : items) {
                        try {
                            Product product = productRepository.findById(item.getProductId());
                            BigDecimal newStock = product.getStock().subtract(item.getQuantity());
                            productRepository.updateStock(product.getProductId(), newStock);
                        } catch (SQLException e) {
                            System.err.println("Error updating stock: " + e.getMessage());
                            e.printStackTrace();
                        }
                    }
                    logTransaction(order.getOrderId(), "Order created with details. Total: " + order.getTotalCost());
                    return true;
                }
            }
        } catch (SQLException e) {
            System.err.println("Error creating order: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Get orders for current customer.
     * 
     * @return List of orders for the current logged-in customer
     */
    public List<Order> getCustomerOrders() {
        int customerId = session.getCurrentUserId();
        try {
            List<Order> orders = orderRepository.findByCustomerId(customerId);
            for (Order order : orders) {
                order.setItems(orderItemRepository.findByOrderId(order.getOrderId()));
            }
            return orders;
        } catch (SQLException e) {
            System.err.println("Error fetching customer orders: " + e.getMessage());
            e.printStackTrace();
            return List.of();
        }
    }

    /**
     * Get available orders for carriers.
     * 
     * @return List of available orders (Pending status, no carrier assigned)
     */
    public List<Order> getAvailableOrders() {
        try {
            return orderRepository.findAvailableOrders();
        } catch (SQLException e) {
            System.err.println("Error fetching available orders: " + e.getMessage());
            e.printStackTrace();
            return List.of();
        }
    }

    /**
     * Get current orders for carrier.
     * 
     * @return List of orders assigned to the current logged-in carrier
     */
    public List<Order> getCarrierCurrentOrders() {
        int carrierId = session.getCurrentUserId();
        try {
            return orderRepository.findByCarrierId(carrierId);
        } catch (SQLException e) {
            System.err.println("Error fetching carrier orders: " + e.getMessage());
            e.printStackTrace();
            return List.of();
        }
    }

    /**
     * Get completed orders for carrier.
     * 
     * @return List of completed orders for the current logged-in carrier
     */
    public List<Order> getCarrierCompletedOrders() {
        int carrierId = session.getCurrentUserId();
        try {
            return orderRepository.findCompletedOrdersByCarrier(carrierId);
        } catch (SQLException e) {
            System.err.println("Error fetching completed orders: " + e.getMessage());
            e.printStackTrace();
            return List.of();
        }
    }

    /**
     * Assign order to carrier (with transaction to prevent multiple assignments).
     * 
     * @param orderId The order ID to assign
     * @return true if order was assigned successfully, false otherwise
     */
    public boolean assignOrderToCarrier(int orderId) {
        int carrierId = session.getCurrentUserId();
        try {
            boolean success = orderRepository.assignCarrier(orderId, carrierId);
            if (success) {
                logTransaction(orderId, "Carrier assigned. Carrier ID: " + carrierId);
            }
            return success;
        } catch (SQLException e) {
            System.err.println("Error assigning order: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Mark order as completed.
     * 
     * @param orderId The order ID to mark as completed
     * @return true if order was marked as completed successfully, false otherwise
     */
    public boolean markOrderAsCompleted(int orderId) {
        try {
            boolean success = orderRepository.markAsCompleted(orderId);
            if (success) {
                logTransaction(orderId, "Order delivered.");
            }
            return success;
        } catch (SQLException e) {
            System.err.println("Error completing order: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Mark order as completed with specific delivery date.
     * 
     * @param orderId      The order ID to mark as completed
     * @param deliveryDate The delivery date to set
     * @return true if order was marked as completed successfully, false otherwise
     */
    public boolean markOrderAsCompletedWithDate(int orderId, LocalDateTime deliveryDate) {
        try {
            boolean success = orderRepository.markAsCompletedWithDate(orderId, deliveryDate);
            if (success) {
                logTransaction(orderId, "Order delivered at " + deliveryDate);
            }
            return success;
        } catch (SQLException e) {
            System.err.println("Error completing order: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Cancel order by carrier (return to Pending status so other carriers can pick
     * it up).
     * Only allows cancellation if order is assigned to the current carrier.
     * 
     * @param orderId The order ID to cancel
     * @return true if order was cancelled successfully, false otherwise
     */
    public boolean cancelOrderByCarrier(int orderId) {
        int carrierId = session.getCurrentUserId();
        try {
            boolean success = orderRepository.cancelOrderByCarrier(orderId, carrierId);
            if (success) {
                logTransaction(orderId, "Order dropped by carrier " + carrierId);
            }
            return success;
        } catch (SQLException e) {
            System.err.println("Error canceling order: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Get order by ID with items.
     * 
     * @param orderId The order ID to search for
     * @return The Order object with items if found, null otherwise
     */
    public Order getOrderById(int orderId) {
        try {
            Order order = orderRepository.findById(orderId);
            if (order != null) {
                order.setItems(orderItemRepository.findByOrderId(orderId));
            }
            return order;
        } catch (SQLException e) {
            System.err.println("Error fetching order: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Save invoice PDF to database as CLOB.
     * 
     * @param orderId  The order ID to save the PDF for
     * @param pdfBytes The PDF file as byte array
     * @return true if PDF was saved successfully, false otherwise
     */
    public boolean saveInvoicePDF(int orderId, byte[] pdfBytes) {
        try {
            return orderRepository.saveInvoicePDF(orderId, pdfBytes);
        } catch (SQLException e) {
            System.err.println("Error saving invoice PDF: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Get invoice PDF from database.
     * 
     * @param orderId The order ID to get the PDF for
     * @return The PDF file as byte array, or null if not found
     */
    public byte[] getInvoicePDF(int orderId) {
        try {
            return orderRepository.getInvoicePDF(orderId);
        } catch (SQLException e) {
            System.err.println("Error retrieving invoice PDF: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Save invoice path for order (legacy support).
     * 
     * @param orderId     The order ID to save the path for
     * @param invoicePath The invoice file path
     * @return true if invoice path was saved successfully, false otherwise
     */
    public boolean saveInvoicePath(int orderId, String invoicePath) {
        try {
            return orderRepository.updateInvoicePath(orderId, invoicePath);
        } catch (SQLException e) {
            System.err.println("Error saving invoice path: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Get all orders (for owner).
     * 
     * @return List of all orders with items loaded
     */
    public List<Order> getAllOrders() {
        try {
            List<Order> orders = orderRepository.findAll();
            for (Order order : orders) {
                order.setItems(orderItemRepository.findByOrderId(order.getOrderId()));
            }
            return orders;
        } catch (SQLException e) {
            System.err.println("Error fetching all orders: " + e.getMessage());
            e.printStackTrace();
            return List.of();
        }
    }

    /**
     * Get all delivered orders (for owner reports).
     * 
     * @return List of all delivered orders
     */
    public List<Order> getAllDeliveredOrders() {
        try {
            return orderRepository.findAllDeliveredOrders();
        } catch (SQLException e) {
            System.err.println("Error fetching delivered orders: " + e.getMessage());
            e.printStackTrace();
            return List.of();
        }
    }

    /**
     * Cancel order by customer (within cancellation time frame).
     * 
     * @param orderId The order ID to cancel
     * @return true if order was cancelled successfully, false otherwise
     */
    public boolean cancelOrderByCustomer(int orderId) {
        int customerId = session.getCurrentUserId();
        try {
            boolean success = orderRepository.cancelOrderByCustomer(orderId, customerId);
            if (success) {
                logTransaction(orderId, "Order cancelled by customer.");
            }
            return success;
        } catch (SQLException e) {
            System.err.println("Error canceling order: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Generate PDF invoice for order using Apache PDFBox.
     * Returns the PDF as byte array for database storage.
     * 
     * @param order The Order object to generate invoice for
     * @return The PDF file as byte array
     */
    public byte[] generateInvoicePDF(Order order) {
        System.out.println("Starting PDF generation for Order ID: " + order.getOrderId());
        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage();
            document.addPage(page);

            try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
                float yPosition = 750;
                float margin = 50;
                float lineHeight = 20;

                // Header
                drawText(contentStream, pdFontBold(), 24, margin, yPosition, "Group5 GreenGrocer");

                yPosition -= 30;
                drawText(contentStream, pdFontBold(), 18, margin, yPosition, "INVOICE");

                yPosition -= 40;

                // Order Details
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

                drawText(contentStream, pdFontRegular(), 12, margin, yPosition, "Order ID: " + order.getOrderId());
                yPosition -= lineHeight;

                drawText(contentStream, pdFontRegular(), 12, margin, yPosition, "Order Date: "
                        + (order.getOrderDate() != null ? order.getOrderDate().format(formatter) : "N/A"));
                yPosition -= lineHeight;

                drawText(contentStream, pdFontRegular(), 12, margin, yPosition, "Delivery Date: "
                        + (order.getDeliveryDate() != null ? order.getDeliveryDate().format(formatter) : "N/A"));
                yPosition -= lineHeight;

                drawText(contentStream, pdFontRegular(), 12, margin, yPosition,
                        "Delivery Address: " + order.getDeliveryAddress()); // drawText does sanitization
                yPosition -= 40;

                // Items Header
                drawText(contentStream, pdFontBold(), 12, margin, yPosition, "Items:");
                yPosition -= lineHeight;

                drawText(contentStream, pdFontBold(), 12, margin, yPosition,
                        "----------------------------------------");
                yPosition -= lineHeight;

                // Items Loop
                if (order.getItems() != null) {
                    for (OrderItem item : order.getItems()) {
                        String productName = "Unknown Product";
                        if (item.getProduct() != null && item.getProduct().getProductName() != null) {
                            productName = item.getProduct().getProductName();
                        }

                        BigDecimal qty = item.getQuantity() != null ? item.getQuantity() : BigDecimal.ZERO;
                        BigDecimal price = item.getUnitPrice() != null ? item.getUnitPrice() : BigDecimal.ZERO;
                        BigDecimal sub = item.getSubtotal() != null ? item.getSubtotal() : BigDecimal.ZERO;

                        String itemLine = String.format("%s - %.2f kg x %.2f TL = %.2f TL",
                                productName, // drawText sanitizes
                                qty,
                                price,
                                sub);

                        drawText(contentStream, pdFontRegular(), 10, margin, yPosition, itemLine);

                        yPosition -= lineHeight;
                    }
                }

                yPosition -= 20;

                // Separator
                drawText(contentStream, pdFontBold(), 12, margin, yPosition,
                        "----------------------------------------");
                yPosition -= lineHeight;

                // Totals
                BigDecimal subtotal = order.getSubtotal() != null ? order.getSubtotal() : BigDecimal.ZERO;
                BigDecimal vat = order.getVatAmount() != null ? order.getVatAmount() : BigDecimal.ZERO;
                BigDecimal discount = order.getDiscountAmount() != null ? order.getDiscountAmount() : BigDecimal.ZERO;
                BigDecimal loyalty = order.getLoyaltyDiscount() != null ? order.getLoyaltyDiscount() : BigDecimal.ZERO;
                BigDecimal total = order.getTotalCost() != null ? order.getTotalCost() : BigDecimal.ZERO;

                drawText(contentStream, pdFontRegular(), 12, margin, yPosition,
                        "Subtotal: " + subtotal.setScale(2, java.math.RoundingMode.HALF_UP) + " TL");
                yPosition -= lineHeight;

                drawText(contentStream, pdFontRegular(), 12, margin, yPosition,
                        "VAT (20%): " + vat.setScale(2, java.math.RoundingMode.HALF_UP) + " TL");
                yPosition -= lineHeight;

                if (discount.compareTo(BigDecimal.ZERO) > 0) {
                    drawText(contentStream, pdFontRegular(), 12, margin, yPosition,
                            "Coupon Discount: -" + discount.setScale(2, java.math.RoundingMode.HALF_UP) + " TL");
                    yPosition -= lineHeight;
                }

                if (loyalty.compareTo(BigDecimal.ZERO) > 0) {
                    drawText(contentStream, pdFontRegular(), 12, margin, yPosition,
                            "Loyalty Discount: -" + loyalty.setScale(2, java.math.RoundingMode.HALF_UP) + " TL");
                    yPosition -= lineHeight;
                }

                // Final Total
                drawText(contentStream, pdFontBold(), 14, margin, yPosition,
                        "TOTAL: " + total.setScale(2, java.math.RoundingMode.HALF_UP) + " TL");
            }

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            document.save(baos);
            System.out.println("PDF generation successful.");
            return baos.toByteArray();

        } catch (Throwable e) {
            System.err.println("CRITICAL ERROR generating PDF invoice: " + e.getMessage());
            e.printStackTrace();
            // Return null but don't rethrow to avoid crashing caller if they don't catch
            // Throwable
            return null;
        }
    }

    // Helper methods for Fonts to avoid deprecation warnings if any, or cleaner
    // code
    private org.apache.pdfbox.pdmodel.font.PDFont pdFontBold() {
        return PDType1Font.HELVETICA_BOLD;
    }

    private org.apache.pdfbox.pdmodel.font.PDFont pdFontRegular() {
        return PDType1Font.HELVETICA;
    }

    /**
     * Helper to draw text safely ensuring beginText/endText are matched.
     */
    private void drawText(PDPageContentStream stream, org.apache.pdfbox.pdmodel.font.PDFont font, float fontSize,
            float x, float y, String text) throws java.io.IOException {
        stream.beginText();
        try {
            stream.setFont(font, fontSize);
            stream.newLineAtOffset(x, y);
            stream.showText(sanitizeForPDF(text));
        } catch (Exception e) {
            System.err.println("Error drawing text '" + text + "': " + e.getMessage());
            // We swallow exception here to allow partial PDF generation?
            // Better to rethrow or log, but ensure endText is called.
            // If showText fails, we might as well just not show that line.
        } finally {
            stream.endText();
        }
    }

    /**
     * Sanitizes string for PDFBox WinAnsiEncoding (Standard Fonts).
     * Replaces non-ASCII characters with nearest ASCII equivalents.
     */
    private String sanitizeForPDF(String input) {
        if (input == null)
            return "";
        StringBuilder updated = new StringBuilder();
        for (char c : input.toCharArray()) {
            switch (c) {
                // Turkish Chars
                case 'ğ':
                    updated.append('g');
                    break;
                case 'Ğ':
                    updated.append('G');
                    break;
                case 'ü':
                    updated.append('u');
                    break;
                case 'Ü':
                    updated.append('U');
                    break;
                case 'ş':
                    updated.append('s');
                    break;
                case 'Ş':
                    updated.append('S');
                    break;
                case 'ı':
                    updated.append('i');
                    break;
                case 'İ':
                    updated.append('I');
                    break;
                case 'ö':
                    updated.append('o');
                    break;
                case 'Ö':
                    updated.append('O');
                    break;
                case 'ç':
                    updated.append('c');
                    break;
                case 'Ç':
                    updated.append('C');
                    break;
                // Other common chars
                case '₺':
                    updated.append("TL");
                    break;
                case '€':
                    updated.append("EUR");
                    break;
                case '$':
                    updated.append("USD");
                    break;
                // Default
                default:
                    // Only append if it's in printable ASCII range or space
                    if ((c >= 32 && c <= 126)) {
                        updated.append(c);
                    } else {
                        updated.append('?'); // Placeholder for unsupported
                    }
                    break;
            }
        }
        return updated.toString();
    }
}
