package Zephyr;

import Zephyr.entities.AcceptedSequences;
import Zephyr.entities.Service;
import Zephyr.entities.Uploads;
import io.vertx.ext.web.FileUpload;
import io.vertx.ext.web.RequestBody;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.handler.BodyHandler;
import jakarta.persistence.EntityManager;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.*;
import java.util.*;

import static Zephyr.MainVerticle.dbHelperInstance;


/**
 * @author Jingyu Wang
 */
public class JackRoutes {

  private final Vertx vertx;

  // 构造函数，接收 Vert.x 实例
  public JackRoutes(Vertx vertx) {
    this.vertx = vertx;
  }

  // 创建并返回一个子路由器
  public Router getSubRouter() {
    Router router = Router.router(vertx);

    // 定义 "/api/jack" 路径
    router.route("/").handler(this::handleRoot);

    // 定义 "/api/jack/info" 路径
    router.route("/info").handler(this::handleInfo);

    // test orm
    router.route("/testOrm").handler(this::testOrm);

    router.route().handler(BodyHandler.create()
      .setBodyLimit(50000)
      //处理后自动移除
      .setDeleteUploadedFilesOnEnd(true)
      .setHandleFileUploads(true)
      .setUploadsDirectory(Paths.get("Zephyr", "uploads").toString())
    );

    router.post("/analyze/text/uploads").handler(ctx -> {
      List<FileUpload> fileUploads = ctx.fileUploads();
      if (fileUploads == null || fileUploads.size() != 1) {
        JsonObject response = new JsonObject()
          .put("status", "error")
          .put("message", "File upload incorrect");
        ctx.response()
          .putHeader("Content-Type", "application/json")
          .setStatusCode(400)
          .end(response.encode());
      }
      else{
        File f = new File(fileUploads.getFirst().uploadedFileName());
        try {
          handleFileUpload(ctx, f);
        } catch (IOException e) {
          throw new RuntimeException(e);
        }
      }
    });

    router.get("/analyze/text/uploads").handler(ctx -> {
      // get method return html form for uploading text files
      ctx.response().putHeader("Content-Type", "text/html")
        .end("""
          <form action="/api/jack/analyze/text/uploads" method="post" enctype="multipart/form-data">
            <input type="file" name="file" accept=".txt">
            <input type="submit" value="Upload">
          </form>""");
    });


    /**
     * 添加一个关键词
     * 前端传入Json对象，提取其input字段，写入数据库
     */
    router.post("/submit/keyword").handler(this::handleKeywordSubmit);

    return router;
  }


  // 处理 "/api/jack" 路径的逻辑
  private void handleRoot(RoutingContext ctx) {
    JsonObject response = new JsonObject()
      .put("status", "ok")
      .put("message", "ready")
      .put("timestamp", System.currentTimeMillis());

    ctx.response()
      .putHeader("Content-Type", "application/json")
      .end(response.encode());
  }

  // 处理 "/api/jack/info" 路径的逻辑
  private void handleInfo(RoutingContext ctx) {
    JsonObject response = new JsonObject()
      .put("status", "ok")
      .put("message", "This is Jack's info endpoint!")
      .put("timestamp", System.currentTimeMillis());

    ctx.response()
      .putHeader("Content-Type", "application/json")
      .end(response.encode());
  }

  private void handleKeywordSubmit(RoutingContext ctx) {
    // 获取请求体
    JsonObject object = ctx.body().asJsonObject();
    String keyword = object.getString("input").toLowerCase();
    JsonObject updateBias = new JsonObject();

    try (Connection connection = dbHelper.getDataSource().getConnection()) {
      // 首先检查是否已经有匹配的记录
      String selectQuery = "SELECT rate FROM accepted_sequences WHERE content = ?";
      try (PreparedStatement selectStmt = connection.prepareStatement(selectQuery)) {
        selectStmt.setString(1, keyword);
        ResultSet rs = selectStmt.executeQuery();

        if (rs.next()) {
          // 如果记录已存在，更新 rate
          int newRate = rs.getInt("rate") + 1;
          String updateQuery = "UPDATE accepted_sequences SET rate = ?, last_updated_at = ? WHERE content = ?";
          try (PreparedStatement updateStmt = connection.prepareStatement(updateQuery)) {
            updateStmt.setInt(1, newRate);
            updateStmt.setLong(2, System.currentTimeMillis());
            updateStmt.setString(3, keyword);
            updateStmt.executeUpdate();
          }

          updateBias.put("success", true)
            .put("content", keyword)
            .put("rate", newRate)
            .put("timestamp", System.currentTimeMillis());
        } else {
          // 如果记录不存在，插入新记录
          String insertQuery = "INSERT INTO accepted_sequences (content, rate, created_at, last_updated_at) VALUES (?, ?, ?, ?)";
          try (PreparedStatement insertStmt = connection.prepareStatement(insertQuery)) {
            insertStmt.setString(1, keyword);
            insertStmt.setInt(2, 1);
            insertStmt.setLong(3, System.currentTimeMillis());
            insertStmt.setLong(4, System.currentTimeMillis());
            insertStmt.executeUpdate();
          }

          updateBias.put("success", true)
            .put("content", keyword)
            .put("rate", 1)
            .put("timestamp", System.currentTimeMillis());
        }
      }
    } catch (SQLException e) {
      updateBias.put("success", false).put("message", "Database error: " + e.getMessage());
    }

    // 将结果返回给客户端
    ctx.response()
      .putHeader("Content-Type", "application/json")
      .end(updateBias.encode());
  }

