package com.group05.greengrocer.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Order model class representing a customer order.
 * Contains order information including items, customer, carrier, dates, and
 * pricing.
 */
public class Order {
    private int orderId;
    private int customerId;
    private Integer carrierId;
    private LocalDateTime orderDate;
    private LocalDateTime deliveryDate;
    private BigDecimal subtotal;
    private BigDecimal vatAmount;
    private BigDecimal discountAmount;
    private BigDecimal loyaltyDiscount;
    private BigDecimal totalCost;
    private String status;
    private String deliveryAddress;
    private String invoicePath;
    private String couponCode;
    private LocalDateTime canCancelUntil;
    private String transactionLog;
    private List<OrderItem> items;

    /**
     * Default constructor for Order.
     * Initializes an empty list of items.
     */
    public Order() {
        this.items = new ArrayList<>();
    }

    /**
     * Gets the transaction log.
     * 
     * @return The transaction log
     */
    public String getTransactionLog() {
        return transactionLog;
    }

    /**
     * Sets the transaction log.
     * 
     * @param transactionLog The transaction log to set
     */
    public void setTransactionLog(String transactionLog) {
        this.transactionLog = transactionLog;
    }

    /**
     * Constructor for Order with basic information.
     * 
     * @param customerId      The ID of the customer placing the order
     * @param deliveryAddress The delivery address
     */
    public Order(int customerId, String deliveryAddress) {
        this.customerId = customerId;
        this.deliveryAddress = deliveryAddress;
        this.status = "Pending";
        this.items = new ArrayList<>();
        this.orderDate = LocalDateTime.now();
    }

    /**
     * Gets the order ID.
     * 
     * @return The order ID
     */
    public int getOrderId() {
        return orderId;
    }

    /**
     * Sets the order ID.
     * 
     * @param orderId The order ID to set
     */
    public void setOrderId(int orderId) {
        this.orderId = orderId;
    }

    /**
     * Gets the customer ID.
     * 
     * @return The customer ID
     */
    public int getCustomerId() {
        return customerId;
    }

    /**
     * Sets the customer ID.
     * 
     * @param customerId The customer ID to set
     */
    public void setCustomerId(int customerId) {
        this.customerId = customerId;
    }

    /**
     * Gets the carrier ID.
     * 
     * @return The carrier ID, or null if not assigned
     */
    public Integer getCarrierId() {
        return carrierId;
    }

    /**
     * Sets the carrier ID.
     * 
     * @param carrierId The carrier ID to set
     */
    public void setCarrierId(Integer carrierId) {
        this.carrierId = carrierId;
    }

    /**
     * Gets the order date.
     * 
     * @return The order date
     */
    public LocalDateTime getOrderDate() {
        return orderDate;
    }

    /**
     * Sets the order date.
     * 
     * @param orderDate The order date to set
     */
    public void setOrderDate(LocalDateTime orderDate) {
        this.orderDate = orderDate;
    }

    /**
     * Gets the delivery date.
     * 
     * @return The delivery date
     */
    public LocalDateTime getDeliveryDate() {
        return deliveryDate;
    }

    /**
     * Sets the delivery date.
     * 
     * @param deliveryDate The delivery date to set
     */
    public void setDeliveryDate(LocalDateTime deliveryDate) {
        this.deliveryDate = deliveryDate;
    }

    /**
     * Gets the total cost.
     * 
     * @return The total cost
     */
    public BigDecimal getTotalCost() {
        return totalCost;
    }

    /**
     * Sets the total cost.
     * 
     * @param totalCost The total cost to set
     */
    public void setTotalCost(BigDecimal totalCost) {
        this.totalCost = totalCost;
    }

    /**
     * Gets the subtotal.
     * 
     * @return The subtotal
     */
    public BigDecimal getSubtotal() {
        return subtotal;
    }

