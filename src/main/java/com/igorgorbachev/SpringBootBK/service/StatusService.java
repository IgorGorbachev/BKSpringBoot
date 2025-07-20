package com.igorgorbachev.SpringBootBK.service;

import com.igorgorbachev.SpringBootBK.entity.Status;

import java.util.List;

public interface StatusService {
    List<Status> getAllStatus();
    Status getStatusById(Long id);
}
