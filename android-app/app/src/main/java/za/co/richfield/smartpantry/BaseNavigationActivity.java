package za.co.richfield.smartpantry;

import android.content.Intent;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;

abstract class BaseNavigationActivity extends AppCompatActivity {
    protected void configureBottomNavigation(int selectedItemId) {
        BottomNavigationView navigation = findViewById(R.id.bottomNavigation);
        navigation.setSelectedItemId(selectedItemId);
        navigation.setOnItemSelectedListener(item -> {
            int destinationId = item.getItemId();
            if (destinationId == selectedItemId) return true;

            Class<?> destination;
            if (destinationId == R.id.nav_pantry) {
                destination = PantryActivity.class;
            } else if (destinationId == R.id.nav_recipes) {
                destination = SuggestedRecipesActivity.class;
            } else if (destinationId == R.id.nav_settings) {
                destination = SettingsActivity.class;
            } else {
                return false;
            }

            Intent intent = new Intent(this, destination);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
            return true;
        });
    }
}
