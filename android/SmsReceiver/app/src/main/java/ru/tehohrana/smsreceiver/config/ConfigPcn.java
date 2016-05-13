package ru.tehohrana.smsreceiver.config;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.util.regex.Pattern;

import ru.tehohrana.smsreceiver.ConfiguratorActivity;
import ru.tehohrana.smsreceiver.R;

/**
 * Created by AG on 03.12.2015.
 */

public class ConfigPcn extends Activity{
    SharedPreferences mDeviceSettings;
    private EditText mEditTextPcnPhone;
    private CheckBox mCheckBoxTestSmsEnable;
    private EditText mEditTextTestSmsPhone;
    private Spinner mSpinnerTestSmsInterval;
    private CheckBox mCheckBoxTestCallEnable;
    private EditText mEditTextTestCallPhone;
    private Spinner mSpinnerTestCallInterval;
    private TextView    mTextViewConfigTestSmsPhoneLabel,
                        mTextViewConfigTestSmsIntervalLabel,
                        mTextViewConfigTestCallPhoneLabel,
                        mTextViewConfigTestCallIntervalLabel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_config_pcn);

        mTextViewConfigTestSmsPhoneLabel = (TextView) findViewById(R.id.textViewConfigTestSmsPhoneLabel);
        mTextViewConfigTestSmsIntervalLabel = (TextView) findViewById(R.id.textViewConfigTestSmsIntervalLabel);
        mTextViewConfigTestCallPhoneLabel = (TextView) findViewById(R.id.textViewConfigTestCallPhoneLabel);
        mTextViewConfigTestCallIntervalLabel = (TextView) findViewById(R.id.textViewConfigTestCallIntervalLabel);


        mSpinnerTestSmsInterval = (Spinner) findViewById(R.id.spinnerConfigTestSmsInterval);
// Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapterTestSmsInterval = ArrayAdapter.createFromResource(this,
                R.array.pcn_sms_time_array, android.R.layout.simple_spinner_item);
// Specify the layout to use when the list of choices appears
        adapterTestSmsInterval.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
// Apply the adapter to the spinner
        mSpinnerTestSmsInterval.setAdapter(adapterTestSmsInterval);

        mSpinnerTestCallInterval = (Spinner) findViewById(R.id.spinnerConfigTestCallInterval);
// Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapterTestCallInterval = ArrayAdapter.createFromResource(this,
                R.array.pcn_call_time_array, android.R.layout.simple_spinner_item);
// Specify the layout to use when the list of choices appears
        adapterTestCallInterval.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
