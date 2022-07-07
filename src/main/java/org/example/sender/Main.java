package org.example.sender;

import org.example.sender.provider.PDFReportProviderTest;
import org.example.sender.service.ReportSender;

public class Main {

    public static void main(String[] args) {
        System.out.println("Hello sender!");
        new ReportSender<>(PDFReportProviderTest::new).run();
    }
}
