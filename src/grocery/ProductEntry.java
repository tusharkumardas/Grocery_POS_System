/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package grocery;
import Project.ConnectionProvider;
import java.sql.Connection;
import java.sql.PreparedStatement;
import javax.swing.JOptionPane;
import java.sql.*;
import grocery.StockManagement;
import javax.swing.event.DocumentListener;
import javax.swing.event.DocumentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import java.awt.Color;


/**
 *
 * @author Tushar Kumar Das
 */
public class ProductEntry extends javax.swing.JFrame {
     private javax.swing.JScrollPane scrollPane;
     private javax.swing.JList<String> suggestionList;
     private DefaultListModel<String> listModel;

    private void showSuggestions() {
    String text = jTextField19.getText().trim();
    listModel.clear();

    if (text.isEmpty()) {
        scrollPane.setVisible(false);
        return;
    }

    try (Connection con = ConnectionProvider.getCon()) {
        String sql = "SELECT item_code, product_name, qty, purchase_price, sale_price, mrp, exp_date, brand_name, barcode, gst, stock_alert FROM products WHERE product_name LIKE ? OR item_code LIKE ?";
        PreparedStatement pst = con.prepareStatement(sql);
        pst.setString(1, "%" + text + "%");
        pst.setString(2, "%" + text + "%");

        ResultSet rs = pst.executeQuery();
        while (rs.next()) {
            String itc=rs.getString("item_code");
            String name = rs.getString("product_name");
            double mrp = rs.getDouble("mrp");
            double price = rs.getDouble("sale_price");
            listModel.addElement(itc+" | "+ name + " | MRP: " + mrp + " | ₹" + price);
        }

        scrollPane.setVisible(listModel.size() > 0);
    } catch (Exception e) {
        e.printStackTrace();
    }

    jPanel2.repaint();
    jPanel2.revalidate();
}
    private void setupSearchSuggestions(){
        listModel = new DefaultListModel<>();
        suggestionList = new JList<>(listModel);
        scrollPane = new JScrollPane(suggestionList);

        scrollPane = new JScrollPane(suggestionList);
        scrollPane.setVisible(false);
        scrollPane.setBackground(Color.WHITE);

        // Add to jPanel2 with AbsoluteLayout
        jPanel2.add(scrollPane, new org.netbeans.lib.awtextra.AbsoluteConstraints(
         jTextField19.getX(),
         jTextField19.getY() + jTextField19.getHeight(),
         jTextField19.getWidth(),
         100
        ));

        jPanel2.setComponentZOrder(scrollPane, 0);

       // Add typing listener
       jTextField19.getDocument().addDocumentListener(new DocumentListener() {
          public void insertUpdate(DocumentEvent e) { showSuggestions(); }
          public void removeUpdate(DocumentEvent e) { showSuggestions(); }
          public void changedUpdate(DocumentEvent e) { showSuggestions(); }
        });

        suggestionList.addMouseListener(new MouseAdapter() {
         public void mouseClicked(MouseEvent evt) {
        if (evt.getClickCount() == 1) {
            String selectedValue = suggestionList.getSelectedValue();
            if (selectedValue != null) {
                String itemCode = selectedValue.split("\\|")[0].trim(); // extract name
                fillProductFields(itemCode);
                scrollPane.setVisible(false); // hide after selection
            }
        }
    }
});
       
}
    
    private void fillProductFields(String itemCode) {
    try (Connection con = ConnectionProvider.getCon()) {
        String sql = "SELECT item_code, product_name, qty, purchase_price, sale_price, mrp, exp_date, brand_name, barcode, gst, stock_alert FROM products WHERE product_name LIKE ? OR item_code LIKE ?";
        PreparedStatement pst = con.prepareStatement(sql);
        pst.setString(1, "%" + itemCode + "%");
        pst.setString(2, "%" + itemCode + "%");

        ResultSet rs = pst.executeQuery();
        if (rs.next()) {
            // Now set your UI fields here:
            jTextField9.setText(rs.getString("item_code"));
            jTextField1.setText(rs.getString("product_name"));
            jTextField2.setText(String.valueOf(rs.getInt("qty")));
            jTextField3.setText(String.valueOf(rs.getDouble("purchase_price")));
            jTextField4.setText(String.valueOf(rs.getDouble("sale_price")));
            jTextField6.setText(String.valueOf(rs.getDouble("mrp")));
            jTextField10.setText(rs.getString("exp_date"));
            jTextField11.setText(rs.getString("brand_name"));
            jTextField8.setText(rs.getString("barcode"));
            jTextField7.setText(rs.getString("gst"));
            jTextField5.setText(rs.getString("stock_alert"));
        }

    } catch (Exception e) {
        e.printStackTrace();
    }
}

