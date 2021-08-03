package org.galatea.starter.service;

import java.util.List;
import org.galatea.starter.domain.IexHistoricalPrice;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * A Feign Declarative REST Client to access endpoints from the Free and Open IEX API to get market
 * data. Contains path for new API services IexClient class lacks.
 */
@FeignClient(name = "iexNew", url = "${spring.rest.iexNewBasePath}")
public interface IexNewClient {

  /**
   * Get historical data of up to 15 years & historical minute-by-minute intraday prices for the
   * last 30 days. See https://iexcloud.io/docs/api/#historical-prices.
   *
   * @return a list of historical data for each of the symbols passed in over the given time ranges.
   * @path symbol stock symbols to get historical data for.
   * @path range selection of time for which to get historical data.
   */
  @GetMapping(value = "/stock/{symbol}/chart/{range}")
  List<IexHistoricalPrice> getHistoricalPricesForSymbol(@PathVariable("symbol") String symbol,
      @PathVariable("range") String range, @RequestParam("token") String token);

}
