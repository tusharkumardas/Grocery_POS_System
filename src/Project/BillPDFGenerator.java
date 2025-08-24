/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Project;
import org.apache.pdfbox.pdmodel.*;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import javax.swing.JTable;
import java.io.File;
import java.io.IOException;

/**
 *
 * @author Tushar Kumar Das
 */
public class BillPDFGenerator {
    public static void generatePDF(JTable table,
                                   String customerName,
                                   String customerPhone,
                                   String invoiceNo,
                                   double totalAmount,
                                   double gstAmount,
                                   double netAmount,
                                   double amountPaid,
                                   double amountDue,
                                   String paymentMode,
                                   String pdfPath) {

        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);

            PDPageContentStream content = new PDPageContentStream(document, page);

            float y = 750; // start from top
            float margin = 50;

            // 🏬 Shop Header
            content.beginText();
            content.setFont(PDType1Font.HELVETICA_BOLD, 18);
            content.newLineAtOffset(margin, y);
            content.showText("My Shop Billing System");
            content.endText();

            y -= 30;

            // Invoice Info
            y = writeLine(content, "Invoice No: " + invoiceNo, margin, y);
            y = writeLine(content, "Customer: " + customerName + " (" + customerPhone + ")", margin, y);
            y = writeLine(content, "Payment Mode: " + paymentMode, margin, y);
            y = writeLine(content, "Date: " + new java.util.Date(), margin, y);

            y -= 20;

            // 📦 Table Header
            String[] headers = {"Product ID", "Product Name", "Qty", "Price", "GST%", "Total"};
            content.setFont(PDType1Font.HELVETICA_BOLD, 10);
            float x = margin;
            for (String h : headers) {
                content.beginText();
                content.newLineAtOffset(x, y);
                content.showText(h);
                content.endText();
                x += 80; // column spacing
            }
            y -= 15;

            // 📦 Table Data
            content.setFont(PDType1Font.HELVETICA, 10);
            for (int i = 0; i < table.getRowCount(); i++) {
                x = margin;
                for (int j = 0; j < headers.length; j++) {
                    String cellValue = (table.getValueAt(i, j) != null) ? table.getValueAt(i, j).toString() : "";
                    content.beginText();
                    content.newLineAtOffset(x, y);
                    content.showText(cellValue);
                    content.endText();
                    x += 80;
                }
                y -= 15;
            }

            y -= 20;

            // 💰 Summary
            y = writeLine(content, "Total Amount (before GST): " + String.format("%.2f", totalAmount), margin, y);
            y = writeLine(content, "GST Amount: " + String.format("%.2f", gstAmount), margin, y);
            y = writeLine(content, "Net Amount: " + String.format("%.2f", netAmount), margin, y);
            y = writeLine(content, "Amount Paid: " + String.format("%.2f", amountPaid), margin, y);
            y = writeLine(content, "Amount Due: " + String.format("%.2f", amountDue), margin, y);

            y -= 30;

            // Footer
            content.beginText();
            content.setFont(PDType1Font.HELVETICA_OBLIQUE, 12);
            content.newLineAtOffset(margin, y);
            content.showText("Thank you for shopping with us!");
            content.endText();

            content.close();

            // Save to file
            document.save(new File(pdfPath));

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Helper method to write lines neatly
    private static float writeLine(PDPageContentStream content, String text, float x, float y) throws IOException {
        content.beginText();
        content.setFont(PDType1Font.HELVETICA, 12);
        content.newLineAtOffset(x, y);
        content.showText(text);
        content.endText();
        return y - 15;
    }
}
