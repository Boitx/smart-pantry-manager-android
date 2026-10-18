package za.co.richfield.smartpantry.logic;

import za.co.richfield.smartpantry.model.PantryItem;
import za.co.richfield.smartpantry.model.Recipe;
import za.co.richfield.smartpantry.model.RecipeIngredient;

import java.text.Normalizer;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Strictly checks every required ingredient and quantity. Compatible units are
 * converted to a shared base unit; incompatible or missing units never count.
 */
public final class IngredientMatcher {
    private IngredientMatcher() {}

    public static boolean canMake(Recipe recipe, List<PantryItem> pantry) {
        if (recipe == null || pantry == null) return false;
        if (recipe.getIngredients().isEmpty()) return false;

        Map<String, Amount> requiredByIngredient = new HashMap<>();
        for (RecipeIngredient ingredient : recipe.getIngredients()) {
            String name = normalizeName(ingredient.getName());
            if (name.isEmpty() || ingredient.getQuantity() <= 0) return false;

            Amount amount = toAmount(ingredient.getQuantity(), ingredient.getUnit());
            String key = name + "|" + amount.dimension;
            Amount previous = requiredByIngredient.get(key);
            requiredByIngredient.put(key, new Amount(amount.dimension,
                    amount.baseQuantity + (previous == null ? 0 : previous.baseQuantity)));
        }

        for (Map.Entry<String, Amount> requirement : requiredByIngredient.entrySet()) {
            int separator = requirement.getKey().lastIndexOf('|');
            String requiredName = requirement.getKey().substring(0, separator);
            Amount needed = requirement.getValue();
            double available = 0;

            for (PantryItem item : pantry) {
                if (!requiredName.equals(normalizeName(item.getName()))) continue;
                Amount stocked = toAmount(item.getQuantity(), item.getUnit());
                if (needed.dimension.equals(stocked.dimension)) {
                    available += stocked.baseQuantity;
                }
            }
            if (available + 1e-9 < needed.baseQuantity) return false;
        }
        return true;
    }

    public static String normalizeName(String input) {
        if (input == null) return "";
        String value = Normalizer.normalize(input, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9 ]", " ")
                .trim()
                .replaceAll("\\s+", " ");

        if (value.equals("scallion") || value.equals("scallions")
                || value.equals("green onion") || value.equals("green onions")) {
            return "spring onion";
        }
        if (value.equals("garbanzo beans") || value.equals("garbanzo bean")) {
            return "chickpea";
        }
        if (value.equals("tomatoes")) return "tomato";
        if (value.equals("potatoes")) return "potato";
        if (value.equals("berries")) return "berry";
        if (value.endsWith("ies") && value.length() > 4) {
            return value.substring(0, value.length() - 3) + "y";
        }
        if (value.endsWith("oes") && value.length() > 4) {
            return value.substring(0, value.length() - 2);
        }
        if (value.endsWith("s") && !value.endsWith("ss") && value.length() > 3) {
            return value.substring(0, value.length() - 1);
        }
        return value;
    }

    private static Amount toAmount(double quantity, String rawUnit) {
        String unit = normalizeUnit(rawUnit);
        switch (unit) {
            case "g": return new Amount("weight", quantity);
            case "kg": return new Amount("weight", quantity * 1000.0);
            case "mg": return new Amount("weight", quantity / 1000.0);
            case "oz": return new Amount("weight", quantity * 28.349523125);
            case "lb": return new Amount("weight", quantity * 453.59237);
            case "ml": return new Amount("volume", quantity);
            case "l": return new Amount("volume", quantity * 1000.0);
            case "tsp": return new Amount("volume", quantity * 4.92892159);
            case "tbsp": return new Amount("volume", quantity * 14.7867648);
            case "cup": return new Amount("volume", quantity * 236.588237);
            case "piece": return new Amount("count", quantity);
            default: return new Amount("custom:" + unit, quantity);
        }
    }

    private static String normalizeUnit(String rawUnit) {
        String unit = rawUnit == null ? "" : rawUnit.trim().toLowerCase(Locale.ROOT);
        unit = unit.replaceAll("\\.", "").replaceAll("\\s+", " ");
        switch (unit) {
            case "gram": case "grams": case "g": return "g";
            case "kilogram": case "kilograms": case "kgs": case "kg": return "kg";
            case "milligram": case "milligrams": case "mgs": case "mg": return "mg";
            case "ounce": case "ounces": case "oz": return "oz";
            case "pound": case "pounds": case "lbs": case "lb": return "lb";
            case "milliliter": case "milliliters": case "millilitre":
            case "millilitres": case "ml": return "ml";
            case "liter": case "liters": case "litre": case "litres": case "l": return "l";
            case "teaspoon": case "teaspoons": case "tsp": return "tsp";
            case "tablespoon": case "tablespoons": case "tbsp": return "tbsp";
            case "cups": case "cup": return "cup";
            case "piece": case "pieces": case "pc": case "pcs":
            case "item": case "items": case "each": return "piece";
            default:
                if (unit.endsWith("s") && !unit.endsWith("ss") && unit.length() > 3) {
                    return unit.substring(0, unit.length() - 1);
                }
                return unit;
        }
    }

    private static final class Amount {
        private final String dimension;
        private final double baseQuantity;

        private Amount(String dimension, double baseQuantity) {
            this.dimension = dimension;
            this.baseQuantity = baseQuantity;
        }
    }
}
