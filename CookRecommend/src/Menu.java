
import java.util.Map;
public class Menu {
    private String name;
    private Map<String, Double> requiredIngredients;  // 材料名と必要量のマップ

    public Menu(String name, Map<String, Double> requiredIngredients) {
        this.name = name;
        this.requiredIngredients = requiredIngredients;
    }

    public String getName() {
        return name;
    }

    public Map<String, Double> getRequiredIngredients() {
        return requiredIngredients;
    }

//    // メニューの材料を使用して総コストを計算
//    public double calculateTotalCost(IngredientDatabase ingredientDatabase,String today) {
//        double totalCost = 0.0;
//        for (Map.Entry<String, Double> entry : requiredIngredients.entrySet()) {
//            Ingredient ingredient = ingredientDatabase.findIngredient(entry.getKey());//材料の探索
//            if (ingredient == null || ingredient.getQuantity() < entry.getValue()) {
//                return Double.MAX_VALUE;  // 材料不足の場合は最大コスト
//            }
//            totalCost += ingredient.calculateCost(today) * entry.getValue();  // コストに必要量を掛ける
//        }
//        return totalCost;
//    }
}

