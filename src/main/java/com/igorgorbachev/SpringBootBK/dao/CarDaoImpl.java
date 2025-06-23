package com.igorgorbachev.SpringBootBK.dao;

import com.igorgorbachev.SpringBootBK.entity.Car;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;

@Repository
public class CarDaoImpl implements CarDao{

    @PersistenceContext
    EntityManager entityManager;

    @Override
    public void addCar(Car car) {
        entityManager.persist(car);
    }

    @Override
    public void changeCar(Car car) {
        entityManager.merge(car);
    }

    @Override
    public List<Car> getAllCars() {
        return entityManager.createQuery("from Car", Car.class).getResultList();
    }

    @Override
    public void deletCar(Car car) {
        entityManager.remove(entityManager.contains(car) ? car : entityManager.merge(car));
    }
    @Override
    public List<Car> getListCarsById(Long id) {
        return entityManager.createQuery("SELECT c FROM Car c WHERE c.klient.id = :id", Car.class)
                .setParameter("id", id)
                .getResultList();
    }

    @Override
    public Car getCarFromBD(Long id) {
        return entityManager.createQuery("SELECT c FROM Car c WHERE c.id = :id", Car.class)
                .setParameter("id", id)
                .getSingleResult();
    }
}
