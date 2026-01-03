package com.group05.greengrocer.repository;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.group05.greengrocer.model.Message;
import com.group05.greengrocer.util.DatabaseAdapter;

/**
 * Repository class for Message database operations.
 */
public class MessageRepository {
    private final DatabaseAdapter dbAdapter;

    /**
     * Constructor for MessageRepository.
     */
    public MessageRepository() {
        this.dbAdapter = DatabaseAdapter.getInstance();
    }

    /**
     * Create a new message (to owner).
     * 
     * @param customerId The customer ID sending the message
     * @param subject    The message subject
     * @param message    The message content
     * @return true if message was created successfully, false otherwise
     * @throws SQLException if database access error occurs
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
     * Get all messages.
     * 
     * @return List of all messages, sorted by creation date descending
     * @throws SQLException if database access error occurs
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
     * Reply to a message (mark as read and add reply).
     * 
     * @param messageId The message ID to reply to
     * @param reply     The reply content (currently only marks as read)
     * @return true if message was updated successfully, false otherwise
     * @throws SQLException if database access error occurs
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
     * Map ResultSet to Message object.
     * 
     * @param rs The ResultSet containing message data
     * @return The mapped Message object
     * @throws SQLException if database access error occurs
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
