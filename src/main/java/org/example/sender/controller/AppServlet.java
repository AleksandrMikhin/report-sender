package org.example.sender.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.example.sender.provider.PDFReportProvider;
import org.example.sender.service.ReportSender;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Date;

@WebServlet("/app")
public class AppServlet extends HttpServlet {

    @Override
    protected void doGet(final HttpServletRequest request,
                         final HttpServletResponse response) throws ServletException, IOException {
        request.setAttribute("date", new Date());
        request.getRequestDispatcher("/index.jsp").forward(request, response);
    }

    @Override
    protected void doPost(final HttpServletRequest req,
                          final HttpServletResponse resp) throws IOException {
        try {
            (new ReportSender<>(PDFReportProvider::new)).run();
        } catch (Throwable e) {
            resp.setContentType("text/html; charset=UTF-8");
            try (final PrintWriter out = resp.getWriter()) {
                Arrays.stream(e.getStackTrace())
                        .map(StackTraceElement::toString)
                        .forEach(out::println);
            }
        }
    }
}
