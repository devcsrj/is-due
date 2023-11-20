package com.github.devcsrj.isdue.converge;

import com.icegreen.greenmail.junit5.GreenMailExtension;
import com.icegreen.greenmail.util.GreenMailUtil;
import com.icegreen.greenmail.util.ServerSetup;
import com.icegreen.greenmail.util.ServerSetupTest;
import org.joda.money.CurrencyUnit;
import org.joda.money.Money;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.core.io.ClassPathResource;

import java.time.LocalDateTime;
import java.time.Month;
import java.time.ZoneId;

import static com.github.devcsrj.isdue.JunitEmails.imapServerFrom;
import static com.github.devcsrj.isdue.JunitEmails.userFor;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class ConvergeEmailInvoiceProviderTest {

    @RegisterExtension
    private static GreenMailExtension greenMail = new GreenMailExtension(new ServerSetup[]{
            ServerSetupTest.IMAP,
            ServerSetupTest.SMTP
    }).withPerMethodLifecycle(false);

    @BeforeAll
    static void beforeAll() throws Exception {
        var input = new ClassPathResource("/converge/2023-nov.html");
        var inputBody = new String(input.getContentAsByteArray());

        var message = GreenMailUtil.createTextEmail(
                "devcsrj@gmail.com",
                "bssnotification@convergeict.com",
                "Payment Reminder",
                inputBody,
                ServerSetupTest.SMTP
        );
        userFor(greenMail).deliver(message);
    }

    @Test
    void canFetchInvoices() {
        var provider = new ConvergeEmailInvoiceProvider(imapServerFrom(greenMail));

        var september1 = LocalDateTime.of(2023, Month.SEPTEMBER, 1, 0, 0, 0);
        var december31 = LocalDateTime.of(2023, Month.DECEMBER, 31, 23, 59, 59);
        var gmt8 = ZoneId.of("Asia/Manila");

        var invoices = provider.getByDate(september1.atZone(gmt8), december31.atZone(gmt8));
        assertEquals(1, invoices.size());

        var first = invoices.get(0);
        assertNotNull(first.id());
        assertEquals(Converge.BILLER, first.biller());
        assertEquals("1680000512345", first.accountNumber());
        assertEquals(Money.of(CurrencyUnit.of("PHP"), 113.27), first.total());

        var november27 = LocalDateTime.of(2023, Month.NOVEMBER, 27, 0, 0, 0);
        assertEquals(november27.atZone(gmt8), first.dueAt());
    }
}
