package tehohrana.ru.cathodmon;

/**
 * Created by AG on 25.05.2016.
 */

import android.os.Bundle;
import android.preference.PreferenceActivity;



public class Prefs extends PreferenceActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.settings);
    }
}