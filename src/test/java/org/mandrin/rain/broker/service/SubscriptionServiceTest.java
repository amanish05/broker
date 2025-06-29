package org.mandrin.rain.broker.service;

import org.junit.jupiter.api.Test;
import org.mandrin.rain.broker.model.Subscription;
import org.mandrin.rain.broker.model.Instrument;
import org.mandrin.rain.broker.repository.InstrumentRepository;
import org.mandrin.rain.broker.repository.SubscriptionRepository;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SubscriptionServiceTest {
    @Test
    void saveAll_ShouldIgnoreDuplicates() {
        SubscriptionRepository repo = mock(SubscriptionRepository.class);
        InstrumentRepository instrumentRepo = mock(InstrumentRepository.class);
        when(repo.existsByInstrumentToken(1L)).thenReturn(false);
        Instrument inst = new Instrument();
        inst.setTradingsymbol("ABC");
        when(instrumentRepo.findById(1L)).thenReturn(Optional.of(inst));
        SubscriptionService service = new SubscriptionService(repo, instrumentRepo);
        service.saveAll(List.of(1L));
        verify(repo).save(any(Subscription.class));
    }

    @Test
    void listTokens_ShouldReturnAll() {
        SubscriptionRepository repo = mock(SubscriptionRepository.class);
        InstrumentRepository instrumentRepo = mock(InstrumentRepository.class);
        Subscription sub = new Subscription();
        sub.setInstrumentToken(1L);
        when(repo.findAll()).thenReturn(List.of(sub));
        SubscriptionService service = new SubscriptionService(repo, instrumentRepo);
        List<Long> list = service.listTokens();
        assertEquals(1, list.size());
        assertEquals(1L, list.get(0));
    }
}
