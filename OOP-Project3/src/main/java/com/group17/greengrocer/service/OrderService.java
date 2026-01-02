package com.group17.greengrocer.service;

import com.group17.greengrocer.model.Order;
import com.group17.greengrocer.model.OrderItem;
import com.group17.greengrocer.model.Product;
import com.group17.greengrocer.repository.OrderItemRepository;
import com.group17.greengrocer.repository.OrderRepository;
import com.group17.greengrocer.repository.ProductRepository;
import com.group17.greengrocer.util.Session;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Service class for order business logic.
 * Enforces business rules like threshold pricing, delivery time validation, etc.
 */
public class OrderService {
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final ProductRepository productRepository;
    private final Session session;
    
    // Business rule: Delivery must be within 48 hours
    private static final int MAX_DELIVERY_HOURS = 48;
    
    public OrderService() {
        this.orderRepository = new OrderRepository();
        this.orderItemRepository = new OrderItemRepository();
        this.productRepository = new ProductRepository();
        this.session = Session.getInstance();
    }
    
    /**
     * Calculate price for a product based on stock and threshold rule
     * Rule: If stock <= threshold, price doubles
     * 
     * @param product The product to calculate price for
     * @return The unit price (doubled if stock <= threshold, otherwise base price)
     */
    public BigDecimal calculateItemPrice(Product product) {
        if (product == null || product.getPricePerKg() == null) {
            return BigDecimal.ZERO;
        }
        
        BigDecimal basePrice = product.getPricePerKg();
        BigDecimal stock = product.getStock();
        BigDecimal threshold = product.getThreshold();
        
        // If threshold is null, use default threshold of 5.0
        if (threshold == null) {
            threshold = new BigDecimal("5.0");
        }
        
        // If stock is null or zero, return base price
        if (stock == null || stock.compareTo(BigDecimal.ZERO) <= 0) {
            return basePrice;
        }
        
        // If stock <= threshold, price doubles
        if (stock.compareTo(threshold) <= 0) {
            return basePrice.multiply(new BigDecimal("2"));
        }
        
        return basePrice;
    }
    
    /**
     * Calculate total cost for order items
     */
    public BigDecimal calculateTotalCost(List<OrderItem> items) {
        BigDecimal total = BigDecimal.ZERO;
        for (OrderItem item : items) {
            total = total.add(item.getSubtotal());
        }
        return total;
    }
    
