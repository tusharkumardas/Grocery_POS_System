/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Project;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.table.TableModel;
import java.awt.Desktop;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.swing.*;
/**
 *
 * @author Tushar Kumar Das
 */
public class ExpensePDFExporter {
    public static void exportToPDF(JTable expenseTable) {
        PDDocument document = new PDDocument();

        try {
            PDPage page = new PDPage();
            document.addPage(page);

            PDPageContentStream contentStream = new PDPageContentStream(document, page);

            // Title
            contentStream.beginText();
            contentStream.setFont(PDType1Font.HELVETICA_BOLD, 18);
            contentStream.newLineAtOffset(220, 750);
            contentStream.showText("Expense Report");
            contentStream.endText();

            // Date
            contentStream.beginText();
            contentStream.setFont(PDType1Font.HELVETICA, 10);
            contentStream.newLineAtOffset(50, 730);
            contentStream.showText("Generated on: " + new Date().toString());
            contentStream.endText();

            // Table start
            float margin = 50;
            float yStart = 700;
            float y = yStart;
            float rowHeight = 20;
            float tableWidth = page.getMediaBox().getWidth() - 2 * margin;
            int cols = expenseTable.getColumnCount();
            float colWidth = tableWidth / cols;

            TableModel model = expenseTable.getModel();

            // Headers
            contentStream.setFont(PDType1Font.HELVETICA_BOLD, 10);
            float x = margin;
            for (int i = 0; i < cols; i++) {
                contentStream.beginText();
                contentStream.newLineAtOffset(x + 2, y);
                contentStream.showText(model.getColumnName(i));
                contentStream.endText();
                x += colWidth;
            }
            y -= rowHeight;

            // Rows
            contentStream.setFont(PDType1Font.HELVETICA, 9);
            for (int row = 0; row < model.getRowCount(); row++) {
                x = margin;
                for (int col = 0; col < cols; col++) {
                    String text = String.valueOf(model.getValueAt(row, col));
                    contentStream.beginText();
                    contentStream.newLineAtOffset(x + 2, y);
                    contentStream.showText(text);
                    contentStream.endText();
                    x += colWidth;
                }
                y -= rowHeight;

                // new page if needed
                if (y < 50) {
                    contentStream.close();
                    page = new PDPage();
                    document.addPage(page);
                    contentStream = new PDPageContentStream(document, page);
                    y = yStart;
                }
            }

            contentStream.close();

            // Ask user where to save
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("Save Expense Report as PDF");
            fileChooser.setSelectedFile(new File("Expense_Report_" +
                    new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date()) + ".pdf"));

            int userSelection = fileChooser.showSaveDialog(null);

            if (userSelection == JFileChooser.APPROVE_OPTION) {
                File fileToSave = fileChooser.getSelectedFile();
                document.save(fileToSave);

                JOptionPane.showMessageDialog(null,
                        "Expense report exported successfully!\nSaved at: " + fileToSave.getAbsolutePath());

                // Auto open
                if (Desktop.isDesktopSupported()) {
                    Desktop.getDesktop().open(fileToSave);
                }
            }

            document.close();

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Error exporting PDF: " + e.getMessage());
        }
    }
}
