package com.igorgorbachev.SpringBootBK.service;

import com.igorgorbachev.SpringBootBK.dao.KlientDao;
import com.igorgorbachev.SpringBootBK.entity.Klient;
import jakarta.transaction.Transactional;
import org.apache.log4j.Logger;
import org.hibernate.Hibernate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

@Service
public class KlientServiceImpl implements KlientService {
    private static final Logger logger = Logger.getLogger(KlientServiceImpl.class);

    @Autowired
    private KlientDao klientDao;

    @Override
    @Transactional
    public void addKlient(Klient klient) {
        klientDao.addKlient(klient);
    }

    @Override
    @Transactional
    public void changeKlient(Klient klient) {
        klientDao.changeKlient(klient);
    }


    @Override
    @Transactional
    public List<Klient> getAllSortedKlients() {
        List<Klient> clients = klientDao.getAllKlients();
        clients.sort(Comparator.comparing(Klient::getName, String.CASE_INSENSITIVE_ORDER));
        return clients;
    }

    @Override
    @Transactional
    public void deleteKlientWithValidation(Long klientId) {
        // Загружаем клиента вместе с автомобилями
        Klient klient = klientDao.getKlientById(klientId);

        // Явно инициализируем коллекцию автомобилей
        Hibernate.initialize(klient.getCar());

        if (klient.getCar() != null && !klient.getCar().isEmpty()) {
            throw new IllegalStateException("Клиента нельзя удалить, так как у него есть автомобили.");
        }

        klientDao.deleteKlient(klient);
    }

    @Override
    @Transactional
    public Klient getKlientById(Long id) {
        return klientDao.getKlientById(id);
    }
}
