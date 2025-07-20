package com.igorgorbachev.SpringBootBK.service;

import com.igorgorbachev.SpringBootBK.dao.OplataDao;
import com.igorgorbachev.SpringBootBK.entity.Oplata;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Transactional
public class OplataServiceImpl implements OplataService {

    @Autowired
    OplataDao oplataDao;

    @Override
    public List<Oplata> getAllOplata() {
        return oplataDao.getAllOplata();
    }

    @Override
    public Oplata getOplataById(Long id) {
        return oplataDao.getOplataById(id);
    }
}
