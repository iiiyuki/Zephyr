package Zephyr;

import io.vertx.ext.web.FileUpload;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.handler.BodyHandler;
import java.nio.file.Paths;
import java.nio.file.Path;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

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

    router.route().handler(BodyHandler.create()
      .setBodyLimit(50000)
      .setDeleteUploadedFilesOnEnd(true)
      .setHandleFileUploads(true)
      .setUploadsDirectory("C:/Users/a1523/Desktop/Zephyr/uploads")
      .setMergeFormAttributes(true));

    router.post("/analyze/text/uploads").handler(ctx -> {
      List<FileUpload> uploads = ctx.fileUploads();
      for(FileUpload u:uploads){
        if ("multipart/form-data".equals(u.contentType())
          &&u.fileName().endsWith(".txt")
          &&"UTF-8".equals(u.charSet())
          &&u.size()<=50000){
          handleFileUpload(ctx, u);
        }
        else{
          uploads.remove(u);
        }
      }
    });

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

  private void handleFileUpload(RoutingContext ctx, FileUpload u){
    JsonObject response = new JsonObject()
    .put("status","uploaded")
    .put("dir", "C:\\Users\\a1523\\Desktop\\Zephyr\\uploads" + "\\" + u.fileName())
    .put("timestamp", System.currentTimeMillis());

    ctx.response()
    .putHeader("Content-Type", "application/json")
    .end(response.encode());
  }

  private boolean processFile(Path path) {
    return true;
    /** 施工中：1.Path对象的用法？ 2.数据库如何在本路径建表？
    * List<CharSequence> acceptedSequences = ;
    * String pathName = path.toString();
    * String line;
    * boolean res;
    * try{
      FileReader reader = new FileReader(pathName);
      BufferedReader br = new BufferedReader(reader);
      while((line = br.readLine())!=null){
        for (CharSequence sequence: acceptedSequences){

        }
      }
    } catch (FileNotFoundException e) {
      throw new RuntimeException(e);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
     */
  }
}



