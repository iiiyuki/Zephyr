package Zephyr;

import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;

/**
 * @author binaryYuki
 */
public class Routes {

  private final Vertx vertx;

  // 构造函数，接收 Vert.x 实例
  public Routes(Vertx vertx) {
    this.vertx = vertx;
  }

  // 定义路由的方法
  public Router createRouter() {
    Router router = Router.router(vertx);

    // 添加全局中间件
    router.route().handler(this::logProcessingTime);
    router.route().handler(this::addProcessingTimeHeader);
    router.route().handler(this::addRequestIdHeader);

    // 定义 "/" 路径
    router.route("/").handler(this::handleRoot);

    // 定义 "/healthz" 路径
    router.route("/healthz").handler(this::handleHealthz);

    return router;
  }

  // 日志中间件
  private void logProcessingTime(RoutingContext ctx) {
    long startTime = System.currentTimeMillis();
    ctx.addBodyEndHandler(v -> {
      long processingTime = System.currentTimeMillis() - startTime;
      System.out.println("Processing time: " + processingTime + "ms");
    });
    ctx.next();
  }

  // 添加 X-processing-time 中间件
  private void addProcessingTimeHeader(RoutingContext ctx) {
    long startTime = System.currentTimeMillis();
    ctx.addBodyEndHandler(v -> {
      long processingTime = System.currentTimeMillis() - startTime;
      ctx.response().putHeader("X-processing-time", String.valueOf(processingTime));
    });
    ctx.next();
  }

  // 添加 X-Request-ID 中间件
  private void addRequestIdHeader(RoutingContext ctx) {
    String requestId = ctx.request().getHeader("X-Request-ID");
    if (requestId == null) {
      requestId = java.util.UUID.randomUUID().toString();
      ctx.request().headers().add("X-Request-ID", requestId);
    }
    ctx.response().putHeader("X-Request-ID", requestId);
    ctx.next();
  }

  // 处理 "/" 路径
  private void handleRoot(RoutingContext ctx) {
    JsonObject response = new JsonObject()
      .put("status", "ok")
      .put("message", "Welcome to the root path!")
      .put("timestamp", System.currentTimeMillis());

    ctx.response()
      .putHeader("Content-Type", "application/json")
      .end(response.encode());
  }

  // 处理 "/healthz" 路径
  private void handleHealthz(RoutingContext ctx) {
    JsonObject response = new JsonObject()
      .put("status", "ok")
      .put("message", "Service is healthy!")
      .put("timestamp", System.currentTimeMillis());

    ctx.response()
      .putHeader("Content-Type", "application/json")
      .end(response.encode());
  }
}
