package com.igorgorbachev.SpringBootBK.dao.impl;

import com.igorgorbachev.SpringBootBK.dao.StatusDao;
import com.igorgorbachev.SpringBootBK.entity.Status;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;

import java.util.List;
@Repository
public class StatusDaoImpl implements StatusDao {

    @PersistenceContext
    EntityManager entityManager;

    @Override
    public List<Status> getAllStatus() {
        return entityManager.createQuery("FROM Status", Status.class).getResultList();
    }

    @Override
    public Status getStatusById(Long id) {
        return entityManager.find(Status.class, id);
    }
}
