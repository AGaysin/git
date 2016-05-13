package ru.tehohrana.smsreceiver.config;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
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
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.regex.Pattern;

import ru.tehohrana.smsreceiver.ConfiguratorActivity;
import ru.tehohrana.smsreceiver.R;

/**
 * Created by AG on 11.12.2015.
 */
public class ConfigBT extends Activity {
    TextView labelStateBluetooth;
    BluetoothAdapter bluetoothAdapter;
    Button btnBtPair;
    Button btnTurnOnBt;
    ProgressDialog prog1;
    // это будет именем файла настроек
    public static final String CONFIG_PREFERENCES = "device_config";

    private static long timerSmsCommandWaiting=0;
    public String mDevicePhoneNumber;
    public SharedPreferences mSettings;
    private int mLastMessageId=0;
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
    public String name;
SharedPreferences mDeviceSettings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_config_bluetooth);

        mDeviceSettings = getSharedPreferences(CONFIG_PREFERENCES, Context.MODE_PRIVATE);

        mSettings = PreferenceManager.getDefaultSharedPreferences(this);
        mDevicePhoneNumber = mSettings.getString("pref_phone", "");

        // получаем адаптер по умолчанию
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        labelStateBluetooth = (TextView) findViewById(R.id.textViewBtState);
        btnTurnOnBt = (Button) findViewById(R.id.buttonTurnBtOn);
        btnBtPair = (Button) findViewById(R.id.btnBtPair);

        BroadcastReceiver bluetoothState = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String prevStateExtra = BluetoothAdapter.EXTRA_PREVIOUS_STATE;
                String stateExtra = BluetoothAdapter.EXTRA_STATE;
                int state = intent.getIntExtra(stateExtra, -1);
                int previousState = intent.getIntExtra(prevStateExtra, -1);

                String address = bluetoothAdapter.getAddress();
                name = bluetoothAdapter.getName();
                switch (state) {
                    case (BluetoothAdapter.STATE_TURNING_ON) : {
                        labelStateBluetooth
                                .setText("Bluetooth в процессе включения ...\r\nИмя устройства: "
                                        + name + "\r\nMac-адрес: " + address);
                        btnTurnOnBt.setEnabled(false);
                        break;
                    }
                    case (BluetoothAdapter.STATE_ON) : {
                        labelStateBluetooth.setText("Bluetooth доступен.\r\nИмя устройства: "
                                + name + "\r\nMac-адрес: " + address);
                        btnBtPair.setEnabled(true);
                        btnBtPair.setEnabled(true);
                        btnTurnOnBt.setEnabled(false);
                        unregisterReceiver(this);

                        break;
                    }
                    case (BluetoothAdapter.STATE_TURNING_OFF) : {
                        labelStateBluetooth.setText("Bluetooth выключается...\r\nИмя устройства: "
                                + name + "\r\nMac-адрес: " + address);
                        btnTurnOnBt.setEnabled(false);
                        btnBtPair.setEnabled(false);
                        break;
                    }
                    case (BluetoothAdapter.STATE_OFF) : {
                        labelStateBluetooth.setText("Bluetooth выключен!\r\nИмя устройства: "
                                + name + "\r\nMac-адрес: " + address);
                        btnTurnOnBt.setEnabled(true);
                        btnBtPair.setEnabled(false);
                        break;
                    }
                    default: break;
                }
            }
        };
        registerReceiver(bluetoothState,
                new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED));

        btnBtPair.setEnabled(false);
        btnTurnOnBt.setEnabled(false);
        if (bluetoothAdapter == null) {
            labelStateBluetooth
                    .setText("Bluetooth на вашем устройстве не поддерживается");

        } else {
            if (bluetoothAdapter.isEnabled()) {
                String address = bluetoothAdapter.getAddress();
                name = bluetoothAdapter.getName();
                if (bluetoothAdapter.isDiscovering()) {
                    labelStateBluetooth
                            .setText("Bluetooth в процессе включения ...\r\nИмя устройства: "
                                    + name + "\r\nMac-адрес: " + address);
                } else {
                    labelStateBluetooth.setText("Bluetooth доступен.\r\nИмя устройства: "
                            + name + "\r\nMac-адрес: " + address);
                    btnBtPair.setEnabled(true);
                }


                //Отправка СМС о спарке
            } else {
                //Dialog о включении BT
                labelStateBluetooth
                        .setText("Bluetooth выключен!");
                btnTurnOnBt.setEnabled(true);
            }
        }



        btnTurnOnBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent enableBTIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivity(enableBTIntent);
            }
        });





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
                        Toast.makeText(getBaseContext(), "SMS Отправлено",
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

                        String bodyText = cur.getString(cur.getColumnIndex("body"));
                        if (bodyText.contains("PAIR OK")) {
                            AlertDialog.Builder builder = new AlertDialog.Builder(ConfigBT.this);
                            builder.setTitle("Операция завершена")
                                    .setMessage("Операция связывания устройств успешно завершена")
                                    .setIcon(R.drawable.ok_icon)
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
                            //editor.apply();
                            return true;
                        } else if (bodyText.contains("UNPAIR OK")) {
                            AlertDialog.Builder builder = new AlertDialog.Builder(ConfigBT.this);
                            builder.setTitle("Операция завершена")
                                    .setMessage("Операция развязывания устройств успешно завершена")
                                    .setIcon(R.drawable.ok_icon)
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
                            //editor.apply();
                            return true;
                        }
                        else if (bodyText.contains("PAIR FAIL: NOT FOUND")){
                            AlertDialog.Builder builder = new AlertDialog.Builder(ConfigBT.this);
                            builder.setTitle("Операция не завершена")
                                    .setMessage("Связывание устройств завешить не удалось! Устройство не найдено в сети.")
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
                            //editor.apply();
                            return true;
                        }
                        else if (bodyText.contains("PAIR FAIL")){
                            AlertDialog.Builder builder = new AlertDialog.Builder(ConfigBT.this);
                            builder.setTitle("Операция не завершена")
                                    .setMessage("Связывание устройств завешить не удалось! Попробуйте повторить попытку")
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
                            //editor.apply();
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

    public void checkNewSms(){
        if (readSmsParameters(mLastMessageId))
        {
            prog1.dismiss();
            //Вывсти сообщение, что параметры успешно обновлены
            timerSmsCommandWaiting = 0;

        }

        prog1.setMessage("Ожидание запроса конфигурации оборудования...(" + timerSmsCommandWaiting + ")");
        //Toast.makeText(getApplicationContext(), "check " + timerSmsCommandWaiting, Toast.LENGTH_SHORT).show();
        //prog1.cancel();
    }

    public void cancelWaiting(){
        prog1.dismiss();
        //Вывести сообщение о таймауте
        AlertDialog.Builder builder = new AlertDialog.Builder(ConfigBT.this);
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

    public void onBtnPairClick(View view) {
        sendSMS(mDevicePhoneNumber, "BT+PAIR="+name);
        mLastMessageId = getLastMessageId();
        prog1 = new ProgressDialog(ConfigBT.this);
        prog1.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        prog1.setMessage("Ожидание запроса конфигурации оборудования...");
        prog1.setIndeterminate(true); // выдать значек ожидания
        prog1.setCancelable(false);
        timerSmsCommandWaiting = 60;
        prog1.show();
    }

    public void onBtnUnpairClick(View view) {
        sendSMS(mDevicePhoneNumber, "BT+UNPAIR=1");
        mLastMessageId = getLastMessageId();
        prog1 = new ProgressDialog(ConfigBT.this);
        prog1.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        prog1.setMessage("Ожидание запроса конфигурации оборудования...");
        prog1.setIndeterminate(true); // выдать значек ожидания
        prog1.setCancelable(false);
        timerSmsCommandWaiting = 40;
        prog1.show();
    }
}
