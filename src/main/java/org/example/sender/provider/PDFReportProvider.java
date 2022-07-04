package org.example.sender.provider;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Font;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import org.example.sender.entity.Team;
import org.example.sender.entity.User;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class PDFReportProvider implements ReportProvider{

    private static final String DATE_FORMAT_PATTERN = "yyyy_MM_dd";
    private static final Font headerFont = FontFactory.getFont(FontFactory.TIMES_BOLD, 16, BaseColor.BLACK);
    private static final Font teamFont = FontFactory.getFont(FontFactory.COURIER_BOLD, 12, BaseColor.BLACK);

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

        for (final Team team : teams) {
            final Paragraph teamParagraph = new Paragraph();
            teamParagraph.add(new Paragraph("Team: " + team.getColor(), teamFont));
            addEmptyLine(teamParagraph, 1);
            document.add(teamParagraph);
            document.add(createTeamTable(team));
        }
        document.close();
        return tempReportFile;
    }

    private PdfPTable createTeamTable(final Team team) {
        final PdfPTable table = new PdfPTable(2);
        Stream.of("User name", "Activity")
                .forEach(columnTitle -> {
                    final PdfPCell header = new PdfPCell();
                    header.setBackgroundColor(BaseColor.LIGHT_GRAY);
                    header.setBorderWidth(2);
                    header.setPhrase(new Phrase(columnTitle));
                    table.addCell(header);
                });

        for (final User user : team.getUsers()) {
            table.addCell(user.getFirstName() + " " + user.getLastName());
            table.addCell(getTasksCell(user));
        }
        return table;
    }

    private PdfPCell getTasksCell(final User user) {
        final PdfPTable table = new PdfPTable(1);
        user.getTasks().forEach(table::addCell);
        return new PdfPCell(table);
    }

    private void addEmptyLine(final Paragraph paragraph, final int number) {
        IntStream.range(0, number).forEach((t) -> paragraph.add(new Paragraph(" ")));
    }
}
