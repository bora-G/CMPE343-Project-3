package com.group05.greengrocer.repository;

import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import com.group05.greengrocer.model.Order;
import com.group05.greengrocer.util.DatabaseAdapter;

/**
 * Repository class for Order database operations.
 * Handles all database access related to orders.
 */
public class OrderRepository {
    private final DatabaseAdapter dbAdapter;

    /**
     * Constructor for OrderRepository.
     */
    public OrderRepository() {
        this.dbAdapter = DatabaseAdapter.getInstance();
    }

    /**
     * Find order by ID.
     * 
     * @param orderId The order ID to search for
     * @return The Order object if found, null otherwise
     * @throws SQLException if database access error occurs
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
     * Get all orders for a customer.
     * 
     * @param customerId The customer ID to filter by
     * @return List of orders for the specified customer, sorted by order date
     *         descending
     * @throws SQLException if database access error occurs
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
     * Get all orders (for owner).
     * 
     * @return List of all orders, sorted by order date descending
     * @throws SQLException if database access error occurs
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
     * Get all available orders (Pending status, no carrier assigned).
     * 
     * @return List of available orders for carriers to accept
     * @throws SQLException if database access error occurs
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
     * Get orders assigned to a carrier.
     * 
     * @param carrierId The carrier ID to filter by
     * @return List of orders assigned to the carrier with status Assigned or
     *         InTransit
     * @throws SQLException if database access error occurs
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
     * Get completed orders for a carrier.
     * 
     * @param carrierId The carrier ID to filter by
     * @return List of delivered orders for the carrier, sorted by delivery date
     *         descending
     * @throws SQLException if database access error occurs
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
     * Get all delivered orders (for owner reports).
     * 
     * @return List of all delivered orders, sorted by delivery date descending
     * @throws SQLException if database access error occurs
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
     * Get completed orders count for a customer (for loyalty discount).
     * 
     * @param customerId The customer ID to count orders for
     * @return The number of delivered orders for the customer
     * @throws SQLException if database access error occurs
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
     * Cancel order by customer (within cancellation time frame).
     * 
     * @param orderId    The order ID to cancel
     * @param customerId The customer ID to verify ownership
     * @return true if order was cancelled successfully, false otherwise
     * @throws SQLException if database access error occurs
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
     * Create a new order.
     * 
     * @param order The Order object to create
     * @return true if order was created successfully, false otherwise
     * @throws SQLException if database access error occurs
     */
    public boolean create(Order order) throws SQLException {
        String sql = "INSERT INTO OrderInfo (customerId, carrierId, orderDate, deliveryDate, subtotal, vatAmount, " +
                "discountAmount, loyaltyDiscount, totalCost, status, deliveryAddress, invoicePath, invoicePdf, couponCode, canCancelUntil) "
                +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = dbAdapter.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setInt(1, order.getCustomerId());
            if (order.getCarrierId() != null) {
                stmt.setInt(2, order.getCarrierId());
            } else {
                stmt.setNull(2, Types.INTEGER);
            }
            stmt.setTimestamp(3, order.getOrderDate() != null ? Timestamp.valueOf(order.getOrderDate())
                    : Timestamp.valueOf(LocalDateTime.now()));
            if (order.getDeliveryDate() != null) {
                stmt.setTimestamp(4, Timestamp.valueOf(order.getDeliveryDate()));
            } else {
                stmt.setNull(4, Types.TIMESTAMP);
            }
            stmt.setBigDecimal(5, order.getSubtotal() != null ? order.getSubtotal() : order.getTotalCost());
            stmt.setBigDecimal(6, order.getVatAmount() != null ? order.getVatAmount() : java.math.BigDecimal.ZERO);
            stmt.setBigDecimal(7,
                    order.getDiscountAmount() != null ? order.getDiscountAmount() : java.math.BigDecimal.ZERO);
            stmt.setBigDecimal(8,
                    order.getLoyaltyDiscount() != null ? order.getLoyaltyDiscount() : java.math.BigDecimal.ZERO);
            stmt.setBigDecimal(9, order.getTotalCost());
            stmt.setString(10, order.getStatus());
            stmt.setString(11, order.getDeliveryAddress());
            stmt.setString(12, order.getInvoicePath());
            stmt.setNull(13, Types.BLOB);
            stmt.setString(14, order.getCouponCode());
            if (order.getCanCancelUntil() != null) {
                stmt.setTimestamp(15, Timestamp.valueOf(order.getCanCancelUntil()));
            } else {
                stmt.setNull(15, Types.TIMESTAMP);
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
     * Update order information.
     * 
     * @param order The Order object with updated information
     * @return true if order was updated successfully, false otherwise
     * @throws SQLException if database access error occurs
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
     * Assign carrier to order (with transaction to prevent multiple assignments).
     * 
     * @param orderId   The order ID to assign
     * @param carrierId The carrier ID to assign to the order
     * @return true if carrier was assigned successfully, false otherwise
     * @throws SQLException if database access error occurs
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
     * Mark order as completed.
     * 
     * @param orderId The order ID to mark as completed
     * @return true if order was marked as completed successfully, false otherwise
     * @throws SQLException if database access error occurs
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
     * Mark order as completed with specific delivery date.
     * 
     * @param orderId      The order ID to mark as completed
     * @param deliveryDate The delivery date to set
     * @return true if order was marked as completed successfully, false otherwise
     * @throws SQLException if database access error occurs
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
     * Cancel order by carrier (return to Pending status and remove carrier
     * assignment).
     * Only allows cancellation if order is assigned to the specified carrier.
     * 
     * @param orderId   The order ID to cancel
     * @param carrierId The carrier ID to verify ownership
     * @return true if order was cancelled successfully, false otherwise
     * @throws SQLException if database access error occurs
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
     * Save invoice PDF to database as LONGBLOB.
     * 
     * @param orderId  The order ID to save the PDF for
     * @param pdfBytes The PDF file as byte array
     * @return true if PDF was saved successfully, false otherwise
     * @throws SQLException if database access error occurs
     */
    public boolean saveInvoicePDF(int orderId, byte[] pdfBytes) throws SQLException {
        String sql = "UPDATE OrderInfo SET invoicePdf = ? WHERE orderId = ?";

        try (Connection conn = dbAdapter.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            if (pdfBytes != null) {
                stmt.setBytes(1, pdfBytes);
            } else {
                stmt.setNull(1, Types.BLOB);
            }
            stmt.setInt(2, orderId);

            return stmt.executeUpdate() > 0;
        }
    }

    /**
     * Get invoice PDF from database.
     * 
     * @param orderId The order ID to get the PDF for
     * @return The PDF file as byte array, or null if not found
     * @throws SQLException if database access error occurs
     */
    public byte[] getInvoicePDF(int orderId) throws SQLException {
        String sql = "SELECT invoicePdf FROM OrderInfo WHERE orderId = ?";

        try (Connection conn = dbAdapter.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, orderId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getBytes("invoicePdf");
                }
            }
        }
        return null;
    }

