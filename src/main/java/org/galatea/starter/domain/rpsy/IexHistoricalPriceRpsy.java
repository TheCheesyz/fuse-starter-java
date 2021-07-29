package org.galatea.starter.domain.rpsy;

import io.micrometer.core.lang.NonNullApi;
import java.util.List;
import java.util.Optional;
import lombok.NonNull;
import org.galatea.starter.domain.IexHistoricalPrice;
import org.galatea.starter.domain.SettlementMission;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.repository.CrudRepository;

public interface IexHistoricalPriceRpsy extends CrudRepository<IexHistoricalPrice, String> {

  /**
   * Retrieves all entities with the given symbol.
   */
  List<IexHistoricalPrice> findBySymbol(String symbol);
}
