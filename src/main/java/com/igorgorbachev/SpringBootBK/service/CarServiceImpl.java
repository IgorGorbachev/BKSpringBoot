package com.igorgorbachev.SpringBootBK.service;

import com.igorgorbachev.SpringBootBK.dao.CarDao;
import com.igorgorbachev.SpringBootBK.entity.Car;
import com.igorgorbachev.SpringBootBK.entity.Klient;
import jakarta.transaction.Transactional;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;


@Service
@Transactional
public class CarServiceImpl implements CarService {
    private static final Logger logger = Logger.getLogger(CarServiceImpl.class);

    private final CarDao carDao;
    private final KlientService klientService;

    @Autowired
    public CarServiceImpl(CarDao carDao, KlientService klientService) {
        this.carDao = carDao;
        this.klientService = klientService;
    }

    @Override
    public void addCarForKlient(Car car, Long klientId) {
        Klient klient = klientService.getKlientById(klientId);
        Car newCar = new Car(car.getName(), car.getVin());
        newCar.setKlient(klient);
        carDao.addCar(newCar);
    }

    @Override
    public void updateCar(Long carId, Car car) {
        Car existingCar = getCarById(carId);
        existingCar.setName(car.getName());
        existingCar.setVin(car.getVin());
        carDao.changeCar(existingCar);
    }

    @Override
    public List<Car> getAllSortedCars() {
        List<Car> cars = carDao.getAllCars();
        cars.sort(Comparator.comparing(Car::getName, String.CASE_INSENSITIVE_ORDER));
        return cars;
    }

    @Transactional
    @Override
    public void deleteCar(Long carId) {
        Car car = carDao.getCarFromBD(carId);
        logger.info("**********************************************"+car.getDetails());
        if (!car.getDetails().isEmpty()) {
            throw new IllegalStateException();
        }
        carDao.deleteCar(carId);
    }

    @Override
    public Car getCarById(Long id) {
        return carDao.getCarFromBD(id);
    }

    @Override
    public List<Car> getCarsByKlientId(Long klientId) {
        return carDao.getListCarsById(klientId);
    }
}