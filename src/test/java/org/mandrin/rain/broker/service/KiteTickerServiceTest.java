package org.mandrin.rain.broker.service;

import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.Test;
import org.mandrin.rain.broker.config.KiteConstants;
import org.mockito.ArgumentCaptor;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class KiteTickerServiceTest {
    private void setField(Object target, String fieldName, Object value) {
        try {
            java.lang.reflect.Field f = target.getClass().getDeclaredField(fieldName);
            f.setAccessible(true);
            f.set(target, value);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void subscribe_ShouldCallKiteTicker() {
        KiteTickerService service = new KiteTickerService();
        setField(service, "apiKey", "key");
        var ticker = mock(com.zerodhatech.ticker.KiteTicker.class);
        when(ticker.isConnectionOpen()).thenReturn(true);
        setField(service, "kiteTicker", ticker);

        HttpSession session = mock(HttpSession.class);
        when(session.getAttribute(KiteConstants.KITE_ACCESS_TOKEN_SESSION)).thenReturn("token");

        service.subscribe(session, List.of(99L));

        ArgumentCaptor<java.util.ArrayList> captor = ArgumentCaptor.forClass(java.util.ArrayList.class);
        verify(ticker).subscribe(captor.capture());
        verify(ticker).setMode(captor.getValue(), com.zerodhatech.ticker.KiteTicker.modeFull);
    }

    @Test
    void connect_WithoutToken_ShouldThrow() {
        KiteTickerService service = new KiteTickerService();
        HttpSession session = mock(HttpSession.class);
        when(session.getAttribute(KiteConstants.KITE_ACCESS_TOKEN_SESSION)).thenReturn(null);
        assertThrows(IllegalStateException.class, () -> service.connect(session));
    }
}
