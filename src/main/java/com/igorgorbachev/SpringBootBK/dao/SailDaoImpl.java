package com.igorgorbachev.SpringBootBK.dao;

import com.igorgorbachev.SpringBootBK.entity.Sail;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;
import org.apache.log4j.Logger;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;


import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

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
    public void deleteSail(Long id) {
        entityManager.createQuery("DELETE FROM Sail WHERE id = :id")
                .setParameter("id", id)
                .executeUpdate();
    }

    @Override
    public List<Sail> getAllSail() {
        return entityManager.createQuery("from Sail", Sail.class).getResultList();
    }

    @Override
    public Sail getSailById(Long id) {
        return entityManager.find(Sail.class, id);
    }

    @Override
    public List<Sail> getListSailByKlient(Long id) {
        return entityManager.createQuery("SELECT s from Sail s where s.klient.id = :id", Sail.class)
                .setParameter("id", id)
                .getResultList();
    }

    @Override
    public BigDecimal zarplataFromPeriod(LocalDate start, LocalDate end) {
        Objects.requireNonNull(start, "Start date cannot be null");
        Objects.requireNonNull(end, "End date cannot be null");

        if (start.isAfter(end)) {
            logger.warn("Start date " + start + " is after end date " + end);
            return BigDecimal.ZERO;
        }

        logger.debug(String.format("Calculating salary from %s to %s", start, end));

        return entityManager.createQuery(
                        "SELECT COALESCE(SUM(s.zarplata), 0) FROM Sail s WHERE s.toDay BETWEEN :start AND :end",
                        BigDecimal.class)
                .setParameter("start", start)
                .setParameter("end", end)
                .getSingleResult();
    }
}

