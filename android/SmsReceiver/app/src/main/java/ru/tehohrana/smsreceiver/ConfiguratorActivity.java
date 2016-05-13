package ru.tehohrana.smsreceiver;


import android.app.Activity;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import java.util.regex.Pattern;

import ru.tehohrana.smsreceiver.config.ConfigAcc;
import ru.tehohrana.smsreceiver.config.ConfigBT;
import ru.tehohrana.smsreceiver.config.ConfigBat;
import ru.tehohrana.smsreceiver.config.ConfigGen;
import ru.tehohrana.smsreceiver.config.ConfigIn1;
import ru.tehohrana.smsreceiver.config.ConfigIn2;
import ru.tehohrana.smsreceiver.config.ConfigJamm;
import ru.tehohrana.smsreceiver.config.ConfigOwn;
import ru.tehohrana.smsreceiver.config.ConfigPcn;
import ru.tehohrana.smsreceiver.config.ConfigSrv;
import ru.tehohrana.smsreceiver.config.ConfigUgon;
import ru.tehohrana.smsreceiver.service.ArchieveListAdapter;
import ru.tehohrana.smsreceiver.service.ConfigListAdapter;

public class ConfiguratorActivity extends Activity {
    SharedPreferences mDeviceSettings;
    ProgressDialog prog1;

    public boolean isSettingsRead = false;
    // это будет именем файла настроек
    public static final String CONFIG_PREFERENCES = "device_config";
    //Настойки напаметров ПЦН
    public static final String CONFIG_PREFERENCES_PCN_PHONE = "PcnPhoneNum"; // номер телефона ПЦН
    public static final String CONFIG_PREFERENCES_TESTSMS_ENABLE = "TestSmsEnable"; //
    public static final String CONFIG_PREFERENCES_TESTSMS_PHONE = "TestSmsPhoneNumber"; //
    public static final String CONFIG_PREFERENCES_TESTSMS_INTERVAL = "TestSmsInterval"; //
    public static final String CONFIG_PREFERENCES_TESTCALL_ENABLE = "TestCallEnable"; //
    public static final String CONFIG_PREFERENCES_TESTCALL_PHONE = "TestCallPhoneNumber"; //
    public static final String CONFIG_PREFERENCES_TESTCALL_INTERVAL = "TestCallInterval"; //
    //Настройка собственников
    public static final String CONFIG_PREFERENCES_OWN1_PHONE = "Own1Phone"; //
    public static final String CONFIG_PREFERENCES_OWN2_PHONE = "Own2Phone"; //
    //Настройка часового пояса и запроса балланса
    public static final String CONFIG_PREFERENCES_USSD_PHONE = "UssdPhone"; //
    public static final String CONFIG_PREFERENCES_USSD_INTERVAL = "UssdInterval"; //
    public static final String CONFIG_PREFERENCES_TIME_ZONE = "TimeZone"; //
    //Настройка тревоги УГОН
    public static final String CONFIG_PREFERENCES_UGON_DISTANCE = "UgonDistance"; //

    //Конфигурация входа IN1
    public static final String CONFIG_PREFERENCES_IN1_ENABLE = "In1Enable"; //
    public static final String CONFIG_PREFERENCES_IN1_TYPE = "In1Type"; //
    public static final String CONFIG_PREFERENCES_IN1_ACTIVE_LEVEL = "In1AciveLevel"; //
    public static final String CONFIG_PREFERENCES_IN1_ACTION_HORN = "In1ActionHorn"; //
    public static final String CONFIG_PREFERENCES_IN1_ACTION_BLOCK = "In1ActionBlock"; //
    public static final String CONFIG_PREFERENCES_IN1_ALARM_PCN_SMS = "In1AlarmPcnSms"; //
    public static final String CONFIG_PREFERENCES_IN1_ALARM_PCN_CALL = "In1AlarmPcnCall"; //
    public static final String CONFIG_PREFERENCES_IN1_ALARM_OWN1_SMS = "In1AlarmOwn1Sms"; //
    public static final String CONFIG_PREFERENCES_IN1_ALARM_OWN1_CALL = "In1AlarmOwn1Call"; //
    public static final String CONFIG_PREFERENCES_IN1_ALARM_OWN2_SMS = "In1AlarmOwn2Sms"; //
    public static final String CONFIG_PREFERENCES_IN1_ALARM_OWN2_CALL = "In1AlarmOwn2Call"; //
    //Конфигурация входа IN2
    public static final String CONFIG_PREFERENCES_IN2_ENABLE = "In2Ebanle"; //
    public static final String CONFIG_PREFERENCES_IN2_TYPE = "In2Type"; //
    public static final String CONFIG_PREFERENCES_IN2_ACTIVE_LEVEL = "In2AciveLevel"; //
    public static final String CONFIG_PREFERENCES_IN2_ACTION_HORN = "In2ActionHorn"; //
    public static final String CONFIG_PREFERENCES_IN2_ACTION_BLOCK = "In2ActionBlock"; //
    public static final String CONFIG_PREFERENCES_IN2_ALARM_PCN_SMS = "In2AlarmPcnSms"; //
    public static final String CONFIG_PREFERENCES_IN2_ALARM_PCN_CALL = "In2AlarmPcnCall"; //
    public static final String CONFIG_PREFERENCES_IN2_ALARM_OWN1_SMS = "In2AlarmOwn1Sms"; //
    public static final String CONFIG_PREFERENCES_IN2_ALARM_OWN1_CALL = "In2AlarmOwn1Call"; //
    public static final String CONFIG_PREFERENCES_IN2_ALARM_OWN2_SMS = "In2AlarmOwn2Sms"; //
    public static final String CONFIG_PREFERENCES_IN2_ALARM_OWN2_CALL = "In2AlarmOwn2Call"; //
    //Конфигурация входа POWER
    public static final String CONFIG_PREFERENCES_PWR_TYPE = "Pwr12V/24V";
    public static final String CONFIG_PREFERENCES_PWR_LOBAT_ENABLE = "PwrLobatEnable";
    public static final String CONFIG_PREFERENCES_PWR_LOBAT_LEVEL = "PwrLobatLevel";
    public static final String CONFIG_PREFERENCES_PWR_LOBAT_TIME = "PwrLobatTime";
    public static final String CONFIG_PREFERENCES_PWR_LOBAT_ALARM_PCN_SMS = "PwrLobatAlarmPcnSms";
    public static final String CONFIG_PREFERENCES_PWR_LOBAT_ALARM_PCN_CALL = "PwrLobatAlarmPcnCall";
    public static final String CONFIG_PREFERENCES_PWR_LOBAT_ALARM_OWN1_SMS = "PwrLobatAlarmOwn1Sms";
    public static final String CONFIG_PREFERENCES_PWR_LOBAT_ALARM_OWN1_CALL = "PwrLobatAlarmOwn1Call";
    public static final String CONFIG_PREFERENCES_PWR_LOBAT_ALARM_OWN2_SMS = "PwrLobatAlarmOwn2Sms";
    public static final String CONFIG_PREFERENCES_PWR_LOBAT_ALARM_OWN2_CALL = "PwrLobatAlarmOwn2Call";
    public static final String CONFIG_PREFERENCES_PWR_NOBAT_ALARM_PCN_SMS = "PwrNobatAlarmPcnSms";
    public static final String CONFIG_PREFERENCES_PWR_NOBAT_ALARM_PCN_CALL = "PwrNobatAlarmPcnCall";
    public static final String CONFIG_PREFERENCES_PWR_NOBAT_ALARM_OWN1_SMS = "PwrNobatAlarmOwn1Sms";
    public static final String CONFIG_PREFERENCES_PWR_NOBAT_ALARM_OWN1_CALL = "PwrNobatAlarmOwn1Call";
    public static final String CONFIG_PREFERENCES_PWR_NOBAT_ALARM_OWN2_SMS = "PwrNobatAlarmOwn2Sms";
    public static final String CONFIG_PREFERENCES_PWR_NOBAT_ALARM_OWN2_CALL = "PwrNobatAlarmOwn2Call";


