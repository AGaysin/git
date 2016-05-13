/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ru.tehohrana.smsreceiver.service;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.telephony.SmsMessage;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.regex.Pattern;

import ru.tehohrana.smsreceiver.MainActivity;
import ru.tehohrana.smsreceiver.R;




public class MessagingService extends IntentService {

    private static final int NOTIFY_ID = 1137;
    private static final String TAG = "MessagingService";
    // These actions are for this app only and are used by MessagingReceiver to start this service
    public static final String ACTION_MY_RECEIVE_SMS = "ru.tehohrana.smsreceiver.RECEIVE_SMS";
    public static final String ACTION_MY_RECEIVE_MMS = "ru.tehohrana.smsreceiver.RECEIVE_MMS";

    private DatabaseHelper mDatabaseHelper;
    private SQLiteDatabase mSqLiteDatabase;
    private SharedPreferences mSettings;
    public final static String IS_APP_RUNNING = "key_AppRunning";


    static final public String COPA_RESULT = "com.controlj.copame.backend.COPAService.REQUEST_PROCESSED";

    static final public String COPA_MESSAGE = "com.controlj.copame.backend.COPAService.COPA_MSG";

    Calendar c = Calendar.getInstance();

    public MessagingService() {
        super(TAG);
    }
    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            String intentAction = intent.getAction();
            if (ACTION_MY_RECEIVE_SMS.equals(intentAction)) {

                //Получаем номер телефона оборудования
                SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
                String mDevicePhoneNumber = sp.getString("pref_phone", "");

                // TODO: Handle incoming SMS
                Object[] pduArray = (Object[]) intent.getExtras().get("pdus");
                SmsMessage[] messages = new SmsMessage[pduArray.length];
                for (int i = 0; i < pduArray.length; i++) {
                    messages[i] = SmsMessage.createFromPdu((byte[]) pduArray[i]);
                }
                StringBuilder bodyText = new StringBuilder();

                for (int i = 0; i < messages.length; i++) {
                    bodyText.append(messages[i].getMessageBody());
                }

                String sms_from = messages[0].getDisplayOriginatingAddress();
                if (sms_from.equalsIgnoreCase(mDevicePhoneNumber) && !bodyText.toString().contains("SET")) {

                    int mId = getLastSmsId() + 1;


//                    int mEventNum = 0;
//                    String mEventType = "UNKNOWN";
//                    String mGpsLong = "00.00";
//                    String mGpsLat = "00.00";
//                    String mEventBat = "--";
//                    String mEventTemp = "--";
//                    String mEventAnt = "--";
//                    String mEventGpsSpeed = "--";
//                    String mReceivedGpsTime = "--";
//                    String mReceivedGpsDate = "--";
//                    String mReceivedCurrentTime = c.get(Calendar.HOUR) + ":" + c.get(Calendar.MINUTE);
//                    String mReceivedCurrentDate = c.get(Calendar.DAY_OF_MONTH) + "." + c.get(Calendar.MONTH) + "." + c.get(Calendar.YEAR);
//                    String mReceiverBalance = "--";
//                    int mMaskValet = 0;
//                    int mMaskBtGuard = 0;
//                    int mMaskUgon = 0;
//                    int mMaskLobat = 0;
//                    int mMask3DFix = 0;
//
//
//                    int mMaskIn1 = 0;
//                    int mMaskIn2 = 0;
//                    int mMaskHorn = 0;
//                    int mMaskBlock = 0;
//                    int mMaskGuard = 0;
//                    int mMaskMove = 0;
//                    int mMaskNobat = 0;
//                    int mMaskJamm = 0;
//                    int mMaskEngine = 0;




                    try {
                        Pattern pattern = Pattern.compile("\n");
                        String[] mReceivedData = pattern.split(bodyText);
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

                        Date date = new Date();

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
                        newValues.put(DatabaseHelper.TRANSMITTER_COLUMN, sms_from);
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


                        mSettings = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                        //is ActivityMain running

                        boolean isAppRunning = mSettings.getBoolean(IS_APP_RUNNING, false);
                        boolean isAlarmNotifyEnable = mSettings.getBoolean("keyAlarmNotifyEnable", false);
                        boolean isServiceNotifyEnable = mSettings.getBoolean("keyServiceNotifyEnable", false);

                        if (isAppRunning) sendResult("UpdateDb");


                        if ((isAlarmNotifyEnable || isServiceNotifyEnable) ) { //&& !isAppRunning
                            //Создаем уведомление для ранниъ версий Android
                            Context context = getApplicationContext();
                            Intent notificationIntent = new Intent(context, MainActivity.class);
                            PendingIntent contentIntent = PendingIntent.getActivity(context,
                                    0, notificationIntent,
                                    PendingIntent.FLAG_CANCEL_CURRENT);

                            Resources res = context.getResources();
                            Notification.Builder builder = new Notification.Builder(context);

                            //ТРЕВОГА !!!







                            switch (mEventType) {
                                case "IN1":
                                    builder.setContentIntent(contentIntent)
                                            .setSmallIcon(R.drawable.in1_alarm)
                                                    // большая картинка
                                            .setLargeIcon(BitmapFactory.decodeResource(res, R.drawable.in1_alarm))
                                                    //.setTicker(res.getString(R.string.warning)) // текст в строке состояния
                                            .setTicker("Тревога")
                                            .setWhen(System.currentTimeMillis())
                                            .setAutoCancel(true)
                                                    //.setContentText(res.getString(R.string.notifytext))
                                            .setContentText(mSettings.getString("pref_in1", "Зона ШС1")); // Текст уведомления
                                    break;
                                case "IN2":
                                    builder.setContentIntent(contentIntent)
                                            .setSmallIcon(R.drawable.in2_alarm)
                                                    // большая картинка
                                            .setLargeIcon(BitmapFactory.decodeResource(res, R.drawable.in2_alarm))
                                                    //.setTicker(res.getString(R.string.warning)) // текст в строке состояния
                                            .setTicker("Тревога")
                                            .setWhen(System.currentTimeMillis())
                                            .setAutoCancel(true)
                                                    //.setContentText(res.getString(R.string.notifytext))
                                            .setContentText(mSettings.getString("pref_in2", "Зона ШС2")); // Текст уведомления
                                    break;
                                case "NET AKB":
                                case "NO BAT":
                                case "NOBAT":
                                    builder.setContentIntent(contentIntent)
                                            .setSmallIcon(R.drawable.battery_alarm)
                                                    // большая картинка
                                            .setLargeIcon(BitmapFactory.decodeResource(res, R.drawable.battery_alarm))
                                                    //.setTicker(res.getString(R.string.warning)) // текст в строке состояния
                                            .setTicker("Тревога")
                                            .setWhen(System.currentTimeMillis())
                                            .setAutoCancel(true)
                                                    //.setContentText(res.getString(R.string.notifytext))
                                            .setContentText("Отключен аккумулятор"); // Текст уведомления

                                    break;
                                case "UDAR":
                                case "MOVE":
                                    builder.setContentIntent(contentIntent)
                                            .setSmallIcon(R.drawable.accelerometer_alarm)
                                                    // большая картинка
                                            .setLargeIcon(BitmapFactory.decodeResource(res, R.drawable.accelerometer_alarm))
                                                    //.setTicker(res.getString(R.string.warning)) // текст в строке состояния
                                            .setTicker("Тревога")
                                            .setWhen(System.currentTimeMillis())
                                            .setAutoCancel(true)
                                                    //.setContentText(res.getString(R.string.notifytext))
                                            .setContentText("Датчик ускорения/удара"); // Текст уведомления

                                    break;
                                case "UGON":
                                    builder.setContentIntent(contentIntent)
                                            .setSmallIcon(R.drawable.mover_alarm)
                                                    // большая картинка
                                            .setLargeIcon(BitmapFactory.decodeResource(res, R.drawable.mover_alarm))
                                                    //.setTicker(res.getString(R.string.warning)) // текст в строке состояния
                                            .setTicker("Тревога")
                                            .setWhen(System.currentTimeMillis())
                                            .setAutoCancel(true)
                                                    //.setContentText(res.getString(R.string.notifytext))
                                            .setContentText("Перемещение объекта"); // Текст уведомления

                                    break;
                                case "ENGINE":
                                    builder.setContentIntent(contentIntent)
                                            .setSmallIcon(R.drawable.engine_working_alarm)
                                                    // большая картинка
                                            .setLargeIcon(BitmapFactory.decodeResource(res, R.drawable.engine_working_alarm))
                                                    //.setTicker(res.getString(R.string.warning)) // текст в строке состояния
                                            .setTicker("Тревога")
                                            .setWhen(System.currentTimeMillis())
                                            .setAutoCancel(true)
                                                    //.setContentText(res.getString(R.string.notifytext))
                                            .setContentText("Двигатель заведен"); // Текст уведомления

                                    break;
                                case "ASK OWN1":
                                case "ASK OWN2":
                                    builder.setContentIntent(contentIntent)
                                            .setSmallIcon(R.drawable.refresh)
                                                    // большая картинка
                                            .setLargeIcon(BitmapFactory.decodeResource(res, R.drawable.refresh))
                                                    //.setTicker(res.getString(R.string.warning)) // текст в строке состояния
                                            .setTicker("Получено сообщение")
                                            .setWhen(System.currentTimeMillis())
                                            .setAutoCancel(true)
                                                    //.setContentText(res.getString(R.string.notifytext))
                                            .setContentText("Опрос состояния"); // Текст уведомления

                                    break;
                                case "JAMM":
                                    builder.setContentIntent(contentIntent)
                                            .setSmallIcon(R.drawable.gsm_jam_alarm)
                                                    // большая картинка
                                            .setLargeIcon(BitmapFactory.decodeResource(res, R.drawable.gsm_jam_alarm))
                                                    //.setTicker(res.getString(R.string.warning)) // текст в строке состояния
                                            .setTicker("Тревога")
                                            .setWhen(System.currentTimeMillis())
                                            .setAutoCancel(true)
                                                    //.setContentText(res.getString(R.string.notifytext))
                                            .setContentText("Глушение GSM-сигнала"); // Текст уведомления
                                    break;
                                case "LOW BAT":
                                case "LOBAT":
                                case "RAZRYAD AKB":
                                    builder.setContentIntent(contentIntent)
                                            .setSmallIcon(R.drawable.battery_low)
                                                    // большая картинка
                                            .setLargeIcon(BitmapFactory.decodeResource(res, R.drawable.battery_low))
                                                    //.setTicker(res.getString(R.string.warning)) // текст в строке состояния
                                            .setTicker("Внимание")
                                            .setWhen(System.currentTimeMillis())
                                            .setAutoCancel(true)
                                                    //.setContentText(res.getString(R.string.notifytext))
                                            .setContentText("Аккумулятор разряжен"); // Текст уведомления

                                    break;
                                case "VZYAT":
                                    builder.setContentIntent(contentIntent)
                                            .setSmallIcon(R.drawable.locked)
                                                    // большая картинка
                                            .setLargeIcon(BitmapFactory.decodeResource(res, R.drawable.locked))
                                                    //.setTicker(res.getString(R.string.warning)) // текст в строке состояния
                                            .setTicker("Получено сообщение")
                                            .setWhen(System.currentTimeMillis())
                                            .setAutoCancel(true)
                                                    //.setContentText(res.getString(R.string.notifytext))
                                            .setContentText("Объект взят под охрану"); // Текст уведомления
                                    break;
                                case "SNYAT":
                                    builder.setContentIntent(contentIntent)
                                            .setSmallIcon(R.drawable.unlocked)
                                                    // большая картинка
                                            .setLargeIcon(BitmapFactory.decodeResource(res, R.drawable.unlocked))
                                                    //.setTicker(res.getString(R.string.warning)) // текст в строке состояния
                                            .setTicker("Получено сообщение")
                                            .setWhen(System.currentTimeMillis())
                                            .setAutoCancel(true)
                                                    //.setContentText(res.getString(R.string.notifytext))
                                            .setContentText("Объект снят с охраны"); // Текст уведомления

                                    break;
                                case "PWR ON":
                                case "INIT":
                                    builder.setContentIntent(contentIntent)
                                            .setSmallIcon(R.drawable.power)
                                                    // большая картинка
                                            .setLargeIcon(BitmapFactory.decodeResource(res, R.drawable.power))
                                                    //.setTicker(res.getString(R.string.warning)) // текст в строке состояния
                                            .setTicker("Получено сообщение")
                                            .setWhen(System.currentTimeMillis())
                                            .setAutoCancel(true)
                                                    //.setContentText(res.getString(R.string.notifytext))
                                            .setContentText("Включение прибора. Инициализация"); // Текст уведомления
                                    break;
                                case "SLEEP":
                                case "PWR OFF":
                                    builder.setContentIntent(contentIntent)
                                            .setSmallIcon(R.drawable.sleep)
                                                    // большая картинка
                                            .setLargeIcon(BitmapFactory.decodeResource(res, R.drawable.sleep))
                                                    //.setTicker(res.getString(R.string.warning)) // текст в строке состояния
                                            .setTicker("Получено сообщение")
                                            .setWhen(System.currentTimeMillis())
                                            .setAutoCancel(true)
                                                    //.setContentText(res.getString(R.string.notifytext))
                                            .setContentText("Выключение. Резервный аккумулятор разряжен"); // Текст уведомления
                                    break;
                                default:
                                    builder.setContentIntent(contentIntent)
                                            .setSmallIcon(R.drawable.nullpic)
                                                    // большая картинка
                                            .setLargeIcon(BitmapFactory.decodeResource(res, R.drawable.nullpic))
                                                    //.setTicker(res.getString(R.string.warning)) // текст в строке состояния
                                            .setTicker("Получено сообщение")
                                            .setWhen(System.currentTimeMillis())
                                            .setAutoCancel(true)
                                                    //.setContentText(res.getString(R.string.notifytext))
                                            .setContentText("Тип сообщения не опознан"); // Текст уведомления

                            }





                            switch (mEventType) {
                                case "IN1":
                                case "IN2":
                                case "NET AKB":
                                case "NO BAT":
                                case "NOBAT":
                                case "UDAR":
                                case "MOVE":
                                case "UGON":
                                case "ENGINE":
                                case "JAMM":
                                    if (isAlarmNotifyEnable) {
                                        Notification notification = builder.getNotification(); // до API 16
                                        //Notification notification = builder.build();
                                        //Потоянное уведомение
                                        boolean isAlarmLoop =
                                                mSettings.getBoolean("keyAlarmLoop", false);
                                        if (isAlarmLoop)
                                            notification.flags = notification.flags
                                                    | Notification.FLAG_INSISTENT;
                                        //Вибрация
                                        boolean isAlarmVibroEnable =
                                                mSettings.getBoolean("keyAlarmVibro", false);
                                        if (isAlarmVibroEnable) {
                                            long[] vibrate = new long[]{1000, 1000, 1000, 1000, 1000};
                                            notification.vibrate = vibrate;
                                        }
                                        //Звук
                                        String strRingtonePreference = mSettings.getString("keyAlarmRingtone", "DEFAULT_SOUND");
                                        notification.sound = Uri.parse(strRingtonePreference);
                                        NotificationManager notificationManager = (NotificationManager) context
                                                .getSystemService(Context.NOTIFICATION_SERVICE);
                                        notificationManager.notify(NOTIFY_ID, notification);

                                    }
                                    break;
                                case "ASK OWN1":
                                case "ASK OWN2":
                                case "LOW BAT":
                                case "LOBAT":
                                case "RAZRYAD AKB":
                                case "VZYAT":
                                case "SNYAT":
                                case "PWR ON":
                                case "INIT":
                                case "SLEEP":
                                case "PWR OFF":
                                default:
                                    if (isServiceNotifyEnable) {
                                        Notification notification = builder.getNotification(); // до API 16
                                        //Notification notification = builder.build();
                                        //Вибрация
                                        boolean isServiceVibroEnable =
                                                mSettings.getBoolean("keyServiceVibro", false);
                                        if (isServiceVibroEnable) {
                                            long[] vibrate = new long[]{1000, 1000, 1000, 1000, 1000};
                                            notification.vibrate = vibrate;
                                        }
                                        //Звук
                                        String strRingtonePreference = mSettings.getString("keyServiceRingtone", "DEFAULT_SOUND");
                                        notification.sound = Uri.parse(strRingtonePreference);
                                        //Потоянное уведомение
                                        notification.flags = notification.flags | Notification.FLAG_ONLY_ALERT_ONCE;
                                        NotificationManager notificationManager = (NotificationManager) context
                                                .getSystemService(Context.NOTIFICATION_SERVICE);
                                        notificationManager.notify(NOTIFY_ID, notification);
                                    }
                                    break;

                            }


                        }

                    } catch (Exception ex) {
                        Log.e("Messaging Receiver", ex.toString());
                    }



                }



                // Ensure wakelock is released that was created by the WakefulBroadcastReceiver
                //MessagingReceiver.completeIntent(intent);
            } else if (ACTION_MY_RECEIVE_MMS.equals(intentAction)) {
                // TODO: Handle incoming MMS
                // Ensure wakelock is released that was created by the WakefulBroadcastReceiver
                //MessagingReceiver.completeWakefulIntent(intent);
            }
        }
    }

    public int getLastSmsId()
    {
        final String SMS_URI_INBOX = "content://sms/inbox";
        int mLastId=0;
        try {
            Uri uri = Uri.parse(SMS_URI_INBOX);
            String[] projection = new String[]{"_id", "address", "person", "body", "date", "type"};
            Cursor cur = getContentResolver().query(uri, projection, null, null, "date desc");
            //startManagingCursor(cur);
            if (cur.moveToFirst())
                mLastId = cur.getInt(cur.getColumnIndex("_id"));
            cur.close();
        } catch (Exception ex) {
            Log.d("AVTO_GPS", ex.getMessage());
        }
        return mLastId;
    }

    public void sendResult(String message) {
        Intent intent = new Intent(COPA_RESULT);
        if(message != null)
            intent.putExtra(COPA_MESSAGE, message);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }
}

