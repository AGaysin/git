package ru.tehohrana.smsreceiver.config;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;

import ru.tehohrana.smsreceiver.ConfiguratorActivity;
import ru.tehohrana.smsreceiver.R;

/**
 * Created by AG on 03.12.2015.
 */

public class ConfigUgon extends Activity{
    SharedPreferences mDeviceSettings;

    private Spinner mSpinnerUgonDistance;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_config_ugon);


        mSpinnerUgonDistance = (Spinner) findViewById(R.id.spinnerConfigUgonDistance);
// Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapterUgonDistance = ArrayAdapter.createFromResource(this,
                R.array.ugon_distance, android.R.layout.simple_spinner_item);
// Specify the layout to use when the list of choices appears
        adapterUgonDistance.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
// Apply the adapter to the spinner
        mSpinnerUgonDistance.setAdapter(adapterUgonDistance);



        //Чтение настроек из файла
        mDeviceSettings = getSharedPreferences(ConfiguratorActivity.CONFIG_PREFERENCES,
                Context.MODE_PRIVATE);


        //Тайм зона
        if (mDeviceSettings.contains(ConfiguratorActivity.CONFIG_PREFERENCES_UGON_DISTANCE)) {
            mSpinnerUgonDistance.setSelection(mDeviceSettings.getInt(
                    ConfiguratorActivity.CONFIG_PREFERENCES_UGON_DISTANCE, 0));
            adapterUgonDistance.notifyDataSetChanged();
        }

    }

    public void onBtnConfigUgonApplyClick(View view) {

        //Записываем данные в файл настроек
        mDeviceSettings = getSharedPreferences(ConfiguratorActivity.CONFIG_PREFERENCES,
                Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = mDeviceSettings.edit();


        editor.putInt(ConfiguratorActivity.CONFIG_PREFERENCES_UGON_DISTANCE,
                mSpinnerUgonDistance.getSelectedItemPosition());

        editor.apply();
        super.finish();
    }


    public void onBtnConfigUgonCancelClick(View view) {
        super.finish();
    }


}