    /**
     * Creates new form ProductEntry
     */
    public ProductEntry() {
        initComponents();
        setupSearchSuggestions();
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

        jPanel2 = new javax.swing.JPanel();
        jTextField19 = new javax.swing.JTextField();
        jLabel21 = new javax.swing.JLabel();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        jLabel11 = new javax.swing.JLabel();
        jLabel12 = new javax.swing.JLabel();
        jLabel13 = new javax.swing.JLabel();
        jLabel14 = new javax.swing.JLabel();
        jTextField1 = new javax.swing.JTextField();
        jTextField2 = new javax.swing.JTextField();
        jTextField3 = new javax.swing.JTextField();
        jTextField4 = new javax.swing.JTextField();
        jTextField5 = new javax.swing.JTextField();
        jTextField6 = new javax.swing.JTextField();
        jTextField7 = new javax.swing.JTextField();
        jTextField8 = new javax.swing.JTextField();
        jTextField9 = new javax.swing.JTextField();
        jTextField10 = new javax.swing.JTextField();
        jLabel7 = new javax.swing.JLabel();
        jTextField11 = new javax.swing.JTextField();
        jButton1 = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jPanel2.setBackground(new java.awt.Color(174, 242, 242));
        jPanel2.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());
        jPanel2.add(jTextField19, new org.netbeans.lib.awtextra.AbsoluteConstraints(190, 10, 730, 30));

