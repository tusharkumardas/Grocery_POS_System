/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Project;
import org.apache.pdfbox.pdmodel.*;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.PDPage;
import javax.swing.*;
import javax.swing.table.TableModel;
import java.awt.Desktop;
import java.io.File;
/**
 *
 * @author Tushar Kumar Das
 */
public class PurchasePDFExporter {
      public static void exportTableToPDF(JTable table) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Save PDF Report");
        fileChooser.setSelectedFile(new File("purchase_report.pdf"));

        int userSelection = fileChooser.showSaveDialog(null);
        if (userSelection != JFileChooser.APPROVE_OPTION) {
            return; // user canceled
        }

        File file = fileChooser.getSelectedFile();

        try (PDDocument document = new PDDocument()) {
            // ✅ Safe landscape page (works in all PDFBox versions)
            PDRectangle landscape = new PDRectangle(PDRectangle.A4.getHeight(), PDRectangle.A4.getWidth());
            PDPage page = new PDPage(landscape);
            document.addPage(page);

            PDPageContentStream content = new PDPageContentStream(document, page);

            // Title
            content.beginText();
            content.setFont(PDType1Font.HELVETICA_BOLD, 18);
            content.newLineAtOffset(280, landscape.getHeight() - 80);
            content.showText("Purchase Report");
            content.endText();

            // Table start position
            float margin = 40;
            float yStart = landscape.getHeight() - 120;
            float tableWidth = landscape.getWidth() - 2 * margin;
            float yPosition = yStart;

            TableModel model = table.getModel();
            int cols = model.getColumnCount();

            // Dynamic column width
            float rowHeight = 20;
            float colWidth = tableWidth / cols;

            // Draw column headers
            float textx = margin + 2;
            float texty = yPosition - 15;

            content.setFont(PDType1Font.HELVETICA_BOLD, 10);
            for (int i = 0; i < cols; i++) {
                content.beginText();
                content.newLineAtOffset(textx, texty);
                content.showText(model.getColumnName(i));
                content.endText();
                textx += colWidth;
            }

            yPosition -= rowHeight;

            // Draw table rows
            content.setFont(PDType1Font.HELVETICA, 9);
            for (int row = 0; row < model.getRowCount(); row++) {
                textx = margin + 2;
                texty = yPosition - 15;

                for (int col = 0; col < cols; col++) {
                    Object value = model.getValueAt(row, col);
                    String text = value != null ? value.toString() : "";
                    content.beginText();
                    content.newLineAtOffset(textx, texty);
                    content.showText(text);
                    content.endText();
                    textx += colWidth;
                }

                yPosition -= rowHeight;

                // If page overflows → new page
                if (yPosition < 50) {
                    content.close();
                    page = new PDPage(landscape);
                    document.addPage(page);
                    content = new PDPageContentStream(document, page);
                    yPosition = yStart;
                }
            }

            content.close();
            document.save(file);

            JOptionPane.showMessageDialog(null, "PDF saved: " + file.getAbsolutePath());

            // Auto open PDF
            if (Desktop.isDesktopSupported()) {
                Desktop.getDesktop().open(file);
            }

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Error exporting PDF: " + e.getMessage());
        }
    }
}
