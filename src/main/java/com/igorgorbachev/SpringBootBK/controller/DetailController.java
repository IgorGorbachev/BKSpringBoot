package com.igorgorbachev.SpringBootBK.controller;

import com.igorgorbachev.SpringBootBK.entity.Car;
import com.igorgorbachev.SpringBootBK.entity.Detail;
import com.igorgorbachev.SpringBootBK.entity.Klient;
import com.igorgorbachev.SpringBootBK.service.CarService;
import com.igorgorbachev.SpringBootBK.service.DetailService;
import com.igorgorbachev.SpringBootBK.service.KlientService;
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
public class DetailController {
    public static final Logger logger = Logger.getLogger(DetailController.class);
    @Autowired
    DetailService detailService;

    @Autowired
    KlientService klientService;

    @Autowired
    CarService carService;

    @GetMapping("/showDetails")
    public String showDetails(@ModelAttribute("car") Car car, Model model) {
        logger.info("showDetails called");
        List<Detail> detailList = detailService.getDetailByCarId(car.getId());
        model.addAttribute("detailList", detailList);
        Car carFromBD = carService.getCarFromBD(car.getId());
        model.addAttribute("car", carFromBD);
        return "details";
    }

    @GetMapping("/allDetails")
    public String getAllDetails(Model model){
        List<Detail> detailList = detailService.getAllDetail();
        model.addAttribute("detailList", detailList);
        return "/allDetails";
    }

    @PostMapping("/addDetail")
    public String addDetail(@ModelAttribute("detail") Detail detail,@ModelAttribute("car") Car car,@RequestParam("id") Long carId) {
        logger.info("ADD DETAIL FROM CONTROLLER carID = " + carId);
        Detail newDetail = new Detail(detail.getName(),detail.getOriginArticul(),detail.getAnalogArticul());
        newDetail.setCar(car);
        detailService.addDetail(newDetail);
        return "redirect:/showDetails?id="+carId;
    }

    @PostMapping("/changeDetail")
    public String changeDetail(@ModelAttribute("detail") Detail detail, @RequestParam("carId") Long carId, @RequestParam("detailId") Long detailId) {
        Detail detailForChange = detailService.getDetailFromBD(detailId);
        detailForChange.setName(detail.getName());
        detailForChange.setOriginArticul(detail.getOriginArticul());
        detailForChange.setAnalogArticul(detail.getAnalogArticul());
        detailService.changeDetail(detailForChange);
        return "redirect:/showDetails?id=" + carId;
    }

    @PostMapping("/deleteDetail")
    public String deleteDetail(@ModelAttribute("detail") Detail detail, @RequestParam("detailId") Long detailId, @RequestParam("carId") Long carId) {
        Detail detailForDelete = detailService.getDetailFromBD(detailId);
        detailService.deleteDetail(detailForDelete);
        return "redirect:/showDetails?id=" + carId;
    }


}
