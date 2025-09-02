package com.igorgorbachev.SpringBootBK.dao.impl;

import com.igorgorbachev.SpringBootBK.dao.OplataDao;
import com.igorgorbachev.SpringBootBK.entity.Oplata;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class OplataDaoImpl implements OplataDao {

    @PersistenceContext
    EntityManager entityManager;


    @Override
    public List<Oplata> getAllOplata() {
        return entityManager.createQuery("FROM Oplata ", Oplata.class).getResultList();
    }

    @Override
    public Oplata getOplataById(Long id) {
        return entityManager.find(Oplata.class, id);
    }
}
