package com.group17.greengrocer.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Order model class representing a customer order.
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
    private String status; // Pending, Assigned, InTransit, Delivered, Cancelled
    private String deliveryAddress;
    private String invoicePath;
    private String couponCode;
    private LocalDateTime canCancelUntil;
    private List<OrderItem> items;
    
    // Constructors
    public Order() {
        this.items = new ArrayList<>();
    }
    
    public Order(int customerId, String deliveryAddress) {
        this.customerId = customerId;
        this.deliveryAddress = deliveryAddress;
        this.status = "Pending";
        this.items = new ArrayList<>();
        this.orderDate = LocalDateTime.now();
    }
    
    // Getters and Setters
    public int getOrderId() {
        return orderId;
    }
    
    public void setOrderId(int orderId) {
        this.orderId = orderId;
    }
    
    public int getCustomerId() {
        return customerId;
    }
    
    public void setCustomerId(int customerId) {
        this.customerId = customerId;
    }
    
    public Integer getCarrierId() {
        return carrierId;
    }
    
    public void setCarrierId(Integer carrierId) {
        this.carrierId = carrierId;
    }
    
    public LocalDateTime getOrderDate() {
        return orderDate;
    }
    
    public void setOrderDate(LocalDateTime orderDate) {
        this.orderDate = orderDate;
    }
    
    public LocalDateTime getDeliveryDate() {
        return deliveryDate;
    }
    
    public void setDeliveryDate(LocalDateTime deliveryDate) {
        this.deliveryDate = deliveryDate;
    }
    
    public BigDecimal getTotalCost() {
        return totalCost;
    }
    
    public void setTotalCost(BigDecimal totalCost) {
        this.totalCost = totalCost;
    }
    
    public BigDecimal getSubtotal() {
        return subtotal;
    }
    
    public void setSubtotal(BigDecimal subtotal) {
        this.subtotal = subtotal;
    }
    
    public BigDecimal getVatAmount() {
        return vatAmount;
    }
    
    public void setVatAmount(BigDecimal vatAmount) {
        this.vatAmount = vatAmount;
    }
    
    public BigDecimal getDiscountAmount() {
        return discountAmount;
    }
    
    public void setDiscountAmount(BigDecimal discountAmount) {
        this.discountAmount = discountAmount;
    }
    
    public BigDecimal getLoyaltyDiscount() {
        return loyaltyDiscount;
    }
    
    public void setLoyaltyDiscount(BigDecimal loyaltyDiscount) {
        this.loyaltyDiscount = loyaltyDiscount;
    }
    
    public String getCouponCode() {
        return couponCode;
    }
    
    public void setCouponCode(String couponCode) {
        this.couponCode = couponCode;
    }
    
    public LocalDateTime getCanCancelUntil() {
        return canCancelUntil;
    }
    
    public void setCanCancelUntil(LocalDateTime canCancelUntil) {
        this.canCancelUntil = canCancelUntil;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    public String getDeliveryAddress() {
        return deliveryAddress;
    }
    
    public void setDeliveryAddress(String deliveryAddress) {
        this.deliveryAddress = deliveryAddress;
    }
    
    public String getInvoicePath() {
        return invoicePath;
    }
    
    public void setInvoicePath(String invoicePath) {
        this.invoicePath = invoicePath;
    }
    
    public List<OrderItem> getItems() {
        return items;
    }
    
    public void setItems(List<OrderItem> items) {
        this.items = items;
    }
    
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








