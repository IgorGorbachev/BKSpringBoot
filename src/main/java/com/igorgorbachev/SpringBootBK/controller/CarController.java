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


@Controller
public class CarController {
    private static final Logger logger = Logger.getLogger(CarController.class);

    private final CarService carService;
    private final KlientService klientService;

    @Autowired
    public CarController(CarService carService, KlientService klientService) {
        this.carService = carService;
        this.klientService = klientService;
    }

    @GetMapping("/showCars")
    public String showCars(@ModelAttribute("klient") Klient klient, Model model) {
        model.addAttribute("carList", carService.getCarsByKlientId(klient.getId()));
        model.addAttribute("klient", klientService.getKlientById(klient.getId()));
        return "cars";
    }

    @GetMapping("/allCars")
    public String getAllCars(Model model) {
        model.addAttribute("carList", carService.getAllSortedCars());
        return "allCars";
    }

    @PostMapping("/addCar")
    public String addCar(@ModelAttribute("car") Car car,
                         @RequestParam("klientId") Long klientId) {
        carService.addCarForKlient(car, klientId);
        return "redirect:/showCars?id=" + klientId;
    }

    @PostMapping("/changedCar")
    public String changedCar(@ModelAttribute("car") Car car,
                             @RequestParam("klientId") Long klientId,
                             @RequestParam("carId") Long carId) {
        carService.updateCar(carId, car);
        return "redirect:/showCars?id=" + klientId;
    }

    @PostMapping("/deleteCar")
    public String deleteCar(@RequestParam("carId") Long carId,
                            @RequestParam("klientId") Long klientId) {
        carService.deleteCar(carId); // Вам нужно добавить этот метод в сервис
        return "redirect:/showCars?id=" + klientId;
    }
}