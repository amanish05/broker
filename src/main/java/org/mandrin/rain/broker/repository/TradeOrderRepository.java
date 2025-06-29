package org.mandrin.rain.broker.repository;

import org.mandrin.rain.broker.model.TradeOrder;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TradeOrderRepository extends JpaRepository<TradeOrder, Long> {
}