        jLabel21.setFont(new java.awt.Font("Segoe UI Black", 1, 18)); // NOI18N
        jLabel21.setText("SEARCH PRODUCT:");
        jPanel2.add(jLabel21, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 10, 200, -1));

        jLabel1.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel1.setText("Item Code:");
        jPanel2.add(jLabel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 80, -1, -1));

        jLabel2.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel2.setText("MRP:");
        jPanel2.add(jLabel2, new org.netbeans.lib.awtextra.AbsoluteConstraints(370, 150, -1, -1));

        jLabel3.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel3.setText("Sale Price:");
        jPanel2.add(jLabel3, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 350, -1, -1));

        jLabel4.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel4.setText("Stock Alert At:");
        jPanel2.add(jLabel4, new org.netbeans.lib.awtextra.AbsoluteConstraints(670, 80, -1, -1));

        jLabel5.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel5.setText("Purchase Price:");
        jPanel2.add(jLabel5, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 280, -1, -1));

        jLabel6.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel6.setText("Brand Name:");
        jPanel2.add(jLabel6, new org.netbeans.lib.awtextra.AbsoluteConstraints(360, 80, -1, -1));

        jLabel11.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel11.setText("QTY:");
        jPanel2.add(jLabel11, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 220, -1, -1));

        jLabel12.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel12.setText("GST:");
        jPanel2.add(jLabel12, new org.netbeans.lib.awtextra.AbsoluteConstraints(370, 220, -1, -1));

        jLabel13.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel13.setText("EXP DATE:");
        jPanel2.add(jLabel13, new org.netbeans.lib.awtextra.AbsoluteConstraints(360, 290, -1, -1));

        jLabel14.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel14.setText("Product Name:");
        jPanel2.add(jLabel14, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 150, -1, -1));
        jPanel2.add(jTextField1, new org.netbeans.lib.awtextra.AbsoluteConstraints(160, 150, 160, -1));

        jTextField2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTextField2ActionPerformed(evt);
            }
        });
        jPanel2.add(jTextField2, new org.netbeans.lib.awtextra.AbsoluteConstraints(160, 220, 160, -1));
        jPanel2.add(jTextField3, new org.netbeans.lib.awtextra.AbsoluteConstraints(160, 290, 160, -1));

        jTextField4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTextField4ActionPerformed(evt);
            }
        });
        jPanel2.add(jTextField4, new org.netbeans.lib.awtextra.AbsoluteConstraints(160, 350, 160, -1));

        jTextField5.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTextField5ActionPerformed(evt);
            }
        });
        jPanel2.add(jTextField5, new org.netbeans.lib.awtextra.AbsoluteConstraints(780, 80, 160, -1));
        jPanel2.add(jTextField6, new org.netbeans.lib.awtextra.AbsoluteConstraints(460, 150, 160, -1));
        jPanel2.add(jTextField7, new org.netbeans.lib.awtextra.AbsoluteConstraints(460, 220, 160, -1));
        jPanel2.add(jTextField8, new org.netbeans.lib.awtextra.AbsoluteConstraints(460, 350, 160, -1));
        jPanel2.add(jTextField9, new org.netbeans.lib.awtextra.AbsoluteConstraints(160, 80, 160, -1));
        jPanel2.add(jTextField10, new org.netbeans.lib.awtextra.AbsoluteConstraints(460, 290, 160, -1));

        jLabel7.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel7.setText("Barcode No:");
        jPanel2.add(jLabel7, new org.netbeans.lib.awtextra.AbsoluteConstraints(360, 350, -1, -1));

        jTextField11.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTextField11ActionPerformed(evt);
            }
        });
        jPanel2.add(jTextField11, new org.netbeans.lib.awtextra.AbsoluteConstraints(460, 80, 160, -1));

        jButton1.setBackground(new java.awt.Color(204, 255, 204));
        jButton1.setText("STOCK CHECK");
        jButton1.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });
        jPanel2.add(jButton1, new org.netbeans.lib.awtextra.AbsoluteConstraints(710, 260, 180, 40));

        jButton2.setBackground(new java.awt.Color(255, 204, 204));
        jButton2.setText("ADD TO STOCK");
        jButton2.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });
        jPanel2.add(jButton2, new org.netbeans.lib.awtextra.AbsoluteConstraints(710, 190, 180, 40));

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

    private void jTextField2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextField2ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jTextField2ActionPerformed

    private void jTextField4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextField4ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jTextField4ActionPerformed

    private void jTextField5ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextField5ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jTextField5ActionPerformed

    private void jTextField11ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextField11ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jTextField11ActionPerformed

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
        // TODO add your handling code here:
        

    try {
        Connection con = ConnectionProvider.getCon();
        String sql = "INSERT INTO products (item_code, product_name, qty, purchase_price, sale_price, mrp, exp_date, brand_name, barcode, gst, stock_alert) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        PreparedStatement pst = con.prepareStatement(sql);

        pst.setString(1, jTextField9.getText());   // item_code
        pst.setString(2, jTextField1.getText());   // product_name
        pst.setInt(3, Integer.parseInt(jTextField2.getText()));  // qty
        pst.setDouble(4, Double.parseDouble(jTextField3.getText())); // purchase_price
        pst.setDouble(5, Double.parseDouble(jTextField4.getText())); // sale_price
        pst.setDouble(6, Double.parseDouble(jTextField6.getText())); // mrp
        pst.setDate(7, java.sql.Date.valueOf(jTextField10.getText())); // exp_date (format: yyyy-mm-dd)
        pst.setString(8, jTextField11.getText()); // brand_name
        pst.setString(9, jTextField8.getText()); // barcode
        pst.setDouble(10, Double.parseDouble(jTextField7.getText())); // gst
        pst.setInt(11, Integer.parseInt(jTextField5.getText())); // stock_alert

        pst.executeUpdate();
        JOptionPane.showMessageDialog(this, "Product added successfully!");
        con.close();
    } catch (Exception e) {
        e.printStackTrace();
        JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
    }
    }//GEN-LAST:event_jButton2ActionPerformed

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        // TODO add your handling code here:
        StockManagement st=new StockManagement();
        st.setVisible(true);
    }//GEN-LAST:event_jButton1ActionPerformed

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
            java.util.logging.Logger.getLogger(ProductEntry.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(ProductEntry.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(ProductEntry.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(ProductEntry.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new ProductEntry().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel21;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JTextField jTextField1;
    private javax.swing.JTextField jTextField10;
    private javax.swing.JTextField jTextField11;
    private javax.swing.JTextField jTextField19;
    private javax.swing.JTextField jTextField2;
    private javax.swing.JTextField jTextField3;
    private javax.swing.JTextField jTextField4;
    private javax.swing.JTextField jTextField5;
    private javax.swing.JTextField jTextField6;
    private javax.swing.JTextField jTextField7;
    private javax.swing.JTextField jTextField8;
    private javax.swing.JTextField jTextField9;
    // End of variables declaration//GEN-END:variables
}
