package com.igorgorbachev.SpringBootBK.service;

import com.igorgorbachev.SpringBootBK.entity.Detail;

import java.util.List;

public interface DetailService {

    void addDetail(Detail detail);

    void changeDetail(Detail detail);

    List<Detail> getAllDetail();

    void deleteDetail(Detail detail);

    Detail getDetailFromBD(Long id);

    List<Detail> getDetailByCarId(Long id);
}
