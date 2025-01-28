package Zephyr;

import Zephyr.caches.ValKeyManager;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import jakarta.persistence.EntityManagerFactory;

import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Main Verticle for the application
 * @author binaryYuki
 */
public class MainVerticle extends AbstractVerticle {


  private DatabaseQueue databaseQueue;
  public static dbHelper dbHelperInstance;

  @Override
  public void start() {
    dbHelperInstance = new dbHelper(vertx);
    databaseQueue = new DatabaseQueue(5);
    dbHelperInstance.init(ar -> {
      if (ar.succeeded()) {
        System.out.println("Database initialized successfully.");
        setupHttpServer(Promise.promise(), dbHelperInstance);
      } else {
        System.err.println("Failed to initialize database: " + ar.cause().getMessage());

      }
    });
  }


  @Override
  public void stop() {
    if (dbHelperInstance != null) {
      dbHelperInstance.close();
    }
  }

  public static dbHelper getDbHelperInstance() {
    return dbHelperInstance;
  }

//  @Override
//  public void start(Promise<Void> startPromise) {
//    // Initialize dbHelper with Vertx instance
//    dbHelper db = new dbHelper(vertx);
//    entityManager = dbHelper.getEntityManagerFactory().createEntityManager();
//    ValKeyManager valKeyManager = ValKeyManager.getInstance();
//
//    // test valkey connection
//    valKeyManager.set("test", "test");
//    String test = valKeyManager.get("test");
//    if (test == null) {
//      System.err.println("Failed to connect to ValKey.");
//      startPromise.fail("Failed to connect to ValKey.");
//      return;
//    } else if (!"test".equals(test)) {
//      System.err.println("Failed to connect to ValKey.");
//      startPromise.fail("Failed to connect to ValKey.");
//      return;
//    }
//
//    // Initialize the database (create table if not exists)
//    db.init(ar -> {
//      if (ar.succeeded()) {
//        System.out.println("Database initialized successfully.");
//        setupHttpServer(startPromise, db);
//      } else {
//        System.err.println("Failed to initialize database: " + ar.cause().getMessage());
//        startPromise.fail(ar.cause());
//      }
//    });
//
//    // Initialize DatabaseQueue
//    databaseQueue = new DatabaseQueue(5);
//  }

  private void setupHttpServer(Promise<Void> startPromise, dbHelper db) {
    return promise.future();
  }

