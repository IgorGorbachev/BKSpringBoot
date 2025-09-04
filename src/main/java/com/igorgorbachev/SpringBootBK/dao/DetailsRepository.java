package com.igorgorbachev.SpringBootBK.dao;

import com.igorgorbachev.SpringBootBK.entity.Detail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
@Repository
public interface DetailsRepository extends JpaRepository<Detail, Long> {

    List<Detail> findAll();

    @Query(value = "SELECT d FROM Detail d WHERE d.id = :id")
    Detail getDetailFromBD(Long id);

    @Query(value = "SELECT d FROM Detail d WHERE d.car.id = :id")
    List<Detail> getDetailByCarId(Long id);
}
