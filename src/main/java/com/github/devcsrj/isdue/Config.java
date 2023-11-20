package com.github.devcsrj.isdue;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("is-due")
public record Config(Source source) {

    public record Source(Email email) {
    }

    public record Email(String host, int port, String username, String password, boolean ssl) {
    }
}
