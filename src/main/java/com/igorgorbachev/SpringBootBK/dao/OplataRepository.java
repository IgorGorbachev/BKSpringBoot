package com.igorgorbachev.SpringBootBK.dao;

import com.igorgorbachev.SpringBootBK.entity.Oplata;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OplataRepository extends JpaRepository<Oplata,Integer> {
    Oplata findOplatasById(Long id);
}
