package org.galatea.starter.service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.galatea.starter.domain.IexHistoricalPrice;
import org.galatea.starter.domain.IexHistoricalPriceId;
import org.galatea.starter.domain.IexLastTradedPrice;
import org.galatea.starter.domain.IexSymbol;
import org.galatea.starter.domain.rpsy.IexHistoricalPriceRpsy;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

/**
 * A layer for transformation, aggregation, and business required when retrieving data from IEX.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class IexService {
  private static final String TOKEN = "pk_eab4a048d00140c197d90565e3c5161f";

  private static final String DATE_RANGE_YEAR_TO_DATE = "ytd";
  private static final String DATE_RANGE_DAILY = "d";
  private static final String DATE_RANGE_MONTHLY = "m";
  private static final String DATE_RANGE_YEARLY = "y";

  @NonNull
  private final IexClient iexClient;
  @NonNull
  private final IexNewClient iexNewClient;
  @NonNull
  private final IexHistoricalPriceRpsy historicalRspy;
  @NonNull
  private final List<LocalDate> holidays;

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
      return Collections.emptyList();
    } else {
      return iexClient.getLastTradedPriceForSymbols(symbols.toArray(new String[0]));
    }
  }

  /**
   * Get the historical price data for the passed in symbol over the passed in range.
   *
   * @param symbol stock symbols to get historical data for.
   * @param range selection of time for which to get historical data.
   * @return a list of historical price objects for the Symbol that is passed in covering the date
   *     range.
   */
  public List<IexHistoricalPrice> getHistoricalPricesForSymbol(final String symbol,
      final String range) {
    if ("".equals(symbol) || "".equals(range)) {
      return Collections.emptyList();
    } else {
      LocalDate today = LocalDate.now();
      LocalDate initialDate = calculateStartingDate(today, range);
      List<LocalDate> datesOverRange = initialDate.datesUntil(today).collect(Collectors.toList());
      List<IexHistoricalPrice> pricesOverRange = getLocalHistoricalPrices(symbol, datesOverRange);

      if(!pricesOverRange.isEmpty()){
        log.info("Elements for symbol: "+symbol+", range: "+range+" retrieved from local storage.");
        return pricesOverRange;
      }else{
        log.info("Elements for symbol: "+symbol+", range: "+range+" retrieved via IEX request, then stored locally.");
        return getIEXRequestedHistoricalPrices(symbol, range, datesOverRange);
      }
    }
  }

  /**
   * Checks for and gathers historical pricing data currently being stored locally
   *
   * @param symbol stock symbols to get historical data for.
   * @param datesOverRange list of days for which data is desired
   * @return List of historical prices if all desired data was found, or an empty list if not.
   */
  private List<IexHistoricalPrice> getLocalHistoricalPrices(final String symbol, final List<LocalDate> datesOverRange) {
    List<IexHistoricalPrice> pricesOverRange = new ArrayList<>();
    log.info(historicalRspy.findAll().toString());
    for (LocalDate date: datesOverRange) {
      if(holidays.contains(date) || date.getDayOfWeek() == DayOfWeek.SATURDAY || date.getDayOfWeek() == DayOfWeek.SUNDAY){
        continue;
      }
      IexHistoricalPriceId id = new IexHistoricalPriceId(symbol.toUpperCase(), date.toString());
      Optional<IexHistoricalPrice> datePrice = historicalRspy.findById(id);
      if(datePrice.isEmpty()){
        return new ArrayList<>();
      }else{
        pricesOverRange.add(datePrice.get());
      }
    }
    return pricesOverRange;
  }

  /**
   * Checks for and gathers historical pricing data currently being stored locally
   *
   * @param symbol stock symbols to get historical data for
   * @param range length of time for which to find historical data
   * @param datesOverRange list of days for which data is desired
   * @return List of historical prices for desired time period.
   */
  private List<IexHistoricalPrice> getIEXRequestedHistoricalPrices(final String symbol, final String range, final List<LocalDate> datesOverRange) {
    List<IexHistoricalPrice> pricesForSymbol = iexNewClient.getHistoricalPricesForSymbol(symbol, range, TOKEN);
    int dataCount = 0;
    for(LocalDate date : datesOverRange){
      if(holidays.contains(date) || date.getDayOfWeek() == DayOfWeek.SATURDAY || date.getDayOfWeek() == DayOfWeek.SUNDAY){
        continue;
      }
      IexHistoricalPrice historicalPrice = pricesForSymbol.get(dataCount);
      if(!date.toString().equals(historicalPrice.getDate())){
        holidays.add(date);
      }else{
        dataCount++;
        historicalRspy.save(historicalPrice);
      }
    }
    return pricesForSymbol;
  }

  /**
   * Calculates the starting date for traversing over the range given. Handles integer-day/month/year increments,
   *
   * @param today current date.
   * @param range length of time into the past in integer+day/month/year format.
   * @return starting date or null if cannot be calculated from given range.
   */
  private LocalDate calculateStartingDate(LocalDate today, String range){
    if(range.equals(DATE_RANGE_YEAR_TO_DATE)){
      return LocalDate.of(today.getYear(), 1, 1);
    }
    String[] rangeSplit = range.split("(?<=\\d)(?=\\D)");
    int timeSpanAmount = Integer.parseInt(rangeSplit[0]);
    String timeSpanSize = rangeSplit[1];

    LocalDate dateFrom;
    switch(timeSpanSize) {
      case DATE_RANGE_DAILY:
        dateFrom = today.minusDays(timeSpanAmount);
        break;
      case DATE_RANGE_MONTHLY:
        dateFrom = today.minusMonths(timeSpanAmount);
        break;
      case DATE_RANGE_YEARLY:
        dateFrom = today.minusYears(timeSpanAmount);
        break;
      default:
        dateFrom = null;
        break;
    }
    return dateFrom;
  }

}
