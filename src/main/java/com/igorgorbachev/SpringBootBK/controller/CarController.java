package com.igorgorbachev.SpringBootBK.controller;

import com.igorgorbachev.SpringBootBK.entity.Car;
import com.igorgorbachev.SpringBootBK.entity.Klient;
import com.igorgorbachev.SpringBootBK.service.CarService;
import com.igorgorbachev.SpringBootBK.service.KlientService;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
public class CarController {
    private static final Logger logger = Logger.getLogger(CarController.class);

    @Autowired
    CarService carService;
    @Autowired
    KlientService klientService;

    @GetMapping("/showCars")
    public String showCars(@ModelAttribute("klient") Klient klient, Model model) {
        List<Car> carList = carService.getCarsByKlientId(klient);
        model.addAttribute("carList", carList);
        Klient klientFromBD = klientService.getKlientById(klient.getId());
        model.addAttribute("klient", klientFromBD);
        return "cars";
    }

    @PostMapping("/addCar")
    public String addCar(@ModelAttribute("car") Car car, @ModelAttribute("klient") Klient klient, Model model) {
        Car newCar = new Car(car.getName(), car.getVin());
        newCar.setKlient(klient);
        carService.addCar(newCar);
        return "redirect:/showCars?id=" + klient.getId();
    }

    @PostMapping("/changedCar")
    public String changedCar(@ModelAttribute("car") Car car, @RequestParam("klientId") Long klientId, @RequestParam("carId") Long carId) {
        Car carForChange = carService.getCarFromBD(carId);
        carForChange.setName(car.getName());
        carForChange.setVin(car.getVin());
        carService.changeCar(carForChange);
        return "redirect:/showCars?id=" + klientId;
    }

    @PostMapping("/deleteCar")
    public String deleteCar(@ModelAttribute("car") Car car, @RequestParam("klientId") Long klientId, @RequestParam("carId") Long carId) {
        Car carForDelete = carService.getCarFromBD(carId);
        carService.deleteCar(carForDelete);
        return "redirect:/showCars?id=" + klientId;
    }
}
