package org.galatea.starter.service;

import java.util.Collections;
import java.util.List;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.galatea.starter.domain.IexHistoricalPrices;
import org.galatea.starter.domain.IexLastTradedPrice;
import org.galatea.starter.domain.IexSymbol;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.web.client.HttpClientErrorException;

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
      throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, "Stock symbol was not provided.");
    } else {
      return iexClient.getLastTradedPriceForSymbols(symbols.toArray(new String[0]));
    }
  }

  /**
   * Get the historical price for each symbol passed in for the date passed in.
   *
   * @param symbol list of symbols to get historical price for.
   * @param dateOrRange the date (formatted YYYYMMDD) or the range of time  (ex. "5m", "ytd" ).
   * @return a IexHistoricalPrices objects for the given symbols.
   */
  public List<IexHistoricalPrices> getHistoricalPricesForSymbol(
      final String symbol, final String dateOrRange) {
    if (symbol.isEmpty()) {
      throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, "Stock symbol was not provided.");
    } else if (dateOrRange.isEmpty()) {
      throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, "Date or Range was not provided.");
    } else {
      return iexClient.getHistoricalPricesForSymbol(symbol, dateOrRange);
    }
  }

}
