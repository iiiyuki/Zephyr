package com.zephyr.caches;

import java.time.LocalDateTime;

public class Task {
    private String key;
    private Object value;
    private LocalDateTime timestamp;

    public Task(String key, Object value) {
        this.key = key;
        this.value = value;
        this.timestamp = LocalDateTime.now();
    }

    // Getters and setters
    public String getKey() {
        return key;
    }

    public Object getValue() {
        return value;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }
}