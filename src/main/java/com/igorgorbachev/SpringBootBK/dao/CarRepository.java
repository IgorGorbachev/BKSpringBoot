package com.igorgorbachev.SpringBootBK.dao;

import com.igorgorbachev.SpringBootBK.entity.Car;
import lombok.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CarRepository extends JpaRepository<Car, Long> {
    @NonNull
    List<Car> findAll();

    @NonNull
    @Query(value = "SELECT c FROM Car c WHERE c.klient.id = :id")
    List<Car> getListCarsById(Long id);

    Optional<Car> findById(Long id);
}
