package com.chrisworks.personal.inventorysystem.Backend.Utility;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

import java.io.ByteArrayOutputStream;

/**
 * @author Chris_Eteka
 * @since 1/3/2020
 * @email chriseteka@gmail.com
 */
public class GeneratePDFReport {

    public static byte[] generatePDFReport(PDFMap pdfMap) {

        Document document = new Document();
        ByteArrayOutputStream output = new ByteArrayOutputStream();

        try {

            PdfPTable table = new PdfPTable(pdfMap.getTableHead().size());
            table.setWidthPercentage(100);

            Font headFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD);

            PdfPCell headerCell;

            for (String columnName : pdfMap.getTableHead()) {

                headerCell = new PdfPCell(new Phrase(columnName, headFont));
                headerCell.setHorizontalAlignment(Element.ALIGN_CENTER);
                table.addCell(headerCell);
            }

            Gson gson = new Gson();

            for (Object object : pdfMap.getTableData()) {

                PdfPCell cell;
                JsonObject data = gson.toJsonTree(object).getAsJsonObject();

                for (String columnName : pdfMap.getTableHead()) {

                    cell = new PdfPCell(new Phrase(String.valueOf(data.get(columnName))
                            .replace("\"", "")));
                    cell.setVerticalAlignment(Element.ALIGN_LEFT);
                    cell.setHorizontalAlignment(Element.ALIGN_LEFT);
                    table.addCell(cell);
                }
            }

            PdfWriter.getInstance(document, output);
            document.addTitle(pdfMap.getTitle());
            document.addAuthor("kris_3t3ka");
            document.open();
            document.add(table);

            document.close();

        } catch (DocumentException ex) {

            ex.printStackTrace();
        }

        return output.toByteArray();
    }

}
