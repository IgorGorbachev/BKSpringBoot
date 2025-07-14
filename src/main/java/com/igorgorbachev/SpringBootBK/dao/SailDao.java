package com.igorgorbachev.SpringBootBK.dao;

import com.igorgorbachev.SpringBootBK.entity.Sail;
import org.springframework.stereotype.Repository;

import java.util.List;
@Repository
public interface SailDao {
    void addSail(Sail sail);

    void changeSail(Sail sail);

    void deleteSail(Long id);

    List<Sail> getAllSail();

    Sail getSailById(Long id);

    List<Sail> getListSailByKlient(Long id);
}
