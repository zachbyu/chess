package ui;
import com.google.gson.Gson;

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

import handlers.*;

public class ServerFacade {
    private final HttpClient http;
    private final String baseUrl;
    private String authToken;

    public ServerFacade(int port){
        this.baseUrl = "http://localhost:" + port;
        this.http = HttpClient.newHttpClient();
        this.authToken = null;
    }

    public RegisterResult register(RegisterRequest request) throws Exception {
        var path = "/user";
        RegisterResult result = this.makeRequest("POST", path, request, RegisterResult.class);
        authToken = result.authToken();
        return result;
    }

    public LoginResult login(LoginRequest request) throws Exception {
        var path = "/session";
        LoginResult result = this.makeRequest("POST", path, request, LoginResult.class);
        authToken = result.authToken();
        return result;
    }

    public void logout(String authToken) throws Exception{
        String endpoint = baseUrl + "/session";
        HttpRequest request = HttpRequest.newBuilder().uri(new URI(endpoint)).header("authorization", authToken)
                .DELETE()
                .build();

        HttpResponse<String> response = http.send(request, HttpResponse.BodyHandlers.ofString());
        //        errors
        if (response.statusCode() != 200) {
            throw new Exception("Logout failed");
        }
    }

    public CreateGameResult createGame(CreateGameRequest request) throws Exception{
        var path = "/game";
        return this.makeRequest("POST", path, request, CreateGameResult.class);
    }

    public ListGamesResult listGames() throws Exception{
        var path = "/game";
        return this.makeRequest("GET", path, null, ListGamesResult.class);
    }

    public Object joinGame(JoinGameRequest request) throws Exception{
        var path = "/game";
        return this.makeRequest("PUT", path, request, Object.class);
    }

    public void clear() throws Exception{
        var path = "/db";
        this.makeRequest("DELETE", path, null, Object.class);
    }

    private <T> T makeRequest(String method, String path, Object request, Class<T> responseClass) throws Exception {
        try {
            URL url = (new URI(baseUrl + path)).toURL();
            HttpURLConnection http = (HttpURLConnection) url.openConnection();
            http.setRequestMethod(method);
            http.setDoOutput(true);

            if (authToken != null && !authToken.isEmpty()) {
                http.setRequestProperty("authorization", authToken);
            }

            writeBody(request, http);
            http.connect();

            return readBody(http, responseClass);
        } catch (Exception ex) {
            throw new Exception(ex.getMessage());
        }
    }

    private static void writeBody(Object request, HttpURLConnection http) throws IOException {
        if (request != null) {
            http.addRequestProperty("Content-Type", "application/json");
            String reqData = new Gson().toJson(request);
            try (OutputStream reqBody = http.getOutputStream()) {
                reqBody.write(reqData.getBytes());
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

