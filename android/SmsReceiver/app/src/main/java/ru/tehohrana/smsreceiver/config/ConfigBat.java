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

import org.w3c.dom.Text;

import java.util.regex.Pattern;

import ru.tehohrana.smsreceiver.ConfiguratorActivity;
import ru.tehohrana.smsreceiver.R;


public class ConfigBat extends Activity {
    SharedPreferences mDeviceSettings;

    RadioGroup mRadioGroup;
    RadioButton mRadioButton12V, mRadioButton24V;
    CheckBox mCheckBoxEnable;
    SeekBar mSeekBarLowBatLevel;

    int mLowBatLevelDefValue;

    Spinner mSpinnerLowBatTime;

    CheckBox    mCheckBoxNobatPcnSms, mCheckBoxNobatPcnCall,
                mCheckBoxNobatOwn1Sms, mCheckBoxNobatOwn1Call,
                mCheckBoxNobatOwn2Sms, mCheckBoxNobatOwn2Call;

    CheckBox    mCheckBoxLobatPcnSms, mCheckBoxLobatPcnCall,
                mCheckBoxLobatOwn1Sms, mCheckBoxLobatOwn1Call,
                mCheckBoxLobatOwn2Sms, mCheckBoxLobatOwn2Call;

    TextView mTextViewLobatLevelLabel, mTextViewLobatTimerLabel, mTextViewLobatAlarm,
            mTextViewLobatAlarmPcn,mTextViewLobatAlarmOwn1,mTextViewLobatAlarmOwn2,
            mTextViewNobatAlarm, mTextViewNobatAlarmPcn,mTextViewNobatAlarmOwn1,mTextViewNobatAlarmOwn2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_config_bat);

        mSpinnerLowBatTime = (Spinner) findViewById(R.id.spinnerConfigBatLowTime);
// Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.bat_time_array, android.R.layout.simple_spinner_item);
// Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
// Apply the adapter to the spinner
        mSpinnerLowBatTime.setAdapter(adapter);

        mRadioGroup = (RadioGroup) findViewById(R.id.radioGroupConfigBat);
        mRadioButton12V = (RadioButton) findViewById(R.id.radioButtonConfigBatLevel12V);
        mRadioButton24V = (RadioButton) findViewById(R.id.radioButtonConfigBatLevel24V);
        mCheckBoxEnable = (CheckBox) findViewById(R.id.checkBoxConfigBatLowEnable);
        mTextViewLobatLevelLabel = (TextView) findViewById(R.id.textViewConfigBatLowLevelLabel);
        mTextViewLobatTimerLabel = (TextView) findViewById(R.id.textViewConfigBatLowTimeLabel);
        mSeekBarLowBatLevel = (SeekBar)findViewById(R.id.seekBarConfigBatLowLevel);

        mSpinnerLowBatTime = (Spinner)findViewById(R.id.spinnerConfigBatLowTime);
        mTextViewLobatAlarm = (TextView) findViewById(R.id.textViewConfigBatLowAlarmLabel);
        mTextViewLobatAlarmPcn = (TextView) findViewById(R.id.textViewConfigBatLowAlarmPcnLabel);
        mTextViewLobatAlarmOwn1 = (TextView) findViewById(R.id.textViewConfigBatLowAlarmOwn1Label);
        mTextViewLobatAlarmOwn2 = (TextView) findViewById(R.id.textViewConfigBatLowAlarmOwn2Label);
        mCheckBoxLobatPcnSms = (CheckBox) findViewById(R.id.checkBoxConfigBatLowAlarmPcnSms);
        mCheckBoxLobatPcnCall = (CheckBox) findViewById(R.id.checkBoxConfigBatLowAlarmPcnCall);
        mCheckBoxLobatOwn1Sms = (CheckBox) findViewById(R.id.checkBoxConfigBatLowAlarmOwn1Sms);
        mCheckBoxLobatOwn1Call = (CheckBox) findViewById(R.id.checkBoxConfigBatLowAlarmOwn1Call);
        mCheckBoxLobatOwn2Sms = (CheckBox) findViewById(R.id.checkBoxConfigBatLowAlarmOwn2Sms);
        mCheckBoxLobatOwn2Call = (CheckBox) findViewById(R.id.checkBoxConfigBatLowAlarmOwn2Call);

        mTextViewNobatAlarm = (TextView) findViewById(R.id.textViewConfigBatNoAlarmLabel);
        mTextViewNobatAlarmPcn = (TextView) findViewById(R.id.textViewConfigBatNoAlarmPcnLabel);
        mTextViewNobatAlarmOwn1 = (TextView) findViewById(R.id.textViewConfigBatNoAlarmOwn1Label);
        mTextViewNobatAlarmOwn2 = (TextView) findViewById(R.id.textViewConfigBatNoAlarmOwn2Label);
        mCheckBoxNobatPcnSms = (CheckBox) findViewById(R.id.checkBoxConfigBatNoAlarmPcnSms);
        mCheckBoxNobatPcnCall = (CheckBox) findViewById(R.id.checkBoxConfigBatNoAlarmPcnCall);
        mCheckBoxNobatOwn1Sms = (CheckBox) findViewById(R.id.checkBoxConfigBatNoAlarmOwn1Sms);
        mCheckBoxNobatOwn1Call = (CheckBox) findViewById(R.id.checkBoxConfigBatNoAlarmOwn1Call);
        mCheckBoxNobatOwn2Sms = (CheckBox) findViewById(R.id.checkBoxConfigBatNoAlarmOwn2Sms);
        mCheckBoxNobatOwn2Call = (CheckBox) findViewById(R.id.checkBoxConfigBatNoAlarmOwn2Call);



        mRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (mRadioButton24V.isChecked()) mSeekBarLowBatLevel.setMax(240);
                else mSeekBarLowBatLevel.setMax(120);
                mTextViewLobatLevelLabel.setText("Порогновое значение " +
                        mSeekBarLowBatLevel.getProgress()/10 + "," +
                        mSeekBarLowBatLevel.getProgress()%10 + " В");
            }
        });

        mSeekBarLowBatLevel.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // TODO Auto-generated method stub
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // TODO Auto-generated method stub
            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                // TODO Auto-generated method stub

                mTextViewLobatLevelLabel.setText("Порогновое значение " +
                        mSeekBarLowBatLevel.getProgress()/10 + "," +
                        mSeekBarLowBatLevel.getProgress()%10 + " В");

            }
        });

        //////  ЗАГРУЗКА ДАННЫХ
        //Чтение настроек из файла
        mDeviceSettings = getSharedPreferences(ConfiguratorActivity.CONFIG_PREFERENCES,
                Context.MODE_PRIVATE);
        //24V
        if (mDeviceSettings.contains(ConfiguratorActivity.CONFIG_PREFERENCES_PWR_TYPE)) {
            if(mDeviceSettings.getBoolean(ConfiguratorActivity.CONFIG_PREFERENCES_PWR_TYPE, false))
                mRadioButton24V.setChecked(true);
            else mRadioButton12V.setChecked(true);
        }
        //Enable
        if (mDeviceSettings.contains(ConfiguratorActivity.CONFIG_PREFERENCES_PWR_LOBAT_ENABLE))
            mCheckBoxEnable.setChecked(mDeviceSettings.getBoolean(
                    ConfiguratorActivity.CONFIG_PREFERENCES_PWR_LOBAT_ENABLE, false));
        //Time
        if (mDeviceSettings.contains(ConfiguratorActivity.CONFIG_PREFERENCES_PWR_LOBAT_TIME)) {
            mSpinnerLowBatTime.setSelection(mDeviceSettings.getInt(
                    ConfiguratorActivity.CONFIG_PREFERENCES_PWR_LOBAT_TIME, 0));
            adapter.notifyDataSetChanged();
        }
        //Level
        if (mDeviceSettings.contains(ConfiguratorActivity.CONFIG_PREFERENCES_PWR_LOBAT_LEVEL))
            mSeekBarLowBatLevel.setProgress(mDeviceSettings.
                    getInt(ConfiguratorActivity.CONFIG_PREFERENCES_PWR_LOBAT_LEVEL, 0));

        //Alarm LOBAT PCN SMS/CALL
        if (mDeviceSettings.contains(ConfiguratorActivity.CONFIG_PREFERENCES_PWR_LOBAT_ALARM_PCN_SMS))
            mCheckBoxLobatPcnSms.setChecked(mDeviceSettings.
                    getBoolean(ConfiguratorActivity.CONFIG_PREFERENCES_PWR_LOBAT_ALARM_PCN_SMS, false));
        if (mDeviceSettings.contains(ConfiguratorActivity.CONFIG_PREFERENCES_PWR_LOBAT_ALARM_PCN_CALL))
            mCheckBoxLobatPcnCall.setChecked(mDeviceSettings.
                    getBoolean(ConfiguratorActivity.CONFIG_PREFERENCES_PWR_LOBAT_ALARM_PCN_CALL, false));
        //Alarm LOBAT OWN1 SMS/CALL
        if (mDeviceSettings.contains(ConfiguratorActivity.CONFIG_PREFERENCES_PWR_LOBAT_ALARM_OWN1_SMS))
            mCheckBoxLobatOwn1Sms.setChecked(mDeviceSettings.
                    getBoolean(ConfiguratorActivity.CONFIG_PREFERENCES_PWR_LOBAT_ALARM_OWN1_SMS, false));
        if (mDeviceSettings.contains(ConfiguratorActivity.CONFIG_PREFERENCES_PWR_LOBAT_ALARM_OWN1_CALL))
            mCheckBoxLobatOwn1Call.setChecked(mDeviceSettings.
                    getBoolean(ConfiguratorActivity.CONFIG_PREFERENCES_PWR_LOBAT_ALARM_OWN1_CALL, false));
        //Alarm LOBAT OWN2 SMS/CALL
        if (mDeviceSettings.contains(ConfiguratorActivity.CONFIG_PREFERENCES_PWR_LOBAT_ALARM_OWN2_SMS))
            mCheckBoxLobatOwn2Sms.setChecked(mDeviceSettings.
                    getBoolean(ConfiguratorActivity.CONFIG_PREFERENCES_PWR_LOBAT_ALARM_OWN2_SMS, false));
        if (mDeviceSettings.contains(ConfiguratorActivity.CONFIG_PREFERENCES_PWR_LOBAT_ALARM_OWN2_CALL))
            mCheckBoxLobatOwn2Call.setChecked(mDeviceSettings.
                    getBoolean(ConfiguratorActivity.CONFIG_PREFERENCES_PWR_LOBAT_ALARM_OWN2_CALL, false));

        //Alarm NOBAT PCN SMS/CALL
        if (mDeviceSettings.contains(ConfiguratorActivity.CONFIG_PREFERENCES_PWR_NOBAT_ALARM_PCN_SMS))
            mCheckBoxNobatPcnSms.setChecked(mDeviceSettings.
                    getBoolean(ConfiguratorActivity.CONFIG_PREFERENCES_PWR_NOBAT_ALARM_PCN_SMS, false));
        if (mDeviceSettings.contains(ConfiguratorActivity.CONFIG_PREFERENCES_PWR_NOBAT_ALARM_PCN_CALL))
            mCheckBoxNobatPcnCall.setChecked(mDeviceSettings.
                    getBoolean(ConfiguratorActivity.CONFIG_PREFERENCES_PWR_NOBAT_ALARM_PCN_CALL, false));
        //Alarm NOBAT OWN1 SMS/CALL
        if (mDeviceSettings.contains(ConfiguratorActivity.CONFIG_PREFERENCES_PWR_NOBAT_ALARM_OWN1_SMS))
            mCheckBoxNobatOwn1Sms.setChecked(mDeviceSettings.
                    getBoolean(ConfiguratorActivity.CONFIG_PREFERENCES_PWR_NOBAT_ALARM_OWN1_SMS, false));
        if (mDeviceSettings.contains(ConfiguratorActivity.CONFIG_PREFERENCES_PWR_NOBAT_ALARM_OWN1_CALL))
            mCheckBoxNobatOwn1Call.setChecked(mDeviceSettings.
                    getBoolean(ConfiguratorActivity.CONFIG_PREFERENCES_PWR_NOBAT_ALARM_OWN1_CALL, false));
        //Alarm NOBAT OWN2 SMS/CALL
        if (mDeviceSettings.contains(ConfiguratorActivity.CONFIG_PREFERENCES_PWR_NOBAT_ALARM_OWN2_SMS))
            mCheckBoxNobatOwn2Sms.setChecked(mDeviceSettings.
                    getBoolean(ConfiguratorActivity.CONFIG_PREFERENCES_PWR_NOBAT_ALARM_OWN2_SMS, false));
        if (mDeviceSettings.contains(ConfiguratorActivity.CONFIG_PREFERENCES_PWR_NOBAT_ALARM_OWN2_CALL))
            mCheckBoxNobatOwn2Call.setChecked(mDeviceSettings.
                    getBoolean(ConfiguratorActivity.CONFIG_PREFERENCES_PWR_NOBAT_ALARM_OWN2_CALL, false));

        //// ОБРАБОТКА НЕАКТИВНЫХ ПОЛЕЙ
        boolean value = mCheckBoxEnable.isChecked();
        mTextViewLobatLevelLabel.setEnabled(value);
        mTextViewLobatTimerLabel.setEnabled(value);
        mSeekBarLowBatLevel.setEnabled(value);
        mSpinnerLowBatTime.setEnabled(value);
        mTextViewLobatAlarm.setEnabled(value);
        mTextViewLobatAlarmPcn.setEnabled(value);
        mTextViewLobatAlarmOwn1.setEnabled(value);
        mTextViewLobatAlarmOwn2.setEnabled(value);
        mCheckBoxLobatPcnSms.setEnabled(value);
        mCheckBoxLobatPcnCall.setEnabled(value);
        mCheckBoxLobatOwn1Sms.setEnabled(value);
        mCheckBoxLobatOwn1Call.setEnabled(value);
        mCheckBoxLobatOwn2Sms.setEnabled(value);
        mCheckBoxLobatOwn2Call.setEnabled(value);
        mTextViewLobatAlarm.setEnabled(value);
        if (value) updateAlarmLobatCheckBox();
        updateAlarmNobatCheckBox();

        if (mRadioButton24V.isChecked()) mSeekBarLowBatLevel.setMax(240);
        else mSeekBarLowBatLevel.setMax(120);
        mTextViewLobatLevelLabel.setText("Порогновое значение " +
                mSeekBarLowBatLevel.getProgress()/10 + "," +
                mSeekBarLowBatLevel.getProgress()%10 + " В");



        //// ОБРАБОТКА НАЖАТИЙ
        mCheckBoxEnable.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean value = mCheckBoxEnable.isChecked();
                mTextViewLobatLevelLabel.setEnabled(value);
                mTextViewLobatTimerLabel.setEnabled(value);
                mSeekBarLowBatLevel.setEnabled(value);
                mSpinnerLowBatTime.setEnabled(value);
                mTextViewLobatAlarm.setEnabled(value);
                mTextViewLobatAlarmPcn.setEnabled(value);
                mTextViewLobatAlarmOwn1.setEnabled(value);
                mTextViewLobatAlarmOwn2.setEnabled(value);
                mCheckBoxLobatPcnSms.setEnabled(value);
                mCheckBoxLobatPcnCall.setEnabled(value);
                mCheckBoxLobatOwn1Sms.setEnabled(value);
                mCheckBoxLobatOwn1Call.setEnabled(value);
                mCheckBoxLobatOwn2Sms.setEnabled(value);
                mCheckBoxLobatOwn2Call.setEnabled(value);
                mTextViewLobatAlarm.setEnabled(value);
                if (value) updateAlarmLobatCheckBox();
                updateAlarmNobatCheckBox();
            }
        });




    }
    public void updateAlarmLobatCheckBox()
    {

        if (mDeviceSettings.contains(ConfiguratorActivity.CONFIG_PREFERENCES_PCN_PHONE))
        {
            if (Pattern.matches("[7][9]([0-9]{9})", mDeviceSettings.
                    getString(ConfiguratorActivity.CONFIG_PREFERENCES_PCN_PHONE, "")))
            {
                mTextViewLobatAlarmPcn.setEnabled(true);
                mCheckBoxLobatPcnSms.setEnabled(true);
                mCheckBoxLobatPcnCall.setEnabled(true);
            } else {
                mTextViewLobatAlarmPcn.setEnabled(false);
                mCheckBoxLobatPcnSms.setEnabled(false);
                mCheckBoxLobatPcnCall.setEnabled(false);
                mCheckBoxLobatPcnSms.setChecked(false);
                mCheckBoxLobatPcnCall.setChecked(false);
            }
        }
        if (mDeviceSettings.contains(ConfiguratorActivity.CONFIG_PREFERENCES_OWN1_PHONE))
        {
            if (Pattern.matches("[7][9]([0-9]{9})", mDeviceSettings.
                    getString(ConfiguratorActivity.CONFIG_PREFERENCES_OWN1_PHONE,"")))
            {
                mTextViewLobatAlarmOwn1.setEnabled(true);
                mCheckBoxLobatOwn1Sms.setEnabled(true);
                mCheckBoxLobatOwn1Call.setEnabled(true);
            } else {
                mTextViewLobatAlarmOwn1.setEnabled(false);
                mCheckBoxLobatOwn1Sms.setEnabled(false);
                mCheckBoxLobatOwn1Call.setEnabled(false);
                mCheckBoxLobatOwn1Sms.setChecked(false);
                mCheckBoxLobatOwn1Call.setChecked(false);
            }
        }
        if (mDeviceSettings.contains(ConfiguratorActivity.CONFIG_PREFERENCES_OWN2_PHONE)) {
            if (Pattern.matches("[7][9]([0-9]{9})", mDeviceSettings.
                    getString(ConfiguratorActivity.CONFIG_PREFERENCES_OWN2_PHONE,"")))
            {
                mTextViewLobatAlarmOwn2.setEnabled(true);
                mCheckBoxLobatOwn2Sms.setEnabled(true);
                mCheckBoxLobatOwn2Call.setEnabled(true);
            } else {
                mTextViewLobatAlarmOwn2.setEnabled(false);
                mCheckBoxLobatOwn2Sms.setEnabled(false);
                mCheckBoxLobatOwn2Call.setEnabled(false);
                mCheckBoxLobatOwn2Sms.setChecked(false);
                mCheckBoxLobatOwn2Call.setChecked(false);
            }
        }
    }

    public void updateAlarmNobatCheckBox()
    {

        if (mDeviceSettings.contains(ConfiguratorActivity.CONFIG_PREFERENCES_PCN_PHONE)) {
            if (Pattern.matches("[7][9]([0-9]{9})", mDeviceSettings.
                    getString(ConfiguratorActivity.CONFIG_PREFERENCES_PCN_PHONE, "")))
            {
                mTextViewNobatAlarmPcn.setEnabled(true);
                mCheckBoxNobatPcnSms.setEnabled(true);
                mCheckBoxNobatPcnCall.setEnabled(true);
            } else {
                mTextViewNobatAlarmPcn.setEnabled(false);
                mCheckBoxNobatPcnSms.setEnabled(false);
                mCheckBoxNobatPcnCall.setEnabled(false);
                mCheckBoxNobatPcnSms.setChecked(false);
                mCheckBoxNobatPcnCall.setChecked(false);
            }
        }
        if (mDeviceSettings.contains(ConfiguratorActivity.CONFIG_PREFERENCES_OWN1_PHONE)) {
            if (Pattern.matches("[7][9]([0-9]{9})", mDeviceSettings.
                    getString(ConfiguratorActivity.CONFIG_PREFERENCES_OWN1_PHONE,"")))
            {
                mTextViewNobatAlarmOwn1.setEnabled(true);
                mCheckBoxNobatOwn1Sms.setEnabled(true);
                mCheckBoxNobatOwn1Call.setEnabled(true);
            } else {
                mTextViewNobatAlarmOwn1.setEnabled(false);
                mCheckBoxNobatOwn1Sms.setEnabled(false);
                mCheckBoxNobatOwn1Call.setEnabled(false);
                mCheckBoxNobatOwn1Sms.setChecked(false);
                mCheckBoxNobatOwn1Call.setChecked(false);
            }
        }
        if (mDeviceSettings.contains(ConfiguratorActivity.CONFIG_PREFERENCES_OWN2_PHONE)) {
            if (Pattern.matches("[7][9]([0-9]{9})", mDeviceSettings.
                    getString(ConfiguratorActivity.CONFIG_PREFERENCES_OWN2_PHONE,"")))
            {
                mTextViewNobatAlarmOwn2.setEnabled(true);
                mCheckBoxNobatOwn2Sms.setEnabled(true);
                mCheckBoxNobatOwn2Call.setEnabled(true);
            } else {
                mTextViewNobatAlarmOwn2.setEnabled(false);
                mCheckBoxNobatOwn2Sms.setEnabled(false);
                mCheckBoxNobatOwn2Call.setEnabled(false);
                mCheckBoxNobatOwn2Sms.setChecked(false);
                mCheckBoxNobatOwn2Call.setChecked(false);
            }
        }
    }


    public void onBtnConfigBatApplyClick(View view) {
        mDeviceSettings = getSharedPreferences(ConfiguratorActivity.CONFIG_PREFERENCES,
                Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = mDeviceSettings.edit();

        editor.putBoolean(ConfiguratorActivity.CONFIG_PREFERENCES_PWR_TYPE,
                mRadioButton24V.isChecked());

        editor.putBoolean(ConfiguratorActivity.CONFIG_PREFERENCES_PWR_LOBAT_ENABLE,
                mCheckBoxEnable.isChecked());

        editor.putInt(ConfiguratorActivity.CONFIG_PREFERENCES_PWR_LOBAT_LEVEL,
                mSeekBarLowBatLevel.getProgress());

        editor.putInt(ConfiguratorActivity.CONFIG_PREFERENCES_PWR_LOBAT_TIME,
                mSpinnerLowBatTime.getSelectedItemPosition());


        editor.putBoolean(ConfiguratorActivity.CONFIG_PREFERENCES_PWR_LOBAT_ALARM_PCN_SMS,
                mCheckBoxLobatPcnSms.isChecked());
        editor.putBoolean(ConfiguratorActivity.CONFIG_PREFERENCES_PWR_LOBAT_ALARM_PCN_CALL,
                mCheckBoxLobatPcnCall.isChecked());

        editor.putBoolean(ConfiguratorActivity.CONFIG_PREFERENCES_PWR_LOBAT_ALARM_OWN1_SMS,
                mCheckBoxLobatOwn1Sms.isChecked());
        editor.putBoolean(ConfiguratorActivity.CONFIG_PREFERENCES_PWR_LOBAT_ALARM_OWN1_CALL,
                mCheckBoxLobatOwn1Call.isChecked());

        editor.putBoolean(ConfiguratorActivity.CONFIG_PREFERENCES_PWR_LOBAT_ALARM_OWN2_SMS,
                mCheckBoxLobatOwn2Sms.isChecked());
        editor.putBoolean(ConfiguratorActivity.CONFIG_PREFERENCES_PWR_LOBAT_ALARM_OWN2_CALL,
                mCheckBoxLobatOwn2Call.isChecked());

        editor.putBoolean(ConfiguratorActivity.CONFIG_PREFERENCES_PWR_NOBAT_ALARM_PCN_SMS,
                mCheckBoxNobatPcnSms.isChecked());
        editor.putBoolean(ConfiguratorActivity.CONFIG_PREFERENCES_PWR_NOBAT_ALARM_PCN_CALL,
                mCheckBoxNobatPcnCall.isChecked());

        editor.putBoolean(ConfiguratorActivity.CONFIG_PREFERENCES_PWR_NOBAT_ALARM_OWN1_SMS,
                mCheckBoxNobatOwn1Sms.isChecked());
        editor.putBoolean(ConfiguratorActivity.CONFIG_PREFERENCES_PWR_NOBAT_ALARM_OWN1_CALL,
                mCheckBoxNobatOwn1Call.isChecked());

        editor.putBoolean(ConfiguratorActivity.CONFIG_PREFERENCES_PWR_NOBAT_ALARM_OWN2_SMS,
                mCheckBoxNobatOwn2Sms.isChecked());
        editor.putBoolean(ConfiguratorActivity.CONFIG_PREFERENCES_PWR_NOBAT_ALARM_OWN2_CALL,
                mCheckBoxNobatOwn2Call.isChecked());

        editor.apply();
        super.finish();
    }

    public void onBtnConfigBatCancelClick(View view) {
        super.finish();
    }
}

