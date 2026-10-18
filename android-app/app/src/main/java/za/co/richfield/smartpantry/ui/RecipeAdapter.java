package za.co.richfield.smartpantry.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import za.co.richfield.smartpantry.R;
import za.co.richfield.smartpantry.model.Recipe;

import java.util.Collections;
import java.util.List;

public class RecipeAdapter extends RecyclerView.Adapter<RecipeAdapter.RecipeViewHolder> {
    public interface RecipeClickListener {
        void onClick(Recipe recipe);
    }

    private final RecipeClickListener clickListener;
    private List<Recipe> recipes = Collections.emptyList();

    public RecipeAdapter(RecipeClickListener clickListener) {
        this.clickListener = clickListener;
    }

    public void submitList(List<Recipe> recipes) {
        this.recipes = recipes == null ? Collections.emptyList() : recipes;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public RecipeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_recipe, parent, false);
        return new RecipeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecipeViewHolder holder, int position) {
        Recipe recipe = recipes.get(position);
        holder.name.setText(recipe.getName());
        holder.summary.setText(recipe.getIngredients().size()
                + " ingredients · everything is in your pantry");
        holder.itemView.setOnClickListener(view -> clickListener.onClick(recipe));
    }

    @Override
    public int getItemCount() {
        return recipes.size();
    }

    static final class RecipeViewHolder extends RecyclerView.ViewHolder {
        private final TextView name;
        private final TextView summary;

        RecipeViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.recipeCardName);
            summary = itemView.findViewById(R.id.recipeCardSummary);
        }
    }
}
