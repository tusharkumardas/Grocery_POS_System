/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package grocery;
import IDgenerator.ExpenseIDGenerator;
import Project.ConnectionProvider;
import java.sql.Connection;
import java.sql.PreparedStatement;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;
import java.sql.ResultSet;
import Project.ExpensePDFExporter;
/**
 *
 * @author Tushar Kumar Das
 */
public class Expense extends javax.swing.JFrame {
private void clearExpenseFields() {
    jdcExpenseDate.setDate(null);
    cmbCategory.setSelectedIndex(0);
    txtDescription.setText("");
    txtAmount.setText("");
    cmbPaymentMode.setSelectedIndex(0);
}

    /**
     * Creates new form Expense
     */
    public Expense() {
        initComponents();
        loadExpenseTable();
        this.setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
    }
    public void searchExpenses() {
    Connection con = null;
    PreparedStatement ps = null;
    ResultSet rs = null;

    try {
        con = ConnectionProvider.getCon();

        String sql = "SELECT id, expense_code, expense_date, category, description, amount, payment_mode, created_at FROM expenses WHERE 1=1";

        if (jRadioButton1.isSelected()) { // Search by date range

    if (jdcFromDate.getDate() == null || jdcToDate.getDate() == null) {
        JOptionPane.showMessageDialog(this, "Please select both FROM and TO dates!");
        return;
    }

      sql += " AND expense_date BETWEEN ? AND ?";
     }

        if (jRadioButton2.isSelected()) { // Search by category
            String category = jComboBox3.getSelectedItem().toString();
            if (!category.equals("--select--")) {
                sql += " AND category = ?";
            } else {
                JOptionPane.showMessageDialog(this, "Please select a category!");
                return;
            }
        }

        ps = con.prepareStatement(sql);

        int paramIndex = 1;
        if (jRadioButton1.isSelected()) {
            if (jdcFromDate.getDate() == null || jdcToDate.getDate() == null) {
        JOptionPane.showMessageDialog(this, "Please select both FROM and TO dates!");
        return;
    }

    java.sql.Date fromDate =
            new java.sql.Date(jdcFromDate.getDate().getTime());
    java.sql.Date toDate =
            new java.sql.Date(jdcToDate.getDate().getTime());

           ps.setDate(paramIndex++, fromDate);
           ps.setDate(paramIndex++, toDate);
        }

        if (jRadioButton2.isSelected()) {
            ps.setString(paramIndex++, jComboBox3.getSelectedItem().toString());
        }

        rs = ps.executeQuery();

        DefaultTableModel model = new DefaultTableModel(
            new Object[]{"ID", "Expense Code", "Date", "Category", "Description", "Amount", "Payment Mode", "Created At"}, 0
        );

        while (rs.next()) {
            model.addRow(new Object[]{
                rs.getInt("id"),
                rs.getString("expense_code"),
                rs.getDate("expense_date"),
                rs.getString("category"),
                rs.getString("description"),
                rs.getDouble("amount"),
                rs.getString("payment_mode"),
                rs.getTimestamp("created_at")
            });
        }

        expenseTable.setModel(model);
        expenseTable.getColumnModel().getColumn(0).setMinWidth(0);
        expenseTable.getColumnModel().getColumn(0).setMaxWidth(0);
        expenseTable.getColumnModel().getColumn(0).setWidth(0);

    } catch (Exception e) {
        e.printStackTrace();
        JOptionPane.showMessageDialog(this, "Error searching expenses: " + e.getMessage());
    } finally {
        try { if (rs != null) rs.close(); if (ps != null) ps.close(); if (con != null) con.close(); } catch (Exception ex) {}
    }
}

