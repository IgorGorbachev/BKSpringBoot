package com.igorgorbachev.SpringBootBK.service.telegram.impl;

import com.igorgorbachev.SpringBootBK.entity.Sail;
import com.igorgorbachev.SpringBootBK.service.telegram.TelegramNotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Slf4j
@Service
@RequiredArgsConstructor
public class TelegramNotificationServiceImpl implements TelegramNotificationService {


    private final SimpleTelegramService telegramService;

    @Override
    public void sendSalaryNotification(BigDecimal weeklySalary, LocalDate weekStart, LocalDate weekEnd) {
        String message = buildSalaryMessage(weeklySalary, weekStart, weekEnd);
        telegramService.sendMessage(message);
    }

    @Override
    public String buildSalaryMessage(BigDecimal weeklySalary, LocalDate weekStart, LocalDate weekEnd) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM");
        String period = weekStart.format(formatter) + " - " + weekEnd.format(formatter);

        return "💼 *Зарплата за неделю*\n" +
                "——————————————\n" +
                "📅 Период: " + period + "\n" +
                "💰 Сумма: *" + formatBigDecimal(weeklySalary) + " ₽*\n" +
                "——————————————\n" +
                "Продажа успешно добавлена!";
    }

    @Override
    public void sendNewSaleNotification(Sail sail) {
        String message = buildNewSaleMessage(sail);
        telegramService.sendMessage(message);
    }

    @Override
    public String buildNewSaleMessage(Sail sail) {
        return "🛒 *Новая продажа*\n" +
                "——————————————\n" +
                "📦 Товар: " + sail.getNameSail() + "\n" +
                "🔢 Артикул: " + sail.getArticul() + "\n" +
                "🧮 Количество: " + sail.getKolichestvo() + "\n" +
                "💰 Сумма: *" + formatBigDecimal(sail.getSumma()) + " ₽*\n" +
                "——————————————\n" +
                "Клиент: " + (sail.getKlient() != null ? sail.getKlient().getName() : "Не указан");
    }

    private String formatBigDecimal(BigDecimal value) {
        if (value == null) {
            return "0.00";
        }
        return String.format("%,.2f", value);
    }

}
