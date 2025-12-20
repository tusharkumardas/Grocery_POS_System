/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package grocery;
import javax.swing.JOptionPane;
import Project.ConnectionProvider;
import java.sql.Connection;
import java.sql.PreparedStatement;
import Project.PinUtil;
import javax.swing.table.DefaultTableModel;
import java.sql.ResultSet;
import Project.Session;
/**
 *
 * @author Tushar Kumar Das
 */
public class Settings extends javax.swing.JFrame {
    private void clearUserFields() {
    txtUsername.setText("");
    txtPassword.setText("");
    cmbRole.setSelectedIndex(0);
    cmbStatus.setSelectedIndex(0);
}
private void loadUsersTable() {
    DefaultTableModel model = (DefaultTableModel) jTable1.getModel();
    model.setRowCount(0);

    try (Connection con = ConnectionProvider.getCon()) {
        ResultSet rs = con.prepareStatement(
            "SELECT username, role, status, created_at FROM users"
        ).executeQuery();

        while (rs.next()) {
            model.addRow(new Object[]{
                rs.getString("username"),
                rs.getString("role"),
                rs.getString("status"),
                rs.getTimestamp("created_at")
            });
        }
    } catch (Exception e) {
        e.printStackTrace();
    }
}
private void loadCompanyInfo() {
    try (Connection con = ConnectionProvider.getCon()) {
        String sql = "SELECT * FROM company_settings WHERE id = 1";
        PreparedStatement pst = con.prepareStatement(sql);
        ResultSet rs = pst.executeQuery();

        if (rs.next()) {
            jTextField7.setText(rs.getString("company_name"));
            jTextField2.setText(rs.getString("gst_no"));
            jTextField3.setText(rs.getString("contact_no"));
        }
    } catch (Exception e) {
        e.printStackTrace();
    }
}
private void saveCompanyInfo() {
    try (Connection con = ConnectionProvider.getCon()) {
        String sql = """
            UPDATE company_settings
            SET company_name=?, gst_no=?, contact_no=?
            WHERE id=1
        """;
        PreparedStatement pst = con.prepareStatement(sql);
        pst.setString(1, jTextField7.getText().trim());
        pst.setString(2, jTextField2.getText().trim());
        pst.setString(3, jTextField3.getText().trim());
        pst.executeUpdate();

        JOptionPane.showMessageDialog(this, "Company info saved");
    } catch (Exception e) {
        e.printStackTrace();
    }
}
    private void deleteSelectedUser() {
    int row = jTable1.getSelectedRow();

    if (row == -1) {
        JOptionPane.showMessageDialog(this, "Please select a user to delete");
        return;
    }

    String username = jTable1.getValueAt(row, 0).toString();

    int confirm = JOptionPane.showConfirmDialog(
        this,
        "Are you sure you want to delete user: " + username + "?",
        "Confirm Delete",
        JOptionPane.YES_NO_OPTION
    );

    if (confirm != JOptionPane.YES_OPTION) return;

    try (Connection con = ConnectionProvider.getCon()) {
        PreparedStatement ps = con.prepareStatement(
            "DELETE FROM users WHERE username = ?"
        );
        ps.setString(1, username);
        ps.executeUpdate();

        JOptionPane.showMessageDialog(this, "User deleted successfully");
        loadUsersTable();
        clearUserFields();

    } catch (Exception e) {
        e.printStackTrace();
        JOptionPane.showMessageDialog(this, "Error deleting user");
    }
}
 
