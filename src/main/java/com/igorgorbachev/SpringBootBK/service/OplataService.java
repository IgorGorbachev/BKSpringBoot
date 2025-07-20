package com.igorgorbachev.SpringBootBK.service;

import com.igorgorbachev.SpringBootBK.entity.Oplata;

import java.util.List;

public interface OplataService {
    List<Oplata> getAllOplata();
    Oplata getOplataById(Long id);
}
