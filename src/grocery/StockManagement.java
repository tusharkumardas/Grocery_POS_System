/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package grocery;
import Project.PDFStockReportGenerator;
import java.sql.*;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;
import javax.swing.JList;
import javax.swing.DefaultListModel;
import javax.swing.JScrollPane;
import javax.swing.JPopupMenu;

/**
 *
 * @author Tushar Kumar Das
 */
public class StockManagement extends javax.swing.JFrame {
    private JPopupMenu suggestionPopup;
    private JList<String> suggestionList;
    
    private void initSearchSuggestions() {
    suggestionPopup = new JPopupMenu();
    suggestionList = new JList<>();
    suggestionList.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);

    JScrollPane scrollPane = new JScrollPane(suggestionList);
    scrollPane.setPreferredSize(new java.awt.Dimension(jTextField19.getWidth(), 120));
    suggestionPopup.add(scrollPane);

    // Mouse selection
    suggestionList.addMouseListener(new java.awt.event.MouseAdapter() {
        public void mouseClicked(java.awt.event.MouseEvent e) {
            if (e.getClickCount() == 1) {
                selectSuggestion();
            }
        }
    });
    
    //keyboard arrow key working in sugestion box
    jTextField19.addKeyListener(new java.awt.event.KeyAdapter() {
    @Override
    public void keyPressed(java.awt.event.KeyEvent e) {

        if (e.getKeyCode() == java.awt.event.KeyEvent.VK_DOWN) {
            if (suggestionPopup.isVisible()) {
                suggestionList.requestFocus();
                suggestionList.setSelectedIndex(0);
            }
        }
    }
   });
    // Keyboard selection
    suggestionList.addKeyListener(new java.awt.event.KeyAdapter() {
        public void keyPressed(java.awt.event.KeyEvent e) {
            if (e.getKeyCode() == java.awt.event.KeyEvent.VK_ENTER) {
                selectSuggestion();
            }
        }
    });

