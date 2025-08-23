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
public class BillingCalculator {
        // Reference to your GUI's jTable1
    public static JTable jTable1;

    public static double calculateTotalAmount() {
        double total = 0;
        if (jTable1 == null) return total;

        for (int i = 0; i < jTable1.getRowCount(); i++) {
            int qty = Integer.parseInt(jTable1.getValueAt(i, 3).toString());
            double price = Double.parseDouble(jTable1.getValueAt(i, 4).toString());
            total += qty * price;
        }
        return total;
    }

    public static double calculateTotalGST() {
        double totalGst = 0;
        if (jTable1 == null) return totalGst;

        for (int i = 0; i < jTable1.getRowCount(); i++) {
            int qty = Integer.parseInt(jTable1.getValueAt(i, 3).toString());
            double price = Double.parseDouble(jTable1.getValueAt(i, 4).toString());
            double gstPercent = Double.parseDouble(jTable1.getValueAt(i, 5).toString());

            double itemAmount = qty * price;
            double gstAmount = (itemAmount * gstPercent) / 100.0;
            totalGst += gstAmount;
        }
        return totalGst;
    }

    public static double calculateNetAmount() {
        return calculateTotalAmount() + calculateTotalGST();
    }
}
