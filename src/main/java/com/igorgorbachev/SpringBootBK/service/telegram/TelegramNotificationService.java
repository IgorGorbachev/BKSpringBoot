package com.igorgorbachev.SpringBootBK.service.telegram;

import com.igorgorbachev.SpringBootBK.entity.Sail;

import java.math.BigDecimal;
import java.time.LocalDate;

public interface TelegramNotificationService {

    void sendSalaryNotification(BigDecimal weeklySalary, LocalDate weekStart, LocalDate weekEnd);

    String buildSalaryMessage(BigDecimal weeklySalary, LocalDate weekStart, LocalDate weekEnd);

    void sendNewSaleNotification(Sail sail);

    String buildNewSaleMessage(Sail sail);
}
