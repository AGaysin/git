package ru.tehohrana.smsreceiver.config;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.RadioButton;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;

import java.util.regex.Pattern;

import ru.tehohrana.smsreceiver.ConfiguratorActivity;
import ru.tehohrana.smsreceiver.R;


public class ConfigSrv extends Activity {
    SharedPreferences mDeviceSettings;
    Switch mSwitchGuardHorn, mSwitchGuardBlock;

    CheckBox    mCheckBoxGuardPcnSms, mCheckBoxGuardPcnCall,
            mCheckBoxGuardOwn1Sms, mCheckBoxGuardOwn1Call,
            mCheckBoxGuardOwn2Sms, mCheckBoxGuardOwn2Call;

    CheckBox    mCheckBoxInitPcnSms, mCheckBoxInitPcnCall,
            mCheckBoxInitOwn1Sms, mCheckBoxInitOwn1Call,
            mCheckBoxInitOwn2Sms, mCheckBoxInitOwn2Call;

    TextView mTextViewGuardPcn,mTextViewGuardOwn1,mTextViewGuardOwn2,
            mTextViewInitPcn,mTextViewInitOwn1,mTextViewInitOwn2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_config_srv);

        mSwitchGuardHorn = (Switch) findViewById(R.id.switchConfigGuardHorn);
        mSwitchGuardBlock = (Switch) findViewById(R.id.switchConfigGuardBlock);

        mCheckBoxGuardPcnSms = (CheckBox) findViewById(R.id.checkBoxConfigGuardAlarmPcnSms);
        mCheckBoxGuardPcnCall = (CheckBox) findViewById(R.id.checkBoxConfigGuardAlarmPcnCall);
        mCheckBoxGuardOwn1Sms = (CheckBox) findViewById(R.id.checkBoxConfigGuardAlarmOwn1Sms);
        mCheckBoxGuardOwn1Call = (CheckBox) findViewById(R.id.checkBoxConfigGuardAlarmOwn1Call);
        mCheckBoxGuardOwn2Sms = (CheckBox) findViewById(R.id.checkBoxConfigGuardAlarmOwn2Sms);
        mCheckBoxGuardOwn2Call = (CheckBox) findViewById(R.id.checkBoxConfigGuardAlarmOwn2Call);

        mTextViewGuardPcn = (TextView) findViewById(R.id.textViewConfigGuardAlarmPcnLabel);
        mTextViewGuardOwn1 = (TextView) findViewById(R.id.textViewConfigGuardAlarmOwn1Label);
        mTextViewGuardOwn2 = (TextView) findViewById(R.id.textViewConfigGuardAlarmOwn2Label);

        mCheckBoxInitPcnSms = (CheckBox) findViewById(R.id.checkBoxConfigInitAlarmPcnSms);
        mCheckBoxInitPcnCall = (CheckBox) findViewById(R.id.checkBoxConfigInitAlarmPcnCall);
        mCheckBoxInitOwn1Sms = (CheckBox) findViewById(R.id.checkBoxConfigInitAlarmOwn1Sms);
        mCheckBoxInitOwn1Call = (CheckBox) findViewById(R.id.checkBoxConfigInitAlarmOwn1Call);
        mCheckBoxInitOwn2Sms = (CheckBox) findViewById(R.id.checkBoxConfigInitAlarmOwn2Sms);
        mCheckBoxInitOwn2Call = (CheckBox) findViewById(R.id.checkBoxConfigInitAlarmOwn2Call);

        mTextViewInitPcn = (TextView) findViewById(R.id.textViewConfigInitAlarmPcnLabel);
        mTextViewInitOwn1 = (TextView) findViewById(R.id.textViewConfigInitAlarmOwn1Label);
        mTextViewInitOwn2 = (TextView) findViewById(R.id.textViewConfigInitAlarmOwn2Label);

        //////  ЗАГРУЗКА ДАННЫХ
        //Чтение настроек из файла
        mDeviceSettings = getSharedPreferences(ConfiguratorActivity.CONFIG_PREFERENCES,
                Context.MODE_PRIVATE);
        //Horn
        if (mDeviceSettings.contains(ConfiguratorActivity.CONFIG_PREFERENCES_GUARD_ACTION_HORN))
            mSwitchGuardHorn.setChecked(mDeviceSettings.
                    getBoolean(ConfiguratorActivity.CONFIG_PREFERENCES_GUARD_ACTION_HORN, false));
        //Bock
        if (mDeviceSettings.contains(ConfiguratorActivity.CONFIG_PREFERENCES_GUARD_ACTION_BLOCK))
            mSwitchGuardBlock.setChecked(mDeviceSettings.
                    getBoolean(ConfiguratorActivity.CONFIG_PREFERENCES_GUARD_ACTION_BLOCK,false));

        //Alarm PCN SMS/CALL
        if (mDeviceSettings.contains(ConfiguratorActivity.CONFIG_PREFERENCES_GUARD_ALARM_PCN_SMS))
            mCheckBoxGuardPcnSms.setChecked(mDeviceSettings.
                    getBoolean(ConfiguratorActivity.CONFIG_PREFERENCES_GUARD_ALARM_PCN_SMS, false));
        if (mDeviceSettings.contains(ConfiguratorActivity.CONFIG_PREFERENCES_GUARD_ALARM_PCN_CALL))
            mCheckBoxGuardPcnCall.setChecked(mDeviceSettings.
                    getBoolean(ConfiguratorActivity.CONFIG_PREFERENCES_GUARD_ALARM_PCN_CALL, false));
        //Alarm OWN1 SMS/CALL
        if (mDeviceSettings.contains(ConfiguratorActivity.CONFIG_PREFERENCES_GUARD_ALARM_OWN1_SMS))
            mCheckBoxGuardOwn1Sms.setChecked(mDeviceSettings.
                    getBoolean(ConfiguratorActivity.CONFIG_PREFERENCES_GUARD_ALARM_OWN1_SMS, false));
        if (mDeviceSettings.contains(ConfiguratorActivity.CONFIG_PREFERENCES_GUARD_ALARM_OWN1_CALL))
            mCheckBoxGuardOwn1Call.setChecked(mDeviceSettings.
                    getBoolean(ConfiguratorActivity.CONFIG_PREFERENCES_GUARD_ALARM_OWN1_CALL, false));
        //Alarm OWN2 SMS/CALL
        if (mDeviceSettings.contains(ConfiguratorActivity.CONFIG_PREFERENCES_GUARD_ALARM_OWN2_SMS))
            mCheckBoxGuardOwn2Sms.setChecked(mDeviceSettings.
                    getBoolean(ConfiguratorActivity.CONFIG_PREFERENCES_GUARD_ALARM_OWN2_SMS, false));
        if (mDeviceSettings.contains(ConfiguratorActivity.CONFIG_PREFERENCES_GUARD_ALARM_OWN2_CALL))
            mCheckBoxGuardOwn2Call.setChecked(mDeviceSettings.
                    getBoolean(ConfiguratorActivity.CONFIG_PREFERENCES_GUARD_ALARM_OWN2_CALL, false));

        //Alarm PCN SMS/CALL
        if (mDeviceSettings.contains(ConfiguratorActivity.CONFIG_PREFERENCES_INIT_ALARM_PCN_SMS))
            mCheckBoxInitPcnSms.setChecked(mDeviceSettings.
                    getBoolean(ConfiguratorActivity.CONFIG_PREFERENCES_INIT_ALARM_PCN_SMS, false));
        if (mDeviceSettings.contains(ConfiguratorActivity.CONFIG_PREFERENCES_INIT_ALARM_PCN_CALL))
            mCheckBoxInitPcnCall.setChecked(mDeviceSettings.
                    getBoolean(ConfiguratorActivity.CONFIG_PREFERENCES_INIT_ALARM_PCN_CALL, false));
        //Alarm OWN1 SMS/CALL
        if (mDeviceSettings.contains(ConfiguratorActivity.CONFIG_PREFERENCES_INIT_ALARM_OWN1_SMS))
            mCheckBoxInitOwn1Sms.setChecked(mDeviceSettings.
                    getBoolean(ConfiguratorActivity.CONFIG_PREFERENCES_INIT_ALARM_OWN1_SMS, false));
        if (mDeviceSettings.contains(ConfiguratorActivity.CONFIG_PREFERENCES_INIT_ALARM_OWN1_CALL))
            mCheckBoxInitOwn1Call.setChecked(mDeviceSettings.
                    getBoolean(ConfiguratorActivity.CONFIG_PREFERENCES_INIT_ALARM_OWN1_CALL, false));
        //Alarm OWN2 SMS/CALL
        if (mDeviceSettings.contains(ConfiguratorActivity.CONFIG_PREFERENCES_INIT_ALARM_OWN2_SMS))
            mCheckBoxInitOwn2Sms.setChecked(mDeviceSettings.
                    getBoolean(ConfiguratorActivity.CONFIG_PREFERENCES_INIT_ALARM_OWN2_SMS, false));
        if (mDeviceSettings.contains(ConfiguratorActivity.CONFIG_PREFERENCES_INIT_ALARM_OWN2_CALL))
            mCheckBoxInitOwn2Call.setChecked(mDeviceSettings.
                    getBoolean(ConfiguratorActivity.CONFIG_PREFERENCES_INIT_ALARM_OWN2_CALL, false));


        //// ОБРАБОТКА НЕАКТИВНЫХ ПОЛЕЙ


        if (mDeviceSettings.contains(ConfiguratorActivity.CONFIG_PREFERENCES_PCN_PHONE))
        {
            if (Pattern.matches("[7][9]([0-9]{9})", mDeviceSettings.
                    getString(ConfiguratorActivity.CONFIG_PREFERENCES_PCN_PHONE, "")))
            {
                mTextViewGuardPcn.setEnabled(true);
                mCheckBoxGuardPcnSms.setEnabled(true);
                mCheckBoxGuardPcnCall.setEnabled(true);
                mTextViewInitPcn.setEnabled(true);
                mCheckBoxInitPcnSms.setEnabled(true);
                mCheckBoxInitPcnCall.setEnabled(true);
            } else {
                mTextViewGuardPcn.setEnabled(false);
                mCheckBoxGuardPcnSms.setEnabled(false);
                mCheckBoxGuardPcnCall.setEnabled(false);
                mTextViewInitPcn.setEnabled(false);
                mCheckBoxInitPcnSms.setEnabled(false);
                mCheckBoxInitPcnCall.setEnabled(false);

                mCheckBoxGuardPcnSms.setChecked(false);
                mCheckBoxGuardPcnCall.setChecked(false);
                mCheckBoxInitPcnSms.setChecked(false);
                mCheckBoxInitPcnCall.setChecked(false);
            }
        }
        if (mDeviceSettings.contains(ConfiguratorActivity.CONFIG_PREFERENCES_OWN1_PHONE))
        {
            if (Pattern.matches("[7][9]([0-9]{9})", mDeviceSettings.
                    getString(ConfiguratorActivity.CONFIG_PREFERENCES_OWN1_PHONE,"")))
            {
                mTextViewGuardOwn1.setEnabled(true);
                mCheckBoxGuardOwn1Sms.setEnabled(true);
                mCheckBoxGuardOwn1Call.setEnabled(true);
                mTextViewInitOwn1.setEnabled(true);
                mCheckBoxInitOwn1Sms.setEnabled(true);
                mCheckBoxInitOwn1Call.setEnabled(true);
            } else {
                mTextViewGuardOwn1.setEnabled(false);
                mCheckBoxGuardOwn1Sms.setEnabled(false);
                mCheckBoxGuardOwn1Call.setEnabled(false);
                mTextViewInitOwn1.setEnabled(false);
                mCheckBoxInitOwn1Sms.setEnabled(false);
                mCheckBoxInitOwn1Call.setEnabled(false);

                mCheckBoxGuardOwn1Sms.setChecked(false);
                mCheckBoxGuardOwn1Call.setChecked(false);
                mCheckBoxInitOwn1Sms.setChecked(false);
                mCheckBoxInitOwn1Call.setChecked(false);
            }
        }
        if (mDeviceSettings.contains(ConfiguratorActivity.CONFIG_PREFERENCES_OWN2_PHONE))
        {
            if (Pattern.matches("[7][9]([0-9]{9})", mDeviceSettings.
                    getString(ConfiguratorActivity.CONFIG_PREFERENCES_OWN2_PHONE,"")))
            {
                mTextViewGuardOwn2.setEnabled(true);
                mCheckBoxGuardOwn2Sms.setEnabled(true);
                mCheckBoxGuardOwn2Call.setEnabled(true);
                mTextViewInitOwn2.setEnabled(true);
                mCheckBoxInitOwn2Sms.setEnabled(true);
                mCheckBoxInitOwn2Call.setEnabled(true);
            } else {
                mTextViewGuardOwn2.setEnabled(false);
                mCheckBoxGuardOwn2Sms.setEnabled(false);
                mCheckBoxGuardOwn2Call.setEnabled(false);
                mTextViewInitOwn2.setEnabled(false);
                mCheckBoxInitOwn2Sms.setEnabled(false);
                mCheckBoxInitOwn2Call.setEnabled(false);

                mCheckBoxGuardOwn2Sms.setChecked(false);
                mCheckBoxGuardOwn2Call.setChecked(false);
                mCheckBoxInitOwn2Sms.setChecked(false);
                mCheckBoxInitOwn2Call.setChecked(false);
            }
        }



    }


    public void onBtnConfigServiceCancelClick(View view) {
        super.finish();
    }

    public void onBtnConfigServiceApplyClick(View view) {
        mDeviceSettings = getSharedPreferences(ConfiguratorActivity.CONFIG_PREFERENCES,
                Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = mDeviceSettings.edit();

        editor.putBoolean(ConfiguratorActivity.CONFIG_PREFERENCES_GUARD_ACTION_HORN,
                mSwitchGuardHorn.isChecked());

        editor.putBoolean(ConfiguratorActivity.CONFIG_PREFERENCES_GUARD_ACTION_BLOCK,
                mSwitchGuardBlock.isChecked());

        editor.putBoolean(ConfiguratorActivity.CONFIG_PREFERENCES_GUARD_ALARM_PCN_SMS,
                mCheckBoxGuardPcnSms.isChecked());
        editor.putBoolean(ConfiguratorActivity.CONFIG_PREFERENCES_GUARD_ALARM_PCN_CALL,
                mCheckBoxGuardPcnCall.isChecked());
        editor.putBoolean(ConfiguratorActivity.CONFIG_PREFERENCES_GUARD_ALARM_OWN1_SMS,
                mCheckBoxGuardOwn1Sms.isChecked());
        editor.putBoolean(ConfiguratorActivity.CONFIG_PREFERENCES_GUARD_ALARM_OWN1_CALL,
                mCheckBoxGuardOwn1Call.isChecked());
        editor.putBoolean(ConfiguratorActivity.CONFIG_PREFERENCES_GUARD_ALARM_OWN2_SMS,
                mCheckBoxGuardOwn2Sms.isChecked());
        editor.putBoolean(ConfiguratorActivity.CONFIG_PREFERENCES_GUARD_ALARM_OWN2_CALL,
                mCheckBoxGuardOwn2Call.isChecked());

        editor.putBoolean(ConfiguratorActivity.CONFIG_PREFERENCES_INIT_ALARM_PCN_SMS,
                mCheckBoxInitPcnSms.isChecked());
        editor.putBoolean(ConfiguratorActivity.CONFIG_PREFERENCES_INIT_ALARM_PCN_CALL,
                mCheckBoxInitPcnCall.isChecked());
        editor.putBoolean(ConfiguratorActivity.CONFIG_PREFERENCES_INIT_ALARM_OWN1_SMS,
                mCheckBoxInitOwn1Sms.isChecked());
        editor.putBoolean(ConfiguratorActivity.CONFIG_PREFERENCES_INIT_ALARM_OWN1_CALL,
                mCheckBoxInitOwn1Call.isChecked());
        editor.putBoolean(ConfiguratorActivity.CONFIG_PREFERENCES_INIT_ALARM_OWN2_SMS,
                mCheckBoxInitOwn2Sms.isChecked());
        editor.putBoolean(ConfiguratorActivity.CONFIG_PREFERENCES_INIT_ALARM_OWN2_CALL,
                mCheckBoxInitOwn2Call.isChecked());

        editor.apply();
        super.finish();
    }
}
