package ru.tehohrana.smsreceiver.config;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;

import java.util.regex.Pattern;

import ru.tehohrana.smsreceiver.ConfiguratorActivity;
import ru.tehohrana.smsreceiver.R;


public class ConfigAcc extends Activity {

    SharedPreferences mDeviceSettings;
    CheckBox mCheckBoxEnable;
    SeekBar mSeekBarAccSens;
    Spinner mSpinnerAccTime;
    Switch mSwitchHorn, mSwitchBlock;
    CheckBox    mCheckBoxPcnSms, mCheckBoxPcnCall,
            mCheckBoxOwn1Sms, mCheckBoxOwn1Call,
            mCheckBoxOwn2Sms, mCheckBoxOwn2Call;
    TextView mTextViewAccSens, mTextViewTime, mTextViewAction, mTextViewAlarm,
            mTextViewAlarmPcn, mTextViewAlarmOwn1, mTextViewAlarmOwn2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_config_acc);

        mSpinnerAccTime = (Spinner) findViewById(R.id.spinnerConfigAccInterval);
// Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.acc_time_array, android.R.layout.simple_spinner_item);
// Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
// Apply the adapter to the spinner
        mSpinnerAccTime.setAdapter(adapter);

        mCheckBoxEnable = (CheckBox) findViewById(R.id.checkBoxConfigAccEnable);
        mSeekBarAccSens = (SeekBar) findViewById(R.id.seekBarConfigAccSens);
        mSpinnerAccTime = (Spinner) findViewById(R.id.spinnerConfigAccInterval);
        mSwitchHorn = (Switch) findViewById(R.id.switchConfigAccHorn);
        mSwitchBlock = (Switch) findViewById(R.id.switchConfigAccBlock);
        mCheckBoxPcnSms = (CheckBox) findViewById(R.id.checkBoxConfigAccPcnSms);
        mCheckBoxPcnCall = (CheckBox) findViewById(R.id.checkBoxConfigAccPcnCall);
        mCheckBoxOwn1Sms = (CheckBox) findViewById(R.id.checkBoxConfigAccOwn1Sms);
        mCheckBoxOwn1Call = (CheckBox) findViewById(R.id.checkBoxConfigAccOwn1Call);
        mCheckBoxOwn2Sms = (CheckBox) findViewById(R.id.checkBoxConfigAccOwn2Sms);
        mCheckBoxOwn2Call = (CheckBox) findViewById(R.id.checkBoxConfigAccOwn2Call);
        mTextViewAccSens = (TextView) findViewById(R.id.textViewConfigAccSensLabel);
        mTextViewTime = (TextView) findViewById(R.id.textViewConfigAccIntervalLabel);
        mTextViewAction = (TextView) findViewById(R.id.textViewConfigAccActionLabel);
        mTextViewAlarm = (TextView) findViewById(R.id.textViewConfigAccAlarmLabel);
        mTextViewAlarmPcn = (TextView) findViewById(R.id.textViewConfigAccAlarmPcnLabel);
        mTextViewAlarmOwn1 = (TextView) findViewById(R.id.textViewConfigAccOwn1Label);
        mTextViewAlarmOwn2 = (TextView) findViewById(R.id.textViewConfigAccOwn2Label);


        //////  ЗАГРУЗКА ДАННЫХ
        //Чтение настроек из файла
        mDeviceSettings = getSharedPreferences(ConfiguratorActivity.CONFIG_PREFERENCES,
                Context.MODE_PRIVATE);
        //Enable
        if (mDeviceSettings.contains(ConfiguratorActivity.CONFIG_PREFERENCES_ACC_ENABLE))
            mCheckBoxEnable.setChecked(mDeviceSettings.getBoolean(
                    ConfiguratorActivity.CONFIG_PREFERENCES_ACC_ENABLE, false));
        //Sense
        if (mDeviceSettings.contains(ConfiguratorActivity.CONFIG_PREFERENCES_ACC_SENS))
            mSeekBarAccSens.setProgress(mDeviceSettings.getInt(
                    ConfiguratorActivity.CONFIG_PREFERENCES_ACC_SENS, 0));
        //Time
        if (mDeviceSettings.contains(ConfiguratorActivity.CONFIG_PREFERENCES_ACC_TIME)) {
            mSpinnerAccTime.setSelection(mDeviceSettings.getInt(
                    ConfiguratorActivity.CONFIG_PREFERENCES_ACC_TIME, 0));
            adapter.notifyDataSetChanged();
        }
        //Horn
        if (mDeviceSettings.contains(ConfiguratorActivity.CONFIG_PREFERENCES_ACC_ACTION_HORN))
            mSwitchHorn.setChecked(mDeviceSettings.
                    getBoolean(ConfiguratorActivity.CONFIG_PREFERENCES_ACC_ACTION_HORN, false));

        //Bock
        if (mDeviceSettings.contains(ConfiguratorActivity.CONFIG_PREFERENCES_ACC_ACTION_BLOCK))
            mSwitchBlock.setChecked(mDeviceSettings.
                    getBoolean(ConfiguratorActivity.CONFIG_PREFERENCES_ACC_ACTION_BLOCK,false));

        //Alarm PCN SMS/CALL
        if (mDeviceSettings.contains(ConfiguratorActivity.CONFIG_PREFERENCES_ACC_ALARM_PCN_SMS))
            mCheckBoxPcnSms.setChecked(mDeviceSettings.
                    getBoolean(ConfiguratorActivity.CONFIG_PREFERENCES_ACC_ALARM_PCN_SMS, false));
        if (mDeviceSettings.contains(ConfiguratorActivity.CONFIG_PREFERENCES_ACC_ALARM_PCN_CALL))
            mCheckBoxPcnCall.setChecked(mDeviceSettings.
                    getBoolean(ConfiguratorActivity.CONFIG_PREFERENCES_ACC_ALARM_PCN_CALL, false));
        //Alarm OWN1 SMS/CALL
        if (mDeviceSettings.contains(ConfiguratorActivity.CONFIG_PREFERENCES_ACC_ALARM_OWN1_SMS))
            mCheckBoxOwn1Sms.setChecked(mDeviceSettings.
                    getBoolean(ConfiguratorActivity.CONFIG_PREFERENCES_ACC_ALARM_OWN1_SMS, false));
        if (mDeviceSettings.contains(ConfiguratorActivity.CONFIG_PREFERENCES_ACC_ALARM_OWN1_CALL))
            mCheckBoxOwn1Call.setChecked(mDeviceSettings.
                    getBoolean(ConfiguratorActivity.CONFIG_PREFERENCES_ACC_ALARM_OWN1_CALL, false));
        //Alarm OWN2 SMS/CALL
        if (mDeviceSettings.contains(ConfiguratorActivity.CONFIG_PREFERENCES_ACC_ALARM_OWN2_SMS))
            mCheckBoxOwn2Sms.setChecked(mDeviceSettings.
                    getBoolean(ConfiguratorActivity.CONFIG_PREFERENCES_ACC_ALARM_OWN2_SMS, false));
        if (mDeviceSettings.contains(ConfiguratorActivity.CONFIG_PREFERENCES_ACC_ALARM_OWN2_CALL))
            mCheckBoxOwn2Call.setChecked(mDeviceSettings.
                    getBoolean(ConfiguratorActivity.CONFIG_PREFERENCES_ACC_ALARM_OWN2_CALL, false));

        mSeekBarAccSens.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                switch (mSeekBarAccSens.getProgress())
                {
                    case 0: mTextViewAccSens.setText("Выключено "); break;
                    case 1: mTextViewAccSens.setText("Супер грубо "); break;
                    case 2: mTextViewAccSens.setText("Очень грубо "); break;
                    case 3: mTextViewAccSens.setText("Грубо "); break;
                    case 4: mTextViewAccSens.setText("Ниже среднего "); break;
                    case 5: mTextViewAccSens.setText("Средний "); break;
                    case 6: mTextViewAccSens.setText("Выше среднего "); break;
                    case 7: mTextViewAccSens.setText("Высокая "); break;
                    case 8: mTextViewAccSens.setText("Очень высокая "); break;
                    case 9: mTextViewAccSens.setText("Супер высокая "); break;
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        //// ОБРАБОТКА НЕАКТИВНЫХ ПОЛЕЙ
        boolean value = mCheckBoxEnable.isChecked();
        mSpinnerAccTime.setEnabled(value);
        mSeekBarAccSens.setEnabled(value);
        mTextViewAccSens.setEnabled(value);
        mTextViewTime.setEnabled(value);
        mTextViewAction.setEnabled(value);
        mSwitchHorn.setEnabled(value);
        mSwitchBlock.setEnabled(value);
        mCheckBoxPcnSms.setEnabled(value);
        mCheckBoxPcnCall.setEnabled(value);
        mCheckBoxOwn1Sms.setEnabled(value);
        mCheckBoxOwn1Call.setEnabled(value);
        mCheckBoxOwn2Sms.setEnabled(value);
        mCheckBoxOwn2Call.setEnabled(value);
        mTextViewAlarm.setEnabled(value);
        mTextViewAlarmPcn.setEnabled(value);
        mTextViewAlarmOwn1.setEnabled(value);
        mTextViewAlarmOwn2.setEnabled(value);
        if (value) updateAlarmCheckBox();


        switch (mSeekBarAccSens.getProgress())
        {
            case 0: mTextViewAccSens.setText("Выключено "); break;
            case 1: mTextViewAccSens.setText("Супер грубо "); break;
            case 2: mTextViewAccSens.setText("Очень грубо "); break;
            case 3: mTextViewAccSens.setText("Грубо "); break;
            case 4: mTextViewAccSens.setText("Ниже среднего "); break;
            case 5: mTextViewAccSens.setText("Средний "); break;
            case 6: mTextViewAccSens.setText("Выше среднего "); break;
            case 7: mTextViewAccSens.setText("Высокая "); break;
            case 8: mTextViewAccSens.setText("Очень высокая "); break;
            case 9: mTextViewAccSens.setText("Супер высокая "); break;

        }
        //// ОБРАБОТКА НАЖАТИЙ
        mCheckBoxEnable.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean value = mCheckBoxEnable.isChecked();
                mSpinnerAccTime.setEnabled(value);
                mSeekBarAccSens.setEnabled(value);
                mTextViewAccSens.setEnabled(value);
                mTextViewTime.setEnabled(value);
                mTextViewAction.setEnabled(value);
                mSwitchHorn.setEnabled(value);
                mSwitchBlock.setEnabled(value);
                mCheckBoxPcnSms.setEnabled(value);
                mCheckBoxPcnCall.setEnabled(value);
                mCheckBoxOwn1Sms.setEnabled(value);
                mCheckBoxOwn1Call.setEnabled(value);
                mCheckBoxOwn2Sms.setEnabled(value);
                mCheckBoxOwn2Call.setEnabled(value);
                mTextViewAlarm.setEnabled(value);
                mTextViewAlarmPcn.setEnabled(value);
                mTextViewAlarmOwn1.setEnabled(value);
                mTextViewAlarmOwn2.setEnabled(value);
                if (value) updateAlarmCheckBox();
            }
        });

    }


    public void updateAlarmCheckBox()
    {
        if (mDeviceSettings.contains(ConfiguratorActivity.CONFIG_PREFERENCES_PCN_PHONE))
        {
            if (Pattern.matches("[7][9]([0-9]{9})", mDeviceSettings.
                    getString(ConfiguratorActivity.CONFIG_PREFERENCES_PCN_PHONE, "")))
            {
                mTextViewAlarmPcn.setEnabled(true);
                mCheckBoxPcnSms.setEnabled(true);
                mCheckBoxPcnCall.setEnabled(true);
            } else {
                mTextViewAlarmPcn.setEnabled(false);
                mCheckBoxPcnSms.setEnabled(false);
                mCheckBoxPcnCall.setEnabled(false);
                mCheckBoxPcnSms.setChecked(false);
                mCheckBoxPcnCall.setChecked(false);
            }
        }
        if (mDeviceSettings.contains(ConfiguratorActivity.CONFIG_PREFERENCES_OWN1_PHONE))
        {
            if (Pattern.matches("[7][9]([0-9]{9})", mDeviceSettings.
                    getString(ConfiguratorActivity.CONFIG_PREFERENCES_OWN1_PHONE,"")))
            {
                mTextViewAlarmOwn1.setEnabled(true);
                mCheckBoxOwn1Sms.setEnabled(true);
                mCheckBoxOwn1Call.setEnabled(true);
            } else {
                mTextViewAlarmOwn1.setEnabled(false);
                mCheckBoxOwn1Sms.setEnabled(false);
                mCheckBoxOwn1Call.setEnabled(false);
                mCheckBoxOwn1Sms.setChecked(false);
                mCheckBoxOwn1Call.setChecked(false);
            }
        }
        if (mDeviceSettings.contains(ConfiguratorActivity.CONFIG_PREFERENCES_OWN2_PHONE)) {
            if (Pattern.matches("[7][9]([0-9]{9})", mDeviceSettings.
                    getString(ConfiguratorActivity.CONFIG_PREFERENCES_OWN2_PHONE,"")))
            {
                mTextViewAlarmOwn2.setEnabled(true);
                mCheckBoxOwn2Sms.setEnabled(true);
                mCheckBoxOwn2Call.setEnabled(true);
            } else {
                mTextViewAlarmOwn2.setEnabled(false);
                mCheckBoxOwn2Sms.setEnabled(false);
                mCheckBoxOwn2Call.setEnabled(false);
                mCheckBoxOwn2Sms.setChecked(false);
                mCheckBoxOwn2Call.setChecked(false);
            }
        }
    }

    public void onBtnConfigAccCancelClick(View view) {
        super.finish();
    }

    public void onBtnConfigAccApplyClick(View view) {
        mDeviceSettings = getSharedPreferences(ConfiguratorActivity.CONFIG_PREFERENCES,
                Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = mDeviceSettings.edit();

        editor.putBoolean(ConfiguratorActivity.CONFIG_PREFERENCES_ACC_ENABLE,
                mCheckBoxEnable.isChecked());

        editor.putInt(ConfiguratorActivity.CONFIG_PREFERENCES_ACC_TIME,
                mSpinnerAccTime.getSelectedItemPosition());

        editor.putInt(ConfiguratorActivity.CONFIG_PREFERENCES_ACC_SENS,
                mSeekBarAccSens.getProgress());

        editor.putBoolean(ConfiguratorActivity.CONFIG_PREFERENCES_ACC_ACTION_HORN,
                mSwitchHorn.isChecked());

        editor.putBoolean(ConfiguratorActivity.CONFIG_PREFERENCES_ACC_ACTION_BLOCK,
                mSwitchBlock.isChecked());

        editor.putBoolean(ConfiguratorActivity.CONFIG_PREFERENCES_ACC_ALARM_PCN_SMS,
                mCheckBoxPcnSms.isChecked());
        editor.putBoolean(ConfiguratorActivity.CONFIG_PREFERENCES_ACC_ALARM_PCN_CALL,
                mCheckBoxPcnCall.isChecked());

        editor.putBoolean(ConfiguratorActivity.CONFIG_PREFERENCES_ACC_ALARM_OWN1_SMS,
                mCheckBoxOwn1Sms.isChecked());
        editor.putBoolean(ConfiguratorActivity.CONFIG_PREFERENCES_ACC_ALARM_OWN1_CALL,
                mCheckBoxOwn1Call.isChecked());

        editor.putBoolean(ConfiguratorActivity.CONFIG_PREFERENCES_ACC_ALARM_OWN2_SMS,
                mCheckBoxOwn2Sms.isChecked());
        editor.putBoolean(ConfiguratorActivity.CONFIG_PREFERENCES_ACC_ALARM_OWN2_CALL,
                mCheckBoxOwn2Call.isChecked());

        editor.apply();
        super.finish();
    }
}
