package com.igorgorbachev.SpringBootBK.controller;

import com.igorgorbachev.SpringBootBK.service.SailService;
import lombok.RequiredArgsConstructor;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;
import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;

@ControllerAdvice
@RequiredArgsConstructor
public class GlobalModelAttributes {

    private final SailService sailService;

    @ModelAttribute
    public void addWeeklySalary(Model model) {
        LocalDate today = LocalDate.now();
        LocalDate weekStart = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate weekEnd = today.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));
        BigDecimal weeklySalary = sailService.getZarplataForPeriod(weekStart, weekEnd);
        model.addAttribute("weeklySalary", weeklySalary);
        model.addAttribute("weekStart", weekStart);
        model.addAttribute("weekEnd", weekEnd);
    }
}
