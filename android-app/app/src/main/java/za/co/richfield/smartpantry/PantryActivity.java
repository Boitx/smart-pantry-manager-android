package za.co.richfield.smartpantry;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import za.co.richfield.smartpantry.data.DatabaseHelper;
import za.co.richfield.smartpantry.model.PantryItem;
import za.co.richfield.smartpantry.ui.PantryAdapter;

import java.util.List;

public class PantryActivity extends BaseNavigationActivity {
    private DatabaseHelper database;
    private PantryAdapter adapter;
    private RecyclerView pantryList;
    private TextView emptyMessage;
    private boolean expiryAlertsEnabled;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pantry);
        database = new DatabaseHelper(this);
        pantryList = findViewById(R.id.pantryList);
        emptyMessage = findViewById(R.id.emptyPantryMessage);
        pantryList.setLayoutManager(new LinearLayoutManager(this));
        adapter = new PantryAdapter(this::editItem, this::confirmDelete);
        pantryList.setAdapter(adapter);

        findViewById(R.id.addIngredientButton).setOnClickListener(view ->
                startActivity(new Intent(this, AddEditIngredientActivity.class)));
        configureBottomNavigation(R.id.nav_pantry);
    }

    @Override
    protected void onResume() {
        super.onResume();
        expiryAlertsEnabled = getSharedPreferences(SettingsActivity.PREFERENCES_NAME, MODE_PRIVATE)
                .getBoolean(SettingsActivity.KEY_EXPIRY_ALERTS, false);
        refreshPantry();
    }

    private void refreshPantry() {
        List<PantryItem> items = database.getAllPantryItems();
        adapter.submitList(items, expiryAlertsEnabled);
        boolean isEmpty = items.isEmpty();
        emptyMessage.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
        pantryList.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
    }

    private void editItem(PantryItem item) {
        Intent intent = new Intent(this, AddEditIngredientActivity.class);
        intent.putExtra(AddEditIngredientActivity.EXTRA_ITEM_ID, item.getId());
        startActivity(intent);
    }

    private void confirmDelete(PantryItem item) {
        new MaterialAlertDialogBuilder(this)
                .setTitle("Remove " + item.getName() + "?")
                .setMessage("This will delete the item from your pantry.")
                .setNegativeButton("Keep item", (dialog, which) -> dialog.dismiss())
                .setPositiveButton("Delete", (dialog, which) -> {
                    database.deletePantryItem(item.getId());
                    refreshPantry();
                })
                .show();
    }
}
