package com.group05.greengrocer.service;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.group05.greengrocer.model.CarrierRating;
import com.group05.greengrocer.model.Coupon;
import com.group05.greengrocer.model.Message;
import com.group05.greengrocer.model.Order;
import com.group05.greengrocer.model.User;
import com.group05.greengrocer.repository.CouponRepository;
import com.group05.greengrocer.repository.MessageRepository;
import com.group05.greengrocer.repository.OrderRepository;
import com.group05.greengrocer.repository.RatingRepository;
import com.group05.greengrocer.repository.UserRepository;

/**
 * Service class for owner business logic.
 * Handles reports, carrier management, etc.
 */
public class OwnerService {
    private final UserRepository userRepository;
    private final OrderRepository orderRepository;
    private final MessageRepository messageRepository;
    private final CouponRepository couponRepository;
    private final RatingRepository ratingRepository;

    /**
     * Constructor for OwnerService.
     */
    public OwnerService() {
        this.userRepository = new UserRepository();
        this.orderRepository = new OrderRepository();
        this.messageRepository = new MessageRepository();
        this.couponRepository = new CouponRepository();
        this.ratingRepository = new RatingRepository();
    }

    /**
     * Get all carriers.
     * 
     * @return List of all active carriers
     */
    public List<User> getAllCarriers() {
        try {
            return userRepository.getAllCarriers();
        } catch (SQLException e) {
            System.err.println("Error fetching carriers: " + e.getMessage());
            e.printStackTrace();
            return List.of();
        }
    }

