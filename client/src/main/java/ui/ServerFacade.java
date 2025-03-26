package ui;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import model.AuthData;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import server.handlers.*;

public class ServerFacade {
    private final HttpClient http;
    private final String baseUrl;

    public ServerFacade(int port){
        this.baseUrl = "http://localhost:" + port;
        this.http = HttpClient.newHttpClient();
    }

    public RegisterResult register(RegisterRequest request) throws Exception{
        String endpoint = baseUrl + "/user";
        Gson gson = new Gson();
        JsonObject json = new JsonObject();
        json.addProperty("username", request.username());
        json.addProperty("password", request.password());
        json.addProperty("email", request.email());
        String requestBody = json.toString();

        HttpRequest httpRequest = HttpRequest.newBuilder().uri(new URI(endpoint)).header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();

        HttpResponse<String> response = http.send(httpRequest, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200){
            throw new Exception("Registration failed");
        }
        return gson.fromJson(response.body(), RegisterResult.class);
    }


}

