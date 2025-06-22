package com.igorgorbachev.SpringBootBK.dao;

import com.igorgorbachev.SpringBootBK.entity.Car;
import com.igorgorbachev.SpringBootBK.entity.Klient;
import org.springframework.stereotype.Repository;

import java.util.List;
@Repository
public interface CarDao {
    void addCar(Car car);

    void changeCar(Car car);

    List<Car> getAllCars();

    void deletCar(Car car);

    List<Car> getCarById(Long id);
}
