package com.todoapp;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public class TodoBackend {
    private final TaskService taskService;
    private final ObjectMapper objectMapper;

    public TodoBackend() {
        this.taskService = new TaskService();
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
    }

    public boolean initializeFirebase(String serviceAccountPath, String userId) {
        return taskService.initializeFirebase(serviceAccountPath, userId);
    }

    public String getTasks() {
        try {
            return objectMapper.writeValueAsString(taskService.getTasks());
        } catch (Exception e) {
            return "[]";
        }
    }

    public String addTask(String title, String dueDate) {
        try {
            LocalDate due = dueDate != null && !dueDate.isEmpty() ? LocalDate.parse(dueDate) : null;
            Task task = new Task(title, due);
            taskService.addTask(task);
            return objectMapper.writeValueAsString(task);
        } catch (Exception e) {
            return "{}";
        }
    }

    public String updateTask(String taskId, String title, boolean completed, String dueDate) {
        try {
            List<Task> tasks = taskService.getTasks();
            Task task = tasks.stream()
                .filter(t -> t.getId().equals(taskId))
                .findFirst()
                .orElse(null);
            
            if (task != null) {
                task.setTitle(title);
                task.setCompleted(completed);
                if (dueDate != null && !dueDate.isEmpty()) {
                    task.setDue(LocalDate.parse(dueDate));
                } else {
                    task.setDue(null);
                }
                taskService.updateTask(task);
                return objectMapper.writeValueAsString(task);
            }
            return "{}";
        } catch (Exception e) {
            return "{}";
        }
    }

    public boolean deleteTask(String taskId) {
        try {
            taskService.removeTask(taskId);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public int clearCompletedTasks() {
        try {
            List<Task> tasks = taskService.getTasks();
            int completedCount = (int) tasks.stream().filter(Task::isCompleted).count();
            taskService.clearCompletedTasks();
            return completedCount;
        } catch (Exception e) {
            return 0;
        }
    }

    public String getSyncStatus() {
        return taskService.getSyncStatus();
    }
    public void close() {
        taskService.close();
    }

}
