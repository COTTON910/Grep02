import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.KeyFactory;
import java.security.interfaces.RSAPrivateKey;
import java.sql.Date;
import java.util.*;

import org.json.JSONArray;
import org.json.JSONObject;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
public class CookUI {
	
	IngredientDatabase ingredientDatabase;
	MenuMatcher menumatcher;
 	RecipeDatabase racipedatabase;
	List<Menu> menus;//Recipedatabaseの代用。メニューのリストを参照。
	
    public static void main(String[] args) throws Exception {

        // 現在の日付を設定
        String todayDate = "2024/11/25"; // yyyy/MM/dd 形式

        // 食材リストを作成
        List<Ingredient> ingredientList = new ArrayList<>();
        ingredientList.add(new Ingredient("Tomato", "2024/12/15", 150, 1.4, 0.3, 6.0));
        ingredientList.add(new Ingredient("Salt", "2024/12/15", 150, 1.4, 0.3, 6.0));
        ingredientList.add(new Ingredient("Soy Sauce", "2024/12/15", 150, 1.4, 0.3, 6.0));
        ingredientList.add(new Ingredient("salt", "2024/12/15", 150, 1.4, 0.3, 6.0));
        ingredientList.add(new Ingredient("Chicken Breast", "2024/12/30", 150, 31.0, 3.6, 0.0));
        ingredientList.add(new Ingredient("Rice", "2025/06/01", 500, 7.1, 0.6, 77.0));
        ingredientList.add(new Ingredient("Onion", "2025/01/01", 150, 1.1, 0.1, 9.3));

        // 食材データベースを作成
        IngredientDatabase ingredientDatabase = new IngredientDatabase(ingredientList);
//        ingredientDatabase.printDatabase();
        
        // メニューリストを作成
        List<Menu> menus = new ArrayList<>();
        /*
        Map<String, Double> menu1Ingredients = new HashMap<>();
        menu1Ingredients.put("Tomato", 100.0);
        menu1Ingredients.put("Onion", 100.0);
        menu1Ingredients.put("Rice", 100.0);

        Map<String, Double> menu2Ingredients = new HashMap<>();
        menu2Ingredients.put("Chicken Breast", 300.0);
        menu2Ingredients.put("Rice", 100.0);
        menu2Ingredients.put("Onion", 200.0);

        
        menus.add(new Menu("Tomato Rice", menu1Ingredients));
        menus.add(new Menu("Chicken Rice", menu2Ingredients));
        */
        
        //UIの起動
        CookUI cookUI = new CookUI();
        cookUI.setIngredientDatabase(ingredientDatabase);
        cookUI.setMenus(menus);
        cookUI.StartMenu();

        // MenuMatcherを使用して最適なメニューを探索
//        MenuMatcher menuMatcher = new MenuMatcher(ingredientDatabase, 70, 65, 300);
//        Menu optimalMenu = menuMatcher.findOptimalMenuBFS(menus);
//        
        // 最後にデータベースを表示(確認用)
        ingredientDatabase.printDatabase();
    }
    
	public CookUI() {
	}
	
	//材料データベースのセッタ
	public void setIngredientDatabase(IngredientDatabase ingredientDatabase) {
		this.ingredientDatabase = ingredientDatabase;
	}
	
	//メニューデータベースのセッタ
	public void setMenus(List<Menu> menus) {
		this.menus = menus;
	}
  	
  	//StartMenuの表示
  	public void StartMenu() throws Exception {
  		boolean endflag = false;
  		while(endflag == false) {
  			System.out.println("*****StartMenu:*****");
  			displayIngredient();
  			System.out.println("---操作一覧---");
  			System.out.println("1.材料を追加する");			
  			System.out.println("2.最適なメニューを探索する");			
  			System.out.println("3.プログラムの終了");
  			System.out.println("-------------");
  			System.out.println("実行したい操作の番号を入力してください。");
  			Scanner scan = new Scanner(System.in);
  			int x = scan.nextInt();
  			switch(x) {
  			case 1: inputIngreadient(); break;
  			case 2: {
  						setMenus(getRecipesFromDatabase(ingredientDatabase));
  						searchOptimalMenu(); break;
  					}
  					
  			case 3: endflag = true; System.out.println("プログラムを終了します。"); break;
  			}
  		}
  	}
  	
