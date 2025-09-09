package com.igorgorbachev.SpringBootBK.service.impl;

import com.igorgorbachev.SpringBootBK.dao.KassaRepository;
import com.igorgorbachev.SpringBootBK.service.KassaService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class KassaServiceImpl implements KassaService {

    private final KassaRepository kassaRepository;

    @Override
    public Map<String, Object> getAllDebtsData() {
        List<Object[]> allData = kassaRepository.getAllDebtsWithPaymentTypes();

        Map<String, Object> result = new HashMap<>();

        List<Object[]> allDebts = processDebts(allData, 3L, 4L, 7L, 8L);
        List<Object[]> nalDebts = processDebts(allData, 4L);
        List<Object[]> beznalNeVistavlen = processDebts(allData, 3L, 7L);
        List<Object[]> beznalVistavlen = processDebts(allData, 8L);

        result.put("all", allDebts);
        result.put("nal", nalDebts);
        result.put("beznalNeVistavlen", beznalNeVistavlen);
        result.put("beznalVistavlen", beznalVistavlen);

        // Добавляем totals сразу
        result.put("totalAll", calculateTotal(allDebts));
        result.put("totalNal", calculateTotal(nalDebts));
        result.put("totalBeznalNeVistavlen", calculateTotal(beznalNeVistavlen));
        result.put("totalBeznalVistavlen", calculateTotal(beznalVistavlen));

        return result;
    }

    private List<Object[]> processDebts(List<Object[]> data, Long... types) {
        Set<Long> typeSet = Set.of(types);
        return data.stream()
                .filter(debt -> typeSet.contains((Long) debt[1]))
                .collect(Collectors.groupingBy(
                        debt -> (String) debt[0],
                        Collectors.reducing(
                                BigDecimal.ZERO,
                                debt -> (BigDecimal) debt[2],
                                BigDecimal::add
                        )
                ))
                .entrySet().stream()
                .map(entry -> new Object[]{entry.getKey(), entry.getValue()})
                .collect(Collectors.toList());
    }

    private BigDecimal calculateTotal(List<Object[]> debts) {
        return debts.stream()
                .map(debt -> (BigDecimal) debt[1])
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
