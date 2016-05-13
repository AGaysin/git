package tehohrana.ru.cathodmon;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;
import android.util.Log;


public class DatabaseHelper extends SQLiteOpenHelper implements BaseColumns{

    private static final String DATABASE_NAME = "CathodMon2.db";
    private static final int DATABASE_VERSION = 2;
    private static final String DATABASE_TABLE = "Cathodes";

    public static final String TEXT_COLUMN = "description_text";
    public static final String PHONE_COLUMN = "phone_num";
    public static final String DEVICE_COLUMN = "device_type";
    public static final String SIGNAL_COOLUMN = "signal_type";
    public static final String IMAX_COLUMN = "max_current";
    public static final String UMAX_COLUMN = "max_voltage";
    public static final String FIMAX_COLUMN = "max_potential";
    public static final String CNT_BEGIN_COLUMN = "counter_begin";
    public static final String CNT_SCALE_COLUMN = "counter_scale";






    private static final String DATABASE_CREATE_SCRIPT = "create table "
            + DATABASE_TABLE + " (" + BaseColumns._ID
            + " integer primary key autoincrement, "
            + TEXT_COLUMN  + " text not null, "
            + PHONE_COLUMN  + " text not null, "
            + DEVICE_COLUMN  + " integer, "
            + SIGNAL_COOLUMN  + " integer, "
            + IMAX_COLUMN + " integer, "
            + UMAX_COLUMN + " integer, "
            + FIMAX_COLUMN + " integer, "
            + CNT_BEGIN_COLUMN + " integer, "
            + CNT_SCALE_COLUMN  + " integer);";



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