  	public static List<Menu> parseRecipesFromResponse(JSONArray recipesResponse) throws Exception {
  	    // レスポンスから "content.parts[0].text" を取得
  	    String jsonText = recipesResponse.getJSONObject(0)
  	                                     .getJSONObject("content")
  	                                     .getJSONArray("parts")
  	                                     .getJSONObject(0)
  	                                     .getString("text");

  	    // JSONコードブロック (```json ... ```) を取り除く
  	    String jsonCleaned = jsonText.replaceAll("```json", "").replaceAll("```", "").trim();

  	    // JSON文字列をパースしてJSONArrayに変換
  	    JSONArray recipes = new JSONArray(jsonCleaned);

  	    // Menuオブジェクトのリストを作成
  	    List<Menu> menuList = new ArrayList<>();
  	    for (int i = 0; i < recipes.length(); i++) {
  	        JSONObject recipe = recipes.getJSONObject(i);
  	        String menuName = recipe.getString("menu");

  	        Map<String, Double> ingredientsMap = new HashMap<>();
  	        JSONArray ingredientsArray = recipe.getJSONArray("ingredients");
  	        for (int j = 0; j < ingredientsArray.length(); j++) {
  	            JSONObject ingredient = ingredientsArray.getJSONObject(j);
  	            ingredientsMap.put(ingredient.getString("name"), ingredient.getDouble("amount"));
  	        }

  	        menuList.add(new Menu(menuName, ingredientsMap));
  	    }
  	    return menuList;
  	}
  	public static List<Menu> getRecipesFromDatabase(IngredientDatabase db) throws Exception {
  	    // Database1から材料名を取得
  	    List<String> ingredients = getIngredientNames(db);

  	    // Gemini APIを使ってレシピを生成
  	    JSONArray recipesResponse = getRecipesFromIngredients(ingredients);
  	    System.out.println("API Response: " + recipesResponse.toString(2)); // デバッグ用

  	    // レスポンスから "content.parts[0].text" を取得し、JSONとしてパース
  	    String jsonText = recipesResponse.getJSONObject(0)
  	                                     .getJSONObject("content")
  	                                     .getJSONArray("parts")
  	                                     .getJSONObject(0)
  	                                     .getString("text");

  	    // JSONコードブロック (```json ... ```) を取り除く
  	    String jsonCleaned = jsonText.replaceAll("```json", "").replaceAll("```", "").trim();

  	    // JSON文字列をパースしてJSONArrayに変換
  	    JSONArray recipes = new JSONArray(jsonCleaned);

  	    // Menuオブジェクトのリストを作成
  	    List<Menu> menuList = new ArrayList<>();
  	    for (int i = 0; i < recipes.length(); i++) {
  	        JSONObject recipe = recipes.getJSONObject(i);
  	        String menuName = recipe.getString("menu");

  	        // 材料をMapに変換
  	        Map<String, Double> ingredientsMap = new HashMap<>();
  	        JSONArray ingredientsArray = recipe.getJSONArray("ingredients");
  	        for (int j = 0; j < ingredientsArray.length(); j++) {
  	            JSONObject ingredient = ingredientsArray.getJSONObject(j);
  	            ingredientsMap.put(ingredient.getString("name"), ingredient.getDouble("amount"));
  	        }

  	        // Menuオブジェクトを生成しリストに追加
  	        menuList.add(new Menu(menuName, ingredientsMap));
  	    }

  	    return menuList;
  	}

  	/**
  	 * Database1から材料名を取得するメソッド
  	 * @return 材料名のリスト
  	 */
  	public static List<String> getIngredientNames(IngredientDatabase db) {
  	    List<String> names = new ArrayList<>();
  	    for (Ingredient ingredient : db.getIngredients()) {
  	        names.add(ingredient.getName());
  	    }
  	    return names;
  	}

