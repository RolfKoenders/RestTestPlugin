package nl.rolflab.resttest;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import io.undertow.Undertow;
import io.undertow.server.HttpHandler;
import io.undertow.server.RoutingHandler;
import io.undertow.util.Headers;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public final class RestTest extends JavaPlugin {

    private String apiKey;
    private int port;
    private Undertow server;
    private RoutingHandler routingHandler;
    private Gson gson;

    @Override
    public void onEnable() {
        loadConfig();

        gson = new GsonBuilder().setPrettyPrinting().create();
        routingHandler = new RoutingHandler();

        String serverIp = getServer().getIp();
        if (serverIp.isEmpty()) {
            serverIp = "0.0.0.0";
        }

        server = Undertow.builder()
                .addHttpListener(port, serverIp)
                .setHandler(routingHandler)
                .build();

        server.start();
        getLogger().info("REST server started on " + serverIp + ":" + port);

        addRoute("GET", "/info", this::handleInfoRequest);
        addRoute("GET", "/status", this::handleStatusRequest);
    }

    @Override
    public void onDisable() {
        if (server != null) {
            server.stop();
            getLogger().info("REST server stopped.");
        }
    }

    private void loadConfig() {
        FileConfiguration config = getConfig();
        apiKey = config.getString("api-key");
        port = config.getInt("port", 8080);
        saveConfig();
    }

    public void addRoute(String method, String path, HttpHandler handler) {
        switch (method.toUpperCase()) {
            case "GET":
                routingHandler.get(path, handler);
                break;
            case "POST":
                routingHandler.post(path, handler);
                break;
            case "PUT":
                routingHandler.put(path, handler);
                break;
            case "DELETE":
                routingHandler.delete(path, handler);
                break;
            default:
                throw new IllegalArgumentException("Unsupported HTTP method: " + method);
        }
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
