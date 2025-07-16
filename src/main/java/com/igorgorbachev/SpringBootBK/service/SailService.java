package com.igorgorbachev.SpringBootBK.service;

import com.igorgorbachev.SpringBootBK.entity.Sail;
import jakarta.servlet.http.HttpSession;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface SailService {
    void addSail(Sail sail);

    void changeSail(Sail sail);

    void deleteSail(Long id);

    List<Sail> getAllSail();

    Sail getSailById(Long id);

    List<Sail> getListSailByKlient(Long id);

    BigDecimal zarplataFromPeriod(LocalDate start, LocalDate end);

    List<Sail> getFilteredSails(Long klientFilter, HttpSession session);
    Map<String, Object> getWeekAttributes();
    void processAddSail(Sail sail, Long klientId, HttpSession session);
    void processChangeSail(Sail sail, Long klientId, String status, String oplata);

}
