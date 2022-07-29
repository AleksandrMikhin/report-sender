package org.example.sender.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.activation.DataHandler;
import jakarta.activation.FileDataSource;
import jakarta.xml.soap.AttachmentPart;
import jakarta.xml.soap.MessageFactory;
import jakarta.xml.soap.SOAPBody;
import jakarta.xml.soap.SOAPConnection;
import jakarta.xml.soap.SOAPConnectionFactory;
import jakarta.xml.soap.SOAPElement;
import jakarta.xml.soap.SOAPEnvelope;
import jakarta.xml.soap.SOAPException;
import jakarta.xml.soap.SOAPMessage;
import jakarta.xml.soap.SOAPPart;
import org.example.sender.entity.Team;
import org.example.sender.provider.ReportProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
@EnableScheduling
public class ReportSender {

    private static final String GET_HTTP_METHOD = "GET";
    private static final String CONTENT_TYPE_PROPERTY_NAME = "content-type";
    private static final String CONTENT_TYPE_JSON = "application/json;charset=UTF-8";
    private static final String DATE_PROPERTY_NAME = "date";
    private static final String SOAP_NAME_SPACE_PREFIX = "soap";
    private static final String FILE_ELEMENT_ID = "file";
    private static final String CID_HEADER = "cid:";
    private static final String CONTEXT_ID = "reports.pdf";

    @Value("${service.accountant.path}")
    private String accountantServiceUrl;

    @Value("${router.path}")
    private String reportSendUrl;

    @Value("${router.soap-actions.name-space}")
    private String soapNameSpace;

    @Value("${router.soap-actions.report}")
    private String reportActionMethod;

    private final ReportProvider reportProvider;

    public ReportSender(final ReportProvider reportProvider) {
        this.reportProvider = reportProvider;
    }

    @Scheduled(cron = "0 0 22 * * *")
    public void sendReport() {
        final File reportFile;
        try {
            final Date reportDate = new Date();
            final ObjectMapper objectMapper = new ObjectMapper();
            final List<Team> teams = objectMapper.readValue(getReport(reportDate), new TypeReference<>() {});
            reportFile = reportProvider.createReport(teams, reportDate);
        } catch (final Exception e) {
            throw new RuntimeException("Something went wrong with an accountant service", e);
        }

        try {
            sendReportFile(reportFile);
        } catch (final SOAPException e) {
            throw new RuntimeException("Something went wrong during sending a report to the router service.", e);
        }
    }

    private String getReport(final Date date) throws IOException {
        final HttpURLConnection con = (HttpURLConnection) new URL(accountantServiceUrl).openConnection();
        con.setRequestMethod(GET_HTTP_METHOD);
        con.setRequestProperty(CONTENT_TYPE_PROPERTY_NAME, CONTENT_TYPE_JSON);
        con.setRequestProperty(DATE_PROPERTY_NAME, date.toString());

        final int responseCode = con.getResponseCode();
        if (responseCode != HttpURLConnection.HTTP_OK) {
            throw new RuntimeException(String.format("Service %s return %d code.",
                    accountantServiceUrl, responseCode));
        }

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(con.getInputStream()))) {
            return reader.lines().collect(Collectors.joining(System.lineSeparator()));
        }
    }

    private void sendReportFile(final File file) throws SOAPException {
        final MessageFactory messageFactory = MessageFactory.newInstance();
        final SOAPMessage message = messageFactory.createMessage();

        final SOAPPart soapPart = message.getSOAPPart();
        final SOAPEnvelope envelope = soapPart.getEnvelope();
        envelope.addNamespaceDeclaration(SOAP_NAME_SPACE_PREFIX, soapNameSpace);

        final SOAPBody soapBody = envelope.getBody();
        final SOAPElement soapElement = soapBody.addChildElement(reportActionMethod, SOAP_NAME_SPACE_PREFIX);

        final DataHandler dataHandler = new DataHandler(new FileDataSource(file));
        final AttachmentPart attachment = message.createAttachmentPart(dataHandler);
        attachment.setContentId(CONTEXT_ID);
        message.addAttachmentPart(attachment);

        final SOAPElement soapFileElement = soapElement.addChildElement(FILE_ELEMENT_ID);
        soapFileElement.addTextNode(CID_HEADER + CONTEXT_ID);
        message.saveChanges();

        try (SOAPConnection soapConnection = SOAPConnectionFactory.newInstance().createConnection()) {
            soapConnection.call(message, reportSendUrl);
        } catch (final Exception e) {
            e.printStackTrace();
        }
    }
}
