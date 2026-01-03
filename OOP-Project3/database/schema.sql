-- Local Greengrocer Project Database Schema
-- CMPE343 Project 3

-- Drop existing tables if they exist (in reverse order of dependencies)
DROP TABLE IF EXISTS Message;
DROP TABLE IF EXISTS CarrierRating;
DROP TABLE IF EXISTS Coupon;
DROP TABLE IF EXISTS OrderItem;
DROP TABLE IF EXISTS OrderInfo;
DROP TABLE IF EXISTS ProductInfo;
DROP TABLE IF EXISTS UserInfo;

-- Create UserInfo table
CREATE TABLE UserInfo (
    userId INT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    role ENUM('Customer', 'Carrier', 'Owner') NOT NULL,
    fullName VARCHAR(100) NOT NULL,
    email VARCHAR(100),
    phone VARCHAR(20),
    address TEXT,
    isActive BOOLEAN DEFAULT TRUE,
    createdAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create ProductInfo table
CREATE TABLE ProductInfo (
    productId INT PRIMARY KEY AUTO_INCREMENT,
    productName VARCHAR(100) NOT NULL,
    productType VARCHAR(50) NOT NULL,
    pricePerKg DECIMAL(10, 2) NOT NULL,
    originalPrice DECIMAL(10, 2),
    discountPercent DECIMAL(5, 2) DEFAULT 0.00,
    stock DECIMAL(10, 2) NOT NULL DEFAULT 0.0,
    threshold DECIMAL(10, 2) NOT NULL DEFAULT 5.0,
    description TEXT,
    imagePath VARCHAR(255),
    imageUrl VARCHAR(500),
    imageData LONGBLOB,
    createdAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updatedAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- Create OrderInfo table
CREATE TABLE OrderInfo (
    orderId INT PRIMARY KEY AUTO_INCREMENT,
    customerId INT NOT NULL,
    carrierId INT,
    orderDate TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deliveryDate TIMESTAMP,
    subtotal DECIMAL(10, 2) NOT NULL,
    vatAmount DECIMAL(10, 2) NOT NULL DEFAULT 0.00,
    discountAmount DECIMAL(10, 2) NOT NULL DEFAULT 0.00,
    loyaltyDiscount DECIMAL(10, 2) NOT NULL DEFAULT 0.00,
    totalCost DECIMAL(10, 2) NOT NULL,
    status ENUM('Pending', 'Assigned', 'InTransit', 'Delivered', 'Cancelled') DEFAULT 'Pending',
    deliveryAddress TEXT NOT NULL,
    invoicePath VARCHAR(255),
    invoicePdf LONGBLOB,
    transactionLog LONGTEXT,
    couponCode VARCHAR(20),
    canCancelUntil TIMESTAMP,
    FOREIGN KEY (customerId) REFERENCES UserInfo(userId) ON DELETE CASCADE,
    FOREIGN KEY (carrierId) REFERENCES UserInfo(userId) ON DELETE SET NULL
);

-- Create OrderItem table
CREATE TABLE OrderItem (
    orderItemId INT PRIMARY KEY AUTO_INCREMENT,
    orderId INT NOT NULL,
    productId INT NOT NULL,
    quantity DECIMAL(10, 2) NOT NULL,
    unitPrice DECIMAL(10, 2) NOT NULL,
    subtotal DECIMAL(10, 2) NOT NULL,
    FOREIGN KEY (orderId) REFERENCES OrderInfo(orderId) ON DELETE CASCADE,
    FOREIGN KEY (productId) REFERENCES ProductInfo(productId) ON DELETE CASCADE
);

-- Create Coupon table for discount coupons
CREATE TABLE Coupon (
    couponId INT PRIMARY KEY AUTO_INCREMENT,
    customerId INT NOT NULL,
    couponCode VARCHAR(20) UNIQUE NOT NULL,
    discountAmount DECIMAL(10, 2) NOT NULL,
    discountPercent DECIMAL(5, 2),
    isUsed BOOLEAN DEFAULT FALSE,
    expiryDate TIMESTAMP,
    createdAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (customerId) REFERENCES UserInfo(userId) ON DELETE CASCADE
);

-- Create CarrierRating table for carrier ratings
CREATE TABLE CarrierRating (
    ratingId INT PRIMARY KEY AUTO_INCREMENT,
    orderId INT NOT NULL,
    carrierId INT NOT NULL,
    customerId INT NOT NULL,
    rating INT NOT NULL CHECK (rating >= 1 AND rating <= 5),
    comment TEXT,
    createdAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (orderId) REFERENCES OrderInfo(orderId) ON DELETE CASCADE,
    FOREIGN KEY (carrierId) REFERENCES UserInfo(userId) ON DELETE CASCADE,
    FOREIGN KEY (customerId) REFERENCES UserInfo(userId) ON DELETE CASCADE
);

-- Create Message table for customer-owner messaging
CREATE TABLE Message (
    messageId INT PRIMARY KEY AUTO_INCREMENT,
    customerId INT NOT NULL,
    ownerId INT,
    subject VARCHAR(200),
    message TEXT NOT NULL,
    isRead BOOLEAN DEFAULT FALSE,
    createdAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (customerId) REFERENCES UserInfo(userId) ON DELETE CASCADE,
    FOREIGN KEY (ownerId) REFERENCES UserInfo(userId) ON DELETE SET NULL
);

-- Insert sample data for UserInfo (at least 25 rows)
-- Passwords are hashed using SHA-256 (original passwords: owner123, customer123, carrier123)
INSERT INTO UserInfo (username, password, role, fullName, email, phone, address) VALUES
('owner1', '43a0d17178a9d26c9e0fe9a74b0b45e38d32f27aed887a008a54bf6e033bf7b9', 'Owner', 'John Owner', 'owner1@greengrocer.com', '5001234567', '123 Main St'),
('customer1', 'b041c0aeb35bb0fa4aa668ca5a920b590196fdaf9a00eb852c9b7f4d123cc6d6', 'Customer', 'Alice Customer', 'alice@email.com', '5051111001', '456 Oak Ave'),
('customer2', 'b041c0aeb35bb0fa4aa668ca5a920b590196fdaf9a00eb852c9b7f4d123cc6d6', 'Customer', 'Bob Customer', 'customer2@email.com', '5051111002', '789 Pine Rd'),
('customer3', 'b041c0aeb35bb0fa4aa668ca5a920b590196fdaf9a00eb852c9b7f4d123cc6d6', 'Customer', 'Charlie Customer', 'customer3@email.com', '5051111003', '321 Elm St'),
('customer4', 'b041c0aeb35bb0fa4aa668ca5a920b590196fdaf9a00eb852c9b7f4d123cc6d6', 'Customer', 'Diana Customer', 'customer4@email.com', '5051111004', '654 Maple Dr'),
('customer5', 'b041c0aeb35bb0fa4aa668ca5a920b590196fdaf9a00eb852c9b7f4d123cc6d6', 'Customer', 'Eve Customer', 'customer5@email.com', '5051111005', '987 Cedar Ln'),
('customer6', 'b041c0aeb35bb0fa4aa668ca5a920b590196fdaf9a00eb852c9b7f4d123cc6d6', 'Customer', 'Frank Customer', 'customer6@email.com', '5051111006', '147 Birch Way'),
('customer7', 'b041c0aeb35bb0fa4aa668ca5a920b590196fdaf9a00eb852c9b7f4d123cc6d6', 'Customer', 'Grace Customer', 'customer7@email.com', '5051111007', '258 Spruce Ct'),
('customer8', 'b041c0aeb35bb0fa4aa668ca5a920b590196fdaf9a00eb852c9b7f4d123cc6d6', 'Customer', 'Henry Customer', 'customer8@email.com', '5051111008', '369 Willow Pl'),
('customer9', 'b041c0aeb35bb0fa4aa668ca5a920b590196fdaf9a00eb852c9b7f4d123cc6d6', 'Customer', 'Ivy Customer', 'customer9@email.com', '5051111009', '741 Aspen Blvd'),
('customer10', 'b041c0aeb35bb0fa4aa668ca5a920b590196fdaf9a00eb852c9b7f4d123cc6d6', 'Customer', 'Jack Customer', 'customer10@email.com', '5051111010', '852 Poplar St'),
('carrier1', '4c612d9736a44808cbb5589c2412e81822612ed7f0e22fdb341366a8d58983a6', 'Carrier', 'Tom Carrier', 'carrier1@carrier.com', '5052222001', '111 Delivery St'),
('carrier2', '4c612d9736a44808cbb5589c2412e81822612ed7f0e22fdb341366a8d58983a6', 'Carrier', 'Sarah Carrier', 'carrier2@carrier.com', '5052222002', '222 Transport Ave'),
('carrier3', '4c612d9736a44808cbb5589c2412e81822612ed7f0e22fdb341366a8d58983a6', 'Carrier', 'Mike Carrier', 'carrier3@carrier.com', '5052222003', '333 Logistics Rd'),
('carrier4', '4c612d9736a44808cbb5589c2412e81822612ed7f0e22fdb341366a8d58983a6', 'Carrier', 'Lisa Carrier', 'carrier4@carrier.com', '5052222004', '444 Shipping Dr'),
('carrier5', '4c612d9736a44808cbb5589c2412e81822612ed7f0e22fdb341366a8d58983a6', 'Carrier', 'David Carrier', 'carrier5@carrier.com', '5052222005', '555 Express Ln'),
('carrier6', '4c612d9736a44808cbb5589c2412e81822612ed7f0e22fdb341366a8d58983a6', 'Carrier', 'Emma Carrier', 'carrier6@carrier.com', '5052222006', '666 Fast Way'),
('carrier7', '4c612d9736a44808cbb5589c2412e81822612ed7f0e22fdb341366a8d58983a6', 'Carrier', 'Noah Carrier', 'carrier7@carrier.com', '5052222007', '777 Quick Ct'),
('carrier8', '4c612d9736a44808cbb5589c2412e81822612ed7f0e22fdb341366a8d58983a6', 'Carrier', 'Olivia Carrier', 'carrier8@carrier.com', '5052222008', '888 Speed Pl'),
('carrier9', '4c612d9736a44808cbb5589c2412e81822612ed7f0e22fdb341366a8d58983a6', 'Carrier', 'Liam Carrier', 'carrier9@carrier.com', '5052222009', '999 Rapid Blvd'),
('carrier10', '4c612d9736a44808cbb5589c2412e81822612ed7f0e22fdb341366a8d58983a6', 'Carrier', 'Ava Carrier', 'carrier10@carrier.com', '5052222010', '101 Swift St'),
('customer11', 'b041c0aeb35bb0fa4aa668ca5a920b590196fdaf9a00eb852c9b7f4d123cc6d6', 'Customer', 'Kevin Customer', 'customer11@email.com', '5051111011', '963 Fir Ave'),
('customer12', 'b041c0aeb35bb0fa4aa668ca5a920b590196fdaf9a00eb852c9b7f4d123cc6d6', 'Customer', 'Laura Customer', 'customer12@email.com', '5051111012', '159 Hemlock Rd'),
('customer13', 'b041c0aeb35bb0fa4aa668ca5a920b590196fdaf9a00eb852c9b7f4d123cc6d6', 'Customer', 'Mark Customer', 'customer13@email.com', '5051111013', '357 Juniper Dr'),
('customer14', 'b041c0aeb35bb0fa4aa668ca5a920b590196fdaf9a00eb852c9b7f4d123cc6d6', 'Customer', 'Nancy Customer', 'customer14@email.com', '5051111014', '468 Cypress Ln'),
('customer15', 'b041c0aeb35bb0fa4aa668ca5a920b590196fdaf9a00eb852c9b7f4d123cc6d6', 'Customer', 'Oscar Customer', 'customer15@email.com', '5051111015', '579 Redwood Way');

-- Insert sample data for ProductInfo (at least 25 rows)
INSERT INTO ProductInfo (productName, productType, pricePerKg, stock, threshold, description) VALUES
-- Fruits
('Apple', 'Fruit', 8.50, 150.0, 5.0, 'Fresh red apples'),
('Banana', 'Fruit', 6.00, 200.0, 5.0, 'Yellow ripe bananas'),
('Orange', 'Fruit', 7.50, 180.0, 5.0, 'Sweet oranges'),
('Grapes', 'Fruit', 12.00, 80.0, 5.0, 'Seedless grapes'),
('Strawberry', 'Fruit', 15.00, 50.0, 5.0, 'Fresh strawberries'),
('Watermelon', 'Fruit', 4.50, 30.0, 5.0, 'Large watermelons'),
('Mango', 'Fruit', 10.00, 100.0, 5.0, 'Tropical mangoes'),
('Pineapple', 'Fruit', 9.00, 60.0, 5.0, 'Sweet pineapples'),
('Peach', 'Fruit', 11.00, 70.0, 5.0, 'Juicy peaches'),
('Pear', 'Fruit', 9.50, 90.0, 5.0, 'Fresh pears'),
('Kiwi', 'Fruit', 13.00, 75.0, 5.0, 'Fresh kiwi fruits'),
('Cherry', 'Fruit', 18.00, 45.0, 5.0, 'Sweet cherries'),
-- Vegetables
('Tomato', 'Vegetable', 7.00, 120.0, 5.0, 'Ripe tomatoes'),
('Cucumber', 'Vegetable', 5.50, 140.0, 5.0, 'Fresh cucumbers'),
('Carrot', 'Vegetable', 6.50, 160.0, 5.0, 'Organic carrots'),
('Potato', 'Vegetable', 4.00, 200.0, 5.0, 'Fresh potatoes'),
('Onion', 'Vegetable', 5.00, 180.0, 5.0, 'Yellow onions'),
('Bell Pepper', 'Vegetable', 8.00, 100.0, 5.0, 'Colorful bell peppers'),
('Lettuce', 'Vegetable', 6.00, 80.0, 5.0, 'Fresh lettuce'),
('Broccoli', 'Vegetable', 9.00, 70.0, 5.0, 'Fresh broccoli'),
('Spinach', 'Vegetable', 7.50, 60.0, 5.0, 'Organic spinach'),
('Cabbage', 'Vegetable', 4.50, 110.0, 5.0, 'Fresh cabbage'),
('Zucchini', 'Vegetable', 7.00, 95.0, 5.0, 'Fresh zucchini'),
('Eggplant', 'Vegetable', 8.50, 85.0, 5.0, 'Fresh eggplant'),
-- Herbs
('Basil', 'Herb', 20.00, 25.0, 2.0, 'Fresh basil leaves'),
('Parsley', 'Herb', 18.00, 30.0, 2.0, 'Fresh parsley'),
('Mint', 'Herb', 22.00, 20.0, 2.0, 'Fresh mint leaves'),
('Cilantro', 'Herb', 19.00, 28.0, 2.0, 'Fresh cilantro'),
('Dill', 'Herb', 21.00, 22.0, 2.0, 'Fresh dill');

-- Insert sample data for OrderInfo (at least 25 rows)
INSERT INTO OrderInfo (customerId, carrierId, orderDate, deliveryDate, subtotal, vatAmount, discountAmount, loyaltyDiscount, totalCost, status, deliveryAddress) VALUES
(2, 12, '2024-01-01 10:00:00', '2024-01-02 14:00:00', 37.92, 7.58, 0.00, 0.00, 45.50, 'Delivered', '456 Oak Ave'),
(3, 13, '2024-01-02 11:00:00', '2024-01-03 15:00:00', 26.67, 5.33, 0.00, 0.00, 32.00, 'Delivered', '789 Pine Rd'),
(4, 14, '2024-01-03 09:00:00', '2024-01-04 13:00:00', 56.04, 11.21, 0.00, 0.00, 67.25, 'Delivered', '321 Elm St'),
(5, 15, '2024-01-04 14:00:00', '2024-01-05 16:00:00', 23.75, 4.75, 0.00, 0.00, 28.50, 'Delivered', '654 Maple Dr'),
(6, 16, '2024-01-05 10:30:00', '2024-01-06 14:30:00', 46.46, 9.29, 0.00, 0.00, 55.75, 'Delivered', '987 Cedar Ln'),
(7, 17, '2024-01-06 08:00:00', '2024-01-07 12:00:00', 34.17, 6.83, 0.00, 0.00, 41.00, 'Delivered', '147 Birch Way'),
(8, 18, '2024-01-07 15:00:00', '2024-01-08 17:00:00', 61.25, 12.25, 0.00, 0.00, 73.50, 'Delivered', '258 Spruce Ct'),
(9, 19, '2024-01-08 11:00:00', '2024-01-09 15:00:00', 30.21, 6.04, 0.00, 0.00, 36.25, 'Delivered', '369 Willow Pl'),
(10, 20, '2024-01-09 13:00:00', '2024-01-10 16:00:00', 40.83, 8.17, 0.00, 0.00, 49.00, 'Delivered', '741 Aspen Blvd'),
(11, 12, '2024-01-10 09:30:00', '2024-01-11 13:30:00', 52.29, 10.46, 0.00, 0.00, 62.75, 'Delivered', '852 Poplar St'),
(2, 13, '2024-01-11 10:00:00', '2024-01-12 14:00:00', 32.08, 6.42, 0.00, 0.00, 38.50, 'Delivered', '456 Oak Ave'),
(3, 14, '2024-01-12 14:00:00', '2024-01-13 18:00:00', 45.00, 9.00, 0.00, 0.00, 54.00, 'Delivered', '789 Pine Rd'),
(4, 15, '2024-01-13 08:00:00', '2024-01-14 12:00:00', 24.79, 4.96, 0.00, 0.00, 29.75, 'Delivered', '321 Elm St'),
(5, 16, '2024-01-14 12:00:00', '2024-01-15 16:00:00', 59.38, 11.87, 0.00, 0.00, 71.25, 'Delivered', '654 Maple Dr'),
(6, 17, '2024-01-15 10:00:00', '2024-01-16 14:00:00', 36.25, 7.25, 0.00, 0.00, 43.50, 'Delivered', '987 Cedar Ln'),
(7, 18, '2024-01-16 11:00:00', NULL, 46.67, 9.33, 0.00, 0.00, 56.00, 'Assigned', '147 Birch Way'),
(8, 19, '2024-01-17 09:00:00', NULL, 28.96, 5.79, 0.00, 0.00, 34.75, 'Assigned', '258 Spruce Ct'),
(9, 20, '2024-01-18 13:00:00', NULL, 40.42, 8.08, 0.00, 0.00, 48.50, 'InTransit', '369 Willow Pl'),
(10, 12, '2024-01-19 10:00:00', NULL, 54.38, 10.87, 0.00, 0.00, 65.25, 'InTransit', '741 Aspen Blvd'),
(11, 13, '2024-01-20 14:00:00', NULL, 32.50, 6.50, 0.00, 0.00, 39.00, 'Pending', '852 Poplar St'),
(2, NULL, '2024-01-21 08:00:00', NULL, 43.96, 8.79, 0.00, 0.00, 52.75, 'Pending', '456 Oak Ave'),
(3, NULL, '2024-01-22 12:00:00', NULL, 22.92, 4.58, 0.00, 0.00, 27.50, 'Pending', '789 Pine Rd'),
(4, NULL, '2024-01-23 10:00:00', NULL, 50.83, 10.17, 0.00, 0.00, 61.00, 'Pending', '321 Elm St'),
(5, NULL, '2024-01-24 11:00:00', NULL, 36.88, 7.37, 0.00, 0.00, 44.25, 'Pending', '654 Maple Dr'),
(6, NULL, '2024-01-25 09:00:00', NULL, 48.75, 9.75, 0.00, 0.00, 58.50, 'Pending', '987 Cedar Ln');

-- Insert sample data for OrderItem (at least 25 rows, matching orders)
INSERT INTO OrderItem (orderId, productId, quantity, unitPrice, subtotal) VALUES
-- Order 1
(1, 1, 3.0, 8.50, 25.50),
(1, 2, 2.0, 6.00, 12.00),
(1, 11, 1.0, 7.00, 7.00),
-- Order 2
(2, 3, 2.0, 7.50, 15.00),
(2, 12, 2.0, 5.50, 11.00),
(2, 13, 1.0, 6.50, 6.50),
-- Order 3
(3, 4, 2.5, 12.00, 30.00),
(3, 5, 1.5, 15.00, 22.50),
(3, 14, 1.0, 4.00, 4.00),
(3, 15, 1.0, 5.00, 5.00),
(3, 21, 0.3, 20.00, 6.00),
-- Order 4
(4, 6, 1.0, 4.50, 4.50),
(4, 7, 2.0, 10.00, 20.00),
(4, 16, 0.5, 8.00, 4.00),
-- Order 5
(5, 8, 2.0, 9.00, 18.00),
(5, 9, 1.5, 11.00, 16.50),
(5, 10, 1.0, 9.50, 9.50),
(5, 17, 1.0, 6.00, 6.00),
(5, 18, 0.5, 9.00, 4.50),
(5, 22, 0.2, 18.00, 3.60),
-- Order 6
(6, 1, 2.0, 8.50, 17.00),
(6, 11, 2.0, 7.00, 14.00),
(6, 12, 1.0, 5.50, 5.50),
(6, 19, 0.5, 7.50, 3.75),
(6, 23, 0.15, 19.00, 2.85),
-- Order 7
(7, 2, 3.0, 6.00, 18.00),
(7, 3, 2.0, 7.50, 15.00),
(7, 4, 1.5, 12.00, 18.00),
(7, 5, 1.0, 15.00, 15.00),
(7, 14, 1.5, 4.00, 6.00),
(7, 24, 0.2, 21.00, 4.20),
-- Order 8
(8, 6, 2.0, 4.50, 9.00),
(8, 7, 1.5, 10.00, 15.00),
(8, 8, 1.0, 9.00, 9.00),
(8, 15, 2.0, 5.00, 10.00),
(8, 16, 1.0, 8.00, 8.00),
(8, 25, 0.25, 21.00, 5.25),
-- Order 9
(9, 9, 2.0, 11.00, 22.00),
(9, 10, 1.0, 9.50, 9.50),
(9, 17, 1.0, 6.00, 6.00),
(9, 18, 0.5, 9.00, 4.50),
(9, 21, 0.2, 20.00, 4.00),
-- Order 10
(10, 1, 3.0, 8.50, 25.50),
(10, 2, 2.0, 6.00, 12.00),
(10, 3, 1.5, 7.50, 11.25),
(10, 11, 2.0, 7.00, 14.00),
(10, 22, 0.3, 18.00, 5.40),
-- Order 11
(11, 4, 1.5, 12.00, 18.00),
(11, 5, 1.0, 15.00, 15.00),
(11, 12, 1.0, 5.50, 5.50),
-- Order 12
(12, 6, 2.0, 4.50, 9.00),
(12, 7, 2.0, 10.00, 20.00),
(12, 8, 1.5, 9.00, 13.50),
(12, 13, 2.0, 6.50, 13.00),
(12, 23, 0.25, 19.00, 4.75),
-- Order 13
(13, 9, 1.0, 11.00, 11.00),
(13, 10, 1.0, 9.50, 9.50),
(13, 14, 1.0, 4.00, 4.00),
(13, 24, 0.2, 21.00, 4.20),
-- Order 14
(14, 1, 4.0, 8.50, 34.00),
(14, 2, 3.0, 6.00, 18.00),
(14, 3, 2.0, 7.50, 15.00),
(14, 15, 1.0, 5.00, 5.00),
(14, 25, 0.3, 21.00, 6.30),
-- Order 15
(15, 4, 2.0, 12.00, 24.00),
(15, 5, 1.0, 15.00, 15.00),
(15, 16, 1.0, 8.00, 8.00),
(15, 22, 0.2, 18.00, 3.60),
-- Order 16
(16, 6, 3.0, 4.50, 13.50),
(16, 7, 2.0, 10.00, 20.00),
(16, 8, 1.5, 9.00, 13.50),
(16, 17, 1.0, 6.00, 6.00),
(16, 21, 0.15, 20.00, 3.00),
-- Order 17
(17, 9, 1.5, 11.00, 16.50),
(17, 10, 1.0, 9.50, 9.50),
(17, 18, 0.5, 9.00, 4.50),
(17, 23, 0.2, 19.00, 3.80),
-- Order 18
(18, 1, 2.5, 8.50, 21.25),
(18, 2, 2.0, 6.00, 12.00),
(18, 11, 1.5, 7.00, 10.50),
(18, 12, 1.0, 5.50, 5.50),
(18, 24, 0.25, 21.00, 5.25),
-- Order 19
(19, 3, 3.0, 7.50, 22.50),
(19, 4, 2.0, 12.00, 24.00),
(19, 5, 1.0, 15.00, 15.00),
(19, 13, 1.0, 6.50, 6.50),
(19, 25, 0.2, 21.00, 4.20),
-- Order 20
(20, 6, 2.0, 4.50, 9.00),
(20, 7, 1.5, 10.00, 15.00),
(20, 14, 2.0, 4.00, 8.00),
(20, 15, 1.0, 5.00, 5.00),
(20, 21, 0.1, 20.00, 2.00),
-- Order 21
(21, 8, 2.5, 9.00, 22.50),
(21, 9, 1.5, 11.00, 16.50),
(21, 10, 1.0, 9.50, 9.50),
(21, 16, 0.5, 8.00, 4.00),
(21, 22, 0.15, 18.00, 2.70),
-- Order 22
(22, 1, 1.5, 8.50, 12.75),
(22, 2, 1.0, 6.00, 6.00),
(22, 11, 1.0, 7.00, 7.00),
(22, 23, 0.1, 19.00, 1.90),
-- Order 23
(23, 3, 2.5, 7.50, 18.75),
(23, 4, 2.0, 12.00, 24.00),
(23, 5, 1.5, 15.00, 22.50),
(23, 12, 1.0, 5.50, 5.50),
(23, 24, 0.2, 21.00, 4.20),
-- Order 24
(24, 6, 2.0, 4.50, 9.00),
(24, 7, 2.0, 10.00, 20.00),
(24, 8, 1.0, 9.00, 9.00),
(24, 13, 1.0, 6.50, 6.50),
(24, 25, 0.15, 21.00, 3.15),
-- Order 25
(25, 9, 2.5, 11.00, 27.50),
(25, 10, 1.5, 9.50, 14.25),
(25, 14, 1.0, 4.00, 4.00),
(25, 15, 1.0, 5.00, 5.00),
(25, 21, 0.2, 20.00, 4.00);








