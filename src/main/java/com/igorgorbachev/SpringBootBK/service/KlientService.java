package com.igorgorbachev.SpringBootBK.service;

import com.igorgorbachev.SpringBootBK.entity.Klient;
import java.util.List;

public interface KlientService {
    void addKlient(Klient klient);
    void changeKlient(Klient klient);
    List<Klient> getAllSortedKlients();
    void deleteKlientWithValidation(Long klientId);
    Klient getKlientById(Long id);
}