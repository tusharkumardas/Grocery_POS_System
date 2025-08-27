/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package grocery;
import IDgenerator.PurchaseIDGenerator;
import Project.ConnectionProvider;
import java.sql.Connection;
import java.sql.PreparedStatement;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;
import java.sql.ResultSet;
import Project.PurchasePDFExporter;
import javax.swing.JFileChooser;
import java.nio.file.Files;                 // For copying files
import java.nio.file.StandardCopyOption;
import java.io.File;    
import java.awt.Desktop;
/**
 *
 * @author Tushar Kumar Das
 */
public class Purchase extends javax.swing.JFrame {
    public void loadPurchaseTable() {
    Connection con = null;
    PreparedStatement ps = null;
    ResultSet rs = null;

    try {
        con = ConnectionProvider.getCon();
        String sql = "SELECT id, purchase_no, supplier_name, supplier_phone, total_amount, " +
                     "total_gst, net_amount, amount_paid, amount_due, payment_mode, created_at, bill_path " +
                     "FROM purchases";
        ps = con.prepareStatement(sql);
        rs = ps.executeQuery();

        // Table Model (columns match your table)
        DefaultTableModel model = new DefaultTableModel(
            new Object[]{
                "ID", "Purchase No", "Supplier Name", "Supplier Phone",
                "Total Amount", "Total GST", "Net Amount",
                "Amount Paid", "Amount Due", "Payment Mode", "Created At","Bill Path"
            }, 0
        );

        // Fill data row by row
        while (rs.next()) {
            model.addRow(new Object[]{
                rs.getInt("id"),
                rs.getString("purchase_no"),
                rs.getString("supplier_name"),
                rs.getString("supplier_phone"),
                rs.getDouble("total_amount"),
                rs.getDouble("total_gst"),
                rs.getDouble("net_amount"),
                rs.getDouble("amount_paid"),
                rs.getDouble("amount_due"),
                rs.getString("payment_mode"),
                rs.getTimestamp("created_at"),
                rs.getString("bill_path")
            });
        }

        // Set model into JTable
        purchaseTable.setModel(model);

        // Hide ID column (index 0)
        purchaseTable.getColumnModel().getColumn(0).setMinWidth(0);
        purchaseTable.getColumnModel().getColumn(0).setMaxWidth(0);
        purchaseTable.getColumnModel().getColumn(0).setWidth(0);
        
        purchaseTable.getColumnModel().getColumn(11).setMinWidth(0);
        purchaseTable.getColumnModel().getColumn(11).setMaxWidth(0);
        purchaseTable.getColumnModel().getColumn(11).setWidth(0);

    } catch (Exception e) {
        e.printStackTrace();
        JOptionPane.showMessageDialog(this, "Error loading purchases: " + e.getMessage());
    } finally {
        try {
            if (rs != null) rs.close();
            if (ps != null) ps.close();
            if (con != null) con.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
    private void addPurchase() {
    // Generate ID
    String purchaseNo = PurchaseIDGenerator.generatePurchaseID();

    // ---- Date from JTextField ----
    String dateStr = txtPurchaseDate.getText().trim();
    if (dateStr.isEmpty()) {
        JOptionPane.showMessageDialog(this, "Please enter date (YYYY-MM-DD).");
        return;
    }
    java.sql.Date purchaseDate;
    try {
        purchaseDate = java.sql.Date.valueOf(dateStr); // Works only for YYYY-MM-DD
    } catch (IllegalArgumentException ex) {
        JOptionPane.showMessageDialog(this, "Invalid date format! Use YYYY-MM-DD.");
        return;
    }

    // ---- Other fields ----
    String supplierName  = txtSupplierName.getText().trim();
    String supplierPhone = txtSupplierPhone.getText().trim();
    double totalAmount, totalGst, netAmount, amountPaid;

    try {
        totalAmount = Double.parseDouble(txtTotalAmount.getText().trim());
        totalGst    = Double.parseDouble(txtTotalGST.getText().trim());
        netAmount   = Double.parseDouble(txtNetAmount.getText().trim());
        amountPaid  = Double.parseDouble(txtAmountPaid.getText().trim());
    } catch (NumberFormatException nfe) {
        JOptionPane.showMessageDialog(this, "Amounts must be valid numbers.");
        return;
    }
    double amountDue = netAmount - amountPaid;
    String paymentMode = cmbPaymentMode.getSelectedItem().toString();

    // ---- Insert query ----
    String sql = "INSERT INTO purchases (" +
                 "purchase_no, purchase_date, supplier_name, supplier_phone, " +
                 "total_amount, total_gst, net_amount, amount_paid, amount_due, payment_mode" +
                 ") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

    try (Connection con = ConnectionProvider.getCon();
         PreparedStatement ps = con.prepareStatement(sql)) {

        ps.setString(1, purchaseNo);
        ps.setDate(2, purchaseDate);   // <-- this will now work
        ps.setString(3, supplierName);
        ps.setString(4, supplierPhone);
        ps.setDouble(5, totalAmount);
        ps.setDouble(6, totalGst);
        ps.setDouble(7, netAmount);
        ps.setDouble(8, amountPaid);
        ps.setDouble(9, amountDue);
        ps.setString(10, paymentMode);

        int rows = ps.executeUpdate();
        if (rows > 0) {
            JOptionPane.showMessageDialog(this, "Purchase added successfully!\nNo: " + purchaseNo);
            loadPurchaseTable();

            // ---- Clear fields ----
            txtPurchaseDate.setText("");
            txtSupplierName.setText("");
            txtSupplierPhone.setText("");
            txtTotalAmount.setText("");
            txtTotalGST.setText("");
            txtNetAmount.setText("");
            txtAmountPaid.setText("");
            txtAmountDue.setText("");
            cmbPaymentMode.setSelectedIndex(0);
        }
    } catch (Exception e) {
        e.printStackTrace();
        JOptionPane.showMessageDialog(this, "Error adding purchase: " + e.getMessage());
    }
}
private void clearPurchaseFields() {
    txtPurchaseDate.setText("");  // reset JDateChooser
    txtSupplierName.setText("");
    txtSupplierPhone.setText("");
    txtTotalAmount.setText("");
    txtTotalGST.setText("");
    txtNetAmount.setText("");
    txtAmountPaid.setText("");
    cmbPaymentMode.setSelectedIndex(0); // Reset to first option
}
private void deletePurchase() {
    int selectedRow = purchaseTable.getSelectedRow();
    if (selectedRow == -1) {
        JOptionPane.showMessageDialog(this, "Please select a record to delete.");
        return;
    }

    // Get purchase_no from JTable (assuming it's column index 1)
    String purchaseNo = purchaseTable.getValueAt(selectedRow, 1).toString();

    int confirm = JOptionPane.showConfirmDialog(
            this,
            "Are you sure you want to delete Purchase No: " + purchaseNo + "?",
            "Confirm Delete",
            JOptionPane.YES_NO_OPTION
    );

    if (confirm == JOptionPane.YES_OPTION) {
        Connection con = null;
        PreparedStatement ps = null;

        try {
            con = ConnectionProvider.getCon();
            String sql = "DELETE FROM purchases WHERE purchase_no = ?";
            ps = con.prepareStatement(sql);
            ps.setString(1, purchaseNo);

            int rows = ps.executeUpdate();
            if (rows > 0) {
                JOptionPane.showMessageDialog(this, "Purchase deleted successfully!");
                loadPurchaseTable(); // refresh table
            } else {
                JOptionPane.showMessageDialog(this, "Failed to delete purchase!");
            }
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error deleting purchase: " + e.getMessage());
        } finally {
            try {
                if (ps != null) ps.close();
                if (con != null) con.close();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }
}

    /**
     * Creates new form Purchase
     */
    public Purchase() {
        initComponents();
        loadPurchaseTable();
        this.setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        txtSupplierPhone = new javax.swing.JTextField();
        txtTotalAmount = new javax.swing.JTextField();
        jLabel3 = new javax.swing.JLabel();
        txtSupplierName = new javax.swing.JTextField();
        jScrollPane1 = new javax.swing.JScrollPane();
        jScrollPane2 = new javax.swing.JScrollPane();
        purchaseTable = new javax.swing.JTable();
        jButton1 = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();
        jButton3 = new javax.swing.JButton();
        jButton4 = new javax.swing.JButton();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        txtTotalGST = new javax.swing.JTextField();
        txtNetAmount = new javax.swing.JTextField();
        txtPurchaseDate = new javax.swing.JTextField();
        jLabel9 = new javax.swing.JLabel();
        txtAmountPaid = new javax.swing.JTextField();
        txtAmountDue = new javax.swing.JTextField();
        jLabel10 = new javax.swing.JLabel();
        cmbPaymentMode = new javax.swing.JComboBox<>();
        jLabel11 = new javax.swing.JLabel();
        jButton5 = new javax.swing.JButton();
        jButton6 = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        getContentPane().setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jPanel1.setBackground(new java.awt.Color(174, 242, 242));
        jPanel1.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        jPanel1.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabel1.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel1.setText("Supplier Phone:");
        jPanel1.add(jLabel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(680, 20, 100, -1));

        jLabel2.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel2.setText("Date:");
        jPanel1.add(jLabel2, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 20, 60, 20));
        jPanel1.add(txtSupplierPhone, new org.netbeans.lib.awtextra.AbsoluteConstraints(780, 10, 120, 30));
        jPanel1.add(txtTotalAmount, new org.netbeans.lib.awtextra.AbsoluteConstraints(120, 310, 110, 30));

        jLabel3.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel3.setText("Supplier Name:");
        jPanel1.add(jLabel3, new org.netbeans.lib.awtextra.AbsoluteConstraints(350, 20, 100, -1));
        jPanel1.add(txtSupplierName, new org.netbeans.lib.awtextra.AbsoluteConstraints(450, 10, 150, 30));

        jScrollPane1.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
        jScrollPane1.setVerticalScrollBarPolicy(javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);

        purchaseTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                " ID", "PURCHASE NO", "DATE", "SUPPLIER NAME", "SUPPLIER PHONE", "TOTAL AMOUNT", "TOTAL GST", "NET AMOUNT", "AMOUNT PAID", "AMOUNT DUE", "PAYMENT MODE", "CREATED AT", "BILL PATH"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false, false, false, false, false, false, false, false, false, false, false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        jScrollPane2.setViewportView(purchaseTable);

        jScrollPane1.setViewportView(jScrollPane2);

        jPanel1.add(jScrollPane1, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 70, 1100, 210));

        jButton1.setText("DELETE");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });
        jPanel1.add(jButton1, new org.netbeans.lib.awtextra.AbsoluteConstraints(470, 440, -1, -1));

        jButton2.setText("VIEW DETAILS");
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });
        jPanel1.add(jButton2, new org.netbeans.lib.awtextra.AbsoluteConstraints(60, 440, -1, -1));