    /**
     * Sets the subtotal.
     * 
     * @param subtotal The subtotal to set
     */
    public void setSubtotal(BigDecimal subtotal) {
        this.subtotal = subtotal;
    }

    /**
     * Gets the VAT amount.
     * 
     * @return The VAT amount
     */
    public BigDecimal getVatAmount() {
        return vatAmount;
    }

    /**
     * Sets the VAT amount.
     * 
     * @param vatAmount The VAT amount to set
     */
    public void setVatAmount(BigDecimal vatAmount) {
        this.vatAmount = vatAmount;
    }

    /**
     * Gets the discount amount.
     * 
     * @return The discount amount
     */
    public BigDecimal getDiscountAmount() {
        return discountAmount;
    }

    /**
     * Sets the discount amount.
     * 
     * @param discountAmount The discount amount to set
     */
    public void setDiscountAmount(BigDecimal discountAmount) {
        this.discountAmount = discountAmount;
    }

    /**
     * Gets the loyalty discount amount.
     * 
     * @return The loyalty discount
     */
    public BigDecimal getLoyaltyDiscount() {
        return loyaltyDiscount;
    }

    /**
     * Sets the loyalty discount amount.
     * 
     * @param loyaltyDiscount The loyalty discount to set
     */
    public void setLoyaltyDiscount(BigDecimal loyaltyDiscount) {
        this.loyaltyDiscount = loyaltyDiscount;
    }

    /**
     * Gets the coupon code used.
     * 
     * @return The coupon code
     */
    public String getCouponCode() {
        return couponCode;
    }

    /**
     * Sets the coupon code used.
     * 
     * @param couponCode The coupon code to set
     */
    public void setCouponCode(String couponCode) {
        this.couponCode = couponCode;
    }

    /**
     * Gets the cancellation deadline.
     * 
     * @return The cancellation deadline
     */
    public LocalDateTime getCanCancelUntil() {
        return canCancelUntil;
    }

    /**
     * Sets the cancellation deadline.
     * 
     * @param canCancelUntil The cancellation deadline to set
     */
    public void setCanCancelUntil(LocalDateTime canCancelUntil) {
        this.canCancelUntil = canCancelUntil;
    }

    /**
     * Gets the order status.
     * 
     * @return The status (Pending, Assigned, InTransit, Delivered, Cancelled)
     */
    public String getStatus() {
        return status;
    }

    /**
     * Sets the order status.
     * 
     * @param status The status to set
     */
    public void setStatus(String status) {
        this.status = status;
    }

    /**
     * Gets the delivery address.
     * 
     * @return The delivery address
     */
    public String getDeliveryAddress() {
        return deliveryAddress;
    }

    /**
     * Sets the delivery address.
     * 
     * @param deliveryAddress The delivery address to set
     */
    public void setDeliveryAddress(String deliveryAddress) {
        this.deliveryAddress = deliveryAddress;
    }

    /**
     * Gets the invoice file path.
     * 
     * @return The invoice path
     */
    public String getInvoicePath() {
        return invoicePath;
    }

    /**
     * Sets the invoice file path.
     * 
     * @param invoicePath The invoice path to set
     */
    public void setInvoicePath(String invoicePath) {
        this.invoicePath = invoicePath;
    }

    /**
     * Gets the list of order items.
     * 
     * @return The list of items
     */
    public List<OrderItem> getItems() {
        return items;
    }

    /**
     * Sets the list of order items.
     * 
     * @param items The list of items to set
     */
    public void setItems(List<OrderItem> items) {
        this.items = items;
    }

    /**
     * Adds an item to the order.
     * 
     * @param item The order item to add
     */
    public void addItem(OrderItem item) {
        this.items.add(item);
    }

    @Override
    public String toString() {
        return "Order{" +
                "orderId=" + orderId +
                ", customerId=" + customerId +
                ", carrierId=" + carrierId +
                ", totalCost=" + totalCost +
                ", status='" + status + '\'' +
                '}';
    }
}
