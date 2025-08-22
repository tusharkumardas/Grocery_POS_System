/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package IDgenerator;
import Project.ConnectionProvider;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.ResultSet;
/**
 *
 * @author Tushar Kumar Das
 */
public class SalesIDGenerator {
  public static String generateInvoiceNo() {
        String prefix = "INV";   // You can make it configurable
        String year = String.valueOf(java.time.Year.now().getValue());
        int nextNumber = 1;

        String sql = "SELECT invoice_no FROM sales " +
                     "WHERE YEAR(created_at) = YEAR(CURDATE()) " +
                     "ORDER BY id DESC LIMIT 1";

        // Get connection
        try (Connection con = ConnectionProvider.getCon();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            if (rs.next()) {
                String lastID = rs.getString("invoice_no");
                String[] parts = lastID.split("-");
                if (parts.length == 3) {
                    nextNumber = Integer.parseInt(parts[2]) + 1;
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return prefix + "-" + year + "-" + String.format("%05d", nextNumber);
    }
}
