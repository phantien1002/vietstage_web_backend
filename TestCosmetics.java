
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class TestCosmetics {
    public static void main(String[] args) throws Exception {
        HttpClient client = HttpClient.newHttpClient();
        
        // Need to login first to get a token
        String loginJson = "{\"email\":\"phuclong2710@gmail.com\",\"password\":\"12345678\"}";
        HttpRequest loginReq = HttpRequest.newBuilder()
            .uri(URI.create("http://localhost:9191/api/auth/login"))
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(loginJson))
            .build();
            
        HttpResponse<String> loginRes = client.send(loginReq, HttpResponse.BodyHandlers.ofString());
        System.out.println("Login status: " + loginRes.statusCode());
        
        String token = "";
        try {
            String body = loginRes.body();
            token = body.split("\"token\":\"")[1].split("\"")[0];
        } catch (Exception e) {
            System.out.println("Failed to parse token: " + loginRes.body());
            return;
        }
        
        HttpRequest getReq = HttpRequest.newBuilder()
            .uri(URI.create("http://localhost:9191/api/users/me/cosmetics"))
            .header("Authorization", "Bearer " + token)
            .GET()
            .build();
            
        HttpResponse<String> getRes = client.send(getReq, HttpResponse.BodyHandlers.ofString());
        System.out.println("GET cosmetics status: " + getRes.statusCode());
        System.out.println("Response: " + getRes.body());
    }
}
