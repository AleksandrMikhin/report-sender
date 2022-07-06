package org.example.sender.provider;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Font;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import org.example.sender.entity.Team;
import org.example.sender.entity.SingleReport;
import org.example.sender.entity.TrackMinInfo;
import org.example.sender.utils.PropertiesUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class PDFReportProviderTest implements ReportProvider{

    private static final TrackMinInfo TRACK_1 = new TrackMinInfo("Задача1", 1);
    private static final TrackMinInfo TRACK_2 = new TrackMinInfo("Задача2", 2);
    private static final TrackMinInfo TRACK_3 = new TrackMinInfo("Задача3", 3);

    private static final SingleReport SINGLE_REPORT_1 = new SingleReport("Имя1", "Фамилия1",
            List.of(TRACK_1, TRACK_2, TRACK_3));
    private static final SingleReport SINGLE_REPORT_2 = new SingleReport("Имя2", "Фамилия2",
            List.of(TRACK_1, TRACK_2, TRACK_3));
    private static final SingleReport SINGLE_REPORT_3 = new SingleReport("Имя3", "Фамилия3",
            List.of(TRACK_1, TRACK_2, TRACK_3));
    private static final Team TEAM_1 = new Team("Команда_1",
            List.of(SINGLE_REPORT_1, SINGLE_REPORT_2, SINGLE_REPORT_3));
    private static final Team TEAM_2 = new Team("Команда_2",
            List.of(SINGLE_REPORT_1, SINGLE_REPORT_2, SINGLE_REPORT_3));
    private static final Team TEAM_3 = new Team("Команда_3",
            List.of(SINGLE_REPORT_1, SINGLE_REPORT_2, SINGLE_REPORT_3));
    private static final List<Team> teams = List.of(TEAM_1, TEAM_2, TEAM_3);;

    private static final BaseFont BASE_FONT;
    static {
        try {
            final String FONT_PATH = PropertiesUtils.getProperty("sender.base-font.path");
            BASE_FONT = BaseFont.createFont(FONT_PATH, BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
        } catch (IOException | DocumentException e) {
            throw new RuntimeException("Base font not found" , e);
        }
    }

    private static final Font HEADER_FONT = new Font(BASE_FONT, 16, Font.NORMAL, BaseColor.BLACK);
    private static final Font TEAM_FONT = new Font(BASE_FONT, 12, Font.BOLD, BaseColor.BLACK);
    private static final Font MAIN_FONT = new Font(BASE_FONT, 10, Font.NORMAL, BaseColor.BLACK);
    private static final String DATE_FORMAT_PATTERN = "yyyy_MM_dd";

    @Override
    public File createReport(final List<Team> teams1, final Date reportDate) throws DocumentException, IOException {
        final DateFormat formatter = new SimpleDateFormat(DATE_FORMAT_PATTERN);
//        final File tempReportFile = File.createTempFile(formatter.format(reportDate) + "_daily_report", ".pdf");
        final File tempReportFile = new File("c:\\!_Github\\report.pdf");

        final Document document = new Document();
        PdfWriter.getInstance(document, new FileOutputStream(tempReportFile));

        document.open();

        final Paragraph headerParagraph = new Paragraph();
        headerParagraph.add(new Paragraph("Daily generated: " + reportDate, HEADER_FONT));
        addEmptyLine(headerParagraph, 1);
        document.add(headerParagraph);

        for (final Team team : teams) {
            final Paragraph teamParagraph = new Paragraph();
            teamParagraph.add(new Paragraph("Team: " + team.getColor(), TEAM_FONT));
            addEmptyLine(teamParagraph, 1);
            document.add(teamParagraph);
            document.add(createTeamTable(team));
        }
        document.close();
        return tempReportFile;
    }

    private PdfPTable createTeamTable(final Team team) throws DocumentException {
        final PdfPTable table = new PdfPTable(2);
        Stream.of("Student", "Activity")
                .forEach(columnTitle -> {
                    final PdfPCell header = new PdfPCell(new Phrase(columnTitle));
                    header.setBackgroundColor(BaseColor.LIGHT_GRAY);
                    header.setHorizontalAlignment(PdfPCell.ALIGN_CENTER);
                    header.setBorderWidth(2);
                    table.addCell(header);
                });

        for (final SingleReport singleReport : team.getSingleReports()) {
            table.addCell(new Paragraph(singleReport.getLastName() + " " + singleReport.getFirstName(), MAIN_FONT));
            table.addCell(getTasksCell(singleReport));
        }
        return table;
    }

    private PdfPCell getTasksCell(final SingleReport singleReport) throws DocumentException {
        final PdfPTable table = new PdfPTable(2);
        table.setWidths(new int[]{85, 15});
        singleReport.getTasks()
                .forEach(task -> {
                    table.addCell(new Paragraph(task.getText(), MAIN_FONT));

                    final PdfPCell spentTimeCell = new PdfPCell(
                            new Paragraph(Double.toString(task.getTimeSpent()), MAIN_FONT));
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
