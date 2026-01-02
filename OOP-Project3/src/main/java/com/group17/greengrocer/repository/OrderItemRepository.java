package com.group17.greengrocer.repository;

import com.group17.greengrocer.model.OrderItem;
import com.group17.greengrocer.util.DatabaseAdapter;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Repository class for OrderItem database operations.
 * Handles all database access related to order items.
 */
public class OrderItemRepository {
    private final DatabaseAdapter dbAdapter;
    
    public OrderItemRepository() {
        this.dbAdapter = DatabaseAdapter.getInstance();
    }
    
    /**
     * Find order item by ID
     */
    public OrderItem findById(int orderItemId) throws SQLException {
        String sql = "SELECT * FROM OrderItem WHERE orderItemId = ?";
        
        try (Connection conn = dbAdapter.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, orderItemId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToOrderItem(rs);
                }
            }
        }
        return null;
    }
    
    /**
     * Get all items for an order
     */
    public List<OrderItem> findByOrderId(int orderId) throws SQLException {
        String sql = "SELECT * FROM OrderItem WHERE orderId = ?";
        List<OrderItem> items = new ArrayList<>();
        
        try (Connection conn = dbAdapter.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, orderId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    items.add(mapResultSetToOrderItem(rs));
                }
            }
        }
        return items;
    }
    
    /**
     * Create a new order item
     */
    public boolean create(OrderItem item) throws SQLException {
        String sql = "INSERT INTO OrderItem (orderId, productId, quantity, unitPrice, subtotal) " +
                     "VALUES (?, ?, ?, ?, ?)";
        
        try (Connection conn = dbAdapter.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            stmt.setInt(1, item.getOrderId());
            stmt.setInt(2, item.getProductId());
            stmt.setBigDecimal(3, item.getQuantity());
            stmt.setBigDecimal(4, item.getUnitPrice());
            stmt.setBigDecimal(5, item.getSubtotal());
            
            int rowsAffected = stmt.executeUpdate();
            
            if (rowsAffected > 0) {
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        item.setOrderItemId(generatedKeys.getInt(1));
                    }
                }
                return true;
            }
        }
        return false;
    }
    
    /**
     * Create multiple order items in a batch
     */
    public boolean createBatch(List<OrderItem> items) throws SQLException {
        String sql = "INSERT INTO OrderItem (orderId, productId, quantity, unitPrice, subtotal) " +
                     "VALUES (?, ?, ?, ?, ?)";
        
        try (Connection conn = dbAdapter.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            for (OrderItem item : items) {
                stmt.setInt(1, item.getOrderId());
                stmt.setInt(2, item.getProductId());
                stmt.setBigDecimal(3, item.getQuantity());
                stmt.setBigDecimal(4, item.getUnitPrice());
                stmt.setBigDecimal(5, item.getSubtotal());
                stmt.addBatch();
            }
            
            int[] results = stmt.executeBatch();
            return results.length == items.size();
        }
    }
    
    /**
     * Update order item
     */
    public boolean update(OrderItem item) throws SQLException {
        String sql = "UPDATE OrderItem SET orderId = ?, productId = ?, quantity = ?, unitPrice = ?, subtotal = ? " +
                     "WHERE orderItemId = ?";
        
        try (Connection conn = dbAdapter.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, item.getOrderId());
            stmt.setInt(2, item.getProductId());
            stmt.setBigDecimal(3, item.getQuantity());
            stmt.setBigDecimal(4, item.getUnitPrice());
            stmt.setBigDecimal(5, item.getSubtotal());
            stmt.setInt(6, item.getOrderItemId());
            
            return stmt.executeUpdate() > 0;
        }
    }
    
    /**
     * Delete order item
     */
    public boolean delete(int orderItemId) throws SQLException {
        String sql = "DELETE FROM OrderItem WHERE orderItemId = ?";
        
        try (Connection conn = dbAdapter.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, orderItemId);
            return stmt.executeUpdate() > 0;
        }
    }
    
    /**
     * Delete all items for an order
     */
    public boolean deleteByOrderId(int orderId) throws SQLException {
        String sql = "DELETE FROM OrderItem WHERE orderId = ?";
        
        try (Connection conn = dbAdapter.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, orderId);
            return stmt.executeUpdate() > 0;
        }
    }
    
    /**
     * Map ResultSet to OrderItem object
     */
    private OrderItem mapResultSetToOrderItem(ResultSet rs) throws SQLException {
        OrderItem item = new OrderItem();
        item.setOrderItemId(rs.getInt("orderItemId"));
        item.setOrderId(rs.getInt("orderId"));
        item.setProductId(rs.getInt("productId"));
        item.setQuantity(rs.getBigDecimal("quantity"));
        item.setUnitPrice(rs.getBigDecimal("unitPrice"));
        item.setSubtotal(rs.getBigDecimal("subtotal"));
        return item;
    }
}








