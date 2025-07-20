package com.igorgorbachev.SpringBootBK.controller;

import com.igorgorbachev.SpringBootBK.entity.Klient;
import com.igorgorbachev.SpringBootBK.service.KlientService;
import com.igorgorbachev.SpringBootBK.service.SailService;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class KlientController {
    private static final Logger logger = Logger.getLogger(KlientController.class);

    @Autowired
    private KlientService klientService;

    @Autowired
    private SailService sailService;

    @GetMapping("/")
    public String showKlients(Model model) {
        logger.info("showKlients called");
        model.addAttribute("klientList", klientService.getAllSortedKlients());
        return "klients";
    }

    @PostMapping("/addKlient")
    public String addKlient(@ModelAttribute("klient") Klient klient) {
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
    public String deleteKlient(@RequestParam("id") Long klientId) {
        klientService.deleteKlientWithValidation(klientId);
        return "redirect:/";
    }

    @GetMapping("/kassa")
    public String kassa(Model model){
        model.addAttribute("sailList", sailService.getAllSail());
        model.addAttribute("klientList", klientService.getAllSortedKlients());
        model.addAttribute("debts", klientService.getAllDebt());
        return "kassa";
    }
}