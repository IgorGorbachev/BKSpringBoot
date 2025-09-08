package com.igorgorbachev.SpringBootBK.controller;


import com.igorgorbachev.SpringBootBK.entity.Klient;
import com.igorgorbachev.SpringBootBK.entity.Oplata;
import com.igorgorbachev.SpringBootBK.entity.Sail;
import com.igorgorbachev.SpringBootBK.entity.Status;
import com.igorgorbachev.SpringBootBK.service.KlientService;
import com.igorgorbachev.SpringBootBK.service.OplataService;
import com.igorgorbachev.SpringBootBK.service.SailService;
import com.igorgorbachev.SpringBootBK.service.StatusService;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.Comparator;
import java.util.List;
import java.util.Map;


@Controller
@Slf4j
public class SailController {

    private final SailService sailService;
    private final KlientService klientService;
    private final StatusService statusService;
    private final OplataService oplataService;

    public SailController(SailService sailService, KlientService klientService, StatusService statusService, OplataService oplataService) {
        this.sailService = sailService;
        this.klientService = klientService;
        this.statusService = statusService;
        this.oplataService = oplataService;
    }

    @GetMapping("/showSails")
    public String showSails(@RequestParam(required = false) Long klientFilter,
                            @RequestParam(required = false) Long statusFilter,
                            @RequestParam(required = false) Long oplataFilter,
                            @RequestParam(required = false) Boolean reset,
                            Model model,
                            HttpSession session) {
        // Обработка сброса фильтров
        if (reset != null && reset) {
            session.removeAttribute("klientFilter");
            session.removeAttribute("statusFilter");
            session.removeAttribute("oplataFilter");
            return "redirect:/showSails";
        }

        // Сохраняем фильтры в сессии
        if (klientFilter != null) {
            session.setAttribute("klientFilter", klientFilter);
        } else {
            // Если параметр не передан, но есть в сессии - используем из сессии
            klientFilter = (Long) session.getAttribute("klientFilter");
        }

        if (statusFilter != null) {
            session.setAttribute("statusFilter", statusFilter);
        } else {
            statusFilter = (Long) session.getAttribute("statusFilter");
        }

        if (oplataFilter != null) {
            session.setAttribute("oplataFilter", oplataFilter);
        } else {
            oplataFilter = (Long) session.getAttribute("oplataFilter");
        }

        // Получаем отфильтрованные данные
        List<Sail> sailList = sailService.getFilteredSails(klientFilter, statusFilter, oplataFilter);
        sailList.sort(Comparator.comparing(Sail::getId).reversed());
        // Добавляем атрибуты в модель
        model.addAttribute("statusColors", Map.of(
                "В пути", "ff0000",
                "Приехал, Не выдан", "ffc107",
                "Выдан", "28a745",
                "Возврат", "007bff"
        ));

        model.addAttribute("oplataColors", Map.of(
                "Не оплачено", "ff0000",
                "Наличные", "007bff",
                "Терминал", "ffc107",
                "Безнал (счет ОПЛАЧЕН)", "28a745",
                "Наличные + чек", "007bff",
                "Терминал + чек", "ffc107",
                "Безнал (добавил в счет)", "808080",
                "Безнал (счет выставлен)", "ffc107"
        ));

        model.addAttribute("lastSelectedKlientId", session.getAttribute("lastSelectedKlientId"));
        model.addAttribute("sailList", sailList); // Используем только отфильтрованный список
        model.addAttribute("klientList", klientService.getAllSortedKlients());
        model.addAttribute("statusList", statusService.getAllStatus());
        model.addAttribute("oplataList", oplataService.findAll());

        // Добавляем текущие значения фильтров для формы
        model.addAttribute("currentKlientFilter", klientFilter);
        model.addAttribute("currentStatusFilter", statusFilter);
        model.addAttribute("currentOplataFilter", oplataFilter);

        return "sails";
    }

    @PostMapping("/addSail")
    public String addSail(@ModelAttribute Sail sail, @RequestParam Long klientId, HttpSession session) {
        Klient klient = klientService.getKlientById(klientId);
        sail.setStatus(statusService.getStatusById(1L));
        sail.setOplata(oplataService.findOplatasById(4L));
        sail.setKlient(klient);
        session.setAttribute("lastSelectedKlientId", sail.getKlient().getId());
        sailService.addSail(sail);
        return "redirect:/showSails";
    }

    @PostMapping("/deleteSail")
    public String deleteSail(@RequestParam Long id) {
        sailService.deleteSail(id);
        return "redirect:/showSails";
    }

    @PostMapping("/changeSail")
    public String changeSail(@RequestParam(required = false) String nameSail,
                             @RequestParam(required = false) String articul,
                             @RequestParam(required = false) BigDecimal zakupka,
                             @RequestParam(required = false) BigDecimal price,
                             @RequestParam(required = false) BigDecimal kolichestvo,
                             @RequestParam Long sailId,
                             @RequestParam Long statusId,
                             @RequestParam Long oplataId) {

        Sail sail = sailService.getSailById(sailId);

        if (nameSail != null) sail.setNameSail(nameSail);
        if (articul != null) sail.setArticul(articul);
        if (zakupka != null) sail.setZakupka(zakupka);
        if (price != null) sail.setPrice(price);
        if (kolichestvo != null) sail.setKolichestvo(kolichestvo);

        Status status = statusService.getStatusById(statusId);
        Oplata oplata = oplataService.findOplatasById(oplataId);
        sail.setOplata(oplata);
        sail.setStatus(status);
        sailService.changeSail(sail);
        return "redirect:/showSails";
    }

    @ModelAttribute
    public void addWeeklySalary(Model model) {
        LocalDate today = LocalDate.now();
        log.info("START addWeeklySalary today =" + today);
        LocalDate weekStart = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        log.info("START addWeeklySalary weekStart =" + weekStart);
        LocalDate weekEnd = today.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));
        log.info("START addWeeklySalary weekEnd =" + weekEnd);

        BigDecimal weeklySalary = sailService.getZarplataForPeriod(weekStart, weekEnd);
        log.info("START addWeeklySalary weeklySalary =" + weeklySalary);

        model.addAttribute("weeklySalary", weeklySalary);
        model.addAttribute("weekStart", weekStart);
        model.addAttribute("weekEnd", weekEnd);
    }


}