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
    router.route("/api/jack/*").subRouter(jackRoutes.getSubRouter());
    router.route("/api/austin/*").subRouter(austinRoutes.getSubRouter());

    // 设置全局错误处理器
    router.errorHandler(500, ctx -> {
      JsonObject error = new JsonObject()
        .put("success", false)
        .put("error", "Server Side Error")
        .put("message", ctx.failure().getMessage());
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

    // 401
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
