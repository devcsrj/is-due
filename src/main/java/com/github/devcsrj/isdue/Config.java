package com.github.devcsrj.isdue;

import java.time.LocalDate;
import java.time.ZoneId;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("is-due")
public record Config(LocalDate since, ZoneId zone, Source source, Provider provider) {

  public record Source(Email email) {}

  public record Email(String host, int port, String username, String password, boolean ssl) {}

  public record Provider(SecurityBank securityBank) {}

  public record SecurityBank(LocalDate dob) {}
}
