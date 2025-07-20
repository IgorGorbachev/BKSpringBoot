package com.igorgorbachev.SpringBootBK.dao;

import com.igorgorbachev.SpringBootBK.entity.Sail;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import jakarta.persistence.TypedQuery;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
        logger.info("Sail в начале в методе changedSail в ДАО " + sail);
        entityManager.merge(sail);
        logger.info("Sail в конце в методе changedSail в ДАО " + sail);
    }

    @Override
    public void deleteSail(Long sailId) {

        Sail sail = entityManager.find(Sail.class, sailId);
        if (sail != null) {
            entityManager.remove(sail);
        }
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
    public List<Sail> getFilteredSails(Long klientId, Long statusId, Long oplataId) {
        String queryStr = "SELECT s FROM Sail s WHERE 1=1";
        Map<String, Object> params = new HashMap<>();

        if (klientId != null) {
            queryStr += " AND s.klient.id = :klientId";
            params.put("klientId", klientId);
        }

        if (statusId != null) {
            queryStr += " AND s.status.id = :statusId";
            params.put("statusId", statusId);
        }

        if (oplataId != null) {
            queryStr += " AND s.oplata.id = :oplataId";
            params.put("oplataId", oplataId);
        }

        Query query = entityManager.createQuery(queryStr);
        params.forEach(query::setParameter);

        return query.getResultList();
    }

    @Override
    public BigDecimal getZarplataForPeriod(LocalDate start, LocalDate end) {
        String jpql = "SELECT COALESCE(SUM(s.zarplata), 0) FROM Sail s " +
                      "WHERE s.toDay BETWEEN :start AND :end";

        return entityManager.createQuery(jpql, BigDecimal.class)
                .setParameter("start", start)
                .setParameter("end", end)
                .getSingleResult();
    }

}

