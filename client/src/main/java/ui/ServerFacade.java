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

import server.handlers.*;

public class ServerFacade {
    private final String baseUrl;

    public ServerFacade(int port){
        this.baseUrl = "http://localhost:" + port;
    }

    public AuthData register(RegisterRequest request) throws Exception{
        return null;
    }

    public <T> T makeRequest(String method, String path, Object request, Class<T> responseClass)throws Exception{
        try{
            URL url = (new URI(baseUrl + path)).toURL();
            HttpURLConnection http = (HttpURLConnection) url.openConnection();
            http.setRequestMethod(method);
            http.setDoOutput(true);
            writeRequestBody(request, http);
            http.connect();
            return readBody(http, responseClass);
        }catch(Exception ex){
            throw new Exception("request failed");
        }
    }

    private static void writeRequestBody(Object request, HttpURLConnection http) throws IOException {
        if (request != null) {
            http.addRequestProperty("Content-Type", "application/json");
            String requestData = new Gson().toJson(request);
            try (OutputStream requestBody = http.getOutputStream()) {
                requestBody.write(requestData.getBytes());
            }
        }
    }

    private static <T> T readBody(HttpURLConnection http, Class<T> responseClass) throws IOException {
        T response = null;
        if (http.getContentLength() < 0) {
            try (InputStream respBody = http.getInputStream()) {
                InputStreamReader reader = new InputStreamReader(respBody);
                if (responseClass != null) {
                    response = new Gson().fromJson(reader, responseClass);
                }
            }
        }
        return response;
    }

}

