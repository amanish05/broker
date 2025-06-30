package org.mandrin.rain.broker.service;

import org.junit.jupiter.api.Test;
import org.mandrin.rain.broker.model.Instrument;
import org.mandrin.rain.broker.repository.InstrumentRepository;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeFunction;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class InstrumentServiceTest {
    @Test
    void fetchAndSave_ShouldParseCsvAndPersist() throws Exception {
        String csv = "instrument_token,exchange_token,tradingsymbol,name,last_price,expiry,strike,tick_size,lot_size,instrument_type,segment,exchange\n" +
                "1,1,AAA,AAA,100,,0,0.05,1,EQ,NSE,NSE";
        ExchangeFunction fn = mock(ExchangeFunction.class);
        ClientResponse resp = ClientResponse.create(HttpStatus.OK).body(csv).build();
        when(fn.exchange(any(ClientRequest.class))).thenReturn(Mono.just(resp));
        WebClient client = WebClient.builder().exchangeFunction(fn).build();
        InstrumentRepository repo = mock(InstrumentRepository.class);
        
        // Mock the individual save calls used by the updated service
        when(repo.save(any(Instrument.class))).thenAnswer(inv -> inv.getArgument(0));
        when(repo.findById(anyLong())).thenReturn(Optional.empty());
        when(repo.findByExchange(anyString())).thenReturn(List.of());
        
        InstrumentService service = new InstrumentService(client, repo);
        List<Instrument> list = service.fetchAndSave("nse");
        assertEquals(1, list.size());
        Instrument i = list.get(0);
        assertEquals(1L, i.getInstrumentToken());
        verify(repo, times(1)).save(any(Instrument.class));
    }

    @Test
    void listExchanges_ShouldDelegateToRepo() {
        ExchangeFunction fn = mock(ExchangeFunction.class);
        WebClient client = WebClient.builder().exchangeFunction(fn).build();
        InstrumentRepository repo = mock(InstrumentRepository.class);
        when(repo.findDistinctExchange()).thenReturn(List.of("NSE"));
        InstrumentService service = new InstrumentService(client, repo);
        List<String> result = service.listExchanges();
        assertEquals(1, result.size());
        verify(repo).findDistinctExchange();
    }

    @Test
    void listNameTokens_ShouldReturnValues() {
        ExchangeFunction fn = mock(ExchangeFunction.class);
        WebClient client = WebClient.builder().exchangeFunction(fn).build();
        InstrumentRepository repo = mock(InstrumentRepository.class);
        InstrumentRepository.NameTokenView view = mock(InstrumentRepository.NameTokenView.class);
        when(repo.findNameTokenAll()).thenReturn(List.of(view));
        InstrumentService service = new InstrumentService(client, repo);
        List<InstrumentRepository.NameTokenView> result = service.listNameTokens();
        assertEquals(1, result.size());
        verify(repo).findNameTokenAll();
    }

    @Test
    void listInstrumentTypes_ShouldDelegateToRepo() {
        ExchangeFunction fn = mock(ExchangeFunction.class);
        WebClient client = WebClient.builder().exchangeFunction(fn).build();
        InstrumentRepository repo = mock(InstrumentRepository.class);
        when(repo.findDistinctInstrumentType("NSE")).thenReturn(List.of("EQ"));
        InstrumentService service = new InstrumentService(client, repo);
        List<String> result = service.listInstrumentTypes("NSE");
        assertEquals(1, result.size());
        verify(repo).findDistinctInstrumentType("NSE");
    }

    @Test
    void listNames_ShouldDelegate() {
        ExchangeFunction fn = mock(ExchangeFunction.class);
        WebClient client = WebClient.builder().exchangeFunction(fn).build();
        InstrumentRepository repo = mock(InstrumentRepository.class);
        InstrumentRepository.NameTokenView view = mock(InstrumentRepository.NameTokenView.class);
        when(repo.findNameToken("NSE", "EQ")).thenReturn(List.of(view));
        InstrumentService service = new InstrumentService(client, repo);
        List<InstrumentRepository.NameTokenView> result = service.listNames("NSE", "EQ");
        assertEquals(1, result.size());
        verify(repo).findNameToken("NSE", "EQ");
    }
}
