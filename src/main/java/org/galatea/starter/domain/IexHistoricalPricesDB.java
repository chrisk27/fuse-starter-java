package org.galatea.starter.domain;

import java.math.BigDecimal;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor
public class IexHistoricalPricesDB {

  @Id
  @GeneratedValue (strategy = GenerationType.AUTO)
  private long id;

  private String symbol;
  private BigDecimal close;
  private BigDecimal high;
  private BigDecimal low;
  private BigDecimal open;
  private long volume;
  private String date;

/**
 * Constructor method for IexHistoricalPrices.
 * @param symbol Stock symbol
 * @param close Price at close
 * @param high Adjusted high price for the day
 * @param low Adjusted low price for the day
 * @param open Adjusted open price for the day
 * @param volume Volume of trades for the day
 * @param date Date queried (formatted yyyy-mm-dd)
 */
  public IexHistoricalPricesDB(final String symbol, final BigDecimal close, final BigDecimal high,
      final BigDecimal low, final BigDecimal open, final long volume, final String date) {
    this.symbol = symbol;
    this.close = close;
    this.high = high;
    this.low = low;
    this.open = open;
    this.volume = volume;
    this.date = date;
  }

  /**
   * Constructs database entry from an IexHistoricalPrices object from API call
   * @param entity an IexHistoricalPrices object
   */
  public IexHistoricalPricesDB(final IexHistoricalPrices entity) {
    this.symbol = entity.getSymbol();
    this.close = entity.getClose();
    this.high = entity.getHigh();
    this.low = entity.getLow();
    this.open = entity.getOpen();
    this.volume = entity.getVolume();
    this.date = entity.getDate();
  }

  /**
   * ID getter.
   * @return id
   */
  public long getId() {
    return id;
  }

  /**
   * Symbol getter.
   * @return symbol
   */
  public String getSymbol() {
    return symbol;
  }

  /**
   * Close price getter.
   * @return close
   */
  public BigDecimal getClose() {
    return close;
  }

  /**
   * High price getter.
   * @return high
   */
  public BigDecimal getHigh() {
    return high;
  }

  /**
   * Low price getter.
   * @return low
   */
  public BigDecimal getLow() {
    return low;
  }

  /**
   * Open price getter.
   * @return open
   */
  public BigDecimal getOpen() {
    return open;
  }

  /**
   * Volume getter.
   * @return volume
   */
  public long getVolume() {
    return volume;
  }

  /**
   * Date getter.
   * @return date
   */
  public String getDate() {
    return date;
  }
}
