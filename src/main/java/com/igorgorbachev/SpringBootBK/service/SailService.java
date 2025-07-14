package com.igorgorbachev.SpringBootBK.service;

import com.igorgorbachev.SpringBootBK.entity.Sail;

import java.math.BigDecimal;
import java.util.List;

public interface SailService {
    void addSail(Sail sail);

    void changeSail(Sail sail);

    void deleteSail(Long id);

    List<Sail> getAllSail();

    Sail getSailById(Long id);

    List<Sail> getListSailByKlient(Long id);

//    public void updatePrice(Long id, BigDecimal price);
}
