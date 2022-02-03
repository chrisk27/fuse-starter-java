package org.galatea.starter.domain;

import java.util.List;

import org.springframework.data.repository.CrudRepository;

public interface HistoricalPricesRepository extends CrudRepository<IexHistoricalPricesDB, Long> {

  /**
   * Query database by both date and symbol.
   * @param symbol Symbol of stock to get historical price for
   * @param date Date you want to query (formatted YYYYMMDD)
   * @return List of IexHistoricalPrices object
   */
  List<IexHistoricalPricesDB> findBySymbolAndDate(String symbol, String date);

  /**
   * Query database by automatically-generated id.
   * @param id ID number of the IexHistoricalPrices object
   * @return IexHistoricalPrices object
   */
  IexHistoricalPricesDB findById(long id);

  List<IexHistoricalPricesDB> findBySymbol(String symbol);

  boolean existsBySymbol(String symbol);
}
