package com.github.devcsrj.isdue;

import com.github.devcsrj.isdue.api.InvoiceProvider;
import jodd.mail.ImapServer;
import jodd.mail.MailServer;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.context.annotation.Bean;

import java.time.LocalDateTime;
import java.time.Month;
import java.time.ZoneId;

@SpringBootApplication
@ConfigurationPropertiesScan
public class IsDueApplication {

    public static void main(String[] args) {
        var ctx = SpringApplication.run(IsDueApplication.class, args);

        var september = LocalDateTime.of(2023, Month.SEPTEMBER, 1, 0, 0, 0);
        var december = LocalDateTime.of(2023, Month.DECEMBER, 31, 23, 59, 59);
        var gmt8 = ZoneId.of("Asia/Manila");

        var providers = ctx.getBeansOfType(InvoiceProvider.class);
        for (var provider : providers.values()) {
            var invoices = provider.getByDate(september.atZone(gmt8), december.atZone(gmt8));
            for (var invoice : invoices) {
                System.out.println(invoice);
            }
        }
    }

    @Bean
    ImapServer mailServer(Config config) {
        var email = config.source().email();
        return MailServer.create()
                .host(email.host())
                .port(email.port())
                .ssl(email.ssl())
                .auth(email.username(), email.password())
                .buildImapMailServer();
    }
}
