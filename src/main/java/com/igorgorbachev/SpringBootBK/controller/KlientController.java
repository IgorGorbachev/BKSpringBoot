package com.igorgorbachev.SpringBootBK.controller;

import com.igorgorbachev.SpringBootBK.entity.Klient;
import com.igorgorbachev.SpringBootBK.service.KlientService;
import com.igorgorbachev.SpringBootBK.service.SailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Slf4j
@Controller
@RequiredArgsConstructor
public class KlientController {

    private final KlientService klientService;
    private final SailService sailService;

    @GetMapping("/")
    public String showKlients(Model model) {
        log.info("showKlients called");
        model.addAttribute("klientList", klientService.getAllSortedKlients());
        return "klients";
    }

    @PostMapping("/addKlient")
    public String addKlient(@ModelAttribute("klient") Klient klient) {
        log.info("addKlient called with id: {} ", klient);
        klientService.addKlient(klient);
        return "redirect:/";
    }

    @PostMapping("/changeKlient")
    public String changeKlient(@ModelAttribute("klient") Klient klient) {
        log.info("changeKlient called with id: {} ", klient);
        klientService.changeKlient(klient);
        return "redirect:/";
    }

    @PostMapping("/deleteKlient")
    public String deleteKlient(@RequestParam("id") Long klientId) {
        klientService.deleteKlientWithValidation(klientId);
        return "redirect:/";
    }

    @GetMapping("/kassa")
    public String kassa(Model model) {
        model.addAttribute("sailList", sailService.getAllSail());
        model.addAttribute("klientList", klientService.getAllSortedKlients());
        model.addAttribute("debts", klientService.getAllDebt());
        return "kassa";
    }
}