    private void populateUserFields() {
    int row = jTable1.getSelectedRow();
    if (row == -1) return;

    txtUsername.setText(jTable1.getValueAt(row, 0).toString());
    txtPassword.setText(jTable1.getValueAt(row, 1).toString());
    cmbRole.setSelectedItem(jTable1.getValueAt(row, 2).toString());
    cmbStatus.setSelectedItem(jTable1.getValueAt(row, 3).toString());

    // IMPORTANT: password field always blank
    txtPassword.setText("");
}
    private void updateUserOrResetPassword() {

    int row = jTable1.getSelectedRow();
    if (row == -1) {
        JOptionPane.showMessageDialog(this, "Please select a user first");
        return;
    }

    String username = txtUsername.getText().trim();
    String role = cmbRole.getSelectedItem().toString();
    String status = cmbStatus.getSelectedItem().toString();
    String newPassword = new String(txtPassword.getPassword()).trim();

    if (username.isEmpty() || role.equals("--ROLE--")) {
        JOptionPane.showMessageDialog(this, "Username and Role are required");
        return;
    }

    try (Connection con = ConnectionProvider.getCon()) {

        PreparedStatement ps;

        // 🔹 CASE 1: Password NOT entered → update only role & status
        if (newPassword.isEmpty()) {

            String sql = """
                UPDATE users
                SET role = ?, status = ?
                WHERE username = ?
            """;

            ps = con.prepareStatement(sql);
            ps.setString(1, role.toLowerCase());
            ps.setString(2, status.toLowerCase());
            ps.setString(3, username);

        } 
        // 🔹 CASE 2: Password entered → hash & update everything
        else {

            if (!newPassword.matches("\\d{6}")) {
                JOptionPane.showMessageDialog(this, "Password must be exactly 6 digits");
                return;
            }

            String hashedPin = PinUtil.hashPin(newPassword);

            String sql = """
                UPDATE users
                SET pin_hash = ?, role = ?, status = ?
                WHERE username = ?
            """;

            ps = con.prepareStatement(sql);
            ps.setString(1, hashedPin);
            ps.setString(2, role.toLowerCase());
            ps.setString(3, status.toLowerCase());
            ps.setString(4, username);
        }

        ps.executeUpdate();

        JOptionPane.showMessageDialog(this, "User updated successfully");
        loadUsersTable();
        clearUserFields();

    } catch (Exception e) {
        e.printStackTrace();
        JOptionPane.showMessageDialog(this, "Error updating user");
    }
}



    
    /**
     * Creates new form Settings
     */
    public Settings() {
        initComponents();
        if (!"admin".equalsIgnoreCase(Session.role)) {
        JOptionPane.showMessageDialog(this, "Admin access only");
        dispose();
}
        loadUsersTable();
        loadCompanyInfo();
        jTable1.addKeyListener(new java.awt.event.KeyAdapter() {
        @Override
        public void keyPressed(java.awt.event.KeyEvent evt) {
         if (evt.getKeyCode() == java.awt.event.KeyEvent.VK_ENTER) {
            evt.consume(); // prevent row jump
            populateUserFields();
        }
    }
});

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
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jTextField2 = new javax.swing.JTextField();
        jTextField3 = new javax.swing.JTextField();
        jLabel5 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTable1 = new javax.swing.JTable();
        jButton1 = new javax.swing.JButton();
        jButton4 = new javax.swing.JButton();
        jLabel6 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        txtUsername = new javax.swing.JTextField();
        cmbStatus = new javax.swing.JComboBox<>();
        jButton2 = new javax.swing.JButton();
        cmbRole = new javax.swing.JComboBox<>();
        jLabel9 = new javax.swing.JLabel();
        jTextField7 = new javax.swing.JTextField();
        jButton5 = new javax.swing.JButton();
        txtPassword = new javax.swing.JPasswordField();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        getContentPane().setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jPanel1.setBackground(new java.awt.Color(236, 233, 233));
        jPanel1.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabel3.setText("Username:");
        jPanel1.add(jLabel3, new org.netbeans.lib.awtextra.AbsoluteConstraints(540, 210, -1, -1));

        jLabel4.setText("Contact No.:");
        jPanel1.add(jLabel4, new org.netbeans.lib.awtextra.AbsoluteConstraints(121, 117, -1, -1));
        jPanel1.add(jTextField2, new org.netbeans.lib.awtextra.AbsoluteConstraints(227, 77, 152, -1));
        jPanel1.add(jTextField3, new org.netbeans.lib.awtextra.AbsoluteConstraints(227, 117, 152, -1));

        jLabel5.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        jLabel5.setText("User Management");
        jPanel1.add(jLabel5, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 160, -1, -1));

        jScrollPane1.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
        jScrollPane1.setVerticalScrollBarPolicy(javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);

