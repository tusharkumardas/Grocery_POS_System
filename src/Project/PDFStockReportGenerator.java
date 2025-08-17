package Project;

/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
import org.apache.pdfbox.pdmodel.*;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import java.awt.Desktop;
import java.io.File;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.swing.*;
/**
 *
 * @author Tushar Kumar Das
 */

public class PDFStockReportGenerator {

    public void generateStockReportPDF() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Save PDF Stock Report");
        fileChooser.setSelectedFile(new File("Stock_Report.pdf"));
        int userSelection = fileChooser.showSaveDialog(null);

        if (userSelection != JFileChooser.APPROVE_OPTION) {
            return;
        }

        File fileToSave = fileChooser.getSelectedFile();

        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);

            PDPageContentStream content = new PDPageContentStream(document, page);
            content.setFont(PDType1Font.HELVETICA_BOLD, 14);

            // Shop Name
            content.beginText();
            content.newLineAtOffset(200, 790);
            content.showText("TUSHAR VARIETY STORE");
            content.endText();

            // Date & Time
            String timeStamp = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss").format(new Date());
            content.beginText();
            content.setFont(PDType1Font.HELVETICA, 10);
            content.newLineAtOffset(400, 770);
            content.showText("Date: " + timeStamp);
            content.endText();

            // Table Headers
            float margin = 30;
            float yStart = 750;
            float tableWidth = page.getMediaBox().getWidth() - 2 * margin;
            float rowHeight = 20;
            float tableTopY = yStart - 40;

            String[] headers = {
                "Item Code", "Product Name", "Qty", "Purchase", "Sale", "MRP", "Exp Date", "Brand", "Barcode", "GST","Stock Alert At"
            };

            float[] colWidths = { 60, 80, 30, 50, 50, 40, 60, 60, 60, 30, 60};
            float nextY = tableTopY;

            // Draw table header row
            content.setFont(PDType1Font.HELVETICA_BOLD, 8);
            float textX = margin;
            float textY = nextY - 12;

            for (int i = 0; i < headers.length; i++) {
              content.beginText();
              content.newLineAtOffset(textX, textY);
              content.showText(headers[i]);
              content.endText();
              textX += colWidths[i];
            }

            // Draw top horizontal line above headers
           float y = nextY;
           content.setStrokingColor(0, 0, 0); // black
           content.moveTo(margin, y);
           content.lineTo(margin + tableWidth, y);
           content.stroke();

           // Draw horizontal line below header row
           y -= rowHeight;
           content.moveTo(margin, y);
           content.lineTo(margin + tableWidth, y);
           content.stroke();


            // Fetch data from DB and draw rows
            java.sql.Connection con = Project.ConnectionProvider.getCon();
            Statement stmt = con.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM products");

            content.setFont(PDType1Font.HELVETICA, 8);
            while (rs.next()) {
                if (y < 50 + rowHeight) break; // Prevent overflow
                y -= rowHeight;
            

                textX = margin;
                textY = y + 5;

                String[] row = {
                    rs.getString("item_code"),
                    rs.getString("product_name"),
                    String.valueOf(rs.getInt("qty")),
                    String.valueOf(rs.getDouble("purchase_price")),
                    String.valueOf(rs.getDouble("sale_price")),
                    String.valueOf(rs.getDouble("mrp")),
                    rs.getString("exp_date"),
                    rs.getString("brand_name"),
                    rs.getString("barcode"),
                    String.valueOf(rs.getDouble("gst")),
                    String.valueOf(rs.getInt("stock_alert"))
                };

                for (int i = 0; i < row.length; i++) {
                    content.beginText();
                    content.newLineAtOffset(textX, textY);
                    content.showText(row[i]);
                    content.endText();
                    textX += colWidths[i];
                }

                // Draw horizontal line for the row
                content.moveTo(margin, y);
                content.lineTo(margin + tableWidth, y);
                content.stroke();
            }

            // Final bottom line
            content.moveTo(margin, y - rowHeight);
            content.lineTo(margin + tableWidth, y - rowHeight);
            content.stroke();

            // Draw vertical lines
            float x = margin;
            for (float colWidth : colWidths) {
                content.moveTo(x, tableTopY);
                content.lineTo(x, y - rowHeight);
                content.stroke();
                x += colWidth;
            }
            // Last vertical line
            content.moveTo(margin + tableWidth, tableTopY);
            content.lineTo(margin + tableWidth, y - rowHeight);
            content.stroke();

            content.close();
            document.save(fileToSave);
            Desktop.getDesktop().open(fileToSave);

            JOptionPane.showMessageDialog(null, "Stock Report saved successfully!");

        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(null, "Error creating PDF: " + ex.getMessage());
        }
    }
}
