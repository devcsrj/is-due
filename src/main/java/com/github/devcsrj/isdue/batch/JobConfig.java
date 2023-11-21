package com.github.devcsrj.isdue.batch;

import com.github.devcsrj.isdue.Config;
import com.github.devcsrj.isdue.api.CompositeInvoiceProvider;
import com.github.devcsrj.isdue.api.Invoice;
import com.github.devcsrj.isdue.api.InvoiceProvider;
import java.util.List;
import javax.sql.DataSource;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.database.builder.JdbcBatchItemWriterBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;

@Configuration
public class JobConfig {

  @Bean
  InvoiceItemReader reader(List<InvoiceProvider> invoiceProviders, Config config) {
    var provider = new CompositeInvoiceProvider(invoiceProviders);
    var since = config.since().atTime(23, 59).atZone(config.zone());
    return new InvoiceItemReader(provider, since);
  }

  @Bean
  JdbcBatchItemWriter<Invoice> writer(DataSource dataSource) {
    return new JdbcBatchItemWriterBuilder<Invoice>()
        .dataSource(dataSource)
        .sql(
            "INSERT INTO invoices (id, biller_id, biller_name, account_number, issued_at, due_at, total) VALUES (?, ?, ?, ?, ?, ?, ?)")
        .itemPreparedStatementSetter(
            (item, ps) -> {
              ps.setString(1, item.id());
              ps.setString(2, item.biller().id());
              ps.setString(3, item.biller().name());
              ps.setString(4, item.accountNumber());
              ps.setTimestamp(5, java.sql.Timestamp.from(item.issuedAt().toInstant()));
              ps.setTimestamp(6, java.sql.Timestamp.from(item.dueAt().toInstant()));
              ps.setBigDecimal(7, item.total().getAmount());
            })
        .build();
  }

  @Bean
  Job fetchInvoicesJob(JobRepository jobRepository, Step step1) {
    return new JobBuilder("fetchInvoicesJob", jobRepository).start(step1).build();
  }

  @Bean
  public Step step1(
      JobRepository jobRepository,
      DataSourceTransactionManager transactionManager,
      InvoiceItemReader reader,
      JdbcBatchItemWriter<Invoice> writer) {
    return new StepBuilder("step1", jobRepository)
        .<Invoice, Invoice>chunk(1, transactionManager)
        .reader(reader)
        .writer(writer)
        .build();
  }
}