    //Конфигурация входа ACCELEROMETER
    public static final String CONFIG_PREFERENCES_ACC_ENABLE = "AccEnable"; //
    public static final String CONFIG_PREFERENCES_ACC_SENS = "AccSens"; //
    public static final String CONFIG_PREFERENCES_ACC_TIME = "AccInterval"; //
    public static final String CONFIG_PREFERENCES_ACC_ACTION_HORN = "AccActionHorn"; //
    public static final String CONFIG_PREFERENCES_ACC_ACTION_BLOCK = "AccActionBlock"; //
    public static final String CONFIG_PREFERENCES_ACC_ALARM_PCN_SMS = "AccAlarmPcnSms"; //
    public static final String CONFIG_PREFERENCES_ACC_ALARM_PCN_CALL = "AccAlarmPcnCall"; //
    public static final String CONFIG_PREFERENCES_ACC_ALARM_OWN1_SMS = "AccAlarmOwn1Sms"; //
    public static final String CONFIG_PREFERENCES_ACC_ALARM_OWN1_CALL = "AccAlarmOwn1Call"; //
    public static final String CONFIG_PREFERENCES_ACC_ALARM_OWN2_SMS = "AccAlarmOwn2Sms"; //
    public static final String CONFIG_PREFERENCES_ACC_ALARM_OWN2_CALL = "AccAlarmOwn2Call"; //

    //Конфигурация входа JAMM
    public static final String CONFIG_PREFERENCES_JAMM_ENABLE = "JammEnable"; //
    public static final String CONFIG_PREFERENCES_JAMM_ACTION_HORN = "JammActionHorn"; //
    public static final String CONFIG_PREFERENCES_JAMM_ACTION_BLOCK = "JammActionBlock"; //
    public static final String CONFIG_PREFERENCES_JAMM_ALARM_PCN_SMS = "JammAlarmPcnSms"; //
    public static final String CONFIG_PREFERENCES_JAMM_ALARM_PCN_CALL = "JammAlarmPcnCall"; //
    public static final String CONFIG_PREFERENCES_JAMM_ALARM_OWN1_SMS = "JammAlarmOwn1Sms"; //
    public static final String CONFIG_PREFERENCES_JAMM_ALARM_OWN1_CALL = "JammAlarmOwn1Call"; //
    public static final String CONFIG_PREFERENCES_JAMM_ALARM_OWN2_SMS = "JammAlarmOwn2Sms"; //
    public static final String CONFIG_PREFERENCES_JAMM_ALARM_OWN2_CALL = "JammAlarmOwn2Call"; //

    //Конфигурация GUARD
    public static final String CONFIG_PREFERENCES_GUARD_ACTION_HORN = "GuardActionHorn"; //
    public static final String CONFIG_PREFERENCES_GUARD_ACTION_BLOCK = "GuardActionBlock"; //
    public static final String CONFIG_PREFERENCES_GUARD_ALARM_PCN_SMS = "GuardAlarmPcnSms"; //
    public static final String CONFIG_PREFERENCES_GUARD_ALARM_PCN_CALL = "GuardAlarmPcnCall"; //
    public static final String CONFIG_PREFERENCES_GUARD_ALARM_OWN1_SMS = "GuardAlarmOwn1Sms"; //
    public static final String CONFIG_PREFERENCES_GUARD_ALARM_OWN1_CALL = "GuardAlarmOwn1Call"; //
    public static final String CONFIG_PREFERENCES_GUARD_ALARM_OWN2_SMS = "GuardAlarmOwn2Sms"; //
    public static final String CONFIG_PREFERENCES_GUARD_ALARM_OWN2_CALL = "GuardAlarmOwn2Call"; //

    //Конфигурация INIT
    public static final String CONFIG_PREFERENCES_INIT_ALARM_PCN_SMS = "InitAlarmPcnSms"; //
    public static final String CONFIG_PREFERENCES_INIT_ALARM_PCN_CALL = "InitAlarmPcnCall"; //
    public static final String CONFIG_PREFERENCES_INIT_ALARM_OWN1_SMS = "InitAlarmOwn1Sms"; //
    public static final String CONFIG_PREFERENCES_INIT_ALARM_OWN1_CALL = "InitAlarmOwn1Call"; //
    public static final String CONFIG_PREFERENCES_INIT_ALARM_OWN2_SMS = "InitAlarmOwn2Sms"; //
    public static final String CONFIG_PREFERENCES_INIT_ALARM_OWN2_CALL = "InitAlarmOwn2Call"; //

    public String mDevicePhoneNumber;
    public SharedPreferences mSettings;

    private Button mBtnWriteConfig;

    private ArrayAdapter<String> mAdapter;

    private int mLastMessageId=0;
    private static long timerSmsCommandWaiting;
    private Handler handler = new Handler();
    private Runnable task = new Runnable() {
        @Override
        public void run() {
            if (timerSmsCommandWaiting > 0)
            {
                checkNewSms();
                if (--timerSmsCommandWaiting == 0) cancelWaiting();
            }
            handler.postDelayed(this,1000);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_config);

        isSettingsRead = false;
        mBtnWriteConfig = (Button) findViewById(R.id.btnWriteConfig);
        mBtnWriteConfig.setEnabled(false);
        //Инициализщируем файл с настрйоками для хранения данных
        mDeviceSettings = getSharedPreferences(CONFIG_PREFERENCES, Context.MODE_PRIVATE);

        mSettings = PreferenceManager.getDefaultSharedPreferences(this);
        mDevicePhoneNumber = mSettings.getString("pref_phone", "");



// определяем массив типа String




    }
    @Override
    protected void onResume() {
        handler.postDelayed(task, 1000);
        super.onResume();


    }
    @Override
    protected void onPause() {
        handler.removeCallbacks(task);
        super.onPause();

    }