    /**
     * Validate delivery date is within 48 hours
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
     * Create a new order with items
     */
    public boolean createOrder(Order order, List<OrderItem> items, LocalDateTime deliveryDate) {
        // Validate delivery date
        if (!isValidDeliveryDate(deliveryDate)) {
            throw new IllegalArgumentException("Delivery date must be within 48 hours from now");
        }
        
        // Calculate prices with threshold rule
        for (OrderItem item : items) {
            try {
                Product product = productRepository.findById(item.getProductId());
                if (product == null) {
                    throw new IllegalArgumentException("Product not found: " + item.getProductId());
                }
                
                // Check stock availability
                if (product.getStock().compareTo(item.getQuantity()) < 0) {
                    throw new IllegalArgumentException("Insufficient stock for product: " + product.getProductName());
                }
                
                // Calculate price with threshold rule (stock <= threshold means doubled price)
                BigDecimal unitPrice = calculateItemPrice(product);
                item.setUnitPrice(unitPrice);
                item.setSubtotal(unitPrice.multiply(item.getQuantity()));
            } catch (SQLException e) {
                System.err.println("Error processing order item: " + e.getMessage());
                e.printStackTrace();
                return false;
            }
        }
        
        // Calculate total cost
        BigDecimal totalCost = calculateTotalCost(items);
        order.setTotalCost(totalCost);
        order.setDeliveryDate(deliveryDate);
        order.setOrderDate(LocalDateTime.now());
        
        try {
            // Create order in transaction
            if (orderRepository.create(order)) {
                // Set order ID for items
                for (OrderItem item : items) {
                    item.setOrderId(order.getOrderId());
                }
                
                // Create order items
                if (orderItemRepository.createBatch(items)) {
                    // Update product stock
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
     * Create a new order with all details (subtotal, VAT, discounts)
     */
    public boolean createOrderWithDetails(Order order, List<OrderItem> items, LocalDateTime deliveryDate) {
        // Validate delivery date
        if (!isValidDeliveryDate(deliveryDate)) {
            throw new IllegalArgumentException("Delivery date must be within 48 hours from now");
        }
        
        // Calculate prices with threshold rule
        for (OrderItem item : items) {
            try {
                Product product = productRepository.findById(item.getProductId());
                if (product == null) {
                    throw new IllegalArgumentException("Product not found: " + item.getProductId());
                }
                
                // Check stock availability
                if (product.getStock().compareTo(item.getQuantity()) < 0) {
                    throw new IllegalArgumentException("Insufficient stock for product: " + product.getProductName());
                }
                
                // Calculate price with threshold rule (stock <= threshold means doubled price)
                BigDecimal unitPrice = calculateItemPrice(product);
                item.setUnitPrice(unitPrice);
                item.setSubtotal(unitPrice.multiply(item.getQuantity()));
            } catch (SQLException e) {
                System.err.println("Error processing order item: " + e.getMessage());
                e.printStackTrace();
                return false;
            }
        }
        
        // Order already has subtotal, VAT, discounts, and total set from CartController
        order.setDeliveryDate(deliveryDate);
        if (order.getOrderDate() == null) {
            order.setOrderDate(LocalDateTime.now());
        }
        
        try {
            // Create order in transaction
            if (orderRepository.create(order)) {
                // Set order ID for items
                for (OrderItem item : items) {
                    item.setOrderId(order.getOrderId());
                }
                
                // Create order items
                if (orderItemRepository.createBatch(items)) {
                    // Update product stock
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
     * Get orders for current customer
     */
    public List<Order> getCustomerOrders() {
        int customerId = session.getCurrentUserId();
        try {
            List<Order> orders = orderRepository.findByCustomerId(customerId);
            // Load order items for each order
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
     * Get available orders for carriers
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
     * Get current orders for carrier
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
     * Get completed orders for carrier
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
     * Assign order to carrier (with transaction to prevent multiple assignments)
     */
    public boolean assignOrderToCarrier(int orderId) {
        int carrierId = session.getCurrentUserId();
        try {
            return orderRepository.assignCarrier(orderId, carrierId);
        } catch (SQLException e) {
            System.err.println("Error assigning order: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Mark order as completed
     */
    public boolean markOrderAsCompleted(int orderId) {
        try {
            return orderRepository.markAsCompleted(orderId);
        } catch (SQLException e) {
            System.err.println("Error completing order: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Mark order as completed with specific delivery date
     */
    public boolean markOrderAsCompletedWithDate(int orderId, LocalDateTime deliveryDate) {
        try {
            return orderRepository.markAsCompletedWithDate(orderId, deliveryDate);
        } catch (SQLException e) {
            System.err.println("Error completing order: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Cancel order by carrier (return to Pending status so other carriers can pick it up)
     * Only allows cancellation if order is assigned to the current carrier
     */
    public boolean cancelOrderByCarrier(int orderId) {
        int carrierId = session.getCurrentUserId();
        try {
            return orderRepository.cancelOrderByCarrier(orderId, carrierId);
        } catch (SQLException e) {
            System.err.println("Error canceling order: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Get order by ID with items
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
     * Save invoice path for order
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
     * Get all orders (for owner)
     */
    public List<Order> getAllOrders() {
        try {
            List<Order> orders = orderRepository.findAll();
            // Load order items for each order
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
     * Get all delivered orders (for owner reports)
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
     * Cancel order by customer (within cancellation time frame)
     */
    public boolean cancelOrderByCustomer(int orderId) {
        int customerId = session.getCurrentUserId();
        try {
            return orderRepository.cancelOrderByCustomer(orderId, customerId);
        } catch (SQLException e) {
            System.err.println("Error canceling order: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Generate PDF invoice for order
     * Returns the file path where invoice is saved
     */
    public String generateInvoicePDF(Order order) {
        // Simplified PDF generation - in production, use a library like Apache PDFBox or iText
        try {
            String invoiceDir = "invoices";
            java.io.File dir = new java.io.File(invoiceDir);
            if (!dir.exists()) {
                dir.mkdirs();
            }
            
            String invoicePath = invoiceDir + "/order_" + order.getOrderId() + ".pdf";
            java.io.File invoiceFile = new java.io.File(invoicePath);
            
            // Create a simple text-based invoice (in production, generate actual PDF)
            StringBuilder invoiceContent = new StringBuilder();
            invoiceContent.append("Group17 GreenGrocer - Invoice\n");
            invoiceContent.append("================================\n\n");
            invoiceContent.append("Order ID: ").append(order.getOrderId()).append("\n");
            invoiceContent.append("Order Date: ").append(order.getOrderDate()).append("\n");
            invoiceContent.append("Delivery Date: ").append(order.getDeliveryDate()).append("\n");
            invoiceContent.append("Delivery Address: ").append(order.getDeliveryAddress()).append("\n\n");
            invoiceContent.append("Items:\n");
            invoiceContent.append("--------------------------------\n");
            
            for (OrderItem item : order.getItems()) {
                invoiceContent.append(item.getProduct().getProductName())
                    .append(" - ").append(item.getQuantity()).append(" kg x ₺")
                    .append(item.getUnitPrice()).append(" = ₺").append(item.getSubtotal()).append("\n");
            }
            
            invoiceContent.append("\n--------------------------------\n");
            invoiceContent.append("Subtotal: ₺").append(order.getSubtotal()).append("\n");
            invoiceContent.append("VAT (20%): ₺").append(order.getVatAmount()).append("\n");
            if (order.getDiscountAmount().compareTo(BigDecimal.ZERO) > 0) {
                invoiceContent.append("Coupon Discount: -₺").append(order.getDiscountAmount()).append("\n");
            }
            if (order.getLoyaltyDiscount().compareTo(BigDecimal.ZERO) > 0) {
                invoiceContent.append("Loyalty Discount: -₺").append(order.getLoyaltyDiscount()).append("\n");
            }
            invoiceContent.append("Total: ₺").append(order.getTotalCost()).append("\n");
            
            // Write to file (as text for now - in production, generate actual PDF)
            try (java.io.FileWriter writer = new java.io.FileWriter(invoiceFile)) {
                writer.write(invoiceContent.toString());
            }
            
            return invoicePath;
        } catch (Exception e) {
            System.err.println("Error generating invoice: " + e.getMessage());
            e.printStackTrace();
            return "invoices/order_" + order.getOrderId() + ".pdf";
        }
    }
}








