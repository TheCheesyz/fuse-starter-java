package org.galatea.starter.domain;

import java.math.BigDecimal;
import lombok.Builder;
import lombok.Data;
import javax.persistence.Entity;
import javax.persistence.Id;

@Data
@Builder
@Entity
public class IexHistoricalPrice {
  @Id
  private String symbolAndDate;
  private BigDecimal close;
  private BigDecimal high;
  private BigDecimal low;
  private BigDecimal open;
  private String symbol;
  private Integer volume;
  private String date;

  protected IexHistoricalPrice(){}

  public IexHistoricalPrice(String symbolAndDate, BigDecimal close, BigDecimal high, BigDecimal low, BigDecimal open, String symbol, Integer volume, String date){
    this.close = close;
    this.high = high;
    this.low = low;
    this.open = open;
    this.symbol = symbol;
    this.volume = volume;
    this.date = date;
    this.symbolAndDate = symbol + date;
  }
}