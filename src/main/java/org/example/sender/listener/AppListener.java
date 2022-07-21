package org.example.sender.listener;

import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;
import org.example.sender.provider.PDFReportProvider;
import org.example.sender.service.ReportSender;

import java.time.Duration;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@WebListener
public class AppListener implements ServletContextListener {

    private static final int SECONDS_PER_DAY = 86400;

    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    @Override
    public void contextInitialized(final ServletContextEvent event) {
        final ZonedDateTime currentTime = ZonedDateTime.now(ZoneId.of("GMT+3"));
        ZonedDateTime nextRunTime = currentTime.withHour(22).withMinute(30).withSecond(0);

        final long initialDelay;
        if (currentTime.isAfter(nextRunTime)) {
            initialDelay = Duration.between(currentTime, nextRunTime.plusDays(1)).getSeconds();
        } else {
            initialDelay = Duration.between(currentTime, nextRunTime).getSeconds();
        }

        scheduler.scheduleAtFixedRate(new ReportSender<>(PDFReportProvider::new),
                initialDelay, SECONDS_PER_DAY, TimeUnit.SECONDS);
    }

    @Override
    public void contextDestroyed(final ServletContextEvent event) {
        scheduler.shutdownNow();
    }
}