  	/**
  	 * 与えられた材料リストを元にGemini APIでレシピを生成するメソッド
  	 * @param ingredients 材料名のリスト
  	 * @return レシピ情報のJSONArray
  	 * @throws Exception APIの呼び出しに失敗した場合
  	 */
  	public static JSONArray getRecipesFromIngredients(List<String> ingredients) throws Exception {
  	    // プロンプトを生成
  	    StringBuilder prompt = new StringBuilder("Generate recipes using the following ingredients: ");
  	    for (int i = 0; i < ingredients.size(); i++) {
  	        prompt.append(ingredients.get(i));
  	        if (i < ingredients.size() - 1) prompt.append(", ");
  	    }
  	    prompt.append(". Provide recipes in JSON format with the following fields:\n")
  	          .append("- \"menu\": Recipe name (string)\n")
  	          .append("- \"ingredients\": List of ingredients with \"name\" (string) and \"amount\" (grams as float)\n");
  	    String promptText = prompt.toString();
  	    
  	    String API_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent";
  	    String accessToken = getAccessToken("YOUR_JSON_KEY");

  	    URL url = new URL(API_URL);
  	    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
  	    connection.setRequestMethod("POST");
  	    connection.setRequestProperty("Authorization", "Bearer " + accessToken);
  	    connection.setRequestProperty("Content-Type", "application/json");
  	    connection.setDoOutput(true);

  	    // リクエストボディを構築
  	    JSONObject requestBody = new JSONObject();
  	    requestBody.put("model", "generateContent"); // モデルIDを指定
  	    JSONArray contents = new JSONArray();
  	    JSONObject contentObject = new JSONObject();
	  	JSONArray parts = new JSONArray();
	    JSONObject part = new JSONObject();
	    part.put("text", promptText); // プロンプトのテキストを parts[] に追加
	    parts.put(part);
	    contentObject.put("parts", parts);
	    contents.put(contentObject);
	    
  	    requestBody.put("contents", contents);

  	    try (OutputStream os = connection.getOutputStream()) {
  	        byte[] input = requestBody.toString().getBytes("utf-8");
  	        os.write(input, 0, input.length);
  	    }

  	    int responseCode = connection.getResponseCode();
  	    if (responseCode != 200) {
  	        throw new RuntimeException("HTTPエラー: " + responseCode + ", 詳細: " + getErrorResponse(connection));
  	    }

  	    String response = readResponse(connection);
  	    JSONObject jsonResponse = new JSONObject(response);
  	    return jsonResponse.getJSONArray("candidates");
  	}

  	/**
  	 * エラーレスポンスを取得するメソッド
  	 * @param connection HTTPコネクション
  	 * @return エラーメッセージ
  	 * @throws IOException
  	 */
  	private static String getErrorResponse(HttpURLConnection connection) throws IOException {
  	    try (BufferedReader errorReader = new BufferedReader(new InputStreamReader(connection.getErrorStream(), "utf-8"))) {
  	        StringBuilder errorResponse = new StringBuilder();
  	        String line;
  	        while ((line = errorReader.readLine()) != null) {
  	            errorResponse.append(line.trim());
  	        }
  	        return errorResponse.toString();
  	    }
  	}

