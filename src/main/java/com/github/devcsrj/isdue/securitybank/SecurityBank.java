package com.github.devcsrj.isdue.securitybank;

import com.github.devcsrj.isdue.api.Biller;
import com.github.devcsrj.isdue.api.InvoiceProvider;
import java.time.LocalDate;
import jodd.mail.ImapServer;

public class SecurityBank {

  static final Biller BILLER = new Biller("securitybank", "Security Bank");

  private SecurityBank() {
    throw new AssertionError();
  }

  public static InvoiceProvider from(ImapServer mailServer, LocalDate dateOfBirth) {
    return new SecurityBankEmailInvoiceProvider(mailServer, dateOfBirth);
  }
}
