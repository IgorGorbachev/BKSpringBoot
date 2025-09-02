package com.igorgorbachev.SpringBootBK.service.impl;

import com.igorgorbachev.SpringBootBK.dao.TaskDao;
import com.igorgorbachev.SpringBootBK.entity.Task;
import com.igorgorbachev.SpringBootBK.service.TaskService;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Transactional
public class TaskServiceImpl implements TaskService {

    @Autowired
    private TaskDao taskDao;

    @Override
    public void addTask(Task task) {
        taskDao.addTask(task);
    }

    @Override
    public void changeTask(Task task) {
        taskDao.changeTask(task);
    }

    @Override
    public void deleteTask(Long id) {
        taskDao.deleteTask(id);
    }

    @Override
    public List<Task> getAllTask() {
        return taskDao.getAllTask();
    }

    @Override
    public Task getTaskById(Long id) {
        return taskDao.getTaskById(id);
    }

}
