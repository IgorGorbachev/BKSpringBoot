package com.igorgorbachev.SpringBootBK.dao.impl;

import com.igorgorbachev.SpringBootBK.dao.TaskDao;
import com.igorgorbachev.SpringBootBK.entity.Task;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class TaskDaoImpl implements TaskDao {

    @PersistenceContext
    EntityManager entityManager;

    @Override
    public void addTask(Task task) {
        entityManager.persist(task);
    }

    @Override
    public void changeTask(Task task) {
        entityManager.merge(task);
    }

    @Override
    public void deleteTask(Long id) {
        Task task = entityManager.find(Task.class, id);
        if (task != null) {
            entityManager.remove(task);
        }
    }

    @Override
    public List<Task> getAllTask() {
        return entityManager.createQuery("FROM Task", Task.class).getResultList();
    }

    @Override
    public Task getTaskById(Long id) {
        return entityManager.find(Task.class, id);
    }
}
