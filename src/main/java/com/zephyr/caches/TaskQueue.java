package com.zephyr.caches;

import Zephyr.caches.Tasks;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;
import java.util.ArrayList;
import java.util.List;

public class TaskQueue {
    private final Cache<String, Object> cache;
    private final ConcurrentLinkedQueue<Tasks> timeQueue;

    public TaskQueue() {
        this.cache = Caffeine.newBuilder()
                .expireAfterWrite(1, TimeUnit.HOURS)  // 默认1小时过期
                .maximumSize(10000)                   // 最大缓存条目数
                .build();
        this.timeQueue = new ConcurrentLinkedQueue<Tasks>();
    }

    public void put(String key, Object value) {
        cache.put(key, value);
        timeQueue.offer(new Tasks(key, value));
    }

    public Object get(String key) {
        return cache.getIfPresent(key);
    }

    public List<Tasks> getTasksByPattern(String regex) {
        List<Tasks> matchedTasks = new ArrayList<>();
        Pattern pattern = Pattern.compile(regex);

        for (Tasks task : timeQueue) {
            if (pattern.matcher(task.getKey()).matches()) {
                matchedTasks.add(task);
            }
        }
        return matchedTasks;
    }

    public Tasks pollTask() {
        Tasks task = timeQueue.poll();
        if (task != null) {
            cache.invalidate(task.getKey());
        }
        return task;
    }

    public Tasks pollTask(String regex) {
        Pattern pattern = Pattern.compile(regex);
        for (Tasks task : timeQueue) {
            if (pattern.matcher(task.getKey()).matches()) {
                if (timeQueue.remove(task)) {
                    cache.invalidate(task.getKey());
                    return task;
                }
            }
        }
        return null;
    }

    public int size() {
        return timeQueue.size();
    }

    public boolean isEmpty() {
        return timeQueue.isEmpty();
    }
}
