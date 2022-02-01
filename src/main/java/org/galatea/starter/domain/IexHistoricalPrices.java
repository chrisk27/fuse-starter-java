package org.galatea.starter.domain;

import java.math.BigDecimal;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
/*import lombok.Builder;
import lombok.Data;

@Data
@Builder*/
@Entity
public class IexHistoricalPrices {

  @Id
  @GeneratedValue (strategy = GenerationType.AUTO)
  private Long id;
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

  @Override
  public String toString() {
    return String.format(
        "Entry[symbol='%s', date='%s', id=%d]",
        symbol, date, id);
  }

  /**
   * ID getter.
   * @return id
   */
  public Long getId() {
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
