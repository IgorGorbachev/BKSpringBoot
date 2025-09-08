package com.igorgorbachev.SpringBootBK.dao;

import com.igorgorbachev.SpringBootBK.entity.Sail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;


@Repository
public interface SailRepository extends JpaRepository<Sail, Long>, JpaSpecificationExecutor<Sail> {

    @Query(value = "SELECT COALESCE(SUM(s.zarplata), 0) FROM Sail s WHERE s.toDay BETWEEN :start AND :end")
    BigDecimal getZarplataForPeriod(LocalDate start, LocalDate end);

    Sail findSailById(Long id);
}
