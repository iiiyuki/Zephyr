package Zephyr;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;

/**
 * @author binaryYuki
 */
public class MainVerticle extends AbstractVerticle {

  @Override
  public void start(Promise<Void> startPromise) throws Exception {
    Router router = Router.router(vertx);

    // Global middleware to log processing time
    router.route().handler(this::logProcessingTime);

    // middleware: X-processing-time
    router.route().handler(ctx -> {
      long startTime = System.currentTimeMillis();
      ctx.addBodyEndHandler(v -> {
        long processingTime = System.currentTimeMillis() - startTime;
        ctx.response().putHeader("X-processing-time", String.valueOf(processingTime));
      });
      ctx.next();
    });

    // middleware: X-Request-ID
    router.route().handler(ctx -> {
      String requestId = ctx.request().getHeader("X-Request-ID");
      if (requestId == null) {
        requestId = java.util.UUID.randomUUID().toString();
        ctx.request().headers().add("X-Request-ID", requestId);
      }
      ctx.response().putHeader("X-Request-ID", requestId);
      ctx.next();
    });

    router.errorHandler(500, ctx -> {
      // 检查响应是否已经结束
      if (!ctx.response().ended()) {
        JsonObject response = new JsonObject()
          .put("success", false)
          .put("status", "error")
          .put("message", "Internal Server Error, please try again later")
          .put("requestId", ctx.request().getHeader("X-Request-ID"))
          .put("timestamp", System.currentTimeMillis());

        ctx.response()
          .setStatusCode(500)
          .putHeader("Content-Type", "application/json")
          .end(response.encode());
      } else {
        // 如果响应已经发送，记录日志
        System.err.println("Response already sent, cannot send error response again.");
      }
    });

    router.route("/").handler(ctx -> {
      JsonObject response = new JsonObject()
        .put("status", "ok")
        .put("message", "success")
        .put("timestamp", System.currentTimeMillis());

      ctx.response()
        .putHeader("Content-Type", "application/json")
        .end(response.encode());
    });

    // Define the new path /healthz
    router.route("/healthz").handler(ctx -> {
      JsonObject response = new JsonObject()
        .put("status", "ok")
        .put("message", "success")
        .put("timestamp", System.currentTimeMillis());

      ctx.response()
        .putHeader("Content-Type", "application/json")
        .end(response.encode());
    });

    vertx.createHttpServer().requestHandler(router).listen(8888).onComplete(http -> {
      if (http.succeeded()) {
        startPromise.complete();
        System.out.println("HTTP server started on port 8888");
      } else {
        startPromise.fail(http.cause());
      }
    });
  }

  private void logProcessingTime(RoutingContext context) {
    long startTime = System.currentTimeMillis();
    context.addBodyEndHandler(v -> {
      long processingTime = System.currentTimeMillis() - startTime;
      System.out.println("Processing time: " + processingTime + "ms");
    });
    context.next();
  }
}
