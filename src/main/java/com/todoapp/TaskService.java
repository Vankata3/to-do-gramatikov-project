package com.todoapp;

import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class TaskService {
    private final LocalStorageService localStorage;
    private final FirebaseService firebaseService;
    private List<Task> tasks;
    private boolean syncEnabled = false;

    public TaskService() {
        this.localStorage = new LocalStorageService();
        this.firebaseService = new FirebaseService();
        this.tasks = localStorage.readLocal();
    }

    public boolean initializeFirebase(String serviceAccountPath, String userId) {
        boolean initialized = firebaseService.initialize(serviceAccountPath);
        if (initialized) {
            firebaseService.setCurrentUser(userId);
            syncEnabled = true;
        }
        return initialized;
    }

    public boolean isFirebaseEnabled() {
        return syncEnabled && firebaseService.isInitialized();
    }

    public List<Task> getTasks() {
        return tasks;
    }

    public void addTask(Task task) {
        tasks.add(0, task);
        localStorage.writeLocal(tasks);

        if (syncEnabled) {
            firebaseService.addTaskToFirestore(task).exceptionally(throwable -> {
                System.err.println("Failed to sync task to Firebase: " + throwable.getMessage());
                return null;
            });
        }
    }

    public void updateTask(Task task) {
        localStorage.writeLocal(tasks);

        if (syncEnabled) {
            firebaseService.updateTaskInFirestore(task).exceptionally(throwable -> {
                System.err.println("Failed to sync task update to Firebase: " + throwable.getMessage());
                return null;
            });
        }
    }

    public void removeTask(String taskId) {
        tasks.removeIf(task -> task.getId().equals(taskId));
        localStorage.writeLocal(tasks);

        if (syncEnabled) {
            firebaseService.deleteTaskFromFirestore(taskId).exceptionally(throwable -> {
                System.err.println("Failed to sync task deletion to Firebase: " + throwable.getMessage());
                return null;
            });
        }
    }

    public void clearCompletedTasks() {
        List<String> completedTaskIds = tasks.stream()
            .filter(Task::isCompleted)
            .map(Task::getId)
            .collect(Collectors.toList());

        tasks.removeIf(Task::isCompleted);
        localStorage.writeLocal(tasks);

        if (syncEnabled && !completedTaskIds.isEmpty()) {
            CompletableFuture.allOf(
                completedTaskIds.stream()
                    .map(firebaseService::deleteTaskFromFirestore)
                    .toArray(CompletableFuture[]::new)
            ).exceptionally(throwable -> {
                System.err.println("Failed to sync completed task deletion to Firebase: " + throwable.getMessage());
                return null;
            });
        }
    }

    public String getSyncStatus() {
        return syncEnabled && firebaseService.isInitialized() ? "Firebase" : "Local";
    }

    public void close() {
        firebaseService.close();
    }
}
