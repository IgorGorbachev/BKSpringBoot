package com.igorgorbachev.SpringBootBK.service.impl;

import com.igorgorbachev.SpringBootBK.dao.KlientRepository;
import com.igorgorbachev.SpringBootBK.entity.Klient;
import com.igorgorbachev.SpringBootBK.service.KlientService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.Hibernate;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class KlientServiceImpl implements KlientService {

    private final KlientRepository klientRepository;


    @Override
    public void addKlient(Klient klient){
        klientRepository.save(klient);
    }

    @Override
    public void changeKlient(Klient klient) {
        klientRepository.save(klient);
    }


    @Override
    @Transactional
    public List<Klient> getAllSortedKlients() {
        List<Klient> clients = klientRepository.findAll();
        clients.sort(Comparator.comparing(Klient::getName, String.CASE_INSENSITIVE_ORDER));
        return clients;
    }

    @Override
    @Transactional
    public void deleteKlientWithValidation(Long klientId) {
        Klient klient = klientRepository.findById(klientId)
                .orElseThrow(() -> new IllegalArgumentException("Klient with id " + klientId + " not found"));

        if (klient.getCars() != null && !klient.getCars().isEmpty()) {
            throw new IllegalStateException("Клиента нельзя удалить, так как у него есть автомобили.");
        }

        klientRepository.delete(klient);
    }

    @Override
    @Transactional
    public Klient getKlientById(Long id) {
        return klientRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Klient with id " + id + " not found"));
    }

    @Override
    public List<Object[]> getAllDebt() {
        return klientRepository.getAllDebt();
    }
}
