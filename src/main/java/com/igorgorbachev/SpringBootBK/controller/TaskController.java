package com.igorgorbachev.SpringBootBK.controller;

import com.igorgorbachev.SpringBootBK.entity.Task;
import com.igorgorbachev.SpringBootBK.service.TaskService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequiredArgsConstructor
public class TaskController {


    private final TaskService taskService;

    @GetMapping("/task")
    public String showTask(Model model){
        model.addAttribute("taskList", taskService.getAllTask());
        return "task";
    }

    @PostMapping("/addTask")
    public String addTask(@ModelAttribute Task task){
        taskService.addTask(task);
        return "redirect:/task";
    }

    @PostMapping("/deleteTask")
    public String deleteTask(@RequestParam Long id){
        taskService.deleteTask(id);
        return "redirect:/task";
    }

    @PostMapping("/changeTask")
    public String changeTask(@RequestParam Long id, @RequestParam String text) {
        Task task = taskService.getTaskById(id);
        task.setText(text);
        taskService.changeTask(task);
        return "redirect:/task";
    }
}
