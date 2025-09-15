package com.igorgorbachev.SpringBootBK.service.impl;

import com.igorgorbachev.SpringBootBK.dao.SailRepository;
import com.igorgorbachev.SpringBootBK.entity.Klient;
import com.igorgorbachev.SpringBootBK.entity.Oplata;
import com.igorgorbachev.SpringBootBK.entity.Sail;
import com.igorgorbachev.SpringBootBK.entity.Status;
import com.igorgorbachev.SpringBootBK.service.KlientService;
import com.igorgorbachev.SpringBootBK.service.OplataService;
import com.igorgorbachev.SpringBootBK.service.SailService;
import com.igorgorbachev.SpringBootBK.service.StatusService;
//import com.igorgorbachev.SpringBootBK.service.command.Command;
//import com.igorgorbachev.SpringBootBK.service.command.CommandFactory;
//import com.igorgorbachev.SpringBootBK.service.command.UndoManager;
import com.igorgorbachev.SpringBootBK.specification.SailSpecifications;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;


import javax.swing.undo.UndoManager;
import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class SailServiceImpl implements SailService {

    private final SailRepository sailRepository;
    private final KlientService klientService;
    private final StatusService statusService;
    private final OplataService oplataService;

//    private final CommandFactory  commandFactory;
//    private final UndoManager undoManager;


    public void calculate(Sail sail) {
        BigDecimal zakupka = sail.getZakupka().multiply(sail.getKolichestvo());
        BigDecimal prodaja = sail.getPrice().multiply(sail.getKolichestvo());
        BigDecimal dohod = prodaja.subtract(zakupka);
        BigDecimal nalog = dohod.multiply(sail.getNds());
        BigDecimal pribil = dohod.subtract(nalog);
        BigDecimal zarplata = pribil.multiply(new BigDecimal("0.25"));
        sail.setSumma(sail.getKolichestvo().multiply(sail.getPrice()));
        sail.setNalog(nalog);
        sail.setPribil(pribil);
        sail.setZarplata(zarplata);
    }

//    @Override
//    public void saveSail(Sail sail) {
//        calculate(sail);
//        sailRepository.save(sail);
//    }
//
//    @Override
//    public void executeAddSailCommand(Sail sail, Long klientId) {
//        Command command = commandFactory.createAddSailCommand(this, sail, klientId);
//        undoManager.executeCommand(command);
//    }
//
//    @Override
//    public void executeEditSailCommand(Sail sail, Long statusId, Long oplataId,
//                                       String nameSail, String articul,
//                                       BigDecimal zakupka, BigDecimal price, BigDecimal kolichestvo) {
//        Command command = commandFactory.createEditSailCommand(this, sail, statusId, oplataId,
//                nameSail, articul, zakupka, price, kolichestvo);
//        undoManager.executeCommand(command);
//    }
//
//    @Override
//    public void executeDeleteSailCommand(Long sailId) {
//        Command command = commandFactory.createDeleteSailCommand(this, sailId);
//        undoManager.executeCommand(command);
//    }



    @Transactional
    @Override
    public void addSail(Sail sail, Long klientId) {
        Klient klient = klientService.getKlientById(klientId);
        Status status = statusService.getStatusById(1L);
        Oplata oplata = oplataService.findOplatasById(4L);

        sail.setKlient(klient);
        sail.setStatus(status);
        sail.setOplata(oplata);
        sail.setToDay(LocalDate.now());

        calculate(sail);
        sailRepository.save(sail);
    }




    @Transactional
    @Override
    public void changeSail(Sail sail, Long statusId, Long oplataId,
                           String nameSail, String articul,
                           BigDecimal zakupka, BigDecimal price, BigDecimal kolichestvo) {
        if (nameSail != null) sail.setNameSail(nameSail);
        if (articul != null) sail.setArticul(articul);
        if (zakupka != null) sail.setZakupka(zakupka);
        if (price != null) sail.setPrice(price);
        if (kolichestvo != null) sail.setKolichestvo(kolichestvo);

        Status status = statusService.getStatusById(statusId);
        Oplata oplata = oplataService.findOplatasById(oplataId);

        sail.setStatus(status);
        sail.setOplata(oplata);
        sail.setToDay(sail.getToDay());

        calculate(sail);
        sailRepository.save(sail);
    }


    @Transactional
    @Override
    public void deleteSail(Long sailId) {
        Sail sail = sailRepository.findById(sailId)
                .orElseThrow(() -> new IllegalArgumentException("Sail with id " + sailId + " not found"));

        sailRepository.delete(sail);
    }


    @Transactional
    @Override
    public List<Sail> getAllSail() {
        return sailRepository.findAll();
    }


    @Transactional
    @Override
    public Sail getSailById(Long id) {
        return sailRepository.findSailById(id);
    }


    @Transactional
    @Override
    public List<Sail> getFilteredSails(Long klientId, Long statusId, Long oplataId) {
        Specification<Sail> spec = Specification.where(null);

        if (klientId != null) {
            spec = spec.and(SailSpecifications.withKlientId(klientId));
        }
        if (statusId != null) {
            spec = spec.and(SailSpecifications.withStatusId(statusId));
        }
        if (oplataId != null) {
            spec = spec.and(SailSpecifications.withOplataId(oplataId));
        }

        return sailRepository.findAll(spec);
    }


    @Transactional
    @Override
    public BigDecimal getZarplataForPeriod(LocalDate start, LocalDate end) {
        return sailRepository.getZarplataForPeriod(start, end);
    }

    @Transactional
    @Override
    public Map<String, Object> getSailViewData(Long klientFilter, Long statusFilter, Long oplataFilter, Pageable pageable) {

        Map<String, Object> modelData = new HashMap<>();

        Page<Sail> sailPage = sailRepository.findFilteredSails(klientFilter, statusFilter, oplataFilter, pageable);

        modelData.put("sailList", sailPage.getContent());
        modelData.put("currentPage", sailPage.getNumber());
        modelData.put("totalPages", sailPage.getTotalPages());
        modelData.put("totalItems", sailPage.getTotalElements());
        modelData.put("pageSize", pageable.getPageSize());

        modelData.put("statusColors", Map.of(
                "В пути", "ff0000",
                "Приехал, Не выдан", "ffc107",
                "Выдан", "28a745",
                "Возврат", "007bff"));

        modelData.put("oplataColors", Map.of(
                "Не оплачено", "ff0000",
                "Наличные", "007bff",
                "Терминал", "ffc107",
                "Безнал (счет ОПЛАЧЕН)", "28a745",
                "Наличные + чек", "28a745",
                "Терминал + чек", "28a745",
                "Безнал (добавил в счет)", "808080",
                "Безнал (счет выставлен)", "ffc107"
        ));

        modelData.put("klientList", klientService.getAllSortedKlients());
        modelData.put("statusList", statusService.getAllStatus());
        modelData.put("oplataList", oplataService.findAll());
        modelData.put("currentKlientFilter", klientFilter);
        modelData.put("currentStatusFilter", statusFilter);
        modelData.put("currentOplataFilter", oplataFilter);

        return modelData;
    }

    @Override
    public void addWeeklySalaryData(Model model) {
        LocalDate today = LocalDate.now();
        log.info("START addWeeklySalary today = {}", today);

        LocalDate weekStart = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        log.info("START addWeeklySalary weekStart = {}", weekStart);

        LocalDate weekEnd = today.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));
        log.info("START addWeeklySalary weekEnd = {}", weekEnd);

        BigDecimal weeklySalary = getZarplataForPeriod(weekStart, weekEnd);
        log.info("START addWeeklySalary weeklySalary = {}", weeklySalary);

        model.addAttribute("weeklySalary", weeklySalary);
        model.addAttribute("weekStart", weekStart);
        model.addAttribute("weekEnd", weekEnd);
    }


}