    @Override
    public void onBackPressed() {
        new AlertDialog.Builder(this)
                .setTitle("Выйти из конфигуратора?")
                .setMessage("Вы действительно хотите выйти из конфигугратора?")
                .setNegativeButton(android.R.string.no, null)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface arg0, int arg1) {
                        ConfiguratorActivity.super.onBackPressed();
                    }
                }).create().show();
    }

    public void onBtnWriteConfigClick(View view) {

        mDeviceSettings = getSharedPreferences(ConfiguratorActivity.CONFIG_PREFERENCES,
                Context.MODE_PRIVATE);

        mLastMessageId = getLastMessageId();
        AlertDialog.Builder sendSmsIdGetDialog = new AlertDialog.Builder(this);
        sendSmsIdGetDialog.setTitle("Будет отправлена SMS-команда! Вы уверены?");

        sendSmsIdGetDialog.setPositiveButton("Да", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //PCN

                String mPcnPhone = mDeviceSettings.getString(ConfiguratorActivity
                        .CONFIG_PREFERENCES_PCN_PHONE, "70000000000");
                if (mPcnPhone.length() < 10) mPcnPhone = "70000000000";
                //OWN
                String mOwn1Phone = mDeviceSettings.getString(ConfiguratorActivity
                        .CONFIG_PREFERENCES_OWN1_PHONE, "70000000000");
                if (mOwn1Phone.length() < 10) mOwn1Phone = "70000000000";

                String mOwn2Phone = mDeviceSettings.getString(ConfiguratorActivity
                        .CONFIG_PREFERENCES_OWN2_PHONE, "70000000000");
                if (mOwn2Phone.length() < 10) mOwn2Phone = "70000000000";

                String mTestSmsPhone = mDeviceSettings.getString(ConfiguratorActivity
                        .CONFIG_PREFERENCES_TESTSMS_PHONE, "70000000000");

                String mTestCallPhone = mDeviceSettings.getString(ConfiguratorActivity
                        .CONFIG_PREFERENCES_TESTCALL_PHONE, "70000000000");

                int mTestSmsInterval = getTestSmsInterval(mDeviceSettings.getInt(ConfiguratorActivity
                        .CONFIG_PREFERENCES_TESTSMS_INTERVAL, 0));
                int mTestCallInterval = getTestCallInterval(mDeviceSettings.getInt(ConfiguratorActivity
                        .CONFIG_PREFERENCES_TESTCALL_INTERVAL, 0));

                Boolean mTestSmsEnable = mDeviceSettings.getBoolean(ConfiguratorActivity
                        .CONFIG_PREFERENCES_TESTSMS_ENABLE, false);
                Boolean mTestCallEnable = mDeviceSettings.getBoolean(ConfiguratorActivity
                        .CONFIG_PREFERENCES_TESTCALL_ENABLE, false);

                if (!mTestSmsEnable) {
                    mTestSmsInterval = 0;
                    mTestSmsPhone = "70000000000";
                }
                if (!mTestCallEnable) {
                    mTestCallInterval = 0;
                    mTestCallPhone = "70000000000";
                }

                if (mTestSmsPhone.length() < 10) mTestSmsPhone = "70000000000";
                if (mTestCallPhone.length() < 10) mTestCallPhone = "70000000000";

                //Конфигурация входа IN1
                Boolean mIn1Enable = mDeviceSettings.getBoolean(ConfiguratorActivity
                        .CONFIG_PREFERENCES_IN1_ENABLE, false);

                int mIn1Type = 1;
                int mIn1Action = 0;
                String mIn1Notify = "000";

                if (mIn1Enable) {
                    mIn1Type = getInputType(
                            mDeviceSettings.getBoolean(ConfiguratorActivity
                                    .CONFIG_PREFERENCES_IN1_ACTIVE_LEVEL, false),
                            mDeviceSettings.getInt(ConfiguratorActivity
                                    .CONFIG_PREFERENCES_IN1_TYPE, 0));


                    mIn1Action = getActionMask(
                            mDeviceSettings.getBoolean(ConfiguratorActivity
                                    .CONFIG_PREFERENCES_IN1_ACTION_HORN, false),
                            mDeviceSettings.getBoolean(ConfiguratorActivity
                                    .CONFIG_PREFERENCES_IN1_ACTION_BLOCK, false));

                    mIn1Notify = getNotifyMask(
                            mDeviceSettings.getBoolean(ConfiguratorActivity
                                    .CONFIG_PREFERENCES_IN1_ALARM_PCN_SMS, false),
                            mDeviceSettings.getBoolean(ConfiguratorActivity
                                    .CONFIG_PREFERENCES_IN1_ALARM_PCN_CALL, false),
                            mDeviceSettings.getBoolean(ConfiguratorActivity
                                    .CONFIG_PREFERENCES_IN1_ALARM_OWN1_SMS, false),
                            mDeviceSettings.getBoolean(ConfiguratorActivity
                                    .CONFIG_PREFERENCES_IN1_ALARM_OWN1_CALL, false),
                            mDeviceSettings.getBoolean(ConfiguratorActivity
                                    .CONFIG_PREFERENCES_IN1_ALARM_OWN2_SMS, false),
                            mDeviceSettings.getBoolean(ConfiguratorActivity
                                    .CONFIG_PREFERENCES_IN1_ALARM_OWN2_CALL, false));
                }

                //Конфигурация входа IN2
                Boolean mIn2Enable = mDeviceSettings.getBoolean(ConfiguratorActivity
                        .CONFIG_PREFERENCES_IN2_ENABLE, false);

                int mIn2Type = 1;
                int mIn2Action = 0;
                String mIn2Notify = "000";

                if (mIn2Enable) {
                    mIn2Type = getInputType(
                            mDeviceSettings.getBoolean(ConfiguratorActivity
                                    .CONFIG_PREFERENCES_IN2_ACTIVE_LEVEL, false),
                            mDeviceSettings.getInt(ConfiguratorActivity
                                    .CONFIG_PREFERENCES_IN2_TYPE, 0));


                    mIn2Action = getActionMask(
                            mDeviceSettings.getBoolean(ConfiguratorActivity
                                    .CONFIG_PREFERENCES_IN2_ACTION_HORN, false),
                            mDeviceSettings.getBoolean(ConfiguratorActivity
                                    .CONFIG_PREFERENCES_IN2_ACTION_BLOCK, false));

                    mIn2Notify = getNotifyMask(
                            mDeviceSettings.getBoolean(ConfiguratorActivity
                                    .CONFIG_PREFERENCES_IN2_ALARM_PCN_SMS, false),
                            mDeviceSettings.getBoolean(ConfiguratorActivity
                                    .CONFIG_PREFERENCES_IN2_ALARM_PCN_CALL, false),
                            mDeviceSettings.getBoolean(ConfiguratorActivity
                                    .CONFIG_PREFERENCES_IN2_ALARM_OWN1_SMS, false),
                            mDeviceSettings.getBoolean(ConfiguratorActivity
                                    .CONFIG_PREFERENCES_IN2_ALARM_OWN1_CALL, false),
                            mDeviceSettings.getBoolean(ConfiguratorActivity
                                    .CONFIG_PREFERENCES_IN2_ALARM_OWN2_SMS, false),
                            mDeviceSettings.getBoolean(ConfiguratorActivity
                                    .CONFIG_PREFERENCES_IN2_ALARM_OWN2_CALL, false));
                }

                //Конфигурация входа POWER
                Boolean mLobatEnable = mDeviceSettings.getBoolean(ConfiguratorActivity
                        .CONFIG_PREFERENCES_PWR_LOBAT_ENABLE, false);

                int mLobatLevel = 105;
                int mLobatTime = 3;
                String mLobatNotify = "000";

                if (mLobatEnable) {
                    mLobatLevel = mDeviceSettings.getInt(ConfiguratorActivity
                            .CONFIG_PREFERENCES_PWR_LOBAT_LEVEL, 105);
                    mLobatTime = getLowbatTime(mDeviceSettings.getInt(ConfiguratorActivity
                            .CONFIG_PREFERENCES_PWR_LOBAT_TIME, 2));

                    mLobatNotify = getNotifyMask(
                            mDeviceSettings.getBoolean(ConfiguratorActivity
                                    .CONFIG_PREFERENCES_PWR_LOBAT_ALARM_PCN_SMS, false),
                            mDeviceSettings.getBoolean(ConfiguratorActivity
                                    .CONFIG_PREFERENCES_PWR_LOBAT_ALARM_PCN_CALL, false),
                            mDeviceSettings.getBoolean(ConfiguratorActivity
                                    .CONFIG_PREFERENCES_PWR_LOBAT_ALARM_OWN1_SMS, false),
                            mDeviceSettings.getBoolean(ConfiguratorActivity
                                    .CONFIG_PREFERENCES_PWR_LOBAT_ALARM_OWN1_CALL, false),
                            mDeviceSettings.getBoolean(ConfiguratorActivity
                                    .CONFIG_PREFERENCES_PWR_LOBAT_ALARM_OWN2_SMS, false),
                            mDeviceSettings.getBoolean(ConfiguratorActivity
                                    .CONFIG_PREFERENCES_PWR_LOBAT_ALARM_OWN2_CALL, false));
                }
                String mNobatNotify = getNotifyMask(
                        mDeviceSettings.getBoolean(ConfiguratorActivity
                                .CONFIG_PREFERENCES_PWR_NOBAT_ALARM_PCN_SMS, false),
                        mDeviceSettings.getBoolean(ConfiguratorActivity
                                .CONFIG_PREFERENCES_PWR_NOBAT_ALARM_PCN_CALL, false),
                        mDeviceSettings.getBoolean(ConfiguratorActivity
                                .CONFIG_PREFERENCES_PWR_NOBAT_ALARM_OWN1_SMS, false),
                        mDeviceSettings.getBoolean(ConfiguratorActivity
                                .CONFIG_PREFERENCES_PWR_NOBAT_ALARM_OWN1_CALL, false),
                        mDeviceSettings.getBoolean(ConfiguratorActivity
                                .CONFIG_PREFERENCES_PWR_NOBAT_ALARM_OWN2_SMS, false),
                        mDeviceSettings.getBoolean(ConfiguratorActivity
                                .CONFIG_PREFERENCES_PWR_NOBAT_ALARM_OWN2_CALL, false));

                //Конфигурация входа ACCELEROMETER
                Boolean mAccEnable = mDeviceSettings.getBoolean(ConfiguratorActivity
                        .CONFIG_PREFERENCES_ACC_ENABLE, false);

                int mAccLevel = 0;
                int mAccTime = 60;
                int mAccAction = 0;
                String mAccNotify = "000";

                if (mAccEnable) {
                    mAccLevel = getAccSens(mDeviceSettings.getInt(ConfiguratorActivity
                            .CONFIG_PREFERENCES_ACC_SENS, 0));
                    mAccTime = getAccTime(mDeviceSettings.getInt(ConfiguratorActivity
                            .CONFIG_PREFERENCES_ACC_TIME, 4));

                    mAccAction = getActionMask(
                            mDeviceSettings.getBoolean(ConfiguratorActivity
                                    .CONFIG_PREFERENCES_ACC_ACTION_HORN, false),
                            mDeviceSettings.getBoolean(ConfiguratorActivity
                                    .CONFIG_PREFERENCES_ACC_ACTION_BLOCK, false));

                    mAccNotify = getNotifyMask(
                            mDeviceSettings.getBoolean(ConfiguratorActivity
                                    .CONFIG_PREFERENCES_ACC_ALARM_PCN_SMS, false),
                            mDeviceSettings.getBoolean(ConfiguratorActivity
                                    .CONFIG_PREFERENCES_ACC_ALARM_PCN_CALL, false),
                            mDeviceSettings.getBoolean(ConfiguratorActivity
                                    .CONFIG_PREFERENCES_ACC_ALARM_OWN1_SMS, false),
                            mDeviceSettings.getBoolean(ConfiguratorActivity
                                    .CONFIG_PREFERENCES_ACC_ALARM_OWN1_CALL, false),
                            mDeviceSettings.getBoolean(ConfiguratorActivity
                                    .CONFIG_PREFERENCES_ACC_ALARM_OWN2_SMS, false),
                            mDeviceSettings.getBoolean(ConfiguratorActivity
                                    .CONFIG_PREFERENCES_ACC_ALARM_OWN2_CALL, false));
                }
                //Конфигурация входа JAMM
                Boolean mJammEnable = mDeviceSettings.getBoolean(ConfiguratorActivity
                        .CONFIG_PREFERENCES_JAMM_ENABLE, false);

                int mJammAction = 0;
                String mJammNotify = "000";
                if (mJammEnable) {
                    mJammAction = getActionMask(
                            mDeviceSettings.getBoolean(ConfiguratorActivity
                                    .CONFIG_PREFERENCES_JAMM_ACTION_HORN, false),
                            mDeviceSettings.getBoolean(ConfiguratorActivity
                                    .CONFIG_PREFERENCES_JAMM_ACTION_BLOCK, false));

                    mJammNotify = getNotifyMask(
                            mDeviceSettings.getBoolean(ConfiguratorActivity
                                    .CONFIG_PREFERENCES_JAMM_ALARM_PCN_SMS, false),
                            mDeviceSettings.getBoolean(ConfiguratorActivity
                                    .CONFIG_PREFERENCES_JAMM_ALARM_PCN_CALL, false),
                            mDeviceSettings.getBoolean(ConfiguratorActivity
                                    .CONFIG_PREFERENCES_JAMM_ALARM_OWN1_SMS, false),
                            mDeviceSettings.getBoolean(ConfiguratorActivity
                                    .CONFIG_PREFERENCES_JAMM_ALARM_OWN1_CALL, false),
                            mDeviceSettings.getBoolean(ConfiguratorActivity
                                    .CONFIG_PREFERENCES_JAMM_ALARM_OWN2_SMS, false),
                            mDeviceSettings.getBoolean(ConfiguratorActivity
                                    .CONFIG_PREFERENCES_JAMM_ALARM_OWN2_CALL, false));
                }


                //Конфигурация INIT
                String mInitNotify = getNotifyMask(
                        mDeviceSettings.getBoolean(ConfiguratorActivity
                                .CONFIG_PREFERENCES_INIT_ALARM_PCN_SMS, false),
                        mDeviceSettings.getBoolean(ConfiguratorActivity
                                .CONFIG_PREFERENCES_INIT_ALARM_PCN_CALL, false),
                        mDeviceSettings.getBoolean(ConfiguratorActivity
                                .CONFIG_PREFERENCES_INIT_ALARM_OWN1_SMS, false),
                        mDeviceSettings.getBoolean(ConfiguratorActivity
                                .CONFIG_PREFERENCES_INIT_ALARM_OWN1_CALL, false),
                        mDeviceSettings.getBoolean(ConfiguratorActivity
                                .CONFIG_PREFERENCES_INIT_ALARM_OWN2_SMS, false),
                        mDeviceSettings.getBoolean(ConfiguratorActivity
                                .CONFIG_PREFERENCES_INIT_ALARM_OWN2_CALL, false));

                //Конфигурация GUARD
                int mGuardAction = getActionMask(
                        mDeviceSettings.getBoolean(ConfiguratorActivity
                                .CONFIG_PREFERENCES_GUARD_ACTION_HORN, false),
                        mDeviceSettings.getBoolean(ConfiguratorActivity
                                .CONFIG_PREFERENCES_GUARD_ACTION_BLOCK, false));

                String mGuardNotify = getNotifyMask(
                        mDeviceSettings.getBoolean(ConfiguratorActivity
                                .CONFIG_PREFERENCES_GUARD_ALARM_PCN_SMS, false),
                        mDeviceSettings.getBoolean(ConfiguratorActivity
                                .CONFIG_PREFERENCES_GUARD_ALARM_PCN_CALL, false),
                        mDeviceSettings.getBoolean(ConfiguratorActivity
                                .CONFIG_PREFERENCES_GUARD_ALARM_OWN1_SMS, false),
                        mDeviceSettings.getBoolean(ConfiguratorActivity
                                .CONFIG_PREFERENCES_GUARD_ALARM_OWN1_CALL, false),
                        mDeviceSettings.getBoolean(ConfiguratorActivity
                                .CONFIG_PREFERENCES_GUARD_ALARM_OWN2_SMS, false),
                        mDeviceSettings.getBoolean(ConfiguratorActivity
                                .CONFIG_PREFERENCES_GUARD_ALARM_OWN2_CALL, false));

                //КОнфигурация запроса баланса и тайм зоны
                String mUssdPhone = mDeviceSettings.getString(ConfiguratorActivity
                        .CONFIG_PREFERENCES_USSD_PHONE, "");
                int mUssdInterval = setUssdInterval(mDeviceSettings.getInt(ConfiguratorActivity
                        .CONFIG_PREFERENCES_USSD_INTERVAL, 0));
                int mTimeZone = mDeviceSettings.getInt(ConfiguratorActivity
                        .CONFIG_PREFERENCES_TIME_ZONE, 0);

                //Конфигурация охраны типа УГОН по спутнкиам ГПС
                int mUgonDistance = setUgonDistance(mDeviceSettings.getInt(ConfiguratorActivity
                        .CONFIG_PREFERENCES_UGON_DISTANCE, 0));


                String string = "&SET=" +
                        "#A" + mPcnPhone +
                        "#B" + mOwn1Phone +
                        "#C" + mOwn2Phone +
                        "#D" + mTestSmsPhone +
                        "#E" + mTestCallPhone +
                        "#F" + mTestSmsInterval + "*" + mTestCallInterval +
                        "#G" + mIn1Action + "*" + mIn1Type + "*" + mIn1Notify +
                        "#H" + mIn2Action + "*" + mIn2Type + "*" + mIn2Notify +
                        "#I" + mNobatNotify +
                        "#J" + mLobatLevel + "*" + mLobatTime + "*" + mLobatNotify +
                        "#K" + mAccAction + "*" + mAccLevel + "*" + mAccTime + "*" + mAccNotify +
                        "#L" + mJammAction + "*" + mJammNotify +
                        "#M" + mInitNotify +
                        "#N" + mGuardAction + "*" + mGuardNotify +
                        "#O" + mTimeZone +
                        "#P" + mUssdInterval + mUssdPhone +
                        "#R" + mUgonDistance + "***";

                sendSMS(mDevicePhoneNumber, string);
                mLastMessageId = getLastMessageId();
                prog1 = new ProgressDialog(ConfiguratorActivity.this);
                prog1.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                prog1.setMessage("Ожидание запроса конфигурации оборудования...");
                prog1.setIndeterminate(true); // выдать значек ожидания
                prog1.setCancelable(false);
                timerSmsCommandWaiting = 60;
                prog1.show();


            }
        });

        sendSmsIdGetDialog.setNegativeButton("Отмена", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //нет действий
            }
        });
        sendSmsIdGetDialog.show();
    }

    private void sendSMS(String phoneNumber, String message)    {
        String SENT="SMS_SENT";
        String DELIVERED="SMS_DELIVERED";

        PendingIntent sentPI= PendingIntent.getBroadcast(this,0,
                new Intent(SENT),0);

        PendingIntent deliveredPI= PendingIntent.getBroadcast(this,0,
                new Intent(DELIVERED),0);

        //---когда SMS отправлено---
        registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context arg0, Intent arg1) {
                switch (getResultCode()) {
                    case Activity.RESULT_OK:
                        Toast.makeText(getBaseContext(), "SMS Отправлено",
                                Toast.LENGTH_SHORT).show();
                        break;
                    case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
                        Toast.makeText(getBaseContext(), "Ошибка отправки",
                                Toast.LENGTH_SHORT).show();
                        break;
                    case SmsManager.RESULT_ERROR_NO_SERVICE:
                        Toast.makeText(getBaseContext(), "Сервис недоступен",
                                Toast.LENGTH_SHORT).show();
                        break;
                    case SmsManager.RESULT_ERROR_NULL_PDU:
                        Toast.makeText(getBaseContext(), "Null PDU",
                                Toast.LENGTH_SHORT).show();
                        break;
                    case SmsManager.RESULT_ERROR_RADIO_OFF:
                        Toast.makeText(getBaseContext(), "Модем выключен",
                                Toast.LENGTH_SHORT).show();
                        break;

                }
                unregisterReceiver(this);
            }
        }, new IntentFilter(SENT));

        //---когда SMS доставлено---
        registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context arg0, Intent arg1) {
                switch (getResultCode()) {
                    case Activity.RESULT_OK:
                        Toast.makeText(getBaseContext(), "SMS Доставлено",
                                Toast.LENGTH_SHORT).show();
                        break;
                    case Activity.RESULT_CANCELED:
                        Toast.makeText(getBaseContext(), "SMS Не доставлено",
                                Toast.LENGTH_SHORT).show();
                        break;
                }
                unregisterReceiver(this);
            }
        }, new IntentFilter(DELIVERED));

        SmsManager sms= SmsManager.getDefault();
        sms.sendTextMessage(phoneNumber, null, message, sentPI, deliveredPI);

    }
    public int getTestSmsInterval(int value)
    {
        switch(value)
        {
            case 0:
            case 1:
            case 2:
            case 3:
            case 4:
            case 5:     return value;
            case 6:     return 8;
            case 7:     return 12;
            case 8:     return 16;
            case 9:     return 24;
            case 10:    return 36;
            case 11:    return 48;
            case 12:    return 72;
            case 13:    return 96;
            case 14:    return 120;
            case 15:    return 148;
            case 16:    return 240;
        }
        return 0;
    }

    public int getTestCallInterval(int value)
    {
        switch(value)
        {
            case 0:     return 0;
            case 1:     return 5;
            case 2:     return 10;
            case 3:     return 15;
            case 4:     return 30;
            case 5:     return 45;
            case 6:     return 60;
            case 7:     return 90;
            case 8:     return 120;
            case 9:     return 180;
            case 10:    return 240;
        }
        return 0;
    }
    public int setUgonDistance(int value)
    {
        switch(value)
        {
            case 0: return 0;
            case 1: return 10;
            case 2: return 20;
            case 3: return 30;
            case 4: return 50;
            case 5: return 100;
            case 6:     return 150;
            case 7:     return 200;
            case 8:     return 300;
            case 9:     return 500;
        }
        return 0;
    }

    public int getUgonDistance(int value)
    {
        if (value==0) return 0;
        else if (value<=10) return 1;
        else if (value<=20) return 2;
        else if (value<=30) return 3;
        else if (value<=50) return 4;
        else if (value<=100) return 5;
        else if (value<=150) return 6;
        else if (value<=200) return 7;
        else if (value<=300) return 8;
        else if (value<=500) return 9;
        else return 0;
    }
    public int getActionMask(boolean mHornEnable, boolean mBlockEnable)
    {
        if (mHornEnable) {
            if (mBlockEnable) return 3;
            else return 1;
        } else {
            if (mBlockEnable) return 2;
            else return 0;
        }
    }
    public String getNotifyMask(boolean mPcnSms, boolean mPcnCall,
                             boolean mOwn1Sms, boolean mOwn1Call,
                             boolean mOwn2Sms, boolean mOwn2Call) {
        String string="";
        if (mPcnSms) {
            if (mPcnCall) string+= "3";
            else string+= "1";
        } else {
            if (mPcnCall) string+= "2";
            else string+= "0";
        }
        if (mOwn1Sms) {
            if (mOwn1Call) string+= "3";
            else string+= "1";
        } else {
            if (mOwn1Call) string+= "2";
            else string+= "0";
        }
        if (mOwn2Sms) {
            if (mOwn2Call) string+= "3";
            else string+= "1";
        } else {
            if (mOwn2Call) string+= "2";
            else string+= "0";
        }
        return string;
    }
    public int getInputType(boolean mLevel, int mType) {
        switch (mType){
            case 0:
                if (!mLevel) return 0;
                else return 1;
            case 1:
                if (!mLevel) return 2;
                else return 3;
            case 2:
                if (!mLevel) return 4;
                else return 5;
        }
        return 1;

    }
    public int getLowbatTime(int value){
        return value+1;
    }
    public int getAccSens(int value) {
        return value+1;
    }
    public int getAccTime(int value){
        switch (value){
            case 0: return 10;
            case 1: return 15;
            case 2: return 20;
            case 3: return 30;
            case 4: return 45;
            case 5: return 60;
            case 6: return 90;
            case 7: return 120;
            case 8: return 180;
            case 9: return 240;
        }
        return 30;
    }
    private int setUssdInterval (int value)
    {
        switch (value)
        {
            case 0:
            case 1:
            case 2:  return value;
            case 3: return 8;
            case 4: return 12;
            case 5: return 24;
            default: return 0;
        }
    }

    private int getUssdInterval (int value)
    {
        if (value<=2) return value;
        else if (value<=4) return 3;
        else if (value<=8) return 4;
        else if (value<=12) return 5;
        else if (value<=24) return 6;
        else return 0;
    }
    public int setTestSmsInterval(int value)
    {
        if (value <=5) return value;
        else if (value <= 8) return 6;
        else if (value <= 12) return 7;
        else if (value <= 16) return 8;
        else if (value <= 24) return 9;
        else if (value <= 36) return 10;
        else if (value <= 48) return 11;
        else if (value <= 72) return 12;
        else if (value <= 96) return 13;
        else if (value <= 120) return 14;
        else if (value <= 148) return 15;
        else if (value <= 255) return 16;
        else return 0;
    }

    public int setTestCallInterval(int value)
    {
        if (value == 0) return 0;
        else if (value <= 5) return 1;
        else if (value <= 10) return 2;
        else if (value <= 15) return 3;
        else if (value <= 30) return 4;
        else if (value <= 45) return 5;
        else if (value <= 60) return 6;
        else if (value <= 90) return 7;
        else if (value <= 120) return 8;
        else if (value <= 180) return 9;
        else if (value <= 255) return 10;
        else return 0;
    }

    public int setLowbatTime(int value){
        if (value>0) value--;
        return value;
    }
    public int setAccSens(int value) {
        if (value>0) value--;
        return value;
    }
    public int setAccTime(int value){
        if (value<=10) return 0;
        else if (value<=15) return 1;
        else if (value<=20) return 2;
        else if (value<=30) return 3;
        else if (value<=45) return 4;
        else if (value<=60) return 5;
        else if (value<=90) return 6;
        else if (value<=120) return 7;
        else if (value<=180) return 8;
        else if (value<=240) return 9;
        else return 9;
    }

    public void onBtnReadConfigClick(View view) {

        AlertDialog.Builder sendSmsIdGetDialog = new AlertDialog.Builder(this);
        sendSmsIdGetDialog.setTitle("Будет отправлена SMS-команда! Вы уверены?");

        sendSmsIdGetDialog.setPositiveButton("Да", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                mLastMessageId = getLastMessageId();
                sendSMS(mDevicePhoneNumber, "&SET?");
                prog1 = new ProgressDialog(ConfiguratorActivity.this);
                prog1.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                prog1.setMessage("Ожидание запроса конфигурации оборудования...");
                prog1.setIndeterminate(true); // выдать значек ожидания
                prog1.setCancelable(false);
                timerSmsCommandWaiting = 60;
                prog1.show();
            }
        });

        sendSmsIdGetDialog.setNegativeButton("Отмена", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //нет действий
            }
        });
        sendSmsIdGetDialog.show();


        //prog1.cancel();
        //startActivity(new Intent(ConfiguratorActivity.this, ConfigReceiver.class));
    }

    public void checkNewSms(){
        if (readSmsParameters(mLastMessageId))
        {
            prog1.dismiss();
            //Вывсти сообщение, что параметры успешно обновлены
            AlertDialog.Builder builder = new AlertDialog.Builder(ConfiguratorActivity.this);
            isSettingsRead = true;
            //Разрешить список для редактирования
            //TODO:
            mBtnWriteConfig.setEnabled(true);

            showParameters();
            builder.setTitle("Данные успешно обновлены")
                    .setMessage("Данные успешно обновлены и загружены")
                    .setIcon(R.drawable.ic_action_error)
                    .setCancelable(false)
                    .setNegativeButton("ОК",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.cancel();
                                }
                            });
            timerSmsCommandWaiting = 0;
            AlertDialog alert = builder.create();
            alert.show();

        }

        prog1.setMessage("Ожидание запроса конфигурации оборудования...(" + timerSmsCommandWaiting + ")");
        //Toast.makeText(getApplicationContext(), "check " + timerSmsCommandWaiting, Toast.LENGTH_SHORT).show();
        //prog1.cancel();
    }

    public void cancelWaiting(){
        prog1.dismiss();
        //Вывести сообщение о таймауте
        AlertDialog.Builder builder = new AlertDialog.Builder(ConfiguratorActivity.this);
        builder.setTitle("Время вышло!")
                .setMessage("Время ожидания СМС сообщеняи истекло!")
                .setIcon(R.drawable.ic_action_error)
                .setCancelable(false)
                .setNegativeButton("ОК",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });
        AlertDialog alert = builder.create();
        alert.show();

    }

    public void showParameters(){
        String[] config_names = getResources().getStringArray(R.array.config_items_array); //11 items

        Integer[] mImagesArray = {
                R.drawable.nullpic,
                R.drawable.nullpic,
                R.drawable.sim,
                R.drawable.in1,
                R.drawable.in2,
                R.drawable.battery_small,
                R.drawable.mover,
                R.drawable.accelerometer,
                R.drawable.gsm_jam,
                R.drawable.refresh,
                R.drawable.bluetooth
        };

        ConfigListAdapter adapter = new ConfigListAdapter(this, config_names, mImagesArray);
        // получаем экземпляр элемента ListView
        ListView listView = (ListView)findViewById(R.id.listViewConfig);
        listView.setAdapter(adapter);

//        ArchieveListAdapter adapter=new ArchieveListAdapter(this, dataBaseEvents, dataBaseDates, dataBaseIcons);
//        ListView list=(ListView)findViewById(R.id.listViewDataBase);
//        list.setAdapter(adapter);


// используем адаптер данных
//        mAdapter = new ArrayAdapter<>(this,
//                android.R.layout.simple_list_item_1, config_names);
//
//        listView.setAdapter(mAdapter);



        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                switch (position)
                {
                    case 0:
                        startActivity(new Intent(ConfiguratorActivity.this, ConfigPcn.class));
                        break;
                    case 1:
                        startActivity(new Intent(ConfiguratorActivity.this, ConfigOwn.class));
                        break;
                    case 2:
                        startActivity(new Intent(ConfiguratorActivity.this, ConfigGen.class));
                        break;
                    case 3:
                        startActivity(new Intent(ConfiguratorActivity.this, ConfigIn1.class));
                        break;
                    case 4:
                        startActivity(new Intent(ConfiguratorActivity.this, ConfigIn2.class));
                        break;
                    case 5:
                        startActivity(new Intent(ConfiguratorActivity.this, ConfigBat.class));
                        break;
                    case 6:
                        startActivity(new Intent(ConfiguratorActivity.this, ConfigUgon.class));
                        break;
                    case 7:
                        startActivity(new Intent(ConfiguratorActivity.this, ConfigAcc.class));
                        break;
                    case 8:
                        startActivity(new Intent(ConfiguratorActivity.this, ConfigJamm.class));
                        break;
                    case 9:
                        startActivity(new Intent(ConfiguratorActivity.this, ConfigSrv.class));
                        break;
                    case 10:
                        startActivity(new Intent(ConfiguratorActivity.this, ConfigBT.class));
                        break;
                    default:
                        break;
                }

            }
        });
    }

    public boolean readSmsParameters(int mId){
        final String SMS_URI_INBOX = "content://sms/inbox";
        if (!mDevicePhoneNumber.isEmpty()) {
            try {
                String mDevicePhoneNumberRequest = "address='" + mDevicePhoneNumber + "'";
                String[] projection = new String[]{"_id", "address", "person", "body", "date", "type"};
                Cursor cur = getContentResolver().query(Uri.parse(SMS_URI_INBOX),
                        projection,
                        mDevicePhoneNumberRequest,
                        null,
                        "date desc");
                //startManagingCursor(cur);

                if (cur.moveToFirst()) {
                    if (cur.getInt(cur.getColumnIndex("_id"))>mId) {
                        mDeviceSettings = getSharedPreferences(ConfiguratorActivity.CONFIG_PREFERENCES,
                                Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = mDeviceSettings.edit();

                        String bodyText = cur.getString(cur.getColumnIndex("body"));
                        if (bodyText.contains("&SET=")) {
                            Pattern patternSharp = Pattern.compile("#");
                            String[] mReceivedData = patternSharp.split(bodyText);
                            Pattern patternStar = Pattern.compile("\\*");
                            boolean mMaskPcnSms;
                            boolean mMaskPcnCall;
                            boolean mMaskOwn1Sms;
                            boolean mMaskOwn1Call;
                            boolean mMaskOwn2Sms;
                            boolean mMaskOwn2Call;
                            int mMaskInt;

                            for (String string : mReceivedData) {
                                String[] mReceivedDataStar = patternStar.split(string.substring(1));

                                try {
                                    mMaskInt = Integer.parseInt(mReceivedDataStar[mReceivedDataStar.length - 1]);
                                } catch (Exception ex) {
                                    mMaskInt = 0;
                                    //Toast.makeText(this, "Exception: " + ex.toString(), Toast.LENGTH_SHORT).show();
                                }

                                mMaskPcnSms = (mMaskInt / 100 == 1 || mMaskInt / 100 == 3);
                                mMaskPcnCall = (mMaskInt >= 200);
                                mMaskOwn1Sms = ((mMaskInt / 10) % 10 == 1 || (mMaskInt / 10) % 10 == 3);
                                mMaskOwn1Call = ((mMaskInt / 10) % 10 >= 2);
                                mMaskOwn2Sms = (mMaskInt % 10 == 1 || mMaskInt % 10 == 3);
                                mMaskOwn2Call = (mMaskInt % 10 >= 2);

                                switch (string.charAt(0)) {
                                    case 'A':
                                        editor.putString(ConfiguratorActivity.
                                                CONFIG_PREFERENCES_PCN_PHONE, string.substring(1));
                                        break;
                                    case 'B':
                                        editor.putString(ConfiguratorActivity.
                                                CONFIG_PREFERENCES_OWN1_PHONE, string.substring(1));
                                        break;
                                    case 'C':
                                        editor.putString(ConfiguratorActivity.
                                                CONFIG_PREFERENCES_OWN2_PHONE, string.substring(1));
                                        break;
                                    case 'D':
                                        editor.putString(ConfiguratorActivity.
                                                CONFIG_PREFERENCES_TESTSMS_PHONE, string.substring(1));
                                        break;
                                    case 'E':
                                        editor.putString(ConfiguratorActivity.
                                                CONFIG_PREFERENCES_TESTCALL_PHONE, string.substring(1));
                                        break;
                                    case 'F':
                                        if (mReceivedDataStar.length >= 2) {
                                            editor.putInt(ConfiguratorActivity.CONFIG_PREFERENCES_TESTSMS_INTERVAL,
                                                    setTestSmsInterval(Integer.valueOf(mReceivedDataStar[0])));
                                            editor.putInt(ConfiguratorActivity.CONFIG_PREFERENCES_TESTCALL_INTERVAL,
                                                    setTestCallInterval(Integer.valueOf(mReceivedDataStar[1])));
                                            editor.putBoolean(ConfiguratorActivity.CONFIG_PREFERENCES_TESTSMS_ENABLE,
                                                    Integer.valueOf(mReceivedDataStar[0]) != 0);
                                            editor.putBoolean(ConfiguratorActivity.CONFIG_PREFERENCES_TESTCALL_ENABLE,
                                                    Integer.valueOf(mReceivedDataStar[1]) != 0);
                                        }

                                        break;
                                    case 'G':
                                        if (mReceivedDataStar.length >= 3) {
                                            editor.putBoolean(ConfiguratorActivity.CONFIG_PREFERENCES_IN1_ENABLE,
                                                    !mReceivedDataStar[2].contentEquals("000"));

                                            editor.putInt(ConfiguratorActivity.CONFIG_PREFERENCES_IN1_TYPE,
                                                    Integer.valueOf(mReceivedDataStar[1]) / 2);

                                            editor.putBoolean(ConfiguratorActivity.CONFIG_PREFERENCES_IN1_ACTIVE_LEVEL,
                                                    Integer.valueOf(mReceivedDataStar[1]) % 2 != 0);

                                            editor.putBoolean(ConfiguratorActivity.CONFIG_PREFERENCES_IN1_ACTION_HORN,
                                                    Integer.valueOf(mReceivedDataStar[0]) % 2 == 1);

                                            editor.putBoolean(ConfiguratorActivity.CONFIG_PREFERENCES_IN1_ACTION_BLOCK,
                                                    Integer.valueOf(mReceivedDataStar[0]) >= 2);

                                            editor.putBoolean(ConfiguratorActivity.CONFIG_PREFERENCES_IN1_ALARM_PCN_SMS,
                                                    mMaskPcnSms);
                                            editor.putBoolean(ConfiguratorActivity.CONFIG_PREFERENCES_IN1_ALARM_PCN_CALL,
                                                    mMaskPcnCall);

                                            editor.putBoolean(ConfiguratorActivity.CONFIG_PREFERENCES_IN1_ALARM_OWN1_SMS,
                                                    mMaskOwn1Sms);
                                            editor.putBoolean(ConfiguratorActivity.CONFIG_PREFERENCES_IN1_ALARM_OWN1_CALL,
                                                    mMaskOwn1Call);

                                            editor.putBoolean(ConfiguratorActivity.CONFIG_PREFERENCES_IN1_ALARM_OWN2_SMS,
                                                    mMaskOwn2Sms);
                                            editor.putBoolean(ConfiguratorActivity.CONFIG_PREFERENCES_IN1_ALARM_OWN2_CALL,
                                                    mMaskOwn2Call);
                                        }
                                        break;
                                    case 'H':
                                        if (mReceivedDataStar.length >= 3) {
                                            editor.putBoolean(ConfiguratorActivity.CONFIG_PREFERENCES_IN2_ENABLE,
                                                    !mReceivedDataStar[2].contentEquals("000"));

                                            editor.putInt(ConfiguratorActivity.CONFIG_PREFERENCES_IN2_TYPE,
                                                    Integer.valueOf(mReceivedDataStar[1]) / 2);

                                            editor.putBoolean(ConfiguratorActivity.CONFIG_PREFERENCES_IN2_ACTIVE_LEVEL,
                                                    Integer.valueOf(mReceivedDataStar[1]) % 2 != 0);

                                            editor.putBoolean(ConfiguratorActivity.CONFIG_PREFERENCES_IN2_ACTION_HORN,
                                                    Integer.valueOf(mReceivedDataStar[0]) % 2 == 1);

                                            editor.putBoolean(ConfiguratorActivity.CONFIG_PREFERENCES_IN2_ACTION_BLOCK,
                                                    Integer.valueOf(mReceivedDataStar[0]) >= 2);

                                            editor.putBoolean(ConfiguratorActivity.CONFIG_PREFERENCES_IN2_ALARM_PCN_SMS,
                                                    mMaskPcnSms);
                                            editor.putBoolean(ConfiguratorActivity.CONFIG_PREFERENCES_IN2_ALARM_PCN_CALL,
                                                    mMaskPcnCall);

                                            editor.putBoolean(ConfiguratorActivity.CONFIG_PREFERENCES_IN2_ALARM_OWN1_SMS,
                                                    mMaskOwn1Sms);
                                            editor.putBoolean(ConfiguratorActivity.CONFIG_PREFERENCES_IN2_ALARM_OWN1_CALL,
                                                    mMaskOwn1Call);

                                            editor.putBoolean(ConfiguratorActivity.CONFIG_PREFERENCES_IN2_ALARM_OWN2_SMS,
                                                    mMaskOwn2Sms);
                                            editor.putBoolean(ConfiguratorActivity.CONFIG_PREFERENCES_IN2_ALARM_OWN2_CALL,
                                                    mMaskOwn2Call);
                                        }
                                        break;
                                    case 'I':
                                        editor.putBoolean(ConfiguratorActivity.CONFIG_PREFERENCES_PWR_NOBAT_ALARM_PCN_SMS,
                                                mMaskPcnSms);
                                        editor.putBoolean(ConfiguratorActivity.CONFIG_PREFERENCES_PWR_NOBAT_ALARM_PCN_CALL,
                                                mMaskPcnCall);
                                        editor.putBoolean(ConfiguratorActivity.CONFIG_PREFERENCES_PWR_NOBAT_ALARM_OWN1_SMS,
                                                mMaskOwn1Sms);
                                        editor.putBoolean(ConfiguratorActivity.CONFIG_PREFERENCES_PWR_NOBAT_ALARM_OWN1_CALL,
                                                mMaskOwn1Call);

                                        editor.putBoolean(ConfiguratorActivity.CONFIG_PREFERENCES_PWR_NOBAT_ALARM_OWN2_SMS,
                                                mMaskOwn2Sms);
                                        editor.putBoolean(ConfiguratorActivity.CONFIG_PREFERENCES_PWR_NOBAT_ALARM_OWN2_CALL,
                                                mMaskOwn2Call);
                                        break;
                                    case 'J':
                                        if (mReceivedDataStar.length >= 3) {
                                            editor.putBoolean(ConfiguratorActivity.CONFIG_PREFERENCES_PWR_LOBAT_ENABLE,
                                                    !mReceivedDataStar[2].contentEquals("000"));

                                            editor.putInt(ConfiguratorActivity.CONFIG_PREFERENCES_PWR_LOBAT_LEVEL,
                                                    Integer.valueOf(mReceivedDataStar[0]));
                                            editor.putInt(ConfiguratorActivity.CONFIG_PREFERENCES_PWR_LOBAT_TIME,
                                                    setLowbatTime(Integer.valueOf(mReceivedDataStar[1])));

                                            editor.putBoolean(ConfiguratorActivity.CONFIG_PREFERENCES_PWR_LOBAT_ALARM_PCN_SMS,
                                                    mMaskPcnSms);
                                            editor.putBoolean(ConfiguratorActivity.CONFIG_PREFERENCES_PWR_LOBAT_ALARM_PCN_CALL,
                                                    mMaskPcnCall);

                                            editor.putBoolean(ConfiguratorActivity.CONFIG_PREFERENCES_PWR_LOBAT_ALARM_OWN1_SMS,
                                                    mMaskOwn1Sms);
                                            editor.putBoolean(ConfiguratorActivity.CONFIG_PREFERENCES_PWR_LOBAT_ALARM_OWN1_CALL,
                                                    mMaskOwn1Call);


                                            editor.putBoolean(ConfiguratorActivity.CONFIG_PREFERENCES_PWR_LOBAT_ALARM_OWN2_SMS,
                                                    mMaskOwn2Sms);
                                            editor.putBoolean(ConfiguratorActivity.CONFIG_PREFERENCES_PWR_LOBAT_ALARM_OWN2_CALL,
                                                    mMaskOwn2Call);

                                        }
                                        break;
                                    case 'K':
                                        if (mReceivedDataStar.length >= 4) {
                                            editor.putBoolean(ConfiguratorActivity.CONFIG_PREFERENCES_ACC_ENABLE,
                                                    !mReceivedDataStar[2].contentEquals("000"));

                                            editor.putInt(ConfiguratorActivity.CONFIG_PREFERENCES_ACC_SENS,
                                                    setAccSens(Integer.valueOf(mReceivedDataStar[1])));
                                            editor.putInt(ConfiguratorActivity.CONFIG_PREFERENCES_ACC_TIME,
                                                    setAccTime(Integer.valueOf(mReceivedDataStar[2])));


                                            editor.putBoolean(ConfiguratorActivity.CONFIG_PREFERENCES_ACC_ACTION_HORN,
                                                    Integer.valueOf(mReceivedDataStar[0]) % 2 == 1);

                                            editor.putBoolean(ConfiguratorActivity.CONFIG_PREFERENCES_ACC_ACTION_BLOCK,
                                                    Integer.valueOf(mReceivedDataStar[0]) >= 2);

                                            editor.putBoolean(ConfiguratorActivity.CONFIG_PREFERENCES_ACC_ALARM_PCN_SMS,
                                                    mMaskPcnSms);
                                            editor.putBoolean(ConfiguratorActivity.CONFIG_PREFERENCES_ACC_ALARM_PCN_CALL,
                                                    mMaskPcnCall);

                                            editor.putBoolean(ConfiguratorActivity.CONFIG_PREFERENCES_ACC_ALARM_OWN1_SMS,
                                                    mMaskOwn1Sms);
                                            editor.putBoolean(ConfiguratorActivity.CONFIG_PREFERENCES_ACC_ALARM_OWN1_CALL,
                                                    mMaskOwn1Call);

                                            editor.putBoolean(ConfiguratorActivity.CONFIG_PREFERENCES_ACC_ALARM_OWN2_SMS,
                                                    mMaskOwn2Sms);
                                            editor.putBoolean(ConfiguratorActivity.CONFIG_PREFERENCES_ACC_ALARM_OWN2_CALL,
                                                    mMaskOwn2Call);
                                        }
                                        break;
                                    case 'L':
                                        if (mReceivedDataStar.length >= 2) {
                                            editor.putBoolean(ConfiguratorActivity.CONFIG_PREFERENCES_JAMM_ENABLE,
                                                    !mReceivedDataStar[1].contentEquals("000"));

                                            editor.putBoolean(ConfiguratorActivity.CONFIG_PREFERENCES_JAMM_ACTION_HORN,
                                                    Integer.valueOf(mReceivedDataStar[0]) % 2 == 1);

                                            editor.putBoolean(ConfiguratorActivity.CONFIG_PREFERENCES_JAMM_ACTION_BLOCK,
                                                    Integer.valueOf(mReceivedDataStar[0]) >= 2);

                                            editor.putBoolean(ConfiguratorActivity.CONFIG_PREFERENCES_JAMM_ALARM_PCN_SMS,
                                                    mMaskPcnSms);
                                            editor.putBoolean(ConfiguratorActivity.CONFIG_PREFERENCES_JAMM_ALARM_PCN_CALL,
                                                    mMaskPcnCall);

                                            editor.putBoolean(ConfiguratorActivity.CONFIG_PREFERENCES_JAMM_ALARM_OWN1_SMS,
                                                    mMaskOwn1Sms);
                                            editor.putBoolean(ConfiguratorActivity.CONFIG_PREFERENCES_JAMM_ALARM_OWN1_CALL,
                                                    mMaskOwn1Call);

                                            editor.putBoolean(ConfiguratorActivity.CONFIG_PREFERENCES_JAMM_ALARM_OWN2_SMS,
                                                    mMaskOwn2Sms);
                                            editor.putBoolean(ConfiguratorActivity.CONFIG_PREFERENCES_JAMM_ALARM_OWN2_CALL,
                                                    mMaskOwn2Call);
                                        }
                                        break;
                                    case 'M':
                                        editor.putBoolean(ConfiguratorActivity.CONFIG_PREFERENCES_PWR_NOBAT_ALARM_PCN_SMS,
                                                mMaskPcnSms);
                                        editor.putBoolean(ConfiguratorActivity.CONFIG_PREFERENCES_PWR_NOBAT_ALARM_PCN_CALL,
                                                mMaskPcnCall);

                                        editor.putBoolean(ConfiguratorActivity.CONFIG_PREFERENCES_PWR_NOBAT_ALARM_OWN1_SMS,
                                                mMaskOwn1Sms);
                                        editor.putBoolean(ConfiguratorActivity.CONFIG_PREFERENCES_PWR_NOBAT_ALARM_OWN1_CALL,
                                                mMaskOwn1Call);

                                        editor.putBoolean(ConfiguratorActivity.CONFIG_PREFERENCES_PWR_NOBAT_ALARM_OWN2_SMS,
                                                mMaskOwn2Sms);
                                        editor.putBoolean(ConfiguratorActivity.CONFIG_PREFERENCES_PWR_NOBAT_ALARM_OWN2_CALL,
                                                mMaskOwn2Call);
                                        break;
                                    case 'N':
                                        editor.putBoolean(ConfiguratorActivity.CONFIG_PREFERENCES_GUARD_ACTION_HORN,
                                                Integer.valueOf(mReceivedDataStar[0]) % 2 == 1);

                                        editor.putBoolean(ConfiguratorActivity.CONFIG_PREFERENCES_GUARD_ACTION_BLOCK,
                                                Integer.valueOf(mReceivedDataStar[0]) >= 2);

                                        editor.putBoolean(ConfiguratorActivity.CONFIG_PREFERENCES_GUARD_ALARM_PCN_SMS,
                                                mMaskPcnSms);
                                        editor.putBoolean(ConfiguratorActivity.CONFIG_PREFERENCES_GUARD_ALARM_PCN_CALL,
                                                mMaskPcnCall);

                                        editor.putBoolean(ConfiguratorActivity.CONFIG_PREFERENCES_GUARD_ALARM_OWN1_SMS,
                                                mMaskOwn1Sms);
                                        editor.putBoolean(ConfiguratorActivity.CONFIG_PREFERENCES_GUARD_ALARM_OWN1_CALL,
                                                mMaskOwn1Call);

                                        editor.putBoolean(ConfiguratorActivity.CONFIG_PREFERENCES_GUARD_ALARM_OWN2_SMS,
                                                mMaskOwn2Sms);
                                        editor.putBoolean(ConfiguratorActivity.CONFIG_PREFERENCES_GUARD_ALARM_OWN2_CALL,
                                                mMaskOwn2Call);

                                    break;
                                    case 'O':
                                        editor.putInt(ConfiguratorActivity.CONFIG_PREFERENCES_TIME_ZONE,
                                                Integer.valueOf(mReceivedDataStar[0]));
                                        break;
                                    case 'P':
                                        editor.putInt(ConfiguratorActivity.CONFIG_PREFERENCES_USSD_INTERVAL,
                                                getUssdInterval(Integer.valueOf(mReceivedDataStar[0])));
                                        editor.putString(ConfiguratorActivity.CONFIG_PREFERENCES_USSD_PHONE,
                                                string.substring(mReceivedDataStar[0].length()+1) + "#");
                                        break;
                                    case 'R':
                                        editor.putInt(ConfiguratorActivity.CONFIG_PREFERENCES_UGON_DISTANCE,
                                                getUgonDistance(Integer.valueOf(mReceivedDataStar[0])));
                                        break;
                                    case 'V':
                                        break;
                                    default:
                                        break;
                                }

                            }
                            editor.apply();
                            return true;
                        }
                    }
                }
                cur.close();
            }
            catch (Exception ex) {
                Log.d("SQLiteException", ex.getMessage());
            }
        }
        return false;
    }

    public int getLastMessageId(){
        final String SMS_URI_INBOX = "content://sms/inbox";
        if (!mDevicePhoneNumber.isEmpty()) {
            try {
                String mDevicePhoneNumberRequest = "address='" + mDevicePhoneNumber + "'";
                String[] projection = new String[]{"_id", "address", "person", "body", "date", "type"};
                Cursor cur = getContentResolver().query(Uri.parse(SMS_URI_INBOX),
                        projection,
                        mDevicePhoneNumberRequest,
                        null,
                        "date desc");
                //startManagingCursor(cur);

                if (cur.moveToFirst()) return cur.getInt(cur.getColumnIndex("_id"));
                cur.close();
            }
            catch (Exception ex) {
                Log.d("SQLiteException", ex.getMessage());
            }
        }
        return 0;
    }

    public void onBackBtnPressed(View view) {
        onBackPressed();
    }
}