    public void loadExpenseTable() {
    Connection con = null;
    PreparedStatement ps = null;
    ResultSet rs = null;

    try {
        con = ConnectionProvider.getCon();
        String sql = "SELECT id, expense_code, expense_date, category, description, amount, payment_mode, created_at FROM expenses";
        ps = con.prepareStatement(sql);
        rs = ps.executeQuery();

        // Table Model matching purchase table columns
        DefaultTableModel model = new DefaultTableModel(
            new Object[]{
                "ID", "Expense Code", "Date", "Category", "Description",
                "Amount", "Payment Mode", "Created At"
            }, 0
        );

        // Fill table rows
        while (rs.next()) {
            model.addRow(new Object[]{
                rs.getInt("id"),
                rs.getString("expense_code"),
                rs.getDate("expense_date"),
                rs.getString("category"),
                rs.getString("description"),
                rs.getDouble("amount"),
                rs.getString("payment_mode"),
                rs.getTimestamp("created_at")
            });
        }

        // Set into JTable
        expenseTable.setModel(model);

        // Hide ID column (not needed for user)
        expenseTable.getColumnModel().getColumn(0).setMinWidth(0);
        expenseTable.getColumnModel().getColumn(0).setMaxWidth(0);
        expenseTable.getColumnModel().getColumn(0).setWidth(0);

    } catch (Exception e) {
        e.printStackTrace();
        JOptionPane.showMessageDialog(this, "Error loading expenses: " + e.getMessage());
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


  public void addExpense(
        java.sql.Date expenseDate,
        String category,
        String description,
        double amount,
        String paymentMode
) {
    if (category.equals("--select--") || jdcExpenseDate.getDate()==null || txtAmount.getText().trim().isEmpty()) {
        JOptionPane.showMessageDialog(this, "Please fill all required fields!");
        return;
    }

    try (Connection con = ConnectionProvider.getCon();
         PreparedStatement ps = con.prepareStatement(
             "INSERT INTO expenses (expense_code, expense_date, category, description, amount, payment_mode) VALUES (?, ?, ?, ?, ?, ?)")) {

        String expenseCode = ExpenseIDGenerator.generateExpenseID();
        ps.setString(1, expenseCode);
        ps.setDate(2, expenseDate);
        ps.setString(3, category);
        ps.setString(4, description);
        ps.setDouble(5, amount);
        ps.setString(6, paymentMode);

        int rows = ps.executeUpdate();
        if (rows > 0) {
            JOptionPane.showMessageDialog(this, "Expense saved successfully!\nExpense Code: " + expenseCode);
            loadExpenseTable();   // Refresh table
            clearExpenseFields(); // Clear input fields
        }

    } catch (Exception e) {
        e.printStackTrace();
        JOptionPane.showMessageDialog(this, "Error saving expense: " + e.getMessage());
    }
}



    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        buttonGroup1 = new javax.swing.ButtonGroup();
        jPanel1 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        txtAmount = new javax.swing.JTextField();
        cmbPaymentMode = new javax.swing.JComboBox<>();
        cmbCategory = new javax.swing.JComboBox<>();
        jButton2 = new javax.swing.JButton();
        jButton3 = new javax.swing.JButton();
        jButton4 = new javax.swing.JButton();
        jButton5 = new javax.swing.JButton();
        jButton6 = new javax.swing.JButton();
        jButton7 = new javax.swing.JButton();
        jComboBox3 = new javax.swing.JComboBox<>();
        jLabel9 = new javax.swing.JLabel();
        jScrollPane3 = new javax.swing.JScrollPane();
        jScrollPane2 = new javax.swing.JScrollPane();
        txtDescription = new javax.swing.JTextArea();
        jScrollPane1 = new javax.swing.JScrollPane();
        jScrollPane4 = new javax.swing.JScrollPane();
        expenseTable = new javax.swing.JTable();
        jRadioButton1 = new javax.swing.JRadioButton();
        jRadioButton2 = new javax.swing.JRadioButton();
        jButton9 = new javax.swing.JButton();
        jTextField6 = new javax.swing.JTextField();
        jLabel7 = new javax.swing.JLabel();
        jdcFromDate = new com.toedter.calendar.JDateChooser();
        jdcToDate = new com.toedter.calendar.JDateChooser();
        jdcExpenseDate = new com.toedter.calendar.JDateChooser();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        getContentPane().setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jPanel1.setBackground(new java.awt.Color(174, 242, 242));
        jPanel1.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabel1.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        jLabel1.setText("EXPENSE  RECORDS");
        jPanel1.add(jLabel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(400, 10, -1, -1));

        jLabel2.setText("Payment Mode:");
        jPanel1.add(jLabel2, new org.netbeans.lib.awtextra.AbsoluteConstraints(290, 80, -1, -1));

        jLabel3.setText("Date:");
        jPanel1.add(jLabel3, new org.netbeans.lib.awtextra.AbsoluteConstraints(40, 50, -1, -1));

        jLabel4.setText("Amount:");
        jPanel1.add(jLabel4, new org.netbeans.lib.awtextra.AbsoluteConstraints(40, 80, -1, -1));

        jLabel5.setText("Description:");
        jPanel1.add(jLabel5, new org.netbeans.lib.awtextra.AbsoluteConstraints(600, 50, -1, -1));

        jLabel6.setText("Category:");
        jPanel1.add(jLabel6, new org.netbeans.lib.awtextra.AbsoluteConstraints(290, 50, -1, -1));
        jPanel1.add(txtAmount, new org.netbeans.lib.awtextra.AbsoluteConstraints(100, 80, 150, -1));

        cmbPaymentMode.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "UPI", "CASH", "CARD" }));
        jPanel1.add(cmbPaymentMode, new org.netbeans.lib.awtextra.AbsoluteConstraints(380, 80, 210, -1));

        cmbCategory.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "--select--", "Shop Rent / Lease", "Electricity Bill", "Water Bill", "Internet / Phone", "Salaries & Wages", "Transportation / Delivery Charges", "Loading & Unloading Charges", "Packaging Materials ", "Repair & Maintenance", "Pest Control", "Security (CCTV, guards)", "Insurance", "Bank Charges", "Taxes (GST, Municipal, etc.)", "Licenses & Renewals", "Others" }));
        jPanel1.add(cmbCategory, new org.netbeans.lib.awtextra.AbsoluteConstraints(380, 50, -1, -1));

        jButton2.setBackground(new java.awt.Color(255, 204, 204));
        jButton2.setText("Delete");
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });
        jPanel1.add(jButton2, new org.netbeans.lib.awtextra.AbsoluteConstraints(230, 130, -1, -1));

        jButton3.setBackground(new java.awt.Color(204, 204, 255));
        jButton3.setText("Clear");
        jButton3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton3ActionPerformed(evt);
            }
        });
        jPanel1.add(jButton3, new org.netbeans.lib.awtextra.AbsoluteConstraints(420, 130, -1, -1));

        jButton4.setBackground(new java.awt.Color(255, 255, 153));
        jButton4.setText("Search");
        jButton4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton4ActionPerformed(evt);
            }
        });
        jPanel1.add(jButton4, new org.netbeans.lib.awtextra.AbsoluteConstraints(832, 370, 80, -1));

        jButton5.setBackground(new java.awt.Color(204, 255, 204));
        jButton5.setText("Export PDF");
        jButton5.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton5ActionPerformed(evt);
            }
        });
        jPanel1.add(jButton5, new org.netbeans.lib.awtextra.AbsoluteConstraints(220, 410, -1, -1));

        jButton6.setBackground(new java.awt.Color(255, 51, 51));
        jButton6.setText("Close");
        jButton6.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton6ActionPerformed(evt);
            }
        });
        jPanel1.add(jButton6, new org.netbeans.lib.awtextra.AbsoluteConstraints(610, 410, -1, -1));

        jButton7.setBackground(new java.awt.Color(204, 255, 204));
        jButton7.setText("Add Expense");
        jButton7.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton7ActionPerformed(evt);
            }
        });
        jPanel1.add(jButton7, new org.netbeans.lib.awtextra.AbsoluteConstraints(40, 130, -1, -1));

        jComboBox3.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "--select--", "Shop Rent / Lease", "Electricity Bill", "Water Bill", "Internet / Phone", "Salaries & Wages", "Transportation / Delivery Charges", "Loading & Unloading Charges", "Packaging Materials ", "Repair & Maintenance", "Pest Control", "Security (CCTV, guards)", "Insurance", "Bank Charges", "Taxes (GST, Municipal, etc.)", "Licenses & Renewals", "Others" }));
        jPanel1.add(jComboBox3, new org.netbeans.lib.awtextra.AbsoluteConstraints(600, 370, -1, -1));

        jLabel9.setText("TO");
        jPanel1.add(jLabel9, new org.netbeans.lib.awtextra.AbsoluteConstraints(270, 370, -1, -1));

        jScrollPane3.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        txtDescription.setColumns(20);
        txtDescription.setRows(5);
        jScrollPane2.setViewportView(txtDescription);

        jScrollPane3.setViewportView(jScrollPane2);

        jPanel1.add(jScrollPane3, new org.netbeans.lib.awtextra.AbsoluteConstraints(670, 50, 250, 100));

        expenseTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "ID", "EXPENSE CODE", "EXPENSE DATE", "CATEGORY", "DESCRIPTION", "AMOUNT", "PAYMENT MODE", "CREATED AT"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false, false, false, false, false, false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        jScrollPane4.setViewportView(expenseTable);

        jScrollPane1.setViewportView(jScrollPane4);

        jPanel1.add(jScrollPane1, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 160, 910, 200));

        buttonGroup1.add(jRadioButton1);
        jRadioButton1.setSelected(true);
        jRadioButton1.setText("Search by Date:");
        jPanel1.add(jRadioButton1, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 370, -1, -1));

        buttonGroup1.add(jRadioButton2);
        jRadioButton2.setText("Search by Category:");
        jPanel1.add(jRadioButton2, new org.netbeans.lib.awtextra.AbsoluteConstraints(470, 370, -1, -1));

        jButton9.setBackground(new java.awt.Color(255, 153, 153));
        jButton9.setText("Reset");
        jButton9.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton9ActionPerformed(evt);
            }
        });
        jPanel1.add(jButton9, new org.netbeans.lib.awtextra.AbsoluteConstraints(430, 410, -1, -1));
        jPanel1.add(jTextField6, new org.netbeans.lib.awtextra.AbsoluteConstraints(940, 310, 110, 30));

        jLabel7.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel7.setText("OR");
        jPanel1.add(jLabel7, new org.netbeans.lib.awtextra.AbsoluteConstraints(440, 370, -1, -1));

        jdcFromDate.setDateFormatString("yyyy-MM-dd");
        jPanel1.add(jdcFromDate, new org.netbeans.lib.awtextra.AbsoluteConstraints(120, 370, 150, -1));

        jdcToDate.setDateFormatString("yyyy-MM-dd");
        jPanel1.add(jdcToDate, new org.netbeans.lib.awtextra.AbsoluteConstraints(290, 370, 140, -1));

        jdcExpenseDate.setDateFormatString("yyyy-MM-dd");
        jPanel1.add(jdcExpenseDate, new org.netbeans.lib.awtextra.AbsoluteConstraints(100, 40, 150, -1));

        getContentPane().add(jPanel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 930, 450));

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jButton7ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton7ActionPerformed
        // TODO add your handling code here:
         try {
        // Get date from DateChooser
        if (jdcExpenseDate.getDate() == null) {
              JOptionPane.showMessageDialog(this, "Please select expense date!");
          return;
        }

        java.sql.Date expenseDate =
        new java.sql.Date(jdcExpenseDate.getDate().getTime());

        

        String category = cmbCategory.getSelectedItem().toString();
        String description = txtDescription.getText().trim();
        if (txtAmount.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter amount.");
            return;
        }
        double amount = Double.parseDouble(txtAmount.getText().trim());
        String paymentMode = cmbPaymentMode.getSelectedItem().toString();

        addExpense(expenseDate, category, description, amount, paymentMode);

    } catch (NumberFormatException e) {
        JOptionPane.showMessageDialog(this, "Amount must be a valid number!");
    } catch (Exception e) {
        e.printStackTrace();
        JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
    }
    }//GEN-LAST:event_jButton7ActionPerformed

    private void jButton4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton4ActionPerformed
        // TODO add your handling code here:
        searchExpenses();
        jdcFromDate.setDate(null);
        jdcToDate.setDate(null);
        jComboBox3.setSelectedIndex(0);
    }//GEN-LAST:event_jButton4ActionPerformed

    private void jButton9ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton9ActionPerformed
        // TODO add your handling code here:
        loadExpenseTable();
        clearExpenseFields();
        jdcFromDate.setDate(null);
        jdcToDate.setDate(null);
        jComboBox3.setSelectedIndex(0);
    }//GEN-LAST:event_jButton9ActionPerformed

    private void jButton6ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton6ActionPerformed
        // TODO add your handling code here:
        this.dispose();
    }//GEN-LAST:event_jButton6ActionPerformed

    private void jButton3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton3ActionPerformed
        // TODO add your handling code here:
        clearExpenseFields();
    }//GEN-LAST:event_jButton3ActionPerformed

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
        // TODO add your handling code here:
            Connection con = null;
    PreparedStatement ps = null;

    try {
        int selectedRow = expenseTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select an expense to delete.");
            return;
        }

        // Get expense ID (hidden column or hidden text field)
        int id = Integer.parseInt(expenseTable.getValueAt(selectedRow, 0).toString());

        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Are you sure you want to delete this expense?",
                "Confirm Delete",
                JOptionPane.YES_NO_OPTION
        );

        if (confirm == JOptionPane.YES_OPTION) {
            con = ConnectionProvider.getCon();
            String sql = "DELETE FROM expenses WHERE id=?";
            ps = con.prepareStatement(sql);
            ps.setInt(1, id);

            int rows = ps.executeUpdate();
            if (rows > 0) {
                JOptionPane.showMessageDialog(this, "Expense deleted successfully!");
                loadExpenseTable();   // Refresh data
                clearExpenseFields(); // Clear fields
            } else {
                JOptionPane.showMessageDialog(this, "Failed to delete expense.");
            }
        }
    } catch (Exception e) {
        e.printStackTrace();
        JOptionPane.showMessageDialog(this, "Error deleting expense: " + e.getMessage());
    } finally {
        try { if (ps != null) ps.close(); if (con != null) con.close(); } catch (Exception ex) {}
    }
    }//GEN-LAST:event_jButton2ActionPerformed

    private void jButton5ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton5ActionPerformed
        // TODO add your handling code here:
        ExpensePDFExporter.exportToPDF(expenseTable);
    }//GEN-LAST:event_jButton5ActionPerformed

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
            java.util.logging.Logger.getLogger(Expense.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(Expense.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(Expense.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(Expense.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new Expense().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.JComboBox<String> cmbCategory;
    private javax.swing.JComboBox<String> cmbPaymentMode;
    private javax.swing.JTable expenseTable;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton3;
    private javax.swing.JButton jButton4;
    private javax.swing.JButton jButton5;
    private javax.swing.JButton jButton6;
    private javax.swing.JButton jButton7;
    private javax.swing.JButton jButton9;
    private javax.swing.JComboBox<String> jComboBox3;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JRadioButton jRadioButton1;
    private javax.swing.JRadioButton jRadioButton2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JTextField jTextField6;
    private com.toedter.calendar.JDateChooser jdcExpenseDate;
    private com.toedter.calendar.JDateChooser jdcFromDate;
    private com.toedter.calendar.JDateChooser jdcToDate;
    private javax.swing.JTextField txtAmount;
    private javax.swing.JTextArea txtDescription;
    // End of variables declaration//GEN-END:variables
}
