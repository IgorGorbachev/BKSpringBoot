package com.igorgorbachev.SpringBootBK.service;

import com.igorgorbachev.SpringBootBK.entity.Sail;

import org.springframework.data.domain.Pageable;
import org.springframework.ui.Model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;


public interface SailService {
    void addSail(Sail sail, Long klientId);

    void changeSail(Sail sail, Long statusId, Long oplataId,
                    String nameSail, String articul,
                    BigDecimal zakupka, BigDecimal price, BigDecimal kolichestvo);

    void deleteSail(Long sailId);

    List<Sail> getAllSail();

    Sail getSailById(Long id);

    List<Sail> getFilteredSails(Long klientId, Long statusId, Long oplataId);

    BigDecimal getZarplataForPeriod(LocalDate start, LocalDate end);

    Map<String, Object> getSailViewData(Long klientFilter, Long statusFilter, Long oplataFilter, Pageable pageable);

    void addWeeklySalaryData(Model model);

//
//    void saveSail(Sail sail);
//
//    void executeAddSailCommand(Sail sail, Long klientId);
//
//    void executeEditSailCommand(Sail sail, Long statusId, Long oplataId,
//                                String nameSail, String articul,
//                                BigDecimal zakupka, BigDecimal price, BigDecimal kolichestvo);
//
//    void executeDeleteSailCommand(Long sailId);

}
