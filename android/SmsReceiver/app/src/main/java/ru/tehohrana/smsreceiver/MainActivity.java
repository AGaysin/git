package ru.tehohrana.smsreceiver;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.ActionBarActivity;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Pattern;

import ru.tehohrana.smsreceiver.service.DatabaseHelper;
import ru.tehohrana.smsreceiver.service.MessagingService;


public class MainActivity extends Activity {

    private static long back_pressed;
    // Реализация задержки ожидания опроса состояния
    private static long timerSmsCommandWaiting;
    private Handler handler = new Handler();
    private Runnable task = new Runnable() {
        @Override
        public void run() {
            if (timerSmsCommandWaiting > 0)timerSmsCommandWaiting--;
            else setAllButtonsEnabled();
            handler.postDelayed(this,1000);
        }
    };

    private ImageButton mImgBtnSendGet, mImgBtnSendMic, mImgBtnSendGuardOn, mImgBtnSendGuardOff,
            mImgBtnSendHorn, mImgBtnSendBlock;

    private TextView mTextViewObjectName;
    private LinearLayout mMainWindow;
    private LinearLayout mLinearLayoutChangePhoneNumTextView;


    public double gpsLatitude, gpsLongitude;
    public String mDevicePhoneNumber="";
    public boolean isDevicePhoneNumberExist = false;
    public boolean isDevicePhoneNumberRight = false;

    private BroadcastReceiver receiver;
    private boolean isDoubleClickExit = false;

    DatabaseHelper mDatabaseHelper;
    SQLiteDatabase mSqLiteDatabase;

    public SharedPreferences mSettings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

//        SlidingMenu menu = new SlidingMenu(this);
//        menu.setMode(SlidingMenu.LEFT);
//        menu.setTouchModeAbove(SlidingMenu.TOUCHMODE_FULLSCREEN);
//        menu.setFadeDegree(0.35f);
//        menu.attachToActivity(this, SlidingMenu.SLIDING_WINDOW);
//        menu.setMenu(R.layout.slidemenu);
//        menu.setBehindWidthRes(R.dimen.slidingmenu_behind_width);

        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String s = intent.getStringExtra(MessagingService.COPA_MESSAGE);
                if (s.equals("UpdateDb")) {
                    updateViews();
                }
                // do something here.
            }
        };


        //Slide menu

//        mTitle = mDrawerTitle = getTitle();
//        mScreenTitles = getResources().getStringArray(R.array.screen_array);
//        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
//        mDrawerList = (ListView) findViewById(R.id.left_drawer);
//
//        // Set the adapter for the list view
//        mDrawerList.setAdapter(new ArrayAdapter<String>(this,
//                R.layout.drawer_list_item, mScreenTitles));

        // Set the list's click listener
        //mDrawerList.setOnItemClickListener(new DrawerItemClickListener());

//        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
//        getSupportActionBar().setHomeButtonEnabled(true);
//
//        mDrawerToggle = new ActionBarDrawerToggle(
//                this, /* host Activity */
//                mDrawerLayout, /* DrawerLayout object */
//                R.drawable.icon_control_on, /* nav drawer icon to replace 'Up' caret */
//                R.string.drawer_open, /* "open drawer" description */
//                R.string.drawer_close /* "close drawer" description */
//        ) {
//
//            /** Called when a drawer has settled in a completely closed state. */
//            public void onDrawerClosed(View view) {
//                getSupportActionBar().setTitle(mTitle);
//                supportInvalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
//            }
//
//            /** Called when a drawer has settled in a completely open state. */
//            public void onDrawerOpened(View drawerView) {
//                getSupportActionBar().setTitle(mDrawerTitle);
//                supportInvalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
//            }
//        };

        // Set the drawer toggle as the DrawerListener
        //mDrawerLayout.setDrawerListener(mDrawerToggle);

        //Slide menu end


        mSettings = PreferenceManager.getDefaultSharedPreferences(this);

        mImgBtnSendGet = (ImageButton)findViewById(R.id.imageButtonSendGet);
        mImgBtnSendMic = (ImageButton)findViewById(R.id.imageButtonSendMic);
        mImgBtnSendGuardOn = (ImageButton)findViewById(R.id.imageButtonSendGuardOn);
        mImgBtnSendGuardOff = (ImageButton)findViewById(R.id.imageButtonSendGuardOff);
        mImgBtnSendHorn = (ImageButton)findViewById(R.id.imageButtonSendHornOn);
        mImgBtnSendBlock = (ImageButton)findViewById(R.id.imageButtonSendBlockOn);
        mMainWindow = (LinearLayout)findViewById(R.id.linearLayoutMain);
        mLinearLayoutChangePhoneNumTextView = (LinearLayout)findViewById(R.id.set_device_phone_number);


        mTextViewObjectName = (TextView)findViewById(R.id.textViewObjectName);


        //Применение настроек при изменении
        SharedPreferences.OnSharedPreferenceChangeListener prefListener =
                new SharedPreferences.OnSharedPreferenceChangeListener() {
                    public void onSharedPreferenceChanged(SharedPreferences prefs,
                                                          String key) {
                        if (key.equals("pref_phone")) {
                            //Проверяем правильность введения номера телефона оборудования
                            mDevicePhoneNumber = prefs.getString("pref_phone","");

                            if (mDevicePhoneNumber.startsWith("+") && mDevicePhoneNumber.length()==12)
                            {
                                isDevicePhoneNumberRight = true;
                                mMainWindow.setVisibility(View.VISIBLE);
                                mLinearLayoutChangePhoneNumTextView.setVisibility(View.GONE);
                            }
                            else
                            {
                                isDevicePhoneNumberRight = false;
                                mMainWindow.setVisibility(View.GONE);
                                mLinearLayoutChangePhoneNumTextView.setVisibility(View.VISIBLE);
                            }
                        }
                        if (key.equals("key_exit")) isDoubleClickExit = prefs.getBoolean("key_exit", false);
                        if (key.equals("pref_name")) mTextViewObjectName.setText(mSettings.getString("pref_name",""));
                    }
                };



        mSettings.registerOnSharedPreferenceChangeListener(prefListener);

        mDevicePhoneNumber = mSettings.getString("pref_phone", "");
        mTextViewObjectName.setText(mSettings.getString("pref_name",""));
        isDoubleClickExit = mSettings.getBoolean("key_exit",false);
        if (mDevicePhoneNumber.startsWith("+") && mDevicePhoneNumber.length()==12)
        {
            isDevicePhoneNumberRight = true;
            mMainWindow.setVisibility(View.VISIBLE);
            mLinearLayoutChangePhoneNumTextView.setVisibility(View.GONE);
        }
        else
        {
            isDevicePhoneNumberRight = false;
            mMainWindow.setVisibility(View.GONE);
            mLinearLayoutChangePhoneNumTextView.setVisibility(View.VISIBLE);
        }
        //Чтение всех СМС из приложения, добавление в БД

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
                int mLastIdDb = getLastIdDb();

                if (cur.moveToFirst()) {
                    isDevicePhoneNumberExist = true;


                    //Записываем все незаписанные события в базу данных До mLastId
                    if (mLastIdDb > 0) {
                        //Перебираем события до последнего, которое не записано в базе данных
                        while (cur.getInt(cur.getColumnIndex("_id")) > mLastIdDb && !cur.isLast()) {
                            cur.moveToNext();
                        }

                    } else
                    {
                        //Если в базе нет записей - записываем последние 100 записей чтобы не перегружать
                        for (int i=0; i<100; i++) cur.moveToNext();
                    }

                    while (!cur.isFirst()) {
                        cur.moveToPrevious();
                        if (cur.getInt(cur.getColumnIndex("_id")) > mLastIdDb &&
                                !cur.getString(cur.getColumnIndex("body")).contains("SET")) {
                            putSmsToDataBase(cur.getInt(cur.getColumnIndex("_id")),
                                    cur.getString(cur.getColumnIndex("address")),
                                    cur.getString(cur.getColumnIndex("body")),
                                    cur.getLong(cur.getColumnIndex("date")));
                        }
                    }
                }
                cur.close();
            }catch (Exception ex) {
                Log.d("SQLiteException", ex.getMessage());
            }
        }



        //Отображаем последнее событие на экране
        updateViews();

