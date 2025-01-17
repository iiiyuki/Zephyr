package Zephyr;

import io.github.cdimascio.dotenv.Dotenv;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.flywaydb.core.Flyway;
import jakarta.persistence.*;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * Database helper class for managing database connections, ORM, and migrations.
 * This class uses HikariCP for connection pooling, Hibernate for ORM, and Flyway for database migrations.
 * It also loads environment variables using dotenv.
 *
 * @author binaryYuki
 */
public class dbHelper {
  private final HikariDataSource dataSource;
  private final EntityManagerFactory entityManagerFactory;

  /**
   * Constructor for dbHelper.
   * Initializes HikariCP, Hibernate, and Flyway.
   *
   * @param vertx Vertx instance
   */
  public dbHelper(Vertx vertx) {
    // Load the .env file
    Dotenv dotenv = Dotenv.load();

    // HikariCP configuration
    HikariConfig config = new HikariConfig();
    config.setJdbcUrl("jdbc:mysql://" + dotenv.get("DB_HOST") + ":" + dotenv.get("DB_PORT") + "/" + dotenv.get("DB_NAME"));
    config.setUsername(dotenv.get("DB_USER"));
    config.setPassword(dotenv.get("DB_PWD"));
    config.setMaximumPoolSize(5);

    // Create the HikariCP data source
    this.dataSource = new HikariDataSource(config);

    // Create the EntityManagerFactory for JPA (Hibernate)
    this.entityManagerFactory = Persistence.createEntityManagerFactory("ZephyrPU");

    // Initialize Flyway for database migration
    Flyway flyway = Flyway.configure().dataSource(dataSource).load();
    flyway.migrate();
  }

  /**
   * Get a connection from the HikariCP data source.
   *
   * @return Connection object
   * @throws SQLException if a database access error occurs
   */
  public Connection getConnection() throws SQLException {
    return dataSource.getConnection();
  }

  /**
   * Get an EntityManager from the EntityManagerFactory.
   *
   * @return EntityManager object
   */
  public EntityManager getEntityManager() {
    return entityManagerFactory.createEntityManager();
  }

  /**
   * Asynchronous database initialization.
   * Flyway migration is already handled in the constructor.
   *
   * @param resultHandler Handler for the result of the initialization
   */
  public void init(Handler<AsyncResult<Void>> resultHandler) {
    // Flyway migration is already handled in the constructor
    resultHandler.handle(io.vertx.core.Future.succeededFuture());
  }
}
