package com.igorgorbachev.SpringBootBK.service;

import com.igorgorbachev.SpringBootBK.dao.DetailsDao;
import com.igorgorbachev.SpringBootBK.entity.Detail;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;
@Service
public class DetailServiceImpl implements DetailService{

    @Autowired
    DetailsDao detailsDao;

    @Transactional
    @Override
    public void addDetail(Detail detail) {
        detailsDao.addDetail(detail);
    }

    @Transactional
    @Override
    public void changeDetail(Detail detail) {
        detailsDao.changeDetail(detail);
    }

    @Transactional
    @Override
    public List<Detail> getAllDetail() {
        return detailsDao.getAllDetail();
    }

    @Transactional
    @Override
    public void deleteDetail(Detail detail) {
        detailsDao.deleteDetail(detail);
    }

    @Transactional
    @Override
    public Detail getDetailFromBD(Long id) {
        return detailsDao.getDetailFromBD(id);
    }

    @Transactional
    @Override
    public List<Detail> getDetailByCarId(Long id) {
        return detailsDao.getDetailByCarId(id);
    }
}
