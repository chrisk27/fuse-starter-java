package org.galatea.starter.service;

import java.util.List;
import javax.websocket.server.PathParam;
import org.galatea.starter.domain.IexHistoricalPrices;
import org.galatea.starter.domain.IexLastTradedPrice;
import org.galatea.starter.domain.IexSymbol;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * A Feign Declarative REST Client to access endpoints from the Free and Open IEX API to get market
 * data. See https://iextrading.com/developer/docs/
 */
@FeignClient(name = "IEX", url = "${spring.rest.iexBasePath}")
public interface IexClient {

  /**
   * Get a list of all stocks supported by IEX. See https://iextrading.com/developer/docs/#symbols.
   * As of July 2019 this returns almost 9,000 symbols, so maybe don't call it in a loop.
   *
   * @return a list of all of the stock symbols supported by IEX.
   */
  @GetMapping("/ref-data/iex/symbols?token=${spring.application.iex_token}")
  List<IexSymbol> getAllSymbols();

  /**
   * Get the last traded price for each stock symbol passed in. See https://iextrading.com/developer/docs/#last.
   *
   * @param symbols stock symbols to get last traded price for.
   * @return a list of the last traded price for each of the symbols passed in.
   */
  @GetMapping("/tops/last?token=${spring.application.iex_token}")
  List<IexLastTradedPrice> getLastTradedPriceForSymbols(@RequestParam("symbols") String[] symbols);

  /**
   * Get the historical price for each symbol passed in for the date passed in.
   *
   * @param symbol symbol to get historical price for.
   * @param date the date (formatted YYYYMMDD) from which we want to get the historical prices for.
   * @return a IexHistoricalPrices objects for the given symbol.
   */
  @GetMapping("/stock/{symbol}/chart/date/{date}?token="
      + "${spring.application.iex_token}&chartByDay=true")
  List<IexHistoricalPrices> getHistoricalPricesForSymbolByDate(
      @PathVariable("symbol") String symbol,
      @PathVariable("date") String date);

  /**
   * Get the historical price for each symbol passed in for the date passed in.
   *
   * @param symbol symbol to get historical price for.
   * @param range the range of time  (ex. "5m", "ytd" ) to get previous data (Optional).
   *      Note: Will default to range=1m
   * @return a IexHistoricalPrices objects for the given symbol.
   */
  @GetMapping("/stock/{symbol}/chart/{range}?token=${spring.application.iex_token}")
  List<IexHistoricalPrices> getHistoricalPricesForSymbolByRange(
      @PathVariable("symbol") String symbol,
      @PathVariable("range") String range);
}
