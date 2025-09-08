package com.igorgorbachev.SpringBootBK.service;

import com.igorgorbachev.SpringBootBK.entity.Sail;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;



public interface SailService {
    void addSail(Sail sail);

    void changeSail(Sail sail);

    void deleteSail(Long sailId);

    List<Sail> getAllSail();

    Sail getSailById(Long id);

    List<Sail> getFilteredSails(Long klientId, Long statusId, Long oplataId);

    BigDecimal getZarplataForPeriod(LocalDate start, LocalDate end);
}