        jButton3.setText("UPLOAD FILE");
        jButton3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton3ActionPerformed(evt);
            }
        });
        jPanel1.add(jButton3, new org.netbeans.lib.awtextra.AbsoluteConstraints(400, 380, -1, -1));

        jButton4.setText("ADD");
        jButton4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton4ActionPerformed(evt);
            }
        });
        jPanel1.add(jButton4, new org.netbeans.lib.awtextra.AbsoluteConstraints(270, 440, -1, -1));

        jLabel4.setText("Summary");
        jPanel1.add(jLabel4, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 290, -1, -1));

        jLabel5.setText("Total GST:");
        jPanel1.add(jLabel5, new org.netbeans.lib.awtextra.AbsoluteConstraints(260, 320, -1, -1));

        jLabel6.setText("Net Amount:");
        jPanel1.add(jLabel6, new org.netbeans.lib.awtextra.AbsoluteConstraints(450, 320, -1, -1));

        jLabel7.setText("Amount Due:");
        jPanel1.add(jLabel7, new org.netbeans.lib.awtextra.AbsoluteConstraints(860, 320, -1, -1));

        jLabel8.setText("UPLOAD PURCHASE BILL:");
        jPanel1.add(jLabel8, new org.netbeans.lib.awtextra.AbsoluteConstraints(260, 380, -1, -1));
        jPanel1.add(txtTotalGST, new org.netbeans.lib.awtextra.AbsoluteConstraints(320, 310, 110, 30));
        jPanel1.add(txtNetAmount, new org.netbeans.lib.awtextra.AbsoluteConstraints(520, 310, 110, 30));
        jPanel1.add(txtPurchaseDate, new org.netbeans.lib.awtextra.AbsoluteConstraints(100, 10, 110, 30));

        jLabel9.setText("Amount Paid:");
        jPanel1.add(jLabel9, new org.netbeans.lib.awtextra.AbsoluteConstraints(650, 320, -1, -1));
        jPanel1.add(txtAmountPaid, new org.netbeans.lib.awtextra.AbsoluteConstraints(730, 310, 110, 30));
        jPanel1.add(txtAmountDue, new org.netbeans.lib.awtextra.AbsoluteConstraints(940, 310, 110, 30));

        jLabel10.setText("Total Amount:");
        jPanel1.add(jLabel10, new org.netbeans.lib.awtextra.AbsoluteConstraints(40, 320, -1, -1));

        cmbPaymentMode.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "--select--", "CASH", "UPI", "CARDS" }));
        jPanel1.add(cmbPaymentMode, new org.netbeans.lib.awtextra.AbsoluteConstraints(120, 380, 110, -1));

        jLabel11.setText("PAYMENT MODE:");
        jPanel1.add(jLabel11, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 380, -1, -1));

        jButton5.setText("GENERATE PDF");
        jButton5.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton5ActionPerformed(evt);
            }
        });
        jPanel1.add(jButton5, new org.netbeans.lib.awtextra.AbsoluteConstraints(880, 440, -1, -1));

        jButton6.setText("CLEAR");
        jButton6.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton6ActionPerformed(evt);
            }
        });
        jPanel1.add(jButton6, new org.netbeans.lib.awtextra.AbsoluteConstraints(680, 440, -1, -1));

        getContentPane().add(jPanel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 1130, 480));

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jButton4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton4ActionPerformed
        // TODO add your handling code here:
        addPurchase();
        clearPurchaseFields();
    }//GEN-LAST:event_jButton4ActionPerformed

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
        // TODO add your handling code here:
         try {
        int selectedRow = purchaseTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a purchase record first!");
            return;
        }

        // bill_path is stored in column 10 (hidden one in loadPurchaseTable)
        String billPath = purchaseTable.getModel().getValueAt(selectedRow, 11).toString();

        if (billPath == null || billPath.trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "No bill uploaded for this purchase!");
            return;
        }

        File billFile = new File(billPath);
        if (!billFile.exists()) {
            JOptionPane.showMessageDialog(this, "Bill file not found!\n" + billPath);
            return;
        }

        // Open with default system viewer
        if (Desktop.isDesktopSupported()) {
            Desktop.getDesktop().open(billFile);
        } else {
            JOptionPane.showMessageDialog(this, "Desktop not supported. Cannot open file.");
        }

    } catch (Exception e) {
        e.printStackTrace();
        JOptionPane.showMessageDialog(this, "Error opening bill: " + e.getMessage());
    }
        
    }//GEN-LAST:event_jButton2ActionPerformed

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        // TODO add your handling code here:
        deletePurchase();
    }//GEN-LAST:event_jButton1ActionPerformed

    private void jButton5ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton5ActionPerformed
        // TODO add your handling code here:
        PurchasePDFExporter.exportTableToPDF(purchaseTable);
    }//GEN-LAST:event_jButton5ActionPerformed

    private void jButton3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton3ActionPerformed
        // TODO add your handling code here:
       try {
        // Select file
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Select Purchase Bill");
        int result = fileChooser.showOpenDialog(this);

        if (result != JFileChooser.APPROVE_OPTION) {
            return; // user cancelled
        }

        File selectedFile = fileChooser.getSelectedFile();

        // Ensure a row is selected in JTable
        int selectedRow = purchaseTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a purchase record first!");
            return;
        }

        // Get purchase number from table (assuming column index 1 is purchase_no)
        String purchaseNo = purchaseTable.getValueAt(selectedRow, 1).toString();

        // Create "bills" folder inside project directory if not exists
        File billsDir = new File("bills");
        if (!billsDir.exists()) {
            billsDir.mkdirs();
        }

        // Keep original extension (.pdf, .jpg, etc.)
        String extension = "";
        int dotIndex = selectedFile.getName().lastIndexOf(".");
        if (dotIndex >= 0) {
            extension = selectedFile.getName().substring(dotIndex);
        }

        // Save file as purchaseNo.extension
        File destFile = new File(billsDir, purchaseNo + extension);

        // Copy file into bills folder
        Files.copy(
            selectedFile.toPath(),
            destFile.toPath(),
            StandardCopyOption.REPLACE_EXISTING
        );

        // Update DB with the stored path
        try (Connection con = ConnectionProvider.getCon();
             PreparedStatement ps = con.prepareStatement("UPDATE purchases SET bill_path = ? WHERE purchase_no = ?")) {

            ps.setString(1, destFile.getAbsolutePath());
            ps.setString(2, purchaseNo);

            int rows = ps.executeUpdate();
            if (rows > 0) {
                JOptionPane.showMessageDialog(this, "Bill uploaded and saved inside project/bills folder!");
                loadPurchaseTable();
            }
        }

    } catch (Exception ex) {
        ex.printStackTrace();
        JOptionPane.showMessageDialog(this, "Error uploading bill: " + ex.getMessage());
    }
    }//GEN-LAST:event_jButton3ActionPerformed

    private void jButton6ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton6ActionPerformed
        // TODO add your handling code here:
        clearPurchaseFields();
    }//GEN-LAST:event_jButton6ActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(Purchase.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(Purchase.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(Purchase.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(Purchase.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new Purchase().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JComboBox<String> cmbPaymentMode;
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton3;
    private javax.swing.JButton jButton4;
    private javax.swing.JButton jButton5;
    private javax.swing.JButton jButton6;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JTable purchaseTable;
    private javax.swing.JTextField txtAmountDue;
    private javax.swing.JTextField txtAmountPaid;
    private javax.swing.JTextField txtNetAmount;
    private javax.swing.JTextField txtPurchaseDate;
    private javax.swing.JTextField txtSupplierName;
    private javax.swing.JTextField txtSupplierPhone;
    private javax.swing.JTextField txtTotalAmount;
    private javax.swing.JTextField txtTotalGST;
    // End of variables declaration//GEN-END:variables
}
