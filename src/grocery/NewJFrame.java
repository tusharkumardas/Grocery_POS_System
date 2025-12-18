/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package grocery;
import grocery.ProductEntry;
import javax.swing.JOptionPane;
import javax.swing.event.DocumentListener;
import javax.swing.event.DocumentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.JScrollPane;
import Project.ConnectionProvider;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import javax.swing.table.DefaultTableModel;
import javax.swing.JTable;
import java.sql.ResultSet;
import Project.BillGenerator;
import Project.BillingCalculator;
import Project.saveBill;


/**
 *
 * @author Tushar Kumar Das
 */
public class NewJFrame extends javax.swing.JFrame {
    DefaultListModel<String> listModel = new DefaultListModel<>();
    JList<String> suggestionList = new JList<>(listModel);
    JScrollPane scrollPane = new JScrollPane(suggestionList);
    private int lastAddedRow = -1;
    
    private void showSuggestions() {
    String text = jTextFieldSearch.getText().trim();
    listModel.clear();

    if (text.isEmpty()) {
        scrollPane.setVisible(false);
        return;
    }

    try (Connection con = ConnectionProvider.getCon()) {
        String sql = "SELECT item_code, product_name, qty, sale_price FROM products WHERE product_name LIKE ? OR item_code LIKE ?";
        PreparedStatement pst = con.prepareStatement(sql);
        pst.setString(1, "%" + text + "%");
        pst.setString(2, "%" + text + "%");

        ResultSet rs = pst.executeQuery();
        while (rs.next()) {
            String itemCode = rs.getString("item_code");
            String name = rs.getString("product_name");
            int qty = rs.getInt("qty");
            double price = rs.getDouble("sale_price");

            listModel.addElement(itemCode + " - " + name + " | Qty: " + qty + " | ₹" + price);
        }

        scrollPane.setVisible(!listModel.isEmpty());
    } catch (Exception e) {
        e.printStackTrace();
    }
}