// Apply the adapter to the spinner
        mSpinnerTestCallInterval.setAdapter(adapterTestCallInterval);


        //Чтение настроек из файла
        mDeviceSettings = getSharedPreferences(ConfiguratorActivity.CONFIG_PREFERENCES,
                Context.MODE_PRIVATE);

        //Номер телефона ПЦН
        mEditTextPcnPhone = (EditText) findViewById(R.id.editTextConfigPcnPhone);
        if (mDeviceSettings.contains(ConfiguratorActivity.CONFIG_PREFERENCES_PCN_PHONE))
            mEditTextPcnPhone.setText(mDeviceSettings.getString(
                    ConfiguratorActivity.CONFIG_PREFERENCES_PCN_PHONE,"70000000000").toString());
        if (mEditTextPcnPhone.getText().length()==0) ((ImageView) findViewById(R.id.imageViewConfigPcnPhoneOk))
                .setImageBitmap(null);
        else if (Pattern.matches("[7]([0-9]{10})", mEditTextPcnPhone.getText().toString()))
            ((ImageView) findViewById(R.id.imageViewConfigPcnPhoneOk))
                    .setImageResource(R.drawable.ic_action_cancel);
        else ((ImageView) findViewById(R.id.imageViewConfigPcnPhoneOk))
                .setImageResource(R.drawable.ic_action_cancel);
        mEditTextPcnPhone.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length()==0)
                    ((ImageView) findViewById(R.id.imageViewConfigPcnPhoneOk))
                            .setImageBitmap(null);
                else if (Pattern.matches("[7]([0-9]{10})", s.toString()))
                    ((ImageView) findViewById(R.id.imageViewConfigPcnPhoneOk))
                            .setImageResource(R.drawable.ic_action_cancel);
                else ((ImageView) findViewById(R.id.imageViewConfigPcnPhoneOk))
                        .setImageResource(R.drawable.ic_action_cancel);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });



        //Номер телефона тестовых SMS
        mEditTextTestSmsPhone = (EditText) findViewById(R.id.editTextConfigTestSmsPhone);
        if (mDeviceSettings.contains(ConfiguratorActivity.CONFIG_PREFERENCES_TESTSMS_PHONE))
            mEditTextTestSmsPhone.setText(mDeviceSettings.getString(
                    ConfiguratorActivity.CONFIG_PREFERENCES_TESTSMS_PHONE,"70000000000").toString());
        if (mEditTextTestSmsPhone.getText().length()==0) ((ImageView) findViewById(R.id.imageViewConfigTestSmsPhoneOk))
                .setImageBitmap(null);
        else if (Pattern.matches("[7]([0-9]{10})", mEditTextTestSmsPhone.getText().toString()))
            ((ImageView) findViewById(R.id.imageViewConfigTestSmsPhoneOk))
                    .setImageResource(R.drawable.ic_action_accept);

        else ((ImageView) findViewById(R.id.imageViewConfigTestSmsPhoneOk))
                .setImageResource(R.drawable.ic_action_cancel);

        mEditTextTestSmsPhone.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length()==0) ((ImageView) findViewById(R.id.imageViewConfigTestSmsPhoneOk))
                        .setImageBitmap(null);
                else if (Pattern.matches("[7]([0-9]{10})", s.toString()))
                    ((ImageView) findViewById(R.id.imageViewConfigTestSmsPhoneOk))
                            .setImageResource(R.drawable.ic_action_accept);
                else ((ImageView) findViewById(R.id.imageViewConfigTestSmsPhoneOk))
                        .setImageResource(R.drawable.ic_action_cancel);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        //Номер телефона тестовых CALL
        mEditTextTestCallPhone = (EditText) findViewById(R.id.editTextConfigTestCallPhone);
        if (mDeviceSettings.contains(ConfiguratorActivity.CONFIG_PREFERENCES_TESTCALL_PHONE))
            mEditTextTestCallPhone.setText(mDeviceSettings.getString(
                    ConfiguratorActivity.CONFIG_PREFERENCES_TESTCALL_PHONE,"70000000000")
                    .toString());

        if (mEditTextTestCallPhone.getText().length()==0)
            ((ImageView) findViewById(R.id.imageViewConfigTestCallPhoneOk))
                .setImageBitmap(null);
        else if (Pattern.matches("[7]([0-9]{10})", mEditTextTestCallPhone.getText().toString()))
            ((ImageView) findViewById(R.id.imageViewConfigTestCallPhoneOk))
                    .setImageResource(R.drawable.ic_action_accept);
        else
            ((ImageView) findViewById(R.id.imageViewConfigTestCallPhoneOk))
                    .setImageResource(R.drawable.ic_action_cancel);

        mEditTextTestCallPhone.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length()==0) ((ImageView) findViewById(R.id.imageViewConfigTestCallPhoneOk))
                        .setImageBitmap(null);
                else if (Pattern.matches("[7]([0-9]{10})", s.toString()) || s.length()==0)
                    ((ImageView) findViewById(R.id.imageViewConfigTestCallPhoneOk))
                            .setImageResource(R.drawable.ic_action_accept);
                else
                    ((ImageView) findViewById(R.id.imageViewConfigTestCallPhoneOk))
                            .setImageResource(R.drawable.ic_action_cancel);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });


        //CheckBox SMS TEST ENABLE
        mCheckBoxTestSmsEnable = (CheckBox) findViewById(R.id.checkBoxConfigTestSmsEnable);
        if (mDeviceSettings.contains(ConfiguratorActivity.CONFIG_PREFERENCES_TESTSMS_ENABLE))
        {
            mCheckBoxTestSmsEnable.setChecked(mDeviceSettings.getBoolean(
                    ConfiguratorActivity.CONFIG_PREFERENCES_TESTSMS_ENABLE, false));
        }

        boolean value = mCheckBoxTestSmsEnable.isChecked();
        mTextViewConfigTestSmsPhoneLabel.setEnabled(value);
        mEditTextTestSmsPhone.setEnabled(value);
        mTextViewConfigTestSmsIntervalLabel.setEnabled(value);
        mSpinnerTestSmsInterval.setEnabled(value);
        if (!value) ((ImageView) findViewById(R.id.imageViewConfigTestSmsPhoneOk))
                .setImageBitmap(null);

        //CheckBox CALL TEST ENABLE
        mCheckBoxTestCallEnable = (CheckBox) findViewById(R.id.checkBoxConfigTestCallEnable);
        if (mDeviceSettings.contains(ConfiguratorActivity.CONFIG_PREFERENCES_TESTCALL_ENABLE))
        {
            mCheckBoxTestCallEnable.setChecked(mDeviceSettings.getBoolean(
                    ConfiguratorActivity.CONFIG_PREFERENCES_TESTCALL_ENABLE, false));
        }

        value = mCheckBoxTestCallEnable.isChecked();
        mTextViewConfigTestCallPhoneLabel.setEnabled(value);
        mEditTextTestCallPhone.setEnabled(value);
        mTextViewConfigTestCallIntervalLabel.setEnabled(value);
        mSpinnerTestCallInterval.setEnabled(value);
        if (!value) ((ImageView) findViewById(R.id.imageViewConfigTestCallPhoneOk))
                .setImageBitmap(null);

        //Интервал посылки тестовых сообщений
        if (mDeviceSettings.contains(ConfiguratorActivity.CONFIG_PREFERENCES_TESTSMS_INTERVAL)) {
            mSpinnerTestSmsInterval.setSelection(mDeviceSettings.getInt(
                    ConfiguratorActivity.CONFIG_PREFERENCES_TESTSMS_INTERVAL, 0));
            adapterTestSmsInterval.notifyDataSetChanged();
        }

        //Интервал посылки CALL
        if (mDeviceSettings.contains(ConfiguratorActivity.CONFIG_PREFERENCES_TESTCALL_INTERVAL)) {
            mSpinnerTestCallInterval.setSelection(mDeviceSettings.getInt(
                    ConfiguratorActivity.CONFIG_PREFERENCES_TESTCALL_INTERVAL, 0));
            adapterTestCallInterval.notifyDataSetChanged();
        }




    }

    public void onBtnConfigPcnApplyClick(View view) {

        //Записываем данные в файл настроек
        mDeviceSettings = getSharedPreferences(ConfiguratorActivity.CONFIG_PREFERENCES,
                Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = mDeviceSettings.edit();

        editor.putString(ConfiguratorActivity.CONFIG_PREFERENCES_PCN_PHONE,
                mEditTextPcnPhone.getText().toString());
        editor.putString(ConfiguratorActivity.CONFIG_PREFERENCES_TESTSMS_PHONE,
                mEditTextTestSmsPhone.getText().toString());
        editor.putString(ConfiguratorActivity.CONFIG_PREFERENCES_TESTCALL_PHONE,
                mEditTextTestCallPhone.getText().toString());


        editor.putBoolean(ConfiguratorActivity.CONFIG_PREFERENCES_TESTSMS_ENABLE,
                mCheckBoxTestSmsEnable.isChecked());
        editor.putBoolean(ConfiguratorActivity.CONFIG_PREFERENCES_TESTCALL_ENABLE,
                mCheckBoxTestCallEnable.isChecked());

        editor.putInt(ConfiguratorActivity.CONFIG_PREFERENCES_TESTSMS_INTERVAL,
                mSpinnerTestSmsInterval.getSelectedItemPosition());
        editor.putInt(ConfiguratorActivity.CONFIG_PREFERENCES_TESTCALL_INTERVAL,
                mSpinnerTestCallInterval.getSelectedItemPosition());

        editor.apply();
        super.finish();
    }

    public void onBtnConfigPcnNextClick(View view) {
    }

    public void onBtnConfigPcnCancelClick(View view) {
        super.finish();
    }

    public void onCheckBoxTestSmsEnableChange(View view) {
        boolean value = mCheckBoxTestSmsEnable.isChecked();
        mTextViewConfigTestSmsPhoneLabel.setEnabled(value);
        mEditTextTestSmsPhone.setEnabled(value);
        mTextViewConfigTestSmsIntervalLabel.setEnabled(value);
        mSpinnerTestSmsInterval.setEnabled(value);
        if (!value) ((ImageView) findViewById(R.id.imageViewConfigTestSmsPhoneOk))
                .setImageBitmap(null);
        else
        {

            if(mEditTextTestSmsPhone.getText().length()==0)
                ((ImageView) findViewById(R.id.imageViewConfigTestSmsPhoneOk))
                        .setImageBitmap(null);
            else if (Pattern.matches("[7]([0-9]{10})", mEditTextTestSmsPhone.getText().toString()))
                ((ImageView) findViewById(R.id.imageViewConfigTestSmsPhoneOk))
                        .setImageResource(R.drawable.ic_action_accept);
            else
                ((ImageView) findViewById(R.id.imageViewConfigTestSmsPhoneOk))
                        .setImageResource(R.drawable.ic_action_cancel);
        }
    }

    public void onCheckBoxTestCallEnableChange(View view) {
        boolean value = mCheckBoxTestCallEnable.isChecked();
        mTextViewConfigTestCallPhoneLabel.setEnabled(value);
        mEditTextTestCallPhone.setEnabled(value);
        mTextViewConfigTestCallIntervalLabel.setEnabled(value);
        mSpinnerTestCallInterval.setEnabled(value);
        if (!value) ((ImageView) findViewById(R.id.imageViewConfigTestCallPhoneOk))
                .setImageBitmap(null);
        else
        {
            if(mEditTextTestCallPhone.getText().length()==0)
                ((ImageView) findViewById(R.id.imageViewConfigTestCallPhoneOk))
                        .setImageBitmap(null);
            if (Pattern.matches("[7]([0-9]{10})", mEditTextTestCallPhone.getText().toString()))
                ((ImageView) findViewById(R.id.imageViewConfigTestCallPhoneOk))
                        .setImageResource(R.drawable.ic_action_accept);
            else
                ((ImageView) findViewById(R.id.imageViewConfigTestCallPhoneOk))
                        .setImageResource(R.drawable.ic_action_cancel);
        }
    }

    public void onBackBtnPressed(View view) {
        onBackPressed();
    }

    public void onBtnConfigPcnСфClick(View view) {
        onBackPressed();
    }
}
