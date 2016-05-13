package ru.tehohrana.smsreceiver.config;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.regex.Pattern;

import ru.tehohrana.smsreceiver.ConfiguratorActivity;
import ru.tehohrana.smsreceiver.R;

/**
 * Created by AG on 03.12.2015.
 */

public class ConfigGen extends Activity{
    SharedPreferences mDeviceSettings;
    private EditText mEditTextUssdPhone;
    private Spinner mSpinnerUssdInterval;
    private Spinner mSpinnerTimeZone;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_config_general);


        mSpinnerTimeZone = (Spinner) findViewById(R.id.spinnerConfigTimeZone);
// Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapterTimeZone = ArrayAdapter.createFromResource(this,
                R.array.timezone, android.R.layout.simple_spinner_item);
// Specify the layout to use when the list of choices appears
        adapterTimeZone.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
// Apply the adapter to the spinner
        mSpinnerTimeZone.setAdapter(adapterTimeZone);

        mSpinnerUssdInterval = (Spinner) findViewById(R.id.spinnerConfigCashPeriod);
// Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapterUssdInterval = ArrayAdapter.createFromResource(this,
                R.array.cash_interval, android.R.layout.simple_spinner_item);
// Specify the layout to use when the list of choices appears
        adapterUssdInterval.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
// Apply the adapter to the spinner
        mSpinnerUssdInterval.setAdapter(adapterUssdInterval);


        //Чтение настроек из файла
        mDeviceSettings = getSharedPreferences(ConfiguratorActivity.CONFIG_PREFERENCES,
                Context.MODE_PRIVATE);

        //Номер USSD запроса баланса
        mEditTextUssdPhone = (EditText) findViewById(R.id.editTextConfigUssdPhone);
        if (mDeviceSettings.contains(ConfiguratorActivity.CONFIG_PREFERENCES_USSD_PHONE))
            mEditTextUssdPhone.setText(mDeviceSettings.getString(
                    ConfiguratorActivity.CONFIG_PREFERENCES_USSD_PHONE,""));

        //Интервал запроса баланса
        if (mDeviceSettings.contains(ConfiguratorActivity.CONFIG_PREFERENCES_USSD_INTERVAL)) {
            mSpinnerUssdInterval.setSelection(mDeviceSettings.getInt(
                    ConfiguratorActivity.CONFIG_PREFERENCES_USSD_INTERVAL, 0));
            adapterUssdInterval.notifyDataSetChanged();
        }

        //Тайм зона
        if (mDeviceSettings.contains(ConfiguratorActivity.CONFIG_PREFERENCES_TIME_ZONE)) {
            mSpinnerTimeZone.setSelection(mDeviceSettings.getInt(
                    ConfiguratorActivity.CONFIG_PREFERENCES_TIME_ZONE, 0));
            adapterTimeZone.notifyDataSetChanged();
        }

    }

    public void onBtnConfigGenApplyClick(View view) {

        //Записываем данные в файл настроек
        mDeviceSettings = getSharedPreferences(ConfiguratorActivity.CONFIG_PREFERENCES,
                Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = mDeviceSettings.edit();

        editor.putString(ConfiguratorActivity.CONFIG_PREFERENCES_USSD_PHONE,
                mEditTextUssdPhone.getText().toString());

        editor.putInt(ConfiguratorActivity.CONFIG_PREFERENCES_USSD_INTERVAL,
                mSpinnerUssdInterval.getSelectedItemPosition());

        editor.putInt(ConfiguratorActivity.CONFIG_PREFERENCES_TIME_ZONE,
                mSpinnerTimeZone.getSelectedItemPosition());

        editor.apply();
        super.finish();
    }


    public void onBtnConfigGenCancelClick(View view) {
        super.finish();
    }


    public void onBackBtnPressed(View view) {
        onBackPressed();
    }
}
