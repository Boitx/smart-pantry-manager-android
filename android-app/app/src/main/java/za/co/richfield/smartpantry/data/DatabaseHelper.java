package za.co.richfield.smartpantry.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import za.co.richfield.smartpantry.model.PantryItem;
import za.co.richfield.smartpantry.model.Recipe;
import za.co.richfield.smartpantry.model.RecipeIngredient;

import java.util.ArrayList;
import java.util.List;

/**
 * Local SQLite storage for pantry items and the preloaded recipe collection.
 * Pantry data remains on-device and survives app restarts.
 */
public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "smart_pantry.db";
    private static final int DATABASE_VERSION = 1;

    private static final String TABLE_PANTRY = "pantry_items";
    private static final String TABLE_RECIPES = "recipes";
    private static final String TABLE_RECIPE_INGREDIENTS = "recipe_ingredients";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onConfigure(SQLiteDatabase db) {
        super.onConfigure(db);
        db.setForeignKeyConstraintsEnabled(true);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLE_PANTRY + " (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "name TEXT NOT NULL, " +
                "quantity REAL NOT NULL CHECK(quantity > 0), " +
                "unit TEXT NOT NULL, " +
                "expiry_date TEXT NOT NULL DEFAULT '')");

        db.execSQL("CREATE TABLE " + TABLE_RECIPES + " (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "name TEXT NOT NULL UNIQUE, " +
                "instructions TEXT NOT NULL)");

        db.execSQL("CREATE TABLE " + TABLE_RECIPE_INGREDIENTS + " (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "recipe_id INTEGER NOT NULL, " +
                "ingredient_name TEXT NOT NULL, " +
                "quantity REAL NOT NULL CHECK(quantity > 0), " +
                "unit TEXT NOT NULL, " +
                "FOREIGN KEY(recipe_id) REFERENCES " + TABLE_RECIPES + "(id) ON DELETE CASCADE, " +
                "UNIQUE(recipe_id, ingredient_name))");

        seedRecipes(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Keep user pantry data intact. Add future schema migrations here.
        if (oldVersion < 2) {
            // No version-2 migration yet.
        }
    }

    public long insertPantryItem(String name, double quantity, String unit, String expiryDate) {
        ContentValues values = pantryValues(name, quantity, unit, expiryDate);
        return getWritableDatabase().insertOrThrow(TABLE_PANTRY, null, values);
    }

    public boolean updatePantryItem(long id, String name, double quantity, String unit, String expiryDate) {
        ContentValues values = pantryValues(name, quantity, unit, expiryDate);
        return getWritableDatabase().update(TABLE_PANTRY, values, "id = ?",
                new String[]{String.valueOf(id)}) == 1;
    }

    public boolean deletePantryItem(long id) {
        return getWritableDatabase().delete(TABLE_PANTRY, "id = ?",
                new String[]{String.valueOf(id)}) == 1;
    }

    public PantryItem getPantryItem(long id) {
        try (Cursor cursor = getReadableDatabase().query(TABLE_PANTRY,
                new String[]{"id", "name", "quantity", "unit", "expiry_date"},
                "id = ?", new String[]{String.valueOf(id)}, null, null, null)) {
            return cursor.moveToFirst() ? pantryItemFrom(cursor) : null;
        }
    }

    public List<PantryItem> getAllPantryItems() {
        List<PantryItem> items = new ArrayList<>();
        try (Cursor cursor = getReadableDatabase().query(TABLE_PANTRY,
                new String[]{"id", "name", "quantity", "unit", "expiry_date"},
                null, null, null, null, "name COLLATE NOCASE ASC")) {
            while (cursor.moveToNext()) items.add(pantryItemFrom(cursor));
        }
        return items;
    }

    public List<Recipe> getAllRecipes() {
        List<Recipe> recipes = new ArrayList<>();
        try (Cursor cursor = getReadableDatabase().query(TABLE_RECIPES,
                new String[]{"id", "name", "instructions"}, null, null, null, null,
                "name COLLATE NOCASE ASC")) {
            while (cursor.moveToNext()) {
                long id = cursor.getLong(0);
                recipes.add(new Recipe(id, cursor.getString(1), cursor.getString(2),
                        getRecipeIngredients(id)));
            }
        }
        return recipes;
    }

    public Recipe getRecipe(long id) {
        try (Cursor cursor = getReadableDatabase().query(TABLE_RECIPES,
                new String[]{"id", "name", "instructions"}, "id = ?",
                new String[]{String.valueOf(id)}, null, null, null)) {
            if (!cursor.moveToFirst()) return null;
            return new Recipe(cursor.getLong(0), cursor.getString(1), cursor.getString(2),
                    getRecipeIngredients(id));
        }
    }

    private List<RecipeIngredient> getRecipeIngredients(long recipeId) {
        List<RecipeIngredient> ingredients = new ArrayList<>();
        try (Cursor cursor = getReadableDatabase().query(TABLE_RECIPE_INGREDIENTS,
                new String[]{"ingredient_name", "quantity", "unit"}, "recipe_id = ?",
                new String[]{String.valueOf(recipeId)}, null, null,
                "ingredient_name COLLATE NOCASE ASC")) {
            while (cursor.moveToNext()) {
                ingredients.add(new RecipeIngredient(
                        cursor.getString(0), cursor.getDouble(1), cursor.getString(2)));
            }
        }
        return ingredients;
    }

    private ContentValues pantryValues(String name, double quantity, String unit, String expiryDate) {
        ContentValues values = new ContentValues();
        values.put("name", name.trim());
        values.put("quantity", quantity);
        values.put("unit", unit.trim());
        values.put("expiry_date", expiryDate == null ? "" : expiryDate.trim());
        return values;
    }

    private PantryItem pantryItemFrom(Cursor cursor) {
        return new PantryItem(cursor.getLong(0), cursor.getString(1), cursor.getDouble(2),
                cursor.getString(3), cursor.getString(4));
    }

    private void seedRecipes(SQLiteDatabase db) {
        db.beginTransaction();
        try {
            seedRecipe(db, "Tomato & Cheese Omelette",
                    "1. Beat the eggs with a pinch of salt.\\n2. Cook the chopped tomato in a lightly oiled pan.\\n3. Pour in the eggs, add cheese, and cook until set.",
                    ingredient("egg", 2, "pcs"), ingredient("tomato", 1, "pc"),
                    ingredient("cheese", 30, "g"), ingredient("salt", 1, "pinch"));
            seedRecipe(db, "Garlic Tomato Pasta",
                    "1. Boil the pasta until tender, then drain.\\n2. Warm olive oil and gently cook the garlic.\\n3. Add chopped tomato, simmer, and toss with the pasta.",
                    ingredient("pasta", 150, "g"), ingredient("tomato", 2, "pcs"),
                    ingredient("garlic", 1, "clove"), ingredient("olive oil", 1, "tbsp"));
            seedRecipe(db, "Banana Oat Pancakes",
                    "1. Mash the banana and mix with the eggs and oats.\\n2. Spoon small rounds into a warm non-stick pan.\\n3. Cook both sides until golden.",
                    ingredient("banana", 1, "pc"), ingredient("egg", 2, "pcs"),
                    ingredient("oats", 60, "g"));
            seedRecipe(db, "Peanut Butter Banana Toast",
                    "1. Toast the bread until crisp.\\n2. Spread with peanut butter.\\n3. Top with sliced banana.",
                    ingredient("bread", 2, "slices"), ingredient("peanut butter", 1, "tbsp"),
                    ingredient("banana", 1, "pc"));
            seedRecipe(db, "Vegetable Fried Rice",
                    "1. Scramble the egg in a hot pan and set aside.\\n2. Cook diced carrot, then add the rice and soy sauce.\\n3. Fold in the egg and sliced spring onion.",
                    ingredient("cooked rice", 200, "g"), ingredient("egg", 1, "pc"),
                    ingredient("carrot", 1, "pc"), ingredient("spring onion", 1, "pc"),
                    ingredient("soy sauce", 1, "tbsp"));
            seedRecipe(db, "Potato & Onion Hash",
                    "1. Dice the potatoes and onion.\\n2. Heat olive oil in a pan and cook the potatoes until browned and tender.\\n3. Add onion and salt; cook until soft.",
                    ingredient("potato", 2, "pcs"), ingredient("onion", 1, "pc"),
                    ingredient("olive oil", 1, "tbsp"), ingredient("salt", 1, "pinch"));
            seedRecipe(db, "Chickpea Tomato Salad",
                    "1. Drain and rinse the chickpeas.\\n2. Chop the tomato, cucumber, and lemon.\\n3. Toss everything with olive oil.",
                    ingredient("chickpea", 150, "g"), ingredient("tomato", 1, "pc"),
                    ingredient("cucumber", 1, "pc"), ingredient("lemon", 1, "pc"),
                    ingredient("olive oil", 1, "tbsp"));
            seedRecipe(db, "Spinach Feta Toast",
                    "1. Toast the bread.\\n2. Wilt the spinach in a pan with olive oil.\\n3. Spoon over the toast and crumble feta on top.",
                    ingredient("bread", 2, "slices"), ingredient("spinach", 30, "g"),
                    ingredient("feta", 30, "g"), ingredient("olive oil", 1, "tsp"));
            seedRecipe(db, "Apple Cinnamon Oats",
                    "1. Simmer the oats in milk until creamy.\\n2. Dice the apple and stir it through.\\n3. Finish with cinnamon.",
                    ingredient("oats", 50, "g"), ingredient("apple", 1, "pc"),
                    ingredient("milk", 150, "ml"), ingredient("cinnamon", 1, "tsp"));
            seedRecipe(db, "Tuna Sweetcorn Pasta",
                    "1. Cook and drain the pasta.\\n2. Mix tuna and sweetcorn with mayonnaise.\\n3. Fold through the pasta and serve.",
                    ingredient("pasta", 150, "g"), ingredient("tuna", 1, "can"),
                    ingredient("sweetcorn", 50, "g"), ingredient("mayonnaise", 1, "tbsp"));
            seedRecipe(db, "Black Bean Quesadilla",
                    "1. Mash the black beans lightly.\\n2. Spread over the tortilla and add grated cheese and chopped tomato.\\n3. Fold and toast in a dry pan until the cheese melts.",
                    ingredient("tortilla", 1, "pc"), ingredient("black bean", 100, "g"),
                    ingredient("cheese", 30, "g"), ingredient("tomato", 1, "pc"));
            seedRecipe(db, "Carrot Soup",
                    "1. Slice the carrot and onion.\\n2. Soften them in olive oil.\\n3. Add vegetable stock and simmer until tender, then blend until smooth.",
                    ingredient("carrot", 3, "pcs"), ingredient("onion", 1, "pc"),
                    ingredient("vegetable stock", 300, "ml"), ingredient("olive oil", 1, "tsp"));
            seedRecipe(db, "Avocado Egg Toast",
                    "1. Toast the bread.\\n2. Mash the avocado with a little salt.\\n3. Cook the egg and serve it on top of the avocado toast.",
                    ingredient("bread", 1, "slice"), ingredient("avocado", 1, "pc"),
                    ingredient("egg", 1, "pc"));
            seedRecipe(db, "Mushroom Garlic Rice",
                    "1. Cook the rice.\\n2. Slice and sauté the mushrooms with garlic.\\n3. Add soy sauce and serve over the rice.",
                    ingredient("rice", 150, "g"), ingredient("mushroom", 100, "g"),
                    ingredient("garlic", 1, "clove"), ingredient("soy sauce", 1, "tbsp"));
            seedRecipe(db, "Yogurt Berry Bowl",
                    "1. Spoon yogurt into a bowl.\\n2. Add berries and oats.\\n3. Drizzle with honey.",
                    ingredient("yogurt", 150, "g"), ingredient("berry", 50, "g"),
                    ingredient("oats", 20, "g"), ingredient("honey", 1, "tsp"));
            seedRecipe(db, "Lentil Tomato Curry",
                    "1. Soften chopped onion in a saucepan.\\n2. Stir in curry powder, lentils, tomato, and coconut milk.\\n3. Simmer until the lentils are tender.",
                    ingredient("lentil", 100, "g"), ingredient("tomato", 1, "pc"),
                    ingredient("onion", 1, "pc"), ingredient("curry powder", 1, "tbsp"),
                    ingredient("coconut milk", 100, "ml"));
            seedRecipe(db, "Cheese & Tomato Wrap",
                    "1. Lay the tortilla flat.\\n2. Add sliced tomato, lettuce, and grated cheese.\\n3. Roll firmly and serve.",
                    ingredient("tortilla", 1, "pc"), ingredient("cheese", 30, "g"),
                    ingredient("tomato", 1, "pc"), ingredient("lettuce", 20, "g"));
            seedRecipe(db, "Sweet Potato & Black Bean Bowl",
                    "1. Dice and roast the sweet potato until tender.\\n2. Warm the black beans.\\n3. Serve together with sliced avocado and lime juice.",
                    ingredient("sweet potato", 1, "pc"), ingredient("black bean", 100, "g"),
                    ingredient("avocado", 1, "pc"), ingredient("lime", 1, "pc"));
            seedRecipe(db, "Cucumber Yogurt Dip",
                    "1. Grate the cucumber and squeeze out excess water.\\n2. Stir into yogurt with crushed garlic.\\n3. Add lemon juice and chill before serving.",
                    ingredient("yogurt", 100, "g"), ingredient("cucumber", 1, "pc"),
                    ingredient("garlic", 1, "clove"), ingredient("lemon", 1, "pc"));
            seedRecipe(db, "Lemon Herb Chickpeas",
                    "1. Drain and rinse the chickpeas.\\n2. Warm them gently with olive oil.\\n3. Add lemon juice and chopped parsley before serving.",
                    ingredient("chickpea", 100, "g"), ingredient("lemon", 1, "pc"),
                    ingredient("olive oil", 1, "tbsp"), ingredient("parsley", 5, "g"));
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    private void seedRecipe(SQLiteDatabase db, String name, String instructions,
                            SeedIngredient... ingredients) {
        ContentValues recipeValues = new ContentValues();
        recipeValues.put("name", name);
        recipeValues.put("instructions", instructions);
        long recipeId = db.insertOrThrow(TABLE_RECIPES, null, recipeValues);

        for (SeedIngredient ingredient : ingredients) {
            ContentValues ingredientValues = new ContentValues();
            ingredientValues.put("recipe_id", recipeId);
            ingredientValues.put("ingredient_name", ingredient.name);
            ingredientValues.put("quantity", ingredient.quantity);
            ingredientValues.put("unit", ingredient.unit);
            db.insertOrThrow(TABLE_RECIPE_INGREDIENTS, null, ingredientValues);
        }
    }

    private static SeedIngredient ingredient(String name, double quantity, String unit) {
        return new SeedIngredient(name, quantity, unit);
    }

    private static final class SeedIngredient {
        private final String name;
        private final double quantity;
        private final String unit;

        private SeedIngredient(String name, double quantity, String unit) {
            this.name = name;
            this.quantity = quantity;
            this.unit = unit;
        }
    }
}
