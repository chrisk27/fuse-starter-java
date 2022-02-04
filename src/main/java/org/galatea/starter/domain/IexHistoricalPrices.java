package org.galatea.starter.domain;

import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class IexHistoricalPrices {

  private String symbol;
  private BigDecimal close;
  private BigDecimal high;
  private BigDecimal low;
  private BigDecimal open;
  private long volume;
  private String date;

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
