package Zephyr.requests;


import io.vertx.core.Vertx;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.http.HttpMethod;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.client.WebClientOptions;
import io.vertx.ext.web.client.HttpResponse;
import io.vertx.core.json.JsonObject;

public class weather {

  public static void main(String[] args) {
    // 创建 Vert.x 实例
    Vertx vertx = Vertx.vertx();

    // 配置 WebClient（类似 httpx）
    WebClientOptions options = new WebClientOptions()
      .setConnectTimeout(5000) // 设置超时时间
      .setSsl(true);           // 支持 HTTPS

    WebClient client = WebClient.create(vertx, options);

    // 创建异步任务
    Future<Object> task = sendHttpRequest(client, "https://jsonplaceholder.typicode.com/posts/1")
      .compose(response -> {
        System.out.println("Response: " + response);
        return Future.succeededFuture();
      }).onFailure(err -> {
        System.err.println("Error: " + err.getMessage());
      });

    // 保证 JVM 不提前退出
    task.onComplete(res -> vertx.close());
  }

  // 异步请求方法
  public static Future<JsonObject> sendHttpRequest(WebClient client, String url) {
    Promise<JsonObject> promise = Promise.promise();

    client.requestAbs(HttpMethod.GET, url)
      .send(ar -> {
        if (ar.succeeded()) {
          HttpResponse<io.vertx.core.buffer.Buffer> response = ar.result();
          if (response.statusCode() == 200) {
            // 返回 JSON 响应
            promise.complete(response.bodyAsJsonObject());
          } else {
            promise.fail("HTTP Error: " + response.statusCode());
          }
        } else {
          promise.fail(ar.cause());
        }
      });

    return promise.future();
  }
}
