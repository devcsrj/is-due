package com.github.devcsrj.isdue;

import java.time.LocalDate;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("is-due")
public record Config(Source source, Provider provider) {

  public record Source(Email email) {}

  public record Email(String host, int port, String username, String password, boolean ssl) {}

  public record Provider(SecurityBank securityBank) {}

  public record SecurityBank(LocalDate dob) {}
}
