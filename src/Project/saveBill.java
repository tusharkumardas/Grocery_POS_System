/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Project;
import Project.ConnectionProvider;
import java.sql.Connection;
import java.sql.PreparedStatement;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import IDgenerator.SalesIDGenerator;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.SQLException;
import java.io.File;
/**
 *
 * @author Tushar Kumar Das
 */
public class saveBill {
      public static void saveBill(JTable jTableBillItems,
                                JTextField jTextFieldCustomerName,
                                JTextField jTextFieldCustomerPhone,
                                String paymentMode,
                                double amountPaid) {
        Connection con = null;
        PreparedStatement psSales = null;
        PreparedStatement psItems = null;
        PreparedStatement psCustomer = null;
        ResultSet rs = null;

        try {
            con = ConnectionProvider.getCon();
            con.setAutoCommit(false); // Start transaction

            // 1. Check if customer exists
            int customerId = -1;
            String phone = jTextFieldCustomerPhone.getText().trim();
            String name = jTextFieldCustomerName.getText().trim();

            String sqlCheckCustomer = "SELECT id FROM customers WHERE phone = ?";
            psCustomer = con.prepareStatement(sqlCheckCustomer);
            psCustomer.setString(1, phone);
            rs = psCustomer.executeQuery();

            if (rs.next()) {
                customerId = rs.getInt("id");
            } else {
                // Insert new customer
                String sqlInsertCustomer = "INSERT INTO customers (name, phone) VALUES (?, ?)";
                psCustomer = con.prepareStatement(sqlInsertCustomer, Statement.RETURN_GENERATED_KEYS);
                psCustomer.setString(1, name);
                psCustomer.setString(2, phone);
                psCustomer.executeUpdate();
                rs = psCustomer.getGeneratedKeys();
                if (rs.next()) {
                    customerId = rs.getInt(1);
                }
            }

            // 2. Insert into sales
            String invoiceNo = SalesIDGenerator.generateInvoiceNo();
            double totalAmount = BillingCalculator.calculateTotalAmount() - BillingCalculator.calculateTotalGST();
            double gstAmount   = BillingCalculator.calculateTotalGST();
            double netAmount   = BillingCalculator.calculateNetAmount();

            double amountDue = netAmount - amountPaid;

            String sqlSales = "INSERT INTO sales (invoice_no, customer_id, total_amount, total_gst, net_amount, amount_paid, amount_due, payment_mode) " +
                              "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
            psSales = con.prepareStatement(sqlSales, Statement.RETURN_GENERATED_KEYS);
            psSales.setString(1, invoiceNo);
            psSales.setInt(2, customerId);
            psSales.setDouble(3, totalAmount);
            psSales.setDouble(4, gstAmount);
            psSales.setDouble(5, netAmount);
            psSales.setDouble(6, amountPaid);
            psSales.setDouble(7, amountDue);
            psSales.setString(8, paymentMode);

            psSales.executeUpdate();

            // Get generated sale_id
            rs = psSales.getGeneratedKeys();
            int saleId = -1;
            if (rs.next()) {
                saleId = rs.getInt(1);
            }

            // 2.1 Generate and save PDF
            String pdfDir = "invoices"; // ensure this folder exists
            File dir = new File(pdfDir);
            if (!dir.exists()) dir.mkdirs();

            String pdfPath = pdfDir + "/" + invoiceNo + ".pdf";

            BillPDFGenerator.generatePDF(
                jTableBillItems,
                name,
                phone,
                invoiceNo,
                totalAmount,
                gstAmount,
                netAmount,
                amountPaid,
                amountDue,
                paymentMode,
                pdfPath
            );

            // 2.2 Update sales with PDF path
            String sqlUpdatePdf = "UPDATE sales SET pdf_path = ? WHERE id = ?";
            PreparedStatement psPdf = con.prepareStatement(sqlUpdatePdf);
            psPdf.setString(1, pdfPath);
            psPdf.setInt(2, saleId);
            psPdf.executeUpdate();
            psPdf.close();

            // 3. Insert sales items
            String sqlItem = "INSERT INTO sales_items (sale_id, product_id, qty, sale_price, gst, gst_amount, total_price) VALUES (?, ?, ?, ?, ?, ?, ?)";
            psItems = con.prepareStatement(sqlItem);

            for (int i = 0; i < jTableBillItems.getRowCount(); i++) {
                int productId   = Integer.parseInt(jTableBillItems.getValueAt(i, 0).toString());
                int qty         = Integer.parseInt(jTableBillItems.getValueAt(i, 3).toString());
                double price    = Double.parseDouble(jTableBillItems.getValueAt(i, 4).toString());
                double gst      = Double.parseDouble(jTableBillItems.getValueAt(i, 5).toString());

                double itemTotal = qty * price;
                double gstValue  = (itemTotal * gst) / 100.0;
                double totalWithGst = itemTotal + gstValue;

                psItems.setInt(1, saleId);
                psItems.setInt(2, productId);
                psItems.setInt(3, qty);
                psItems.setDouble(4, price);
                psItems.setDouble(5, gst);
                psItems.setDouble(6, gstValue);
                psItems.setDouble(7, totalWithGst);

                psItems.addBatch();
            }

            psItems.executeBatch();
            con.commit();

            JOptionPane.showMessageDialog(null, "Bill saved successfully with Invoice No: " + invoiceNo);

        } catch (Exception e) {
            try {
                if (con != null) con.rollback();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Error saving bill: " + e.getMessage());
        } finally {
            try {
                if (psSales != null) psSales.close();
                if (psItems != null) psItems.close();
                if (psCustomer != null) psCustomer.close();
                if (rs != null) rs.close();
                if (con != null) con.close();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }
}
