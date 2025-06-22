package com.igorgorbachev.SpringBootBK.controller;

import com.igorgorbachev.SpringBootBK.entity.Car;
import com.igorgorbachev.SpringBootBK.entity.Detail;
import com.igorgorbachev.SpringBootBK.service.DetailService;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.List;

@Controller
public class DetailController {
    public static final Logger logger = Logger.getLogger(DetailController.class);
    @Autowired
    DetailService detailService;

    @GetMapping("/showDetails")
    public String showDetails(Model model){
        logger.info("showDetails called");
        List<Detail> detailList = detailService.getAllDetail();
        model.addAttribute("detailList", detailList);
        return "details";
    }

    @PostMapping("/addDetail")
    public String addDetail(@ModelAttribute("detail") Detail detail){
        logger.info("addDetail called");
        detailService.addDetail(detail);
        return "redirect:/showDetails";
    }

    @PostMapping("/changeDetail")
    public String changeDetail(@ModelAttribute("detail") Detail detail){
        logger.info("changeDetail called");
        detailService.changeDetail(detail);
        return "redirect:/showDetails";
    }

    @PostMapping("/deleteDetail")
    public String deleteDetail(@ModelAttribute("detail") Detail detail){
        logger.info("deleteDetail called");
        detailService.deleteDetail(detail);
        return "redirect:/showDetails";
    }


}
