package org.mandrin.rain.broker.service;

import org.mandrin.rain.broker.model.Subscription;
import org.mandrin.rain.broker.repository.InstrumentRepository;
import org.mandrin.rain.broker.repository.SubscriptionRepository;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class SubscriptionService {
    private final SubscriptionRepository repository;
    private final InstrumentRepository instrumentRepository;

    public void saveAll(List<Long> tokens) {
        for (Long t : tokens) {
            if (!repository.existsByInstrumentToken(t)) {
                Subscription s = new Subscription();
                s.setInstrumentToken(t);
                instrumentRepository.findById(t).ifPresent(i -> s.setTradingsymbol(i.getTradingsymbol()));
                s.setSubscribedAt(LocalDateTime.now());
                repository.save(s);
                log.info("Subscribed instrument {}", t);
            } else {
                log.debug("Instrument {} already subscribed", t);
            }
        }
    }

    public List<Long> listTokens() {
        List<Long> list = repository.findAll().stream().map(Subscription::getInstrumentToken).collect(Collectors.toList());
        log.debug("listTokens -> {}", list);
        return list;
    }
}
