package Zephyr;

import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;

/**
 * @author Austin Ma
 */
public class AustinRoutes {

  private final Vertx vertx;

  // 构造函数，接收 Vert.x 实例
  public AustinRoutes(Vertx vertx) {
    this.vertx = vertx;
  }

  // 创建并返回一个子路由器
  public Router getSubRouter() {
    Router router = Router.router(vertx);

    // 定义 "/api/austin/healthz" 路径
    router.route("/healthz").handler(this::handleHealthz);

    // 定义 "/api/austin/status" 路径
    router.route("/status").handler(this::handleStatus);

    return router;
  }

  // 处理 "/api/austin/healthz" 路径的逻辑
  private void handleHealthz(RoutingContext ctx) {
    JsonObject response = new JsonObject()
      .put("status", "ok")
      .put("message", "Austin's health check is successful!")
      .put("timestamp", System.currentTimeMillis());

    ctx.response()
      .putHeader("Content-Type", "application/json")
      .end(response.encode());
  }

  // 处理 "/api/austin/status" 路径的逻辑
  private void handleStatus(RoutingContext ctx) {
    JsonObject response = new JsonObject()
      .put("status", "ok")
      .put("message", "Austin's status is good!")
      .put("timestamp", System.currentTimeMillis());

    ctx.response()
      .putHeader("Content-Type", "application/json")
      .end(response.encode());
  }
}
