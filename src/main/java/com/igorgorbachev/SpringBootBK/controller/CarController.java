package com.igorgorbachev.SpringBootBK.controller;

import com.igorgorbachev.SpringBootBK.entity.Car;
import com.igorgorbachev.SpringBootBK.entity.Klient;
import com.igorgorbachev.SpringBootBK.service.CarService;
import com.igorgorbachev.SpringBootBK.service.KlientService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Slf4j
@Controller
@RequiredArgsConstructor
public class CarController {

    private final CarService carService;
    private final KlientService klientService;


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
        carService.deleteCar(carId);
        return "redirect:/showCars?id=" + klientId;
    }
}