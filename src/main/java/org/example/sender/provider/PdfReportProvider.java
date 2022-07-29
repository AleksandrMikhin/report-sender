package org.example.sender.provider;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Font;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import org.example.sender.entity.Team;
import org.example.sender.entity.SingleReport;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.stream.IntStream;

@Service
public class PdfReportProvider implements ReportProvider {

    private static final String DATE_FORMAT_PATTERN = "yyyy_MM_dd";

    private final Font teamFont;
    private final Font headerFont;
    private final Font mainFont;

    public PdfReportProvider(@Value("${sender.base-font}") final String fontPath) {
        final BaseFont baseFont;
        try {
            baseFont = BaseFont.createFont(fontPath, BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
        } catch (IOException | DocumentException e) {
            throw new RuntimeException("Base font not found", e);
        }
        teamFont = new Font(baseFont, 12, Font.BOLD, BaseColor.BLACK);
        headerFont = new Font(baseFont, 12, Font.NORMAL, BaseColor.BLACK);
        mainFont = new Font(baseFont, 10, Font.NORMAL, BaseColor.BLACK);
    }

    @Override
    public File createReport(final List<Team> teams, final Date reportDate) throws DocumentException, IOException {
        final DateFormat formatter = new SimpleDateFormat(DATE_FORMAT_PATTERN);
        final File tempReportFile = File.createTempFile(formatter.format(reportDate) + "_daily_report", ".pdf");

        final Document document = new Document();
        PdfWriter.getInstance(document, new FileOutputStream(tempReportFile));

        document.open();

        final Paragraph headerParagraph = new Paragraph();
        headerParagraph.add(new Paragraph("Daily generated: " + reportDate, headerFont));
        addEmptyLine(headerParagraph, 1);
        document.add(headerParagraph);

        if (teams.isEmpty()) {
            document.add(new Paragraph("Something went wrong: accountant service's given back an empty response.",
                    mainFont));
        } else {
            for (final Team team : teams) {
                final Paragraph teamParagraph = new Paragraph();
                teamParagraph.add(new Paragraph("Team: " + team.getColor(), teamFont));
                addEmptyLine(teamParagraph, 1);
                document.add(teamParagraph);
                document.add(createTeamTable(team));
            }
        }
        document.close();
        return tempReportFile;
    }

    private PdfPTable createTeamTable(final Team team) throws DocumentException {
        final PdfPTable table = new PdfPTable(2);
        table.addCell(getHeaderCell("Student"));

        final PdfPTable tableActivityHeader = new PdfPTable(2);
        tableActivityHeader.setWidths(new int[]{80, 20});
        tableActivityHeader.addCell(getHeaderCell("Activity"));
        tableActivityHeader.addCell(getHeaderCell("time:"));
        table.addCell(new PdfPCell(tableActivityHeader));

        for (final SingleReport singleReport : team.getSingleReports()) {
            table.addCell(new Paragraph(singleReport.getLastName() + " " + singleReport.getFirstName(), mainFont));
            table.addCell(getTasksCell(singleReport));
        }
        return table;
    }

    private PdfPCell getHeaderCell(final String description) {
        final PdfPCell spentTimeCell = new PdfPCell(new Paragraph(description, headerFont));
        spentTimeCell.setHorizontalAlignment(PdfPCell.ALIGN_CENTER);
        spentTimeCell.setBackgroundColor(BaseColor.LIGHT_GRAY);
        spentTimeCell.setBorderWidth(2);
        return spentTimeCell;
    }

    private PdfPCell getTasksCell(final SingleReport singleReport) throws DocumentException {
        final PdfPTable table = new PdfPTable(2);
        table.setWidths(new int[]{80, 20});
        singleReport.getTasks()
                .forEach(task -> {
                    table.addCell(new Paragraph(task.getText(), mainFont));

                    final PdfPCell spentTimeCell = new PdfPCell(
                            new Paragraph(Double.toString(task.getTimeSpent()), mainFont));
                    spentTimeCell.setHorizontalAlignment(PdfPCell.ALIGN_RIGHT);
                    spentTimeCell.setBorder(3);
                    table.addCell(spentTimeCell);
                });
        return new PdfPCell(table);
    }

    private void addEmptyLine(final Paragraph paragraph, final int number) {
        IntStream.range(0, number).forEach((t) -> paragraph.add(new Paragraph(" ")));
    }
}
