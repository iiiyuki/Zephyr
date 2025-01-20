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

    // 定义 "/api/austin/todo" 路径
    router.post("/todoList").handler(this::handleTodo);
//    router.route().handler(BodyHandler.create());


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

  //处理 "/api/austin/todoList" 路径的逻辑
  private void handleTodo(RoutingContext ctx) {
    ctx.request().bodyHandler(buffer -> {
      JsonObject body = buffer.toJsonObject();
      log.info("Received body: " + body.encode());
      //todo 为什么这里输出为空
      String title = body.getString("title");
      log.info("Received title: " + title);
      //todo 为什么这里输出为空
      ctx.response()
        .setStatusCode(200)
        .putHeader("Content-Type", "application/json");
      // 假设要查找或更新 ID 为 1 的实体
      Long id = 1L;

      // Get the entity manager
      EntityManager entityManager = dbHelper.getEntityManagerFactory().createEntityManager();

      try {
        // Begin a transaction
        entityManager.getTransaction().begin();

        // 查找现有的服务
        Todo exisitingTodo = entityManager.find(Todo.class, id);
        if (exisitingTodo != null) {
          // 更新现有服务
          exisitingTodo.setTitle(title);
        } else {
          // 如果服务不存在，则创建一个新服务
          Todo newService = new Todo();
          newService.setTitle(title);

          // Persist the new service
          entityManager.persist(newService);
        }

        // Commit the transaction
        entityManager.getTransaction().commit();
      } catch (Exception e) {
        // Rollback the transaction in case of errors
        if (entityManager.getTransaction().isActive()) {
          entityManager.getTransaction().rollback();
        }
        ctx.response().setStatusCode(500).end("Error: " + e.getMessage());
      } finally {
        // Close the entity manager
        entityManager.close();
      }

      // 新EntityManager
      entityManager = dbHelper.getEntityManagerFactory().createEntityManager();

      // 从数据库中查询所有服务
      List<Todo> services = entityManager.createQuery("SELECT s FROM Todo s", Todo.class).getResultList();
      JsonObject response = new JsonObject()
        .put("status", "ok")
        .put("title", title);
      entityManager.close();
      ctx.response()
        .putHeader("Content-Type", "application/json")
        .end(response.encode());
  });
  }
}


