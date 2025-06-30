package org.mandrin.rain.broker.mock;

import lombok.extern.slf4j.Slf4j;
import org.mandrin.rain.broker.model.Instrument;
import org.mandrin.rain.broker.repository.InstrumentRepository;
import org.mandrin.rain.broker.repository.SubscriptionRepository;
import org.mandrin.rain.broker.websocket.TickerWebSocketHandler;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Mock ticker service that generates realistic price updates for subscribed instruments
 * when mock_session=true. This replaces the real KiteTicker WebSocket connection
 * with simulated market data that follows realistic price movement patterns.
 */
@Service
@ConditionalOnProperty(name = "kite.dev.mock_session", havingValue = "true")
@EnableAsync
@Slf4j
public class MockTickerService {
    
    private final TickerWebSocketHandler webSocketHandler;
    private final InstrumentRepository instrumentRepository;
    private final SubscriptionRepository subscriptionRepository;
    
    // Track current prices and price history for each instrument
    private final Map<Long, Double> currentPrices = new ConcurrentHashMap<>();
    private final Map<Long, Double> openPrices = new ConcurrentHashMap<>();
    private final Map<Long, Double> dayHighPrices = new ConcurrentHashMap<>();
    private final Map<Long, Double> dayLowPrices = new ConcurrentHashMap<>();
    private final Map<Long, Long> lastTickTime = new ConcurrentHashMap<>();
    
    // Price movement parameters for realistic simulation
    private final Random random = new Random();
    private boolean marketOpen = true;
    
    public MockTickerService(TickerWebSocketHandler webSocketHandler, 
                           InstrumentRepository instrumentRepository,
                           SubscriptionRepository subscriptionRepository) {
        this.webSocketHandler = webSocketHandler;
        this.instrumentRepository = instrumentRepository;
        this.subscriptionRepository = subscriptionRepository;
        
        log.info("MockTickerService initialized - will generate realistic price updates");
        initializePrices();
    }
    
    /**
     * Initialize current prices from database
     */
    private void initializePrices() {
        List<Instrument> instruments = instrumentRepository.findAll();
        for (Instrument instrument : instruments) {
            if (instrument.getLastPrice() != null && instrument.getLastPrice() > 0) {
                Long token = instrument.getInstrumentToken();
                Double price = instrument.getLastPrice();
                
                currentPrices.put(token, price);
                openPrices.put(token, price);
                dayHighPrices.put(token, price);
                dayLowPrices.put(token, price);
                lastTickTime.put(token, System.currentTimeMillis());
            }
        }
        log.info("Initialized prices for {} instruments", currentPrices.size());
    }
    
    /**
     * Generate realistic price ticks every 1-3 seconds for subscribed instruments
     */
    @Scheduled(fixedDelay = 2000, initialDelay = 5000)
    @Async
    public void generateMockTicks() {
        if (!marketOpen || webSocketHandler.getActiveSessionCount() == 0) {
            return;
        }
        
        // Get currently subscribed instruments
        Set<Long> subscribedTokens = new HashSet<>();
        subscriptionRepository.findAll().forEach(sub -> 
            subscribedTokens.add(sub.getInstrumentToken()));
        
        if (subscribedTokens.isEmpty()) {
            return;
        }
        
        // Generate ticks for random subset of subscribed instruments
        int tickCount = Math.min(subscribedTokens.size(), random.nextInt(5) + 1);
        List<Long> tokensToTick = new ArrayList<>(subscribedTokens);
        Collections.shuffle(tokensToTick);
        
        for (int i = 0; i < tickCount && i < tokensToTick.size(); i++) {
            Long token = tokensToTick.get(i);
            generateTickForInstrument(token);
        }
    }
    
