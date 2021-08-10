package org.galatea.starter.domain;

import java.io.Serializable;
import lombok.Data;

@Data
public class IexHistoricalPriceId implements Serializable {
  private String symbol;
  private String date;

  protected IexHistoricalPriceId(){}

  public IexHistoricalPriceId(String symbol, String date){
    this.symbol = symbol;
    this.date = date;
  }

}