package Zephyr;

import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpClientRequest;
import io.vertx.core.http.HttpClientResponse;
import io.vertx.core.json.JsonObject;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(VertxExtension.class)
public class TestJackRoutes {

  @BeforeEach
  void deploy_verticle(Vertx vertx, VertxTestContext testContext) {
    vertx.deployVerticle(new MainVerticle()).onComplete(testContext.succeeding(id -> testContext.completeNow()));
  }

  @Test
  void verticle_deployed(Vertx vertx, VertxTestContext testContext) throws Throwable {
    testContext.completeNow();
  }

  @Test
  void testRoot(Vertx vertx, VertxTestContext testContext) {
    // 创建 HTTP 客户端并发送 GET 请求到 "/api/jack/"
    vertx.createHttpClient()
      .request(io.vertx.core.http.HttpMethod.GET, 8888, "127.0.0.1", "/api/jack/")
      .compose(HttpClientRequest::send) // 发送请求
      .onSuccess(resp -> handleResponse(resp, testContext)) // 处理成功响应
      .onFailure(testContext::failNow); // 处理请求失败
  }

  private void handleResponse(HttpClientResponse resp, VertxTestContext testContext) {
    testContext.verify(() -> {
      // 验证 HTTP 状态码
      assertEquals(200, resp.statusCode(), "HTTP status code should be 200");

      // 读取响应体
      resp.body()
        .onSuccess(buffer -> handleResponseBody(buffer, testContext)) // 处理响应体
        .onFailure(testContext::failNow); // 如果 body() 失败
    });
  }

  private void handleResponseBody(Buffer body, VertxTestContext testContext) {
    testContext.verify(() -> {
      // 将响应体解析为 JSON 对象
      JsonObject responseJson = body.toJsonObject();

      // should be {"success":true,"status":"ok","timestamp":1736940859504}
      // 验证响应内容
      assertEquals(true, responseJson.getBoolean("success"), "Success should be true");
      assertEquals("ok", responseJson.getString("status"), "Status should be 'ok'");

      // 验证时间戳是否存在
      assertNotNull(responseJson.getLong("timestamp"), "Timestamp should not be null");

      // 标记测试完成
      testContext.completeNow();
    });
  }
}
