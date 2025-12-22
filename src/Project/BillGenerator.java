/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Project;
import javax.swing.JTable;
import Project.ConnectionProvider;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
/**
 *
 * @author Tushar Kumar Das
 */
public class BillGenerator {
public static String generateBill(
            JTable table,
            double subTotal,
            double gstAmount,
            double discountAmount,
            double finalTotal) {

        StringBuilder sb = new StringBuilder();

        // ================== COMPANY DETAILS ==================
        String companyName = "";
        String gstNo = "";
        String contactNo = "";
        String address = "";

        try {
            Connection con = ConnectionProvider.getCon();
            PreparedStatement ps = con.prepareStatement(
                "SELECT company_name, gst_no, contact_no, address FROM company_settings LIMIT 1"
            );
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                companyName = rs.getString("company_name");
                gstNo = rs.getString("gst_no");
                contactNo = rs.getString("contact_no");
                address = rs.getString("address");
            }
            con.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        // ================== BILL HEADER ==================
        sb.append("=====================================\n");
        sb.append(centerText(companyName)).append("\n");
        sb.append(centerText(address)).append("\n");
        sb.append(centerText("GST No: " + gstNo)).append("\n");
        sb.append(centerText("Contact: " + contactNo)).append("\n");
        sb.append("============================================\n");

        // ❌ REMOVED TOTAL COLUMN
        sb.append(String.format("%-24s %6s %10s\n",
                "Item", "Qty", "Price"));
        sb.append("--------------------------------------------\n");

        // ================== PRODUCTS ==================
        for (int i = 0; i < table.getRowCount(); i++) {

            String name = table.getValueAt(i, 2).toString();
            int qty = Integer.parseInt(table.getValueAt(i, 3).toString());
            double price = Double.parseDouble(table.getValueAt(i, 4).toString());

            sb.append(String.format("%-24s %6d %10.2f\n",
                    name, qty, price));
        }

        // ================== BILL SUMMARY ==================
        sb.append("--------------------------------------------\n");
        sb.append(String.format("%-30s %10.2f\n", "Sub Total:", subTotal));
        sb.append(String.format("%-30s %10.2f\n", "GST:", gstAmount));
        sb.append(String.format("%-30s %10.2f\n", "Discount:", discountAmount));
        sb.append("============================================\n");
        sb.append(String.format("%-30s %10.2f\n", "TOTAL:", finalTotal));
        sb.append("============================================\n");
        sb.append("        Thank you! Visit Again\n");
        sb.append("============================================\n");

        return sb.toString();
    }

    // ================== CENTER ALIGN TEXT ==================
    private static String centerText(String text) {
        int width = 44;
        if (text == null) text = "";
        int padding = (width - text.length()) / 2;
        return " ".repeat(Math.max(0, padding)) + text;
    }
}
