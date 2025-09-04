package com.igorgorbachev.SpringBootBK.service.impl;

import com.igorgorbachev.SpringBootBK.dao.CarRepository;
import com.igorgorbachev.SpringBootBK.entity.Car;
import com.igorgorbachev.SpringBootBK.entity.Klient;
import com.igorgorbachev.SpringBootBK.service.CarService;
import com.igorgorbachev.SpringBootBK.service.KlientService;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;

@Slf4j
@Service
@Transactional
public class CarServiceImpl implements CarService {

    private final CarRepository carRepository;
    private final KlientService klientService;

    public CarServiceImpl(CarRepository carDao, KlientService klientService) {
        this.carRepository = carDao;
        this.klientService = klientService;
    }

    @Override
    public void addCarForKlient(Car car, Long klientId) {
        Klient klient = klientService.getKlientById(klientId);
        Car newCar = new Car(car.getName(), car.getVin());
        newCar.setKlient(klient);
        carRepository.save(newCar);
    }

    @Override
    public void updateCar(Long carId, Car car) {
        Car existingCar = getCarById(carId);
        existingCar.setName(car.getName());
        existingCar.setVin(car.getVin());
        carRepository.save(existingCar);
    }

    @Override
    public List<Car> getAllSortedCars() {
        List<Car> cars = carRepository.findAll();
        cars.sort(Comparator.comparing(Car::getName, String.CASE_INSENSITIVE_ORDER));
        return cars;
    }

    @Transactional
    @Override
    public void deleteCar(Long carId) {
        Car car = carRepository.getCarFromBD(carId);
        log.info("**********************************************"+car.getDetails());
        if (!car.getDetails().isEmpty()) {
            throw new IllegalStateException();
        }
        carRepository.delete(car);
    }

    @Override
    public Car getCarById(Long id) {
        return carRepository.getCarFromBD(id);
    }

    @Override
    public List<Car> getCarsByKlientId(Long klientId) {
        return carRepository.getListCarsById(klientId);
    }
}