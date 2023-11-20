package com.github.devcsrj.isdue.converge;

import com.github.devcsrj.isdue.api.Biller;
import com.github.devcsrj.isdue.api.InvoiceProvider;
import jodd.mail.ImapServer;

public class Converge {
    static final Biller BILLER = new Biller("converge", "Converge ICT Solutions Inc.");

    private Converge() {
        throw new AssertionError();
    }

    public static InvoiceProvider from(ImapServer mailServer) {
        return new ConvergeEmailInvoiceProvider(mailServer);
    }
}
