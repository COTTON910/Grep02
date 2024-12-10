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
        // �v�����v�g�̓��e���\�z
        String promptText = "Generate a recipe using the following ingredients: tomatoes, onions, chicken.";
        

        // ���N�G�X�g�{�f�B���\�z
        JSONObject requestBody = new JSONObject();
        requestBody.put("model", "generateContent"); // ���f��ID���w��

        // contents �z��� Content �I�u�W�F�N�g���܂߂�
        JSONArray contents = new JSONArray();
        JSONObject contentObject = new JSONObject();

        // parts[] ���܂� Content
        JSONArray parts = new JSONArray();
        JSONObject part = new JSONObject();
        part.put("text", promptText); // �v�����v�g�̃e�L�X�g�� parts[] �ɒǉ�
        parts.put(part);
        contentObject.put("parts", parts);
        contents.put(contentObject);

        requestBody.put("contents", contents);

        // API���N�G�X�g�𑗐M
        String response = sendPostRequest(API_URL, requestBody.toString());
        System.out.println("Response: " + response);
    }

    public static String getAccessToken(String serviceAccountJsonPath) throws Exception {
        String jsonContent = new String(java.nio.file.Files.readAllBytes(java.nio.file.Paths.get(serviceAccountJsonPath)));

        JSONObject credentialsJson = new JSONObject(jsonContent);
        String clientEmail = credentialsJson.getString("client_email");
        String privateKey = credentialsJson.getString("private_key");

        String assertion = createJwt(clientEmail, privateKey);
        System.out.println("Generated JWT: " + assertion); // �f�o�b�O�p

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
                throw new RuntimeException("���s���܂���: HTTP�G���[�R�[�h : " + responseCode + ", �G���[�ڍ�: " + errorResponse.toString());
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
            // �閧���̕������RSAPrivateKey�ɕϊ�
            RSAPrivateKey privateKey = loadPrivateKey(privateKeyStr);

            // �w�b�_�[�̐ݒ� (JWT�̃A���S���Y���Ȃ�)
            Algorithm algorithm = Algorithm.RSA256(null, privateKey); // ���J����null�ɐݒ�

            // ���ݎ��� (Unix�G�|�b�N����)
            long now = System.currentTimeMillis();
            Date issuedAt = new Date(now);
            Date expiresAt = new Date(now + 3600000); // 1���Ԍ�Ɋ�����ݒ�

            // JWT�̃y�C���[�h�ݒ�
            return JWT.create()
                    .withIssuer(clientEmail) // ���s�� (�T�[�r�X�A�J�E���g�̃��[���A�h���X)
                    .withSubject(clientEmail) // �T�u�W�F�N�g (�T�[�r�X�A�J�E���g�̃��[���A�h���X)
                    .withAudience("https://oauth2.googleapis.com/token") // �g�[�N���̃I�[�f�B�G���X
                    .withClaim("scope", "https://www.googleapis.com/auth/generative-language") // �X�R�[�v��ǉ�
                    .withIssuedAt(issuedAt) // ���s����
                    .withExpiresAt(expiresAt) // �����i1���Ԍ�j
                    .sign(algorithm); // ����
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
        String ACCESS_TOKEN = getAccessToken("C:\\Users\\orang\\Downloads\\gen-lang-client-0738237564-5efade0e18c8.json");
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Authorization", "Bearer " + ACCESS_TOKEN);
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setDoOutput(true);

        // ���N�G�X�g�{�f�B�𑗐M
        try (OutputStream os = connection.getOutputStream()) {
            byte[] input = requestBody.getBytes("utf-8");
            os.write(input, 0, input.length);
        }

        // ���X�|���X���擾
        int responseCode = connection.getResponseCode();
        if (responseCode != 200) {
            try (BufferedReader errorReader = new BufferedReader(new InputStreamReader(connection.getErrorStream(), "utf-8"))) {
                StringBuilder errorResponse = new StringBuilder();
                String errorLine;
                while ((errorLine = errorReader.readLine()) != null) {
                    errorResponse.append(errorLine.trim());
                }
                throw new RuntimeException("HTTP�G���[: " + responseCode + ", �ڍ�: " + errorResponse.toString());
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
