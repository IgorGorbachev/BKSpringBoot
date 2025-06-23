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
    public String addCar(@ModelAttribute("car") Car car, Model model){
        logger.info("ADDCAR FROM CONTROLLER model = " + model);
        carService.addCar(car);
        return "redirect:/showCars";
    }

    @PostMapping("/changeCar")
    public String changedCar(@ModelAttribute("car") Car car){
        logger.info("changedCar called from CarController");
        carService.changeCar(car);
        return "redirect:/showCars";
    }

    @PostMapping("/deleteCar")
    public String deleteCar(@ModelAttribute("car") Car car){
        logger.info("deleteCar called from CarController");
        carService.deleteCar(car);
        return "redirect:/showCars";
    }
}
