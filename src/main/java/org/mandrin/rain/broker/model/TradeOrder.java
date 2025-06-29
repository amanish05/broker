package org.mandrin.rain.broker.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "trade_orders")
@Data
@NoArgsConstructor
public class TradeOrder {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long instrumentToken;
    private String tradingsymbol;
    private String exchange;
    private String transactionType;
    private Integer quantity;
    private Double price;
    private String orderId;
    private LocalDateTime placedAt;
}