//        FloatingActionButton fabButton = new FloatingActionButton.Builder(this)
//                .withDrawable(getResources().getDrawable(R.drawable.car_locked))
//                .withButtonColor(Color.BLUE)
//                .withGravity(Gravity.CLIP_VERTICAL | Gravity.RIGHT)
//                .withMargins(0, 0, 16, 16)
//                .create();


    }


    @Override
    public void onStart() {

        SharedPreferences.Editor editor = mSettings.edit();
        editor.putBoolean(MessagingService.IS_APP_RUNNING, true);
        editor.apply();
        LocalBroadcastManager.getInstance(this).registerReceiver((receiver),
                new IntentFilter(MessagingService.COPA_RESULT)
        );
        super.onStart();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Toast.makeText(this, "New INTENT STARTED", Toast.LENGTH_LONG).show();
    }

    @Override
    public void onStop() {

        SharedPreferences.Editor editor = mSettings.edit();
        editor.putBoolean(MessagingService.IS_APP_RUNNING, false);
        editor.apply();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(receiver);
        super.onStop();

    }

    @Override
    protected void onResume() {
        super.onResume();

        handler.postDelayed(task, 1000);
    }

    protected void onPause() {
        super.onPause();
        handler.removeCallbacks(task);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }

    // у атрибута пункта меню Settings установлено значение android:onClick="onSettingsMenuClick"
    public void onSettingsMenuClick(MenuItem item) {
        Intent i = new Intent(this, Prefs.class);
        startActivity(i);
    }

    public void onBtnSettingsHiddenClick(View view) {
        Intent i = new Intent(this, Prefs.class);
        startActivity(i);
    }

