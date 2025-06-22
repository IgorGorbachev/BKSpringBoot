package com.igorgorbachev.SpringBootBK.service;

import com.igorgorbachev.SpringBootBK.dao.KlientDao;
import com.igorgorbachev.SpringBootBK.entity.Car;
import com.igorgorbachev.SpringBootBK.entity.Klient;
import org.springframework.stereotype.Repository;

import java.util.List;
@Repository
public interface CarService {
    void addCar(Car car);

    void changeCar(Car car);

    List<Car> getAllCars();

    void deleteCar(Car car);

    List<Car> getCarsByKlientId(Klient klient);

}
