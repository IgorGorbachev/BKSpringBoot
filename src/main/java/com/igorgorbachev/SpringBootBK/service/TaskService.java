package com.igorgorbachev.SpringBootBK.service;

import com.igorgorbachev.SpringBootBK.entity.Task;

import java.util.List;

public interface TaskService {
    void addTask(Task task);
    void changeTask(Task task);
    void deleteTask(Long id);
    List<Task> getAllTask();
    Task getTaskById(Long id);
}
