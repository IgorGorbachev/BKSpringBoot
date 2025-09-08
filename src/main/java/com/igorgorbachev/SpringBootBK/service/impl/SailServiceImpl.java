package com.igorgorbachev.SpringBootBK.service.impl;


import com.igorgorbachev.SpringBootBK.dao.SailRepository;
import com.igorgorbachev.SpringBootBK.entity.Sail;
import com.igorgorbachev.SpringBootBK.service.SailService;
import com.igorgorbachev.SpringBootBK.specification.SailSpecifications;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;



@Service
@Transactional
@RequiredArgsConstructor
public class SailServiceImpl implements SailService {

    private final SailRepository sailRepository;

    public void calculate(Sail sail) {
        BigDecimal zakupka = sail.getZakupka().multiply(sail.getKolichestvo());
        BigDecimal prodaja = sail.getPrice().multiply(sail.getKolichestvo());
        BigDecimal dohod = prodaja.subtract(zakupka);
        BigDecimal nalog = dohod.multiply(sail.getNds());
        BigDecimal pribil = dohod.subtract(nalog);
        BigDecimal zarplata = pribil.multiply(new BigDecimal("0.25"));
        sail.setSumma(sail.getKolichestvo().multiply(sail.getPrice()));
        sail.setNalog(nalog);
        sail.setPribil(pribil);
        sail.setZarplata(zarplata);
    }

    @Transactional
    @Override
    public void addSail(Sail sail) {
        sail.setToDay(LocalDate.now());
        calculate(sail);
        sailRepository.save(sail);
    }



    @Transactional
    @Override
    public void changeSail(Sail sail) {
        sail.setToDay(sail.getToDay());
        calculate(sail);
        sailRepository.save(sail);
    }



    @Transactional
    @Override
    public void deleteSail(Long sailId) {
        Sail sail = sailRepository.findById(sailId)
                .orElseThrow(() -> new IllegalArgumentException("Sail with id " + sailId + " not found"));

        sailRepository.delete(sail);
    }



    @Transactional
    @Override
    public List<Sail> getAllSail() {
        return sailRepository.findAll();
    }



    @Transactional
    @Override
    public Sail getSailById(Long id) {
        return sailRepository.findSailById(id);
    }



    @Transactional
    @Override
    public List<Sail> getFilteredSails(Long klientId, Long statusId, Long oplataId) {
        Specification<Sail> spec = Specification.where(null);

        if (klientId != null) {
            spec = spec.and(SailSpecifications.withKlientId(klientId));
        }
        if (statusId != null) {
            spec = spec.and(SailSpecifications.withStatusId(statusId));
        }
        if (oplataId != null) {
            spec = spec.and(SailSpecifications.withOplataId(oplataId));
        }

        return sailRepository.findAll(spec);
    }


    @Transactional
    @Override
    public BigDecimal getZarplataForPeriod(LocalDate start, LocalDate end) {
        return sailRepository.getZarplataForPeriod(start, end);
    }


}