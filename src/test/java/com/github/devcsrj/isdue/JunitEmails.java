package com.github.devcsrj.isdue;

import com.icegreen.greenmail.junit5.GreenMailExtension;
import com.icegreen.greenmail.user.GreenMailUser;
import jodd.mail.ImapServer;
import jodd.mail.MailServer;

public final class JunitEmails {

    private static final String testUsername = "duebot";
    private static final String testPassword = "testing";


    public static GreenMailUser userFor(GreenMailExtension extension) {
        return extension.setUser(testUsername, testPassword);
    }
    public static ImapServer imapServerFrom(GreenMailExtension extension) {
        var setup = extension.getImap().getServerSetup();
        return MailServer.create()
                         .host(setup.getBindAddress())
                         .port(setup.getPort())
                         .ssl(setup.isSecure())
                         .auth(testUsername, testPassword)
                         .buildImapMailServer();
    }

}