//    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
//        @Override
//        public void onReceive(Context context, Intent intent) {
//            updateArchieve();
//            updateViews();
//        }
//    };

    @Override
    public void onBackPressed() {
        if(isDoubleClickExit) {
            if (back_pressed + 2000 > System.currentTimeMillis())
                super.onBackPressed();
            else
                Toast.makeText(getBaseContext(), "Нажмите дважды для выхода",
                        Toast.LENGTH_SHORT).show();
            back_pressed = System.currentTimeMillis();
        } else super.onBackPressed();
    }




    public void onAboutMenuClick(MenuItem item) {
        Intent intent = new Intent(MainActivity.this, AboutActivity.class);
        startActivity(intent);
    }




    public boolean onSmsListUpdate(Intent intent)    {
        try {
            Object[] pduArray = (Object[]) intent.getExtras().get("pdus");
            android.telephony.gsm.SmsMessage[] messages = new android.telephony.gsm.SmsMessage[pduArray.length];
            for (int i = 0; i < pduArray.length; i++) {
                messages[i] = android.telephony.gsm.SmsMessage.createFromPdu((byte[]) pduArray[i]);
            }

            String sms_from = messages[0].getDisplayOriginatingAddress();
            if (sms_from.equalsIgnoreCase(mDevicePhoneNumber)) return true;
        }
        catch (Exception ex)
        {
            Toast.makeText(this, "Exception: "+ex.toString(), Toast.LENGTH_LONG).show();
        }
        return false;

    }


    private void sendSMS(String phoneNumber, String message)    {
        String SENT="SMS_SENT";
        String DELIVERED="SMS_DELIVERED";

        PendingIntent sentPI= PendingIntent.getBroadcast(this,0,
                new Intent(SENT),0);

        PendingIntent deliveredPI= PendingIntent.getBroadcast(this,0,
                new Intent(DELIVERED),0);

        //---когда SMS отправлено---
        registerReceiver(new BroadcastReceiver(){
            @Override
            public void onReceive(Context arg0, Intent arg1){
                switch(getResultCode())
                {
                    case Activity.RESULT_OK:
                        Toast.makeText(getBaseContext(),"SMS Отправлено",
                                Toast.LENGTH_SHORT).show();
                        break;
                    case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
                        Toast.makeText(getBaseContext(),"Ошибка отправки",
                                Toast.LENGTH_SHORT).show();
                        break;
                    case SmsManager.RESULT_ERROR_NO_SERVICE:
                        Toast.makeText(getBaseContext(),"Сервис недоступен",
                                Toast.LENGTH_SHORT).show();
                        break;
                    case SmsManager.RESULT_ERROR_NULL_PDU:
                        Toast.makeText(getBaseContext(),"Null PDU",
                                Toast.LENGTH_SHORT).show();
                        break;
                    case SmsManager.RESULT_ERROR_RADIO_OFF:
                        Toast.makeText(getBaseContext(),"Модем выключен",
                                Toast.LENGTH_SHORT).show();
                        break;

                }
                unregisterReceiver(this);
            }
        },new IntentFilter(SENT));

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

    public void onBtnSendGetClick(View view) {

        if (timerSmsCommandWaiting > 0)
            Toast.makeText(getBaseContext(), "Отправлена SMS команда запроса состояния, ждите ответа ("
                            + (timerSmsCommandWaiting)
                            + " s)!",
                    Toast.LENGTH_SHORT).show();
        else {

            AlertDialog.Builder sendSmsIdGetDialog = new AlertDialog.Builder(this);
            sendSmsIdGetDialog.setTitle("\"Будет отправлена SMS-команда 'ОПРОС СОСТОЯНИЯ'\"");

            sendSmsIdGetDialog.setPositiveButton("Да", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {


                    // отправляем сообщение
                    //String sent = "android.telephony.SmsManager.STATUS_ON_ICC_SENT";
                    // PendingIntent piSent = PendingIntent.getBroadcast(MainActivity.this, 0,new Intent(sent), 0);
                    sendSMS(mDevicePhoneNumber, "GPS");
                    timerSmsCommandWaiting = 10;
                    setAllButtonsDisabled();
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

    }

    public void onBtnSendMicClick(View view) {

        if (timerSmsCommandWaiting > 0)
            Toast.makeText(getBaseContext(), "Отправлена SMS команда запроса состояния, ждите ответа ("
                            + (timerSmsCommandWaiting)
                            + " s)!",
                    Toast.LENGTH_SHORT).show();
        else {
            AlertDialog.Builder sendSmsIdGetDialog = new AlertDialog.Builder(this);
            sendSmsIdGetDialog.setTitle("Будет отправлена SMS-команда 'АУДИО-КОНТРОЛЬ'");
            sendSmsIdGetDialog.setPositiveButton("Да", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    // отправляем сообщение
                    sendSMS(mDevicePhoneNumber, "MIC");
                    //Показать уведомление
                    Toast.makeText(getBaseContext(), "Отправлена SMS команда запроса состояния, ждите ответа!",
                            Toast.LENGTH_SHORT).show();
                    timerSmsCommandWaiting = 10;
                    setAllButtonsDisabled();
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


    }

    public void onBtnSendGuardOnClick(View view) {

        if (timerSmsCommandWaiting > 0)
            Toast.makeText(getBaseContext(), "Отправлена SMS команда запроса состояния, ждите ответа ("
                            + (timerSmsCommandWaiting)
                            + " s)!",
                    Toast.LENGTH_SHORT).show();
        else {
            AlertDialog.Builder sendSmsIdGetDialog = new AlertDialog.Builder(this);
            sendSmsIdGetDialog.setTitle("Будет отправлена SMS-команда 'ВЗЯТЬ ПОД ОХРАНУ'");
            sendSmsIdGetDialog.setPositiveButton("Да", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    // отправляем сообщение
                    sendSMS(mDevicePhoneNumber, "GUARD ON");
                    //Показать уведомление
                    Toast.makeText(getBaseContext(), "Отправлена SMS команда запроса состояния, ждите ответа!",
                            Toast.LENGTH_SHORT).show();
                    timerSmsCommandWaiting = 10;
                    setAllButtonsDisabled();
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
    }

    public void onBtnSendGuardOffClick(View view) {


        if (timerSmsCommandWaiting > 0)
            Toast.makeText(getBaseContext(), "Отправлена SMS команда запроса состояния, ждите ответа ("
                            + (timerSmsCommandWaiting)
                            + " s)!",
                    Toast.LENGTH_SHORT).show();
        else {
            AlertDialog.Builder sendSmsIdGetDialog = new AlertDialog.Builder(this);
            sendSmsIdGetDialog.setTitle("Будет отправлена SMS-команда 'СНЯТЬ С ОХРАНЫ'");
            sendSmsIdGetDialog.setPositiveButton("Да", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    // отправляем сообщение
                    sendSMS(mDevicePhoneNumber, "GUARD OFF");
                    //Показать уведомление
                    Toast.makeText(getBaseContext(), "Отправлена SMS команда запроса состояния, ждите ответа!",
                            Toast.LENGTH_SHORT).show();
                    timerSmsCommandWaiting = 10;
                    setAllButtonsDisabled();
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
    }
    public void onBtnBluetoothModeClick(View view) {
        if (timerSmsCommandWaiting > 0)
            Toast.makeText(getBaseContext(), "Отправлена SMS команда запроса состояния, ждите ответа ("
                            + (timerSmsCommandWaiting)
                            + " s)!",
                    Toast.LENGTH_SHORT).show();
        else {
            AlertDialog.Builder sendSmsIdGetDialog = new AlertDialog.Builder(this);
            sendSmsIdGetDialog.setTitle("Будет отправлена SMS-команда 'АКТИВАЦИЯ Bluetooth'");
            sendSmsIdGetDialog.setPositiveButton("Да", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    // отправляем сообщение
                    sendSMS(mDevicePhoneNumber, "G BT ON");
                    //Показать уведомление
                    Toast.makeText(getBaseContext(), "Отправлена SMS команда запроса состояния, ждите ответа!",
                            Toast.LENGTH_SHORT).show();
                    timerSmsCommandWaiting = 10;
                    setAllButtonsDisabled();
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
    }
    public void onBtnSendHornOnClick(View view) {

        if (timerSmsCommandWaiting > 0)
            Toast.makeText(getBaseContext(), "Отправлена SMS команда запроса состояния, ждите ответа ("
                            + (timerSmsCommandWaiting)
                            + " s)!",
                    Toast.LENGTH_SHORT).show();
        else {
            AlertDialog.Builder sendSmsIdGetDialog = new AlertDialog.Builder(this);
            sendSmsIdGetDialog.setTitle("Будет отправлена SMS-команда 'УПРАВЛЕНИЕ СИРЕНОЙ'");
            sendSmsIdGetDialog.setPositiveButton("ВКЛЮЧИТЬ", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    // отправляем сообщение
                    sendSMS(mDevicePhoneNumber, "HORN ON");
                    //Показать уведомление
                    Toast.makeText(getBaseContext(), "Отправлена SMS команда запроса состояния, ждите ответа!",
                            Toast.LENGTH_SHORT).show();
                    timerSmsCommandWaiting = 10;
                    setAllButtonsDisabled();
                }
            });
            sendSmsIdGetDialog.setNeutralButton("ВЫКЛЮЧИТЬ", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    // отправляем сообщение
                    sendSMS(mDevicePhoneNumber, "HORN OFF");
                    //Показать уведомление
                    Toast.makeText(getBaseContext(), "Отправлена SMS команда запроса состояния, ждите ответа!",
                            Toast.LENGTH_SHORT).show();
                    timerSmsCommandWaiting = 10;
                    setAllButtonsDisabled();
                }
            });
            sendSmsIdGetDialog.setNegativeButton("ОТМЕНА", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    //нет действий
                }
            });
            sendSmsIdGetDialog.show();
        }
    }

    public void onBtnSendBlockOnClick(View view) {


        if (timerSmsCommandWaiting > 0)
            Toast.makeText(getBaseContext(), "Отправлена SMS команда запроса состояния, ждите ответа ("
                            + (timerSmsCommandWaiting)
                            + " s)!",
                    Toast.LENGTH_SHORT).show();
        else {
            AlertDialog.Builder sendSmsIdGetDialog = new AlertDialog.Builder(this);
            sendSmsIdGetDialog.setTitle("Будет отправлена SMS-команда 'ЗАБЛОКИРОВАТЬ ДВИГАТЕЛЬ'");
            sendSmsIdGetDialog.setPositiveButton("ЗАБЛОКИРОВАТЬ", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    // отправляем сообщение
                    sendSMS(mDevicePhoneNumber,"BLOCK");
                    //Показать уведомление
                    Toast.makeText(getBaseContext(), "Отправлена SMS команда запроса состояния, ждите ответа!",
                            Toast.LENGTH_SHORT).show();
                    timerSmsCommandWaiting = 10;
                    setAllButtonsDisabled();
                }
            });
            sendSmsIdGetDialog.setNeutralButton("РАЗБЛОКИРОВАТЬ", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    // отправляем сообщение
                    sendSMS(mDevicePhoneNumber,"GUARD OFF");
                    //Показать уведомление
                    Toast.makeText(getBaseContext(), "Отправлена SMS команда запроса состояния, ждите ответа!",
                            Toast.LENGTH_SHORT).show();
                    timerSmsCommandWaiting = 10;
                    setAllButtonsDisabled();
                }
            });
            sendSmsIdGetDialog.setNegativeButton("ОТМЕНА", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    //нет действий
                }
            });
            sendSmsIdGetDialog.show();
        }
    }

    public void onBtnValetModeClick(View view) {
        if (timerSmsCommandWaiting > 0)
            Toast.makeText(getBaseContext(), "Отправлена SMS команда запроса состояния, ждите ответа ("
                            + (timerSmsCommandWaiting)
                            + " s)!",
                    Toast.LENGTH_SHORT).show();
        else {
            AlertDialog.Builder sendSmsIdGetDialog = new AlertDialog.Builder(this);
            sendSmsIdGetDialog.setTitle("Будет отправлена SMS-команда 'РЕЖИМ VALET'");
            sendSmsIdGetDialog.setPositiveButton("АКТИВИРОВАТЬ", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    // отправляем сообщение
                    sendSMS(mDevicePhoneNumber, "VALET ON");
                    //Показать уведомление
                    Toast.makeText(getBaseContext(), "Отправлена SMS команда запроса состояния, ждите ответа!",
                            Toast.LENGTH_SHORT).show();
                    timerSmsCommandWaiting = 10;
                    setAllButtonsDisabled();
                }
            });
            sendSmsIdGetDialog.setNeutralButton("ДЕАКТИВИРОВАТЬ", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    // отправляем сообщение
                    sendSMS(mDevicePhoneNumber, "VALET OFF");
                    //Показать уведомление
                    Toast.makeText(getBaseContext(), "Отправлена SMS команда запроса состояния, ждите ответа!",
                            Toast.LENGTH_SHORT).show();
                    timerSmsCommandWaiting = 10;
                    setAllButtonsDisabled();
                }
            });
            sendSmsIdGetDialog.setNegativeButton("ОТМЕНА", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    //нет действий
                }
            });
            sendSmsIdGetDialog.show();
        }
    }


    public void setAllButtonsDisabled()    {
        mImgBtnSendGet.setEnabled(false);
        mImgBtnSendMic.setEnabled(false);
        mImgBtnSendGuardOn.setEnabled(false);
        mImgBtnSendGuardOff.setEnabled(false);
        mImgBtnSendHorn.setEnabled(false);
        mImgBtnSendBlock.setEnabled(false);
    }

    public void setAllButtonsEnabled()    {
        mImgBtnSendGet.setEnabled(true);
        mImgBtnSendMic.setEnabled(true);
        mImgBtnSendGuardOn.setEnabled(true);
        mImgBtnSendGuardOff.setEnabled(true);
        mImgBtnSendHorn.setEnabled(true);
        mImgBtnSendBlock.setEnabled(true);
    }



    public void updateViews(){

        mDatabaseHelper = new DatabaseHelper(this);
        mSqLiteDatabase = mDatabaseHelper.getWritableDatabase();


        TextView mTextViewAlarml = (TextView) findViewById(R.id.textViewAlarm);
        ImageView mImageViewAlarml = (ImageView) findViewById(R.id.imageViewAlarm);

        ImageView mImageViewStateGuard = (ImageView) findViewById(R.id.imageViewStateGuard);
        ImageView mImageButtonSendGuardOff = (ImageView) findViewById(R.id.imageButtonSendGuardOff);
        ImageView mImageButtonSendGuardOn = (ImageView) findViewById(R.id.imageButtonSendGuardOn);
        ImageView mImageButtonSendBlockOn = (ImageView) findViewById(R.id.imageButtonSendBlockOn);
        ImageView mImageButtonSendHornOn = (ImageView) findViewById(R.id.imageButtonSendHornOn);

//        ImageView mImageViewStateIn1 = (ImageView) findViewById(R.id.imageViewStateIn1);
//        ImageView mImageViewStateIn2 = (ImageView) findViewById(R.id.imageViewStateIn2);
//        ImageView mImageViewStateMove = (ImageView) findViewById(R.id.imageViewStateMove);
//        ImageView mImageViewStateEngine = (ImageView) findViewById(R.id.imageViewStateEngine);
//        ImageView mImageViewStateBat = (ImageView) findViewById(R.id.imageViewStateBat);
//        ImageView mImageViewStateJamm = (ImageView) findViewById(R.id.imageViewStateJamm);
//        ImageView mImageViewStateHorn = (ImageView) findViewById(R.id.imageViewStateHorn);
//        ImageView mImageViewStateBlock = (ImageView) findViewById(R.id.imageViewStateBlock);
//
        ImageView mImageViewScreenGsmLevel = (ImageView) findViewById(R.id.imageViewScreenGsmLevel);
//        ImageView mImageViewStateUgon = (ImageView) findViewById(R.id.imageViewStateUgon);
        ImageView mImageViewScreenBat = (ImageView) findViewById(R.id.imageViewScreenBat);

        TextView mTextViewScreenReceiver = (TextView)findViewById(R.id.textViewScreenReceiver);
        TextView mTextViewScreenReceiverDate = (TextView)findViewById(R.id.textViewScreenReceiverDate);
        TextView mTextViewScreenGmsBalance = (TextView)findViewById(R.id.textViewScreenGmsBalance);
        TextView mTextViewScreenGps = (TextView)findViewById(R.id.textViewScreenGps);

        ImageButton mImageButtonBluetoothMode = (ImageButton) findViewById(R.id.imageButtonBluetoothMode);
        ImageButton mImageButtonValetMode = (ImageButton) findViewById(R.id.imageButtonValetMode);

        TextView mTextViewBatVal = (TextView)findViewById(R.id.textViewBatVal);
        TextView mTextViewTempVal = (TextView)findViewById(R.id.textViewTempVal);
        TextView mTextViewDateVal = (TextView)findViewById(R.id.textViewDateVal);
        TextView mTextViewTimeVal = (TextView)findViewById(R.id.textViewTimeVal);
        TextView mTextViewSpeedVal = (TextView)findViewById(R.id.textViewSpeedVal);



        int mPacketId=0;
        String mTransmitter="";
        String mReceiver="";
        String mEventNum="";
        String mEventType="";
        String mDataTemp="";
        String mDataVolt="";
        String mGsmAnt="";
        String mGspTime="";
        String mGpsDate="";
        String mGpsLong="";
        String mGpsLat="";
        String mGpsSpeed="";
        Boolean mMaskIn1=false;
        Boolean mMaskIn2=false;
        Boolean mMaskHorn=false;
        Boolean mMaskBlock=false;
        Boolean mMaskGuard=false;
        Boolean mMaskMove=false;
        Boolean mMaskNobat=false;
        Boolean mMaskJamm=false;
        Boolean mMaskEngine=false;

        String mReceivedCurrentTime = "";
        String mReceivedCurrentDate = "";
        String mReceiverBalance = "";
        Boolean mMaskValet = false;
        Boolean mMaskBtGuard = false;
        Boolean mMaskUgon = false;
        Boolean mMaskLobat = false;
        int mMask3DFix = 0;

        try {
            //Открытие Базы Данных и оторажение параметров последнего события на экран
            Cursor cursor = mSqLiteDatabase.query("auto_gps", new String[]{
                            DatabaseHelper.PACKET_ID_COLUMN,
                            DatabaseHelper.TRANSMITTER_COLUMN,
                            DatabaseHelper.RECEIVER_COLUMN,
                            DatabaseHelper.EVENT_NUM_COLUMN,
                            DatabaseHelper.EVENT_TYPE_COLUMN,
                            DatabaseHelper.DATA_TEMP_COLUMN,
                            DatabaseHelper.DATA_VOLT_COLUMN,
                            DatabaseHelper.GSM_ANT_COLUMN,
                            DatabaseHelper.GPS_TIME_COLUMN,
                            DatabaseHelper.GPS_DATE_COLUMN,
                            DatabaseHelper.GPS_LONG_COLUMN,
                            DatabaseHelper.GPS_LAT_COLUMN,
                            DatabaseHelper.GPS_SPEED_COLUMN,
                            DatabaseHelper.MASK_IN1,
                            DatabaseHelper.MASK_IN2,
                            DatabaseHelper.MASK_HORN,
                            DatabaseHelper.MASK_BLOCK,
                            DatabaseHelper.MASK_GUARD,
                            DatabaseHelper.MASK_MOVE,
                            DatabaseHelper.MASK_NOBAT,
                            DatabaseHelper.MASK_JAMM,
                            DatabaseHelper.MASK_ENGINE,
                            DatabaseHelper.RECEIVED_DATE,
                            DatabaseHelper.RECEIVED_TIME,
                            DatabaseHelper.TRANSMITTER_BALANCE,
                            DatabaseHelper.MASK_VALET,
                            DatabaseHelper.MASK_BT_GUARD,
                            DatabaseHelper.MASK_UGON,
                            DatabaseHelper.MASK_LOBAT,
                            DatabaseHelper.GPS_FIX_3D,
                    },
                    null, null,
                    null, null, null);

            cursor.moveToLast();
            mReceivedCurrentTime = cursor.getString(cursor.getColumnIndex(DatabaseHelper.RECEIVED_TIME));
            mReceivedCurrentDate = cursor.getString(cursor.getColumnIndex(DatabaseHelper.RECEIVED_DATE));
            mReceiverBalance = cursor.getString(cursor.getColumnIndex(DatabaseHelper.TRANSMITTER_BALANCE));

            mMask3DFix = cursor.getInt(cursor.getColumnIndex(DatabaseHelper.GPS_FIX_3D));
            //Читаем текущие данные из базы
            mPacketId = cursor.getInt(cursor.getColumnIndex(DatabaseHelper.PACKET_ID_COLUMN));
            mTransmitter = cursor.getString(cursor.getColumnIndex(DatabaseHelper.TRANSMITTER_COLUMN));
            mReceiver = cursor.getString(cursor.getColumnIndex(DatabaseHelper.RECEIVER_COLUMN));
            mEventNum = cursor.getString(cursor.getColumnIndex(DatabaseHelper.EVENT_NUM_COLUMN));
            mDataTemp = cursor.getString(cursor.getColumnIndex(DatabaseHelper.DATA_TEMP_COLUMN));
            mDataVolt = cursor.getString(cursor.getColumnIndex(DatabaseHelper.DATA_VOLT_COLUMN));
            mGsmAnt = cursor.getString(cursor.getColumnIndex(DatabaseHelper.GSM_ANT_COLUMN));
            mGspTime = cursor.getString(cursor.getColumnIndex(DatabaseHelper.GPS_TIME_COLUMN));
            mGpsDate = cursor.getString(cursor.getColumnIndex(DatabaseHelper.GPS_DATE_COLUMN));
            mGpsLong = cursor.getString(cursor.getColumnIndex(DatabaseHelper.GPS_LONG_COLUMN));
            mGpsLat = cursor.getString(cursor.getColumnIndex(DatabaseHelper.GPS_LAT_COLUMN));
            mGpsSpeed = cursor.getString(cursor.getColumnIndex(DatabaseHelper.GPS_SPEED_COLUMN));

            mMaskIn1 = cursor.getInt(cursor.getColumnIndex(DatabaseHelper.MASK_IN1)) == 1;
            mMaskIn2 = cursor.getInt(cursor.getColumnIndex(DatabaseHelper.MASK_IN2)) == 1;
            mMaskHorn = cursor.getInt(cursor.getColumnIndex(DatabaseHelper.MASK_HORN)) == 1;
            mMaskBlock = cursor.getInt(cursor.getColumnIndex(DatabaseHelper.MASK_BLOCK)) == 1;
            mMaskGuard = cursor.getInt(cursor.getColumnIndex(DatabaseHelper.MASK_GUARD)) == 1;
            mMaskMove = cursor.getInt(cursor.getColumnIndex(DatabaseHelper.MASK_MOVE)) == 1;
            mMaskNobat = cursor.getInt(cursor.getColumnIndex(DatabaseHelper.MASK_NOBAT)) == 1;
            mMaskJamm = cursor.getInt(cursor.getColumnIndex(DatabaseHelper.MASK_JAMM)) == 1;
            mMaskEngine = cursor.getInt(cursor.getColumnIndex(DatabaseHelper.MASK_ENGINE)) == 1;

            mMaskValet = cursor.getInt(cursor.getColumnIndex(DatabaseHelper.MASK_VALET)) == 1;
            mMaskBtGuard = cursor.getInt(cursor.getColumnIndex(DatabaseHelper.MASK_BT_GUARD)) == 1;
            mMaskUgon = cursor.getInt(cursor.getColumnIndex(DatabaseHelper.MASK_UGON)) == 1;
            mMaskLobat = cursor.getInt(cursor.getColumnIndex(DatabaseHelper.MASK_LOBAT)) == 1;

            //Отображаем последнее состояние на главной активности
            switch (cursor.getString(cursor.getColumnIndex(DatabaseHelper.EVENT_TYPE_COLUMN))) {
                case "IN1":
                    mTextViewAlarml.setText(mSettings.getString("pref_in1", "Тревога. Зона ШС1"));
                    mTextViewAlarml.setTextColor(getResources().getColor(R.color.red));
                    mImageViewAlarml.setImageBitmap(BitmapFactory.decodeResource(
                            this.getResources(), R.drawable.in1_alarm));
                    break;
                case "IN2":
                    mTextViewAlarml.setText(mSettings.getString("pref_in2", "Тревога. Зона ШС2"));
                    mTextViewAlarml.setTextColor(getResources().getColor(R.color.red));
                    mImageViewAlarml.setImageBitmap(BitmapFactory.decodeResource(
                            this.getResources(), R.drawable.in2_alarm));
                    break;
                case "NET AKB":
                case "NO BAT":
                case "NOBAT":
                    mTextViewAlarml.setText("Тревога. Отключен аккумулятор");
                    mTextViewAlarml.setTextColor(getResources().getColor(R.color.red));
                    mImageViewAlarml.setImageBitmap(BitmapFactory.decodeResource(
                            this.getResources(), R.drawable.battery_alarm));
                    break;
                case "UDAR":
                case "MOVE":
                    mTextViewAlarml.setText("Тревога. Сработка датчика акселерометра");
                    mTextViewAlarml.setTextColor(getResources().getColor(R.color.red));
                    mImageViewAlarml.setImageBitmap(BitmapFactory.decodeResource(
                            this.getResources(), R.drawable.accelerometer_alarm));
                    break;
                case "UGON":
                    mTextViewAlarml.setText("Тревога. Объект находится вне охраняемого периметра");
                    mTextViewAlarml.setTextColor(getResources().getColor(R.color.red));
                    mImageViewAlarml.setImageBitmap(BitmapFactory.decodeResource(
                            this.getResources(), R.drawable.mover_alarm));
                    break;
                case "ENGINE":
                    mTextViewAlarml.setText("Тревога. Двигатель заведен");
                    mTextViewAlarml.setTextColor(getResources().getColor(R.color.red));
                    mImageViewAlarml.setImageBitmap(BitmapFactory.decodeResource(
                            this.getResources(), R.drawable.engine_working_alarm));
                    break;
                case "ASK OWN1":
                    mTextViewAlarml.setText("Новое состояние. Опрос собственником 1");
                    mTextViewAlarml.setTextColor(getResources().getColor(R.color.white));
                    mImageViewAlarml.setImageBitmap(BitmapFactory.decodeResource(
                            this.getResources(), R.drawable.nullpic));
                    break;
                case "ASK OWN2":
                    mTextViewAlarml.setText("Новое состояние. Опрос собственником 2");
                    mTextViewAlarml.setTextColor(getResources().getColor(R.color.white));
                    mImageViewAlarml.setImageBitmap(BitmapFactory.decodeResource(
                            this.getResources(), R.drawable.nullpic));
                    break;
                case "JAMM":
                    mTextViewAlarml.setText("Тревога. Зафиксировано глушени GSM-сигнала");
                    mTextViewAlarml.setTextColor(getResources().getColor(R.color.red));
                    mImageViewAlarml.setImageBitmap(BitmapFactory.decodeResource(
                            this.getResources(), R.drawable.gsm_jam_alarm));
                    break;
                case "LOW BAT":
                case "LOBAT":
                case "RAZRYAD AKB":
                    mTextViewAlarml.setText("Внимание. Аккумулятор разряжен");
                    mTextViewAlarml.setTextColor(getResources().getColor(R.color.yellow));
                    mImageViewAlarml.setImageBitmap(BitmapFactory.decodeResource(
                            this.getResources(), R.drawable.battery_low));
                    break;
                case "VZYAT":
                    mTextViewAlarml.setText("Внимание. Объект взят под охрану");
                    mTextViewAlarml.setTextColor(getResources().getColor(R.color.white));
                    mImageViewAlarml.setImageBitmap(BitmapFactory.decodeResource(
                            this.getResources(), R.drawable.locked));
                    break;
                case "SNYAT":
                    mTextViewAlarml.setText("Внимание. Объект снят с охраны");
                    mTextViewAlarml.setTextColor(getResources().getColor(R.color.white));
                    mImageViewAlarml.setImageBitmap(BitmapFactory.decodeResource(
                            this.getResources(), R.drawable.unlocked));
                    break;
                case "SLEEP":
                case "PWR OFF":
                    mTextViewAlarml.setText("Выключение. Встроенный аккумулятор разряжен");
                    mTextViewAlarml.setTextColor(getResources().getColor(R.color.white));
                    mImageViewAlarml.setImageBitmap(BitmapFactory.decodeResource(
                            this.getResources(), R.drawable.nullpic));
                    break;
                case "INIT":
                case "PWR ON":
                    mTextViewAlarml.setText("Включение питания. Инициализация");
                    mTextViewAlarml.setTextColor(getResources().getColor(R.color.white));
                    mImageViewAlarml.setImageBitmap(BitmapFactory.decodeResource(
                            this.getResources(), R.drawable.nullpic));
                    break;
                default:
                    mTextViewAlarml.setText("Тип сообщения не опознан");
                    mTextViewAlarml.setTextColor(getResources().getColor(R.color.yellow));
                    mImageViewAlarml.setImageBitmap(BitmapFactory.decodeResource(
                            this.getResources(), R.drawable.nullpic));
            }

            if (mMaskGuard)
            {
                mImageViewStateGuard.setImageBitmap(BitmapFactory.decodeResource(
                        this.getResources(), R.drawable.locked));
                mImageButtonSendGuardOff.setImageBitmap(BitmapFactory.decodeResource(
                        this.getResources(), R.drawable.unlock));
                mImageButtonSendGuardOn.setImageBitmap(BitmapFactory.decodeResource(
                        this.getResources(), R.drawable.lock_active));
            }
            else
            {
                mImageViewStateGuard.setImageBitmap(BitmapFactory.decodeResource(
                        this.getResources(), R.drawable.unlocked));
                mImageButtonSendGuardOff.setImageBitmap(BitmapFactory.decodeResource(
                        this.getResources(), R.drawable.unlock_active));
                mImageButtonSendGuardOn.setImageBitmap(BitmapFactory.decodeResource(
                        this.getResources(), R.drawable.lock));
            }

//            if (mMaskIn1) mImageViewStateIn1.setImageBitmap(BitmapFactory.decodeResource(
//                    this.getResources(), R.drawable.in1_alert));
//            else mImageViewStateIn1.setImageBitmap(BitmapFactory.decodeResource(
//                    this.getResources(), R.drawable.in1));
//
//            if (mMaskIn2) mImageViewStateIn2.setImageBitmap(BitmapFactory.decodeResource(
//                    this.getResources(), R.drawable.in2_alert));
//            else mImageViewStateIn2.setImageBitmap(BitmapFactory.decodeResource(
//                    this.getResources(), R.drawable.in2));
//
//            if (mMaskMove) mImageViewStateMove.setImageBitmap(BitmapFactory.decodeResource(
//                    this.getResources(), R.drawable.accelerometer_alert));
//            else mImageViewStateMove.setImageBitmap(BitmapFactory.decodeResource(
//                    this.getResources(), R.drawable.accelerometer));
//
//            if (mMaskEngine) {
//                if (mMaskGuard) mImageViewStateEngine.setImageBitmap(BitmapFactory.decodeResource(
//                        this.getResources(), R.drawable.engine_alert));
//                else mImageViewStateEngine.setImageBitmap(BitmapFactory.decodeResource(
//                        this.getResources(), R.drawable.engine_working));
//            } else mImageViewStateEngine.setImageBitmap(BitmapFactory.decodeResource(
//                    this.getResources(), R.drawable.engine_ok));
//
//            if (mMaskNobat) mImageViewStateBat.setImageBitmap(BitmapFactory.decodeResource(
//                    this.getResources(), R.drawable.battery_none));
//            else if (mMaskLobat) mImageViewStateBat.setImageBitmap(BitmapFactory.decodeResource(
//                    this.getResources(), R.drawable.battery_low));
//            else mImageViewStateBat.setImageBitmap(BitmapFactory.decodeResource(
//                        this.getResources(), R.drawable.battery_ok));
//
//            if (mMaskJamm) mImageViewStateJamm.setImageBitmap(BitmapFactory.decodeResource(
//                    this.getResources(), R.drawable.gsm_jam_alert));
//            else mImageViewStateJamm.setImageBitmap(BitmapFactory.decodeResource(
//                    this.getResources(), R.drawable.gsm_jam));
//
            if (mMaskHorn) mImageButtonSendHornOn.setImageBitmap(BitmapFactory.decodeResource(
                    this.getResources(), R.drawable.siren_on));
            else mImageButtonSendHornOn.setImageBitmap(BitmapFactory.decodeResource(
                    this.getResources(), R.drawable.siren_off));

            if (mMaskBlock) mImageButtonSendBlockOn.setImageBitmap(BitmapFactory.decodeResource(
                    this.getResources(), R.drawable.engine_block_active));
            else mImageButtonSendBlockOn.setImageBitmap(BitmapFactory.decodeResource(
                    this.getResources(), R.drawable.engine_block));
//
//            if (mMaskUgon) mImageViewStateUgon.setImageBitmap(BitmapFactory.decodeResource(
//                    this.getResources(), R.drawable.mover_alert));
//            else mImageViewStateUgon.setImageBitmap(BitmapFactory.decodeResource(
//                    this.getResources(), R.drawable.mover));


            int mGsmAntValue = Integer.parseInt(mGsmAnt.substring(0, mGsmAnt.indexOf('%')));
            if (mGsmAntValue>=80) mImageViewScreenGsmLevel.setImageBitmap(BitmapFactory.decodeResource(
                    this.getResources(), R.drawable.signal_5));
            else if (mGsmAntValue>=60) mImageViewScreenGsmLevel.setImageBitmap(BitmapFactory.decodeResource(
                    this.getResources(), R.drawable.signal_4));
            else if (mGsmAntValue>=40) mImageViewScreenGsmLevel.setImageBitmap(BitmapFactory.decodeResource(
                    this.getResources(), R.drawable.signal_3));
            else if (mGsmAntValue>=20) mImageViewScreenGsmLevel.setImageBitmap(BitmapFactory.decodeResource(
                    this.getResources(), R.drawable.signal_2));
            else if (mGsmAntValue>=5) mImageViewScreenGsmLevel.setImageBitmap(BitmapFactory.decodeResource(
                    this.getResources(), R.drawable.signal_1));
            else mImageViewScreenGsmLevel.setImageBitmap(BitmapFactory.decodeResource(
                        this.getResources(), R.drawable.signal_0));



            if (mDataVolt.contains("%")) mImageViewScreenBat.setImageBitmap(BitmapFactory.decodeResource(
                    this.getResources(), R.drawable.battery_alt));
            else mImageViewScreenBat.setImageBitmap(BitmapFactory.decodeResource(
                    this.getResources(), R.drawable.battery_small));

            mTextViewBatVal.setText(mDataVolt);
            mTextViewTempVal.setText(mDataTemp);
            mTextViewSpeedVal.setText(mGpsSpeed);
            mTextViewDateVal.setText(mGpsDate);
            mTextViewTimeVal.setText(mGspTime);

            mTextViewScreenGmsBalance.setText(mReceiverBalance + " Р");
            mTextViewScreenReceiver.setText(mReceiver);

            mTextViewScreenReceiverDate.setText(mReceivedCurrentDate + " " + mReceivedCurrentTime);


            if (mMask3DFix == 2){
                mTextViewScreenGps.setTextColor(getResources().getColor(R.color.green));
            }
            else if (mMask3DFix == 1)
            {
                mTextViewScreenGps.setTextColor(getResources().getColor(R.color.yellow));
            }
            else
            {
                mTextViewScreenGps.setTextColor(getResources().getColor(R.color.red));
            }

            if (mMaskBtGuard) mImageButtonBluetoothMode.setImageBitmap(BitmapFactory.decodeResource(
                    this.getResources(), R.drawable.bluetooth_on));
            else mImageButtonBluetoothMode.setImageBitmap(BitmapFactory.decodeResource(
                    this.getResources(), R.drawable.bluetooth_off));

            if (mMaskValet) mImageButtonValetMode.setImageBitmap(BitmapFactory.decodeResource(
                    this.getResources(), R.drawable.position_track_on));
            else mImageButtonValetMode.setImageBitmap(BitmapFactory.decodeResource(
                    this.getResources(), R.drawable.position_track_off));

            gpsLatitude = Double.parseDouble(mGpsLat);
            gpsLongitude = Double.parseDouble(mGpsLong);

            cursor.close();
        }
        catch (Exception ex)
        {
            Toast.makeText(this, "База данных пуста", Toast.LENGTH_LONG).show();

        }

    }

    public int getLastIdDb() {
        mDatabaseHelper = new DatabaseHelper(this);
        mSqLiteDatabase = mDatabaseHelper.getWritableDatabase();


        //TODO: Поиск индекса последнего события_СМС в базе
        Cursor cursor = mSqLiteDatabase.query("auto_gps", new String[]{
                        DatabaseHelper.PACKET_ID_COLUMN},
                null, null, null, null, null);
        int mMaxId = 0;
        try {
            if (cursor.moveToLast())
                mMaxId = cursor.getInt(cursor.getColumnIndex(DatabaseHelper.PACKET_ID_COLUMN));
        }
        catch (Exception ex)
        {
            Toast.makeText(this, "База данных пуста", Toast.LENGTH_SHORT).show();
        }
        cursor.close();
        return mMaxId;
    }

    public void putSmsToDataBase(int mId, String strAdress, String strBody, Long mDate) {
        try {
            Pattern pattern = Pattern.compile("\n");
            String[] mReceivedData = pattern.split(strBody);
            //TODO: Cделать разбитие строк по идентификаторам типа GPS BAT TIME DATE...
            int mEventNum = 0;
            String mEventType = "UNKNOWN";
            String mGpsLong = "00.00";
            String mGpsLat = "00.00";
            String mEventBat = "--";
            String mEventTemp = "--";
            String mEventAnt = "--";
            String mEventGpsSpeed = "--";
            String mReceivedGpsTime = "--";
            String mReceivedGpsDate = "--";

            Date date = new Date(mDate);
            //date.getTime();

            String mReceivedCurrentTime = new SimpleDateFormat("yyyy.MM.dd").format(date);
            String mReceivedCurrentDate = new SimpleDateFormat("HH:mm:ss").format(date);
            String mReceiverBalance = "--";

            int mMaskValet = 0;
            int mMaskBtGuard = 0;
            int mMaskUgon = 0;
            int mMaskLobat = 0;
            int mMask3DFix = 0;

            //foreach loop to detect strings

            for (String string:mReceivedData)
            {
                if (string.contains("GPS") && string.length()>20 && string.contains("="))
                {
                    if (string.contains("3D OLD")) mMask3DFix = 0;
                    else if (string.contains("2D")) mMask3DFix = 1;
                    else if (string.contains("3D")) mMask3DFix = 2;
                    Pattern gpsPattern = Pattern.compile("=");
                    String[] mReceivedGpsCoordinates = gpsPattern.split(mReceivedData[1].toString());
                    mGpsLong = mReceivedGpsCoordinates[1].substring(0, 9);
                    mGpsLat = mReceivedGpsCoordinates[2];
                }
                else if (string.contains("BAT:")) mEventBat = string.substring(4);
                else if (string.contains("RUB:")) mReceiverBalance = string.substring(4);
                else if (string.contains("LiPol:")) mEventBat = string.substring(6);
                else if (string.contains("GSM:")) mEventAnt = string.substring(4);
                else if (string.contains("TMP:")) mEventTemp = string.substring(4);
                else if (string.contains("SPD:")) mEventGpsSpeed = string.substring(4);
                else if (Pattern.matches("^?([A-F0-9]{2})-?([A-Z0-9 ]+)", string))
                {
                    //Get Event counter Number
                    mEventNum = Integer.parseInt(string.substring(0, 2), 16);
                    //Get Event Type
                    mEventType = string.substring(3);
                }
                else if (Pattern.matches("^?([0-9]{2}):?([0-9]{2}):?([0-9]{2})", string))  mReceivedGpsTime = string;
                else if (Pattern.matches("^?([0-9]{2})/?([0-9]{2})/?([0-9]{2})", string))  mReceivedGpsDate = string;
            }

            int mMaskIn1 = mReceivedData[2].contains("IN1,")? 1:0;
            int mMaskIn2 = mReceivedData[2].contains("IN2,")? 1:0;
            int mMaskHorn = (mReceivedData[2].contains("H,") || mReceivedData[2].contains("S,"))? 1:0;
            int mMaskBlock = mReceivedData[2].contains("B,")? 1:0;
            int mMaskGuard = mReceivedData[2].contains("G,")? 1:0;
            int mMaskMove = mReceivedData[2].contains("M,")? 1:0;
            int mMaskNobat = mReceivedData[2].contains("N,")? 1:0;
            int mMaskJamm = mReceivedData[2].contains("J,")? 1:0;
            int mMaskEngine = mReceivedData[2].contains("E,")? 1:0;
            mMaskValet = mReceivedData[2].contains("V,")? 1:0;
            mMaskBtGuard = mReceivedData[2].contains("BT,")? 1:0;
            mMaskUgon = mReceivedData[2].contains("GT,")? 1:0;

            mMaskLobat = mReceivedData[2].contains("L,")? 1:0;

            mDatabaseHelper = new DatabaseHelper(this);

            mSqLiteDatabase = mDatabaseHelper.getWritableDatabase();

            ContentValues newValues = new ContentValues();


            newValues.put(DatabaseHelper.PACKET_ID_COLUMN, mId);
            newValues.put(DatabaseHelper.TRANSMITTER_COLUMN, strAdress);
            newValues.put(DatabaseHelper.RECEIVER_COLUMN, "SMS");
            newValues.put(DatabaseHelper.EVENT_NUM_COLUMN, mEventNum);
            newValues.put(DatabaseHelper.EVENT_TYPE_COLUMN, mEventType);
            newValues.put(DatabaseHelper.DATA_TEMP_COLUMN, mEventTemp);
            newValues.put(DatabaseHelper.DATA_VOLT_COLUMN, mEventBat);
            newValues.put(DatabaseHelper.GSM_ANT_COLUMN, mEventAnt);
            newValues.put(DatabaseHelper.GPS_TIME_COLUMN, mReceivedGpsTime);
            newValues.put(DatabaseHelper.GPS_DATE_COLUMN, mReceivedGpsDate);
            newValues.put(DatabaseHelper.GPS_LONG_COLUMN, mGpsLong);
            newValues.put(DatabaseHelper.GPS_LAT_COLUMN, mGpsLat);
            newValues.put(DatabaseHelper.GPS_SPEED_COLUMN, mEventGpsSpeed);
            newValues.put(DatabaseHelper.MASK_IN1, mMaskIn1);
            newValues.put(DatabaseHelper.MASK_IN2, mMaskIn2);
            newValues.put(DatabaseHelper.MASK_HORN, mMaskHorn);
            newValues.put(DatabaseHelper.MASK_BLOCK, mMaskBlock);
            newValues.put(DatabaseHelper.MASK_GUARD, mMaskGuard);
            newValues.put(DatabaseHelper.MASK_MOVE, mMaskMove);
            newValues.put(DatabaseHelper.MASK_NOBAT, mMaskNobat);
            newValues.put(DatabaseHelper.MASK_JAMM, mMaskJamm);
            newValues.put(DatabaseHelper.MASK_ENGINE, mMaskEngine);

            newValues.put(DatabaseHelper.RECEIVED_TIME, mReceivedCurrentTime );
            newValues.put(DatabaseHelper.RECEIVED_DATE, mReceivedCurrentDate );
            newValues.put(DatabaseHelper.TRANSMITTER_BALANCE, mReceiverBalance );

            newValues.put(DatabaseHelper.MASK_VALET, mMaskValet );
            newValues.put(DatabaseHelper.MASK_BT_GUARD, mMaskBtGuard );
            newValues.put(DatabaseHelper.MASK_UGON, mMaskUgon );
            newValues.put(DatabaseHelper.MASK_LOBAT, mMaskLobat );
            newValues.put(DatabaseHelper.GPS_FIX_3D, mMask3DFix );

            // Вставляем данные в таблицу
            mSqLiteDatabase.insert("auto_gps", null, newValues);
            Log.d("Message Receiver", "Event inserted to DataBase");

        } catch (Exception ex) {
            Log.e("Messaging Receiver", ex.toString());
        }

    }


