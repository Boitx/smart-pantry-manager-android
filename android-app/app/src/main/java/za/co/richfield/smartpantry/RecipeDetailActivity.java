package za.co.richfield.smartpantry;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import za.co.richfield.smartpantry.data.DatabaseHelper;
import za.co.richfield.smartpantry.model.Recipe;
import za.co.richfield.smartpantry.model.RecipeIngredient;

public class RecipeDetailActivity extends AppCompatActivity {
    public static final String EXTRA_RECIPE_ID = "recipe_id";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe_detail);

        long recipeId = getIntent().getLongExtra(EXTRA_RECIPE_ID, -1L);
        Recipe recipe = new DatabaseHelper(this).getRecipe(recipeId);
        if (recipe == null) {
            finish();
            return;
        }

        ((TextView) findViewById(R.id.recipeDetailTitle)).setText(recipe.getName());
        StringBuilder ingredients = new StringBuilder();
        for (RecipeIngredient ingredient : recipe.getIngredients()) {
            ingredients.append("•  ")
                    .append(formatQuantity(ingredient.getQuantity()))
                    .append(' ')
                    .append(ingredient.getUnit())
                    .append(' ')
                    .append(ingredient.getName())
                    .append('\n');
        }
        ((TextView) findViewById(R.id.recipeDetailIngredients)).setText(ingredients.toString().trim());
        ((TextView) findViewById(R.id.recipeDetailInstructions)).setText(recipe.getInstructions());
        findViewById(R.id.recipeDetailBackButton).setOnClickListener(view -> finish());
    }

    private String formatQuantity(double quantity) {
        if (quantity == Math.rint(quantity)) return Long.toString((long) quantity);
        return Double.toString(quantity);
    }
}
