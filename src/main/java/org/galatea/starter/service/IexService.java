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

  @NonNull
  private final IexClient iexClient;
  @NonNull
  private final IexNewClient iexNewClient;

  @NonNull
  IexHistoricalPriceRpsy historicalRspy;

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
      final String range, final String token) {
    if ("".equals(symbol) || "".equals(range) || "".equals(token)) {
      return Collections.emptyList();
    } else {

      boolean queryInDB = true;
      LocalDate today = LocalDate.now();
      LocalDate initialDate = calculateStartingDate(today, range);
      List<LocalDate> datesOverRange = initialDate.datesUntil(today).collect(Collectors.toList());
      List<IexHistoricalPrice> pricesOverRange = new ArrayList<>();
      for (LocalDate date: datesOverRange) {
        if(holidays.contains(date) || date.getDayOfWeek() == DayOfWeek.SATURDAY || date.getDayOfWeek() == DayOfWeek.SUNDAY){
          continue;
        }
        Optional<IexHistoricalPrice> datePrice = historicalRspy.findById(symbol.toUpperCase() + date);
        if(datePrice.isEmpty()){
          queryInDB = false;
          break;
        }else{
          pricesOverRange.add(datePrice.get());
        }
      }

      if(queryInDB){
        log.info("Elements for symbol: "+symbol+", range: "+range+" retrieved from local storage.");
        return pricesOverRange;
      }else{
        List<IexHistoricalPrice> pricesForSymbol = iexNewClient.getHistoricalPricesForSymbol(symbol, range, token);
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
            historicalPrice.setSymbolAndDate(historicalPrice.getSymbol()+historicalPrice.getDate());
            historicalRspy.save(historicalPrice);
          }
        }
        log.info("Elements for symbol: "+symbol+", range: "+range+" retrieved via IEX request, then stored locally.");
        return pricesForSymbol;
      }
    }
  }

  /**
   * Calculates the starting date for traversing over the range given. Handles integer-day/month/year increments,
   *
   * @param today current date.
   * @param range length of time into the past in integer+day/month/year format.
   * @return starting date or null if cannot be calculated from given range.
   */
  private LocalDate calculateStartingDate(LocalDate today, String range){
    if(range.equals("ytd")){
      return LocalDate.of(today.getYear(), 1, 1);
    }
    String[] rangeSplit = range.split("(?<=\\d)(?=\\D)");
    int timeSpanAmount = Integer.parseInt(rangeSplit[0]);
    char timeSpanSize = rangeSplit[1].charAt(0);

    LocalDate dateFrom;
    switch(timeSpanSize) {
      case 'd':
        dateFrom = today.minusDays(timeSpanAmount);
        break;
      case 'm':
        dateFrom = today.minusMonths(timeSpanAmount);
        break;
      case 'y':
        dateFrom = today.minusYears(timeSpanAmount);
        break;
      default:
        dateFrom = null;
        break;
    }
    return dateFrom;
  }

}
