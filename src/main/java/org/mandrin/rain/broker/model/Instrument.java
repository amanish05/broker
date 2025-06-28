package org.mandrin.rain.broker.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDate;

@Entity
@Table(name = "instruments")
public class Instrument {
    @Id
    private Long instrumentToken;
    private Long exchangeToken;
    private String tradingsymbol;
    private String name;
    private Double lastPrice;
    private LocalDate expiry;
    private Double strike;
    private Double tickSize;
    private Integer lotSize;
    private String instrumentType;
    private String segment;
    private String exchange;

    // getters and setters
    public Long getInstrumentToken() {
        return instrumentToken;
    }
    public void setInstrumentToken(Long instrumentToken) {
        this.instrumentToken = instrumentToken;
    }
    public Long getExchangeToken() {
        return exchangeToken;
    }
    public void setExchangeToken(Long exchangeToken) {
        this.exchangeToken = exchangeToken;
    }
    public String getTradingsymbol() {
        return tradingsymbol;
    }
    public void setTradingsymbol(String tradingsymbol) {
        this.tradingsymbol = tradingsymbol;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public Double getLastPrice() {
        return lastPrice;
    }
    public void setLastPrice(Double lastPrice) {
        this.lastPrice = lastPrice;
    }
    public LocalDate getExpiry() {
        return expiry;
    }
    public void setExpiry(LocalDate expiry) {
        this.expiry = expiry;
    }
    public Double getStrike() {
        return strike;
    }
    public void setStrike(Double strike) {
        this.strike = strike;
    }
    public Double getTickSize() {
        return tickSize;
    }
    public void setTickSize(Double tickSize) {
        this.tickSize = tickSize;
    }
    public Integer getLotSize() {
        return lotSize;
    }
    public void setLotSize(Integer lotSize) {
        this.lotSize = lotSize;
    }
    public String getInstrumentType() {
        return instrumentType;
    }
    public void setInstrumentType(String instrumentType) {
        this.instrumentType = instrumentType;
    }
    public String getSegment() {
        return segment;
    }
    public void setSegment(String segment) {
        this.segment = segment;
    }
    public String getExchange() {
        return exchange;
    }
    public void setExchange(String exchange) {
        this.exchange = exchange;
    }
}
