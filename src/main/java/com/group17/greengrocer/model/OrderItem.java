package com.group17.greengrocer.model;

import java.math.BigDecimal;

/**
 * OrderItem model class representing an item in an order.
 * Contains product information, quantity, and pricing for a single order item.
 */
public class OrderItem {
    private int orderItemId;
    private int orderId;
    private int productId;
    private BigDecimal quantity;
    private BigDecimal unitPrice;
    private BigDecimal subtotal;
    private Product product;
    
    /**
     * Default constructor for OrderItem.
     */
    public OrderItem() {
    }
    
    /**
     * Constructor for OrderItem with basic information.
     * @param orderId The ID of the order this item belongs to
     * @param productId The ID of the product
     * @param quantity The quantity ordered
     * @param unitPrice The price per unit
     */
    public OrderItem(int orderId, int productId, BigDecimal quantity, BigDecimal unitPrice) {
        this.orderId = orderId;
        this.productId = productId;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        calculateSubtotal();
    }
    
    /**
     * Gets the order item ID.
     * @return The order item ID
     */
    public int getOrderItemId() {
        return orderItemId;
    }
    
    /**
     * Sets the order item ID.
     * @param orderItemId The order item ID to set
     */
    public void setOrderItemId(int orderItemId) {
        this.orderItemId = orderItemId;
    }
    
    /**
     * Gets the order ID.
     * @return The order ID
     */
    public int getOrderId() {
        return orderId;
    }
    
    /**
     * Sets the order ID.
     * @param orderId The order ID to set
     */
    public void setOrderId(int orderId) {
        this.orderId = orderId;
    }
    
    /**
     * Gets the product ID.
     * @return The product ID
     */
    public int getProductId() {
        return productId;
    }
    
    /**
     * Sets the product ID.
     * @param productId The product ID to set
     */
    public void setProductId(int productId) {
        this.productId = productId;
    }
    
    /**
     * Gets the quantity.
     * @return The quantity
     */
    public BigDecimal getQuantity() {
        return quantity;
    }
    
    /**
     * Sets the quantity and recalculates subtotal.
     * @param quantity The quantity to set
     */
    public void setQuantity(BigDecimal quantity) {
        this.quantity = quantity;
        calculateSubtotal();
    }
    
    /**
     * Gets the unit price.
     * @return The unit price
     */
    public BigDecimal getUnitPrice() {
        return unitPrice;
    }
    
    /**
     * Sets the unit price and recalculates subtotal.
     * @param unitPrice The unit price to set
     */
    public void setUnitPrice(BigDecimal unitPrice) {
        this.unitPrice = unitPrice;
        calculateSubtotal();
    }
    
    /**
     * Gets the subtotal.
     * @return The subtotal (quantity * unitPrice)
     */
    public BigDecimal getSubtotal() {
        return subtotal;
    }
    
    /**
     * Sets the subtotal.
     * @param subtotal The subtotal to set
     */
    public void setSubtotal(BigDecimal subtotal) {
        this.subtotal = subtotal;
    }
    
    /**
     * Gets the product object.
     * @return The product object
     */
    public Product getProduct() {
        return product;
    }
    
    /**
     * Sets the product object.
     * @param product The product object to set
     */
    public void setProduct(Product product) {
        this.product = product;
    }
    
    /**
     * Calculates the subtotal based on quantity and unit price.
     */
    private void calculateSubtotal() {
        if (quantity != null && unitPrice != null) {
            this.subtotal = quantity.multiply(unitPrice);
        }
    }
    
    @Override
    public String toString() {
        return "OrderItem{" +
                "orderItemId=" + orderItemId +
                ", productId=" + productId +
                ", quantity=" + quantity +
                ", unitPrice=" + unitPrice +
                ", subtotal=" + subtotal +
                '}';
    }
}