    /**
     * Generate a realistic tick for a specific instrument
     */
    private void generateTickForInstrument(Long instrumentToken) {
        if (!currentPrices.containsKey(instrumentToken)) {
            return;
        }
        
        Optional<Instrument> instrumentOpt = instrumentRepository.findById(instrumentToken);
        if (instrumentOpt.isEmpty()) {
            return;
        }
        
        Instrument instrument = instrumentOpt.get();
        Double currentPrice = currentPrices.get(instrumentToken);
        Double openPrice = openPrices.get(instrumentToken);
        
        // Generate realistic price movement based on instrument type
        Double newPrice = generateRealisticPrice(currentPrice, instrument);
        
        // Update price tracking
        currentPrices.put(instrumentToken, newPrice);
        dayHighPrices.put(instrumentToken, Math.max(dayHighPrices.get(instrumentToken), newPrice));
        dayLowPrices.put(instrumentToken, Math.min(dayLowPrices.get(instrumentToken), newPrice));
        lastTickTime.put(instrumentToken, System.currentTimeMillis());
        
        // Generate market depth (buy/sell orders)
        Map<String, Object> depth = generateMarketDepth(newPrice, instrument.getTickSize());
        
        // Create realistic tick data structure similar to KiteTicker
        Map<String, Object> tick = createTickData(instrument, newPrice, openPrice, depth);
        
        log.debug("Generated tick for {}: {} (change: {:.2f})", 
                 instrument.getTradingsymbol(), newPrice, newPrice - currentPrice);
        
        // Broadcast to WebSocket clients
        webSocketHandler.broadcastTickerData(tick);
    }
    
    /**
     * Generate realistic price movement based on instrument characteristics
     */
    private Double generateRealisticPrice(Double currentPrice, Instrument instrument) {
        String instrumentType = instrument.getInstrumentType();
        Double tickSize = instrument.getTickSize();
        
        // Different volatility for different instrument types
        double volatilityFactor;
        double maxMovement;
        
        switch (instrumentType) {
            case "EQ": // Equity
                volatilityFactor = 0.002; // 0.2% typical movement
                maxMovement = 0.05; // Max 5% in one tick
                break;
            case "CE", "PE": // Options
                volatilityFactor = 0.05; // 5% typical movement (high volatility)
                maxMovement = 0.20; // Max 20% in one tick
                break;
            case "FUT": // Futures
                volatilityFactor = 0.003; // 0.3% typical movement
                maxMovement = 0.08; // Max 8% in one tick
                break;
            default:
                volatilityFactor = 0.002;
                maxMovement = 0.05;
        }
        
        // Generate price change with realistic distribution (mostly small moves)
        double changePercent;
        if (random.nextDouble() < 0.7) {
            // 70% small movements (within 1 standard deviation)
            changePercent = random.nextGaussian() * volatilityFactor;
        } else if (random.nextDouble() < 0.9) {
            // 20% medium movements (1-2 standard deviations)
            changePercent = random.nextGaussian() * volatilityFactor * 2;
        } else {
            // 10% large movements (up to max)
            changePercent = (random.nextDouble() - 0.5) * 2 * maxMovement;
        }
        
        // Apply the change
        double newPrice = currentPrice * (1 + changePercent);
        
        // Ensure minimum price movement is tick size
        if (Math.abs(newPrice - currentPrice) < tickSize) {
            newPrice = currentPrice + (random.nextBoolean() ? tickSize : -tickSize);
        }
        
        // Round to tick size
        newPrice = Math.round(newPrice / tickSize) * tickSize;
        
        // Ensure price stays positive and within reasonable bounds
        newPrice = Math.max(newPrice, tickSize);
        newPrice = Math.min(newPrice, currentPrice * 1.5); // Cap at 50% increase
        
        return newPrice;
    }
    