    // Detect typing
    jTextField19.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
        public void insertUpdate(javax.swing.event.DocumentEvent e) { showSuggestions(); }
        public void removeUpdate(javax.swing.event.DocumentEvent e) { showSuggestions(); }
        public void changedUpdate(javax.swing.event.DocumentEvent e) { showSuggestions(); }
    });
}

    public void loadStockData() {
       DefaultTableModel model = (DefaultTableModel) jTable1.getModel();
       model.setRowCount(0); // clear existing rows
       try {
          Connection con = Project.ConnectionProvider.getCon();
          Statement st = con.createStatement();
          ResultSet rs = st.executeQuery("SELECT * FROM products");

          while (rs.next()) {
              Object[] row = new Object[] {
                  rs.getString("item_code"),
                  rs.getString("product_name"),
                  rs.getInt("qty"),
                  rs.getDouble("purchase_price"),
                  rs.getDouble("sale_price"),
                  rs.getDouble("mrp"),
                  rs.getDate("exp_date"),
                  rs.getString("brand_name"),
                  rs.getString("barcode"),
                  rs.getDouble("gst"),   
                  rs.getInt("stock_alert")
              };
              model.addRow(row);
            }

       } catch (Exception e) {
        System.out.println("Error: " + e);
       }
   }
    private void deleteSelectedProduct() {
    int selectedRow = jTable1.getSelectedRow();
    if (selectedRow != -1) {
        String itemCode = jTable1.getValueAt(selectedRow, 0).toString(); // assuming item code is column 0

        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to delete this product?",
                "Confirm Delete", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                java.sql.Connection con = Project.ConnectionProvider.getCon();
                String sql = "DELETE FROM products WHERE item_code = ?";
                PreparedStatement pst = con.prepareStatement(sql);
                pst.setString(1, itemCode);

                int rowsDeleted = pst.executeUpdate();
                if (rowsDeleted > 0) {
                    JOptionPane.showMessageDialog(this, "Product deleted successfully!");
                    loadStockData(); // reload updated data
                } else {
                    JOptionPane.showMessageDialog(this, "Failed to delete product.");
                }

                pst.close();
                con.close();
            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Error deleting product: " + ex.getMessage());
            }
        }
    } else {
        JOptionPane.showMessageDialog(this, "Please select a product to delete.");
    }
}

    private void showSuggestions() {
    String text = jTextField19.getText().trim();

    if (text.isEmpty()) {
        suggestionPopup.setVisible(false);
        loadStockData();
        return;
    }

    DefaultListModel<String> model = new DefaultListModel<>();

    try {
        Connection con = Project.ConnectionProvider.getCon();
        String sql = """
            SELECT item_code, product_name 
            FROM products
            WHERE product_name LIKE ?
               OR item_code LIKE ?
               OR barcode LIKE ?
            LIMIT 10
        """;

        PreparedStatement ps = con.prepareStatement(sql);
        String query = "%" + text + "%";
        ps.setString(1, query);
        ps.setString(2, query);
        ps.setString(3, query);

        ResultSet rs = ps.executeQuery();

        while (rs.next()) {
            String display =
                rs.getString("item_code") + " | " + rs.getString("product_name");
            model.addElement(display);
        }

        rs.close();
        ps.close();
        con.close();

    } catch (Exception e) {
        e.printStackTrace();
    }

    if (model.isEmpty()) {
        suggestionPopup.setVisible(false);
    } else {
        suggestionList.setModel(model);
        suggestionPopup.show(jTextField19, 0, jTextField19.getHeight());
        jTextField19.requestFocus();
    }

    // 🔥 LIVE table update while typing
    loadFilteredStock(text);
}

    private void selectSuggestion() {
    String selectedValue = suggestionList.getSelectedValue();
    if (selectedValue == null) return;

    jTextField19.setText(selectedValue);
    suggestionPopup.setVisible(false);
    loadFilteredStock(selectedValue);
}

    private void loadFilteredStock(String keyword) {
    DefaultTableModel model = (DefaultTableModel) jTable1.getModel();
    model.setRowCount(0);

    try {
        Connection con = Project.ConnectionProvider.getCon();
        String sql = """
            SELECT * FROM products 
            WHERE product_name LIKE ? 
               OR item_code LIKE ? 
               OR barcode LIKE ?
        """;

        PreparedStatement ps = con.prepareStatement(sql);
        String query = "%" + keyword + "%";
        ps.setString(1, query);
        ps.setString(2, query);
        ps.setString(3, query);

        ResultSet rs = ps.executeQuery();

        while (rs.next()) {
            model.addRow(new Object[]{
                rs.getString("item_code"),
                rs.getString("product_name"),
                rs.getInt("qty"),
                rs.getDouble("purchase_price"),
                rs.getDouble("sale_price"),
                rs.getDouble("mrp"),
                rs.getDate("exp_date"),
                rs.getString("brand_name"),
                rs.getString("barcode"),
                rs.getInt("stock_alert")
            });
        }

        rs.close();
        ps.close();
        con.close();

    } catch (Exception e) {
        e.printStackTrace();
    }
}

    
    /**
     * Creates new form StockManagement
     */
    public StockManagement() {
        initComponents();
        loadStockData();
        this.setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        initSearchSuggestions();
    }
    
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel2 = new javax.swing.JPanel();
        jTextField19 = new javax.swing.JTextField();
        jLabel21 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jButton2 = new javax.swing.JButton();
        jButton3 = new javax.swing.JButton();
        jButton5 = new javax.swing.JButton();
        jButton6 = new javax.swing.JButton();
        jScrollPane2 = new javax.swing.JScrollPane();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTable1 = new javax.swing.JTable();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jPanel2.setBackground(new java.awt.Color(174, 242, 242));
        jPanel2.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());
        jPanel2.add(jTextField19, new org.netbeans.lib.awtextra.AbsoluteConstraints(190, 10, 730, 30));

        jLabel21.setFont(new java.awt.Font("Segoe UI Black", 1, 18)); // NOI18N
        jLabel21.setText("SEARCH PRODUCT:");
        jPanel2.add(jLabel21, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 10, 200, -1));

        jLabel3.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        jLabel3.setText("Stock Details");
        jPanel2.add(jLabel3, new org.netbeans.lib.awtextra.AbsoluteConstraints(430, 50, -1, -1));

        jButton2.setBackground(new java.awt.Color(255, 51, 51));
        jButton2.setText("CLOSE");
        jButton2.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });
        jPanel2.add(jButton2, new org.netbeans.lib.awtextra.AbsoluteConstraints(750, 400, 110, 30));

        jButton3.setBackground(new java.awt.Color(153, 255, 153));
        jButton3.setText("STOCK REPORT");
        jButton3.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        jButton3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton3ActionPerformed(evt);
            }
        });
        jPanel2.add(jButton3, new org.netbeans.lib.awtextra.AbsoluteConstraints(530, 400, 110, 30));

        jButton5.setBackground(new java.awt.Color(255, 255, 153));
        jButton5.setText("DELETE");
        jButton5.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        jButton5.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton5ActionPerformed(evt);
            }
        });
        jPanel2.add(jButton5, new org.netbeans.lib.awtextra.AbsoluteConstraints(310, 400, 110, 30));

        jButton6.setBackground(new java.awt.Color(204, 204, 255));
        jButton6.setText("UPDATE");
        jButton6.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        jButton6.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton6ActionPerformed(evt);
            }
        });
        jPanel2.add(jButton6, new org.netbeans.lib.awtextra.AbsoluteConstraints(110, 400, 110, 30));

        jTable1.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null, null, null, null, null, null, null}
            },
            new String [] {
                "Item code", "Product Name", "QTY", "Purchase price", "Sale Price", "MRP", "EXP Date", "BRAND", "BARCODE", "GST", "Stock Alert"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false, false, false, false, false, false, false, false, false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        jScrollPane1.setViewportView(jTable1);

        jScrollPane2.setViewportView(jScrollPane1);

        jPanel2.add(jScrollPane2, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 80, 910, 300));

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addGap(0, 0, Short.MAX_VALUE)
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, 950, javax.swing.GroupLayout.PREFERRED_SIZE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel2, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 440, javax.swing.GroupLayout.PREFERRED_SIZE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents
    
    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
        // TODO add your handling code here:
        this.dispose();
        
    }//GEN-LAST:event_jButton2ActionPerformed

    private void jButton3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton3ActionPerformed
        // TODO add your handling code here:
        PDFStockReportGenerator generator = new PDFStockReportGenerator();
        generator.generateStockReportPDF();

}

