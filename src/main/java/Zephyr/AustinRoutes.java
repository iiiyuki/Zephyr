package Zephyr;

import io.vertx.core.Future;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.client.WebClientOptions;

import static Zephyr.requests.weather.sendHttpRequest;


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

    // 定义 "/api/austin/status" 路径
    router.route("/status").handler(this::handleStatus);

    // 定义 "/api/austin/weather" 路径
    router.route("/weather").handler(this::handleWeather);

    return router;
  }

  // 处理 "/api/austin/status" 路径的逻辑
  private void handleStatus(RoutingContext ctx) {
    JsonObject response = new JsonObject()
      .put("success", true)
      .put("status", "ok")
      .put("timestamp", System.currentTimeMillis());

    ctx.response()
      .putHeader("Content-Type", "application/json")
      .end(response.encode());
  }

  // 处理 "/api/austin/weather" 路径的逻辑
  private void handleWeather(RoutingContext ctx) {
    String city = ctx.request().getParam("city");
    if (city == null) {
      ctx.response()
        .setStatusCode(400)
        .putHeader("Content-Type", "application/json")
        .end(new JsonObject().put("error", "城市参数缺失").encode());
      return;
    }
    // 配置 WebClient（类似 httpx）
    WebClientOptions options = new WebClientOptions()
       // 设置超时时间
      .setConnectTimeout(5000)
      // 支持 HTTPS
      .setSsl(true);

    WebClient client = WebClient.create(vertx, options);

    // 创建异步任务
    sendHttpRequest(client, "https://api.qster.top/API/v1/weather/?city=" + city)
      .compose(response -> {
        ctx.response()
          .putHeader("Content-Type", "application/json")
          .end(response.encode());
        return Future.succeededFuture();
      }).onFailure(err -> {
        System.err.println("Error: " + err.getMessage());
        ctx.fail(500);
      });

  }
}


