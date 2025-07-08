package com.igorgorbachev.SpringBootBK.service;

import com.igorgorbachev.SpringBootBK.dao.KlientDao;
import com.igorgorbachev.SpringBootBK.entity.Klient;
import jakarta.transaction.Transactional;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


import java.util.Collections;
import java.util.Comparator;
import java.util.List;

@Service
public class KlientServiceImpl implements KlientService {

    private static final Logger logger = Logger.getLogger(KlientService.class);

    @Autowired
    KlientDao klientDao;

    @Transactional
    @Override
    public void addKlient(Klient klient) {
        klientDao.addKlient(klient);
    }


    @Transactional
    @Override
    public void changeKlient(Klient klient) {
        klientDao.changeKlient(klient);
    }

    @Transactional
    @Override
    public List<Klient> getAllKlients() {
        List<Klient> sortList = klientDao.getAllKlients();
        Collections.sort(sortList, Comparator.comparing(Klient::getName, String.CASE_INSENSITIVE_ORDER));
        return sortList;
    }

    @Transactional
    @Override
    public void deleteKlient(Klient klient) {
        if (klient.getCar() != null && !klient.getCar().isEmpty()) {
            throw new IllegalStateException("Клиента нельзя удалить, так как у него есть автомобили.");
        } else  {
            klientDao.deleteKlient(klient);
        }
    }

    @Override
    public Klient getKlientById(Long id) {
        return klientDao.getKlientById(id);
    }


}
