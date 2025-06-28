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
}
