package com.igorgorbachev.SpringBootBK.dao;

import com.igorgorbachev.SpringBootBK.entity.Klient;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;


import java.util.List;

@Repository
public class KlientDaoImpl implements KlientDao{

    @PersistenceContext
    EntityManager entityManager;

    @Override
    public void addKlient(Klient klient) {
        entityManager.persist(klient);
    }

    @Override
    public void changeKlient(Klient klient) {
        entityManager.merge(klient);
    }

    @Override
    public List<Klient> getAllKlients() {
        return entityManager.createQuery("from Klient", Klient.class).getResultList();
    }
    @Override
    public void deleteKlient(Klient klient) {
        entityManager.remove(entityManager.contains(klient) ? klient : entityManager.merge(klient));
    }

    @Override
    public Klient getKlientById(Long id) {
        return entityManager.find(Klient.class, id);
    }
}
