/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package grocery;

import Project.ConnectionProvider;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import IDgenerator.StaffIDGenerator;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.JOptionPane;
import java.awt.Desktop;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
/**
 *
 * @author Tushar Kumar Das
 */
public class Staff extends javax.swing.JFrame {
    private void addStaff() {
    String staffId = StaffIDGenerator.generateStaffID(); // generate ID
    String name = jTextField1.getText();
    String dob = jTextField2.getText();  // format: yyyy-mm-dd
    String phone = jTextField3.getText();
    String email = jTextField4.getText();
    String role = jComboBox1.getSelectedItem().toString();
    String salary = jTextField5.getText();
    String joinDate = jTextField9.getText(); // format: yyyy-mm-dd
    String govtId = jTextField6.getText();

    // Basic validation
    if (name.isEmpty() || dob.isEmpty() || phone.isEmpty() || email.isEmpty() || "--select--".equals(role)) {
        javax.swing.JOptionPane.showMessageDialog(this, "Please fill all required fields!");
        return;
    }

    try (Connection con = ConnectionProvider.getCon()) {
        String sql = "INSERT INTO staff (staff_code, name, dob, phone, email, role, salary, join_date, govt_id_path) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        PreparedStatement pst = con.prepareStatement(sql);
        pst.setString(1, staffId);
        pst.setString(2, name);
        pst.setString(3, dob);
        pst.setString(4, phone);
        pst.setString(5, email);
        pst.setString(6, role);
        pst.setString(7, salary);
        pst.setString(8, joinDate);
        pst.setString(9, govtId);

        pst.executeUpdate();
        javax.swing.JOptionPane.showMessageDialog(this, "Staff added successfully! ID: " + staffId);

        loadStaffTable(); // refresh table
    } catch (Exception e) {
        e.printStackTrace();
        javax.swing.JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
    }
}


