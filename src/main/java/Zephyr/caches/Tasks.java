package Zephyr.caches;

import java.time.LocalDateTime;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

/**
 * Task 类用于表示一个任务对象，并支持将任务保存到 ValKey 分布式缓存中。
 * 提供了序列化和反序列化功能，以及支持设置过期时间的缓存操作。
 * @author tingzhanghuang
 */
public class Tasks {
  // 任务的唯一标识
  private final String key;
  // 任务的值，可以是任意对象
  private final Object value;
  // 任务的创建时间戳
  private final LocalDateTime timestamp;
  // JSON 序列化和反序列化工具
  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
  // ValKey 管理器实例
  private static final ValKeyManager VAL_KEY_MANAGER = null;

  // 静态代码块初始化 ObjectMapper 和 ValKeyManager
  static {
    // 注册 JavaTimeModule 以支持 LocalDateTime 的序列化和反序列化
    ObjectMapper objectMapper = new ObjectMapper();
    objectMapper.registerModule(new JavaTimeModule());
    // 初始化 ValKeyManager
    ValKeyManager valKeyManager = ValKeyManager.getInstance();
  }

  /**
   * 构造函数，用于创建一个新的 Task 实例。
   *
   * @param key   任务的唯一标识
   * @param value 任务的值
   */
  public Tasks(String key, Object value) {
    this.key = key;
    this.value = value;
    // 设置当前时间为任务的创建时间
    this.timestamp = LocalDateTime.now();
  }

  // Getters
  public String getKey() {
    return key;
  }

  public Object getValue() {
    return value;
  }

  public LocalDateTime getTimestamp() {
    return timestamp;
  }

  /**
   * 将任务保存到 ValKey 缓存中。
   */
  public void saveToValKey() {
    try {
      // 将 Task 对象序列化为 JSON 字符串
      ObjectMapper objectMapper = new ObjectMapper();
      String jsonValue = objectMapper.writeValueAsString(this);
      // 使用 ValKeyManager 保存键值对
      ValKeyManager valKeyManager = ValKeyManager.getInstance();
      valKeyManager.set(key, jsonValue);
    } catch (Exception e) {
      throw new RuntimeException("Failed to save task to ValKey", e);
    }
  }

  /**
   * 从 ValKey 缓存中获取任务。
   *
   * @param key 任务的唯一标识
   * @return 反序列化后的 Task 对象，如果键不存在则返回 null
   */
  public static Tasks getFromValKey(String key) {
    try {
      // 从 ValKey 缓存中获取 JSON 字符串
      ValKeyManager valKeyManager = ValKeyManager.getInstance();
      String jsonValue = valKeyManager.get(key);
      if (jsonValue == null) {
        // 如果键不存在，返回 null
        return null;
      }
      // 将 JSON 字符串反序列化为 Task 对象
      ObjectMapper objectMapper = new ObjectMapper();
      return objectMapper.readValue(jsonValue, Tasks.class);
    } catch (Exception e) {
      throw new RuntimeException("Failed to get task from ValKey", e);
    }
  }

  /**
   * 将任务保存到 ValKey 缓存中，并设置过期时间。
   *
   * @param seconds 过期时间（秒）
   */
  public void saveToValKeyWithExpire(int seconds) {
    try {
      // 将 Task 对象序列化为 JSON 字符串
      ObjectMapper objectMapper = new ObjectMapper();
      String jsonValue = objectMapper.writeValueAsString(this);
      // 使用 ValKeyManager 保存键值对，并设置过期时间
      ValKeyManager valKeyManager = ValKeyManager.getInstance();
      valKeyManager.setWithExpire(key, jsonValue, seconds);
    } catch (Exception e) {
      throw new RuntimeException("Failed to save task to ValKey with expiration", e);
    }
  }
}