    private void autoAddByBarcode(String barcode) {
    if (barcode == null || barcode.isEmpty()) return;

    try (Connection con = ConnectionProvider.getCon()) {

        String sql = "SELECT id, item_code, product_name, sale_price, gst, mrp " +
                     "FROM products WHERE barcode = ?";
        PreparedStatement pst = con.prepareStatement(sql);
        pst.setString(1, barcode);

        ResultSet rs = pst.executeQuery();

        if (rs.next()) {
            int productId = rs.getInt("id");
            String code = rs.getString("item_code");
            String name = rs.getString("product_name");
            double sale = rs.getDouble("sale_price");
            double gst = rs.getDouble("gst");
            double mrp = rs.getDouble("mrp");

            DefaultTableModel model = (DefaultTableModel) jTable1.getModel();
            if (model == null) {
                System.err.println("Table model is null. Make sure jTable1 is initialized properly.");
                return;
            }

            boolean found = false;

            // 🔹 EXISTING PRODUCT CASE
            for (int i = 0; i < model.getRowCount(); i++) {
                String existingCode = model.getValueAt(i, 1).toString();

                if (existingCode.equals(code)) {
                    int existingQty = Integer.parseInt(
                        model.getValueAt(i, 3).toString()
                    );
                    existingQty++;

                    model.setValueAt(existingQty, i, 3);        // Qty
                    model.setValueAt(existingQty * sale, i, 7); // Total

                    // ✅ NEW: auto-select existing row
                    lastAddedRow = i;
                    jTable1.setRowSelectionInterval(i, i);
                    jTable1.scrollRectToVisible(
                        jTable1.getCellRect(i, 0, true)
                    );

                    found = true;
                    break;
                }
            }

            // 🔹 NEW PRODUCT CASE
            if (!found) {
                int qty = 1;
                double total = sale;

                model.addRow(new Object[]{
                    productId, code, name, qty, sale, gst, mrp, total
                });

                // ✅ NEW: auto-select newly added row
                lastAddedRow = model.getRowCount() - 1;
                jTable1.setRowSelectionInterval(lastAddedRow, lastAddedRow);
                jTable1.scrollRectToVisible(
                    jTable1.getCellRect(lastAddedRow, 0, true)
                );
            }

        } else {
            System.out.println("No product found for barcode: " + barcode);
        }

        // Clear search for next scan
        jTextFieldSearch.setText("");
        scrollPane.setVisible(false);

    } catch (Exception e) {
        e.printStackTrace();
    }
}
    private void setupTableEnterKey() {
    jTable1.addKeyListener(new java.awt.event.KeyAdapter() {
        @Override
        public void keyPressed(java.awt.event.KeyEvent e) {
            if (e.getKeyCode() == java.awt.event.KeyEvent.VK_ENTER) {
                e.consume();

                int row = jTable1.getSelectedRow();
                if (row == -1) return;

                String productName = jTable1.getValueAt(row, 2).toString();
                double price = Double.parseDouble(
                    jTable1.getValueAt(row, 4).toString()
                );

                String input = JOptionPane.showInputDialog(
                    NewJFrame.this,
                    "Enter final quantity for:\n" + productName,
                    "Update Quantity",
                    JOptionPane.QUESTION_MESSAGE
                );

                if (input == null) return;

                try {
                    int qty = Integer.parseInt(input.trim());
                    if (qty <= 0) return;

                    jTable1.setValueAt(qty, row, 3);
                    jTable1.setValueAt(qty * price, row, 7);
                    jTextFieldSearch.setText("");
                    jTextFieldSearch.requestFocusInWindow();

                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(
                        NewJFrame.this,
                        "Invalid quantity",
                        "Error",
                        JOptionPane.ERROR_MESSAGE
                    );
                }
            }
        }
    });
}

    
    private void fillProductTable(String selectedValue) {
    if (selectedValue == null || selectedValue.isEmpty()) return;

    String itemCode = selectedValue.split(" - ")[0].trim();

    try (Connection con = ConnectionProvider.getCon()) {
        String sql = "SELECT id, item_code, product_name, sale_price, gst, mrp FROM products WHERE item_code = ?";
        PreparedStatement pst = con.prepareStatement(sql);
        pst.setString(1, itemCode);

        ResultSet rs = pst.executeQuery();
        if (rs.next()) {
            int productId = rs.getInt("id");
            String code = rs.getString("item_code");
            String name = rs.getString("product_name");
            double sale = rs.getDouble("sale_price");
            double gst = rs.getDouble("gst");
            double mrp = rs.getDouble("mrp");

            DefaultTableModel model = (DefaultTableModel) jTable1.getModel();
            if (model == null) {
                System.err.println("Table model is null. Make sure jTable1 is initialized properly.");
                return;
            }

            boolean found = false;

            for (int i = 0; i < model.getRowCount(); i++) {
                String existingCode = model.getValueAt(i, 1).toString();
                if (existingCode.equals(code)) {
                    int existingQty = Integer.parseInt(model.getValueAt(i, 3).toString());
                    existingQty++;
                    model.setValueAt(existingQty, i, 3); // Qty
                    model.setValueAt(existingQty * sale, i, 7); // Total = qty * sale
                    lastAddedRow = i;
                    jTable1.setRowSelectionInterval(i, i);
                    jTable1.scrollRectToVisible(
                    jTable1.getCellRect(i, 0, true)
                     );

                    found = true;
                    break;
                }
            }

            if (!found) {
                int qty = 1;
                double total = sale;
                model.addRow(new Object[]{productId,code, name, qty, sale, gst, mrp, total});
                lastAddedRow = model.getRowCount() - 1;
                jTable1.setRowSelectionInterval(lastAddedRow, lastAddedRow);
                jTable1.scrollRectToVisible(
                jTable1.getCellRect(lastAddedRow, 0, true)
    );
            }
        } else {
            System.out.println("No product found for item code: " + itemCode);
        }
        jTextFieldSearch.setText("");
    } catch (Exception e) {
        e.printStackTrace();
    }
}


    


