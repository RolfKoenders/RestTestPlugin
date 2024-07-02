package nl.rolflab.resttest;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import okhttp3.OkHttpClient;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

public final class RestTest extends JavaPlugin {

    private String apiKey;
    private int port;
    private MyApi api;

    @Override
    public void onEnable() {
        loadConfig();

        OkHttpClient client = new OkHttpClient.Builder()
                .readTimeout(10, TimeUnit.SECONDS) // Set timeout (optional)
                .build();

        Gson gson = new GsonBuilder().setPrettyPrinting().create();

        String serverIp = getServer().getIp();
        String baseUrl = "https://" + serverIp + ":" + port + "/";

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(baseUrl)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .client(client)
                .build();

        // Create API interface
        api = retrofit.create(MyApi.class);
    }

    // Load configuration from config.yml
    private void loadConfig() {
        FileConfiguration config = getConfig();
        apiKey = config.getString("api-key");
        port = config.getInt("port", 8080);
        saveConfig();
    }

    public interface MyApi {
        @GET("/info")
        retrofit2.Call<String> getInfo();
    }

    public String getPluginInfo() throws IOException {
        retrofit2.Call<String> call = api.getInfo();
        retrofit2.Response<String> response = call.execute();
        if (response.isSuccessful()) {
            return response.body();
        } else {
            throw new IOException("Failed to get info: " + response.errorBody().string());
        }
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
