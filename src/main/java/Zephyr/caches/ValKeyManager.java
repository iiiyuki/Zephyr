package Zephyr.caches;

import io.github.cdimascio.dotenv.Dotenv;
import io.valkey.JedisPoolConfig;
import io.valkey.JedisPooled;
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
  @SuppressWarnings("FieldCanBeLocal")
  private final JedisPooled jedisPooled;

  /**
   * 私有构造函数，初始化 JedisPool。
   */
  private ValKeyManager() {
    // 配置连接池
    JedisPoolConfig config = new JedisPoolConfig();
    // 最大连接数
    config.setMaxTotal(10);
    // 最大空闲连接数
    config.setMaxIdle(3);
    // 最小空闲连接数
    config.setMinIdle(1);

    // 初始化 JedisPool
    String host = dotenv.get("VALKEY_HOST");
    int port = Integer.parseInt(dotenv.get("VALKEY_PORT"));
    String password = dotenv.get("VALKEY_PASSWORD");
    String url = "rediss://" + "default" + ":" + password + "@" + host + ":" + port;
    this.jedisPooled = new JedisPooled(url);
    String res = this.jedisPooled.ping();
    // 测试连接
    if (!"PONG".equals(res)) {
      log.error("Failed to connect to ValKey.");
    } else {
      log.info("Connected to ValKey.");
    }
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

  public void set(String key, String value) {
    try{
      jedisPooled.set(key, value);
    } catch (Exception e) {
      log.error("Failed to set key: {}, value: {}", key, value, e);
    }
  }

  public String get(String key) {
    try {
      return jedisPooled.get(key);
    } catch (Exception e) {
      log.error("Failed to get key: {}", key, e);
      return null;
    }
  }

  public void setWithExpire(String key, String value, int seconds) {
    try {
      jedisPooled.setex(key, seconds, value);
    } catch (Exception e) {
      log.error("Failed to set key: {}, value: {}, expire: {}", key, value, seconds, e);
    }
  }

  public void del(String key) {
    try {
      jedisPooled.del(key);
    } catch (Exception e) {
      log.error("Failed to delete key: {}", key, e);
    }
  }
}
