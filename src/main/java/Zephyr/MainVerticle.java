package Zephyr;

import Zephyr.caches.ValKeyManager;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import jakarta.persistence.EntityManager;

import java.nio.file.Paths;

/**
 * Main Verticle for the application
 * @author binaryYuki
 */
public class MainVerticle extends AbstractVerticle {

  private DatabaseQueue databaseQueue;

  @Override
  public void start(Promise<Void> startPromise) {
    // Initialize dbHelper with Vertx instance
    dbHelper db = new dbHelper(vertx);
    ValKeyManager valKeyManager = ValKeyManager.getInstance();

    // test valkey connection
    valKeyManager.set("test", "test");
    String test = valKeyManager.get("test");
    if (test == null) {
      System.err.println("Failed to connect to ValKey.");
      startPromise.fail("Failed to connect to ValKey.");
      return;
    } else if (!"test".equals(test)) {
      System.err.println("Failed to connect to ValKey.");
      startPromise.fail("Failed to connect to ValKey.");
      return;
    }

    // Initialize the database (create table if not exists)
    db.init(ar -> {
      if (ar.succeeded()) {
        System.out.println("Database initialized successfully.");
        setupHttpServer(startPromise, db);
      } else {
        System.err.println("Failed to initialize database: " + ar.cause().getMessage());
        startPromise.fail(ar.cause());
      }
    });

    // Initialize DatabaseQueue
    databaseQueue = new DatabaseQueue(5);
  }

  private void setupHttpServer(Promise<Void> startPromise, dbHelper db) {
    // Create the main Router
    Router router = Router.router(vertx);

    // 添加 requestId 和 process time 中间件
    router.route().handler(ctx -> {

      // 生成 requestId 长度：16位
      String requestId = IdGenerator.generateRequestId();

      // 设置响应头中的 requestId
      ctx.response().putHeader("X-Request-Id", requestId);

      if ("/api/jack/analyze/text/uploads".equals(ctx.request().path())) {
        if (ctx.fileUploads().isEmpty()) {
          // 如果没有文件上传，返回 400 错误
          ctx.fail(400);
          return;
        }
      }

      router.route().handler(BodyHandler.create()
        .setBodyLimit(50000)
        //处理后自动移除
        .setDeleteUploadedFilesOnEnd(true)
        .setHandleFileUploads(true)
        .setUploadsDirectory(Paths.get("Zephyr", "uploads").toString())
        .setMergeFormAttributes(true)
      );

      // 调用下一个处理器
      ctx.next();
    });

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
        EntityManager entityManager = dbHelper.getEntityManagerFactory().createEntityManager();
        entityManager.createNativeQuery("SELECT 1").getSingleResult();
        entityManager.close();
        // 数据库连接正常
        dbStatus.put("success", true)
          .put("message", "Database connection is healthy");
      } catch (Exception e) {
        dbStatus.put("success", false)
          .put("message", "Failed to connect to database.");
        responseObject.put("status", "error")
          .put("message", "Health check failed")
          .put("database", dbStatus)
          .put("valkey", valKeyStatus)
          .put("timestamp", System.currentTimeMillis());
        ctx.fail(500);
        return;
      }

      // 执行 valkey 查询
      ValKeyManager valKeyManager = ValKeyManager.getInstance();
      var key = "health@"+System.currentTimeMillis();
      valKeyManager.set(key, "healthy");
      if (!"healthy".equals(valKeyManager.get(key))) {
        valKeyStatus.put("success", false)
          .put("message", "Failed to connect to ValKey.");
      } else {
        valKeyManager.del(key);
        valKeyStatus.put("success", true)
          .put("message", "ValKey connection is healthy")
          .put("key", key)
          .put("value", "healthy");
      }

      // 构建最终的响应对象
      responseObject.put("status", "ok")
        .put("message", "Health check passed")
        .put("database", dbStatus)
        .put("valkey", valKeyStatus)
        .put("timestamp", System.currentTimeMillis());


      // 在异步回调中发送响应
      ctx.response()
        .putHeader("Content-Type", "application/json")
        .end(responseObject.encode());
    });

    vertx.createHttpServer().requestHandler(router).listen(8888).onComplete(http -> {
      if (http.succeeded()) {
        if (!startPromise.future().isComplete()) { // 检查是否已完成
          startPromise.complete();
          System.out.println("HTTP server started on port 8888");
        }
      } else {
        if (!startPromise.future().isComplete()) { // 检查是否已完成
          startPromise.fail(http.cause());
        }
      }
    });
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

  public DatabaseQueue getDatabaseQueue() {
    return databaseQueue;
  }
}