        jTable1.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Username", "Role", "Status", "Created At"
            }
        ));
        jScrollPane1.setViewportView(jTable1);

        jPanel1.add(jScrollPane1, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 190, 490, 270));

        jButton1.setBackground(new java.awt.Color(204, 255, 255));
        jButton1.setText("UPDATE USER OR RESET PASSWORD");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });
        jPanel1.add(jButton1, new org.netbeans.lib.awtextra.AbsoluteConstraints(550, 420, 270, -1));

        jButton4.setText("ADD USER");
        jButton4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton4ActionPerformed(evt);
            }
        });
        jPanel1.add(jButton4, new org.netbeans.lib.awtextra.AbsoluteConstraints(550, 390, 130, -1));

        jLabel6.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        jLabel6.setText("Company Info");
        jPanel1.add(jLabel6, new org.netbeans.lib.awtextra.AbsoluteConstraints(6, 6, -1, -1));

        jLabel7.setText("Company Name:");
        jPanel1.add(jLabel7, new org.netbeans.lib.awtextra.AbsoluteConstraints(121, 40, -1, -1));

        jLabel8.setText("GST No.:");
        jPanel1.add(jLabel8, new org.netbeans.lib.awtextra.AbsoluteConstraints(121, 80, -1, -1));
        jPanel1.add(txtUsername, new org.netbeans.lib.awtextra.AbsoluteConstraints(600, 210, 152, -1));

        cmbStatus.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "ACTIVE", "INACTIVE" }));
        jPanel1.add(cmbStatus, new org.netbeans.lib.awtextra.AbsoluteConstraints(680, 300, -1, -1));

        jButton2.setText("DELETE USER");
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });
        jPanel1.add(jButton2, new org.netbeans.lib.awtextra.AbsoluteConstraints(690, 390, 130, -1));

        cmbRole.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "--ROLE--", "ADMIN", "MANAGER", "CASHIER" }));
        jPanel1.add(cmbRole, new org.netbeans.lib.awtextra.AbsoluteConstraints(540, 300, -1, -1));

        jLabel9.setText("Password:");
        jPanel1.add(jLabel9, new org.netbeans.lib.awtextra.AbsoluteConstraints(540, 250, -1, -1));
        jPanel1.add(jTextField7, new org.netbeans.lib.awtextra.AbsoluteConstraints(227, 37, 152, -1));

        jButton5.setText("SAVE INFO");
        jButton5.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton5ActionPerformed(evt);
            }
        });
        jPanel1.add(jButton5, new org.netbeans.lib.awtextra.AbsoluteConstraints(410, 110, 140, 30));
        jPanel1.add(txtPassword, new org.netbeans.lib.awtextra.AbsoluteConstraints(600, 250, 150, -1));

        getContentPane().add(jPanel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 840, 470));

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jButton4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton4ActionPerformed
        // TODO add your handling code here:
    String username = txtUsername.getText().trim();   // Username
    String pin = txtPassword.getText().trim();        // PIN
    String role = cmbRole.getSelectedItem().toString();
    String status = cmbStatus.getSelectedItem().toString();

    if (username.isEmpty() || pin.isEmpty() || role.equals("--ROLE--")) {
        JOptionPane.showMessageDialog(this, "All fields are required");
        return;
    }

    if (!pin.matches("\\d{6}")) {
        JOptionPane.showMessageDialog(this, "PIN must be exactly 6 digits");
        return;
    }

    String pinHash = PinUtil.hashPin(pin);

    try (Connection con = ConnectionProvider.getCon()) {

        String sql = """
            INSERT INTO users (username, pin_hash, role, status)
            VALUES (?, ?, ?, ?)
        """;

        PreparedStatement pst = con.prepareStatement(sql);
        pst.setString(1, username);
        pst.setString(2, pinHash);
        pst.setString(3, role.toLowerCase());
        pst.setString(4, status.toLowerCase());
        pst.executeUpdate();

        JOptionPane.showMessageDialog(this, "User created successfully");
        loadUsersTable();
        clearUserFields();

    } catch (Exception e) {
        JOptionPane.showMessageDialog(this, "Username already exists");
    }
    }//GEN-LAST:event_jButton4ActionPerformed

    private void jButton5ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton5ActionPerformed
        // TODO add your handling code here:
        saveCompanyInfo();
    }//GEN-LAST:event_jButton5ActionPerformed

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
        // TODO add your handling code here:
        deleteSelectedUser();
    }//GEN-LAST:event_jButton2ActionPerformed

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        // TODO add your handling code here:
        updateUserOrResetPassword();
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
            java.util.logging.Logger.getLogger(Settings.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(Settings.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(Settings.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(Settings.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new Settings().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JComboBox<String> cmbRole;
    private javax.swing.JComboBox<String> cmbStatus;
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton4;
    private javax.swing.JButton jButton5;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTable jTable1;
    private javax.swing.JTextField jTextField2;
    private javax.swing.JTextField jTextField3;
    private javax.swing.JTextField jTextField7;
    private javax.swing.JPasswordField txtPassword;
    private javax.swing.JTextField txtUsername;
    // End of variables declaration//GEN-END:variables
}
