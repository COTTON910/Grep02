import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.KeyFactory;
import java.security.interfaces.RSAPrivateKey;
import java.sql.Date;

import org.json.JSONArray;
import org.json.JSONObject;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;

public class GoogleLanguageApiExample {

    private static final String API_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent";

    public static void main(String[] args) throws Exception {
        // プロンプトの内容を構築
        String promptText = "Generate a recipe using the following ingredients: tomatoes, onions, chicken.";
        

        // リクエストボディを構築
        JSONObject requestBody = new JSONObject();
        requestBody.put("model", "generateContent"); // モデルIDを指定

        // contents 配列に Content オブジェクトを含める
        JSONArray contents = new JSONArray();
        JSONObject contentObject = new JSONObject();

        // parts[] を含む Content
        JSONArray parts = new JSONArray();
        JSONObject part = new JSONObject();
        part.put("text", promptText); // プロンプトのテキストを parts[] に追加
        parts.put(part);
        contentObject.put("parts", parts);
        contents.put(contentObject);

        requestBody.put("contents", contents);

        // APIリクエストを送信
        String response = sendPostRequest(API_URL, requestBody.toString());
        System.out.println("Response: " + response);
    }

    public static String getAccessToken(String serviceAccountJsonPath) throws Exception {
        String jsonContent = new String(java.nio.file.Files.readAllBytes(java.nio.file.Paths.get(serviceAccountJsonPath)));

        JSONObject credentialsJson = new JSONObject(jsonContent);
        String clientEmail = credentialsJson.getString("client_email");
        String privateKey = credentialsJson.getString("private_key");

        String assertion = createJwt(clientEmail, privateKey);
        System.out.println("Generated JWT: " + assertion); // デバッグ用

        String tokenUrl = "https://oauth2.googleapis.com/token";
        String data = "grant_type=urn%3Aietf%3Aparams%3Aoauth%3Agrant-type%3Ajwt-bearer&assertion=" + assertion;

        URL url = new URL(tokenUrl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setDoOutput(true);
        connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

        try (OutputStream os = connection.getOutputStream()) {
            os.write(data.getBytes("utf-8"));
        }

        int responseCode = connection.getResponseCode();
        if (responseCode != 200) {
            try (BufferedReader errorReader = new BufferedReader(new InputStreamReader(connection.getErrorStream(), "utf-8"))) {
                StringBuilder errorResponse = new StringBuilder();
                String errorLine;
                while ((errorLine = errorReader.readLine()) != null) {
                    errorResponse.append(errorLine.trim());
                }
                throw new RuntimeException("失敗しました: HTTPエラーコード : " + responseCode + ", エラー詳細: " + errorResponse.toString());
            }
        }

        StringBuilder response = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream(), "utf-8"))) {
            String responseLine;
            while ((responseLine = br.readLine()) != null) {
                response.append(responseLine.trim());
            }
        }

        JSONObject jsonResponse = new JSONObject(response.toString());
        return jsonResponse.getString("access_token");
    }

    private static String createJwt(String clientEmail, String privateKeyStr) {
        try {
            // 秘密鍵の文字列をRSAPrivateKeyに変換
            RSAPrivateKey privateKey = loadPrivateKey(privateKeyStr);

            // ヘッダーの設定 (JWTのアルゴリズムなど)
            Algorithm algorithm = Algorithm.RSA256(null, privateKey); // 公開鍵はnullに設定

            // 現在時刻 (Unixエポック時間)
            long now = System.currentTimeMillis();
            Date issuedAt = new Date(now);
            Date expiresAt = new Date(now + 3600000); // 1時間後に期限を設定

            // JWTのペイロード設定
            return JWT.create()
                    .withIssuer(clientEmail) // 発行者 (サービスアカウントのメールアドレス)
                    .withSubject(clientEmail) // サブジェクト (サービスアカウントのメールアドレス)
                    .withAudience("https://oauth2.googleapis.com/token") // トークンのオーディエンス
                    .withClaim("scope", "https://www.googleapis.com/auth/generative-language") // スコープを追加
                    .withIssuedAt(issuedAt) // 発行日時
                    .withExpiresAt(expiresAt) // 期限（1時間後）
                    .sign(algorithm); // 署名
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private static RSAPrivateKey loadPrivateKey(String privateKeyStr) throws Exception {
        String privateKeyPEM = privateKeyStr.replace("-----BEGIN PRIVATE KEY-----", "")
                .replace("-----END PRIVATE KEY-----", "")
                .replaceAll("\\s", "");

        byte[] encoded = java.util.Base64.getDecoder().decode(privateKeyPEM);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        return (RSAPrivateKey) keyFactory.generatePrivate(new java.security.spec.PKCS8EncodedKeySpec(encoded));
    }

    private static String sendPostRequest(String apiUrl, String requestBody) throws Exception {
        URL url = new URL(apiUrl);
        String ACCESS_TOKEN = getAccessToken("YOUR_JSON_KEY");
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Authorization", "Bearer " + ACCESS_TOKEN);
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setDoOutput(true);

        // リクエストボディを送信
        try (OutputStream os = connection.getOutputStream()) {
            byte[] input = requestBody.getBytes("utf-8");
            os.write(input, 0, input.length);
        }

        // レスポンスを取得
        int responseCode = connection.getResponseCode();
        if (responseCode != 200) {
            try (BufferedReader errorReader = new BufferedReader(new InputStreamReader(connection.getErrorStream(), "utf-8"))) {
                StringBuilder errorResponse = new StringBuilder();
                String errorLine;
                while ((errorLine = errorReader.readLine()) != null) {
                    errorResponse.append(errorLine.trim());
                }
                throw new RuntimeException("HTTPエラー: " + responseCode + ", 詳細: " + errorResponse.toString());
            }
        }

        StringBuilder response = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream(), "utf-8"))) {
            String responseLine;
            while ((responseLine = br.readLine()) != null) {
                response.append(responseLine.trim());
            }
        }
        return response.toString();
    }
}
