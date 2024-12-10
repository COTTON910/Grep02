import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import org.json.JSONArray;
import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

public class CookGUI extends JFrame {
    private IngredientDatabase ingredientDatabase;
    private List<Menu> menus;
    private DefaultTableModel tableModel;

    public CookGUI(IngredientDatabase ingredientDatabase, List<Menu> menus) {
        this.ingredientDatabase = ingredientDatabase;
        this.menus = menus;

        setTitle("料理管理システム");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // メインパネル
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout());

        // ボタンパネル
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new GridLayout(1, 4));

        JButton addIngredientButton = new JButton("材料を追加");
        JButton delButton = new JButton("材料の削除");
        JButton generateMenuButton = new JButton("メニュー生成");
        JButton searchMenuButton = new JButton("最適メニュー探索");
        JButton exitButton = new JButton("終了");

        buttonPanel.add(addIngredientButton);
        buttonPanel.add(delButton);
        buttonPanel.add(generateMenuButton);
        buttonPanel.add(searchMenuButton);
        buttonPanel.add(exitButton);

        // テーブルの作成
        String[] columnNames = {"名前", "賞味期限", "残量(g)", "タンパク質(g)", "脂質(g)", "炭水化物(g)"};
        tableModel = new DefaultTableModel(columnNames, 0);
        JTable ingredientTable = new JTable(tableModel);

        // テーブルのデータ更新
        updateIngredientTable(tableModel);

        JScrollPane scrollPane = new JScrollPane(ingredientTable);
        mainPanel.add(new JLabel("材料データベース"), BorderLayout.NORTH);
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        // ボタンのアクションリスナー
        addIngredientButton.addActionListener(e -> addIngredientDialog(tableModel));
        delButton.addActionListener(e -> delIngredientDialog(ingredientTable));
        generateMenuButton.addActionListener(e -> generateMenuFromIngredients());
        searchMenuButton.addActionListener(e -> searchOptimalMenu());
        exitButton.addActionListener(e -> System.exit(0));

        add(mainPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    // 材料を追加するダイアログ
    private void addIngredientDialog(DefaultTableModel tableModel) {
        JPanel inputPanel = new JPanel();
        inputPanel.setLayout(new GridLayout(6, 2));

        JTextField nameField = new JTextField();
        JTextField expiryDateField = new JTextField();
        JTextField quantityField = new JTextField();
        JTextField proteinField = new JTextField();
        JTextField lipidsField = new JTextField();
        JTextField carbohydratesField = new JTextField();

        inputPanel.add(new JLabel("名前:"));
        inputPanel.add(nameField);
        inputPanel.add(new JLabel("賞味期限 (YYYY/MM/DD):"));
        inputPanel.add(expiryDateField);
        inputPanel.add(new JLabel("量 (g):"));
        inputPanel.add(quantityField);
        inputPanel.add(new JLabel("タンパク質 (g):"));
        inputPanel.add(proteinField);
        inputPanel.add(new JLabel("脂質 (g):"));
        inputPanel.add(lipidsField);
        inputPanel.add(new JLabel("炭水化物 (g):"));
        inputPanel.add(carbohydratesField);

        int result = JOptionPane.showConfirmDialog(this, inputPanel, "材料を追加", JOptionPane.OK_CANCEL_OPTION);
        if (result == JOptionPane.OK_OPTION) {
            try {
                String name = nameField.getText();
                String expiryDate = expiryDateField.getText();
                double quantity = parseDoubleWithValidation(quantityField.getText(), "量");
                double protein = parseDoubleWithValidation(proteinField.getText(), "タンパク質");
                double lipids = parseDoubleWithValidation(lipidsField.getText(), "脂質");
                double carbohydrates = parseDoubleWithValidation(carbohydratesField.getText(), "炭水化物");

                Ingredient ingredient = new Ingredient(name, expiryDate, quantity, protein, lipids, carbohydrates);
                ingredientDatabase.addIngredient(ingredient);

                updateIngredientTable(tableModel);
                JOptionPane.showMessageDialog(this, "材料を追加しました: " + ingredient);
            } catch (IllegalArgumentException ex) {
                JOptionPane.showMessageDialog(this, "入力エラー: " + ex.getMessage(), "エラー", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    // Gemini APIを使ってメニューを生成
    private void generateMenuFromIngredients() {
        try {
            List<String> ingredientNames = ingredientDatabase.getIngredients().stream()
                    .map(Ingredient::getName)
                    .toList();

            JSONArray recipesResponse = CookUI.getRecipesFromIngredients(ingredientNames);
            menus = CookUI.parseRecipesFromResponse(recipesResponse);

            JOptionPane.showMessageDialog(this, "メニューが生成されました。数: " + menus.size());
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "メニュー生成中にエラーが発生しました: " + e.getMessage(), "エラー", JOptionPane.ERROR_MESSAGE);
        }
    }

    // 最適なメニューを探索
    private void searchOptimalMenu() {
        if (menus.isEmpty()) {
            JOptionPane.showMessageDialog(this, "メニューが生成されていません。先にメニュー生成を行ってください。", "エラー", JOptionPane.ERROR_MESSAGE);
            return;
        }

        JPanel inputPanel = new JPanel();
        inputPanel.setLayout(new GridLayout(3, 2));

        JTextField proteinField = new JTextField();
        JTextField lipidsField = new JTextField();
        JTextField carbohydratesField = new JTextField();

        inputPanel.add(new JLabel("タンパク質 (g):"));
        inputPanel.add(proteinField);
        inputPanel.add(new JLabel("脂質 (g):"));
        inputPanel.add(lipidsField);
        inputPanel.add(new JLabel("炭水化物 (g):"));
        inputPanel.add(carbohydratesField);

        int result = JOptionPane.showConfirmDialog(this, inputPanel, "栄養目標を設定", JOptionPane.OK_CANCEL_OPTION);
        if (result == JOptionPane.OK_OPTION) {
            try {
                double protein = parseDoubleWithValidation(proteinField.getText(), "タンパク質");
                double lipids = parseDoubleWithValidation(lipidsField.getText(), "脂質");
                double carbohydrates = parseDoubleWithValidation(carbohydratesField.getText(), "炭水化物");

                MenuMatcher menuMatcher = new MenuMatcher(ingredientDatabase, protein, lipids, carbohydrates);
                Menu optimalMenu = menuMatcher.findOptimalMenuBFS(menus);

                if (optimalMenu != null) {
                	updateIngredientTable(tableModel);
                    JOptionPane.showMessageDialog(this, "最適なメニュー: " + optimalMenu.getName()
                            + "\n" + optimalMenu.getRequiredIngredients().entrySet().stream()
                            .map(entry -> "- " + entry.getKey() + ": " + entry.getValue() + "g")
                            .collect(Collectors.joining("\n")));
                } else {
                    JOptionPane.showMessageDialog(this, "最適なメニューは見つかりませんでした。");
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "入力エラー: " + ex.getMessage(), "エラー", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void updateIngredientTable(DefaultTableModel tableModel) {
        tableModel.setRowCount(0);
        for (Ingredient ingredient : ingredientDatabase.getIngredients()) {
            tableModel.addRow(new Object[]{
                    ingredient.getName(),
                    ingredient.getExpiryDate(),
                    ingredient.getQuantity(),
                    ingredient.getProtein(),
                    ingredient.getLipids(),
                    ingredient.getCarbohydrates()
            });
        }
    }

    private void delIngredientDialog(JTable ingredientTable) {
        int row = ingredientTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "削除する行を選択してください", "エラー", JOptionPane.ERROR_MESSAGE);
            return;
        }
        tableModel.removeRow(row);
        ingredientDatabase.getIngredients().remove(row);
    }

    private double parseDoubleWithValidation(String value, String fieldName) {
        try {
            double result = Double.parseDouble(value);
            if (result < 0) {
                throw new IllegalArgumentException(fieldName + "は0以上の数値を入力してください。");
            }
            return result;
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException(fieldName + "は数値で入力してください。");
        }
    }

    public static void main(String[] args) {
        IngredientDatabase ingredientDatabase = new IngredientDatabase(new ArrayList<>());
        List<Ingredient> ingredients = List.of(
                new Ingredient("Tomato", "2025/01/01", 500, 7.76, 6.11, 0.17),
                new Ingredient("Chicken Breast", "2025/01/01", 300, 21.3, 5.9, 0.1),
                new Ingredient("Rice", "2025/06/01", 1000, 6.1, 0.9, 77.6),
                new Ingredient("Onion", "2025/01/01", 300, 1.1, 0.1, 8.5),
                new Ingredient("Carrot", "2025/01/01", 250, 0.9, 0.2, 9.6),
                new Ingredient("Radish", "2000/01/01", 550, 0.9, 0.2, 9.6));
        ingredients.forEach(ingredientDatabase::addIngredient);

        SwingUtilities.invokeLater(() -> {
            CookGUI cookGUI = new CookGUI(ingredientDatabase, new ArrayList<>());
            cookGUI.setVisible(true);
        });
    }
}
