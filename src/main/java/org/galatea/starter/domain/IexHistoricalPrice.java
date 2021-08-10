package org.galatea.starter.domain;

import java.math.BigDecimal;
import javax.persistence.IdClass;
import lombok.Builder;
import lombok.Data;
import javax.persistence.Entity;
import javax.persistence.Id;

@Data
@Builder
@Entity
@IdClass(IexHistoricalPriceId.class)
public class IexHistoricalPrice {
  private BigDecimal close;
  private BigDecimal high;
  private BigDecimal low;
  private BigDecimal open;
  @Id
  private String symbol;
  private Integer volume;
  @Id
  private String date;

  protected IexHistoricalPrice(){}

  public IexHistoricalPrice(BigDecimal close, BigDecimal high, BigDecimal low, BigDecimal open, String symbol, Integer volume, String date){
    this.close = close;
    this.high = high;
    this.low = low;
    this.open = open;
    this.symbol = symbol;
    this.volume = volume;
    this.date = date;
  }
}