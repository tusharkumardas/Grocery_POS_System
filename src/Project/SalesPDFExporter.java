/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Project;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
/**
 *
 * @author Tushar Kumar Das
 */
public class SalesPDFExporter {
    public static void exportTable(JTable table, Component parent) {
        try {
            DefaultTableModel model = (DefaultTableModel) table.getModel();
            if (model.getRowCount() == 0) {
                JOptionPane.showMessageDialog(parent, "No data to export!");
                return;
            }

            // File chooser
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("Save PDF");
            fileChooser.setSelectedFile(new File("Sales_Report.pdf"));
            int userSelection = fileChooser.showSaveDialog(parent);

            if (userSelection != JFileChooser.APPROVE_OPTION) {
                return; // user canceled
            }

            File file = fileChooser.getSelectedFile();

            // Create PDF
            PDDocument document = new PDDocument();
            PDPage page = new PDPage();
            document.addPage(page);

            PDPageContentStream contentStream = new PDPageContentStream(document, page);
            PDType1Font font = PDType1Font.HELVETICA;

            int margin = 40;
            int y = 750;  // Starting Y position
            int rowHeight = 20;
            int tableWidth = (int) page.getMediaBox().getWidth() - 2 * margin;
            int colCount = model.getColumnCount();
            int colWidth = tableWidth / colCount;

            // Title
            contentStream.beginText();
            contentStream.setFont(PDType1Font.HELVETICA_BOLD, 16);
            contentStream.newLineAtOffset(margin, y);
            contentStream.showText("Sales Report");
            contentStream.endText();
            y -= 40;

            // Draw Table Headers
            contentStream.setFont(PDType1Font.HELVETICA_BOLD, 10);
            for (int col = 0; col < colCount; col++) {
                drawCell(contentStream, margin + col * colWidth, y, colWidth, rowHeight,
                        model.getColumnName(col), true);
            }
            y -= rowHeight;

            // Draw Rows
            contentStream.setFont(font, 9);
            for (int row = 0; row < model.getRowCount(); row++) {
                for (int col = 0; col < colCount; col++) {
                    Object val = model.getValueAt(row, col);
                    drawCell(contentStream, margin + col * colWidth, y, colWidth, rowHeight,
                            (val != null ? val.toString() : ""), false);
                }
                y -= rowHeight;

                // Page break if necessary
                if (y < 50) {
                    contentStream.close();
                    page = new PDPage();
                    document.addPage(page);
                    contentStream = new PDPageContentStream(document, page);
                    y = 750;
                }
            }

            contentStream.close();
            document.save(file);
            document.close();

            JOptionPane.showMessageDialog(parent, "PDF exported successfully:\n" + file.getAbsolutePath());

            // Auto open
            if (Desktop.isDesktopSupported()) {
                Desktop.getDesktop().open(file);
            }

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(parent, "Error exporting PDF: " + e.getMessage());
        }
    }

    // Helper method to draw a table cell
    private static void drawCell(PDPageContentStream contentStream,
                                 int x, int y, int width, int height,
                                 String text, boolean isHeader) throws Exception {
        // Draw rectangle border
        contentStream.addRect(x, y, width, height);
        contentStream.stroke();

        // Add text inside
        contentStream.beginText();
        contentStream.setFont(isHeader ? PDType1Font.HELVETICA_BOLD : PDType1Font.HELVETICA, 9);
        contentStream.newLineAtOffset(x + 2, y + 5); // padding
        contentStream.showText(text.length() > 20 ? text.substring(0, 20) + "..." : text);
        contentStream.endText();
    }
}
