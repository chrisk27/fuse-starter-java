package org.galatea.starter.domain;

import java.math.BigDecimal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class AccessingHistoricalPricesJpaApplication {

  private static final Logger log =
      LoggerFactory.getLogger(AccessingHistoricalPricesJpaApplication.class);

  /**
   * Begins running the application.
   * @param args input arguments
   */
  public static void main(final String[] args) {
    SpringApplication.run(AccessingHistoricalPricesJpaApplication.class, args);
  }

  /**
   * Demonstrates some functionality of the database.
   * @param repository Instance of HistoricalPricesRepository
   * @return Outputs on Command line
   */
  @Bean
  public CommandLineRunner demo(final HistoricalPricesRepository repository) {
    return (args) -> {
      // save a few entries
      repository.save(new IexHistoricalPricesDB("DUMM", new BigDecimal("90.0"),
          new BigDecimal("95.50"), new BigDecimal("87.50"), new BigDecimal("92.75"),
          123456L, "2020-11-09"));
      repository.save(new IexHistoricalPricesDB("DUMB", new BigDecimal("60.0"),
          new BigDecimal("99.50"), new BigDecimal("45.50"), new BigDecimal("80.75"),
          179876L, "2020-11-09"));
      repository.save(new IexHistoricalPricesDB("DUMM", new BigDecimal("91.0"),
          new BigDecimal("92.50"), new BigDecimal("90.00"), new BigDecimal("90.0"),
          289366L, "2020-11-10"));

      // fetch all entries
      log.info("Entries found with findAll():");
      log.info("-----------------------------");
      for (IexHistoricalPrices entry : repository.findAll()) {
        log.info(entry.toString());
      }
      log.info("");

      // fetch individual entry by date and name
      log.info("Customer found with findBySymbolAndDate('DUMM', '2020-11-09'):");
      log.info("--------------------------------------------------------------");
      repository.findBySymbolAndDate("DUMM", "2020-11-09").forEach(entry -> {
        log.info(entry.toString());
      });
      log.info("");
    };
  }
}
