package za.co.richfield.smartpantry;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import za.co.richfield.smartpantry.data.DatabaseHelper;
import za.co.richfield.smartpantry.logic.IngredientMatcher;
import za.co.richfield.smartpantry.model.PantryItem;
import za.co.richfield.smartpantry.model.Recipe;
import za.co.richfield.smartpantry.ui.RecipeAdapter;

import java.util.ArrayList;
import java.util.List;

public class SuggestedRecipesActivity extends BaseNavigationActivity {
    private DatabaseHelper database;
    private RecipeAdapter adapter;
    private RecyclerView recipeList;
    private TextView emptyMessage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_suggested_recipes);
        database = new DatabaseHelper(this);
        recipeList = findViewById(R.id.suggestedRecipeList);
        emptyMessage = findViewById(R.id.emptyRecipesMessage);
        recipeList.setLayoutManager(new LinearLayoutManager(this));
        adapter = new RecipeAdapter(this::openRecipe);
        recipeList.setAdapter(adapter);
        configureBottomNavigation(R.id.nav_recipes);
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshSuggestions();
    }

    private void refreshSuggestions() {
        List<PantryItem> pantry = database.getAllPantryItems();
        List<Recipe> matches = new ArrayList<>();
        for (Recipe recipe : database.getAllRecipes()) {
            if (IngredientMatcher.canMake(recipe, pantry)) matches.add(recipe);
        }
        adapter.submitList(matches);
        boolean isEmpty = matches.isEmpty();
        emptyMessage.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
        recipeList.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
    }

    private void openRecipe(Recipe recipe) {
        Intent intent = new Intent(this, RecipeDetailActivity.class);
        intent.putExtra(RecipeDetailActivity.EXTRA_RECIPE_ID, recipe.getId());
        startActivity(intent);
    }
}
