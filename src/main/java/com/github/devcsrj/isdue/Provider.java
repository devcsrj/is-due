package com.github.devcsrj.isdue;

import com.github.devcsrj.isdue.api.InvoiceProvider;
import com.github.devcsrj.isdue.converge.Converge;
import jodd.mail.ImapServer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class Provider {
    @Bean
    InvoiceProvider convergeInvoiceProvider(ImapServer mailServer) {
        return Converge.from(mailServer);

    }
}
