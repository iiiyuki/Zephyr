package Zephyr;

import io.vertx.ext.web.FileUpload;
import io.vertx.ext.web.RequestBody;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.handler.BodyHandler;
import java.util.*;

public class JackRoutes {

  private final Vertx vertx;

  // 构造函数，接收 Vert.x 实例
  public JackRoutes(Vertx vertx) {
    this.vertx = vertx;
  }

  // 创建并返回一个子路由器
  public Router getSubRouter() {
    Router router = Router.router(vertx);

    // 定义 "/api/jack/" 路径
    router.route("/").handler(this::handleRoot);

    // 定义 "/api/jack/info" 路径
    router.route("/info").handler(this::handleInfo);

    router.route().handler(BodyHandler.create()
      .setBodyLimit(100000)
      .setDeleteUploadedFilesOnEnd(true)
      .setHandleFileUploads(true)
      .setUploadsDirectory("C:/Users/a1523/Desktop/Zephyr/docsUploeaded")
      .setMergeFormAttributes(true));

    router.post("/analyze/text/uploads").handler(ctx -> {

      List<FileUpload> uploads = ctx.fileUploads();
      for(FileUpload u:uploads){
        if ("multipart/form-data".equals(u.contentType())
          &&u.fileName().endsWith(".txt")
          && "UTF-8".equals(u.charSet())
          &&u.size()<=100000){
          handleFileUpload(ctx);
        }
        else{
          ctx.fail(401);
        }
      }
    });

    return router;
  }

  // 处理 "/api/jack/" 路径的逻辑
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

  private void handleFileUpload(RoutingContext ctx){
    ctx.fail(400);
  }
}


