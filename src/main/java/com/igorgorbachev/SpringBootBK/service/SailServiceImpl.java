package com.igorgorbachev.SpringBootBK.service;

import com.igorgorbachev.SpringBootBK.dao.SailDao;
import com.igorgorbachev.SpringBootBK.entity.Klient;
import com.igorgorbachev.SpringBootBK.entity.Sail;

import jakarta.servlet.http.HttpSession;
import jakarta.transaction.Transactional;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

@Service
public class SailServiceImpl implements SailService {

    private static final Logger logger = Logger.getLogger(SailServiceImpl.class);
    private static final RoundingMode ROUNDING_MODE = RoundingMode.HALF_UP;
    private static final int SCALE = 0;

    @Autowired
    SailDao sailDao;

    @Autowired
    KlientService klientService;

    @Transactional
    @Override
    public void addSail(Sail sail) {
        logger.info("addSail = " + sail);

        validateSail(sail);
        setCurrentFormattedDate(sail);
        calculateSailValues(sail);

        logger.info("addSail END = " + sail);
        sailDao.addSail(sail);
    }

    @Transactional
    @Override
    public void changeSail(Sail sail) {
        logger.info("changeSail = " + sail);

        validateSail(sail);
        setCurrentFormattedDate(sail);
        calculateSailValues(sail);

        sailDao.changeSail(sail);
    }

    private void validateSail(Sail sail) {
        if (sail == null) {
            throw new IllegalArgumentException("Объект продажи не может быть null");
        }

        if (sail.getPrice() == null || sail.getZakupka() == null
            || sail.getSumma() == null || sail.getKolichestvo() == null) {
            throw new IllegalArgumentException("Все обязательные числовые поля должны быть заполнены");
        }

        if (sail.getPrice().compareTo(BigDecimal.ZERO) <= 0
            || sail.getZakupka().compareTo(BigDecimal.ZERO) <= 0
            || sail.getKolichestvo().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Цена, закупка и количество должны быть больше 0");
        }
    }

    private void setCurrentFormattedDate(Sail sail) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEEE, d MMMM yyyy", new Locale("ru"));
        String formattedDate = LocalDate.now().format(formatter);
        formattedDate = formattedDate.substring(0, 1).toUpperCase() + formattedDate.substring(1);
        sail.setToDay(formattedDate);
    }

    private void calculateSailValues(Sail sail) {
        BigDecimal nalog = calculateNalog(sail);
        BigDecimal profit = calculateProfit(sail, nalog);
        BigDecimal salary = calculateSalary(profit);

        sail.setNalog(nalog);
        sail.setPribil(profit);
        sail.setZarplata(salary);
        sail.setSumma(sail.getPrice().multiply(sail.getKolichestvo()));
    }

    private BigDecimal calculateNalog(Sail sail) {
        if (sail.getNds() == null || sail.getNds().compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }

        try {
            BigDecimal totalRevenue = (sail.getPrice().multiply(sail.getKolichestvo()))
                    .multiply(BigDecimal.valueOf(20))
                    .divide(BigDecimal.valueOf(120), SCALE, ROUNDING_MODE);
            BigDecimal totalZakupka = (sail.getZakupka().multiply(sail.getKolichestvo()))
                    .multiply(BigDecimal.valueOf(20))
                    .divide(BigDecimal.valueOf(120), SCALE, ROUNDING_MODE);
            return totalRevenue.subtract(totalZakupka);
        } catch (ArithmeticException e) {
            throw new IllegalArgumentException("Ошибка расчета НДС: " + e.getMessage());
        }
    }

    private BigDecimal calculateProfit(Sail sail, BigDecimal nalog) {
        try {
            BigDecimal totalIncome = sail.getPrice().multiply(sail.getKolichestvo());
            BigDecimal totalCost = sail.getZakupka().multiply(sail.getKolichestvo()).add(nalog);
            return totalIncome.subtract(totalCost).setScale(SCALE, ROUNDING_MODE);
        } catch (ArithmeticException e) {
            throw new IllegalArgumentException("Ошибка в расчетах прибыли: " + e.getMessage());
        }
    }

    private BigDecimal calculateSalary(BigDecimal profit) {
        try {
            return profit.multiply(new BigDecimal("0.25")).setScale(SCALE, ROUNDING_MODE);
        } catch (ArithmeticException e) {
            throw new IllegalArgumentException("Ошибка в расчетах зарплаты: " + e.getMessage());
        }
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

    @Transactional
    @Override
    public BigDecimal zarplataFromPeriod(LocalDate start, LocalDate end) {
        Objects.requireNonNull(start, "Start date cannot be null");
        Objects.requireNonNull(end, "End date cannot be null");

        return sailDao.zarplataFromPeriod(start, end);
    }

    @Override
    public List<Sail> getFilteredSails(Long klientFilter, HttpSession session) {
        // Сохраняем фильтр в сессии
        if (klientFilter != null) {
            session.setAttribute("klientFilter", klientFilter);
        } else {
            session.removeAttribute("klientFilter");
        }

        return (klientFilter != null && klientFilter > 0)
                ? getListSailByKlient(klientFilter)
                : getAllSail();
    }

    @Override
    public Map<String, Object> getWeekAttributes() {
        LocalDate now = LocalDate.now();
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("startOfWeek", now.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)));
        attributes.put("endOfWeek", now.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY)));
        return attributes;
    }

    @Override
    @Transactional
    public void processAddSail(Sail sail, Long klientId, HttpSession session) {
        session.setAttribute("lastSelectedKlientId", klientId);
        Klient klient = klientService.getKlientById(klientId);
        sail.setKlient(klient);
        addSail(sail);
    }

    @Override
    @Transactional
    public void processChangeSail(Sail sail, Long klientId, String status, String oplata) {
        Klient klient = klientService.getKlientById(klientId);
        sail.setStatus(status);
        sail.setOplata(oplata);
        sail.setKlient(klient);
        changeSail(sail);
    }
}