  	/**
  	 * レスポンスを取得するメソッド
  	 * @param connection HTTPコネクション
  	 * @return レスポンス文字列
  	 * @throws IOException
  	 */
  	private static String readResponse(HttpURLConnection connection) throws IOException {
  	    try (BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream(), "utf-8"))) {
  	        StringBuilder response = new StringBuilder();
  	        String line;
  	        while ((line = br.readLine()) != null) {
  	            response.append(line.trim());
  	        }
  	        return response.toString();
  	    }
  	}



	//現存する素材を全て表示
  	public void displayIngredient() {
  		ingredientDatabase.printDatabase();
  	}
  	
  	//最適なメニューを探索する
  	public void searchOptimalMenu() {
  		System.out.println("目標とする栄養素を入力してください");
  		Scanner scan = new Scanner(System.in);
  		System.out.println("タンパク質の量(g):");
  		double protein = scan.nextDouble();      	// タンパク質の量
  		System.out.println("脂質の量(g):");
  		double lipids = scan.nextDouble();		 	// 脂質の量
  		System.out.println("炭水化物の量(g):");
  	    double carbohydrates= scan.nextDouble();	// 炭水化物の量
  	    System.out.println("探索を開始します。");
  		this.menumatcher = new MenuMatcher(ingredientDatabase, protein, lipids, carbohydrates);
  		menumatcher.findOptimalMenuBFS(menus);
  	}
  	
  	//素材の内容を入力
  	public void inputIngreadient() {
  		Scanner scan = new Scanner(System.in);
  		System.out.println("材料の情報を入力してください");
  		System.out.println("名前(xxxx):");
  		String name = scan.next();
  		System.out.println("賞味期限(YYYY/MM/DD):");
  		String expiryDate = scan.next();   	// 賞味期限
  		System.out.println("食材の量(g):");
  		double quantity = scan.nextDouble();     	// 食材の量
  		System.out.println("タンパク質の量(g):");
  		double protein = scan.nextDouble();      	// タンパク質の量
  		System.out.println("脂質の量(g):");
  		double lipids = scan.nextDouble();		 	// 脂質の量
  		System.out.println("炭水化物の量(g):");
  	    double carbohydrates= scan.nextDouble();	// 炭水化物の量
  	    var ingredient = new Ingredient(name, expiryDate, quantity, protein, lipids, carbohydrates);
  	    System.out.println(ingredient + "を材料リストに追加しました");
  		ingredientDatabase.addIngredient(ingredient);
  	}
  	
  	
    /**
     * アクセストークンを取得するメソッド
     * @param serviceAccountJsonPath サービスアカウントJSONファイルのパス
     * @return アクセストークン
     * @throws Exception API呼び出しに失敗した場合
     */
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
  	        BufferedReader errorReader = new BufferedReader(new InputStreamReader(connection.getErrorStream(), "utf-8"));
  	        StringBuilder errorResponse = new StringBuilder();
  	        String errorLine;
  	        while ((errorLine = errorReader.readLine()) != null) {
  	            errorResponse.append(errorLine.trim());
  	        }
  	        throw new RuntimeException("失敗しました: HTTPエラーコード : " + responseCode + ", エラー詳細: " + errorResponse.toString());
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
  	        Algorithm algorithm = Algorithm.RSA256(null, privateKey);  // 公開鍵はnullに設定

  	        // 現在時刻 (Unixエポック時間)
  	        long now = System.currentTimeMillis();
  	        Date issuedAt = new Date(now);
  	        Date expiresAt = new Date(now + 3600000); // 1時間後に期限を設定

  	        // JWTのペイロード設定
  	        return JWT.create()
  	                .withIssuer(clientEmail)   // 発行者 (サービスアカウントのメールアドレス)
  	                .withSubject(clientEmail)  // サブジェクト (サービスアカウントのメールアドレス)
  	                .withAudience("https://oauth2.googleapis.com/token") // トークンのオーディエンス
  	              .withClaim("scope", "https://www.googleapis.com/auth/generative-language") // スコープを追加
  	                .withIssuedAt(issuedAt)  // 発行日時
  	                .withExpiresAt(expiresAt)  // 期限（1時間後）
  	                .sign(algorithm);  // 署名
  	    } catch (Exception e) {
  	        e.printStackTrace();
  	        return null;
  	    }
  	}
    
    /**
     * 文字列で与えられた秘密鍵をRSAPrivateKeyに変換する関数
     * @param privateKeyStr 秘密鍵の文字列
     * @return RSAPrivateKey
     */
    private static RSAPrivateKey loadPrivateKey(String privateKeyStr) throws Exception {
        String privateKeyPEM = privateKeyStr.replace("-----BEGIN PRIVATE KEY-----", "")
                                             .replace("-----END PRIVATE KEY-----", "")
                                             .replaceAll("\\s", "");

        byte[] encoded = java.util.Base64.getDecoder().decode(privateKeyPEM);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        return (RSAPrivateKey) keyFactory.generatePrivate(new java.security.spec.PKCS8EncodedKeySpec(encoded));
    }
  	
}
