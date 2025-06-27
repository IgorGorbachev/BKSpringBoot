package com.igorgorbachev.SpringBootBK.service;

import com.igorgorbachev.SpringBootBK.dao.DetailsDao;
import com.igorgorbachev.SpringBootBK.entity.Detail;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.awt.dnd.DragGestureEvent;
import java.util.Collections;
import java.util.Comparator;
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
        List<Detail> sortDetailList = detailsDao.getAllDetail();
        Collections.sort(sortDetailList, Comparator.comparing(Detail::getName,String.CASE_INSENSITIVE_ORDER));
        return sortDetailList;
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
        List<Detail> sortList = detailsDao.getDetailByCarId(id);
        Collections.sort(sortList, Comparator.comparing(Detail::getName, String.CASE_INSENSITIVE_ORDER));

        return sortList;
    }
}
