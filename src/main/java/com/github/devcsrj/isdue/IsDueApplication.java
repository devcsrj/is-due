package com.github.devcsrj.isdue;

import jodd.mail.MailServer;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
@ConfigurationPropertiesScan
public class IsDueApplication {

    public static void main(String[] args) {
        SpringApplication.run(IsDueApplication.class, args);
    }


    @Bean
    MailServer mailServer(Config config) {
        var email = config.source().email();
        return MailServer.create()
                .host(email.host())
                .port(email.port())
                .ssl(email.ssl())
                .auth(email.username(), email.password())
                .buildImapMailServer();
    }
}
