# Local Greengrocer Project

A comprehensive JavaFX + JDBC + MySQL application for managing a local greengrocer business with customer, carrier, and owner management features.

**CMPE343 Project 3 - Group 05**

---

## 📋 Table of Contents

- [Requirements](#-requirements)
- [Installation](#-installation)
- [Database Setup](#-database-setup)
- [Running the Project](#-running-the-project)
- [Login Credentials](#-login-credentials)
- [Features](#-features)
- [Project Structure](#-project-structure)
- [Architecture](#-architecture)
- [Business Rules](#-business-rules)
- [Troubleshooting](#-troubleshooting)
- [Technologies Used](#-technologies-used)
- [Documentation](#-documentation)

---

## 🔧 Requirements

The following software must be installed to run the project:

### 1. Java Development Kit (JDK)
- **Version**: JDK 11 or higher (JDK 17 recommended)
- **Download**: [Oracle JDK](https://www.oracle.com/java/technologies/downloads/) or [OpenJDK](https://adoptium.net/)
- **Check**: Run `java -version` in the command line

### 2. Apache Maven
- **Version**: 3.6.0 or higher
- **Download**: [Maven Download](https://maven.apache.org/download.cgi)
- **Installation**: 
  - Windows: Extract ZIP file and add `bin` folder to system PATH
  - Alternative: [Maven Installation Guide](https://maven.apache.org/install.html)
- **Check**: Run `mvn -version` in the command line

### 3. MySQL Server
- **Version**: MySQL 5.7 or higher (MySQL 8.0 recommended)
- **Download**: [MySQL Community Server](https://dev.mysql.com/downloads/mysql/)
- **Installation**: 
  - Windows: Use MySQL Installer
  - Set root password during installation (default: `1234`)
- **Check**: Ensure MySQL service is running

### 4. MySQL Workbench (Optional - Recommended)
- **Download**: [MySQL Workbench](https://dev.mysql.com/downloads/workbench/)
- Useful for database management and SQL file imports

---

## 🚀 Installation

### Step 1: Download the Project

Download or clone the project to your computer:
```bash
git clone <repository-url>
cd OOP-Project3
```

### Step 2: Download Maven Dependencies

Run the following command in the project folder:
```bash
mvn clean install
```

This command will automatically download all dependencies (JavaFX, MySQL Connector, PDFBox, etc.).

**Note**: Internet connection is required. The first run may take some time as dependencies are downloaded.

### Step 3: Check Database Connection Settings

Open `src/main/java/com/group05/greengrocer/util/DatabaseAdapter.java` and verify your MySQL password:

```java
private static final String DB_URL = "jdbc:mysql://localhost:3306/greengrocer_db";
private static final String DB_USER = "root";
private static final String DB_PASSWORD = "1234"; // Update with your MySQL password
```

**Important**: If your MySQL root password is not `1234`, update it in this file.

---

## 💾 Database Setup

### Method 1: Automatic Setup (Windows - Recommended)

1. Double-click `setup-database.bat`
2. The script will automatically:
   - Create the database
   - Create tables
   - Load sample data

**Note**: If your MySQL password is not `1234`, edit `setup-database.bat` and update the password.

### Method 2: MySQL Workbench Setup (Easiest)

See `IMPORT_WITH_WORKBENCH.md` for detailed steps.

**Quick Summary:**
1. Open MySQL Workbench
2. Connect to your MySQL server (root / your password)
3. Go to **Server** → **Data Import**
4. Select **"Import from Self-Contained File"**
5. Select `database/schema.sql` file
6. Select `greengrocer_db` as **Default Target Schema** (create if it doesn't exist)
7. Click **Start Import**

### Method 3: Command Line Setup

1. Open MySQL command line:
   ```bash
   mysql -u root -p
   ```

2. Enter your password

3. Create the database:
   ```sql
   CREATE DATABASE IF NOT EXISTS greengrocer_db;
   USE greengrocer_db;
   ```

4. Import the SQL file:
   ```sql
   source database/schema.sql;
   ```
   
   Or on Windows:
   ```bash
   mysql -u root -p greengrocer_db < database\schema.sql
   ```

### Verify Database Setup

In MySQL Workbench or command line:
```sql
USE greengrocer_db;
SHOW TABLES;
```

You should see the following tables:
- `UserInfo`
- `ProductInfo`
- `OrderInfo`
- `OrderItem`
- `Coupon`
- `CarrierRating`
- `Message`

To check sample data:
```sql
SELECT * FROM UserInfo LIMIT 5;
SELECT * FROM ProductInfo LIMIT 5;
```

**Note**: The application automatically runs database migrations on startup to add missing columns and tables if needed.

---

## ▶️ Running the Project

### Method 1: Run with Maven (Recommended)

In the command line, from the project folder:
```bash
mvn clean javafx:run
```

### Method 2: Windows Batch Script

Double-click `run.bat` or run in command line:
```bash
run.bat
```

### Method 3: Run from IDE

#### IntelliJ IDEA
1. Open project via **File** → **Open**
2. Import as Maven project
3. **Run** → **Edit Configurations**
4. Click **+** and select **Maven**
5. **Command line**: `clean javafx:run`
6. **Apply** and **Run**

Alternatively, open `Main.java` and right-click **Run 'Main.main()'** (JavaFX module settings may be required).

#### Eclipse
1. Open project via **File** → **Import** → **Existing Maven Projects**
2. **Run** → **Run Configurations**
3. Create **Maven Build**
4. **Goals**: `clean javafx:run`
5. **Run**

#### VS Code
1. Install Java Extension Pack
2. Open the project
3. Run `mvn clean javafx:run` in the terminal

---

## 🔐 Login Credentials

The application comes with sample users:

### Customer
- **Username**: `customer1`
- **Password**: `customer123`

### Carrier
- **Username**: `carrier1`
- **Password**: `carrier123`

### Owner
- **Username**: `owner1`
- **Password**: `owner123`

**Note**: More sample users are available in the database (`customer2`, `customer3`, `carrier2`, etc.)

---

## ✨ Features

### Customer Features
- ✅ View products grouped by type (using TitledPane)
- ✅ Sort products by name (A-Z / Z-A) or price (ascending / descending)
- ✅ Filter to show only in-stock products
- ✅ Search products (case-insensitive)
- ✅ Add products to cart by weight (kg)
- ✅ Input validation (prevents negative, zero, or non-numeric values)
- ✅ Merge same products in cart
- ✅ View cart in separate window
- ✅ View available coupons
- ✅ Apply coupons at checkout
- ✅ Complete orders with:
  - Delivery time validation (within 48 hours)
  - Accurate total cost calculation
  - Threshold rule (price doubles if stock <= threshold)
  - Minimum cart value (200 TL)
- ✅ Generate and download PDF invoices
- ✅ View product images
- ✅ Rate carriers after order completion
- ✅ Send messages to owner

### Carrier Features
- ✅ View available orders
- ✅ View assigned orders
- ✅ View completed orders
- ✅ Select orders (prevents multiple carriers from selecting the same order)
- ✅ Prevent accepting orders with past delivery dates
- ✅ Mark orders as completed
- ✅ Cancel assigned orders (returns them to available orders)

### Owner Features
- ✅ Product management:
  - Add/update/delete products
  - Update product stock
  - Apply discounts to products
  - Set product images (via URL)
  - View low stock alerts (when stock falls below threshold)
- ✅ Carrier management:
  - Hire carriers
  - Fire carriers
  - View carrier performance
- ✅ Reports:
  - Profit report
  - Delivered orders report
  - Carrier performance report (with charts)
- ✅ Coupon management:
  - Create coupons for all customers or specific customers
  - Set custom coupon names
  - View all coupons
- ✅ Message management:
  - View customer messages
  - Reply to customer messages
- ✅ Loyalty program management:
  - Adjust loyalty threshold (minimum completed orders)
  - Adjust loyalty discount percentage
- ✅ View carrier ratings

### General Features
- ✅ Full-screen application mode
- ✅ Modern and responsive UI
- ✅ Input validation for all forms
- ✅ PDF invoice generation and storage (as CLOB in database)
- ✅ Product images stored as BLOBs in database
- ✅ Comprehensive JavaDoc documentation
- ✅ Automatic database migrations

---

## 📁 Project Structure

```
OOP-Project3/
├── database/
│   ├── schema.sql              # Database schema and sample data
│   └── add_image_columns.sql   # Additional image columns
├── src/
│   └── main/
│       ├── java/
│       │   └── com/group05/greengrocer/
│       │       ├── app/        # Main application entry point
│       │       ├── controller/ # JavaFX controllers
│       │       ├── model/      # POJO/entity classes
│       │       ├── repository/ # Database access layer
│       │       ├── service/    # Business logic layer
│       │       └── util/       # Utility classes
│       └── resources/
│           ├── css/           # Style files
│           ├── fxml/          # FXML view files
│           └── images/       # Image resources
├── target/
│   └── reports/
│       └── apidocs/          # Generated JavaDoc documentation
├── lib/                       # External JAR files
│   ├── mysql-connector-j-8.2.0.jar
│   └── pdfbox-2.0.29.jar
├── invoices/                  # Generated PDF invoices
├── pom.xml                    # Maven configuration file
├── run.bat                     # Windows run script
├── setup-database.bat          # Database setup script
├── DATABASE_SETUP.md           # Database setup documentation
├── IMPORT_WITH_WORKBENCH.md    # MySQL Workbench import guide
└── README.md                   # This file
```

---

## 🏗️ Architecture

The project follows **MVC (Model-View-Controller)** architecture:

- **Model**: Entity classes (User, Product, Order, OrderItem, Coupon, CarrierRating, Message)
- **View**: UI defined by FXML files
- **Controller**: JavaFX controllers that manage user interactions

### Layer Separation

- **Repository Layer**: Manages all database access (JDBC/SQL)
- **Service Layer**: Contains business logic and enforces business rules
- **Controller Layer**: Manages UI events and delegates to services
- **No SQL in Controllers**: All database operations are in repositories
- **No Business Logic in Repositories**: Business rules are implemented in services

### Design Patterns

- **Singleton Pattern**: Used for DatabaseAdapter and Session management
- **Repository Pattern**: Data access abstraction
- **Service Pattern**: Business logic encapsulation
- **MVC Pattern**: Separation of concerns

---

## 📜 Business Rules

1. **Threshold Pricing**: If ordered quantity causes product stock to fall below threshold, price per kg doubles.
2. **Delivery Time**: Delivery must be scheduled within 48 hours from order placement.
3. **Stock Validation**: Products with stock = 0 are not shown to customers.
4. **Order Assignment**: Only one carrier can be assigned to an order (transaction-based).
5. **Minimum Cart Value**: Minimum cart total is 200 TL.
6. **Past Delivery Dates**: Carriers cannot accept orders with delivery dates in the past.
7. **Loyalty Program**: Customers who complete a certain number of orders (configurable) receive a discount percentage (configurable) on future orders.
8. **Coupon System**: Owners can create coupons for all customers or specific customers with custom names.
9. **Low Stock Alert**: Owner is notified when product stock falls below threshold.

---

## 🔧 Troubleshooting

### Database Connection Issues

**Error**: `Access denied for user 'root'@'localhost'`
- Check your MySQL password
- Ensure the password in `DatabaseAdapter.java` is correct
- Verify MySQL service is running

**Error**: `Unknown database 'greengrocer_db'`
- Create the database: `CREATE DATABASE greengrocer_db;`
- Run `setup-database.bat` script

**Error**: `Table doesn't exist`
- Import `database/schema.sql` file
- Re-run import using MySQL Workbench or command line
- The application automatically runs migrations on startup to add missing tables/columns

### JavaFX Issues

**Error**: `Error: JavaFX runtime components are missing`
- Ensure Maven dependencies are downloaded: `mvn clean install`
- Use `mvn javafx:run` command (instead of IDE)

**Error**: `Module javafx.controls not found`
- Ensure `pom.xml` is correct
- Reload Maven project in IDE

### Compilation Issues

**Error**: `Package does not exist` or `Cannot find symbol`
- Download Maven dependencies: `mvn clean install`
- Re-import Maven project in IDE
- Delete `target` folder and recompile: `mvn clean compile`

**Error**: `FXML file not found`
- Check that FXML files exist in `src/main/resources/fxml/` folder
- Verify file paths are correct

### Maven Issues

**Error**: `'mvn' is not recognized as an internal or external command`
- Check Maven is installed: `mvn -version`
- Add Maven to system PATH
- Ensure Maven plugin is installed in IDE

**Error**: `Could not resolve dependencies`
- Check your internet connection
- Verify Maven repository access
- Check proxy settings (if needed)

### JavaDoc Generation Issues

**Error**: `MalformedInputException: Input length = 1`
- Run `mvn clean` to clear old JavaDoc files
- Then run `mvn javadoc:javadoc`
- UTF-8 encoding is configured in `pom.xml`

### Other Issues

**Application won't start**
- Check Java version: `java -version` (should be JDK 11+)
- Verify MySQL service is running
- Check database connection settings

**PDF invoices not generating**
- Ensure `invoices` folder is writable
- Check disk space
- Verify PDFBox dependency is downloaded

**Images not displaying**
- Check image URLs are valid and accessible
- Verify image URLs are direct image links (not Wikipedia page URLs)
- Check database BLOB storage

---

## 📚 Technologies Used

- **Java**: JDK 11+
- **JavaFX**: 17.0.2 (GUI framework)
- **MySQL**: 5.7+ (Database)
- **JDBC**: MySQL Connector/J 8.0.33
- **Maven**: Dependency management and build tool
- **Apache PDFBox**: 2.0.29 (PDF invoice generation)
- **SHA-256**: Password hashing

---

## 📖 Documentation

### JavaDoc

Comprehensive JavaDoc documentation is available for all classes and methods.

**Generate JavaDoc:**
```bash
mvn javadoc:javadoc
```

**View JavaDoc:**
- Location: `target/reports/apidocs/index.html`
- Open in browser to view complete API documentation

### Additional Documentation

- `DATABASE_SETUP.md`: Detailed database setup instructions
- `IMPORT_WITH_WORKBENCH.md`: MySQL Workbench import guide

---

## 📝 Notes

- Invoice PDFs are generated using Apache PDFBox and stored as CLOBs in the database.
- Product images are stored as BLOBs in the database (via URL links).
- The application uses singleton pattern for DatabaseAdapter and Session management.
- All database operations use PreparedStatement to prevent SQL injection.
- Input validation is performed at both UI and service layers.
- The application automatically runs database migrations on startup.
- All tabs in Owner and Carrier dashboards are non-closable.
- Application opens in full-screen mode by default.
- Comprehensive JavaDoc documentation is available for all methods and classes.

---

## 👥 Contributors

**Group 05 - CMPE343 Project 3**

---

## 📄 License

This project is created for educational purposes (CMPE343 Project 3).

---

## 📞 Support

If you encounter issues:
1. Check the **Troubleshooting** section in this README
2. Examine project structure and code comments
3. Generate and review JavaDoc documentation

---

**Last Updated**: January 2025
