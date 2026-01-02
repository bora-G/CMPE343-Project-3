package com.group17.greengrocer.repository;

import com.group17.greengrocer.model.Order;
import com.group17.greengrocer.util.DatabaseAdapter;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Repository class for Order database operations.
 * Handles all database access related to orders.
 */
public class OrderRepository {
    private final DatabaseAdapter dbAdapter;
    
    public OrderRepository() {
        this.dbAdapter = DatabaseAdapter.getInstance();
    }
    
    /**
     * Find order by ID
     */
    public Order findById(int orderId) throws SQLException {
        String sql = "SELECT * FROM OrderInfo WHERE orderId = ?";
        
        try (Connection conn = dbAdapter.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, orderId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToOrder(rs);
                }
            }
        }
        return null;
    }
    
    /**
     * Get all orders for a customer
     */
    public List<Order> findByCustomerId(int customerId) throws SQLException {
        String sql = "SELECT * FROM OrderInfo WHERE customerId = ? ORDER BY orderDate DESC";
        List<Order> orders = new ArrayList<>();
        
        try (Connection conn = dbAdapter.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, customerId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    orders.add(mapResultSetToOrder(rs));
                }
            }
        }
        return orders;
    }
    
    /**
     * Get all orders (for owner)
     */
    public List<Order> findAll() throws SQLException {
        String sql = "SELECT * FROM OrderInfo ORDER BY orderDate DESC";
        List<Order> orders = new ArrayList<>();
        
        try (Connection conn = dbAdapter.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                orders.add(mapResultSetToOrder(rs));
            }
        }
        return orders;
    }
    
    /**
     * Get all available orders (Pending status, no carrier assigned)
     */
    public List<Order> findAvailableOrders() throws SQLException {
        String sql = "SELECT * FROM OrderInfo WHERE status = 'Pending' AND carrierId IS NULL ORDER BY orderDate";
        List<Order> orders = new ArrayList<>();
        
        try (Connection conn = dbAdapter.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                orders.add(mapResultSetToOrder(rs));
            }
        }
        return orders;
    }
    
    /**
     * Get orders assigned to a carrier
     */
    public List<Order> findByCarrierId(int carrierId) throws SQLException {
        String sql = "SELECT * FROM OrderInfo WHERE carrierId = ? AND status IN ('Assigned', 'InTransit') ORDER BY orderDate";
        List<Order> orders = new ArrayList<>();
        
        try (Connection conn = dbAdapter.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, carrierId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    orders.add(mapResultSetToOrder(rs));
                }
            }
        }
        return orders;
    }
    
    /**
     * Get completed orders for a carrier
     */
    public List<Order> findCompletedOrdersByCarrier(int carrierId) throws SQLException {
        String sql = "SELECT * FROM OrderInfo WHERE carrierId = ? AND status = 'Delivered' ORDER BY deliveryDate DESC";
        List<Order> orders = new ArrayList<>();
        
        try (Connection conn = dbAdapter.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, carrierId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    orders.add(mapResultSetToOrder(rs));
                }
            }
        }
        return orders;
    }
    
    /**
     * Get all delivered orders (for owner reports)
     */
    public List<Order> findAllDeliveredOrders() throws SQLException {
        String sql = "SELECT * FROM OrderInfo WHERE status = 'Delivered' ORDER BY deliveryDate DESC";
        List<Order> orders = new ArrayList<>();
        
        try (Connection conn = dbAdapter.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                orders.add(mapResultSetToOrder(rs));
            }
        }
        return orders;
    }
    
    /**
     * Get completed orders count for a customer (for loyalty discount)
     */
    public int getCompletedOrdersCountByCustomer(int customerId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM OrderInfo WHERE customerId = ? AND status = 'Delivered'";
        
        try (Connection conn = dbAdapter.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, customerId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        return 0;
    }
    
    /**
     * Cancel order by customer (within cancellation time frame)
     */
    public boolean cancelOrderByCustomer(int orderId, int customerId) throws SQLException {
        String sql = "UPDATE OrderInfo SET status = 'Cancelled' " +
                     "WHERE orderId = ? AND customerId = ? AND status = 'Pending' " +
                     "AND (canCancelUntil IS NULL OR canCancelUntil > NOW())";
        
        try (Connection conn = dbAdapter.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, orderId);
            stmt.setInt(2, customerId);
            
            return stmt.executeUpdate() > 0;
        }
    }
    
    /**
     * Create a new order
     */
    public boolean create(Order order) throws SQLException {
        String sql = "INSERT INTO OrderInfo (customerId, carrierId, orderDate, deliveryDate, subtotal, vatAmount, " +
                     "discountAmount, loyaltyDiscount, totalCost, status, deliveryAddress, invoicePath, invoiceContent, couponCode, canCancelUntil) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        
        System.out.println("=== OrderRepository.create() called ===");
        System.out.println("Customer ID: " + order.getCustomerId());
        System.out.println("Subtotal: " + order.getSubtotal());
        System.out.println("Total Cost: " + order.getTotalCost());
        System.out.println("Status: " + order.getStatus());
        System.out.println("Delivery Address: " + order.getDeliveryAddress());
        System.out.println("Invoice Path: " + order.getInvoicePath());
        System.out.println("Invoice Content (BLOB): " + (order.getInvoiceContent() != null ? order.getInvoiceContent().length + " bytes" : "null"));
        
        try (Connection conn = dbAdapter.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            stmt.setInt(1, order.getCustomerId());
            if (order.getCarrierId() != null) {
                stmt.setInt(2, order.getCarrierId());
            } else {
                stmt.setNull(2, Types.INTEGER);
            }
            stmt.setTimestamp(3, order.getOrderDate() != null ? Timestamp.valueOf(order.getOrderDate()) : Timestamp.valueOf(LocalDateTime.now()));
            if (order.getDeliveryDate() != null) {
                stmt.setTimestamp(4, Timestamp.valueOf(order.getDeliveryDate()));
            } else {
                stmt.setNull(4, Types.TIMESTAMP);
            }
            stmt.setBigDecimal(5, order.getSubtotal() != null ? order.getSubtotal() : order.getTotalCost());
            stmt.setBigDecimal(6, order.getVatAmount() != null ? order.getVatAmount() : java.math.BigDecimal.ZERO);
            stmt.setBigDecimal(7, order.getDiscountAmount() != null ? order.getDiscountAmount() : java.math.BigDecimal.ZERO);
            stmt.setBigDecimal(8, order.getLoyaltyDiscount() != null ? order.getLoyaltyDiscount() : java.math.BigDecimal.ZERO);
            stmt.setBigDecimal(9, order.getTotalCost());
            stmt.setString(10, order.getStatus());
            stmt.setString(11, order.getDeliveryAddress());
            stmt.setString(12, order.getInvoicePath());
            if (order.getInvoiceContent() != null && order.getInvoiceContent().length > 0) {
                stmt.setBytes(13, order.getInvoiceContent()); // Store PDF as BLOB
                System.out.println("Setting invoiceContent BLOB: " + order.getInvoiceContent().length + " bytes");
            } else {
                stmt.setNull(13, Types.BLOB);
                System.out.println("Setting invoiceContent to NULL");
            }
            stmt.setString(14, order.getCouponCode());
            if (order.getCanCancelUntil() != null) {
                stmt.setTimestamp(15, Timestamp.valueOf(order.getCanCancelUntil()));
            } else {
                stmt.setNull(15, Types.TIMESTAMP);
            }
            
            System.out.println("Executing INSERT query...");
            int rowsAffected = stmt.executeUpdate();
            System.out.println("INSERT executed. Rows affected: " + rowsAffected);
            
            if (rowsAffected > 0) {
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        int orderId = generatedKeys.getInt(1);
                        order.setOrderId(orderId);
                        System.out.println("Order created successfully! Order ID: " + orderId);
                        return true;
                    } else {
                        System.err.println("WARNING: Order inserted but no generated key returned!");
                        return false;
                    }
                }
            } else {
                System.err.println("ERROR: No rows affected by INSERT query!");
                return false;
            }
        } catch (SQLException e) {
            System.err.println("SQLException in OrderRepository.create():");
            System.err.println("  SQL State: " + e.getSQLState());
            System.err.println("  Error Code: " + e.getErrorCode());
            System.err.println("  Message: " + e.getMessage());
            e.printStackTrace();
            throw e; // Re-throw to be caught by service layer
        }
    }
    
    /**
     * Update order information
     */
    public boolean update(Order order) throws SQLException {
        String sql = "UPDATE OrderInfo SET customerId = ?, carrierId = ?, orderDate = ?, deliveryDate = ?, " +
                     "totalCost = ?, status = ?, deliveryAddress = ?, invoicePath = ? WHERE orderId = ?";
        
        try (Connection conn = dbAdapter.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, order.getCustomerId());
            if (order.getCarrierId() != null) {
                stmt.setInt(2, order.getCarrierId());
            } else {
                stmt.setNull(2, Types.INTEGER);
            }
            stmt.setTimestamp(3, order.getOrderDate() != null ? Timestamp.valueOf(order.getOrderDate()) : null);
            if (order.getDeliveryDate() != null) {
                stmt.setTimestamp(4, Timestamp.valueOf(order.getDeliveryDate()));
            } else {
                stmt.setNull(4, Types.TIMESTAMP);
            }
            stmt.setBigDecimal(5, order.getTotalCost());
            stmt.setString(6, order.getStatus());
            stmt.setString(7, order.getDeliveryAddress());
            stmt.setString(8, order.getInvoicePath());
            stmt.setInt(9, order.getOrderId());
            
            return stmt.executeUpdate() > 0;
        }
    }
    
    /**
     * Assign carrier to order (with transaction to prevent multiple assignments)
     */
    public boolean assignCarrier(int orderId, int carrierId) throws SQLException {
        String sql = "UPDATE OrderInfo SET carrierId = ?, status = 'Assigned' " +
                     "WHERE orderId = ? AND status = 'Pending' AND carrierId IS NULL";
        
        try (Connection conn = dbAdapter.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, carrierId);
            stmt.setInt(2, orderId);
            
            return stmt.executeUpdate() > 0;
        }
    }
    
    /**
     * Mark order as completed
     */
    public boolean markAsCompleted(int orderId) throws SQLException {
        String sql = "UPDATE OrderInfo SET status = 'Delivered', deliveryDate = NOW() WHERE orderId = ?";
        
        try (Connection conn = dbAdapter.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, orderId);
            return stmt.executeUpdate() > 0;
        }
    }
    
    /**
     * Mark order as completed with specific delivery date
     */
    public boolean markAsCompletedWithDate(int orderId, LocalDateTime deliveryDate) throws SQLException {
        String sql = "UPDATE OrderInfo SET status = 'Delivered', deliveryDate = ? WHERE orderId = ?";
        
        try (Connection conn = dbAdapter.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setTimestamp(1, Timestamp.valueOf(deliveryDate));
            stmt.setInt(2, orderId);
            return stmt.executeUpdate() > 0;
        }
    }
    
    /**
     * Cancel order by carrier (return to Pending status and remove carrier assignment)
     * Only allows cancellation if order is assigned to the specified carrier
     */
    public boolean cancelOrderByCarrier(int orderId, int carrierId) throws SQLException {
        String sql = "UPDATE OrderInfo SET status = 'Pending', carrierId = NULL " +
                     "WHERE orderId = ? AND carrierId = ? AND status IN ('Assigned', 'InTransit')";
        
        try (Connection conn = dbAdapter.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, orderId);
            stmt.setInt(2, carrierId);
            
            return stmt.executeUpdate() > 0;
        }
    }
    
    /**
     * Update invoice path for order
     */
    public boolean updateInvoicePath(int orderId, String invoicePath) throws SQLException {
        String sql = "UPDATE OrderInfo SET invoicePath = ? WHERE orderId = ?";
        
        try (Connection conn = dbAdapter.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, invoicePath);
            stmt.setInt(2, orderId);
            
            return stmt.executeUpdate() > 0;
        }
    }
    
    /**
     * Update invoice path and PDF content for order
     */
    public boolean updateInvoiceData(int orderId, String invoicePath, byte[] invoicePdfBytes) throws SQLException {
        String sql = "UPDATE OrderInfo SET invoicePath = ?, invoiceContent = ? WHERE orderId = ?";
        
        try (Connection conn = dbAdapter.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, invoicePath);
            if (invoicePdfBytes != null && invoicePdfBytes.length > 0) {
                stmt.setBytes(2, invoicePdfBytes);
            } else {
                stmt.setNull(2, Types.BLOB);
            }
            stmt.setInt(3, orderId);
            
            return stmt.executeUpdate() > 0;
        }
    }
    
    /**
     * Map ResultSet to Order object
     */
    private Order mapResultSetToOrder(ResultSet rs) throws SQLException {
        Order order = new Order();
        order.setOrderId(rs.getInt("orderId"));
        order.setCustomerId(rs.getInt("customerId"));
        int carrierId = rs.getInt("carrierId");
        if (!rs.wasNull()) {
            order.setCarrierId(carrierId);
        }
        Timestamp orderDate = rs.getTimestamp("orderDate");
        if (orderDate != null) {
            order.setOrderDate(orderDate.toLocalDateTime());
        }
        Timestamp deliveryDate = rs.getTimestamp("deliveryDate");
        if (deliveryDate != null) {
            order.setDeliveryDate(deliveryDate.toLocalDateTime());
        }
        
        // Handle new fields (with backward compatibility)
        try {
            order.setSubtotal(rs.getBigDecimal("subtotal"));
        } catch (SQLException e) {
            // Field doesn't exist in old schema, calculate from totalCost
            order.setSubtotal(rs.getBigDecimal("totalCost"));
        }
        
        try {
            order.setVatAmount(rs.getBigDecimal("vatAmount"));
        } catch (SQLException e) {
            order.setVatAmount(java.math.BigDecimal.ZERO);
        }
        
        try {
            order.setDiscountAmount(rs.getBigDecimal("discountAmount"));
        } catch (SQLException e) {
            order.setDiscountAmount(java.math.BigDecimal.ZERO);
        }
        
        try {
            order.setLoyaltyDiscount(rs.getBigDecimal("loyaltyDiscount"));
        } catch (SQLException e) {
            order.setLoyaltyDiscount(java.math.BigDecimal.ZERO);
        }
        
        try {
            order.setCouponCode(rs.getString("couponCode"));
        } catch (SQLException e) {
            order.setCouponCode(null);
        }
        
        Timestamp canCancelUntil = rs.getTimestamp("canCancelUntil");
        if (canCancelUntil != null) {
            order.setCanCancelUntil(canCancelUntil.toLocalDateTime());
        }
        
        order.setTotalCost(rs.getBigDecimal("totalCost"));
        order.setStatus(rs.getString("status"));
        order.setDeliveryAddress(rs.getString("deliveryAddress"));
        order.setInvoicePath(rs.getString("invoicePath"));
        
        // Read BLOB invoice PDF content if available
        try {
            byte[] invoiceBytes = rs.getBytes("invoiceContent");
            if (invoiceBytes != null && invoiceBytes.length > 0) {
                order.setInvoiceContent(invoiceBytes); // Store PDF as byte array
            }
        } catch (SQLException e) {
            // Column might not exist in old schema, ignore
            System.err.println("Warning: Could not read invoiceContent: " + e.getMessage());
        }
        
        return order;
    }
}

