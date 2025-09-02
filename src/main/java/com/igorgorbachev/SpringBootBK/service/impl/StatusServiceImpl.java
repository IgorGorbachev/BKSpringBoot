package com.igorgorbachev.SpringBootBK.service.impl;

import com.igorgorbachev.SpringBootBK.dao.StatusDao;
import com.igorgorbachev.SpringBootBK.entity.Status;
import com.igorgorbachev.SpringBootBK.service.StatusService;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
@Service
@Transactional
public class StatusServiceImpl implements StatusService {

    @Autowired
    private StatusDao statusDao;

    @Override
    public List<Status> getAllStatus() {
        return statusDao.getAllStatus();
    }

    @Override
    public Status getStatusById(Long id) {
        return statusDao.getStatusById(id);
    }
}
