package com.igorgorbachev.SpringBootBK.dao;

import com.igorgorbachev.SpringBootBK.entity.Status;

import java.util.List;

public interface StatusDao {
    List<Status> getAllStatus();
    Status getStatusById(Long id);
}