     private void loadStaffTable() {
    try (Connection con = ConnectionProvider.getCon()) {
        String sql = "SELECT staff_code, name, dob, phone, email, role, salary, join_date, govt_id_path FROM staff";
        PreparedStatement pst = con.prepareStatement(sql);
        ResultSet rs = pst.executeQuery();

        javax.swing.table.DefaultTableModel model = (javax.swing.table.DefaultTableModel) jTable2.getModel();
        model.setRowCount(0); // clear table

        while (rs.next()) {
            Object[] row = {
                rs.getString("staff_code"),
                rs.getString("name"),
                rs.getString("dob"),
                rs.getString("phone"),
                rs.getString("email"),
                rs.getString("role"),
                rs.getString("salary"),
                rs.getString("join_date"),
                rs.getString("govt_id_path")
            };
            model.addRow(row);
        }
    } catch (Exception e) {
        e.printStackTrace();
    }
}
 private void clearFields() {
    // Text fields on the left
    if (jTextField1 != null) jTextField1.setText(""); // Name
    if (jTextField2 != null) jTextField2.setText(""); // DOB (yyyy-mm-dd)
    if (jTextField3 != null) jTextField3.setText(""); // Phone
    if (jTextField4 != null) jTextField4.setText(""); // Email
    if (jTextField5 != null) jTextField5.setText(""); // Salary
    if (jTextField9 != null) jTextField9.setText(""); // Join Date (yyyy-mm-dd)
    if (jTextField6 != null) jTextField6.setText(""); // Govt ID path

    // If you still have the (hidden) Staff ID text field in the form:
    // if (jTextField8 != null) jTextField8.setText("");

    // Role dropdown
    if (jComboBox1 != null) jComboBox1.setSelectedIndex(0); // --select--

    // Clear any table selection
    if (jTable2 != null) jTable2.clearSelection();
}
private void updateStaff() {
    int selectedRow = jTable2.getSelectedRow();
    if (selectedRow == -1) {
        JOptionPane.showMessageDialog(this, "Please select a staff record to update.");
        return;
    }

    try (Connection con = ConnectionProvider.getCon()) {
        // Get staff_code from JTable
        String staffCode = (String) jTable2.getValueAt(selectedRow, 0);

        // Get current DB values from JTable row
        String oldName = jTable2.getValueAt(selectedRow, 1).toString();
        String oldDob = jTable2.getValueAt(selectedRow, 2) != null ? jTable2.getValueAt(selectedRow, 2).toString() : "";
        String oldPhone = jTable2.getValueAt(selectedRow, 3) != null ? jTable2.getValueAt(selectedRow, 3).toString() : "";
        String oldEmail = jTable2.getValueAt(selectedRow, 4) != null ? jTable2.getValueAt(selectedRow, 4).toString() : "";
        String oldRole = jTable2.getValueAt(selectedRow, 5) != null ? jTable2.getValueAt(selectedRow, 5).toString() : "";
        String oldSalary = jTable2.getValueAt(selectedRow, 6) != null ? jTable2.getValueAt(selectedRow, 6).toString() : "0";
        String oldJoinDate = jTable2.getValueAt(selectedRow, 7) != null ? jTable2.getValueAt(selectedRow, 7).toString() : "";
        String oldGovtId = jTable2.getValueAt(selectedRow, 8) != null ? jTable2.getValueAt(selectedRow, 8).toString() : "";

        // Collect new form values (if empty, keep old)
        String name = jTextField1.getText().trim().isEmpty() ? oldName : jTextField1.getText().trim();
        String dob = jTextField2.getText().trim().isEmpty() ? oldDob : jTextField2.getText().trim();
        String phone = jTextField3.getText().trim().isEmpty() ? oldPhone : jTextField3.getText().trim();
        String email = jTextField4.getText().trim().isEmpty() ? oldEmail : jTextField4.getText().trim();
        String role = (jComboBox1.getSelectedItem() == null || jComboBox1.getSelectedItem().toString().equals("--select--")) 
                        ? oldRole : jComboBox1.getSelectedItem().toString();
        String salaryStr = jTextField5.getText().trim().isEmpty() ? oldSalary : jTextField5.getText().trim();
        String joinDate = jTextField9.getText().trim().isEmpty() ? oldJoinDate : jTextField9.getText().trim();
        String govtIdPath = jTextField6.getText().trim().isEmpty() ? oldGovtId : jTextField6.getText().trim();

        double salary = Double.parseDouble(salaryStr);

        // SQL Update query
        String sql = "UPDATE staff SET name=?, dob=?, phone=?, email=?, role=?, salary=?, join_date=?, govt_id_path=?, updated_at=NOW() WHERE staff_code=?";

        PreparedStatement pst = con.prepareStatement(sql);
        pst.setString(1, name);
        pst.setString(2, dob.isEmpty() ? null : dob);
        pst.setString(3, phone.isEmpty() ? null : phone);
        pst.setString(4, email.isEmpty() ? null : email);
        pst.setString(5, role);
        pst.setDouble(6, salary);
        pst.setString(7, joinDate.isEmpty() ? null : joinDate);
        pst.setString(8, govtIdPath);
        pst.setString(9, staffCode);

        int rowsAffected = pst.executeUpdate();

        if (rowsAffected > 0) {
            JOptionPane.showMessageDialog(this, "Staff updated successfully!");
            loadStaffTable();
            clearFields();
        } else {
            JOptionPane.showMessageDialog(this, "Update failed!");
        }

        pst.close();

    } catch (Exception e) {
        JOptionPane.showMessageDialog(this, "Error updating staff: " + e.getMessage());
        e.printStackTrace();
    }
}



