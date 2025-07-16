package com.igorgorbachev.SpringBootBK.controller;

import com.igorgorbachev.SpringBootBK.entity.Sail;
import com.igorgorbachev.SpringBootBK.service.KlientService;
import com.igorgorbachev.SpringBootBK.service.SailService;
import jakarta.servlet.http.HttpSession;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
public class SailController {

    private static final Logger logger = Logger.getLogger(SailController.class);

    @Autowired
    private SailService sailService;

    @Autowired
    private KlientService klientService;

    @GetMapping("/showSails")
    public String getAllSails(@RequestParam(required = false) Long klientFilter,
                              Model model,
                              HttpSession session) {

        // Получаем отфильтрованный список продаж
        List<Sail> sailList = sailService.getFilteredSails(klientFilter, session);

        // Добавляем атрибуты в модель
        model.addAttribute("sailList", sailList);
        model.addAttribute("klientList", klientService.getAllSortedKlients());
        model.addAllAttributes(sailService.getWeekAttributes());
        model.addAttribute("lastSelectedKlientId", session.getAttribute("lastSelectedKlientId"));
        model.addAttribute("klientFilter", klientFilter);

        return "sails";
    }

    @PostMapping("/addSail")
    public String addSail(@ModelAttribute Sail sail,
                          @RequestParam Long klientId,
                          HttpSession session) {

        sailService.processAddSail(sail, klientId, session);
        return "redirect:/showSails";
    }

    @PostMapping("/deleteSail")
    public String deleteSail(@ModelAttribute Sail sail) {
        sailService.deleteSail(sail.getId());
        return "redirect:/showSails";
    }

    @PostMapping("/changeSail")
    public String changeSail(@ModelAttribute Sail sail,
                             @RequestParam Long klientId,
                             @RequestParam String status,
                             @RequestParam String oplata) {

        sailService.processChangeSail(sail, klientId, status, oplata);
        return "redirect:/showSails";
    }
}