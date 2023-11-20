package com.github.devcsrj.isdue.converge;

import com.github.devcsrj.isdue.api.Invoice;
import com.github.devcsrj.isdue.api.InvoiceProvider;
import jodd.mail.EmailFilter;
import jodd.mail.ImapServer;
import jodd.mail.ReceiveMailSession;
import jodd.mail.ReceivedEmail;
import org.joda.money.CurrencyUnit;
import org.joda.money.Money;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static jodd.mail.EmailFilter.filter;

/**
 * A provider of invoices from Converge.
 * <p>
 * Converge is an ISP in the Philippines - https://www.convergeict.com/. They send invoices
 * via email using {@code bssnotification@convergeict.com}.
 */
class ConvergeEmailInvoiceProvider implements InvoiceProvider {

    private static final Logger LOG = LoggerFactory.getLogger(ConvergeEmailInvoiceProvider.class);
    private static final Pattern ACCOUNT_NUMBER_PATTERN = Pattern.compile("Account Number: (\\d+)");
    private static final Pattern TOTAL_AMOUNT_PATTERN = Pattern.compile("Your total amount due is: P (\\d+\\.\\d+)");
    private static final Pattern DUE_DATE_PATTERN = Pattern.compile("Due Date: (.+)\\.");

    private final ImapServer mailServer;

    ConvergeEmailInvoiceProvider(ImapServer mailServer) {
        this.mailServer = mailServer;
    }

    @Override
    public List<Invoice> getByDate(ZonedDateTime start, ZonedDateTime end) {
        var filter = Filters.sender().and(Filters.between(start, end));
        LOG.debug("Fetching emails between {} and {}", start, end);

        var invoices = new LinkedList<Invoice>();
        try (ReceiveMailSession session = this.mailServer.createSession()) {
            session.open();

            var emails = session.receiveEmail(filter);
            LOG.debug("Found {} email(s)", emails.length);

            for (var email : emails) {
                var invoice = readInvoice(email);
                if (invoice.isPresent()) {
                    invoices.add(invoice.get());
                }
            }
        }

        return invoices;
    }

    private Optional<Invoice> readInvoice(ReceivedEmail email) {
        var messages = email.messages();
        for (var message : messages) {
            var content = message.getContent();

            var id = email.messageId();
            var accountNumber = this.parseAccountNumber(content);
            var issuedDate = ZonedDateTime.ofInstant(email.receivedDate().toInstant(), ZoneId.of("Asia/Manila"));
            var dueDate = this.parseDueDate(content);
            var totalAmount = this.parseTotalAmount(content);

            var invoice = new Invoice(
                    id,
                    Converge.BILLER,
                    accountNumber,
                    issuedDate,
                    dueDate,
                    totalAmount,
                    List.of()
            );
            return Optional.of(invoice);
        }
        return Optional.empty();
    }

    private String parseAccountNumber(String content) {
        Matcher matcher = ACCOUNT_NUMBER_PATTERN.matcher(content);
        if (!matcher.find()) {
            throw new IllegalArgumentException("Unable to parse account number");
        }
        return matcher.group(1);
    }

    private Money parseTotalAmount(String content) {
        Matcher matcher = TOTAL_AMOUNT_PATTERN.matcher(content);
        if (!matcher.find()) {
            throw new IllegalArgumentException("Unable to parse total amount");
        }
        var amount = Double.parseDouble(matcher.group(1));
        return Money.of(CurrencyUnit.of("PHP"), amount);
    }

    private ZonedDateTime parseDueDate(String content) {
        Matcher matcher = DUE_DATE_PATTERN.matcher(content);
        if (!matcher.find()) {
            throw new IllegalArgumentException("Unable to parse due date");
        }
        var dueDate = matcher.group(1);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMMM dd,yyyy");
        var localDate = LocalDate.parse(dueDate, formatter);

        return ZonedDateTime.of(localDate.atTime(0, 0), ZoneId.of("Asia/Manila"));
    }

    static class Filters {

        static EmailFilter sender() {
            return filter()
                    .from("bssnotification@convergeict.com")
                    .and()
                    .subject("Payment Reminder");
        }

        static EmailFilter between(ZonedDateTime start, ZonedDateTime end) {
            return filter()
                    .and(
                            filter().receivedDate(EmailFilter.Operator.GE, start.toInstant().toEpochMilli()),
                            filter().receivedDate(EmailFilter.Operator.LE, end.toInstant().toEpochMilli())
                    );
        }
    }
}
