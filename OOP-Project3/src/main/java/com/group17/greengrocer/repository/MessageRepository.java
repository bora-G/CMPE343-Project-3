package com.group17.greengrocer.repository;

import com.group17.greengrocer.model.Message;
import com.group17.greengrocer.util.DatabaseAdapter;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Repository class for Message database operations.
 */
public class MessageRepository {
    private final DatabaseAdapter dbAdapter;
    
    public MessageRepository() {
        this.dbAdapter = DatabaseAdapter.getInstance();
    }
    
    /**
     * Create a new message (to owner)
     */
    public boolean create(int customerId, String subject, String message) throws SQLException {
        String sql = "INSERT INTO Message (customerId, ownerId, subject, message) " +
                     "VALUES (?, NULL, ?, ?)";
        
        try (Connection conn = dbAdapter.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, customerId);
            stmt.setString(2, subject);
            stmt.setString(3, message);
            
            return stmt.executeUpdate() > 0;
        }
    }
    
    /**
     * Get all messages
     */
    public List<Message> findAll() throws SQLException {
        String sql = "SELECT * FROM Message ORDER BY createdAt DESC";
        List<Message> messages = new ArrayList<>();
        
        try (Connection conn = dbAdapter.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                messages.add(mapResultSetToMessage(rs));
            }
        }
        return messages;
    }
    
    /**
     * Reply to a message (mark as read and add reply)
     */
    public boolean reply(int messageId, String reply) throws SQLException {
        String sql = "UPDATE Message SET isRead = TRUE WHERE messageId = ?";
        
        try (Connection conn = dbAdapter.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, messageId);
            return stmt.executeUpdate() > 0;
        }
    }
    
    /**
     * Map ResultSet to Message object
     */
    private Message mapResultSetToMessage(ResultSet rs) throws SQLException {
        Message message = new Message();
        message.setMessageId(rs.getInt("messageId"));
        message.setCustomerId(rs.getInt("customerId"));
        int ownerId = rs.getInt("ownerId");
        if (!rs.wasNull()) {
            message.setOwnerId(ownerId);
        }
        message.setSubject(rs.getString("subject"));
        message.setMessage(rs.getString("message"));
        message.setRead(rs.getBoolean("isRead"));
        Timestamp createdAt = rs.getTimestamp("createdAt");
        if (createdAt != null) {
            message.setCreatedAt(createdAt.toLocalDateTime());
        }
        return message;
    }
}

