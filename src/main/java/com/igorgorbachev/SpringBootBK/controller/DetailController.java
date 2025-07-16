package com.igorgorbachev.SpringBootBK.controller;

import com.igorgorbachev.SpringBootBK.entity.Car;
import com.igorgorbachev.SpringBootBK.entity.Detail;
import com.igorgorbachev.SpringBootBK.service.CarService;
import com.igorgorbachev.SpringBootBK.service.DetailService;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class DetailController {
    private static final Logger logger = Logger.getLogger(DetailController.class);

    private final DetailService detailService;
    private final CarService carService;

    @Autowired
    public DetailController(DetailService detailService, CarService carService) {
        this.detailService = detailService;
        this.carService = carService;
    }

    @GetMapping("/showDetails")
    public String showDetails(@ModelAttribute("car") Car car, Model model) {
        logger.info("showDetails called");
        model.addAttribute("detailList", detailService.getSortedDetailsByCarId(car.getId()));
        model.addAttribute("car", carService.getCarById(car.getId()));
        return "details";
    }

    @GetMapping("/allDetails")
    public String getAllDetails(Model model) {
        model.addAttribute("detailList", detailService.getAllSortedDetails());
        return "allDetails";
    }

    @PostMapping("/addDetail")
    public String addDetail(@ModelAttribute("detail") Detail detail,
                            @RequestParam("carId") Long carId) {
        logger.info("****************************************************************************************************************ADD DETAIL FROM CONTROLLER carID = " + carId);
        detailService.addDetailToCar(detail, carId);
        return "redirect:/showDetails?id=" + carId;
    }

    @PostMapping("/changeDetail")
    public String changeDetail(@ModelAttribute("detail") Detail detail,
                               @RequestParam("carId") Long carId,
                               @RequestParam("detailId") Long detailId) {
        detailService.updateDetail(detailId, detail);
        return "redirect:/showDetails?id=" + carId;
    }

    @PostMapping("/deleteDetail")
    public String deleteDetail(@RequestParam("detailId") Long detailId,
                               @RequestParam("carId") Long carId) {
        detailService.deleteDetail(detailId);
        return "redirect:/showDetails?id=" + carId;
    }
}