    /**
     * Hire a new carrier (create user with Carrier role).
     * 
     * @param carrier The User object to create as carrier
     * @return true if carrier was hired successfully, false otherwise
     */
    public boolean hireCarrier(User carrier) {
        carrier.setRole("Carrier");
        carrier.setActive(true);
        try {
            return userRepository.create(carrier);
        } catch (SQLException e) {
            System.err.println("Error hiring carrier: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Fire a carrier (soft delete).
     * 
     * @param carrierId The carrier ID to fire
     * @return true if carrier was fired successfully, false otherwise
     */
    public boolean fireCarrier(int carrierId) {
        try {
            return userRepository.delete(carrierId);
        } catch (SQLException e) {
            System.err.println("Error firing carrier: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Calculate total profit from delivered orders.
     * 
     * @return The total profit from all delivered orders
     */
    public BigDecimal calculateTotalProfit() {
        try {
            List<Order> deliveredOrders = orderRepository.findAllDeliveredOrders();
            BigDecimal totalProfit = BigDecimal.ZERO;

            for (Order order : deliveredOrders) {
                if (order.getTotalCost() != null) {
                    totalProfit = totalProfit.add(order.getTotalCost());
                }
            }

            return totalProfit;
        } catch (SQLException e) {
            System.err.println("Error calculating profit: " + e.getMessage());
            e.printStackTrace();
            return BigDecimal.ZERO;
        }
    }

    /**
     * Get profit report data.
     * 
     * @return Map containing total profit, total orders, and list of delivered
     *         orders
     */
    public Map<String, Object> getProfitReport() {
        Map<String, Object> report = new HashMap<>();
        try {
            List<Order> deliveredOrders = orderRepository.findAllDeliveredOrders();
            BigDecimal totalProfit = calculateTotalProfit();
            int totalOrders = deliveredOrders.size();

            report.put("totalProfit", totalProfit);
            report.put("totalOrders", totalOrders);
            report.put("orders", deliveredOrders);

            return report;
        } catch (SQLException e) {
            System.err.println("Error generating profit report: " + e.getMessage());
            e.printStackTrace();
            return report;
        }
    }

    /**
     * Get delivered orders report.
     * 
     * @return List of all delivered orders
     */
    public List<Order> getDeliveredOrdersReport() {
        try {
            return orderRepository.findAllDeliveredOrders();
        } catch (SQLException e) {
            System.err.println("Error fetching delivered orders: " + e.getMessage());
            e.printStackTrace();
            return List.of();
        }
    }

    /**
     * Get carrier performance report.
     * Returns map of carrier ID to number of completed orders.
     * 
     * @return Map of carrier ID to number of completed orders
     */
    public Map<Integer, Integer> getCarrierPerformanceReport() {
        Map<Integer, Integer> performance = new HashMap<>();
        try {
            List<Order> deliveredOrders = orderRepository.findAllDeliveredOrders();

            for (Order order : deliveredOrders) {
                if (order.getCarrierId() != null) {
                    int carrierId = order.getCarrierId();
                    performance.put(carrierId, performance.getOrDefault(carrierId, 0) + 1);
                }
            }

            return performance;
        } catch (SQLException e) {
            System.err.println("Error generating carrier performance report: " + e.getMessage());
            e.printStackTrace();
            return performance;
        }
    }

    /**
     * Get user by ID.
     * 
     * @param userId The user ID to search for
     * @return The User object if found, null otherwise
     */
    public User getUserById(int userId) {
        try {
            return userRepository.findById(userId);
        } catch (SQLException e) {
            System.err.println("Error fetching user: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Get all messages.
     * 
     * @return List of all messages from customers
     */
    public List<Message> getAllMessages() {
        try {
            return messageRepository.findAll();
        } catch (SQLException e) {
            System.err.println("Error fetching messages: " + e.getMessage());
            e.printStackTrace();
            return List.of();
        }
    }

    /**
     * Reply to a message.
     * 
     * @param messageId The message ID to reply to
     * @param reply     The reply content (currently only marks as read)
     * @return true if message was updated successfully, false otherwise
     */
    public boolean replyToMessage(int messageId, String reply) {
        try {
            return messageRepository.reply(messageId, reply);
        } catch (SQLException e) {
            System.err.println("Error replying to message: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Get all coupons.
     * 
     * @return List of all coupons
     */
    public List<Coupon> getAllCoupons() {
        try {
            return couponRepository.findAll();
        } catch (SQLException e) {
            System.err.println("Error fetching coupons: " + e.getMessage());
            e.printStackTrace();
            return List.of();
        }
    }

    /**
     * Create a coupon for a customer.
     * 
     * @param customerId      The customer ID to assign the coupon to (or -1 for all
     *                        customers)
     * @param couponCode      The unique coupon code
     * @param discountAmount  The fixed discount amount
     * @param discountPercent The percentage discount
     * @param couponName      The optional coupon name
     * @return true if coupon was created successfully, false otherwise
     */
    public boolean createCoupon(int customerId, String couponCode, BigDecimal discountAmount,
            BigDecimal discountPercent, String couponName) {
        try {
            return couponRepository.create(customerId, couponCode, discountAmount, discountPercent, couponName);
        } catch (SQLException e) {
            System.err.println("Error creating coupon: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Get all ratings.
     * 
     * @return List of all carrier ratings
     */
    public List<CarrierRating> getAllRatings() {
        try {
            return ratingRepository.findAll();
        } catch (SQLException e) {
            System.err.println("Error fetching ratings: " + e.getMessage());
            e.printStackTrace();
            return List.of();
        }
    }

    /**
     * Get all customers.
     * 
     * @return List of all active customers
     */
    public List<User> getAllCustomers() {
        try {
            return userRepository.getAllCustomers();
        } catch (SQLException e) {
            System.err.println("Error fetching customers: " + e.getMessage());
            e.printStackTrace();
            return List.of();
        }
    }

    /**
     * Check if coupon code is unique.
     * 
     * @param couponCode The coupon code to check
     * @return true if the coupon code is unique, false otherwise
     */
    public boolean isCouponCodeUnique(String couponCode) {
        try {
            return couponRepository.isCodeUnique(couponCode);
        } catch (SQLException e) {
            System.err.println("Error checking coupon code uniqueness: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
}
