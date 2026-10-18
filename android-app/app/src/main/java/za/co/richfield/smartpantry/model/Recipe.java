package za.co.richfield.smartpantry.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Recipe {
    private final long id;
    private final String name;
    private final String instructions;
    private final List<RecipeIngredient> ingredients;

    public Recipe(long id, String name, String instructions, List<RecipeIngredient> ingredients) {
        this.id = id;
        this.name = name == null ? "" : name;
        this.instructions = instructions == null ? "" : instructions;
        this.ingredients = Collections.unmodifiableList(
                new ArrayList<>(ingredients == null ? Collections.emptyList() : ingredients));
    }

    public long getId() { return id; }
    public String getName() { return name; }
    public String getInstructions() { return instructions; }
    public List<RecipeIngredient> getIngredients() { return ingredients; }
}
