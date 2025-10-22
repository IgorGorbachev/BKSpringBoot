package com.igorgorbachev.SpringBootBK.controller;

import com.igorgorbachev.SpringBootBK.entity.Sail;
import com.igorgorbachev.SpringBootBK.service.SailService;
import com.igorgorbachev.SpringBootBK.service.telegram.TelegramNotificationService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;


@Controller
@Slf4j
@RequiredArgsConstructor
public class SailController {

    private final SailService sailService;
    private final TelegramNotificationService telegramNotificationService;

//    private final UndoManager undoManager;

    @GetMapping("/showSails")
    public String showSails(@RequestParam(required = false) Long klientFilter,
                            @RequestParam(required = false) Long statusFilter,
                            @RequestParam(required = false) Long oplataFilter,
                            @RequestParam(required = false) Boolean reset,
                            @RequestParam(defaultValue = "0") int page,
                            @RequestParam(defaultValue = "60") int size,
                            Model model,
                            HttpSession session) {

        if (reset != null && reset) {
            session.removeAttribute("klientFilter");
            session.removeAttribute("statusFilter");
            session.removeAttribute("oplataFilter");
            return "redirect:/showSails";
        }

        klientFilter = updateSessionAttribute(session, "klientFilter", klientFilter);
        statusFilter = updateSessionAttribute(session, "statusFilter", statusFilter);
        oplataFilter = updateSessionAttribute(session, "oplataFilter", oplataFilter);

        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());

        Map<String, Object> viewData = sailService.getSailViewData(klientFilter, statusFilter, oplataFilter, pageable);
        model.addAllAttributes(viewData);
        model.addAttribute("lastSelectedKlientId", session.getAttribute("lastSelectedKlientId"));

        return "sails";
    }

    @PostMapping("/addSail")
    public String addSail(@ModelAttribute Sail sail, @RequestParam Long klientId, HttpSession session) {
        sailService.addSail(sail, klientId);
        try {
            // Сохраняем продажу
            sailService.addSail(sail, klientId);

            // Отправляем уведомление о новой продаже
            telegramNotificationService.sendNewSaleNotification(sail);

            // Получаем актуальные данные о зарплате за неделю
            LocalDate[] weekDates = sailService.getCurrentWeekDates();
            BigDecimal weeklySalary = sailService.calculateWeeklySalary(weekDates[0], weekDates[1]);

            // Отправляем уведомление о зарплате
            telegramNotificationService.sendSalaryNotification(weeklySalary, weekDates[0], weekDates[1]);

        } catch (Exception e) {
            log.error("Error in addSail: {}", e.getMessage());
            // Можно добавить сообщение об ошибке в модель
        }
        session.setAttribute("lastSelectedKlientId", sail.getKlient().getId());
        return "redirect:/showSails";
    }

//    @PostMapping("/addSail")
//    public String addSail(@ModelAttribute Sail sail, @RequestParam Long klientId, HttpSession session) {
//        sailService.executeAddSailCommand(sail, klientId);
//        session.setAttribute("lastSelectedKlientId", sail.getKlient().getId());
//        return "redirect:/showSails";
//    }

    @PostMapping("/deleteSail")
    public String deleteSail(@RequestParam Long id) {
        sailService.deleteSail(id);
        return "redirect:/showSails";
    }

//    @PostMapping("/deleteSail")
//    public String deleteSail(@RequestParam Long id) {
//        sailService.executeDeleteSailCommand(id);
//        return "redirect:/showSails";
//    }

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
        sailService.changeSail(sail, statusId, oplataId, nameSail, articul, zakupka, price, kolichestvo);
        return "redirect:/showSails";
    }

//    @PostMapping("/changeSail")
//    public String changeSail(@RequestParam(required = false) String nameSail,
//                             @RequestParam(required = false) String articul,
//                             @RequestParam(required = false) BigDecimal zakupka,
//                             @RequestParam(required = false) BigDecimal price,
//                             @RequestParam(required = false) BigDecimal kolichestvo,
//                             @RequestParam Long sailId,
//                             @RequestParam Long statusId,
//                             @RequestParam Long oplataId) {
//
//        Sail sail = sailService.getSailById(sailId);
//        sailService.executeEditSailCommand(sail, statusId, oplataId,
//                nameSail, articul, zakupka, price, kolichestvo);
//        return "redirect:/showSails";
//    }

//    @PostMapping("/undo")
//    public String undo() {
//        if (undoManager.canUndo()) {
//            undoManager.undo();
//        }
//        return "redirect:/showSails";
//    }
//
//    // Добавляем информацию о возможности undo/redo в модель
//    @ModelAttribute
//    public void addUndoRedoInfo(Model model) {
//        model.addAttribute("canUndo", undoManager.canUndo());
//        model.addAttribute("canRedo", undoManager.canRedo());
//    }


    @ModelAttribute
    public void addWeeklySalary(Model model) {
        sailService.addWeeklySalaryData(model);
    }

    private Long updateSessionAttribute(HttpSession session, String attributeName, Long newValue) {
        if (newValue != null) {
            session.setAttribute(attributeName, newValue);
            return newValue;
        } else {
            return (Long) session.getAttribute(attributeName);
        }
    }
}