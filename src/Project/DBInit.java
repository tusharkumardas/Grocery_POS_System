/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Project;
import java.sql.Connection;
import java.sql.Statement;
import java.sql.ResultSet;
import org.mindrot.jbcrypt.BCrypt;
/**
 *
 * @author Tushar Kumar Das
 */
public class DBInit {
    public static void initialize() {

        try (Connection con = ConnectionProvider.getCon();
             Statement st = con.createStatement()) {

            /* ================= COMPANY SETTINGS ================= */
            st.execute("""
                CREATE TABLE IF NOT EXISTS company_settings (
                    id INT PRIMARY KEY,
                    company_name VARCHAR(200),
                    gst_no VARCHAR(50),
                    contact_no VARCHAR(20),
                    address VARCHAR(200)
                )
            """);

            /* ================= USERS ================= */
            st.execute("""
                CREATE TABLE IF NOT EXISTS users (
                    id INT AUTO_INCREMENT PRIMARY KEY,
                    username VARCHAR(100) UNIQUE NOT NULL,
                    pin_hash VARCHAR(255) NOT NULL,
                    role ENUM('admin','manager','cashier','staff') DEFAULT 'staff',
                    status ENUM('active','inactive','suspended') DEFAULT 'active',
                    failed_attempts INT DEFAULT 0,
                    last_login DATETIME,
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                        ON UPDATE CURRENT_TIMESTAMP
                )
            """);

            /* ================= STAFF ================= */
            st.execute("""
                CREATE TABLE IF NOT EXISTS staff (
                    id INT AUTO_INCREMENT PRIMARY KEY,
                    staff_code VARCHAR(50) UNIQUE,
                    name VARCHAR(200) NOT NULL,
                    dob DATE,
                    phone VARCHAR(15) UNIQUE,
                    email VARCHAR(150) UNIQUE,
                    role VARCHAR(100),
                    salary DOUBLE,
                    join_date DATE NOT NULL,
                    govt_id_path VARCHAR(255),
                    status ENUM('Active','Inactive','Resigned','Terminated') DEFAULT 'Active',
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                        ON UPDATE CURRENT_TIMESTAMP
                )
            """);

            /* ================= CUSTOMERS ================= */
            st.execute("""
                CREATE TABLE IF NOT EXISTS customers (
                    id INT AUTO_INCREMENT PRIMARY KEY,
                    name VARCHAR(200) NOT NULL,
                    phone VARCHAR(15) UNIQUE,
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                        ON UPDATE CURRENT_TIMESTAMP
                )
            """);

            /* ================= PRODUCTS ================= */
            st.execute("""
                CREATE TABLE IF NOT EXISTS products (
                    id INT AUTO_INCREMENT PRIMARY KEY,
                    item_code VARCHAR(100) UNIQUE,
                    product_name VARCHAR(200),
                    qty INT,
                    purchase_price DOUBLE,
                    sale_price DOUBLE,
                    mrp DOUBLE,
                    exp_date DATE,
                    brand_name VARCHAR(200),
                    barcode VARCHAR(100) UNIQUE,
                    gst DOUBLE,
                    stock_alert INT,
                    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                        ON UPDATE CURRENT_TIMESTAMP,
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                )
            """);

            /* ================= SALES ================= */
            st.execute("""
                CREATE TABLE IF NOT EXISTS sales (
                    id INT AUTO_INCREMENT PRIMARY KEY,
                    invoice_no VARCHAR(50) UNIQUE NOT NULL,
                    customer_id INT NOT NULL,
                    total_amount DOUBLE NOT NULL,
                    total_gst DOUBLE NOT NULL,
                    net_amount DOUBLE NOT NULL,
                    amount_paid DOUBLE DEFAULT 0,
                    amount_due DOUBLE DEFAULT 0,
                    payment_mode VARCHAR(50) NOT NULL,
                    pdf_path VARCHAR(255),
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                        ON UPDATE CURRENT_TIMESTAMP
                )
            """);

            /* ================= SALES ITEMS ================= */
            st.execute("""
                CREATE TABLE IF NOT EXISTS sales_items (
                    id INT AUTO_INCREMENT PRIMARY KEY,
                    sale_id INT NOT NULL,
                    product_id INT NOT NULL,
                    qty INT NOT NULL,
                    sale_price DOUBLE NOT NULL,
                    gst DOUBLE NOT NULL,
                    gst_amount DOUBLE NOT NULL,
                    total_price DOUBLE NOT NULL,
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                )
            """);

            /* ================= PURCHASES ================= */
            st.execute("""
                CREATE TABLE IF NOT EXISTS purchases (
                    id INT AUTO_INCREMENT PRIMARY KEY,
                    purchase_no VARCHAR(50) UNIQUE NOT NULL,
                    purchase_date DATE NOT NULL,
                    supplier_name VARCHAR(200) NOT NULL,
                    supplier_phone VARCHAR(15),
                    total_amount DOUBLE NOT NULL,
                    total_gst DOUBLE NOT NULL,
                    net_amount DOUBLE NOT NULL,
                    amount_paid DOUBLE DEFAULT 0,
                    amount_due DOUBLE NOT NULL,
                    payment_mode VARCHAR(50),
                    bill_path VARCHAR(500),
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                )
            """);

            /* ================= EXPENSES ================= */
            st.execute("""
                CREATE TABLE IF NOT EXISTS expenses (
                    id INT AUTO_INCREMENT PRIMARY KEY,
                    expense_code VARCHAR(50) UNIQUE,
                    expense_date DATE NOT NULL,
                    category VARCHAR(100) NOT NULL,
                    description TEXT,
                    amount DOUBLE NOT NULL,
                    payment_mode VARCHAR(50),
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                )
            """);

            /* ================= DEFAULT COMPANY ROW ================= */
            st.execute("""
                INSERT INTO company_settings (id)
                SELECT 1 FROM DUAL
                WHERE NOT EXISTS (SELECT 1 FROM company_settings)
            """);

            /* ================= DEFAULT ADMIN ================= */
            ResultSet rs = st.executeQuery(
                "SELECT COUNT(*) FROM users WHERE role='admin'"
            );
            rs.next();

            if (rs.getInt(1) == 0) {
                String adminHash = BCrypt.hashpw("123456", BCrypt.gensalt());

                st.execute(
                    "INSERT INTO users (username, pin_hash, role, status) VALUES (" +
                    "'admin', '" + adminHash + "', 'admin', 'active')"
                );
            }

            System.out.println("✅ Database initialized successfully");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
