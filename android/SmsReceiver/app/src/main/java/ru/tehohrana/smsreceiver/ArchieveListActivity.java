package ru.tehohrana.smsreceiver;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import ru.tehohrana.smsreceiver.service.ArchieveListAdapter;
import ru.tehohrana.smsreceiver.service.DatabaseHelper;
import ru.tehohrana.smsreceiver.service.MessagingService;
/**
 * Created by AG on 24.12.2015.
 */
public class ArchieveListActivity extends Activity {

    DatabaseHelper mDatabaseHelper;
    SQLiteDatabase mSqLiteDatabase;

    public SharedPreferences mSettings;
    private BroadcastReceiver receiver;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_archieve);

        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String s = intent.getStringExtra(MessagingService.COPA_MESSAGE);
                if (s.equals("UpdateDb"))
                {
                    updateArchieve();
                }
                // do something here.
            }
        };
        mSettings = PreferenceManager.getDefaultSharedPreferences(this);

        //Создаем список событий в архиве
        updateArchieve();
    }


    @Override
    public void onStart() {

        LocalBroadcastManager.getInstance(this).registerReceiver((receiver),
                new IntentFilter(MessagingService.COPA_RESULT)
        );
        super.onStart();
    }

    @Override
    public void onStop() {

        LocalBroadcastManager.getInstance(this).unregisterReceiver(receiver);
        super.onStop();

    }


    public void updateArchieve(){

        mDatabaseHelper = new DatabaseHelper(this);
        mSqLiteDatabase = mDatabaseHelper.getWritableDatabase();

        String[] dataBaseEvents = new String[50];
        String[] dataBaseDates = new String[50];
        Integer[] dataBaseIcons = new Integer[50];
        //Integer[] dataId = new Integer[50];

        try {
            //Открытие Базы Данных и оторажение параметров события на экран
            Cursor cursor = mSqLiteDatabase.query("auto_gps", new String[]{
                            DatabaseHelper.PACKET_ID_COLUMN,
                            DatabaseHelper.EVENT_TYPE_COLUMN,
                            DatabaseHelper.RECEIVED_DATE,
                            DatabaseHelper.RECEIVED_TIME,
                    },
                    null, null,
                    null, null, null);

            cursor.moveToLast();
            int counter=0;
            while(!cursor.isBeforeFirst() && counter<50)
            {

                //Читаем текущие данные из базы
//                String string =
//                        cursor.getInt(cursor.getColumnIndex(DatabaseHelper.PACKET_ID_COLUMN)) + ": "+
//                                cursor.getString(cursor.getColumnIndex(DatabaseHelper.GPS_DATE_COLUMN )) + "  " +
//                                cursor.getString(cursor.getColumnIndex(DatabaseHelper.GPS_TIME_COLUMN )) + " >> " +
//                                cursor.getString(cursor.getColumnIndex(DatabaseHelper.EVENT_TYPE_COLUMN)) + ": "  +
//                                cursor.getString(cursor.getColumnIndex(DatabaseHelper.DATA_TEMP_COLUMN )) + ", " + cursor.getString(cursor.getColumnIndex(DatabaseHelper.DATA_VOLT_COLUMN )) + "\r\n";
                //dataId[counter]=cursor.getInt(cursor.getColumnIndex(DatabaseHelper.PACKET_ID_COLUMN));
                dataBaseEvents[counter]=getTextByEvent(cursor.getString(cursor.getColumnIndex(DatabaseHelper.EVENT_TYPE_COLUMN)));
                dataBaseDates[counter]=cursor.getString(cursor.getColumnIndex(DatabaseHelper.RECEIVED_DATE)) + " " +cursor.getString(cursor.getColumnIndex(DatabaseHelper.RECEIVED_TIME));
                dataBaseIcons[counter]=getImageByEvent(cursor.getString(cursor.getColumnIndex(DatabaseHelper.EVENT_TYPE_COLUMN)));
                cursor.moveToPrevious();
                counter++;
            }
            cursor.close();
        }
        catch (Exception ex)
        {
            Toast.makeText(this, "Error while creating archieve list: " + ex.toString(), Toast.LENGTH_LONG).show();
        }


        ArchieveListAdapter adapter=new ArchieveListAdapter(this, dataBaseEvents, dataBaseDates, dataBaseIcons);
        ListView list=(ListView)findViewById(R.id.listViewDataBase);
        list.setAdapter(adapter);

        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                // TODO Auto-generated method stub
                Intent intent = new Intent(ArchieveListActivity.this, ArchieveActivity.class);
                intent.putExtra(ArchieveActivity.EVENT_ID_ARCHIEVE, position);
                startActivity(intent);

            }
        });

    }

    String getTextByEvent (String event)
    {
        switch(event)
        {
            case "IN1": return mSettings.getString("pref_in1", "Тревога. Зона ШС1");
            case "IN2": return mSettings.getString("pref_in2", "Тревога. Зона ШС2");
            case "NET AKB":
            case "NO BAT":
            case "NOBAT":
                return "Отключение аккумулятора";
            case "UDAR":
            case "MOVE":
                return "Сработка акселерометра";
            case "UGON": return "Смена местоположения";
            case "ENGINE": return "Двигатель заведен";
            case "ASK OWN1":
            case "ASK OWN2": return "Опрос состояние";
            case "JAMM": return "Глушение сигнала GSM";
            case "LOW BAT":
            case "LOBAT":
            case "RAZRYAD AKB":
                return "Разряд аккумулятора";
            case "VZYAT": return "Взятие под охрану";
            case "SNYAT": return "Снятие с охраны";
            case "SLEEP":
            case "PWR OFF":
                return "ыключение. Встроенный аккумулятор разряжен";
            case "INIT":
            case "PWR ON":
                return "Включение питания. Инициализация";
            default: return "Событие не определено";
        }

    }
    Integer getImageByEvent (String event)
    {
        switch(event)
        {
            case "IN1": return R.drawable.in1_alarm;
            case "IN2": return R.drawable.in2_alarm;
            case "NET AKB": return R.drawable.battery_alarm;
            case "UDAR": return R.drawable.accelerometer_alarm;
            case "UGON": return R.drawable.mover_alarm;
            case "ENGINE": return R.drawable.engine_working_alarm;
            case "ASK OWN1": return R.drawable.refresh;
            case "ASK OWN2": return R.drawable.refresh;
            case "JAMM": return R.drawable.gsm_jam_alarm;
            case "LOW BAT": return R.drawable.battery_low;
            case "VZYAT": return R.drawable.locked;
            case "SNYAT": return R.drawable.unlocked;
            case "SLEEP":
            case "PWR OFF": return R.drawable.sleep;
            case "INIT":
            case "PWR ON": return R.drawable.power;
            default: return R.drawable.nullpic;
        }
    }


    public void onBackBtnPressed(View view) {
        onBackPressed();
    }
}