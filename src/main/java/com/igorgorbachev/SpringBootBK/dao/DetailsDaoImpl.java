package com.igorgorbachev.SpringBootBK.dao;

import com.igorgorbachev.SpringBootBK.entity.Car;
import com.igorgorbachev.SpringBootBK.entity.Detail;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;


import java.util.List;
@Repository
public class DetailsDaoImpl implements DetailsDao{

    @PersistenceContext
    EntityManager entityManager;

    @Override
    public void addDetail(Detail detail) {
        entityManager.persist(detail);
    }

    @Override
    public void changeDetail(Detail detail) {
        entityManager.merge(detail);
    }

    @Override
    public List<Detail> getAllDetail() {
        return entityManager.createQuery("from Detail", Detail.class).getResultList();
    }

    @Override
    public void deleteDetail(Detail detail) {
        entityManager.remove(entityManager.contains(detail) ? detail : entityManager.merge(detail));
    }

    @Override
    public Detail getDetailFromBD(Long id) {
        return entityManager.createQuery("SELECT d FROM Detail d WHERE d.id = :id", Detail.class)
                .setParameter("id", id)
                .getSingleResult();
    }

    @Override
    public List<Detail> getDetailByCarId(Long id) {
        return entityManager.createQuery("SELECT d FROM Detail d WHERE d.car.id = :id", Detail.class)
                .setParameter("id", id)
                .getResultList();
    }
}
