package com.igorgorbachev.SpringBootBK.service;

import com.igorgorbachev.SpringBootBK.entity.Sail;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;


public interface SailService {
    void addSail(Sail sail);

    void changeSail(Sail sail);

    void deleteSail(Long sailId);

    List<Sail> getAllSail();

    Sail getSailById(Long id);

    public List<Sail> getFilteredSails(Long klientId, Long statusId, Long oplataId);

    BigDecimal getZarplataForPeriod(LocalDate start, LocalDate end);
}
