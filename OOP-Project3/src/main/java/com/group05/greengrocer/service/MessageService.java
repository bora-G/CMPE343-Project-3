package com.group05.greengrocer.service;

import java.sql.SQLException;

import com.group05.greengrocer.repository.MessageRepository;

/**
 * Service class for messaging business logic.
 */
public class MessageService {
    private final MessageRepository messageRepository;

    /**
     * Constructor for MessageService.
     */
    public MessageService() {
        this.messageRepository = new MessageRepository();
    }

    /**
     * Send a message to the owner.
     * 
     * @param customerId The customer ID sending the message
     * @param subject    The message subject
     * @param message    The message content
     * @return true if message was sent successfully, false otherwise
     */
    public boolean sendMessageToOwner(int customerId, String subject, String message) {
        try {
            return messageRepository.create(customerId, subject, message);
        } catch (SQLException e) {
            System.err.println("Error sending message: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
}