  //关键词检测器 A naive approach of a text-based fraud detector.
  private void handleFileUpload(RoutingContext ctx, File f) throws IOException {
    if(!f.getName().endsWith(".txt")){
      JsonObject response = new JsonObject()
        .put("status", "error")
        .put("message", "File type incorrect");
      ctx.response()
        .putHeader("Content-Type", "application/json")
        .setStatusCode(400)
        .end(response.encode());
    }
    BufferedReader br = new BufferedReader(new FileReader(f));
    String line;
    int total = 0;
    while ((line = br.readLine()) != null) {
      try (Connection connection = dbHelper.getDataSource().getConnection()) {
        // 提取所有记录
        String selectQuery = "SELECT * FROM accepted_sequences";
        try (PreparedStatement selectStmt = connection.prepareStatement(selectQuery)) {
          ResultSet rs = selectStmt.executeQuery();
          rs.first();
          while(!rs.isLast()){
            if(line.contains(rs.getString("content"))){
              total += rs.getInt("rate");
            }
            rs.next();
          }
          if(rs.isLast()&&line.contains(rs.getString("content"))){
            total += rs.getInt("rate");
          }
        }
      } catch (SQLException e) {
        JsonObject response = new JsonObject()
          .put("status", "error")
          .put("message", "Failed to process the file: " + e.getMessage());
        ctx.response()
          .putHeader("Content-Type", "application/json")
          .end(response.encode());
      }
    }
    if(total>=20){
      JsonObject response = new JsonObject()
        .put("status", "ok")
        .put("result", "alarmed")
        .put("timestamp", System.currentTimeMillis());

      ctx.response()
        .putHeader("Content-Type", "application/json")
        .end(response.encode());
    }
    else{
      JsonObject response = new JsonObject()
        .put("status", "ok")
        .put("result", "unalarmed")
        .put("timestamp", System.currentTimeMillis());

      ctx.response()
        .putHeader("Content-Type", "application/json")
        .end(response.encode());
    }
  }

  // orm test
  private void testOrm(RoutingContext ctx) {
    // 假设要查找或更新 ID 为 1 的实体
    Long id = 1L;

    // Get the entity manager
    EntityManager entityManager = dbHelperInstance.getEntityManager();

    try {
      // Begin a transaction
      entityManager.getTransaction().begin();

      // 查找现有的服务
      Service existingService = entityManager.find(Service.class, id);
      if (existingService != null) {
        // 更新现有服务
        existingService.setName("Updated Service Name@" + System.currentTimeMillis());
        existingService.setDescription("Updated Description");
        existingService.setStatus("inactive");
      } else {
        // 如果服务不存在，则创建一个新服务
        Service newService = new Service();
        newService.setName("New Service");
        newService.setDescription("New Description");
        newService.setStatus("active");

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
      return;
    } finally {
      // Close the entity manager
      entityManager.close();
    }

    // 新EntityManager
    entityManager = dbHelperInstance.getEntityManager();
    // 从数据库中查询所有服务
    List<Service> services = entityManager.createQuery("SELECT s FROM Service s", Service.class).getResultList();
    JsonObject response = new JsonObject()
      .put("status", "ok")
      .put("message", "Test ORM Success")
      .put("timestamp", System.currentTimeMillis())
      .put("services", services);
    ctx.response()
      .putHeader("Content-Type", "application/json")
      .end(response.encode());
    entityManager.close();
  }
}



