package com.restohub.adminapi.service;

import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.restohub.adminapi.entity.RestaurantSubscription;
import com.restohub.adminapi.entity.SubscriptionPayment;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.awt.Color;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

@Service
public class InvoicePdfService {
    
    private static final Font TITLE_FONT = new Font(Font.HELVETICA, 18, Font.BOLD);
    private static final Font HEADER_FONT = new Font(Font.HELVETICA, 12, Font.BOLD);
    private static final Font NORMAL_FONT = new Font(Font.HELVETICA, 10, Font.NORMAL);
    private static final Font SMALL_FONT = new Font(Font.HELVETICA, 9, Font.NORMAL);
    
    @Value("${company.name:RestoHub}")
    private String companyName;
    
    @Value("${company.bin:}")
    private String companyBin;
    
    @Value("${company.address:}")
    private String companyAddress;
    
    @Value("${company.bank:}")
    private String companyBank;
    
    @Value("${company.bik:}")
    private String companyBik;
    
    @Value("${company.iik:}")
    private String companyIik;
    
    @Value("${company.kbe:}")
    private String companyKbe;
    
    public byte[] generateInvoice(RestaurantSubscription subscription) throws DocumentException {
        Document document = new Document(PageSize.A4);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PdfWriter.getInstance(document, baos);
        
        document.open();
        
        // Заголовок
        Paragraph title = new Paragraph("СЧЕТ НА ОПЛАТУ", TITLE_FONT);
        title.setAlignment(Element.ALIGN_CENTER);
        title.setSpacingAfter(20);
        document.add(title);
        
        // Информация о компании
        addCompanyInfo(document);
        
        // Информация о клиенте
        addClientInfo(document, subscription);
        
        // Детали счета
        addInvoiceDetails(document, subscription, false);
        
        // Таблица позиций
        addItemsTable(document, subscription);
        
        // Итого
        addTotal(document, subscription);
        
        // Подписи
        addSignatures(document, false);
        
        document.close();
        return baos.toByteArray();
    }
    
    public byte[] generatePaidInvoice(RestaurantSubscription subscription, SubscriptionPayment payment) throws DocumentException {
        Document document = new Document(PageSize.A4);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PdfWriter.getInstance(document, baos);
        
        document.open();
        
        // Заголовок
        Paragraph title = new Paragraph("ОПЛАЧЕННЫЙ СЧЕТ", TITLE_FONT);
        title.setAlignment(Element.ALIGN_CENTER);
        title.setSpacingAfter(20);
        document.add(title);
        
        // Информация о компании
        addCompanyInfo(document);
        
        // Информация о клиенте
        addClientInfo(document, subscription);
        
        // Детали счета
        addInvoiceDetails(document, subscription, true);
        
        // Информация о платеже
        addPaymentInfo(document, payment);
        
        // Таблица позиций
        addItemsTable(document, subscription);
        
        // Итого
        addTotal(document, subscription);
        
        // Подписи
        addSignatures(document, true);
        
        document.close();
        return baos.toByteArray();
    }
    
    private void addCompanyInfo(Document document) throws DocumentException {
        Paragraph company = new Paragraph();
        company.add(new Chunk("Поставщик: ", HEADER_FONT));
        company.add(new Chunk(companyName, NORMAL_FONT));
        company.setSpacingAfter(5);
        document.add(company);
        
        if (companyBin != null && !companyBin.isEmpty()) {
            Paragraph bin = new Paragraph("БИН: " + companyBin, NORMAL_FONT);
            bin.setSpacingAfter(5);
            document.add(bin);
        }
        
        if (companyAddress != null && !companyAddress.isEmpty()) {
            Paragraph address = new Paragraph("Адрес: " + companyAddress, NORMAL_FONT);
            address.setSpacingAfter(5);
            document.add(address);
        }
        
        if (companyBank != null && !companyBank.isEmpty()) {
            Paragraph bank = new Paragraph("Банк: " + companyBank, NORMAL_FONT);
            bank.setSpacingAfter(5);
            document.add(bank);
        }
        
        if (companyBik != null && !companyBik.isEmpty()) {
            Paragraph bik = new Paragraph("БИК: " + companyBik, NORMAL_FONT);
            bik.setSpacingAfter(5);
            document.add(bik);
        }
        
        if (companyIik != null && !companyIik.isEmpty()) {
            Paragraph iik = new Paragraph("ИИК: " + companyIik, NORMAL_FONT);
            iik.setSpacingAfter(5);
            document.add(iik);
        }
        
        if (companyKbe != null && !companyKbe.isEmpty()) {
            Paragraph kbe = new Paragraph("КБЕ: " + companyKbe, NORMAL_FONT);
            kbe.setSpacingAfter(15);
            document.add(kbe);
        } else {
            document.add(new Paragraph(" "));
        }
    }
    