    /**
     * Generate realistic market depth (order book)
     */
    private Map<String, Object> generateMarketDepth(Double currentPrice, Double tickSize) {
        List<Map<String, Object>> buyOrders = new ArrayList<>();
        List<Map<String, Object>> sellOrders = new ArrayList<>();
        
        // Generate 5 levels each side
        for (int i = 1; i <= 5; i++) {
            // Buy orders (below current price)
            double buyPrice = currentPrice - (i * tickSize);
            buyOrders.add(Map.of(
                "price", Math.round(buyPrice * 100.0) / 100.0,
                "quantity", ThreadLocalRandom.current().nextInt(100, 5000),
                "orders", ThreadLocalRandom.current().nextInt(1, 20)
            ));
            
            // Sell orders (above current price)
            double sellPrice = currentPrice + (i * tickSize);
            sellOrders.add(Map.of(
                "price", Math.round(sellPrice * 100.0) / 100.0,
                "quantity", ThreadLocalRandom.current().nextInt(100, 5000),
                "orders", ThreadLocalRandom.current().nextInt(1, 20)
            ));
        }
        
        return Map.of(
            "buy", buyOrders,
            "sell", sellOrders
        );
    }
    
    /**
     * Create tick data structure compatible with frontend expectations
     */
    private Map<String, Object> createTickData(Instrument instrument, Double newPrice, 
                                              Double openPrice, Map<String, Object> depth) {
        Long token = instrument.getInstrumentToken();
        Double high = dayHighPrices.get(token);
        Double low = dayLowPrices.get(token);
        Double change = newPrice - openPrice;
        Double changePercent = (change / openPrice) * 100;
        
        Map<String, Object> tickData = new HashMap<>();
        tickData.put("instrument_token", token);
        tickData.put("exchange_token", instrument.getExchangeToken());
        tickData.put("tradingsymbol", instrument.getTradingsymbol());
        tickData.put("exchange", instrument.getExchange());
        tickData.put("last_price", Math.round(newPrice * 100.0) / 100.0);
        tickData.put("last_quantity", ThreadLocalRandom.current().nextInt(1, 1000));
        tickData.put("average_price", Math.round((newPrice + openPrice) / 2 * 100.0) / 100.0);
        tickData.put("volume", ThreadLocalRandom.current().nextInt(10000, 1000000));
        tickData.put("buy_quantity", ThreadLocalRandom.current().nextInt(1000, 50000));
        tickData.put("sell_quantity", ThreadLocalRandom.current().nextInt(1000, 50000));
        tickData.put("ohlc", Map.of(
            "open", Math.round(openPrice * 100.0) / 100.0,
            "high", Math.round(high * 100.0) / 100.0,
            "low", Math.round(low * 100.0) / 100.0,
            "close", Math.round(openPrice * 100.0) / 100.0 // Previous day close
        ));
        tickData.put("net_change", Math.round(change * 100.0) / 100.0);
        tickData.put("net_change_percentage", Math.round(changePercent * 100.0) / 100.0);
        tickData.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        tickData.put("depth", depth);
        tickData.put("oi", instrument.getInstrumentType().equals("FUT") ? 
                  ThreadLocalRandom.current().nextInt(10000, 1000000) : 0);
        tickData.put("mode", "full");
        
        return tickData;
    }
    
    /**
     * Market hours simulation (9:15 AM to 3:30 PM on weekdays)
     */
    @Scheduled(cron = "0 15 9 * * MON-FRI") // 9:15 AM weekdays
    public void openMarket() {
        marketOpen = true;
        log.info("Mock market opened - ticker generation started");
        
        // Reset day prices
        for (Long token : currentPrices.keySet()) {
            Double currentPrice = currentPrices.get(token);
            openPrices.put(token, currentPrice);
            dayHighPrices.put(token, currentPrice);
            dayLowPrices.put(token, currentPrice);
        }
    }
    
    @Scheduled(cron = "0 30 15 * * MON-FRI") // 3:30 PM weekdays  
    public void closeMarket() {
        marketOpen = false;
        log.info("Mock market closed - ticker generation stopped");
    }
    
    /**
     * For development - force market open state
     */
    public void forceMarketOpen() {
        marketOpen = true;
        log.info("Mock market forced open for development");
    }
    
    /**
     * Get current market status
     */
    public boolean isMarketOpen() {
        return marketOpen;
    }
    
    /**
     * Get current price for an instrument
     */
    public Double getCurrentPrice(Long instrumentToken) {
        return currentPrices.get(instrumentToken);
    }
}