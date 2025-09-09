package com.igorgorbachev.SpringBootBK.controller;


import com.igorgorbachev.SpringBootBK.service.KassaService;
import com.igorgorbachev.SpringBootBK.service.KlientService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import java.util.Map;

@Controller
@Slf4j
@RequiredArgsConstructor
public class KassaController {

    private final KassaService kassaService;
    private final KlientService klientService;

    @GetMapping("/kassa")
    public String kassa(Model model) {

        Map<String, Object> debtsData = kassaService.getAllDebtsData();

        model.addAttribute("klientList", klientService.getAllSortedKlients());
        model.addAttribute("debts", debtsData.get("all"));
        model.addAttribute("debtsOnlyNal", debtsData.get("nal"));
        model.addAttribute("debtsOnlyBezNalNeVistavlen", debtsData.get("beznalNeVistavlen"));
        model.addAttribute("debtsOnlyBezNalVistavlen", debtsData.get("beznalVistavlen"));

        model.addAttribute("totalNalDebts", debtsData.get("totalNal"));
        model.addAttribute("totalBezNalNeVistavlenDebts", debtsData.get("totalBeznalNeVistavlen"));
        model.addAttribute("totalBezNalVistavlenDebts", debtsData.get("totalBeznalVistavlen"));
        return "kassa";
    }

}
