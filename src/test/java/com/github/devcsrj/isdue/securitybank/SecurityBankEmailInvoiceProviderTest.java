package com.github.devcsrj.isdue.securitybank;

import static com.github.devcsrj.isdue.JunitEmails.imapServerFrom;
import static com.github.devcsrj.isdue.JunitEmails.userFor;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.icegreen.greenmail.junit5.GreenMailExtension;
import com.icegreen.greenmail.util.ServerSetup;
import com.icegreen.greenmail.util.ServerSetupTest;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMultipart;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.ZoneId;
import org.joda.money.CurrencyUnit;
import org.joda.money.Money;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.core.io.ClassPathResource;

public class SecurityBankEmailInvoiceProviderTest {

  @RegisterExtension
  private static GreenMailExtension greenMail =
      new GreenMailExtension(new ServerSetup[] {ServerSetupTest.IMAP, ServerSetupTest.SMTP})
          .withPerMethodLifecycle(false);

  @BeforeAll
  static void beforeAll() throws Exception {
    var session = greenMail.getSmtp().createSession();
    var message = new MimeMessage(session);
    message.setFrom("CardsESOA@securitybank.com.ph");
    message.setRecipients(MimeMessage.RecipientType.TO, "devcsrj@gmail.com");
    message.setSubject("Security Bank Credit Card Statement of Account as of 24 October 2023");

    var multipart = getMimeMultipart();
    message.setContent(multipart);

    userFor(greenMail).deliver(message);
  }

  private static MimeMultipart getMimeMultipart() throws IOException, MessagingException {
    var html = new ClassPathResource("/securitybank/2023-oct.html");
    var htmlBody = new String(html.getContentAsByteArray());
    var textPart = new MimeBodyPart();
    textPart.setText(htmlBody);

    var pdf = new ClassPathResource("/securitybank/2023-oct.pdf");
    var attachmentPart = new MimeBodyPart();
    attachmentPart.attachFile(pdf.getFile());

    var multipart = new MimeMultipart();
    multipart.addBodyPart(textPart);
    multipart.addBodyPart(attachmentPart);
    return multipart;
  }

  @Test
  void canFetchInvoices() {
    var dob = LocalDate.of(1995, 6, 1);
    var provider = new SecurityBankEmailInvoiceProvider(imapServerFrom(greenMail), dob);

    var september1 = LocalDateTime.of(2023, Month.SEPTEMBER, 1, 0, 0, 0);
    var december31 = LocalDateTime.of(2023, Month.DECEMBER, 31, 23, 59, 59);
    var gmt8 = ZoneId.of("Asia/Manila");

    var invoices = provider.getByDate(september1.atZone(gmt8), december31.atZone(gmt8));
    assertEquals(1, invoices.size());

    var first = invoices.get(0);
    assertNotNull(first.id());
    assertEquals(SecurityBank.BILLER, first.biller());
    assertEquals("5182-", first.accountNumber());
    assertEquals(Money.of(CurrencyUnit.of("PHP"), 549), first.total());
  }
}
