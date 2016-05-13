package ru.tehohrana.smsreceiver.config;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;

import java.util.regex.Pattern;

import ru.tehohrana.smsreceiver.ConfiguratorActivity;
import ru.tehohrana.smsreceiver.R;


public class ConfigJamm extends Activity {
    SharedPreferences mDeviceSettings;
    CheckBox mCheckBoxEnable;
    Switch mSwitchHorn, mSwitchBlock;
    CheckBox    mCheckBoxPcnSms, mCheckBoxPcnCall,
            mCheckBoxOwn1Sms, mCheckBoxOwn1Call,
            mCheckBoxOwn2Sms, mCheckBoxOwn2Call;
    TextView mTextViewAction, mTextViewAlarm,
            mTextViewAlarmPcn, mTextViewAlarmOwn1, mTextViewAlarmOwn2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_config_jamm);

        mCheckBoxEnable = (CheckBox) findViewById(R.id.checkBoxConfigJammEnable);
        mSwitchHorn = (Switch) findViewById(R.id.switchConfigJammHorn);
        mSwitchBlock = (Switch) findViewById(R.id.switchConfigJammBlock);
        mCheckBoxPcnSms = (CheckBox) findViewById(R.id.checkBoxConfigJammAlarmPcnSms);
        mCheckBoxPcnCall = (CheckBox) findViewById(R.id.checkBoxConfigJammAlarmPcnCall);
        mCheckBoxOwn1Sms = (CheckBox) findViewById(R.id.checkBoxConfigJammAlarmOwn1Sms);
        mCheckBoxOwn1Call = (CheckBox) findViewById(R.id.checkBoxConfigJammAlarmOwn1Call);
        mCheckBoxOwn2Sms = (CheckBox) findViewById(R.id.checkBoxConfigJammAlarmOwn2Sms);
        mCheckBoxOwn2Call = (CheckBox) findViewById(R.id.checkBoxConfigJammAlarmOwn2Call);
        mTextViewAction = (TextView) findViewById(R.id.textViewConfigJammActionLabel);
        mTextViewAlarm = (TextView) findViewById(R.id.textViewConfigJammAlarmLabel);
        mTextViewAlarmPcn = (TextView) findViewById(R.id.textViewConfigJammAlarmPcnLabel);
        mTextViewAlarmOwn1 = (TextView) findViewById(R.id.textViewConfigJammAlarmOwn1Label);
        mTextViewAlarmOwn2 = (TextView) findViewById(R.id.textViewConfigJammAlarmOwn2Label);


        //////  ЗАГРУЗКА ДАННЫХ
        //Чтение настроек из файла
        mDeviceSettings = getSharedPreferences(ConfiguratorActivity.CONFIG_PREFERENCES,
                Context.MODE_PRIVATE);
        //Enable
        if (mDeviceSettings.contains(ConfiguratorActivity.CONFIG_PREFERENCES_JAMM_ENABLE))
            mCheckBoxEnable.setChecked(mDeviceSettings.getBoolean(
                    ConfiguratorActivity.CONFIG_PREFERENCES_JAMM_ENABLE, false));
        //Horn
        if (mDeviceSettings.contains(ConfiguratorActivity.CONFIG_PREFERENCES_JAMM_ACTION_HORN))
            mSwitchHorn.setChecked(mDeviceSettings.
                    getBoolean(ConfiguratorActivity.CONFIG_PREFERENCES_JAMM_ACTION_HORN, false));

        //Bock
        if (mDeviceSettings.contains(ConfiguratorActivity.CONFIG_PREFERENCES_JAMM_ACTION_BLOCK))
            mSwitchBlock.setChecked(mDeviceSettings.
                    getBoolean(ConfiguratorActivity.CONFIG_PREFERENCES_JAMM_ACTION_BLOCK,false));

        //Alarm PCN SMS/CALL
        if (mDeviceSettings.contains(ConfiguratorActivity.CONFIG_PREFERENCES_JAMM_ALARM_PCN_SMS))
            mCheckBoxPcnSms.setChecked(mDeviceSettings.
                    getBoolean(ConfiguratorActivity.CONFIG_PREFERENCES_JAMM_ALARM_PCN_SMS, false));
        if (mDeviceSettings.contains(ConfiguratorActivity.CONFIG_PREFERENCES_JAMM_ALARM_PCN_CALL))
            mCheckBoxPcnCall.setChecked(mDeviceSettings.
                    getBoolean(ConfiguratorActivity.CONFIG_PREFERENCES_JAMM_ALARM_PCN_CALL, false));
        //Alarm OWN1 SMS/CALL
        if (mDeviceSettings.contains(ConfiguratorActivity.CONFIG_PREFERENCES_JAMM_ALARM_OWN1_SMS))
            mCheckBoxOwn1Sms.setChecked(mDeviceSettings.
                    getBoolean(ConfiguratorActivity.CONFIG_PREFERENCES_JAMM_ALARM_OWN1_SMS, false));
        if (mDeviceSettings.contains(ConfiguratorActivity.CONFIG_PREFERENCES_JAMM_ALARM_OWN1_CALL))
            mCheckBoxOwn1Call.setChecked(mDeviceSettings.
                    getBoolean(ConfiguratorActivity.CONFIG_PREFERENCES_JAMM_ALARM_OWN1_CALL, false));
        //Alarm OWN2 SMS/CALL
        if (mDeviceSettings.contains(ConfiguratorActivity.CONFIG_PREFERENCES_JAMM_ALARM_OWN2_SMS))
            mCheckBoxOwn2Sms.setChecked(mDeviceSettings.
                    getBoolean(ConfiguratorActivity.CONFIG_PREFERENCES_JAMM_ALARM_OWN2_SMS, false));
        if (mDeviceSettings.contains(ConfiguratorActivity.CONFIG_PREFERENCES_JAMM_ALARM_OWN2_CALL))
            mCheckBoxOwn2Call.setChecked(mDeviceSettings.
                    getBoolean(ConfiguratorActivity.CONFIG_PREFERENCES_JAMM_ALARM_OWN2_CALL, false));


        //// ОБРАБОТКА НЕАКТИВНЫХ ПОЛЕЙ
        boolean value = mCheckBoxEnable.isChecked();
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



        //// ОБРАБОТКА НАЖАТИЙ
        mCheckBoxEnable.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean value = mCheckBoxEnable.isChecked();
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
                    getString(ConfiguratorActivity.CONFIG_PREFERENCES_OWN1_PHONE, "")))
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
                    getString(ConfiguratorActivity.CONFIG_PREFERENCES_OWN2_PHONE, "")))
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

    public void onBtnConfigJammCancelClick(View view) {
        super.finish();
    }

    public void onBtnConfigJammApplyClick(View view) {
        mDeviceSettings = getSharedPreferences(ConfiguratorActivity.CONFIG_PREFERENCES,
                Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = mDeviceSettings.edit();

        editor.putBoolean(ConfiguratorActivity.CONFIG_PREFERENCES_JAMM_ENABLE,
                mCheckBoxEnable.isChecked());

        editor.putBoolean(ConfiguratorActivity.CONFIG_PREFERENCES_JAMM_ACTION_HORN,
                mSwitchHorn.isChecked());

        editor.putBoolean(ConfiguratorActivity.CONFIG_PREFERENCES_JAMM_ACTION_BLOCK,
                mSwitchBlock.isChecked());

        editor.putBoolean(ConfiguratorActivity.CONFIG_PREFERENCES_JAMM_ALARM_PCN_SMS,
                mCheckBoxPcnSms.isChecked());
        editor.putBoolean(ConfiguratorActivity.CONFIG_PREFERENCES_JAMM_ALARM_PCN_CALL,
                mCheckBoxPcnCall.isChecked());

        editor.putBoolean(ConfiguratorActivity.CONFIG_PREFERENCES_JAMM_ALARM_OWN1_SMS,
                mCheckBoxOwn1Sms.isChecked());
        editor.putBoolean(ConfiguratorActivity.CONFIG_PREFERENCES_JAMM_ALARM_OWN1_CALL,
                mCheckBoxOwn1Call.isChecked());

        editor.putBoolean(ConfiguratorActivity.CONFIG_PREFERENCES_JAMM_ALARM_OWN2_SMS,
                mCheckBoxOwn2Sms.isChecked());
        editor.putBoolean(ConfiguratorActivity.CONFIG_PREFERENCES_JAMM_ALARM_OWN2_CALL,
                mCheckBoxOwn2Call.isChecked());

        editor.apply();
        super.finish();
    }
}
