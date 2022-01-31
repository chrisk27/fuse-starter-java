package org.galatea.starter.entrypoint;

import java.util.List;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.sf.aspect4log.Log;
import net.sf.aspect4log.Log.Level;
import org.galatea.starter.domain.IexHistoricalPrices;
import org.galatea.starter.domain.IexLastTradedPrice;
import org.galatea.starter.domain.IexSymbol;
import org.galatea.starter.service.IexService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.CollectionUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@Slf4j
@Log(enterLevel = Level.INFO, exitLevel = Level.INFO)
@Validated
@RestController
@RequiredArgsConstructor
public class IexRestController extends ResponseEntityExceptionHandler {

  @NonNull
  private IexService iexService;

  /**
   * Exposes an endpoint to get all of the symbols available on IEX.
   *
   * @return a list of all IexStockSymbols.
   */
  @GetMapping(value = "${mvc.iex.getAllSymbolsPath}", produces = {MediaType.APPLICATION_JSON_VALUE})
  public List<IexSymbol> getAllStockSymbols() {
    return iexService.getAllSymbols();
  }

  /**
   * Get the last traded price for each of the symbols passed in.
   *
   * @param symbols list of symbols to get last traded price for.
   * @return a List of IexLastTradedPrice objects for the given symbols.
   */
  @GetMapping(value = "${mvc.iex.getLastTradedPricePath}", produces = {
      MediaType.APPLICATION_JSON_VALUE})
  public ResponseEntity getLastTradedPrice(
      @RequestParam(value = "symbols") final List<String> symbols) {
    if (CollectionUtils.isEmpty(symbols)) {
      return new ResponseEntity<>("No Stock Symbols Provided", HttpStatus.BAD_REQUEST);
    } else {
      return new ResponseEntity<>(iexService.getLastTradedPriceForSymbols(symbols), HttpStatus.OK);
    }
  }

  /**
   * Get the historical price for each symbol passed in for the date passed in.
   *
   * @param symbol list of symbols to get historical price for.
   * @param range the range of time  (ex. "5m", "ytd" ) to get previous data (Optional).
   * @param date the date from which we would want to get the previous data from (Optional).
   * Note: If neither optional parameter is used, the system will default to range = 1m.
   * @return a List of IexHistoricalPrices objects for the given symbols.
   */
  @GetMapping(value = "${mvc.iex.getHistoricalPricesPath}", produces = {
      MediaType.APPLICATION_JSON_VALUE})
  public ResponseEntity getHistoricalPrices(
      @RequestParam final String symbol,
      @RequestParam(name="range",required = false) String range,
      @RequestParam(name="date",required = false) String date) {
    if (symbol.isBlank()) {
      return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    } else {
      return new ResponseEntity<>(iexService.getHistoricalPricesForSymbol(symbol, range, date),
          HttpStatus.OK);
    }
  }

}
