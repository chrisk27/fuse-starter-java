package org.galatea.starter.domain;

import java.math.BigDecimal;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class IexHistoricalPrices {

  private String symbol;
  private BigDecimal close;
  private BigDecimal high;
  private BigDecimal low;
  private BigDecimal open;
  private long volume;
  private String date;

  protected IexHistoricalPrices() {}

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
  public IexHistoricalPrices(final String symbol, final BigDecimal close, final BigDecimal high,
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
   * Constructor from a database entry (IexHistoricalPricesDB).
   * @param entity IexHistoricalPricesDB entry
   */
  public IexHistoricalPrices(final IexHistoricalPricesDB entity) {
    this.symbol = entity.getSymbol();
    this.close = entity.getClose();
    this.high = entity.getHigh();
    this.low = entity.getLow();
    this.open = entity.getOpen();
    this.volume = entity.getVolume();
    this.date = entity.getDate();
  }
}
