package com.igorgorbachev.SpringBootBK.controller;


import com.igorgorbachev.SpringBootBK.entity.Car;
import com.igorgorbachev.SpringBootBK.entity.Klient;
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
public class KlientController {
    private static final Logger logger = Logger.getLogger(KlientController.class);

    @Autowired
    KlientService klientService;

    @GetMapping("/")
    public String showKlients(Model model) {
        logger.info("showKlients called");
        List<Klient> klientList = klientService.getAllKlients();
        model.addAttribute("klientList", klientList);
        return "klients";
    }

    @GetMapping("/showCars")
    public String showCars(@ModelAttribute("klient") Klient klient, Model model) {
        logger.info("showCars called with id: " + klient);
        List<Car> carList = klientService.getCarsByKlientId(klient.getId());
        model.addAttribute("carList", carList);
        return "cars";
    }


    @PostMapping("/addKlient")
    public String addKlient(@ModelAttribute("klient") Klient klient, Model model) {
        logger.info("addKlient called with id: " + klient);
        klientService.addKlient(klient);
        return "redirect:/";
    }


    @PostMapping("/changeKlient")
    public String changeKlient(@ModelAttribute("klient") Klient klient) {
        logger.info("changeKlient called with id: " + klient);
        klientService.changeKlient(klient);
        return "redirect:/";
    }

    @PostMapping("/deleteKlient")
    public String deleteKlient(@ModelAttribute("klient") Klient klient) {
        logger.info("deletetKlient called with id: " + klient);
        klientService.deleteKlient(klient);
        return "redirect:/";
    }


}
