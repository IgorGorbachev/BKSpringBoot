package com.igorgorbachev.SpringBootBK.dao;

import com.igorgorbachev.SpringBootBK.entity.Klient;

import org.springframework.stereotype.Repository;

import java.util.List;
@Repository
public interface KlientDao {
    void addKlient(Klient klient);

    void changeKlient(Klient klient);

    List<Klient> getAllKlients();

    void deleteKlient(Klient klient);

    Klient getKlientById(Long id);



}
