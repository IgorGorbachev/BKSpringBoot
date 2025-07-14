package com.igorgorbachev.SpringBootBK.controller;


import com.igorgorbachev.SpringBootBK.dao.KlientDao;
import com.igorgorbachev.SpringBootBK.entity.Car;
import com.igorgorbachev.SpringBootBK.entity.Detail;
import com.igorgorbachev.SpringBootBK.entity.Klient;
import com.igorgorbachev.SpringBootBK.entity.Sail;
import com.igorgorbachev.SpringBootBK.service.CarService;
import com.igorgorbachev.SpringBootBK.service.DetailService;
import com.igorgorbachev.SpringBootBK.service.KlientService;
import com.igorgorbachev.SpringBootBK.service.SailService;
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
public class KlientController {
    private static final Logger logger = Logger.getLogger(KlientController.class);

    @Autowired
    KlientService klientService;

    @Autowired
    SailService sailService;

    @GetMapping("/")
    public String showKlients(Model model) {
        logger.info("showKlients called");
        List<Klient> klientList = klientService.getAllKlients();
        model.addAttribute("klientList", klientList);
        return "klients";
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

    @PostMapping("/showSailsKlient")
    public String getSailsByKlient(@RequestParam(value = "klientId", required = false) Long klientId, Model model){
        List<Sail> sailList = sailService.getListSailByKlient(klientId);
        if (klientId != null) {
            sailList = sailService.getListSailByKlient(klientId);
        } else {
            sailList = sailService.getAllSail();
        }
        List<Klient> klientList = klientService.getAllKlients();
        model.addAttribute("klientList", klientList);
        model.addAttribute("sailList", sailList);
        model.addAttribute("allKlients", klientService.getAllKlients());
        return "/sails";
    }


}
