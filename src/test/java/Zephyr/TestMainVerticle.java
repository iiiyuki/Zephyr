package Zephyr;

import io.vertx.core.Vertx;
import io.vertx.core.http.HttpClientRequest;
import io.vertx.core.json.JsonObject;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(VertxExtension.class)
public class TestMainVerticle {

  @BeforeEach
  void deploy_verticle(Vertx vertx, VertxTestContext testContext) {
    vertx.deployVerticle(new MainVerticle()).onComplete(testContext.succeeding(id -> testContext.completeNow()));
  }

  @Test
  void verticle_deployed(Vertx vertx, VertxTestContext testContext) throws Throwable {
    testContext.completeNow();
  }

  @Test
  void testHealthz(Vertx vertx, VertxTestContext testContext) {
    vertx.createHttpClient()
      .request(io.vertx.core.http.HttpMethod.GET, 8888, "127.0.0.1", "/healthz")
      .compose(HttpClientRequest::send)
      .onSuccess(resp -> {
        testContext.verify(() -> {
          // 检查 HTTP 状态码
          assertEquals(200, resp.statusCode(), "HTTP status code should be 200");

          // 检查响应内容
          resp.body().onSuccess(body -> {
            JsonObject responseJson = body.toJsonObject();
            assertEquals("ok", responseJson.getString("status"), "Status should be 'ok'");
            assertEquals("Health check passed", responseJson.getString("message"), "Message should be 'Health check passed'");

            JsonObject database = responseJson.getJsonObject("database");
            assertNotNull(database, "Database object should not be null");
            assertTrue(database.getBoolean("success"), "Database success should be true");
            assertEquals("Database connection is healthy", database.getString("message"), "Database message should match");

            // 检查时间戳是否存在
            assertNotNull(responseJson.getLong("timestamp"), "Timestamp should not be null");

            testContext.completeNow();
          }).onFailure(testContext::failNow); // 如果 body() 失败
        });
      })
      .onFailure(testContext::failNow); // 如果 request 或 send() 失败
  }
}