    /**
     * Creates new form Staff
     */
    public Staff() {
        initComponents();
        loadStaffTable();
        jTable2.addMouseListener(new java.awt.event.MouseAdapter() {
    @Override
    public void mouseClicked(java.awt.event.MouseEvent evt) {
        if (evt.getClickCount() == 2) {  // double-click
            int row = jTable2.getSelectedRow();
            if (row != -1) {
                openGovtIdPdf(row);
            }
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
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        jLabel9 = new javax.swing.JLabel();
        jTextField1 = new javax.swing.JTextField();
        jTextField2 = new javax.swing.JTextField();
        jTextField3 = new javax.swing.JTextField();
        jTextField4 = new javax.swing.JTextField();
        jTextField5 = new javax.swing.JTextField();
        jTextField6 = new javax.swing.JTextField();
        jComboBox1 = new javax.swing.JComboBox<>();
        jButton1 = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();
        jButton3 = new javax.swing.JButton();
        jButton4 = new javax.swing.JButton();
        jScrollPane2 = new javax.swing.JScrollPane();
        jTable2 = new javax.swing.JTable();
        jButton5 = new javax.swing.JButton();
        jTextField9 = new javax.swing.JTextField();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        getContentPane().setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jPanel1.setBackground(new java.awt.Color(174, 242, 242));
        jPanel1.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabel1.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        jLabel1.setText("STAFF RECORDS");
        jPanel1.add(jLabel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(380, 10, -1, 20));

        jLabel2.setText("Name:");
        jPanel1.add(jLabel2, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 90, -1, -1));

        jLabel3.setText("D.O.B.:");
        jPanel1.add(jLabel3, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 140, -1, -1));

        jLabel4.setText("Phone No.:");
        jPanel1.add(jLabel4, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 180, -1, -1));

        jLabel5.setText("E-mail:");
        jPanel1.add(jLabel5, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 220, -1, -1));

        jLabel6.setText("Role:");
        jPanel1.add(jLabel6, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 270, -1, -1));