    private void addClientInfo(Document document, RestaurantSubscription subscription) throws DocumentException {
        Paragraph client = new Paragraph();
        client.add(new Chunk("Покупатель: ", HEADER_FONT));
        client.add(new Chunk(subscription.getRestaurant().getName(), NORMAL_FONT));
        client.setSpacingAfter(5);
        document.add(client);
        
        if (subscription.getRestaurant().getAddress() != null && !subscription.getRestaurant().getAddress().isEmpty()) {
            Paragraph address = new Paragraph("Адрес: " + subscription.getRestaurant().getAddress(), NORMAL_FONT);
            address.setSpacingAfter(15);
            document.add(address);
        } else {
            document.add(new Paragraph(" "));
        }
    }
    
    private void addInvoiceDetails(Document document, RestaurantSubscription subscription, boolean isPaid) throws DocumentException {
        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{1, 2});
        
        addTableRow(table, "Номер счета:", subscription.getPaymentReference() != null ? subscription.getPaymentReference() : "N/A");
        addTableRow(table, "Дата:", subscription.getCreatedAt().format(DateTimeFormatter.ofPattern("dd.MM.yyyy", Locale.forLanguageTag("ru"))));
        
        if (isPaid && subscription.getStartDate() != null) {
            addTableRow(table, "Дата оплаты:", subscription.getStartDate().format(DateTimeFormatter.ofPattern("dd.MM.yyyy", Locale.forLanguageTag("ru"))));
        }
        
        document.add(table);
        document.add(new Paragraph(" "));
    }
    
    private void addPaymentInfo(Document document, SubscriptionPayment payment) throws DocumentException {
        Paragraph paymentInfo = new Paragraph("Информация о платеже:", HEADER_FONT);
        paymentInfo.setSpacingAfter(5);
        document.add(paymentInfo);
        
        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{1, 2});
        
        if (payment.getExternalTransactionId() != null) {
            addTableRow(table, "ID транзакции:", payment.getExternalTransactionId());
        }
        addTableRow(table, "Дата платежа:", payment.getPaymentDate().format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm", Locale.forLanguageTag("ru"))));
        
        document.add(table);
        document.add(new Paragraph(" "));
    }
    
    private void addItemsTable(Document document, RestaurantSubscription subscription) throws DocumentException {
        PdfPTable table = new PdfPTable(5);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{1, 3, 1, 1, 1});
        
        // Заголовки
        addTableHeaderCell(table, "№");
        addTableHeaderCell(table, "Наименование");
        addTableHeaderCell(table, "Кол-во");
        addTableHeaderCell(table, "Цена");
        addTableHeaderCell(table, "Сумма");
        
        // Позиция
        String subscriptionName = subscription.getSubscriptionType() != null 
            ? subscription.getSubscriptionType().getName() 
            : "Подписка";
        BigDecimal price = subscription.getSubscriptionType() != null && subscription.getSubscriptionType().getPrice() != null
            ? subscription.getSubscriptionType().getPrice()
            : BigDecimal.ZERO;
        
        addTableCell(table, "1", NORMAL_FONT);
        addTableCell(table, subscriptionName, NORMAL_FONT);
        addTableCell(table, "1", NORMAL_FONT);
        addTableCell(table, String.format("%.2f", price), NORMAL_FONT);
        addTableCell(table, String.format("%.2f", price), NORMAL_FONT);
        
        document.add(table);
        document.add(new Paragraph(" "));
    }
    
    private void addTotal(Document document, RestaurantSubscription subscription) throws DocumentException {
        BigDecimal total = subscription.getSubscriptionType() != null && subscription.getSubscriptionType().getPrice() != null
            ? subscription.getSubscriptionType().getPrice()
            : BigDecimal.ZERO;
        
        Paragraph totalParagraph = new Paragraph();
        totalParagraph.add(new Chunk("Итого: ", HEADER_FONT));
        totalParagraph.add(new Chunk(String.format("%.2f тенге", total), HEADER_FONT));
        totalParagraph.setAlignment(Element.ALIGN_RIGHT);
        totalParagraph.setSpacingAfter(20);
        document.add(totalParagraph);
    }
    
    private void addSignatures(Document document, boolean isPaid) throws DocumentException {
        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{1, 1});
        
        PdfPCell supplierCell = new PdfPCell(new Phrase("Поставщик:", SMALL_FONT));
        supplierCell.setBorder(Rectangle.NO_BORDER);
        supplierCell.setPaddingTop(30);
        table.addCell(supplierCell);
        
        PdfPCell customerCell = new PdfPCell(new Phrase("Покупатель:", SMALL_FONT));
        customerCell.setBorder(Rectangle.NO_BORDER);
        customerCell.setPaddingTop(30);
        table.addCell(customerCell);
        
        document.add(table);
    }
    
    private void addTableRow(PdfPTable table, String label, String value) {
        addTableCell(table, label, HEADER_FONT);
        addTableCell(table, value, NORMAL_FONT);
    }
    
    private void addTableHeaderCell(PdfPTable table, String text) {
        PdfPCell cell = new PdfPCell(new Phrase(text, HEADER_FONT));
        cell.setPadding(5);
        cell.setBackgroundColor(new Color(220, 220, 220));
        table.addCell(cell);
    }
    
    private void addTableCell(PdfPTable table, String text, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setPadding(5);
        table.addCell(cell);
    }
}