    /**
     * Update invoice path for order (legacy support).
     * 
     * @param orderId     The order ID to update
     * @param invoicePath The invoice file path
     * @return true if invoice path was updated successfully, false otherwise
     * @throws SQLException if database access error occurs
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
     * Map ResultSet to Order object.
     * 
     * @param rs The ResultSet containing order data
     * @return The mapped Order object
     * @throws SQLException if database access error occurs
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

        try {
            order.setSubtotal(rs.getBigDecimal("subtotal"));
        } catch (SQLException e) {
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
        try {
            order.setTransactionLog(rs.getString("transactionLog"));
        } catch (SQLException e) {
            order.setTransactionLog(null);
        }
        return order;
    }

    /**
     * Append text to the transaction log (CLOB).
     * 
     * @param orderId  The order ID
     * @param logEntry The text to append
     * @return true if successful
     */
    public boolean appendTransactionLog(int orderId, String logEntry) throws SQLException {
        // MySQL formatting to append: CONCAT(IFNULL(transactionLog, ''), '\n', ?)
        String sql = "UPDATE OrderInfo SET transactionLog = CONCAT(IFNULL(transactionLog, ''), ?, '\n') WHERE orderId = ?";

        try (Connection conn = dbAdapter.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1,
                    DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").format(LocalDateTime.now()) + ": " + logEntry);
            stmt.setInt(2, orderId);
            return stmt.executeUpdate() > 0;
        }
    }
}
