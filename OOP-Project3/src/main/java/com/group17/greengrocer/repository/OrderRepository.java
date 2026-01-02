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
                     "discountAmount, loyaltyDiscount, totalCost, status, deliveryAddress, invoicePath, couponCode, canCancelUntil) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        
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
            stmt.setString(13, order.getCouponCode());
            if (order.getCanCancelUntil() != null) {
                stmt.setTimestamp(14, Timestamp.valueOf(order.getCanCancelUntil()));
            } else {
                stmt.setNull(14, Types.TIMESTAMP);
            }
            
            int rowsAffected = stmt.executeUpdate();
            
            if (rowsAffected > 0) {
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        order.setOrderId(generatedKeys.getInt(1));
                    }
                }
                return true;
            }
        }
        return false;
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
        return order;
    }
}