private String padRight(String text, int length) {
    if (text == null) text = "";
    return String.format("%-" + length + "s", text);
    }//GEN-LAST:event_jButton3ActionPerformed

    private void jButton5ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton5ActionPerformed
        // TODO add your handling code here:
        deleteSelectedProduct();
    }//GEN-LAST:event_jButton5ActionPerformed

    private void jButton6ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton6ActionPerformed
        // TODO add your handling code here:                                        
        int selectedRow = jTable1.getSelectedRow();
        if (selectedRow != -1) {
        // Fetch data from selected row
        String itemCode = jTable1.getValueAt(selectedRow, 0).toString();
        String productName = jTable1.getValueAt(selectedRow, 1).toString();
        String qty = jTable1.getValueAt(selectedRow, 2).toString();
        String purchasePrice = jTable1.getValueAt(selectedRow, 3).toString();
        String salePrice = jTable1.getValueAt(selectedRow, 4).toString();
        String mrp = jTable1.getValueAt(selectedRow, 5).toString();
        String expDate = jTable1.getValueAt(selectedRow, 6).toString();
        String brand = jTable1.getValueAt(selectedRow, 7).toString();
        String barcode = jTable1.getValueAt(selectedRow, 8).toString();
        String gst = jTable1.getValueAt(selectedRow, 9).toString();
        String stockAlert = jTable1.getValueAt(selectedRow, 10).toString();

        UpdateForm updateForm = new UpdateForm(); // use default constructor
        updateForm.setProductDetails(itemCode, productName, qty, purchasePrice, salePrice, mrp, expDate, brand, barcode, gst, stockAlert);
        updateForm.setVisible(true);
 
    } else {
        JOptionPane.showMessageDialog(null, "Please select a row to update.");
    }
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
            java.util.logging.Logger.getLogger(StockManagement.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(StockManagement.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(StockManagement.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(StockManagement.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new StockManagement().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton3;
    private javax.swing.JButton jButton5;
    private javax.swing.JButton jButton6;
    private javax.swing.JLabel jLabel21;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JTable jTable1;
    private javax.swing.JTextField jTextField19;
    // End of variables declaration//GEN-END:variables
}
