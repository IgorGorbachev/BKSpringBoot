package com.igorgorbachev.SpringBootBK.service;

import com.igorgorbachev.SpringBootBK.entity.Detail;

import java.util.List;
public interface DetailService {
    void addDetailToCar(Detail detail, Long carId);
    void updateDetail(Long detailId, Detail detail);
    List<Detail> getAllSortedDetails();
    void deleteDetail(Long detailId);
    Detail getDetailById(Long id);
    List<Detail> getSortedDetailsByCarId(Long carId);
}