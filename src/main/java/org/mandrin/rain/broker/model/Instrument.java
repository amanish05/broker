package org.mandrin.rain.broker.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Index;
import jakarta.persistence.UniqueConstraint;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;

@Entity
@Table(name = "instruments", 
       uniqueConstraints = @UniqueConstraint(columnNames = {"tradingsymbol", "exchange"}),
       indexes = {
           @Index(name = "idx_instrument_exchange", columnList = "exchange"),
           @Index(name = "idx_instrument_type", columnList = "instrumentType"),
           @Index(name = "idx_instrument_expiry", columnList = "expiry"),
           @Index(name = "idx_instrument_name", columnList = "name")
       })
@Data
@NoArgsConstructor
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
}
