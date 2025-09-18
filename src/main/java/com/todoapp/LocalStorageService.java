package com.todoapp;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class LocalStorageService {
    private static final String STORAGE_KEY = "todo.tasks.v1";
    private final ObjectMapper objectMapper;
    private final Path storagePath;

    public LocalStorageService() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
        this.storagePath = Paths.get("data", "tasks.json");
        
        try {
            Files.createDirectories(storagePath.getParent());
        } catch (IOException e) {
            throw new RuntimeException("Failed to create storage directory", e);
        }
    }

    public List<Task> readLocal() {
        if (!Files.exists(storagePath)) {
            return new ArrayList<>();
        }

        try {
            byte[] bytes = Files.readAllBytes(storagePath);
            String json = new String(bytes, StandardCharsets.UTF_8);
            if (json.trim().isEmpty()) {
                return new ArrayList<>();
            }
            return objectMapper.readValue(json, new TypeReference<List<Task>>() {});
        } catch (IOException e) {
            System.err.println("Error loading tasks: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    public void writeLocal(List<Task> tasks) {
        try {
            String json = objectMapper.writeValueAsString(tasks);
            Files.write(storagePath, json.getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            throw new RuntimeException("Failed to save tasks", e);
        }
    }
}
