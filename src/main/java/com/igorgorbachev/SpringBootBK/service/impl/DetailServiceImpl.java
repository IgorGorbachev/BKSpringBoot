package com.igorgorbachev.SpringBootBK.service.impl;

import com.igorgorbachev.SpringBootBK.dao.DetailsRepository;
import com.igorgorbachev.SpringBootBK.entity.Car;
import com.igorgorbachev.SpringBootBK.entity.Detail;
import com.igorgorbachev.SpringBootBK.service.CarService;
import com.igorgorbachev.SpringBootBK.service.DetailService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.Comparator;
import java.util.List;

@Slf4j
@Service
@Transactional
public class DetailServiceImpl implements DetailService {

    private final DetailsRepository detailsRepository;
    private final CarService carService;


    public DetailServiceImpl(DetailsRepository detailsRepository, CarService  carService) {
        this.detailsRepository = detailsRepository;
        this.carService = carService;
    }

    @Override
    public void addDetailToCar(Detail detail, Long carId) {
        Car car = carService.getCarById(carId);
        Detail newDetail = new Detail(detail.getName(), detail.getOriginArticul(), detail.getAnalogArticul());
        newDetail.setCar(car);
        car.getDetails().add(newDetail);
        detailsRepository.save(newDetail);
    }

    @Override
    public void updateDetail(Long detailId, Detail detail) {
        Detail existingDetail = getDetailById(detailId);
        existingDetail.setName(detail.getName());
        existingDetail.setOriginArticul(detail.getOriginArticul());
        existingDetail.setAnalogArticul(detail.getAnalogArticul());
        detailsRepository.save(existingDetail);
    }

    @Override
    public List<Detail> getAllSortedDetails() {
        List<Detail> details = detailsRepository.findAll();
        details.sort(Comparator.comparing(Detail::getName, String.CASE_INSENSITIVE_ORDER));
        return details;
    }

    @Override
    public void deleteDetail(Long detailId, Long carId) {
        Detail detail = detailsRepository.findById(detailId).orElseThrow(()-> new EntityNotFoundException("Detail not found"));
        Car car = carService.getCarById(carId);
        if (!car.getDetails().contains(detail)) {
            throw new IllegalStateException("Detail not associated with this car");
        }
        car.getDetails().remove(detail);
        detailsRepository.delete(detail);
    }

    @Override
    public Detail getDetailById(Long id) {
        return detailsRepository.getDetailFromBD(id);
    }

    @Override
    public List<Detail> getSortedDetailsByCarId(Long carId) {
        List<Detail> details = detailsRepository.getDetailByCarId(carId);
        details.sort(Comparator.comparing(Detail::getId).reversed());
        return details;
    }
}