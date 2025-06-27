package com.igorgorbachev.SpringBootBK.service;

import com.igorgorbachev.SpringBootBK.entity.Sail;

import java.util.List;

public interface SailService {
    void addSail(Sail sail);

    void changeSail(Sail sail);

    void deleteSail(Sail sail);

    List<Sail> getAllSail();

    Sail getSailById(Long id);
}
