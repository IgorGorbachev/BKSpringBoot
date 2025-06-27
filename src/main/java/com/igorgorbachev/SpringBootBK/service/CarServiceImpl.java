package com.igorgorbachev.SpringBootBK.service;

import com.igorgorbachev.SpringBootBK.dao.CarDao;
import com.igorgorbachev.SpringBootBK.entity.Car;
import com.igorgorbachev.SpringBootBK.entity.Klient;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

@Service
public class CarServiceImpl implements CarService {

    private static final Logger logger = Logger.getLogger(CarServiceImpl.class);

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
        List<Car> carListSort = carDao.getAllCars();
        Collections.sort(carListSort, Comparator.comparing(Car::getName, String.CASE_INSENSITIVE_ORDER));
        return carListSort;
    }

    @Transactional
    @Override
    public void deleteCar(Car car) {
        carDao.deletCar(car);
    }

    @Transactional
    @Override
    public List<Car> getCarsByKlientId(Klient klient) {
        List<Car> carList = new ArrayList<>();
        for (Car car : carDao.getListCarsById(klient.getId())) carList.add(car);
        return carList;
    }

    @Transactional
    @Override
    public Car getCarFromBD(Long id) {
        return carDao.getCarFromBD(id);
    }


}
