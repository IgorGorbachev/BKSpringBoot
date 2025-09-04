package com.igorgorbachev.SpringBootBK.dao;

import com.igorgorbachev.SpringBootBK.entity.Car;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CarRepository extends JpaRepository<Car, Long> {
    List<Car> findAll();

    @Query(value = "SELECT c FROM Car c WHERE c.klient.id = :id")
    List<Car> getListCarsById(Long id);

    @Query(value = "SELECT c FROM Car c WHERE c.id = :id")
    Car getCarFromBD(Long id);
}
