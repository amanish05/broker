package org.mandrin.rain.broker.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "subscriptions", uniqueConstraints = @UniqueConstraint(columnNames = "instrument_token"))
@Data
@NoArgsConstructor
public class Subscription {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "instrument_token", nullable = false)
    private Long instrumentToken;
    private String tradingsymbol;
    private LocalDateTime subscribedAt;
}
