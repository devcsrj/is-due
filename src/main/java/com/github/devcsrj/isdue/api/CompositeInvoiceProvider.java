package com.github.devcsrj.isdue.api;

import java.time.ZonedDateTime;
import java.util.List;

/** A composite of {@link InvoiceProvider}s */
public final class CompositeInvoiceProvider implements InvoiceProvider {

  private final List<InvoiceProvider> providers;

  public CompositeInvoiceProvider(List<InvoiceProvider> providers) {
    this.providers = providers;
  }

  @Override
  public List<Invoice> getByDate(ZonedDateTime start, ZonedDateTime end) {
    return providers.stream()
        .flatMap(p -> p.getByDate(start, end).stream())
        .collect(java.util.stream.Collectors.toList());
  }
}
