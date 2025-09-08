package com.igorgorbachev.SpringBootBK.dao;

import com.igorgorbachev.SpringBootBK.entity.Sail;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;


@Repository
public interface SailRepository extends JpaRepository<Sail, Long>, JpaSpecificationExecutor<Sail> {

    @Query(value = "SELECT COALESCE(SUM(s.zarplata), 0) FROM Sail s WHERE s.toDay BETWEEN :start AND :end")
    BigDecimal getZarplataForPeriod(LocalDate start, LocalDate end);

    Sail findSailById(Long id);

    @Query("SELECT s FROM Sail s WHERE " +
            "(:klientFilter IS NULL OR s.klient.id = :klientFilter) AND " +
            "(:statusFilter IS NULL OR s.status.id = :statusFilter) AND " +
            "(:oplataFilter IS NULL OR s.oplata.id = :oplataFilter)")
    Page<Sail> findFilteredSails(@Param("klientFilter") Long klientFilter,
                                 @Param("statusFilter") Long statusFilter,
                                 @Param("oplataFilter") Long oplataFilter,
                                 Pageable pageable);
}
