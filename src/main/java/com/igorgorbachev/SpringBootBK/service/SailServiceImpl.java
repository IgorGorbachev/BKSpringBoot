package com.igorgorbachev.SpringBootBK.service;


import com.igorgorbachev.SpringBootBK.dao.SailDao;
import com.igorgorbachev.SpringBootBK.entity.Sail;
import jakarta.transaction.Transactional;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Map;


@Service
@Transactional
public class SailServiceImpl implements SailService {

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

    private static final Logger logger = Logger.getLogger(SailServiceImpl.class);

    @Autowired
    private SailDao sailDao;

    @Override
    public void addSail(Sail sail) {
        LocalDate today = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EE, dd.MM.yy", new Locale("ru"));
        String data = today.format(formatter).toUpperCase();
        sail.setToDay(data);
        calculate(sail);
        sailDao.addSail(sail);
    }

    @Override
    public void changeSail(Sail sail) {
        sail.setToDay(sail.getToDay());
        calculate(sail);
        sailDao.changeSail(sail);
    }

    @Override
    public void deleteSail(Long sailId) {
        sailDao.deleteSail(sailId);
    }

    @Override
    public List<Sail> getAllSail() {
        return sailDao.getAllSail();
    }

    @Override
    public Sail getSailById(Long id) {
        return sailDao.getSailById(id);
    }

    @Override
    public List<Sail> getFilteredSails(Long klientId, Long statusId, Long oplataId) {
        return sailDao.getFilteredSails(klientId, statusId, oplataId);
    }



}