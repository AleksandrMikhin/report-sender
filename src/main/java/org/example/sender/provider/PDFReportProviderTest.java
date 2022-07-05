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
import org.example.sender.entity.SingleReport;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class PDFReportProviderTest implements ReportProvider{

    static final SingleReport SINGLE_REPORT_1 = new SingleReport("firstName1", "lastName1", List.of("task1", "task2", "task3"));
    static final SingleReport SINGLE_REPORT_2 = new SingleReport("firstName2", "lastName2", List.of("task4", "task5", "task6"));
    static final SingleReport SINGLE_REPORT_3 = new SingleReport("firstName3", "lastName3", List.of("task7", "task8", "task9"));
    static final Team team1 = new Team("team1", List.of(SINGLE_REPORT_1, SINGLE_REPORT_2, SINGLE_REPORT_3));
    static final Team team2 = new Team("team2", List.of(SINGLE_REPORT_1, SINGLE_REPORT_2, SINGLE_REPORT_3));
    static final Team team3 = new Team("team3", List.of(SINGLE_REPORT_1, SINGLE_REPORT_2, SINGLE_REPORT_3));
    static final List<Team> teams = List.of(team1, team2, team3);

    public static void main(String[] args) {
        try {
            new PDFReportProviderTest().createReport(teams, new Date());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    final static Font headerFont = FontFactory.getFont(FontFactory.TIMES_BOLD, 16, BaseColor.BLACK);
    final static Font teamFont = FontFactory.getFont(FontFactory.COURIER_BOLD, 12, BaseColor.BLACK);

    @Override
    public File createReport(final List<Team> teams, final Date reportDate) throws DocumentException, IOException {
        final Document document = new Document();

//        final File tempFile = File.createTempFile("report", ".pdf");
        final File tempFile = new File("/home/mansur/IdeaProjects/report-sender/files/report.pdf");
        PdfWriter.getInstance(document, new FileOutputStream(tempFile));

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
        return tempFile;
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

        for (final SingleReport singleReport : team.getSingleReports()) {
            table.addCell(singleReport.getFirstName() + " " + singleReport.getLastName());
            table.addCell(getTasksCell(singleReport));
        }
        return table;
    }

    private PdfPCell getTasksCell(final SingleReport singleReport) {
        final PdfPTable table = new PdfPTable(1);
        singleReport.getTasks().forEach(table::addCell);
        return new PdfPCell(table);
    }

    private void addEmptyLine(final Paragraph paragraph, final int number) {
        IntStream.range(0, number).forEach((t) -> paragraph.add(new Paragraph(" ")));
    }
}
