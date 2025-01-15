package Zephyr;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.mysqlclient.MySQLConnectOptions;
import io.vertx.sqlclient.Pool;
import io.vertx.sqlclient.PoolOptions;
import io.github.cdimascio.dotenv.Dotenv;

/**
 * @author binaryYuki
 */
public class dbHelper {
  private final Pool client;

  public dbHelper(Vertx vertx) {
    // Load the .env file
    Dotenv dotenv = Dotenv.load();

    // MySQL connection options
    MySQLConnectOptions connectOptions = new MySQLConnectOptions()
      .setPort(Integer.parseInt(dotenv.get("DB_PORT")))
      .setHost(dotenv.get("DB_HOST"))
      .setDatabase(dotenv.get("DB_NAME"))
      .setUser(dotenv.get("DB_USER"))
      .setPassword(dotenv.get("DB_PWD"));

    // Pool options
    PoolOptions poolOptions = new PoolOptions()
      .setMaxSize(5);

    // Create the pooled client using Vertx context
    this.client = Pool.pool(vertx, connectOptions, poolOptions);
  }

  public Pool getClient() {
    return client;
  }

  // Asynchronous database initialization
  public void init(Handler<AsyncResult<Void>> resultHandler) {
    // SQL command to create the table
    String createTableSQL = """
            CREATE TABLE IF NOT EXISTS services (
                id INT PRIMARY KEY,
                service_name VARCHAR(255),
                service_description VARCHAR(255),
                service_version VARCHAR(255),
                service_status VARCHAR(255),
                host_id VARCHAR(255),
                host_name VARCHAR(255),
                host_ip VARCHAR(255),
                updated_at TIMESTAMP,
                created_at TIMESTAMP,
                is_deleted BOOLEAN
            );
        """;
    // Execute the query asynchronously
    client.query(createTableSQL).execute(ar -> {
      if (ar.succeeded()) {
        System.out.println("Table 'services' created successfully (if not exists).");
        resultHandler.handle(io.vertx.core.Future.succeededFuture());
      } else {
        System.err.println("Failed to create table 'services': " + ar.cause().getMessage());
        resultHandler.handle(io.vertx.core.Future.failedFuture(ar.cause()));
      }
    });
  }
}
