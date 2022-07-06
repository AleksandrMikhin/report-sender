package org.example.sender.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import org.example.sender.utils.FileUtils;
import org.example.sender.utils.PropertiesUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.util.Date;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class ReportSenderTest<T extends ReportProvider> implements Runnable {

    private static final String GET_HTTP_METHOD = "GET";
    private static final String CONTENT_TYPE_PROPERTY_NAME = "content-type";
    private static final String CONTENT_TYPE_JSON = "application/json;charset=UTF-8";
    private static final String DATE_PROPERTY_NAME = "date";
    private static final String SOAP_ACTION_HEADER = "SOAPAction";
    private static final String ATTACHMENT_CONTENT_ID = "attachment";

    private static final String ACCOUNTANT_SERVICE_PATH = PropertiesUtils.getProperty("service.accountant.path");
    private static final String REPORT_SEND_URL = PropertiesUtils.getProperty("router.path");
    private static final String REPORT_ACTION = PropertiesUtils.getProperty("router.soap-actions.report");
    private static final String REPORT_SOAP_ACTION = PropertiesUtils.getProperty("router.soap-actions.report.path");

    private static final String SOAP_NAME_SPACE = "soap";
    private static final String FILE_ELEMENT_ID = "file";



    private final T reportProvider;

    public ReportSenderTest(final Supplier<T> reportSupplier) {
        reportProvider = reportSupplier.get();
    }

    @Override
    public void run() {
        final Date reportDate = new Date();
        try {
            final ObjectMapper objectMapper = new ObjectMapper();
            final List<Team> teams = objectMapper.readValue(getReport(reportDate), new TypeReference<>() {});
            final File reportFile = reportProvider.createReport(teams, reportDate);
//            final File reportFile = reportProvider.createReport(null, reportDate);
            sendReport(reportFile);
        } catch (Exception e) {
            e.printStackTrace();
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

    private static void sendReport(final File reportFile) throws SOAPException, IOException {
        final MessageFactory messageFactory = MessageFactory.newInstance();
        final SOAPMessage soapMessage = messageFactory.createMessage();

        try (final InputStream targetStream = new FileInputStream(reportFile)) {
            final SOAPPart soapPart = soapMessage.getSOAPPart();
            final SOAPEnvelope envelope = soapPart.getEnvelope();
            envelope.addNamespaceDeclaration(SOAP_NAME_SPACE, REPORT_SOAP_ACTION);

            final SOAPBody soapBody = envelope.getBody();
            final SOAPElement bodyChild = soapBody.addChildElement(REPORT_ACTION, SOAP_NAME_SPACE);
//            final SOAPElement fileChild = bodyChild.addChildElement(FILE_ELEMENT_ID);
//            fileChild.addTextNode(ATTACHMENT_CONTENT_ID);

            final SOAPElement fileChild = bodyChild.addChildElement("array");
            final SOAPElement byteArray = fileChild.addChildElement("bytes");
            byteArray.addTextNode(FileUtils.getChairSequence(reportFile));
            final SOAPElement fileName = fileChild.addChildElement("bytes");
            fileName.addTextNode(reportFile.getName());
            fileChild.addTextNode(ATTACHMENT_CONTENT_ID);

//            final DataSource dataSource = new FileDataSource(reportFile);
//            final DataHandler dataHandler = new DataHandler(dataSource);
//            final AttachmentPart attachment = soapMessage.createAttachmentPart(dataHandler);
//            attachment.setContentId(ATTACHMENT_CONTENT_ID);
//            soapMessage.addAttachmentPart(attachment);



//            final MimeHeaders mimeHeaders = soapMessage.getMimeHeaders();
//            mimeHeaders.addHeader(SOAP_ACTION_HEADER, REPORT_SOAP_ACTION);

//            final AttachmentPart attachment = soapMessage.createAttachmentPart();
//            attachment.setContentId(ATTACHMENT_CONTENT_ID);
//            attachment.setRawContent(targetStream, Files.probeContentType(reportFile.toPath()));
//            soapMessage.addAttachmentPart(attachment);
            soapMessage.saveChanges();

            System.out.println("-----------Request SOAP Message---------------");
            soapMessage.writeTo(System.out);
            System.out.println("\n-----------Request SOAP Message---------------");

        }

        final SOAPConnectionFactory soapConnectionFactory = SOAPConnectionFactory.newInstance();
        try (final SOAPConnection soapConnection = soapConnectionFactory.createConnection()) {
            soapConnection.call(soapMessage, REPORT_SEND_URL);
        }
    }
}
