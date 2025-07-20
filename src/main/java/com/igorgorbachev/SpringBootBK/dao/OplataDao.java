package com.igorgorbachev.SpringBootBK.dao;

import com.igorgorbachev.SpringBootBK.entity.Oplata;

import java.util.List;

public interface OplataDao {
    List<Oplata> getAllOplata();
    Oplata getOplataById(Long id);
}
