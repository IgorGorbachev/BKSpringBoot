package com.igorgorbachev.SpringBootBK.dao;

import com.igorgorbachev.SpringBootBK.entity.Klient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
@Repository
public interface KlientRepository extends JpaRepository<Klient, Long> {

    @Query(value = "SELECT k.name, SUM(s.summa) " +
            "FROM Klient k " +
            "JOIN k.sails s " +
            "WHERE s.oplata.id IN (3, 4, 7, 8) " +
            "GROUP BY k.name")
    List<Object[]> getAllDebt();

    List<Klient> findAll();

}
