package com.github.devcsrj.isdue.api;

import java.time.ZonedDateTime;
import java.util.List;

public interface InvoiceProvider {

  default List<Invoice> getSince(ZonedDateTime start) {
    return getByDate(start, ZonedDateTime.now());
  }

  List<Invoice> getByDate(ZonedDateTime start, ZonedDateTime end);
}
