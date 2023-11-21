package com.github.devcsrj.isdue.securitybank;

import static jodd.mail.EmailFilter.filter;

import com.github.devcsrj.isdue.api.Invoice;
import com.github.devcsrj.isdue.api.InvoiceProvider;
import jakarta.activation.DataSource;
import java.awt.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import jodd.mail.EmailAttachment;
import jodd.mail.EmailFilter;
import jodd.mail.ImapServer;
import jodd.mail.ReceivedEmail;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageTree;
import org.apache.pdfbox.text.PDFTextStripperByArea;
import org.joda.money.CurrencyUnit;
import org.joda.money.Money;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A provider of invoices from Security Bank.
 *
 * <p>Security Bank is a bank in the Philippines - https://www.securitybank.com/. They send invoices
 * via email using {@code CardsESOA@securitybank.com.ph}.
 */
class SecurityBankEmailInvoiceProvider implements InvoiceProvider {

  private static final Logger LOG = LoggerFactory.getLogger(SecurityBankEmailInvoiceProvider.class);
  private final ImapServer mailServer;
  private final LocalDate dateOfBirth;

  SecurityBankEmailInvoiceProvider(ImapServer mailServer, LocalDate dateOfBirth) {
    this.mailServer = mailServer;
    this.dateOfBirth = dateOfBirth;
  }

  @Override
  public List<Invoice> getByDate(ZonedDateTime start, ZonedDateTime end) {
    var filter = Filters.sender().and(Filters.between(start, end));
    LOG.debug("Fetching emails between {} and {}", start, end);

    var invoices = new LinkedList<Invoice>();
    try (var session = this.mailServer.createSession()) {
      session.open();

      var emails = session.receiveEmail(filter);
      LOG.debug("Found {} email(s)", emails.length);

      for (var email : emails) {
        var invoice = readInvoice(email);
        invoice.ifPresent(invoices::add);
      }
    }

    return invoices;
  }

  private Optional<Invoice> readInvoice(ReceivedEmail email) {
    List<EmailAttachment<? extends DataSource>> attachments = email.attachments();
    if (attachments == null || attachments.isEmpty()) {
      LOG.warn("Email '{}' has {} attachments", email.messageId(), attachments.size());
      return Optional.empty();
    }

    for (var attachment : attachments) {
      var name = attachment.getName();
      if (!name.endsWith(".pdf")) {
        LOG.warn("Ignoring unsupported attachment {} on '{}'", name, email.messageId());
        continue;
      }

      Path file = null;
      try {
        file = Files.createTempFile(tempFileName(name), ".pdf");
        attachment.writeToFile(file.toFile());

        var invoice = readInvoice(email, file);
        if (invoice.isPresent()) {
          return invoice;
        }
      } catch (IOException e) {
        throw new IllegalStateException("Unable to create temp file for attachment " + name, e);
      } finally {
        if (file != null) {
          var deleted = file.toFile().delete();
          if (!deleted) {
            LOG.warn("Unable to delete temp file {}", file);
          }
        }
      }
    }

    return Optional.empty();
  }

  private Optional<Invoice> readInvoice(ReceivedEmail email, Path file) {
    var password = pdfPassword();
    try (PDDocument pdf = Loader.loadPDF(file.toFile(), password)) {
      PDPageTree pages = pdf.getPages();
      for (var page : pages) {
        var block = extractSummaryBlock(page);
        if (block.isEmpty()) {
          continue;
        }
        LOG.debug(
            "Found summary block on page {} of '{}'", pages.indexOf(page), file.getFileName());

        var b = block.orElseThrow();
        var id = email.messageId();
        var gmt8 = ZoneId.of("Asia/Manila");
        var issuedDate = ZonedDateTime.ofInstant(email.receivedDate().toInstant(), gmt8);
        var dueDate = ZonedDateTime.of(b.dueDate(), LocalTime.MIDNIGHT, gmt8);
        var invoice =
            new Invoice(
                id,
                SecurityBank.BILLER,
                b.accountNumber(),
                issuedDate,
                dueDate,
                b.totalAmountDue(),
                List.of());

        return Optional.of(invoice);
      }
      return Optional.empty();
    } catch (IOException e) {
      throw new IllegalStateException("Unable to read PDF file " + file, e);
    }
  }

  private String tempFileName(String attachmentName) {
    return attachmentName.substring(0, attachmentName.length() - 4);
  }

  /**
   * The password to open the PDF file is the date of birth of the cardholder.
   *
   * <p>Example: if your date of birth is November 5, 1980, your SOA password is 05NOV1980.
   */
  private String pdfPassword() {
    var format = DateTimeFormatter.ofPattern("ddMMMyyyy");
    return dateOfBirth.format(format).toUpperCase();
  }

  private Optional<SummaryBlock> extractSummaryBlock(PDPage page) {
    try {
      var rect = new Rectangle(254, 83, 247, 106);

      var stripper = new PDFTextStripperByArea();
      stripper.setSortByPosition(true);
      stripper.addRegion("target", rect);
      stripper.extractRegions(page);

      var txt = stripper.getTextForRegion("target");
      return SummaryBlock.parse(txt);
    } catch (IOException e) {
      throw new IllegalStateException("Unable to create PDF stripper", e);
    }
  }

  private record SummaryBlock(String accountNumber, LocalDate dueDate, Money totalAmountDue) {

    /**
     * Parses the raw text into a {@link SummaryBlock}.
     *
     * <p>``` CREDIT CARD ACCOUNT NUMBER xxxx-xxxx-xxxx-xxxx CUT-OFF STATEMENT DATE 24 OCT 2023
     * PAYMENT DUE DATE 14 NOV 2023 CREDIT LIMIT PHP 400,000.00 TOTAL AMOUNT DUE PHP 549.00 MINIMUM
     * AMOUNT DUE PHP 500.00 ```
     */
    static Optional<SummaryBlock> parse(String raw) {
      var lines = raw.split("\n");
      if (lines.length < 6) {
        return Optional.empty();
      }
      if (!lines[0].equals("CREDIT CARD ACCOUNT NUMBER")) {
        return Optional.empty();
      }

      var accountNumber = lines[1].trim();

      var dueDateLine = lines[3].trim().substring("PAYMENT DUE DATE ".length());
      var dueDateFormatter =
          new DateTimeFormatterBuilder()
              .parseCaseInsensitive()
              .append(DateTimeFormatter.ofPattern("dd MMM yyyy"))
              .toFormatter();
      var dueDate = LocalDate.parse(dueDateLine, dueDateFormatter);

      var totalAmountLine = lines[5].trim().substring("TOTAL AMOUNT DUE PHP ".length());
      var totalAmount = Double.parseDouble(totalAmountLine);
      var total = Money.of(CurrencyUnit.of("PHP"), totalAmount);

      var block = new SummaryBlock(accountNumber, dueDate, total);
      return Optional.of(block);
    }
  }

  private static class Filters {

    static EmailFilter sender() {
      return filter().from("CardsESOA@securitybank.com.ph");
    }

    static EmailFilter between(ZonedDateTime start, ZonedDateTime end) {
      return filter()
          .and(
              filter().receivedDate(EmailFilter.Operator.GE, start.toInstant().toEpochMilli()),
              filter().receivedDate(EmailFilter.Operator.LE, end.toInstant().toEpochMilli()));
    }
  }
}
