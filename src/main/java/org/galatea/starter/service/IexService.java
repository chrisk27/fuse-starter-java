package org.galatea.starter.service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.function.Predicate;
import java.util.regex.*;
import java.util.List;
import java.util.stream.Collectors;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.galatea.starter.domain.HistoricalPricesRepository;
import org.galatea.starter.domain.IexHistoricalPrices;
import org.galatea.starter.domain.IexHistoricalPricesDB;
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

  @NonNull
  private HistoricalPricesRepository repository;


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
   * @param symbol Stock symbol to get historical price for.
   * @param range the range of time  (ex. "5m", "ytd" ) to get previous data (Optional).
   * @param date the date from which we would want to get the previous data from (Optional).
   *      Note: If neither optional parameter is used, the system will default to range = 1m.
   *      - default behavior of Iex Cloud API
   *      Note: If both optional parameters are provided, the system will default to the date.
   *      - can be changed later if needed: IexCloud will not allow both options in call to client
   * @return a list of IexHistoricalPrices objects for the given symbols.
   */
  public List<IexHistoricalPrices> getHistoricalPricesForSymbol(
      final String symbol, final String range, final String date) {

    if (symbol == null) {
      throw new IllegalArgumentException("No Stock Symbol Provided.");
    } else if (!isSymbolInDatabase(symbol)) {
      //if symbol isn't in database, call Iex normally
      return getHistoricalPricesFromIex(symbol, range, date);
    }
    else if (date != null) {
      if (!isDateWeekend(date)) {
        return getHistoricalPriceBySymbolAndDate(symbol, date);
      } else {
        return Collections.emptyList();
      }
    } else {
      // Parse range into a list of dates and run them through getHistoricalPriceBySymbolAndDate
      List<String> datesToLookThrough = rangeToDateList(range);
      List<IexHistoricalPrices> outList = new ArrayList<>();
      for (String dateVal : datesToLookThrough) {
        List<IexHistoricalPrices> out = getHistoricalPriceBySymbolAndDate(symbol, dateVal);
        if (!out.isEmpty()) {
          outList.add(out.get(0));
        }
      }
      return outList;
    }
  }

  /**
   * Check if the date is a weekend.
   * @param date date (string formatted YYYYMMDD) we are considering
   * @return boolean: true if the date is a weekend, false otherwise
   */
  public boolean isDateWeekend(final String date) {
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
    LocalDate dateToCheck = LocalDate.parse(date, formatter);

    DayOfWeek d = dateToCheck.getDayOfWeek();
    return (d == DayOfWeek.SATURDAY || d == DayOfWeek.SUNDAY);
  }

  /**
   * Converts a range string into a list of dates (string) in YYYYMMDD format.
   * Will ignore weekend dates. Later, will need to also remove bank holidays.
   *
   * @param range The range of time we are interested. Input options are:
   *      - "5y", "2m", "7d": x number of years(y)/months(m)/days(d)
   *      - "ytd": year to date
   *
   * @return a list of valid date strings (YYYYMMDD) in the range
   */
  public List<String> rangeToDateList(final String range) {
    LocalDate endDate = LocalDate.now();
    LocalDate startDate;

    if (range.equalsIgnoreCase("ytd")) {
      startDate = LocalDate.ofYearDay(endDate.getYear(), 1);
    } else {
      // Confirms range is valid entry
      String pattern = "\\d+[dmyDMY]";
      Pattern p = Pattern.compile(pattern);
      Matcher m = p.matcher(range);
      if (!m.matches()) {
        throw new IllegalArgumentException("Improper Range Provided.");
      }
      // Parses string to correct start date
      String tp = range.substring(range.length() - 1);
      int numOfTime = Integer.parseInt(range.substring(0,range.length()-1));
      if (tp.equalsIgnoreCase("d")) {
        startDate = endDate.minusDays(numOfTime);
      } else if (tp.equalsIgnoreCase("m")) {
        startDate = endDate.minusMonths(numOfTime);
      } else {
        startDate = endDate.minusYears(numOfTime);
      }
    }

    // Predicate for checking on weekends
    Predicate<LocalDate> isWeekend = date -> date.getDayOfWeek() == DayOfWeek.SATURDAY
        || date.getDayOfWeek() == DayOfWeek.SUNDAY;

    // List of Business Days
    List<LocalDate> businessDays = startDate.datesUntil(endDate)
        .filter(isWeekend.negate())
        .collect(Collectors.toList());

    // Convert to list of strings in proper format
    List<String> dateList = new ArrayList<>();
    for (LocalDate lD : businessDays) {
      dateList.add(lD.format(DateTimeFormatter.ofPattern("yyyyMMdd")));
    }
    return dateList;
  }

  /**
   * Chooses whether to get the historical price for the symbol from the repository or Iex.
   * Precedence is always given to the repository, unless is not found within it.
   *
   * @param symbol Stock symbol to get the historical price for.
   * @param date Input date (formatted YYYYMMDD) to get price data for.
   *      Note: We will convert this into YYYY-MM-DD format for repository search.
   *      - Format of Iex API output
   * @return a list (length = 1) of IexHistoricalPrices objects.
   */
  public List<IexHistoricalPrices> getHistoricalPriceBySymbolAndDate(
      final String symbol, final String date) {
    //First, try the database
    List<IexHistoricalPrices> listObjConvertedFromDB = new ArrayList<>();
    List<IexHistoricalPricesDB> dbMatch =
        repository.findBySymbolAndDate(symbol, convertDateFormatToOutput(date));

    if (!dbMatch.isEmpty()) {
      listObjConvertedFromDB.add(new IexHistoricalPrices(dbMatch.get(0)));
      return listObjConvertedFromDB;
    } else {
      //If not in database, call from Iex, insert into database, and return
      List<IexHistoricalPrices> call = iexClient.getHistoricalPricesForSymbolByDate(symbol, date);
      if (!call.isEmpty()) {
        repository.save(new IexHistoricalPricesDB(call.get(0)));
      }
      return call;
    }
  }

  /**
   * Converts input date (format:YYYYMMDD) to output date (format:YYYY-MM-DD).
   * @param inputDate date string in YYYYMMDD format
   * @return outputDate string in YYYY-MM-DD format.
   */
  public String convertDateFormatToOutput(final String inputDate) {
    StringBuilder builder = new StringBuilder(inputDate);
    builder.insert(6,'-');
    builder.insert(4,'-');
    return builder.toString();
  }

  /**
   * Calls Iex API client to obtain historical prices for full query.
   *
   * @param symbol list of symbols to get historical price for.
   * @param range the range of time  (ex. "5m", "ytd" ) to get previous data (Optional).
   * @param date the date from which we would want to get the previous data from (Optional).
   *      Note: If neither optional parameter is used, the system will default to range = 1m.
   *      - default behavior of Iex Cloud API
   *      Note: If both optional parameters are provided, the system will default to the date.
   *      - can be changed later if needed: IexCloud will not allow both options in call to client
   * @return a list of IexHistoricalPrices objects for the given symbols.
   */
  public List<IexHistoricalPrices> getHistoricalPricesFromIex(
      final String symbol, final String range, final String date) {
    List<IexHistoricalPrices> pricesFromIexApi
        = getFromIex(symbol, range, date);

    for (IexHistoricalPrices entity : pricesFromIexApi) {
      repository.save(new IexHistoricalPricesDB(entity));
    }
    return pricesFromIexApi;
  }


  /**
   * Checks in database if there are any entries with the stock.
   *
   * @param symbol Stock symbol to get historical price for
   *
   * @return true if there is an entry of a stock in the database, false otherwise
   */
  public boolean isSymbolInDatabase(final String symbol) {
    return repository.existsBySymbol(symbol);
  }

}
