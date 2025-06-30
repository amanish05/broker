package org.mandrin.rain.broker.repository;

import org.mandrin.rain.broker.model.Instrument;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;

public interface InstrumentRepository extends JpaRepository<Instrument, Long> {

    @Query("select distinct i.exchange from Instrument i")
    List<String> findDistinctExchange();

    interface NameTokenView {
        Long getInstrumentToken();
        String getName();
    }

    @Query("select i.instrumentToken as instrumentToken, i.name as name from Instrument i")
    List<NameTokenView> findNameTokenAll();

    @Query("select distinct i.instrumentType from Instrument i where i.exchange = :exchange")
    List<String> findDistinctInstrumentType(String exchange);

    @Query("select i.instrumentToken as instrumentToken, i.name as name from Instrument i where i.exchange = :exchange and i.instrumentType = :type")
    List<NameTokenView> findNameToken(String exchange, String type);
    
    List<Instrument> findByExchange(String exchange);
    
    // Methods for underlying asset filtering (NIFTY, BANKNIFTY, etc.)
    @Query("select distinct substring(i.name, 1, locate(' ', i.name) - 1) from Instrument i where i.name like '%NIFTY%' or i.name like '%BANKNIFTY%' or i.name like '%FINNIFTY%' or i.name like '%RELIANCE%' order by substring(i.name, 1, locate(' ', i.name) - 1)")
    List<String> findDistinctUnderlyingAssets();
    
    @Query("select i from Instrument i where i.name like concat('%', :underlying, '%') and i.expiry is not null")
    List<Instrument> findByUnderlyingAsset(String underlying);
    
    @Query("select distinct i.expiry from Instrument i where i.name like concat('%', :underlying, '%') and i.expiry is not null order by i.expiry")
    List<java.time.LocalDate> findDistinctExpiryByUnderlying(String underlying);
    
    @Query("select i from Instrument i where i.name like concat('%', :underlying, '%') and i.expiry = :expiry")
    List<Instrument> findByUnderlyingAndExpiry(String underlying, java.time.LocalDate expiry);
}
