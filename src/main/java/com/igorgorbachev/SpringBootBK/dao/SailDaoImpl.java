package com.igorgorbachev.SpringBootBK.dao;

import com.igorgorbachev.SpringBootBK.entity.Sail;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Repository;


import java.util.List;

@Repository
public class SailDaoImpl implements SailDao {

    private static final Logger logger = Logger.getLogger(SailDaoImpl.class);

    @PersistenceContext
    EntityManager entityManager;

    @Override
    public void addSail(Sail sail) {
        entityManager.persist(sail);
    }

    @Override
    public void changeSail(Sail sail) {
        logger.info("Sail в начале в методе changedSail в ДАО" + sail.getPrice());
        entityManager.merge(sail);
        logger.info("Sail в конце в методе changedSail в ДАО" + sail.getPrice());
    }

    @Override
    public void deleteSail(Sail sail) {
        entityManager.remove(entityManager.contains(sail) ? sail : entityManager.merge(sail));
    }

    @Override
    public List<Sail> getAllSail() {
       return entityManager.createQuery("from Sail", Sail.class).getResultList();
    }

    @Override
    public Sail getSailById(Long id) {
        return entityManager.find(Sail.class, id);
    }
}
