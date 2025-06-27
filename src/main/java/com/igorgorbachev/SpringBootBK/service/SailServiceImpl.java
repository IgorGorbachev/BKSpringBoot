package com.igorgorbachev.SpringBootBK.service;

import com.igorgorbachev.SpringBootBK.dao.SailDao;
import com.igorgorbachev.SpringBootBK.entity.Sail;

import jakarta.transaction.Transactional;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


import java.math.BigDecimal;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

@Service
public class SailServiceImpl implements SailService {

    private static final Logger logger = Logger.getLogger(SailServiceImpl.class);

    @Autowired
    SailDao sailDao;

    @Transactional
    @Override
    public void addSail(Sail sail) {
        sail.setPrice(sail.calculatePriceFromZakupkaAndNacenka());

        BigDecimal price = sail.getZakupka().multiply(sail.getNacenka()).add(sail.getZakupka());
        sail.setPrice(price);

        BigDecimal summa = price.multiply(BigDecimal.valueOf(sail.getKolichestvo()));
        sail.setSumma(summa);

        BigDecimal zakupkaTotal = sail.getZakupka().multiply(BigDecimal.valueOf(sail.getKolichestvo()));
        BigDecimal nalog = summa.subtract(zakupkaTotal).multiply(sail.getNds());
        sail.setNalog(nalog);

        BigDecimal pribil = summa.subtract(zakupkaTotal).subtract(nalog);
        sail.setPribil(pribil);

        BigDecimal zarplata = pribil.multiply(BigDecimal.valueOf(0.5));
        sail.setZarplata(zarplata);

        sailDao.addSail(sail);
    }

    @Transactional
    @Override
    public void changeSail(Sail sail) {
        logger.info("Sail в начале в методе changedSail в СЕРВИСЕ" + sail.getPrice());


        BigDecimal summa = sail.getPrice().multiply(BigDecimal.valueOf(sail.getKolichestvo()));
        sail.setSumma(summa);

        BigDecimal zakupkaTotal = sail.getZakupka().multiply(BigDecimal.valueOf(sail.getKolichestvo()));
        BigDecimal nalog = summa.subtract(zakupkaTotal).multiply(sail.getNds());
        sail.setNalog(nalog);

        BigDecimal pribil = summa.subtract(zakupkaTotal).subtract(nalog);
        sail.setPribil(pribil);

        BigDecimal zarplata = pribil.multiply(BigDecimal.valueOf(0.5));
        sail.setZarplata(zarplata);

        sailDao.changeSail(sail);
        logger.info("Sail в конце в методе changedSail В СЕРВИСЕ " + sail.getPrice());
    }

    @Transactional
    @Override
    public void deleteSail(Sail sail) {
        sailDao.deleteSail(sail);
    }

    @Transactional
    @Override
    public List<Sail> getAllSail() {
        List<Sail> sailList = sailDao.getAllSail();
        Collections.sort(sailList, Comparator.comparing(Sail::getId));
        return sailList;
    }

    @Transactional
    @Override
    public Sail getSailById(Long id) {
        return sailDao.getSailById(id);
    }



}
