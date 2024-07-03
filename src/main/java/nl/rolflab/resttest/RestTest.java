package nl.rolflab.resttest;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import io.undertow.Undertow;
import io.undertow.server.HttpHandler;
import io.undertow.server.handlers.PathHandler;
import io.undertow.util.Headers;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public final class RestTest extends JavaPlugin {

    private String apiKey;
    private int port;
    private Undertow server;
    private Gson gson;

    @Override
    public void onEnable() {
        super.onEnable();
        loadConfig();

        gson = new GsonBuilder().setPrettyPrinting().create();

        String serverIp = getServer().getIp();
        if (serverIp == null || serverIp.isEmpty()) {
            serverIp = "0.0.0.0";
        }

        server = Undertow.builder()
                .addHttpListener(port, serverIp)
                .setHandler(createHandlers())
                .build();

        server.start();
        getLogger().info("REST server started on " + serverIp + ":" + port);
    }

    @Override
    public void onDisable() {
        if (server != null) {
            server.stop();
            getLogger().info("REST server stopped.");
        }
    }

    @Override
    public void saveDefaultConfig() {
        super.saveDefaultConfig();
    }

    private void loadConfig() {
        reloadConfig();
        FileConfiguration config = getConfig();
        apiKey = config.getString("api-key");
        port = config.getInt("port", 8080);
    }

    private String extractApiKeyFromAuthorizationHeader(io.undertow.server.HttpServerExchange exchange) {
        String authorizationHeader = exchange.getRequestHeaders().getFirst(Headers.AUTHORIZATION);
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            return authorizationHeader.substring("Bearer ".length()).trim();
        }
        return null;
    }

    private HttpHandler createHandlers() {
        PathHandler pathHandler = new PathHandler();

        pathHandler.addExactPath("/api/info", exchange -> {
            String apiKeyHeader = extractApiKeyFromAuthorizationHeader(exchange);
            if (apiKeyHeader != null && apiKeyHeader.equals(apiKey)) {
                handleInfoRequest(exchange);
            } else {
                exchange.setStatusCode(401);
                exchange.getResponseSender().send("Unauthorized: Invalid API key");
            }
        });

        pathHandler.addExactPath("/api/status", exchange -> {
            String apiKeyHeader = extractApiKeyFromAuthorizationHeader(exchange);
            if (apiKeyHeader != null && apiKeyHeader.equals(apiKey)) {
                handleStatusRequest(exchange);
            } else {
                exchange.setStatusCode(401);
                exchange.getResponseSender().send("Unauthorized: Invalid API key");
            }
        });

        pathHandler.addPrefixPath("/api", exchange -> {
            String apiKeyHeader = extractApiKeyFromAuthorizationHeader(exchange);
            if (apiKeyHeader != null && apiKeyHeader.equals(apiKey)) {
                handleInfoRequest(exchange);
            } else {
                exchange.setStatusCode(401);
                exchange.getResponseSender().send("Unauthorized: Invalid API key");
            }
        });

        return pathHandler;
    }


    public JsonObject getPluginInfo() {
        JsonObject info = new JsonObject();
        info.addProperty("plugin", "RestTest");
        info.addProperty("version", "0.1.0");
        return info;
    }

    public JsonObject getServerStatus() {
        JsonObject status = new JsonObject();
        status.addProperty("status", "server is running");
        return status;
    }

    private void handleInfoRequest(io.undertow.server.HttpServerExchange exchange) {
        exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "application/json");
        JsonObject jsonResponse = getPluginInfo();
        exchange.getResponseSender().send(gson.toJson(jsonResponse));
    }

    private void handleStatusRequest(io.undertow.server.HttpServerExchange exchange) {
        exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "application/json");
        exchange.getResponseSender().send(gson.toJson(this.getServerStatus()));
    }
}
