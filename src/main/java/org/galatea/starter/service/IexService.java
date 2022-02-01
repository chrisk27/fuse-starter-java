package org.galatea.starter.service;

import java.util.List;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.galatea.starter.domain.IexHistoricalPrices;
import org.galatea.starter.domain.IexLastTradedPrice;
import org.galatea.starter.domain.IexSymbol;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

/**
 * A layer for transformation, aggregation, and business required when retrieving data from IEX.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class IexService {

  @NonNull
  private IexClient iexClient;


  /**
   * Get all stock symbols from IEX.
   *
   * @return a list of all Stock Symbols from IEX.
   */
  public List<IexSymbol> getAllSymbols() {
    return iexClient.getAllSymbols();
  }

  /**
   * Get the last traded price for each Symbol that is passed in.
   *
   * @param symbols the list of symbols to get a last traded price for.
   * @return a list of last traded price objects for each Symbol that is passed in.
   */
  public List<IexLastTradedPrice> getLastTradedPriceForSymbols(final List<String> symbols) {
    if (CollectionUtils.isEmpty(symbols)) {
      throw new IllegalArgumentException("No Stock Symbol Provided.");
    } else {
      return iexClient.getLastTradedPriceForSymbols(symbols.toArray(new String[0]));
    }
  }

  /**
   * Get the historical price for each symbol passed in for the date passed in.
   *
   * @param symbol list of symbols to get historical price for.
   * @param range the range of time  (ex. "5m", "ytd" ) to get previous data (Optional).
   * @param date the date from which we would want to get the previous data from (Optional).
   *      Note: If neither optional parameter is used, the system will default to range = 1m.
   *      - default behavior of Iex Cloud API
   *      Note: If both optional parameters are provided, the system will default to the date.
   *      - can be changed later if needed: IexCloud will not allow both options in call to client
   * @return a IexHistoricalPrices objects for the given symbols.
   */
  public List<IexHistoricalPrices> getHistoricalPricesForSymbol(
      final String symbol, final String range, final String date) {
    if (symbol == null) {
      throw new IllegalArgumentException("No Stock Symbol Provided.");
    } else if (range == null && date == null) {
      String rangeVal = "1m";
      return iexClient.getHistoricalPricesForSymbolByRange(symbol, rangeVal);
    } else if (date == null) {
      return iexClient.getHistoricalPricesForSymbolByRange(symbol, range);
    } else {
      return iexClient.getHistoricalPricesForSymbolByDate(symbol, date);
    }
  }


}
