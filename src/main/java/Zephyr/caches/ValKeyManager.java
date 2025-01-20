package Zephyr.caches;

import io.valkey.Jedis;
import io.github.cdimascio.dotenv.Dotenv;
import io.valkey.JedisPool;
import io.valkey.JedisPoolConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author tingzhanghuang
 */
public class ValKeyManager {
  private static final Logger log = LoggerFactory.getLogger(ValKeyManager.class);
  // 单例实例
  private static volatile ValKeyManager instance;
  Dotenv dotenv = Dotenv.load();

  // Jedis 连接池
  private final JedisPool jedisPool;

  /**
   * 私有构造函数，初始化 JedisPool。
   */
  private ValKeyManager() {
    // 配置连接池
    JedisPoolConfig config = new JedisPoolConfig();
    // 最大连接数
    config.setMaxTotal(32);
    // 最大空闲连接数
    config.setMaxIdle(24);
    // 最小空闲连接数
    config.setMinIdle(1);

    // 初始化 JedisPool
    // Valkey 主机地址
    String host = dotenv.get("REDIS_HOST");
    // Valkey 端口
    int port = Integer.parseInt(dotenv.get("REDIS_PORT"));
    String password = dotenv.get("REDIS_PASSWORD");
    // 超时时间（毫秒）
    int timeout = 2000;

    this.jedisPool = new JedisPool(config, host, port, timeout, password);
  }

  /**
   * 获取 ValKeyManager 的单例实例。
   *
   * @return ValKeyManager 实例
   */
  public static ValKeyManager getInstance() {
    if (instance == null) {
      synchronized (ValKeyManager.class) {
        if (instance == null) {
          instance = new ValKeyManager();
        }
      }
    }
    return instance;
  }

  /**
   * 设置一个键值对。
   *
   * @param key   键
   * @param value 值
   */
  public void set(String key, String value) {
    try (Jedis jedis = jedisPool.getResource()) {
      jedis.set(key, value);
    } catch (Exception e) {
      System.err.println("Error setting key: " + key + ", value: " + value + ", error: " + e.getMessage());
      throw e;
    }
  }

  /**
   * 获取一个键的值。
   *
   * @param key 键
   * @return 值，如果键不存在则返回 null
   */
  public String get(String key) {
    try (Jedis jedis = jedisPool.getResource()) {
      return jedis.get(key);
    } catch (Exception e) {
      System.err.println("Error getting key: " + key + ", error: " + e.getMessage());
      throw e;
    }
  }

  /**
   * 删除一个键。
   *
   * @param key 键
   */
  public void delete(String key) {
    try (Jedis jedis = jedisPool.getResource()) {
      jedis.del(key);
    } catch (Exception e) {
      System.err.println("Error deleting key: " + key + ", error: " + e.getMessage());
      throw e;
    }
  }

  /**
   * 检查一个键是否存在。
   *
   * @param key 键
   * @return 如果键存在则返回 true，否则返回 false
   */
  public boolean exists(String key) {
    try (Jedis jedis = jedisPool.getResource()) {
      return jedis.exists(key);
    } catch (Exception e) {
      System.err.println("Error checking existence of key: " + key + ", error: " + e.getMessage());
      throw e;
    }
  }

  /**
   * 关闭连接池。
   */
  public void close() {
    if (jedisPool != null) {
      jedisPool.close();
    }
  }

  public void setWithExpire(String key, String jsonValue, long seconds) {
    try (Jedis jedis = jedisPool.getResource()) {
      jedis.setex(key, seconds, jsonValue);
    } catch (Exception e) {
      log.error("Error setting key: {}, value: {}, error: {}", key, jsonValue, e.getMessage());
      throw e;
    }
  }
}
