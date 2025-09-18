package com.todoapp;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

/**
 * Task model - replaces the JavaScript task object
 */
public class Task {
    @JsonProperty("id")
    private String id;
    
    @JsonProperty("title")
    private String title;
    
    @JsonProperty("completed")
    private boolean completed;
    
    @JsonProperty("due")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate due;
    
    @JsonProperty("updatedAt")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS")
    private LocalDateTime updatedAt;
    
    @JsonProperty("ownerId")
    private String ownerId;

    public Task() {
        this.id = UUID.randomUUID().toString();
        this.updatedAt = LocalDateTime.now();
    }

    public Task(String title, LocalDate due) {
        this();
        this.title = title;
        this.due = due;
        this.completed = false;
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    
    public String getTitle() { return title; }
    public void setTitle(String title) { 
        this.title = title; 
        this.updatedAt = LocalDateTime.now(); 
    }
    
    public boolean isCompleted() { return completed; }
    public void setCompleted(boolean completed) { 
        this.completed = completed; 
        this.updatedAt = LocalDateTime.now(); 
    }
    
    public LocalDate getDue() { return due; }
    public void setDue(LocalDate due) { 
        this.due = due; 
        this.updatedAt = LocalDateTime.now(); 
    }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    
    public String getOwnerId() { return ownerId; }
    public void setOwnerId(String ownerId) { this.ownerId = ownerId; }
}
