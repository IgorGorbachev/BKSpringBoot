package com.igorgorbachev.SpringBootBK.service.impl;

import com.igorgorbachev.SpringBootBK.dao.OplataRepository;
import com.igorgorbachev.SpringBootBK.entity.Oplata;
import com.igorgorbachev.SpringBootBK.service.OplataService;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Transactional
public class OplataServiceImpl implements OplataService {

    OplataRepository oplataRepository;

    public OplataServiceImpl(OplataRepository oplataRepository) {
        this.oplataRepository = oplataRepository;
    }


    @Override
    public List<Oplata> findAll() {
        return oplataRepository.findAll();
    }

    @Override
    public Oplata findOplatasById(Long id) {
        return oplataRepository.findOplatasById(id);
    }
}
