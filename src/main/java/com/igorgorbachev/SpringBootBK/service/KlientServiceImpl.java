package com.igorgorbachev.SpringBootBK.service;

import com.igorgorbachev.SpringBootBK.dao.CarDao;
import com.igorgorbachev.SpringBootBK.dao.KlientDao;
import com.igorgorbachev.SpringBootBK.entity.Car;
import com.igorgorbachev.SpringBootBK.entity.Klient;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.ArrayList;
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
        return klientDao.getAllKlients();
    }

    @Transactional
    @Override
    public void deleteKlient(Klient klient) {
        klientDao.deleteKlient(klient);
    }

    @Override
    public Klient getKlientById(Long id) {
        return klientDao.getKlientById(id);
    }


}
