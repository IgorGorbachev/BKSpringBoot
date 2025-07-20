package com.igorgorbachev.SpringBootBK.dao;

import com.igorgorbachev.SpringBootBK.entity.Task;

import java.util.List;

public interface TaskDao {
    void addTask(Task task);
    void changeTask(Task task);
    void deleteTask(Long id);
    List<Task> getAllTask();
    Task getTaskById(Long id);
}
