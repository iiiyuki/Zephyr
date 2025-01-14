package Zephyr;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;

/**
 * Main Verticle for the application
 * @author binaryYuki
 */
public class MainVerticle extends AbstractVerticle {

  @Override
  public void start(Promise<Void> startPromise) {
    // Initialize dbHelper with Vertx instance
    dbHelper db = new dbHelper(vertx);

    // Initialize the database (create table if not exists)
    db.init(ar -> {
      if (ar.succeeded()) {
        System.out.println("Database initialized successfully.");
        setupHttpServer(startPromise, db);
      } else {
        System.err.println("Failed to initialize database: " + ar.cause().getMessage());
        startPromise.fail(ar.cause());
      }
    });
  }

  private void setupHttpServer(Promise<Void> startPromise, dbHelper db) {
    // Create the main Router
    Router router = Router.router(vertx);

    // Create Jack and Austin route instances
    JackRoutes jackRoutes = new JackRoutes(vertx);
    AustinRoutes austinRoutes = new AustinRoutes(vertx);

    // Mount sub-routers to "/api/jack" and "/api/austin"
    router.route("/api/jack/*").subRouter(jackRoutes.getSubRouter());
    router.route("/api/austin/*").subRouter(austinRoutes.getSubRouter());

    // Set up global error handlers
    setupErrorHandlers(router);

    // Health check endpoint
    router.route("/healthz").handler(ctx -> {
      // Optional: Check database connection status dynamically
      db.getClient().query("SELECT 1").execute(ar -> {
        if (ar.succeeded()) {
          ctx.response()
            .putHeader("Content-Type", "application/json")
            .end("{\"status\": \"ok\"}");
        } else {
          ctx.response()
            .setStatusCode(500)
            .putHeader("Content-Type", "application/json")
            .end("{\"status\": \"error\", \"message\": \"Database connection failed\"}");
        }
      });
    });

    // Create HTTP server and bind the router
    vertx.createHttpServer().requestHandler(router).listen(8888).onComplete(http -> {
      if (http.succeeded()) {
        startPromise.complete();
        System.out.println("HTTP server started on port 8888");
      } else {
        startPromise.fail(http.cause());
      }
    });
  }


  private void setupErrorHandlers(Router router) {
    // 500 Internal Server Error
    router.errorHandler(500, ctx -> {
      JsonObject error = new JsonObject()
        .put("success", false)
        .put("error", "Server Side Error")
        .put("message", ctx.failure() != null ? ctx.failure().getMessage() : "Unknown error");
      if (!ctx.response().ended()) {
        ctx.response()
          .setStatusCode(500)
          .putHeader("Content-Type", "application/json")
          .end(error.encode());
      }
    });

    // 404 errors
    router.errorHandler(404, ctx -> {
      JsonObject error = new JsonObject()
        .put("success", false)
        .put("error", "Not Found")
        .put("message", "The requested resource was not found.");
      if (!ctx.response().ended()) {
        ctx.response()
          .setStatusCode(404)
          .putHeader("Content-Type", "application/json")
          .end(error.encode());
      }
    });

    // 403
    router.errorHandler(403, ctx -> {
      JsonObject error = new JsonObject()
        .put("success", false)
        .put("error", "Forbidden")
        .put("message", "You do not have permission to access this resource.");
      if (!ctx.response().ended()) {
        ctx.response()
          .setStatusCode(403)
          .putHeader("Content-Type", "application/json")
          .end(error.encode());
      }
    });

    // 401 Unauthorized
    router.errorHandler(401, ctx -> {
      JsonObject error = new JsonObject()
        .put("success", false)
        .put("error", "Unauthorized")
        .put("message", "You are not authorized to access this resource.");
      if (!ctx.response().ended()) {
        ctx.response()
          .setStatusCode(401)
          .putHeader("Content-Type", "application/json")
          .end(error.encode());
      }
    });

    // 400
    router.errorHandler(400, ctx -> {
      JsonObject error = new JsonObject()
        .put("success", false)
        .put("error", "Bad Request")
        .put("message", "The request was invalid or cannot be served.");
      if (!ctx.response().ended()) {
        ctx.response()
          .setStatusCode(400)
          .putHeader("Content-Type", "application/json")
          .end(error.encode());
      }
    });
  }
}
