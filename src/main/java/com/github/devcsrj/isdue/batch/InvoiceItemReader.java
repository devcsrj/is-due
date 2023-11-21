package com.github.devcsrj.isdue.batch;

import com.github.devcsrj.isdue.api.Invoice;
import com.github.devcsrj.isdue.api.InvoiceProvider;
import com.google.common.base.Suppliers;
import java.time.ZonedDateTime;
import java.util.Iterator;
import java.util.List;
import java.util.function.Supplier;
import org.springframework.batch.item.ItemReader;

final class InvoiceItemReader implements ItemReader<Invoice> {
  private final Supplier<List<Invoice>> invoices;
  private final Supplier<Iterator<Invoice>> iterator;

  InvoiceItemReader(InvoiceProvider provider, ZonedDateTime since) {
    this.invoices = Suppliers.memoize(() -> provider.getSince(since));
    this.iterator = Suppliers.memoize(() -> invoices.get().iterator());
  }

  @Override
  public Invoice read() throws Exception {
    if (iterator.get().hasNext()) {
      return iterator.get().next();
    }
    return null; // end of data
  }
}
