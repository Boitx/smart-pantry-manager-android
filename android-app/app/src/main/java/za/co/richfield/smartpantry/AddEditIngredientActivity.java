package za.co.richfield.smartpantry;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputLayout;

import za.co.richfield.smartpantry.data.DatabaseHelper;
import za.co.richfield.smartpantry.model.PantryItem;

import java.util.Locale;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;

public class AddEditIngredientActivity extends AppCompatActivity {
    public static final String EXTRA_ITEM_ID = "pantry_item_id";
    private static final long NEW_ITEM_ID = -1L;

    private DatabaseHelper database;
    private long itemId = NEW_ITEM_ID;
    private TextInputLayout nameLayout;
    private TextInputLayout quantityLayout;
    private TextInputLayout unitLayout;
    private EditText nameInput;
    private EditText quantityInput;
    private EditText unitInput;
    private EditText expiryInput;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit_ingredient);
        database = new DatabaseHelper(this);

        nameLayout = findViewById(R.id.ingredientNameLayout);
        quantityLayout = findViewById(R.id.ingredientQuantityLayout);
        unitLayout = findViewById(R.id.ingredientUnitLayout);
        nameInput = findViewById(R.id.ingredientNameInput);
        quantityInput = findViewById(R.id.ingredientQuantityInput);
        unitInput = findViewById(R.id.ingredientUnitInput);
        expiryInput = findViewById(R.id.ingredientExpiryInput);

        itemId = getIntent().getLongExtra(EXTRA_ITEM_ID, NEW_ITEM_ID);
        TextView title = findViewById(R.id.ingredientFormTitle);
        title.setText(itemId == NEW_ITEM_ID ? "Add pantry item" : "Edit pantry item");
        if (itemId != NEW_ITEM_ID) populateForm(database.getPantryItem(itemId));

        Button saveButton = findViewById(R.id.saveIngredientButton);
        saveButton.setOnClickListener(view -> saveItem());
        findViewById(R.id.cancelIngredientButton).setOnClickListener(view -> finish());
    }

    private void populateForm(PantryItem item) {
        if (item == null) {
            finish();
            return;
        }
        nameInput.setText(item.getName());
        quantityInput.setText(String.format(Locale.getDefault(), "%s",
                item.getQuantity() == Math.rint(item.getQuantity())
                        ? Long.toString((long) item.getQuantity())
                        : Double.toString(item.getQuantity())));
        unitInput.setText(item.getUnit());
        expiryInput.setText(item.getExpiryDate());
    }

    private void saveItem() {
        nameLayout.setError(null);
        quantityLayout.setError(null);
        unitLayout.setError(null);
        expiryInput.setError(null);

        String name = nameInput.getText() == null ? "" : nameInput.getText().toString().trim();
        String quantityText = quantityInput.getText() == null
                ? "" : quantityInput.getText().toString().trim();
        String unit = unitInput.getText() == null ? "" : unitInput.getText().toString().trim();
        String expiry = expiryInput.getText() == null ? "" : expiryInput.getText().toString().trim();

        boolean valid = true;
        if (TextUtils.isEmpty(name)) {
            nameLayout.setError("Enter an ingredient name");
            valid = false;
        }
        double quantity = 0;
        try {
            quantity = Double.parseDouble(quantityText);
            if (!Double.isFinite(quantity) || quantity <= 0) {
                quantityLayout.setError("Quantity must be greater than zero");
                valid = false;
            }
        } catch (NumberFormatException exception) {
            quantityLayout.setError("Enter a valid quantity");
            valid = false;
        }
        if (TextUtils.isEmpty(unit)) {
            unitLayout.setError("Enter a unit, such as g, ml, or pcs");
            valid = false;
        }
        if (!expiry.isEmpty() && !isValidExpiryDate(expiry)) {
            expiryInput.setError("Use YYYY-MM-DD or leave blank");
            valid = false;
        }
        if (!valid) return;

        if (itemId == NEW_ITEM_ID) {
            database.insertPantryItem(name, quantity, unit, expiry);
        } else {
            database.updatePantryItem(itemId, name, quantity, unit, expiry);
        }
        finish();
    }

    private boolean isValidExpiryDate(String value) {
        if (!value.matches("\\d{4}-\\d{2}-\\d{2}")) return false;
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        format.setLenient(false);
        ParsePosition position = new ParsePosition(0);
        return format.parse(value, position) != null && position.getIndex() == value.length();
    }
}
