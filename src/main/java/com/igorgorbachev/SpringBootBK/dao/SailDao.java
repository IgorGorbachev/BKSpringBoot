package com.igorgorbachev.SpringBootBK.dao;

import com.igorgorbachev.SpringBootBK.entity.Sail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
@Repository
public interface SailDao {
    void addSail(Sail sail);

    void changeSail(Sail sail);

    void deleteSail(Long sailId);

    List<Sail> getAllSail();

    Sail getSailById(Long id);

    public List<Sail> getFilteredSails(Long klientId, Long statusId, Long oplataId);

//    BigDecimal getZarplataForPeriod(LocalDate start, LocalDate end);
}
