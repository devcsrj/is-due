package com.github.devcsrj.isdue.api;

import java.time.ZonedDateTime;
import java.util.List;

public interface InvoiceProvider {

  List<Invoice> getByDate(ZonedDateTime start, ZonedDateTime end);
}
