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
import org.example.sender.utils.PropertiesUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Date;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class ReportSender<T extends ReportProvider> implements Runnable {

    private static final String GET_HTTP_METHOD = "GET";
    private static final String CONTENT_TYPE_PROPERTY_NAME = "content-type";
    private static final String CONTENT_TYPE_JSON = "application/json;charset=UTF-8";
    private static final String DATE_PROPERTY_NAME = "date";

    private static final String ACCOUNTANT_SERVICE_PATH = PropertiesUtils.getProperty("service.accountant.path");
    private static final String REPORT_SEND_URL = PropertiesUtils.getProperty("router.path");
    private static final String SOAP_NAME_SPACE = PropertiesUtils.getProperty("router.soap-actions.name-space");
    private static final String REPORT_ACTION_METHOD = PropertiesUtils.getProperty("router.soap-actions.report");

    private static final String SOAP_NAME_SPACE_PREFIX = "soap";
    private static final String FILE_ELEMENT_ID = "file";
    private static final String CID_HEADER = "cid:";
    private static final String CONTEXT_ID = "reports.pdf";

    private final T reportProvider;

    public ReportSender(final Supplier<T> reportSupplier) {
        reportProvider = reportSupplier.get();
    }

    @Override
    public void run() {
        final File reportFile;
        try {
            final Date reportDate = new Date();
            final ObjectMapper objectMapper = new ObjectMapper();
            final List<Team> teams = objectMapper.readValue(getReport(reportDate), new TypeReference<>() {});
            reportFile = reportProvider.createReport(teams, reportDate);
        } catch (Exception e) {
            throw new RuntimeException("Something went wrong with an accountant service", e);
        }

        try {
            sendReport(reportFile);
        } catch (SOAPException e) {
            throw new RuntimeException("Something went wrong during sending a report to the router service.", e);
        }
    }

    private static String getReport(final Date date) throws IOException {
        final HttpURLConnection con = (HttpURLConnection) new URL(ACCOUNTANT_SERVICE_PATH).openConnection();
        con.setRequestMethod(GET_HTTP_METHOD);
        con.setRequestProperty(CONTENT_TYPE_PROPERTY_NAME, CONTENT_TYPE_JSON);
        con.setRequestProperty(DATE_PROPERTY_NAME, date.toString());

        final int responseCode = con.getResponseCode();
        if (responseCode != HttpURLConnection.HTTP_OK) {
            throw new RuntimeException(String.format("Service %s return %d code.",
                    ACCOUNTANT_SERVICE_PATH, responseCode));
        }

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(con.getInputStream()))) {
            return reader.lines().collect(Collectors.joining(System.lineSeparator()));
        }
    }

    public static void sendReport(File file) throws SOAPException {
        final MessageFactory messageFactory = MessageFactory.newInstance();
        final SOAPMessage message = messageFactory.createMessage();

        final SOAPPart soapPart = message.getSOAPPart();
        final SOAPEnvelope envelope = soapPart.getEnvelope();
        envelope.addNamespaceDeclaration(SOAP_NAME_SPACE_PREFIX, SOAP_NAME_SPACE);

        final SOAPBody soapBody = envelope.getBody();
        final SOAPElement soapElement = soapBody.addChildElement(REPORT_ACTION_METHOD, SOAP_NAME_SPACE_PREFIX);

        final DataHandler dataHandler = new DataHandler(new FileDataSource(file));
        final AttachmentPart attachment = message.createAttachmentPart(dataHandler);
        attachment.setContentId(CONTEXT_ID);
        message.addAttachmentPart(attachment);

        final SOAPElement soapFileElement = soapElement.addChildElement(FILE_ELEMENT_ID);
        soapFileElement.addTextNode(CID_HEADER + CONTEXT_ID);
        message.saveChanges();

        try (final SOAPConnection soapConnection = SOAPConnectionFactory.newInstance().createConnection()) {
            soapConnection.call(message, REPORT_SEND_URL);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
