package com.igorgorbachev.SpringBootBK.service;

import com.igorgorbachev.SpringBootBK.entity.Car;
import org.springframework.stereotype.Repository;

import java.util.List;
@Repository
public interface CarService {
    void addCarForKlient(Car car, Long klientId);
    void updateCar(Long carId, Car car);
    List<Car> getAllSortedCars();
    void deleteCar(Long carId);
    Car getCarById(Long id);
    List<Car> getCarsByKlientId(Long klientId);
}