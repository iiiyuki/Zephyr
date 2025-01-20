package com.zephyr.caches;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.locks.ReentrantLock;
import java.util.regex.Pattern;
import java.util.ArrayList;
import java.util.List;

public class TaskQueue {
    private final ConcurrentHashMap<String, Object> kvStore;
    private final ConcurrentLinkedQueue<Task> timeQueue;
    private final ReentrantLock lock;

    public TaskQueue() {
        this.kvStore = new ConcurrentHashMap<>();
        this.timeQueue = new ConcurrentLinkedQueue<>();
        this.lock = new ReentrantLock();
    }

    public void put(String key, Object value) {
        lock.lock();
        try {
            kvStore.put(key, value);
            timeQueue.offer(new Task(key, value));
        } finally {
            lock.unlock();
        }
    }

    public Object get(String key) {
        return kvStore.get(key);
    }

    public List<Task> getTasksByPattern(String regex) {
        List<Task> matchedTasks = new ArrayList<>();
        Pattern pattern = Pattern.compile(regex);
        
        lock.lock();
        try {
            for (Task task : timeQueue) {
                if (pattern.matcher(task.getKey()).matches()) {
                    matchedTasks.add(task);
                }
            }
        } finally {
            lock.unlock();
        }
        return matchedTasks;
    }

    public Task pollTask() {
        return timeQueue.poll();
    }

    public Task pollTask(String regex) {
        lock.lock();
        try {
            Pattern pattern = Pattern.compile(regex);
            Task task = timeQueue.peek();
            while (task != null) {
                if (pattern.matcher(task.getKey()).matches()) {
                    timeQueue.remove(task);
                    return task;
                }
                task = timeQueue.peek();
            }
            return null;
        } finally {
            lock.unlock();
        }
    }

    public int size() {
        return timeQueue.size();
    }

    public boolean isEmpty() {
        return timeQueue.isEmpty();
    }
}