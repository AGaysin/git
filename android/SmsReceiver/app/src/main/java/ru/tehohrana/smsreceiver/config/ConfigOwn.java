package ru.tehohrana.smsreceiver.config;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;

import java.util.regex.Pattern;

import ru.tehohrana.smsreceiver.ConfiguratorActivity;
import ru.tehohrana.smsreceiver.R;

/**
 * Created by AG on 03.12.2015.
 */
public class ConfigOwn extends Activity {
    SharedPreferences mDeviceSettings;
    private EditText mEditTextOwn1Phone;
    private EditText mEditTextOwn2Phone;
    private ImageView mImageViewConfigOwn1PhoneOK;
    private ImageView mImageViewConfigOwn2PhoneOK;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_config_own);

        //Чтение настроек из файла
        mDeviceSettings = getSharedPreferences(ConfiguratorActivity.CONFIG_PREFERENCES,
                Context.MODE_PRIVATE);

        //Номер телефона OWN1
        mEditTextOwn1Phone = (EditText) findViewById(R.id.editTextConfigOwn1Phone);
        if (mDeviceSettings.contains(ConfiguratorActivity.CONFIG_PREFERENCES_OWN1_PHONE))
            mEditTextOwn1Phone.setText(mDeviceSettings.getString(
                    ConfiguratorActivity.CONFIG_PREFERENCES_OWN1_PHONE,"").toString());

        mImageViewConfigOwn1PhoneOK = (ImageView) findViewById(R.id.imageViewConfigOwn1PhoneOk);

        if (mEditTextOwn1Phone.getText().length()==0) mImageViewConfigOwn1PhoneOK.setImageBitmap(null);
        else if (Pattern.matches("[7]([0-9]{10})", mEditTextOwn1Phone.getText().toString()))
            mImageViewConfigOwn1PhoneOK.setImageResource(R.drawable.ic_action_accept);
        else mImageViewConfigOwn1PhoneOK.setImageResource(R.drawable.ic_action_cancel);
        mEditTextOwn1Phone.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length()==0)
                    mImageViewConfigOwn1PhoneOK.setImageBitmap(null);
                else if (Pattern.matches("[7]([0-9]{10})", s.toString()))
                    mImageViewConfigOwn1PhoneOK.setImageResource(R.drawable.ic_action_accept);
                else mImageViewConfigOwn1PhoneOK.setImageResource(R.drawable.ic_action_cancel);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        //Номер телефона OWN2
        mEditTextOwn2Phone = (EditText) findViewById(R.id.editTextConfigOwn2Phone);
        if (mDeviceSettings.contains(ConfiguratorActivity.CONFIG_PREFERENCES_OWN2_PHONE))
            mEditTextOwn2Phone.setText(mDeviceSettings.getString(
                    ConfiguratorActivity.CONFIG_PREFERENCES_OWN2_PHONE,"").toString());

        mImageViewConfigOwn2PhoneOK = (ImageView) findViewById(R.id.imageViewConfigOwn2PhoneOk);

        if (mEditTextOwn2Phone.getText().length()==0) mImageViewConfigOwn2PhoneOK.setImageBitmap(null);
        else if (Pattern.matches("[7]([0-9]{10})", mEditTextOwn2Phone.getText().toString()))
            mImageViewConfigOwn2PhoneOK.setImageResource(R.drawable.ic_action_accept);
        else mImageViewConfigOwn2PhoneOK.setImageResource(R.drawable.ic_action_cancel);
        mEditTextOwn2Phone.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length()==0)
                    mImageViewConfigOwn2PhoneOK.setImageBitmap(null);
                else if (Pattern.matches("[7]([0-9]{10})", s.toString()))
                    mImageViewConfigOwn2PhoneOK.setImageResource(R.drawable.ic_action_accept);
                else mImageViewConfigOwn2PhoneOK.setImageResource(R.drawable.ic_action_cancel);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

    }



    public void onBtnConfigOwnApplyClick(View view) {
        mDeviceSettings = getSharedPreferences(ConfiguratorActivity.CONFIG_PREFERENCES,
                Context.MODE_PRIVATE);
        //Записываем данные в файл настроек
        mDeviceSettings = getSharedPreferences(ConfiguratorActivity.CONFIG_PREFERENCES,
                Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = mDeviceSettings.edit();

        editor.putString(ConfiguratorActivity.CONFIG_PREFERENCES_OWN1_PHONE,
                mEditTextOwn1Phone.getText().toString());
        editor.putString(ConfiguratorActivity.CONFIG_PREFERENCES_OWN2_PHONE,
                mEditTextOwn2Phone.getText().toString());
        editor.apply();
        super.finish();
    }

    public void onBackBtnPressed(View view) {
        onBackPressed();
    }

    public void onBtnConfigOwnCancelClick(View view) {
        super.finish();
    }
}