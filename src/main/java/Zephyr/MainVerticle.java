package Zephyr;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;

public class MainVerticle extends AbstractVerticle {

  @Override
  public void start(Promise<Void> startPromise) throws Exception {
    // 创建主 Router
    Router router = Router.router(vertx);

    // 创建 Jack 和 Austin 的路由实例
    JackRoutes jackRoutes = new JackRoutes(vertx);
    AustinRoutes austinRoutes = new AustinRoutes(vertx);

    // 挂载子路由到 "/api/jack" 和 "/api/austin"
    router.mountSubRouter("/api/jack", jackRoutes.getSubRouter());
    router.mountSubRouter("/api/austin", austinRoutes.getSubRouter());

    // 设置全局错误处理器
    router.errorHandler(500, ctx -> {
      JsonObject error = new JsonObject()
        .put("error", "Internal Server Error")
        .put("success", false);
      if (!ctx.response().ended()) {
        ctx.response()
          .setStatusCode(500)
          .putHeader("Content-Type", "application/json")
          .end("{\"error\": \"Internal Server Error\"}");

      }
    });

    // healthz
    router.route("/healthz").handler(ctx -> {
      ctx.response()
        .putHeader("Content-Type", "application/json")
        .end("{\"status\": \"ok\"}");
    });

    // 创建 HTTP 服务器并绑定路由器
    vertx.createHttpServer().requestHandler(router).listen(8888).onComplete(http -> {
      if (http.succeeded()) {
        startPromise.complete();
        System.out.println("HTTP server started on port 8888");
      } else {
        startPromise.fail(http.cause());
      }
    });
  }
}