    private void setupSearchBar() {
    // Setup scrollPane location
    scrollPane.setVisible(false);
    jPanel2.add(scrollPane, new org.netbeans.lib.awtextra.AbsoluteConstraints(
        jTextFieldSearch.getX(),
        jTextFieldSearch.getY() + jTextFieldSearch.getHeight(),
        jTextFieldSearch.getWidth(),
        100
    ));
    jPanel2.setComponentZOrder(scrollPane, 0);

    // Document listener for real-time suggestions
    jTextFieldSearch.getDocument().addDocumentListener(new DocumentListener() {
        public void insertUpdate(DocumentEvent e) {
            showSuggestions();
        }
        public void removeUpdate(DocumentEvent e) {
            showSuggestions();
        }
        public void changedUpdate(DocumentEvent e) {
            showSuggestions();
        }
    });

    // Mouse listener for selection from suggestions
    suggestionList.addMouseListener(new MouseAdapter() {
        public void mouseClicked(MouseEvent e) {
            if (e.getClickCount() == 1) {
                String selected = suggestionList.getSelectedValue();
                if (selected != null) {
                    fillProductTable(selected);
                    scrollPane.setVisible(false);
                    jTextFieldSearch.setText("");
                    jTextFieldSearch.requestFocusInWindow();
                }
            }
        }
    });
}
    private void addBarcodeListener() {
    jTextFieldSearch.addActionListener(e -> {
        String text = jTextFieldSearch.getText().trim();
        if (text.matches("\\d{8,}")) {
            autoAddByBarcode(text);
        }
    });
}
    private void loadLastInvoiceNo() {
    try (Connection con = ConnectionProvider.getCon()) {

        String sql = "SELECT invoice_no FROM sales ORDER BY id DESC LIMIT 1";
        PreparedStatement pst = con.prepareStatement(sql);
        ResultSet rs = pst.executeQuery();

        if (rs.next()) {
            invoiceTextField.setText(rs.getString("invoice_no"));
        } else {
            invoiceTextField.setText("N/A");
        }

    } catch (Exception e) {
        invoiceTextField.setText("N/A");
        e.printStackTrace();
    }
}

    private void updateBillingSummary() {

    DefaultTableModel model = (DefaultTableModel) jTable1.getModel();

    double subTotal = 0;
    double totalTax = 0;

    for (int i = 0; i < model.getRowCount(); i++) {

        int qty = Integer.parseInt(model.getValueAt(i, 3).toString());
        double price = Double.parseDouble(model.getValueAt(i, 4).toString());
        double gst = Double.parseDouble(model.getValueAt(i, 5).toString());

        double rowTotal = qty * price;
        double rowTax = rowTotal * gst / 100;

        subTotal += rowTotal;
        totalTax += rowTax;
    }

    double discount = 0;
    try {
        discount = Double.parseDouble(txtDiscount.getText().trim());
    } catch (Exception ignored) {}

    double finalTotal = subTotal - discount + totalTax;

    txtSubTotal.setText(String.format("%.2f", subTotal));
    txtTax.setText(String.format("%.2f", totalTax));
    txtFinalTotal.setText(String.format("%.2f", finalTotal));

    updateAmountDue();
}

    private void updateAmountDue() {
    try {
        double finalTotal = Double.parseDouble(txtFinalTotal.getText());
        double paid = Double.parseDouble(txtAmountPaid.getText());
        txtAmountDue.setText(String.format("%.2f", finalTotal - paid));
        } catch (Exception e) {
        txtAmountDue.setText("0.00");
    }
}


