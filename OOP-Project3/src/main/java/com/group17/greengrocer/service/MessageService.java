package com.group17.greengrocer.service;

import com.group17.greengrocer.repository.MessageRepository;
import java.sql.SQLException;

/**
 * Service class for messaging business logic.
 */
public class MessageService {
    private final MessageRepository messageRepository;
    
    public MessageService() {
        this.messageRepository = new MessageRepository();
    }
    
    /**
     * Send a message to the owner
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