/*
    private class DrawerItemClickListener implements ListView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            selectItem(position);
        }
    }

    /** Swaps fragments in the main content view */
    /*private void selectItem(int position) {
        // Update the main content by replacing fragments
        Toast.makeText(this, "Position: " + position, Toast.LENGTH_SHORT).show();
    }*/



    public void onConfigMenuClick(MenuItem item) {
        AlertDialog.Builder openConfiguratorGetDialog = new AlertDialog.Builder(this);
        openConfiguratorGetDialog.setTitle("Изменение конфигурации оборудования может привести к некорректной работе прибора");
        openConfiguratorGetDialog.setPositiveButton("Отмена", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // нет действий
            }
        });

        openConfiguratorGetDialog.setNegativeButton("Изменить", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(MainActivity.this, ConfiguratorActivity.class);
                startActivity(intent);
            }
        });
        openConfiguratorGetDialog.show();
    }


    public void onBtnArchieveClick(View view) {
        Intent intent = new Intent(MainActivity.this, ArchieveListActivity.class);
        startActivity(intent);
    }


    public void onArchieveMenuItemClick(MenuItem item) {
        Intent intent = new Intent(MainActivity.this, ArchieveListActivity.class);
        startActivity(intent);
    }

    public void onBtnShowGpsOnMapClick(View view) {
        Intent intent = new Intent(MainActivity.this, MapsActivity.class);
        intent.putExtra(ArchieveActivity.GPS_LONGITUDE, gpsLatitude);
        intent.putExtra(ArchieveActivity.GPS_LATITUDE,gpsLongitude);
        startActivity(intent);
    }
    public void onGpsLocationMainClick(View view) {
        Intent intent = new Intent(MainActivity.this, MapsActivity.class);
        intent.putExtra(ArchieveActivity.GPS_LONGITUDE, gpsLatitude);
        intent.putExtra(ArchieveActivity.GPS_LATITUDE,gpsLongitude);
        startActivity(intent);
    }

    public void onGpsClick(View view) {
        Intent intent = new Intent(MainActivity.this, MapsActivity.class);
        intent.putExtra(ArchieveActivity.GPS_LONGITUDE, gpsLatitude);
        intent.putExtra(ArchieveActivity.GPS_LATITUDE, gpsLongitude);
        startActivity(intent);
    }


    public void onBtnConfigMenuClick(View view) {
        AlertDialog.Builder openConfiguratorGetDialog = new AlertDialog.Builder(this);
        openConfiguratorGetDialog.setTitle("Изменение конфигурации оборудования может привести к некорректной работе прибора");
        openConfiguratorGetDialog.setPositiveButton("Отмена", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // нет действий
            }
        });

        openConfiguratorGetDialog.setNegativeButton("Изменить", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(MainActivity.this, ConfiguratorActivity.class);
                startActivity(intent);
            }
        });
        openConfiguratorGetDialog.show();
    }


    public void onBtnMenuMenuClick(View view) {

        openOptionsMenu();
    }


}
