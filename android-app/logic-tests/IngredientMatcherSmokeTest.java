import za.co.richfield.smartpantry.logic.IngredientMatcher;
import za.co.richfield.smartpantry.model.PantryItem;
import za.co.richfield.smartpantry.model.Recipe;
import za.co.richfield.smartpantry.model.RecipeIngredient;

import java.util.Arrays;
import java.util.Collections;

public final class IngredientMatcherSmokeTest {
    private static int checks;

    public static void main(String[] args) {
        Recipe tomatoPasta = recipe(
                ingredient("tomato", 2, "pcs"),
                ingredient("pasta", 150, "g"),
                ingredient("garlic", 1, "clove"));

        check("missing one required ingredient excludes recipe",
                !IngredientMatcher.canMake(tomatoPasta, pantry(
                        item("tomatoes", 2, "pieces"),
                        item("pasta", 150, "g"))));

        check("plural ingredient names match",
                IngredientMatcher.canMake(recipe(ingredient("tomato", 2, "pcs")),
                        pantry(item("tomatoes", 2, "pieces"))));

        check("insufficient quantity excludes recipe",
                !IngredientMatcher.canMake(recipe(ingredient("pasta", 150, "g")),
                        pantry(item("pasta", 149, "g"))));

        check("kilograms convert to grams",
                IngredientMatcher.canMake(recipe(ingredient("flour", 0.5, "kg")),
                        pantry(item("flour", 500, "g"))));

        check("duplicate pantry quantities combine",
                IngredientMatcher.canMake(recipe(ingredient("rice", 0.5, "kg")),
                        pantry(item("rice", 200, "g"), item("rice", 300, "g"))));

        check("litres convert to millilitres",
                IngredientMatcher.canMake(recipe(ingredient("milk", 0.25, "l")),
                        pantry(item("milk", 250, "ml"))));

        check("incompatible weight and volume units do not match",
                !IngredientMatcher.canMake(recipe(ingredient("oil", 100, "g")),
                        pantry(item("oil", 100, "ml"))));

        check("one piece and one item are equivalent count units",
                IngredientMatcher.canMake(recipe(ingredient("egg", 1, "pc")),
                        pantry(item("eggs", 1, "item"))));

        check("empty pantry never suggests a recipe",
                !IngredientMatcher.canMake(recipe(ingredient("egg", 1, "pc")),
                        Collections.emptyList()));

        check("empty recipe is not suggested",
                !IngredientMatcher.canMake(recipe(), pantry(item("egg", 3, "pcs"))));

        System.out.println("Passed " + checks + " strict-matching checks.");
    }

    private static void check(String name, boolean passed) {
        if (!passed) throw new AssertionError("FAILED: " + name);
        checks++;
        System.out.println("PASS: " + name);
    }

    private static Recipe recipe(RecipeIngredient... ingredients) {
        return new Recipe(1, "Test recipe", "Test steps", Arrays.asList(ingredients));
    }

    private static RecipeIngredient ingredient(String name, double quantity, String unit) {
        return new RecipeIngredient(name, quantity, unit);
    }

    private static PantryItem item(String name, double quantity, String unit) {
        return new PantryItem(1, name, quantity, unit, "");
    }

    private static java.util.List<PantryItem> pantry(PantryItem... items) {
        return Arrays.asList(items);
    }
}