  private Router setupHttpServer() {
    // Create the main Router
    Router router = Router.router(vertx);

    // 通用路由配置
    router.route().handler(ctx -> {
      String requestId = IdGenerator.generateRequestId();
      ctx.response().putHeader("X-Request-Id", requestId);
      logger.debug("Request ID generated: {}");
      ctx.next();
    });

    // 添加 BodyHandler
    router.route().handler(BodyHandler.create()
        .setBodyLimit(50_000)
        .setDeleteUploadedFilesOnEnd(true)
        .setHandleFileUploads(true)
        .setUploadsDirectory(Paths.get("Zephyr", "uploads").toString())
        .setMergeFormAttributes(true)
      );

      // 配置子路由
      router.route("/api/jack/*").subRouter(new JackRoutes(vertx).getSubRouter());
      router.route("/api/austin/*").subRouter(new AustinRoutes(vertx).getSubRouter());
      router.route("/api/v1/*").subRouter(new YukiRoutes(vertx).getSubRouter());


    // Create Jack and Austin route instances
    JackRoutes jackRoutes = new JackRoutes(vertx);
    AustinRoutes austinRoutes = new AustinRoutes(vertx);

    // Mount sub-routers to "/api/jack" and "/api/austin"
    router.route("/api/jack/*").subRouter(jackRoutes.getSubRouter());
    router.route("/api/austin/*").subRouter(austinRoutes.getSubRouter());
    router.route("/api/v1/*").subRouter(new YukiRoutes(vertx).getSubRouter());
    router.route("/api").handler(ctx -> ctx.response()
      .putHeader("Content-Type", "application/json")
      .end(new JsonObject().put("message", "success").encode()));

    // Set up global error handlers
    setupErrorHandlers(router);

    // Health check endpoint
    router.route("/healthz").handler(ctx -> {
      JsonObject responseObject = new JsonObject();
      JsonObject dbStatus = new JsonObject();
      JsonObject valKeyStatus = new JsonObject();

      // 执行数据库查询
      try {
        dbHelperInstance.getConnection().createStatement().executeQuery("SELECT 1");
      } catch (SQLException e) {
        throw new RuntimeException(e);
      }

      // 添加健康检查路由
      router.get("/healthz").handler(this::handleHealthCheck);


      // 错误处理
      setupErrorHandlers(router);

      return router;
    }

  private void handleHealthCheck(RoutingContext ctx) {
    JsonObject responseObject = new JsonObject();

    // 检查数据库状态
    JsonObject dbStatus = new JsonObject();
    try (Connection connection = dbHelper.getDataSource().getConnection()) {
      try (PreparedStatement stmt = connection.prepareStatement("SELECT 1")) {
        ResultSet rs = stmt.executeQuery();
        if (rs.next() && rs.getInt(1) == 1) {
          dbStatus.put("success", true).put("message", "Database is healthy.");
        } else {
          dbStatus.put("success", false).put("message", "Unexpected query result.");
        }
      }
    } catch (SQLException e) {
      dbStatus.put("success", false).put("message", "Database connection failed: " + e.getMessage());
      logger.warn("Database health check failed.", e);
    }

    // 检查 ValKey 状态
    JsonObject valKeyStatus = new JsonObject();
    ValKeyManager valKeyManager = ValKeyManager.getInstance();
    String key = "health@" + System.currentTimeMillis();
    valKeyManager.set(key, "healthy");
    if ("healthy".equals(valKeyManager.get(key))) {
      valKeyStatus.put("success", true).put("message", "ValKey is healthy.");
      valKeyManager.del(key);
    } else {
      valKeyStatus.put("success", false).put("message", "ValKey is not accessible.");
      logger.warn("ValKey health check failed.");
    }

    responseObject.put("status", "ok")
      .put("database", dbStatus)
      .put("valkey", valKeyStatus)
      .put("timestamp", System.currentTimeMillis());

    ctx.response().putHeader("Content-Type", "application/json").end(responseObject.encode());
  }

  private void setupErrorHandlers(Router router) {
    // 500 Internal Server Error
    router.errorHandler(500, ctx -> {
      JsonObject error = new JsonObject()
        .put("success", false)
        .put("error", "Server Side Error")
        .put("message", ctx.failure() != null ? ctx.failure().getMessage() : "Unknown error");
      if (!ctx.response().ended()) {
        ctx.response()
          .setStatusCode(500)
          .putHeader("Content-Type", "application/json")
          .end(error.encode());
      }
    });

    // 404 errors
    router.errorHandler(404, ctx -> {
      JsonObject error = new JsonObject()
        .put("success", false)
        .put("error", "Not Found")
        .put("message", "The requested resource was not found.");
      if (!ctx.response().ended()) {
        ctx.response()
          .setStatusCode(404)
          .putHeader("Content-Type", "application/json")
          .end(error.encode());
      }
    });

    // 403
    router.errorHandler(403, ctx -> {
      JsonObject error = new JsonObject()
        .put("success", false)
        .put("error", "Forbidden")
        .put("message", "You do not have permission to access this resource.");
      if (!ctx.response().ended()) {
        ctx.response()
          .setStatusCode(403)
          .putHeader("Content-Type", "application/json")
          .end(error.encode());
      }
    });

    // 401 Unauthorized
    router.errorHandler(401, ctx -> {
      JsonObject error = new JsonObject()
        .put("success", false)
        .put("error", "Unauthorized")
        .put("message", "You are not authorized to access this resource.");
      if (!ctx.response().ended()) {
        ctx.response()
          .setStatusCode(401)
          .putHeader("Content-Type", "application/json")
          .end(error.encode());
      }
    });

    // 400
    router.errorHandler(400, ctx -> {
      JsonObject error = new JsonObject()
        .put("success", false)
        .put("error", "Bad Request")
        .put("message", "The request was invalid or cannot be served.");
      if (!ctx.response().ended()) {
        ctx.response()
          .setStatusCode(400)
          .putHeader("Content-Type", "application/json")
          .end(error.encode());
      }
    });
  }


}
