package tehohrana.ru.cathodmon;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;
import android.util.Log;


public class DatabaseHelper extends SQLiteOpenHelper implements BaseColumns{

    public static final String DATABASE_NAME = "CathodMon.db";
    private static final int DATABASE_VERSION = 3;
    public static final String DATABASE_TABLE_CATHODES = "Cathodes";

    public static final String TEXT_COLUMN = "description_text";
    public static final String PHONE_COLUMN = "phone_num";
    public static final String DEVICE_COLUMN = "device_type";
    public static final String SIGNAL_COOLUMN = "signal_type";
    public static final String INFO_COLUMN = "device_info";
    public static final String IMAX_COLUMN = "max_current";
    public static final String UMAX_COLUMN = "max_voltage";
    public static final String FIMAX_COLUMN = "max_potential";
    public static final String CNT_BEGIN_COLUMN = "counter_begin";
    public static final String CNT_SCALE_COLUMN = "counter_scale";

    public static final String VAL_DATETIME_COLUMN = "val_datetime";
    public static final String VAL_U_COLUMN = "val_u";
    public static final String VAL_I_COLUMN = "val_i";
    public static final String VAL_P_COLUMN = "val_p";
    public static final String VAL_DOOR_COLUMN = "val_door";
    public static final String VAL_TC_COLUMN = "val_tc";
    public static final String VAL_SVN1_COLUMN = "val_svn1";
    public static final String VAL_SVN2_COLUMN = "val_svn2";
    public static final String VAL_CNT_COLUMN = "val_cnt";
    public static final String VAL_220_COLUMN = "val_220";
    public static final String VAL_TEMP_COLUMN = "val_temp";
    public static final String VAL_HEATER_COLUMN = "val_heater";
    public static final String VAL_STAB_PARAM_COLUMN = "val_stab_param";
    public static final String VAL_STAB_VAL_COLUMN = "val_stab_val";
    public static final String VAL_ALARMS_MASK_COLUMN = "val_alarm_mask";
    public static final String VAL_STAB_OK_COLUMN = "val_stab_ok";


    private static final String DATABASE_CREATE_SCRIPT = "create table "
            + DATABASE_TABLE_CATHODES + " (" + BaseColumns._ID
            + " integer primary key autoincrement, "
            + TEXT_COLUMN  + " text not null, "
            + PHONE_COLUMN  + " text not null, "
            + INFO_COLUMN  + " text not null, "
            + DEVICE_COLUMN  + " integer, "
            + SIGNAL_COOLUMN  + " integer, "
            + IMAX_COLUMN + " integer, "
            + UMAX_COLUMN + " integer, "
            + FIMAX_COLUMN + " integer, "
            + CNT_BEGIN_COLUMN + " integer, "
            + CNT_SCALE_COLUMN + " integer, "
            + VAL_DATETIME_COLUMN + " text not null, "
            + VAL_U_COLUMN + " integer, "
            + VAL_I_COLUMN + " integer, "
            + VAL_P_COLUMN + " integer, "
            + VAL_DOOR_COLUMN + " integer, "
            + VAL_TC_COLUMN + " integer, "
            + VAL_SVN1_COLUMN + " integer, "
            + VAL_SVN2_COLUMN + " integer, "
            + VAL_CNT_COLUMN + " integer, "
            + VAL_220_COLUMN + " integer, "
            + VAL_TEMP_COLUMN + " integer, "
            + VAL_HEATER_COLUMN + " integer, "
            + VAL_STAB_PARAM_COLUMN + " integer, "
            + VAL_STAB_VAL_COLUMN + " integer, "
            + VAL_ALARMS_MASK_COLUMN + " integer, "
            + VAL_STAB_OK_COLUMN  + " integer);";



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
        db.execSQL("DROP TABLE IF IT EXISTS " + DATABASE_TABLE_CATHODES);
        // Создаём новую таблицу
        onCreate(db);

    }




}