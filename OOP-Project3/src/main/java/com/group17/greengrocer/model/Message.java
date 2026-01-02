package com.group17.greengrocer.model;

import java.time.LocalDateTime;

/**
 * Message model class representing a message from customer to owner.
 * Contains message information including subject, content, and read status.
 */
public class Message {
    private int messageId;
    private int customerId;
    private Integer ownerId;
    private String subject;
    private String message;
    private boolean isRead;
    private LocalDateTime createdAt;
    
    /**
     * Default constructor for Message.
     */
    public Message() {
    }
    
    /**
     * Gets the message ID.
     * @return The message ID
     */
    public int getMessageId() {
        return messageId;
    }
    
    /**
     * Sets the message ID.
     * @param messageId The message ID to set
     */
    public void setMessageId(int messageId) {
        this.messageId = messageId;
    }
    
    /**
     * Gets the customer ID.
     * @return The customer ID
     */
    public int getCustomerId() {
        return customerId;
    }
    
    /**
     * Sets the customer ID.
     * @param customerId The customer ID to set
     */
    public void setCustomerId(int customerId) {
        this.customerId = customerId;
    }
    
    /**
     * Gets the owner ID.
     * @return The owner ID, or null if not assigned
     */
    public Integer getOwnerId() {
        return ownerId;
    }
    
    /**
     * Sets the owner ID.
     * @param ownerId The owner ID to set
     */
    public void setOwnerId(Integer ownerId) {
        this.ownerId = ownerId;
    }
    
    /**
     * Gets the message subject.
     * @return The subject
     */
    public String getSubject() {
        return subject;
    }
    
    /**
     * Sets the message subject.
     * @param subject The subject to set
     */
    public void setSubject(String subject) {
        this.subject = subject;
    }
    
    /**
     * Gets the message content.
     * @return The message content
     */
    public String getMessage() {
        return message;
    }
    
    /**
     * Sets the message content.
     * @param message The message content to set
     */
    public void setMessage(String message) {
        this.message = message;
    }
    
    /**
     * Checks if the message is read.
     * @return true if read, false otherwise
     */
    public boolean isRead() {
        return isRead;
    }
    
    /**
     * Sets the read status.
     * @param read The read status to set
     */
    public void setRead(boolean read) {
        isRead = read;
    }
    
    /**
     * Gets the creation date.
     * @return The creation date
     */
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    /**
     * Sets the creation date.
     * @param createdAt The creation date to set
     */
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
