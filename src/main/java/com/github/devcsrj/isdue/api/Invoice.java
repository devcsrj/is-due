package com.github.devcsrj.isdue.api;

import org.joda.money.Money;

import java.time.ZonedDateTime;
import java.util.List;

public record Invoice(
        String id,
        ZonedDateTime date,
        Money total,
        List<Item> items) {

    public record Item(
            String description,
            ZonedDateTime date,
            Money amount) {
    }
}