        jLabel7.setText("Salary:");
        jPanel1.add(jLabel7, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 310, -1, -1));

        jLabel8.setText("Join Date:");
        jPanel1.add(jLabel8, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 350, -1, -1));

        jLabel9.setText("GOVT ID:");
        jPanel1.add(jLabel9, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 390, -1, -1));
        jPanel1.add(jTextField1, new org.netbeans.lib.awtextra.AbsoluteConstraints(100, 90, 130, -1));
        jPanel1.add(jTextField2, new org.netbeans.lib.awtextra.AbsoluteConstraints(100, 140, 130, -1));
        jPanel1.add(jTextField3, new org.netbeans.lib.awtextra.AbsoluteConstraints(100, 180, 130, -1));
        jPanel1.add(jTextField4, new org.netbeans.lib.awtextra.AbsoluteConstraints(100, 220, 130, -1));
        jPanel1.add(jTextField5, new org.netbeans.lib.awtextra.AbsoluteConstraints(100, 310, 130, -1));

        jTextField6.setEditable(false);
        jPanel1.add(jTextField6, new org.netbeans.lib.awtextra.AbsoluteConstraints(100, 390, 130, -1));

        jComboBox1.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "--select--", "ADMIN", "CASHIER", "MANAGER" }));
        jPanel1.add(jComboBox1, new org.netbeans.lib.awtextra.AbsoluteConstraints(100, 270, 130, -1));

        jButton1.setText("UPDATE");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });
        jPanel1.add(jButton1, new org.netbeans.lib.awtextra.AbsoluteConstraints(500, 420, -1, -1));

        jButton2.setText("CLEAR");
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });
        jPanel1.add(jButton2, new org.netbeans.lib.awtextra.AbsoluteConstraints(720, 420, -1, -1));

        jButton3.setText("DELETE");
        jButton3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton3ActionPerformed(evt);
            }
        });
        jPanel1.add(jButton3, new org.netbeans.lib.awtextra.AbsoluteConstraints(610, 420, -1, -1));

        jButton4.setText("BROWSE");
        jButton4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton4ActionPerformed(evt);
            }
        });
        jPanel1.add(jButton4, new org.netbeans.lib.awtextra.AbsoluteConstraints(100, 420, 80, -1));

        jTable2.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "STAFF ID", "NAME", "D.O.B.", "PHONE NO.", "E-MAIL", "ROLE", "SALARY", "JOIN DATE", "GOVT. ID."
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false, false, false, false, false, false, false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        jScrollPane2.setViewportView(jTable2);

        jPanel1.add(jScrollPane2, new org.netbeans.lib.awtextra.AbsoluteConstraints(250, 50, 640, 350));

        jButton5.setText("ADD");
        jButton5.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton5ActionPerformed(evt);
            }
        });
        jPanel1.add(jButton5, new org.netbeans.lib.awtextra.AbsoluteConstraints(380, 420, -1, -1));
        jPanel1.add(jTextField9, new org.netbeans.lib.awtextra.AbsoluteConstraints(100, 350, 130, -1));

        getContentPane().add(jPanel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 900, 460));

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jButton5ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton5ActionPerformed
        // TODO add your handling code here:
        addStaff();
    }//GEN-LAST:event_jButton5ActionPerformed

    private void jButton4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton4ActionPerformed
        // TODO add your handling code here:
        try {
        // Open file chooser
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Select Government ID PDF");
        fileChooser.setFileFilter(new FileNameExtensionFilter("PDF Documents", "pdf"));

        int result = fileChooser.showOpenDialog(this);

        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();

            // Fixed destination folder
            String destDir = "C:/GroceryApp/StaffDocs/";
            File dir = new File(destDir);
            if (!dir.exists()) {
                dir.mkdirs(); // create folder if not exists
            }

            // Destination file with timestamp to avoid overwrite
            String newFileName = System.currentTimeMillis() + "_" + selectedFile.getName();
            File destFile = new File(destDir + newFileName);

            // Copy file
            Files.copy(selectedFile.toPath(), destFile.toPath(), StandardCopyOption.REPLACE_EXISTING);

            // Save the path in textfield (hidden or readonly)
            jTextField6.setText(destFile.getAbsolutePath());

            JOptionPane.showMessageDialog(this, "File uploaded successfully!");
        }

    } catch (Exception e) {
        e.printStackTrace();
        JOptionPane.showMessageDialog(this, "Error uploading file: " + e.getMessage());
    }
    }//GEN-LAST:event_jButton4ActionPerformed

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
        // TODO add your handling code here:
        clearFields();
    }//GEN-LAST:event_jButton2ActionPerformed

    private void jButton3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton3ActionPerformed
        // TODO add your handling code here:
        try {
        int row = jTable2.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Please select a staff member to delete!");
            return;
        }

        String staffId = (String) jTable2.getValueAt(row, 0);

        int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to delete this staff?", "Confirm", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            Connection con = ConnectionProvider.getCon();
            String sql = "DELETE FROM staff WHERE staff_code=?";
            PreparedStatement pst = con.prepareStatement(sql);
            pst.setString(1, staffId);
            pst.executeUpdate();

            JOptionPane.showMessageDialog(this, "Staff deleted successfully!");
            loadStaffTable();
            clearFields();
        }
    } catch (Exception e) {
        e.printStackTrace();
        JOptionPane.showMessageDialog(this, "Error deleting staff: " + e.getMessage());
    }
    }//GEN-LAST:event_jButton3ActionPerformed

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        // TODO add your handling code here:
         updateStaff();
    }//GEN-LAST:event_jButton1ActionPerformed

    private void openGovtIdPdf(int row) {
    try {
        // Assuming "govt_id_path" is in column 8 (adjust index if different)
        String filePath = (String) jTable2.getValueAt(row, 8);

        if (filePath != null && !filePath.trim().isEmpty()) {
            File file = new File(filePath);
            if (file.exists()) {
                if (Desktop.isDesktopSupported()) {
                    Desktop.getDesktop().open(file);
                } else {
                    JOptionPane.showMessageDialog(this, "Desktop not supported on this system.");
                }
            } else {
                JOptionPane.showMessageDialog(this, "File not found: " + filePath);
            }
        } else {
            JOptionPane.showMessageDialog(this, "No Govt. ID file uploaded for this staff.");
        }
    } catch (Exception e) {
        JOptionPane.showMessageDialog(this, "Error opening file: " + e.getMessage());
        e.printStackTrace();
    }
}

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
            java.util.logging.Logger.getLogger(Staff.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(Staff.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(Staff.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(Staff.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new Staff().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton3;
    private javax.swing.JButton jButton4;
    private javax.swing.JButton jButton5;
    private javax.swing.JComboBox<String> jComboBox1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JTable jTable2;
    private javax.swing.JTextField jTextField1;
    private javax.swing.JTextField jTextField2;
    private javax.swing.JTextField jTextField3;
    private javax.swing.JTextField jTextField4;
    private javax.swing.JTextField jTextField5;
    private javax.swing.JTextField jTextField6;
    private javax.swing.JTextField jTextField9;
    // End of variables declaration//GEN-END:variables
}
