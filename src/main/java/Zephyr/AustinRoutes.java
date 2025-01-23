package Zephyr;

import Zephyr.entities.Todo;
import io.vertx.core.Future;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.client.WebClientOptions;
import jakarta.persistence.EntityManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import io.vertx.ext.web.handler.BodyHandler;

import java.util.List;

import static Zephyr.requests.weather.sendHttpRequest;


/**
 * @author Austin Ma
 */
public class AustinRoutes {

  private static final Logger log = LoggerFactory.getLogger(AustinRoutes.class);
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

    // 定义 "/api/austin/poem" 路径
    router.route("/poem").handler(this::handlePoem);

    // 定义 "/api/austin/todolist" 路径
    router.post("/todolist").handler(this::handleTodo);
    router.route().handler(BodyHandler.create());


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
        ctx.fail(400);
      });
  }

  //处理 "/api/austin/poem" 路径的逻辑
  private void handlePoem(RoutingContext ctx) {
    // 配置 WebClient（类似 httpx）
    WebClientOptions options = new WebClientOptions()
      // 设置超时时间
      .setConnectTimeout(5000)
      // 支持 HTTPS
      .setSsl(true);

    WebClient client = WebClient.create(vertx, options);

    // 创建异步任务
    sendHttpRequest(client, "https://api.qster.top/API/v1/randpo/")
      .compose(response -> {
        ctx.response()
          .putHeader("Content-Type", "application/json")
          .end(response.encode());
        return Future.succeededFuture();
      }).onFailure(err -> {
        System.err.println("Error: " + err.getMessage());
        ctx.fail(400);
      });
  }

  //处理 "/api/austin/todolist" 路径的逻辑
  private void handleTodo(RoutingContext ctx) {
    ctx.request().bodyHandler(buffer -> {
      JsonObject body = buffer.toJsonObject();
      log.info("Received body: " + body.encode());

      // 获取请求体中的 title 和 id
      String title = body.getString("title");
      Long id = body.getLong("id", null); // 如果 id 不存在，默认为 null

      log.info("Received title: " + title);
      ctx.response()
        .setStatusCode(200)
        .putHeader("Content-Type", "application/json");

      // 获取 EntityManager
      EntityManager entityManager = dbHelper.getEntityManagerFactory().createEntityManager();

      try {
        // 开始事务
        entityManager.getTransaction().begin();

        Todo todo;
        if (id != null) {
          // 如果提供了 ID，则尝试查找现有记录
          todo = entityManager.find(Todo.class, id);
          if (todo != null) {
            log.info("Updating existing Todo with ID: " + id);
            todo.setTitle(title); // 更新现有记录
          } else {
            ctx.response()
              .setStatusCode(404)
              .end(new JsonObject().put("error", "Todo with ID " + id + " not found").encode());
            entityManager.getTransaction().rollback();
            return;
          }
        } else {
          // 如果未提供 ID，则创建新的 Todo
          log.info("Creating a new Todo");
          todo = new Todo();
          todo.setTitle(title);
          entityManager.persist(todo); // 持久化新记录
        }

        // 提交事务
        entityManager.getTransaction().commit();

        // 构建响应
        JsonObject response = new JsonObject()
          .put("status", "ok")
          .put("id", todo.getId())
          .put("title", todo.getTitle());
        ctx.response()
          .putHeader("Content-Type", "application/json")
          .end(response.encode());

      } catch (Exception e) {
        // 事务出错时回滚
        if (entityManager.getTransaction().isActive()) {
          entityManager.getTransaction().rollback();
        }
        ctx.response()
          .setStatusCode(500)
          .end("Error: " + e.getMessage());
      } finally {
        // 关闭 EntityManager
        entityManager.close();
      }
    });
  }

}


