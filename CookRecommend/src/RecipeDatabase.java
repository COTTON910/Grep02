
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RecipeDatabase {

    // Database2: レシピ名(String)、材料と量(List<Map<String, Double>>)、調理時間(int)のリスト
    private static List<Map<String, Object>> Database2 = new ArrayList<>();

    /**
     * JSONデータからDatabase2を構築するメソッド
     * @param jsonData JSONデータ
     */
    public static void createDatabase2(String jsonData) {
        // JSONデータを解析
        JSONArray recipes = new JSONArray(jsonData);

        // 各レシピを処理
        for (int i = 0; i < recipes.length(); i++) {
            JSONObject recipe = recipes.getJSONObject(i);

            // レシピ情報を抽出
            String menu = recipe.getString("menu");
            int timeSeconds = recipe.getInt("time_seconds");

            // 材料とその量を抽出
            List<Map<String, Double>> ingredientsList = new ArrayList<>();
            JSONArray ingredients = recipe.getJSONArray("ingredients");
            for (int j = 0; j < ingredients.length(); j++) {
                JSONObject ingredient = ingredients.getJSONObject(j);
                Map<String, Double> ingredientMap = new HashMap<>();
                ingredientMap.put(ingredient.getString("name"), ingredient.getDouble("amount"));
                ingredientsList.add(ingredientMap);
            }

            // レシピ情報をマップに格納
            Map<String, Object> recipeMap = new HashMap<>();
            recipeMap.put("menu", menu);
            recipeMap.put("time_seconds", timeSeconds);
            recipeMap.put("ingredients", ingredientsList);

            // Database2に追加
            Database2.add(recipeMap);
        }
    }

    /**
     * Database2を表示するメソッド
     */
    public static void displayDatabase2() {
        System.out.println("Database2:");
        for (Map<String, Object> recipe : Database2) {
            System.out.println("Menu: " + recipe.get("menu"));
            System.out.println("Time (seconds): " + recipe.get("time_seconds"));

            // 材料リストを表示
            System.out.println("Ingredients:");
            List<Map<String, Double>> ingredients = (List<Map<String, Double>>) recipe.get("ingredients");
            for (Map<String, Double> ingredient : ingredients) {
                for (Map.Entry<String, Double> entry : ingredient.entrySet()) {
                    System.out.println(" - " + entry.getKey() + ": " + entry.getValue() + "g");
                }
            }
            System.out.println();
        }
    }

    /**
     * デバッグ用のメインメソッド
     */
    public static void main(String[] args) {
        // JSONデータのモック
        String jsonData = """
        [
          {
            "menu": "Caprese Salad",
            "time_seconds": 600,
            "ingredients": [
              {"name": "tomato", "amount": 200.0},
              {"name": "cheese", "amount": 100.0},
              {"name": "basil", "amount": 50.0},
              {"name": "olive oil", "amount": 10.0}
            ]
          }
        ]
        """;

        // Database2を作成
        createDatabase2(jsonData);

        // Database2を表示
        displayDatabase2();
    }
}