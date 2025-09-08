package com.igorgorbachev.SpringBootBK.service.impl;

import com.igorgorbachev.SpringBootBK.dao.TaskDao;
import com.igorgorbachev.SpringBootBK.entity.Task;
import com.igorgorbachev.SpringBootBK.service.TaskService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class TaskServiceImpl implements TaskService {

    private final TaskDao taskDao;

    @Override
    public void addTask(Task task) {
        taskDao.save(task);
    }

    @Override
    public void changeTask(Task task) {
        taskDao.save(task);
    }

    @Override
    public void deleteTask(Long id) {
        Task task = taskDao.findById(id).get();
        taskDao.delete(task);
    }

    @Override
    public List<Task> getAllTask() {
        return taskDao.findAll();
    }

    @Override
    public Task getTaskById(Long id) {
        return taskDao.findTaskById(id);
    }

}
