package com.todoapp;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.firestore.*;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.cloud.FirestoreClient;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class FirebaseService {
    private FirebaseApp firebaseApp;
    private Firestore firestore;
    private final ObjectMapper objectMapper;
    private String currentUserId;
    private boolean isInitialized = false;

    public FirebaseService() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
    }

    public boolean initialize(String serviceAccountPath) {
        try {
            if (isInitialized) return true;

            GoogleCredentials credentials = GoogleCredentials.fromStream(new FileInputStream(serviceAccountPath));
            FirebaseOptions options = FirebaseOptions.builder()
                .setCredentials(credentials)
                .build();

            firebaseApp = FirebaseApp.initializeApp(options);
            firestore = FirestoreClient.getFirestore(firebaseApp);
            isInitialized = true;
            return true;
        } catch (IOException e) {
            System.err.println("Failed to initialize Firebase: " + e.getMessage());
            return false;
        }
    }

    public void setCurrentUser(String userId) {
        this.currentUserId = userId;
    }

    public String getCurrentUser() {
        return currentUserId;
    }

    public boolean isInitialized() {
        return isInitialized && firestore != null;
    }

    public CompletableFuture<Void> addTaskToFirestore(Task task) {
        if (!isInitialized || currentUserId == null) {
            return CompletableFuture.completedFuture(null);
        }

        return CompletableFuture.runAsync(() -> {
            try {
                DocumentReference docRef = firestore
                    .collection("users")
                    .document(currentUserId)
                    .collection("tasks")
                    .document(task.getId());
                
                Map<String, Object> data = convertTaskToMap(task);
                data.put("ownerId", currentUserId);
                docRef.set(data).get();
            } catch (InterruptedException | ExecutionException e) {
                throw new RuntimeException("Failed to add task to Firestore", e);
            }
        });
    }

    public CompletableFuture<Void> updateTaskInFirestore(Task task) {
        if (!isInitialized || currentUserId == null) {
            return CompletableFuture.completedFuture(null);
        }

        return CompletableFuture.runAsync(() -> {
            try {
                DocumentReference docRef = firestore
                    .collection("users")
                    .document(currentUserId)
                    .collection("tasks")
                    .document(task.getId());
                
                Map<String, Object> data = convertTaskToMap(task);
                data.put("ownerId", currentUserId);
                docRef.set(data, SetOptions.merge()).get();
            } catch (InterruptedException | ExecutionException e) {
                throw new RuntimeException("Failed to update task in Firestore", e);
            }
        });
    }

    public CompletableFuture<Void> deleteTaskFromFirestore(String taskId) {
        if (!isInitialized || currentUserId == null) {
            return CompletableFuture.completedFuture(null);
        }

        return CompletableFuture.runAsync(() -> {
            try {
                DocumentReference docRef = firestore
                    .collection("users")
                    .document(currentUserId)
                    .collection("tasks")
                    .document(taskId);
                docRef.delete().get();
            } catch (InterruptedException | ExecutionException e) {
                throw new RuntimeException("Failed to delete task from Firestore", e);
            }
        });
    }

    public CompletableFuture<List<Task>> subscribeToTasks(String uid, java.util.function.Consumer<List<Task>> onTasks) {
        if (!isInitialized) {
            return CompletableFuture.completedFuture(new ArrayList<>());
        }

        return CompletableFuture.supplyAsync(() -> {
            try {
                CollectionReference tasksRef = firestore
                    .collection("users")
                    .document(uid)
                    .collection("tasks");
                
                QuerySnapshot snapshot = tasksRef.get().get();
                List<Task> tasks = new ArrayList<>();
                
                for (QueryDocumentSnapshot document : snapshot.getDocuments()) {
                    Task task = convertMapToTask(document.getData());
                    tasks.add(task);
                }
                
                tasks.sort((a, b) -> b.getUpdatedAt().compareTo(a.getUpdatedAt()));
                onTasks.accept(tasks);
                return tasks;
            } catch (InterruptedException | ExecutionException e) {
                throw new RuntimeException("Failed to load tasks from Firestore", e);
            }
        });
    }

    private Map<String, Object> convertTaskToMap(Task task) {
        try {
            String json = objectMapper.writeValueAsString(task);
            return objectMapper.readValue(json, new TypeReference<Map<String, Object>>() {});
        } catch (Exception e) {
            throw new RuntimeException("Failed to convert task to map", e);
        }
    }

    private Task convertMapToTask(Map<String, Object> data) {
        try {
            String json = objectMapper.writeValueAsString(data);
            return objectMapper.readValue(json, Task.class);
        } catch (Exception e) {
            throw new RuntimeException("Failed to convert map to task", e);
        }
    }

    public void close() {
        if (firestore != null) {
            try {
                firestore.close();
            } catch (Exception e) {
                System.err.println("Error closing Firestore connection: " + e.getMessage());
            }
        }
        isInitialized = false;
    }
}