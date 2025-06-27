package com.igorgorbachev.SpringBootBK.dao;

import com.igorgorbachev.SpringBootBK.entity.Sail;

import java.util.List;

public interface SailDao {
    void addSail(Sail sail);

    void changeSail(Sail sail);

    void deleteSail(Sail sail);

    List<Sail> getAllSail();

    Sail getSailById(Long id);
}
