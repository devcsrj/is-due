package com.github.devcsrj.isdue;

import com.github.devcsrj.isdue.api.InvoiceProvider;
import com.github.devcsrj.isdue.converge.Converge;
import com.github.devcsrj.isdue.securitybank.SecurityBank;
import jodd.mail.ImapServer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class Provider {
  @Bean
  InvoiceProvider convergeInvoiceProvider(ImapServer mailServer) {
    return Converge.from(mailServer);
  }

  @Bean
  InvoiceProvider securityBankInvoiceProvider(ImapServer mailServer, Config config) {
    var c = config.provider().securityBank();
    return SecurityBank.from(mailServer, c.dob());
  }
}
