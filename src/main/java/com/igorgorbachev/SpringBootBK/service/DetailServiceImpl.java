package com.igorgorbachev.SpringBootBK.service;

import com.igorgorbachev.SpringBootBK.dao.CarDao;
import com.igorgorbachev.SpringBootBK.dao.DetailsDao;
import com.igorgorbachev.SpringBootBK.entity.Car;
import com.igorgorbachev.SpringBootBK.entity.Detail;
import jakarta.transaction.Transactional;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.Comparator;
import java.util.List;


@Service
@Transactional
public class DetailServiceImpl implements DetailService {
    Logger logger = Logger.getLogger(DetailServiceImpl.class);

    @Autowired
    private DetailsDao detailsDao;

    @Autowired
    private CarDao carDao;

    @Autowired
    public DetailServiceImpl(DetailsDao detailsDao) {
        this.detailsDao = detailsDao;
    }

    @Override
    public void addDetailToCar(Detail detail, Long carId) {
        Car car = carDao.getCarFromBD(carId);
        Detail newDetail = new Detail(detail.getName(), detail.getOriginArticul(), detail.getAnalogArticul());
        newDetail.setCar(car);
        car.getDetails().add(newDetail);
        detailsDao.addDetail(newDetail);
    }

    @Override
    public void updateDetail(Long detailId, Detail detail) {
        Detail existingDetail = getDetailById(detailId);
        existingDetail.setName(detail.getName());
        existingDetail.setOriginArticul(detail.getOriginArticul());
        existingDetail.setAnalogArticul(detail.getAnalogArticul());
        detailsDao.changeDetail(existingDetail);
    }

    @Override
    public List<Detail> getAllSortedDetails() {
        List<Detail> details = detailsDao.getAllDetail();
        details.sort(Comparator.comparing(Detail::getName, String.CASE_INSENSITIVE_ORDER));
        return details;
    }

    @Override
    public void deleteDetail(Long detailId) {
        detailsDao.deleteDetail(getDetailById(detailId));
    }

    @Override
    public Detail getDetailById(Long id) {
        return detailsDao.getDetailFromBD(id);
    }

    @Override
    public List<Detail> getSortedDetailsByCarId(Long carId) {
        List<Detail> details = detailsDao.getDetailByCarId(carId);
        details.sort(Comparator.comparing(Detail::getId).reversed());
        return details;
    }
}