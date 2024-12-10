
import java.util.*;

public class Node implements Comparable<Node> {
    private String ingredientName;
    private double quantityNeeded;
    private double cost;
    private Node parent;

    public Node(String ingredientName, double quantityNeeded, double cost, Node parent) {
        this.ingredientName = ingredientName;
        this.quantityNeeded = quantityNeeded;
        this.cost = cost;
        this.parent = parent;
    }

    //ゲッター
    public String getIngredientName() {
        return ingredientName;
    }

    public double getQuantityNeeded() {
        return quantityNeeded;
    }

    public double getCost() {
        return cost;
    }

    public Node getParent() {
        return parent;
    }

    @Override
    //ここで比較をするか否かは要検討必要
    public int compareTo(Node other) {
        return Double.compare(this.cost, other.cost);  // コスト順で優先度キューに追加
    }
}
