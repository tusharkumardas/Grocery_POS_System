/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Project;
import javax.swing.JTable;
/**
 *
 * @author Tushar Kumar Das
 */
public class BillGenerator {
    public static String generateBill(JTable jTable1) {
 StringBuilder sb = new StringBuilder();

        // Bill Header
        sb.append("============================================\n");
        sb.append("           TUSHAR VARIETY STORE\n");
        sb.append("      MAIN ROAD KUCHAI, NEAR RIDDHI SIDDHI HOTEL\n");
        sb.append("             Contact: 1234567890\n");
        sb.append("============================================\n");
        sb.append(String.format("%-20s %5s %8s %10s\n", "Item", "Qty", "Price", "Total"));
        sb.append("--------------------------------------------\n");

        double grandTotal = 0.0;

        // Loop through cartTable
        for (int i = 0; i < jTable1.getRowCount(); i++) {
            String name = jTable1.getValueAt(i, 1).toString();   // item name
            int qty = Integer.parseInt(jTable1.getValueAt(i, 2).toString()); // qty
            double price = Double.parseDouble(jTable1.getValueAt(i, 3).toString()); // price per unit

            double total = qty * price;
            grandTotal += total;

            // Properly aligned row
            sb.append(String.format("%-20s %5d %8.2f %10.2f\n", name, qty, price, total));
        }

        sb.append("--------------------------------------------\n");
        sb.append(String.format("%-30s %10.2f\n", "Subtotal:", grandTotal));

        // Example tax & discount
        double tax = grandTotal * 0.05;      // 5% GST
        double discount = grandTotal * 0.10; // 10% discount
        double finalTotal = grandTotal + tax - discount;

        sb.append(String.format("%-30s %10.2f\n", "GST (5%):", tax));
        sb.append(String.format("%-30s %10.2f\n", "Discount (10%):", discount));
        sb.append("============================================\n");
        sb.append(String.format("%-30s %10.2f\n", "TOTAL:", finalTotal));
        sb.append("============================================\n");
        sb.append("         Thank you, Visit Again!\n");
        sb.append("============================================\n");

        return sb.toString();
    }
    
}
