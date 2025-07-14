package com.igorgorbachev.SpringBootBK.controller;


import com.igorgorbachev.SpringBootBK.entity.Sail;
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
public class SailController {

    @Autowired
    SailService sailService;


    private static final Logger logger = Logger.getLogger(SailController.class);


    @GetMapping("/showSails")
    public String getAllSails(Model model) {
        List<Sail> sailList = sailService.getAllSail();
        model.addAttribute("sailList", sailList);
        return "sails";
    }

    @PostMapping("/addSail")
    public String addSail(@ModelAttribute Sail sail){
        logger.info("addSail = " + sail);
        sailService.addSail(sail);
        return "redirect:/showSails";
    }

    @PostMapping("/deleteSail")
    public String deleteSail(@ModelAttribute Sail sail) {
        sailService.deleteSail(sail.getId());
        return "redirect:/showSails";
    }

    @PostMapping("/changeSail")
    public String changeSail(@ModelAttribute Sail sail){
        sailService.changeSail(sail);
        return "redirect:/showSails";
    }






}
