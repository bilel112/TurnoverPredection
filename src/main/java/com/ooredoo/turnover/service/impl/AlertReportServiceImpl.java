package com.ooredoo.turnover.service.impl;

import com.ooredoo.turnover.entity.Alert;
import com.ooredoo.turnover.entity.Employee;
import com.ooredoo.turnover.repository.AlertRepository;
import com.ooredoo.turnover.repository.EmployeeRepository;
import com.ooredoo.turnover.service.AlertReportService;
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.List;

@Service
@Transactional
public class AlertReportServiceImpl implements AlertReportService {

    private final AlertRepository alertRepository;
    private final EmployeeRepository employeeRepository;
    private final JavaMailSender mailSender;

    @Value("${alert.report.recipient:bilelbrino3@gmail.com}")
    private String recipient;

    public AlertReportServiceImpl(AlertRepository alertRepository,
                                  EmployeeRepository employeeRepository,
                                  JavaMailSender mailSender) {
        this.alertRepository = alertRepository;
        this.employeeRepository = employeeRepository;
        this.mailSender = mailSender;
    }

    @Override
    public void sendHighRiskAlertReport() {
        List<Alert> alerts = alertRepository.findAll().stream()
                .filter(alert -> "HIGH".equalsIgnoreCase(alert.getSeverity()))
                .filter(alert -> alert.getEmployee() != null)
                .toList();

        if (alerts.isEmpty()) {
            return;
        }

        byte[] pdfBytes = buildHighRiskAlertPdf(alerts);
        String htmlBody = buildEmailHtml(alerts);

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setTo(recipient);
            helper.setSubject("Rapport PDF des alertes High Risk - Turnover");
            helper.setText(htmlBody, true);
            helper.addAttachment("high-risk-alerts.pdf", new ByteArrayResource(pdfBytes));
            mailSender.send(message);
        } catch (MessagingException ex) {
            throw new IllegalStateException("Impossible d'envoyer le rapport par mail", ex);
        }
    }

    byte[] buildHighRiskAlertPdf(List<Alert> alerts) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        Document document = new Document();
        BaseColor ooredooRed = new BaseColor(227, 6, 19);
        BaseColor subtleRed = new BaseColor(255, 242, 242);
        BaseColor borderGray = new BaseColor(221, 221, 221);
        Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 20, ooredooRed);
        Font subtitleFont = FontFactory.getFont(FontFactory.HELVETICA, 10, BaseColor.GRAY);
        Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, BaseColor.WHITE);
        Font bodyFont = FontFactory.getFont(FontFactory.HELVETICA, 9, BaseColor.BLACK);
        Font brandFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11, ooredooRed);

        try {
            PdfWriter.getInstance(document, outputStream);
            document.open();

            Paragraph title = new Paragraph("Rapport des alertes High Risk", titleFont);
            title.setSpacingAfter(4f);
            document.add(title);

            Paragraph subtitle = new Paragraph("Généré le " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")), subtitleFont);
            subtitle.setSpacingAfter(6f);
            document.add(subtitle);

            Paragraph branding = new Paragraph("Ooredoo • Turnover", brandFont);
            branding.setSpacingAfter(10f);
            document.add(branding);

            PdfPTable table = new PdfPTable(6);
            table.setWidthPercentage(100);
            table.setSpacingBefore(4f);
            table.setSpacingAfter(8f);
            table.setWidths(new int[]{1, 2, 3, 4, 2, 2});

            for (String header : List.of("ID", "Employé", "Titre", "Message", "Créée le", "Statut")) {
                PdfPCell headerCell = new PdfPCell(new Paragraph(header, headerFont));
                headerCell.setBackgroundColor(ooredooRed);
                headerCell.setPadding(8);
                headerCell.setBorderColor(borderGray);
                headerCell.setBorderWidth(0.5f);
                headerCell.setHorizontalAlignment(Element.ALIGN_LEFT);
                table.addCell(headerCell);
            }

            for (int rowIndex = 0; rowIndex < alerts.size(); rowIndex++) {
                Alert alert = alerts.get(rowIndex);
                Employee employee = alert.getEmployee();
                String employeeLabel = employee != null ? (employee.getEmployeeNumber() != null ? employee.getEmployeeNumber().toString() : String.valueOf(employee.getId())) : "N/A";
                String message = alert.getMessage() == null ? "" : alert.getMessage().replace("\n", " ");
                String createdAt = alert.getCreatedAt() == null ? "" : alert.getCreatedAt().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));

                addTableCell(table, String.valueOf(alert.getId()), bodyFont, rowIndex, subtleRed, borderGray);
                addTableCell(table, employeeLabel, bodyFont, rowIndex, subtleRed, borderGray);
                addTableCell(table, alert.getTitle() == null ? "" : alert.getTitle(), bodyFont, rowIndex, subtleRed, borderGray);
                addTableCell(table, message, bodyFont, rowIndex, subtleRed, borderGray);
                addTableCell(table, createdAt, bodyFont, rowIndex, subtleRed, borderGray);
                addTableCell(table, alert.getStatus() == null ? "" : alert.getStatus(), bodyFont, rowIndex, subtleRed, borderGray);
            }

            document.add(table);
        } catch (DocumentException e) {
            throw new IllegalStateException("Impossible de générer le rapport PDF", e);
        } finally {
            document.close();
        }

        return outputStream.toByteArray();
    }

    String buildEmailHtml(List<Alert> alerts) {
        String svgDataUri = loadSvgDataUri();
        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html><html><body style='margin:0;padding:0;background:#f4f4f4;font-family:Arial,Helvetica,sans-serif;'>")
                .append("<div style='max-width:920px;margin:24px auto;background:#ffffff;border:1px solid #e7e7e7;border-radius:16px;overflow:hidden;box-shadow:0 8px 24px rgba(0,0,0,0.06);'>")
                .append("<div style='background:linear-gradient(90deg,#E30613 0%,#b1000d 100%);color:#ffffff;padding:24px 30px;display:flex;justify-content:space-between;align-items:center;'>")
                .append("<div><div style='font-size:24px;font-weight:700;'>Rapport des alertes High Risk</div><div style='font-size:13px;margin-top:4px;opacity:0.95;'>Turnover • Généré automatiquement</div></div>");
        if (svgDataUri != null) {
            html.append("<img src='")
                    .append(svgDataUri)
                    .append("' alt='Ooredoo' style='max-height:48px;background:#ffffff;padding:8px 10px;border-radius:8px;' />");
        } else {
            html.append("<div style='background:#ffffff;color:#E30613;padding:8px 12px;border-radius:8px;font-weight:700;'>Ooredoo</div>");
        }
        html.append("</div>")
                .append("<div style='padding:24px 30px;color:#222222;'>")
                .append("<p style='margin:0 0 14px 0;font-size:15px;line-height:1.5;'>Le PDF joint contient la liste complète des alertes High Risk détectées. Téléchargez-le pour consulter le détail des employés concernés.</p>")
                .append("<table role='presentation' cellspacing='0' cellpadding='10' style='border-collapse:collapse;width:100%;font-size:14px;border:1px solid #e7e7e7;border-radius:8px;overflow:hidden;'>")
                .append("<tr style='background:#E30613;color:#ffffff;'><th align='left' style='padding:12px;'>ID</th><th align='left' style='padding:12px;'>Employé</th><th align='left' style='padding:12px;'>Titre</th><th align='left' style='padding:12px;'>Message</th><th align='left' style='padding:12px;'>Créée le</th><th align='left' style='padding:12px;'>Statut</th></tr>");

        for (Alert alert : alerts) {
            Employee employee = alert.getEmployee();
            String employeeLabel = employee != null ? (employee.getEmployeeNumber() != null ? employee.getEmployeeNumber().toString() : String.valueOf(employee.getId())) : "N/A";
            html.append("<tr style='background:#ffffff;'>")
                    .append("<td style='border-top:1px solid #f0f0f0;padding:12px;'>").append(escapeHtml(String.valueOf(alert.getId()))).append("</td>")
                    .append("<td style='border-top:1px solid #f0f0f0;padding:12px;'>").append(escapeHtml(employeeLabel)).append("</td>")
                    .append("<td style='border-top:1px solid #f0f0f0;padding:12px;'>").append(escapeHtml(alert.getTitle() == null ? "" : alert.getTitle())).append("</td>")
                    .append("<td style='border-top:1px solid #f0f0f0;padding:12px;'>").append(escapeHtml(alert.getMessage() == null ? "" : alert.getMessage().replace("\n", "<br/>")))
                    .append("</td>")
                    .append("<td style='border-top:1px solid #f0f0f0;padding:12px;'>").append(escapeHtml(alert.getCreatedAt() == null ? "" : alert.getCreatedAt().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")))).append("</td>")
                    .append("<td style='border-top:1px solid #f0f0f0;padding:12px;'>").append(escapeHtml(alert.getStatus() == null ? "" : alert.getStatus())).append("</td>")
                    .append("</tr>");
        }
        html.append("</table>")
                .append("<p style='margin:16px 0 0 0;font-size:13px;color:#777777;'>Ce rapport a été envoyé automatiquement depuis l’application Turnover.</p>")
                .append("</div></div></body></html>");
        return html.toString();
    }

    private void addTableCell(PdfPTable table, String value, Font font, int rowIndex, BaseColor subtleRed, BaseColor borderGray) {
        PdfPCell cell = new PdfPCell(new Paragraph(value == null ? "" : value, font));
        cell.setPadding(7);
        cell.setBorderColor(borderGray);
        cell.setBorderWidth(0.5f);
        cell.setBackgroundColor(rowIndex % 2 == 0 ? subtleRed : BaseColor.WHITE);
        table.addCell(cell);
    }

    private String escapeHtml(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }

    private String loadSvgDataUri() {
        try {
            Path svgPath = Path.of("frontend/images/logoOoredoo.svg");
            if (!Files.exists(svgPath)) {
                return null;
            }
            byte[] svgBytes = Files.readAllBytes(svgPath);
            String encoded = Base64.getEncoder().encodeToString(svgBytes);
            return "data:image/svg+xml;base64," + encoded;
        } catch (Exception ex) {
            return null;
        }
    }
}
