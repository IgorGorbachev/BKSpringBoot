package com.igorgorbachev.SpringBootBK.dao;

import com.igorgorbachev.SpringBootBK.entity.Detail;

import java.util.List;

public interface DetailsDao {

    void addDetail(Detail detail);

    void changeDetail(Detail detail);

    List<Detail> getAllDetail();

    void deleteDetail(Detail detail);

    Detail getDetailFromBD(Long id);

    List<Detail> getDetailByCarId(Long id);
}
