package com.igorgorbachev.SpringBootBK.service;

import com.igorgorbachev.SpringBootBK.dao.SailDao;
import com.igorgorbachev.SpringBootBK.entity.Sail;

import jakarta.transaction.Transactional;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


import java.math.BigDecimal;

import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

@Service
public class SailServiceImpl implements SailService {

    private static final Logger logger = Logger.getLogger(SailServiceImpl.class);

    @Autowired
    SailDao sailDao;

    @Transactional
    @Override
    public void addSail(Sail sail) {

        // Устанавливаем округление до целых чисел
        RoundingMode roundingMode = RoundingMode.HALF_UP;
        int scale = 0; // 0 знаков после запятой = целые числа


        logger.info("addSail = " + sail);
        // 1. Проверка основного объекта
        if (sail == null) {
            throw new IllegalArgumentException("Объект продажи не может быть null");
        }

        // 2. Установка текущей даты с форматированием
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEEE, d MMMM yyyy", new Locale("ru"));
        String formattedDate = LocalDate.now().format(formatter);
        formattedDate = formattedDate.substring(0, 1).toUpperCase() + formattedDate.substring(1);
        sail.setToDay(formattedDate);

        // 3. Проверка обязательных полей
        if (sail.getPrice() == null || sail.getZakupka() == null
            || sail.getSumma() == null || sail.getKolichestvo() == null) {
            throw new IllegalArgumentException("Все обязательные числовые поля должны быть заполнены");
        }

        // 4. Проверка корректности значений
        if (sail.getPrice().compareTo(BigDecimal.ZERO) <= 0
            || sail.getZakupka().compareTo(BigDecimal.ZERO) <= 0
            || sail.getKolichestvo().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Цена, закупка и количество должны быть больше 0");
        }

        // 5. Расчет НДС (если ставка задана)
        BigDecimal nalog = BigDecimal.ZERO;
        if (sail.getNds() != null && sail.getNds().compareTo(BigDecimal.ZERO) > 0) {
            try {
                // Общая выручка (цена * количество)
                BigDecimal totalRevenue = (sail.getPrice().multiply(sail.getKolichestvo())).multiply(BigDecimal.valueOf(20)).divide(BigDecimal.valueOf(120), scale,roundingMode);
                BigDecimal totalZakupka = (sail.getZakupka().multiply(sail.getKolichestvo())).multiply(BigDecimal.valueOf(20)).divide(BigDecimal.valueOf(120), scale,roundingMode);
                // Расчет НДС с округлением до 2 знаков
                nalog = totalRevenue.subtract(totalZakupka);
                sail.setNalog(nalog);
            } catch (ArithmeticException e) {
                throw new IllegalArgumentException("Ошибка расчета НДС: " + e.getMessage());
            }
        }

        // 6. Расчет прибыли
        BigDecimal profit;
        try {
            BigDecimal totalIncome = sail.getPrice().multiply(sail.getKolichestvo());
            BigDecimal totalCost = sail.getZakupka().multiply(sail.getKolichestvo()).add(nalog);
            profit = totalIncome.subtract(totalCost).setScale(scale, roundingMode);
            sail.setPribil(profit);
        } catch (ArithmeticException e) {
            throw new IllegalArgumentException("Ошибка в расчетах прибыли: " + e.getMessage());
        }

        // 7. Расчет зарплаты (25% от прибыли)
        try {
            BigDecimal salary = profit.multiply(new BigDecimal("0.25")).setScale(scale, roundingMode);
            sail.setZarplata(salary);
        } catch (ArithmeticException e) {
            throw new IllegalArgumentException("Ошибка в расчетах зарплаты: " + e.getMessage());
        }

        logger.info("addSail END  = " + sail);
        // 8. Сохранение в DAO
        sailDao.addSail(sail);
    }

    @Transactional
    @Override
    public void changeSail(Sail sail) {
        // Устанавливаем округление до целых чисел
        RoundingMode roundingMode = RoundingMode.HALF_UP;
        int scale = 0; // 0 знаков после запятой = целые числа

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEEE, d MMMM yyyy", new Locale("ru"));
        String formattedDate = LocalDate.now().format(formatter);
        formattedDate = formattedDate.substring(0, 1).toUpperCase() + formattedDate.substring(1);
        sail.setToDay(formattedDate);

        logger.info("addSail = " + sail);
        // 1. Проверка основного объекта
        if (sail == null) {
            throw new IllegalArgumentException("Объект продажи не может быть null");
        }

        // 3. Проверка обязательных полей
        if (sail.getPrice() == null || sail.getZakupka() == null
            || sail.getSumma() == null || sail.getKolichestvo() == null) {
            throw new IllegalArgumentException("Все обязательные числовые поля должны быть заполнены");
        }

        // 4. Проверка корректности значений
        if (sail.getPrice().compareTo(BigDecimal.ZERO) <= 0
            || sail.getZakupka().compareTo(BigDecimal.ZERO) <= 0
            || sail.getKolichestvo().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Цена, закупка и количество должны быть больше 0");
        }

        // 5. Расчет НДС (если ставка задана)
        BigDecimal nalog = BigDecimal.ZERO;
        if (sail.getNds() != null && sail.getNds().compareTo(BigDecimal.ZERO) > 0) {
            try {
                // Общая выручка (цена * количество)
                BigDecimal totalRevenue = (sail.getPrice().multiply(sail.getKolichestvo())).multiply(BigDecimal.valueOf(20)).divide(BigDecimal.valueOf(120), scale,roundingMode);
                BigDecimal totalZakupka = (sail.getZakupka().multiply(sail.getKolichestvo())).multiply(BigDecimal.valueOf(20)).divide(BigDecimal.valueOf(120), scale,roundingMode);
                // Расчет НДС с округлением до 2 знаков
                nalog = totalRevenue.subtract(totalZakupka);
                sail.setNalog(nalog);
            } catch (ArithmeticException e) {
                throw new IllegalArgumentException("Ошибка расчета НДС: " + e.getMessage());
            }
        }

        // 6. Расчет прибыли
        BigDecimal profit;
        try {
            BigDecimal totalIncome = sail.getPrice().multiply(sail.getKolichestvo());
            BigDecimal totalCost = sail.getZakupka().multiply(sail.getKolichestvo()).add(nalog);
            profit = totalIncome.subtract(totalCost).setScale(scale, roundingMode);
            sail.setPribil(profit);
        } catch (ArithmeticException e) {
            throw new IllegalArgumentException("Ошибка в расчетах прибыли: " + e.getMessage());
        }

        // 7. Расчет зарплаты (25% от прибыли)
        try {
            BigDecimal salary = profit.multiply(new BigDecimal("0.25")).setScale(scale, roundingMode);
            sail.setZarplata(salary);
        } catch (ArithmeticException e) {
            throw new IllegalArgumentException("Ошибка в расчетах зарплаты: " + e.getMessage());
        }
        BigDecimal summa = sail.getPrice().multiply(sail.getKolichestvo());
        sail.setSumma(summa);

        sailDao.changeSail(sail);
    }

    @Transactional
    @Override
    public void deleteSail(Long id) {
        sailDao.deleteSail(id);
    }

    @Transactional
    @Override
    public List<Sail> getAllSail() {
        List<Sail> sailList = sailDao.getAllSail();
        Collections.sort(sailList, Comparator.comparing(Sail::getId).reversed());
        return sailList;
    }

    @Transactional
    @Override
    public Sail getSailById(Long id) {
        return sailDao.getSailById(id);
    }

    @Transactional
    @Override
    public List<Sail> getListSailByKlient(Long id) {
        return sailDao.getListSailByKlient(id);
    }



}
