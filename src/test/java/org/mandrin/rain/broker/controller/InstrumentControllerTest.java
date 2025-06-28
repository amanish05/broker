package org.mandrin.rain.broker.controller;

import org.junit.jupiter.api.Test;
import org.mandrin.rain.broker.model.Instrument;
import org.mandrin.rain.broker.service.InstrumentService;
import org.mandrin.rain.broker.repository.InstrumentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(InstrumentController.class)
class InstrumentControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private InstrumentService service;

    @Test
    void load_ShouldReturnOk() throws Exception {
        Instrument instr = new Instrument();
        instr.setInstrumentToken(1L);
        when(service.fetchAndSave(anyString())).thenReturn(List.of(instr));
        mockMvc.perform(post("/api/instruments/nse").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
        verify(service).fetchAndSave("nse");
    }

    @Test
    void exchanges_ShouldReturnList() throws Exception {
        when(service.listExchanges()).thenReturn(List.of("NSE"));
        mockMvc.perform(get("/api/instruments/exchanges"))
                .andExpect(status().isOk());
        verify(service).listExchanges();
    }

    @Test
    void names_ShouldReturnList() throws Exception {
        InstrumentRepository.NameTokenView view = mock(InstrumentRepository.NameTokenView.class);
        when(view.getInstrumentToken()).thenReturn(1L);
        when(view.getName()).thenReturn("ABC");
        when(service.listNameTokens()).thenReturn(List.of(view));
        mockMvc.perform(get("/api/instruments/names"))
                .andExpect(status().isOk());
        verify(service).listNameTokens();
    }
}