    /**
     * Creates new form NewJFrame
     */
    public NewJFrame() {
        initComponents();
        
        // Hide Product ID column
        jTable1.getColumnModel().getColumn(0).setMinWidth(0);
        jTable1.getColumnModel().getColumn(0).setMaxWidth(0);
        jTable1.getColumnModel().getColumn(0).setPreferredWidth(0);
        jTable1.getTableHeader().setReorderingAllowed(false);
        jTable1.getTableHeader().setResizingAllowed(false);
        setupSearchBar();
        BillingCalculator.jTable1 = jTable1;
        addBarcodeListener();
        setupTableEnterKey();
        loadLastInvoiceNo();
        DefaultTableModel model = (DefaultTableModel) jTable1.getModel();
        model.addTableModelListener(e -> updateBillingSummary());
        txtAmountPaid.getDocument().addDocumentListener(new DocumentListener() {
        public void insertUpdate(DocumentEvent e) { updateAmountDue(); }
        public void removeUpdate(DocumentEvent e) { updateAmountDue(); }
        public void changedUpdate(DocumentEvent e) { updateAmountDue(); }
    });
        txtDiscount.getDocument().addDocumentListener(new DocumentListener() {
        public void insertUpdate(DocumentEvent e) { updateBillingSummary(); }
        public void removeUpdate(DocumentEvent e) { updateBillingSummary(); }
        public void changedUpdate(DocumentEvent e) { updateBillingSummary(); }
});


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
        jComboBox1 = new javax.swing.JComboBox<>();
        jPanel2 = new javax.swing.JPanel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jTextFieldCustomerPhone = new javax.swing.JTextField();
        jTextFieldCustomerName = new javax.swing.JTextField();
        jButton1 = new javax.swing.JButton();
        jScrollPane2 = new javax.swing.JScrollPane();
        jTable1 = new javax.swing.JTable();
        jTextFieldSearch = new javax.swing.JTextField();
        jLabel21 = new javax.swing.JLabel();
        jPanel3 = new javax.swing.JPanel();
        jLabel5 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        txtFinalTotal = new javax.swing.JTextField();
        txtTax = new javax.swing.JTextField();
        txtDiscount = new javax.swing.JTextField();
        invoiceTextField = new javax.swing.JTextField();
        jPanel4 = new javax.swing.JPanel();
        jLabel9 = new javax.swing.JLabel();
        jLabel10 = new javax.swing.JLabel();
        jLabel11 = new javax.swing.JLabel();
        jLabel12 = new javax.swing.JLabel();
        jTextField7 = new javax.swing.JTextField();
        jTextField8 = new javax.swing.JTextField();
        jTextField9 = new javax.swing.JTextField();
        jTextField10 = new javax.swing.JTextField();
        jPanel5 = new javax.swing.JPanel();
        jLabel13 = new javax.swing.JLabel();
        jLabel14 = new javax.swing.JLabel();
        jLabel15 = new javax.swing.JLabel();
        jLabel16 = new javax.swing.JLabel();
        jTextField11 = new javax.swing.JTextField();
        jTextField12 = new javax.swing.JTextField();
        jTextField13 = new javax.swing.JTextField();
        jTextField14 = new javax.swing.JTextField();
        jPanel6 = new javax.swing.JPanel();
        jLabel17 = new javax.swing.JLabel();
        jLabel18 = new javax.swing.JLabel();
        jLabel19 = new javax.swing.JLabel();
        jLabel20 = new javax.swing.JLabel();
        jTextField15 = new javax.swing.JTextField();
        jTextField16 = new javax.swing.JTextField();
        jTextField17 = new javax.swing.JTextField();
        jTextField18 = new javax.swing.JTextField();
        jLabel22 = new javax.swing.JLabel();
        cmbPaymentMode = new javax.swing.JComboBox<>();
        jLabel23 = new javax.swing.JLabel();
        jLabel24 = new javax.swing.JLabel();
        txtAmountDue = new javax.swing.JTextField();
        txtSubTotal = new javax.swing.JTextField();
        txtAmountPaid = new javax.swing.JTextField();
        jLabel25 = new javax.swing.JLabel();
        jLabel26 = new javax.swing.JLabel();
        jPanel7 = new javax.swing.JPanel();
        jButton7 = new javax.swing.JButton();
        jButton8 = new javax.swing.JButton();
        jButton9 = new javax.swing.JButton();
        jButton10 = new javax.swing.JButton();
        jButton11 = new javax.swing.JButton();
        jButton12 = new javax.swing.JButton();
        jButton13 = new javax.swing.JButton();
        jButton14 = new javax.swing.JButton();
        jButton16 = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();
        jButton3 = new javax.swing.JButton();
        jButton4 = new javax.swing.JButton();
        jButton5 = new javax.swing.JButton();
        jButton6 = new javax.swing.JButton();
        jMenuBar1 = new javax.swing.JMenuBar();
        jMenu1 = new javax.swing.JMenu();
        jMenu2 = new javax.swing.JMenu();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        getContentPane().setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jPanel1.setBackground(new java.awt.Color(224, 255, 255));
        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createEtchedBorder(javax.swing.border.EtchedBorder.RAISED)));
        jPanel1.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabel1.setFont(new java.awt.Font("Algerian", 1, 48)); // NOI18N
        jLabel1.setText("TUSHAR VARIETY STORE");
        jPanel1.add(jLabel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 0, -1, -1));

        jLabel2.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel2.setText("13/07/2025 10:10 AM");
        jPanel1.add(jLabel2, new org.netbeans.lib.awtextra.AbsoluteConstraints(1170, 20, -1, -1));

        jComboBox1.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Profile", "Logout" }));
        jComboBox1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jComboBox1ActionPerformed(evt);
            }
        });
        jPanel1.add(jComboBox1, new org.netbeans.lib.awtextra.AbsoluteConstraints(1420, 20, -1, -1));

        getContentPane().add(jPanel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 1520, 60));

        jPanel2.setBackground(new java.awt.Color(174, 242, 242));
        jPanel2.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabel3.setText("MOBILE NO:");
        jPanel2.add(jLabel3, new org.netbeans.lib.awtextra.AbsoluteConstraints(350, 60, -1, -1));

        jLabel4.setText("CUSTOMER  NAME:");
        jPanel2.add(jLabel4, new org.netbeans.lib.awtextra.AbsoluteConstraints(70, 60, -1, -1));
        jPanel2.add(jTextFieldCustomerPhone, new org.netbeans.lib.awtextra.AbsoluteConstraints(440, 60, 140, -1));
        jPanel2.add(jTextFieldCustomerName, new org.netbeans.lib.awtextra.AbsoluteConstraints(180, 60, 140, -1));

        jButton1.setText("ADD CUSTOMER");
        jPanel2.add(jButton1, new org.netbeans.lib.awtextra.AbsoluteConstraints(650, 60, -1, -1));

        jTable1.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "PRODUCT ID", " ITEM CODE", "PRODUCT NAME", "QTY", "SALE PRICE", "GST", "MRP", "TOTAL"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false, false, true, false, false, false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        jTable1.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        jScrollPane2.setViewportView(jTable1);

        jPanel2.add(jScrollPane2, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 100, 900, 320));

        jTextFieldSearch.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTextFieldSearchActionPerformed(evt);
            }
        });
        jPanel2.add(jTextFieldSearch, new org.netbeans.lib.awtextra.AbsoluteConstraints(190, 10, 730, 30));

        jLabel21.setFont(new java.awt.Font("Segoe UI Black", 1, 18)); // NOI18N
        jLabel21.setText("SEARCH PRODUCT:");
        jPanel2.add(jLabel21, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 10, 200, -1));

        getContentPane().add(jPanel2, new org.netbeans.lib.awtextra.AbsoluteConstraints(230, 70, 950, 440));

        jPanel3.setBackground(new java.awt.Color(192, 245, 255));
        jPanel3.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        jPanel3.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabel5.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel5.setText("Discount:");
        jPanel3.add(jLabel5, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 130, -1, -1));

        jLabel6.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel6.setText("Tax:");
        jPanel3.add(jLabel6, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 180, -1, -1));

        jLabel7.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel7.setText("PAYMENT MODE:");
        jPanel3.add(jLabel7, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 280, 130, 20));

        jLabel8.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel8.setText("SUMMARY");
        jPanel3.add(jLabel8, new org.netbeans.lib.awtextra.AbsoluteConstraints(120, 10, -1, 20));
        jPanel3.add(txtFinalTotal, new org.netbeans.lib.awtextra.AbsoluteConstraints(150, 230, 130, -1));

        txtTax.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtTaxActionPerformed(evt);
            }
        });
        jPanel3.add(txtTax, new org.netbeans.lib.awtextra.AbsoluteConstraints(150, 180, 130, -1));
        jPanel3.add(txtDiscount, new org.netbeans.lib.awtextra.AbsoluteConstraints(150, 130, 130, -1));

        invoiceTextField.setEditable(false);
        invoiceTextField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                invoiceTextFieldActionPerformed(evt);
            }
        });
        jPanel3.add(invoiceTextField, new org.netbeans.lib.awtextra.AbsoluteConstraints(150, 40, 130, -1));

        jPanel4.setBackground(new java.awt.Color(192, 245, 255));
        jPanel4.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        jPanel4.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabel9.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel9.setText("Discount:");
        jPanel4.add(jLabel9, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 130, -1, -1));

        jLabel10.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel10.setText("Tax:");
        jPanel4.add(jLabel10, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 180, -1, -1));

        jLabel11.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel11.setText("Final-Total:");
        jPanel4.add(jLabel11, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 230, -1, 20));

        jLabel12.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel12.setText("Sub-Total:");
        jPanel4.add(jLabel12, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 80, -1, 20));

        jTextField7.setText("jTextField3");
        jPanel4.add(jTextField7, new org.netbeans.lib.awtextra.AbsoluteConstraints(150, 230, 130, -1));

        jTextField8.setText("jTextField3");
        jPanel4.add(jTextField8, new org.netbeans.lib.awtextra.AbsoluteConstraints(150, 180, 130, -1));

        jTextField9.setText("jTextField3");
        jPanel4.add(jTextField9, new org.netbeans.lib.awtextra.AbsoluteConstraints(150, 130, 130, -1));

        jTextField10.setText("jTextField3");
        jPanel4.add(jTextField10, new org.netbeans.lib.awtextra.AbsoluteConstraints(150, 80, 130, -1));

        jPanel3.add(jPanel4, new org.netbeans.lib.awtextra.AbsoluteConstraints(780, 70, 320, 390));

        jPanel5.setBackground(new java.awt.Color(192, 245, 255));
        jPanel5.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        jPanel5.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabel13.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel13.setText("Discount:");
        jPanel5.add(jLabel13, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 130, -1, -1));

        jLabel14.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel14.setText("Tax:");
        jPanel5.add(jLabel14, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 180, -1, -1));

        jLabel15.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel15.setText("Final-Total:");
        jPanel5.add(jLabel15, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 230, -1, 20));

        jLabel16.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel16.setText("Sub-Total:");
        jPanel5.add(jLabel16, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 80, -1, 20));

        jTextField11.setText("jTextField3");
        jPanel5.add(jTextField11, new org.netbeans.lib.awtextra.AbsoluteConstraints(150, 230, 130, -1));

        jTextField12.setText("jTextField3");
        jPanel5.add(jTextField12, new org.netbeans.lib.awtextra.AbsoluteConstraints(150, 180, 130, -1));

        jTextField13.setText("jTextField3");
        jPanel5.add(jTextField13, new org.netbeans.lib.awtextra.AbsoluteConstraints(150, 130, 130, -1));

        jTextField14.setText("jTextField3");
        jPanel5.add(jTextField14, new org.netbeans.lib.awtextra.AbsoluteConstraints(150, 80, 130, -1));

        jPanel6.setBackground(new java.awt.Color(192, 245, 255));
        jPanel6.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        jPanel6.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabel17.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel17.setText("Discount:");
        jPanel6.add(jLabel17, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 130, -1, -1));

        jLabel18.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel18.setText("Tax:");
        jPanel6.add(jLabel18, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 180, -1, -1));

        jLabel19.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel19.setText("Final-Total:");
        jPanel6.add(jLabel19, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 230, -1, 20));

        jLabel20.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel20.setText("Sub-Total:");
        jPanel6.add(jLabel20, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 80, -1, 20));

        jTextField15.setText("jTextField3");
        jPanel6.add(jTextField15, new org.netbeans.lib.awtextra.AbsoluteConstraints(150, 230, 130, -1));

        jTextField16.setText("jTextField3");
        jPanel6.add(jTextField16, new org.netbeans.lib.awtextra.AbsoluteConstraints(150, 180, 130, -1));

        jTextField17.setText("jTextField3");
        jPanel6.add(jTextField17, new org.netbeans.lib.awtextra.AbsoluteConstraints(150, 130, 130, -1));

        jTextField18.setText("jTextField3");
        jPanel6.add(jTextField18, new org.netbeans.lib.awtextra.AbsoluteConstraints(150, 80, 130, -1));

        jPanel5.add(jPanel6, new org.netbeans.lib.awtextra.AbsoluteConstraints(780, 70, 320, 390));

        jPanel3.add(jPanel5, new org.netbeans.lib.awtextra.AbsoluteConstraints(780, 70, 320, 390));

        jLabel22.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel22.setText("Amount Paid:");
        jPanel3.add(jLabel22, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 330, -1, 20));

        cmbPaymentMode.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "CASH", "UPI", "CARD", "NET BANKING" }));
        jPanel3.add(cmbPaymentMode, new org.netbeans.lib.awtextra.AbsoluteConstraints(150, 280, 130, -1));

        jLabel23.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel23.setText("Invoice No.:");
        jPanel3.add(jLabel23, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 40, -1, 20));

        jLabel24.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel24.setText("Sub-Total:");
        jPanel3.add(jLabel24, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 80, -1, 20));

        txtAmountDue.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtAmountDueActionPerformed(evt);
            }
        });
        jPanel3.add(txtAmountDue, new org.netbeans.lib.awtextra.AbsoluteConstraints(150, 380, 130, -1));

        txtSubTotal.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtSubTotalActionPerformed(evt);
            }
        });
        jPanel3.add(txtSubTotal, new org.netbeans.lib.awtextra.AbsoluteConstraints(150, 80, 130, -1));

        txtAmountPaid.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtAmountPaidActionPerformed(evt);
            }
        });
        jPanel3.add(txtAmountPaid, new org.netbeans.lib.awtextra.AbsoluteConstraints(150, 330, 130, -1));

        jLabel25.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel25.setText("Amount Due:");
        jPanel3.add(jLabel25, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 380, -1, 20));

        jLabel26.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel26.setText("Final-Total:");
        jPanel3.add(jLabel26, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 230, -1, 20));

        getContentPane().add(jPanel3, new org.netbeans.lib.awtextra.AbsoluteConstraints(1190, 70, 320, 440));

        jPanel7.setBackground(new java.awt.Color(167, 236, 236));
        jPanel7.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jButton7.setText("SALE");
        jButton7.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton7ActionPerformed(evt);
            }
        });
        jPanel7.add(jButton7, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 80, 180, -1));

        jButton8.setText("PURCHASE");
        jButton8.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton8ActionPerformed(evt);
            }
        });
        jPanel7.add(jButton8, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 130, 180, -1));

        jButton9.setText("INVENTORY");
        jButton9.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton9ActionPerformed(evt);
            }
        });
        jPanel7.add(jButton9, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 180, 180, -1));

        jButton10.setText("EXPENSE");
        jButton10.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton10ActionPerformed(evt);
            }
        });
        jPanel7.add(jButton10, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 230, 180, -1));

        jButton11.setText("CUSTOMER");
        jButton11.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton11ActionPerformed(evt);
            }
        });
        jPanel7.add(jButton11, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 280, 180, -1));

        jButton12.setText("REPORTS");
        jButton12.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton12ActionPerformed(evt);
            }
        });
        jPanel7.add(jButton12, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 340, 180, -1));

        jButton13.setText("STAFF");
        jButton13.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton13ActionPerformed(evt);
            }
        });
        jPanel7.add(jButton13, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 400, 180, -1));

        jButton14.setText("SETTINGS");
        jButton14.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton14ActionPerformed(evt);
            }
        });
        jPanel7.add(jButton14, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 460, 180, -1));

        jButton16.setText("HOME");
        jPanel7.add(jButton16, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 30, 180, -1));

        getContentPane().add(jPanel7, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 60, 200, 690));

        jButton2.setBackground(new java.awt.Color(46, 152, 249));
        jButton2.setFont(new java.awt.Font("Segoe UI", 1, 24)); // NOI18N
        jButton2.setText("GENERATE BILL");
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });
        getContentPane().add(jButton2, new org.netbeans.lib.awtextra.AbsoluteConstraints(1190, 520, 320, -1));

        jButton3.setBackground(new java.awt.Color(255, 102, 102));
        jButton3.setText("DELETE ITEM");
        getContentPane().add(jButton3, new org.netbeans.lib.awtextra.AbsoluteConstraints(950, 550, 130, -1));

        jButton4.setBackground(new java.awt.Color(255, 255, 153));
        jButton4.setText("PRODUCT ENTRY");
        jButton4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton4ActionPerformed(evt);
            }
        });
        getContentPane().add(jButton4, new org.netbeans.lib.awtextra.AbsoluteConstraints(290, 550, -1, -1));

        jButton5.setBackground(new java.awt.Color(204, 204, 255));
        jButton5.setText("SEARCH PRODUCT");
        getContentPane().add(jButton5, new org.netbeans.lib.awtextra.AbsoluteConstraints(510, 550, -1, -1));

        jButton6.setBackground(new java.awt.Color(204, 255, 204));
        jButton6.setText("STOCK");
        getContentPane().add(jButton6, new org.netbeans.lib.awtextra.AbsoluteConstraints(740, 550, 130, -1));

        jMenu1.setText("File");
        jMenuBar1.add(jMenu1);

        jMenu2.setText("Edit");
        jMenuBar1.add(jMenu2);

        setJMenuBar(jMenuBar1);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jComboBox1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboBox1ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jComboBox1ActionPerformed

    private void invoiceTextFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_invoiceTextFieldActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_invoiceTextFieldActionPerformed

    private void txtTaxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtTaxActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtTaxActionPerformed

    private void jButton4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton4ActionPerformed
        // TODO add your handling code here:
        ProductEntry pe= new ProductEntry();
        pe.setVisible(true);
    }//GEN-LAST:event_jButton4ActionPerformed

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
        // TODO add your handling code here:
          try {
        
        // Payment details
        String paymentMode = cmbPaymentMode.getSelectedItem().toString(); // Combo box
        double amountPaid  = 0.0;
        if (!txtAmountPaid.getText().trim().isEmpty()) {
            amountPaid = Double.parseDouble(txtAmountPaid.getText().trim());
        }

        // Call saveBill with values (not textfields)
        saveBill.saveBill(
            jTable1,         
            jTextFieldCustomerName,      
            jTextFieldCustomerPhone,     
            paymentMode,       
            amountPaid         
        );
        loadLastInvoiceNo();
        // Generate bill text and show preview only if save was successful
        String billText = BillGenerator.generateBill(jTable1);
        Bill_Dialogbox preview = new Bill_Dialogbox();
        preview.setBillText(billText);
        preview.setVisible(true);

    } catch (Exception e) {
        e.printStackTrace();
        JOptionPane.showMessageDialog(this, "Error saving bill: " + e.getMessage());
    }
        
    }//GEN-LAST:event_jButton2ActionPerformed

    private void jButton8ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton8ActionPerformed
        // TODO add your handling code here:
        Purchase pr= new Purchase();
        pr.setVisible(true);
    }//GEN-LAST:event_jButton8ActionPerformed

    private void jButton7ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton7ActionPerformed
        // TODO add your handling code here:
        Sale sl=new Sale();
        sl.setVisible(true);
    }//GEN-LAST:event_jButton7ActionPerformed

    private void jButton14ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton14ActionPerformed
        // TODO add your handling code here:
        Settings stg=new Settings();
        stg.setVisible(true);
    }//GEN-LAST:event_jButton14ActionPerformed

    private void jButton9ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton9ActionPerformed
        // TODO add your handling code here:
        StockManagement stc=new StockManagement();
        stc.setVisible(true);
    }//GEN-LAST:event_jButton9ActionPerformed

    private void jButton11ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton11ActionPerformed
        // TODO add your handling code here:
        Customer cr=new Customer();
        cr.setVisible(true);
    }//GEN-LAST:event_jButton11ActionPerformed

    private void jButton13ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton13ActionPerformed
        // TODO add your handling code here:
        Staff stf=new Staff();
        stf.setVisible(true);
    }//GEN-LAST:event_jButton13ActionPerformed

    private void jButton12ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton12ActionPerformed
        // TODO add your handling code here:
        Report rpt=new Report();
        rpt.setVisible(true);
    }//GEN-LAST:event_jButton12ActionPerformed

    private void jButton10ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton10ActionPerformed
        // TODO add your handling code here:
        Expense exp=new Expense();
        exp.setVisible(true);
    }//GEN-LAST:event_jButton10ActionPerformed

    private void txtAmountDueActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtAmountDueActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtAmountDueActionPerformed

    private void txtSubTotalActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtSubTotalActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtSubTotalActionPerformed

    private void txtAmountPaidActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtAmountPaidActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtAmountPaidActionPerformed

    private void jTextFieldSearchActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextFieldSearchActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jTextFieldSearchActionPerformed

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
            java.util.logging.Logger.getLogger(NewJFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(NewJFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(NewJFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(NewJFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new NewJFrame().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JComboBox<String> cmbPaymentMode;
    private javax.swing.JTextField invoiceTextField;
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton10;
    private javax.swing.JButton jButton11;
    private javax.swing.JButton jButton12;
    private javax.swing.JButton jButton13;
    private javax.swing.JButton jButton14;
    private javax.swing.JButton jButton16;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton3;
    private javax.swing.JButton jButton4;
    private javax.swing.JButton jButton5;
    private javax.swing.JButton jButton6;
    private javax.swing.JButton jButton7;
    private javax.swing.JButton jButton8;
    private javax.swing.JButton jButton9;
    private javax.swing.JComboBox<String> jComboBox1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel16;
    private javax.swing.JLabel jLabel17;
    private javax.swing.JLabel jLabel18;
    private javax.swing.JLabel jLabel19;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel20;
    private javax.swing.JLabel jLabel21;
    private javax.swing.JLabel jLabel22;
    private javax.swing.JLabel jLabel23;
    private javax.swing.JLabel jLabel24;
    private javax.swing.JLabel jLabel25;
    private javax.swing.JLabel jLabel26;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JMenu jMenu1;
    private javax.swing.JMenu jMenu2;
    private javax.swing.JMenuBar jMenuBar1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JPanel jPanel7;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JTable jTable1;
    private javax.swing.JTextField jTextField10;
    private javax.swing.JTextField jTextField11;
    private javax.swing.JTextField jTextField12;
    private javax.swing.JTextField jTextField13;
    private javax.swing.JTextField jTextField14;
    private javax.swing.JTextField jTextField15;
    private javax.swing.JTextField jTextField16;
    private javax.swing.JTextField jTextField17;
    private javax.swing.JTextField jTextField18;
    private javax.swing.JTextField jTextField7;
    private javax.swing.JTextField jTextField8;
    private javax.swing.JTextField jTextField9;
    private javax.swing.JTextField jTextFieldCustomerName;
    private javax.swing.JTextField jTextFieldCustomerPhone;
    private javax.swing.JTextField jTextFieldSearch;
    private javax.swing.JTextField txtAmountDue;
    private javax.swing.JTextField txtAmountPaid;
    private javax.swing.JTextField txtDiscount;
    private javax.swing.JTextField txtFinalTotal;
    private javax.swing.JTextField txtSubTotal;
    private javax.swing.JTextField txtTax;
    // End of variables declaration//GEN-END:variables
}
