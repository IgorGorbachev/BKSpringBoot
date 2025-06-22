package com.igorgorbachev.SpringBootBK.service;

import com.igorgorbachev.SpringBootBK.dao.CarDao;
import com.igorgorbachev.SpringBootBK.dao.KlientDao;
import com.igorgorbachev.SpringBootBK.entity.Car;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.transaction.Transactional;
import java.util.List;

@Component
public class CarServiceImpl implements CarService{

    @Autowired
    CarDao carDao;

    @Transactional
    @Override
    public void addCar(Car car) {
        carDao.addCar(car);
    }

    @Transactional
    @Override
    public void changeCar(Car car) {
        carDao.changeCar(car);
    }

    @Transactional
    @Override
    public List<Car> getAllCars() {
        return carDao.getAllCars();
    }

    @Transactional
    @Override
    public void deleteCar(Car car) {
        carDao.deletCar(car);
    }

}
