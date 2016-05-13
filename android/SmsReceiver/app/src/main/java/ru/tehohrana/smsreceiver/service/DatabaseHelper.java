package ru.tehohrana.smsreceiver.service;


import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;
import android.util.Log;


public class DatabaseHelper extends SQLiteOpenHelper implements BaseColumns{

    private static final String DATABASE_NAME = "sms_receiver_alpha.db";
    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_TABLE = "auto_gps";

    public static final String PACKET_ID_COLUMN = "packet_id";
    public static final String TRANSMITTER_COLUMN = "transmitter";
    public static final String RECEIVED_DATE = "received_date";
    public static final String RECEIVED_TIME = "received_time";
    public static final String TRANSMITTER_BALANCE = "balance";
    public static final String RECEIVER_COLUMN = "receiver";
    public static final String EVENT_NUM_COLUMN = "event_num";
    public static final String EVENT_TYPE_COLUMN = "event_type";
    public static final String DATA_TEMP_COLUMN = "data_temp";
    public static final String DATA_VOLT_COLUMN = "data_volt";
    public static final String GSM_ANT_COLUMN = "gsm_ant";
    public static final String GPS_TIME_COLUMN = "gps_time";
    public static final String GPS_DATE_COLUMN = "gps_date";
    public static final String GPS_LONG_COLUMN = "gps_long";
    public static final String GPS_LAT_COLUMN = "gps_lat";
    public static final String GPS_SPEED_COLUMN = "gps_speed";
    public static final String GPS_FIX_3D = "gps_fix";
    public static final String MASK_VALET = "mask_valet";
    public static final String MASK_BT_GUARD = "mask_bt_guard";
    public static final String MASK_UGON = "mask_ugon";
    public static final String MASK_LOBAT = "mask_lowbat";
    public static final String MASK_IN1 = "mask_in1";
    public static final String MASK_IN2 = "mask_in2";
    public static final String MASK_HORN = "mask_horn";
    public static final String MASK_BLOCK = "mask_block";
    public static final String MASK_GUARD = "mask_guard";
    public static final String MASK_MOVE = "mask_move";
    public static final String MASK_NOBAT = "mask_nobat";
    public static final String MASK_JAMM = "mask_jamm";
    public static final String MASK_ENGINE = "mask_engine";





    private static final String DATABASE_CREATE_SCRIPT = "create table "
            + DATABASE_TABLE + " (" + BaseColumns._ID
            + " integer primary key autoincrement, "
            + PACKET_ID_COLUMN  + " integer, "
            + TRANSMITTER_COLUMN  + " text not null, "
            + RECEIVER_COLUMN  + " text not null, "
            + EVENT_NUM_COLUMN  + " text not null, "
            + EVENT_TYPE_COLUMN  + " text not null, "
            + DATA_TEMP_COLUMN + " text not null, "
            + DATA_VOLT_COLUMN + " text not null, "
            + GSM_ANT_COLUMN  + " text not null, "
            + GPS_TIME_COLUMN  + " text not null, "
            + GPS_DATE_COLUMN  + " text not null, "
            + GPS_LONG_COLUMN  + " text not null, "
            + GPS_LAT_COLUMN  + " text not null, "
            + GPS_SPEED_COLUMN  + " text not null, "
            + RECEIVED_DATE  + " text not null, "
            + RECEIVED_TIME  + " text not null, "
            + TRANSMITTER_BALANCE  + " text not null, "
            + GPS_FIX_3D  + " integer, "
            + MASK_VALET  + " integer, "
            + MASK_BT_GUARD  + " integer, "
            + MASK_UGON  + " integer, "
            + MASK_LOBAT  + " integer, "
            + MASK_IN1  + " integer, "
            + MASK_IN2  + " integer, "
            + MASK_HORN  + " integer, "
            + MASK_BLOCK  + " integer, "
            + MASK_GUARD  + " integer, "
            + MASK_MOVE  + " integer, "
            + MASK_NOBAT  + " integer, "
            + MASK_JAMM  + " integer, "
            + MASK_ENGINE + " integer);";



    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public DatabaseHelper(Context context, String name, SQLiteDatabase.CursorFactory factory,
                          int version) {
        super(context, name, factory, version);
    }

    /*public DatabaseHelper(Context context, String name, SQLiteDatabase.CursorFactory factory,
                          int version, DatabaseErrorHandler errorHandler) {
        super(context, name, factory, version, errorHandler);
    }*/

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(DATABASE_CREATE_SCRIPT);

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Запишем в журнал
        Log.w("SQLite", "Обновляемся с версии " + oldVersion + " на версию " + newVersion);

        // Удаляем старую таблицу и создаём новую
        db.execSQL("DROP TABLE IF IT EXISTS " + DATABASE_TABLE);
        // Создаём новую таблицу
        onCreate(db);

    }




}