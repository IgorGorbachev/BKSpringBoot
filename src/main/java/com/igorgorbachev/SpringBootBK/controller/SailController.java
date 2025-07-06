package com.igorgorbachev.SpringBootBK.controller;

import com.igorgorbachev.SpringBootBK.entity.Klient;
import com.igorgorbachev.SpringBootBK.entity.Sail;
import com.igorgorbachev.SpringBootBK.service.KlientService;
import com.igorgorbachev.SpringBootBK.service.SailService;
import jakarta.validation.Valid;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Controller
public class SailController {

    private static final Logger logger = Logger.getLogger(SailController.class);

    @Autowired
    SailService sailService;

    @Autowired
    KlientService klientService;

    @GetMapping("/sail")
    public String sail(Model model) {
        List<Sail> sailList = sailService.getAllSail();
        List<Klient> klientList = klientService.getAllKlients();
        logger.info("METHODS SAIL FROM CONTROLLER klientList = " + klientList);

        model.addAttribute("klientlist", klientList);
        model.addAttribute("sailList", sailList);
        return "sail";
    }

    @PostMapping("/addSail")
    public String addSail(@Valid @ModelAttribute("sail") Sail sail) {

        sail.setToDay(LocalDate.now().toString());
        sail.setToDay(LocalDate.now().toString());
        sailService.addSail(sail);
        return "redirect:/sail";
    }

    @PostMapping("/changedSail")
    public String changedSail(@ModelAttribute("sail") Sail sail) {
        logger.info("Sail в начале в методе changedSail В КОНТРОЛЛЕРЕ " + sail.getPrice());

        sailService.changeSail(sail);
        logger.info("Sail в конце в методе changedSail В КОНТРОЛЛЕРЕ " + sail.getPrice());
        return "redirect:/sail";
    }

    @PostMapping("/deleteSail")
    public String deleteSail(@ModelAttribute("sail") Sail sail) {
        sailService.deleteSail(sail);
        return "redirect:/sail";
    }


}
