package za.co.richfield.smartpantry;

import android.os.Bundle;

import com.google.android.material.switchmaterial.SwitchMaterial;

public class SettingsActivity extends BaseNavigationActivity {
    public static final String PREFERENCES_NAME = "smart_pantry_preferences";
    public static final String KEY_EXPIRY_ALERTS = "expiry_alerts_enabled";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        configureBottomNavigation(R.id.nav_settings);

        SwitchMaterial expirySwitch = findViewById(R.id.expiryAlertsSwitch);
        expirySwitch.setChecked(getSharedPreferences(PREFERENCES_NAME, MODE_PRIVATE)
                .getBoolean(KEY_EXPIRY_ALERTS, false));
        expirySwitch.setOnCheckedChangeListener((buttonView, isChecked) ->
                getSharedPreferences(PREFERENCES_NAME, MODE_PRIVATE)
                        .edit()
                        .putBoolean(KEY_EXPIRY_ALERTS, isChecked)
                        .apply());
    }
}
