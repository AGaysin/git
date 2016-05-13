package ru.tehohrana.smsreceiver;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import ru.tehohrana.smsreceiver.service.DatabaseHelper;



public class ArchieveActivity extends Activity{
    public final static String EVENT_ID_ARCHIEVE = "ru.tehohrana.SmartMon.EVENT_ID_ARCHIEVE";

    private DatabaseHelper mDatabaseHelper;


    public final static String GPS_LONGITUDE = "ru.tehohrana.SmartMon.GPS_LONGITUDE";
    public final static String GPS_LATITUDE = "ru.tehohrana.SmartMon.GPS_LATITUDE";


    public double gpsLatitude, gpsLongitude;

    public SharedPreferences mSettings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event);
        mSettings = PreferenceManager.getDefaultSharedPreferences(this);

        int mEventId = getIntent().getExtras().getInt(EVENT_ID_ARCHIEVE);


        ImageView mImageViewAlarm = (ImageView) findViewById(R.id.imageViewAlarmArchieve);
        TextView mTextViewAlarml = (TextView) findViewById(R.id.textViewAlarmArchieve);

        ImageView mImageViewStateGuard = (ImageView) findViewById(R.id.imageViewStateGuardArchieve);
        ImageView mImageViewStateIn1 = (ImageView) findViewById(R.id.imageViewStateIn1Archieve);
        ImageView mImageViewStateIn2 = (ImageView) findViewById(R.id.imageViewStateIn2Archieve);
        ImageView mImageViewStateMove = (ImageView) findViewById(R.id.imageViewStateMoveArchieve);
        ImageView mImageViewStateEngine = (ImageView) findViewById(R.id.imageViewStateEngineArchieve);
        ImageView mImageViewStateBat = (ImageView) findViewById(R.id.imageViewStateBatArchieve);
        ImageView mImageViewStateJamm = (ImageView) findViewById(R.id.imageViewStateJammArchieve);
        ImageView mImageViewStateHorn = (ImageView) findViewById(R.id.imageViewStateHornArchieve);
        ImageView mImageViewStateBlock = (ImageView) findViewById(R.id.imageViewStateBlockArchieve);

        ImageView mImageViewScreenGsmLevel = (ImageView) findViewById(R.id.imageViewScreenGsmLevelArchieve);
        ImageView mImageViewStateUgon = (ImageView) findViewById(R.id.imageViewStateUgonArchieve);
        ImageView mImageViewScreenBat = (ImageView) findViewById(R.id.imageViewScreenBatArchieve);

        ImageView mImageViewBluetoothMode = (ImageView) findViewById(R.id.imageViewBluetoothMode);
        ImageView mImageViewValetMode = (ImageView) findViewById(R.id.imageViewValetMode);


        TextView mTextViewScreenReceiver = (TextView)findViewById(R.id.textViewScreenReceiverArchieve);
        TextView mTextViewScreenReceiverDate = (TextView)findViewById(R.id.textViewScreenReceiverDateArchieve);
        TextView mTextViewScreenGmsBalance = (TextView)findViewById(R.id.textViewScreenGmsBalanceArchieve);


        TextView mTextViewScreenGpsArchieve = (TextView) findViewById(R.id.textViewScreenGpsArchieve);
        TextView mTextViewBatVal = (TextView)findViewById(R.id.textViewBatValArchieve);
        TextView mTextViewTempVal = (TextView)findViewById(R.id.textViewTempValArchieve);
        TextView mTextViewDateVal = (TextView)findViewById(R.id.textViewDateValArchieve);
        TextView mTextViewTimeVal = (TextView)findViewById(R.id.textViewTimeValArchieve);
        TextView mTextViewSpeedVal = (TextView)findViewById(R.id.textViewSpeedValArchieve);

        int mPacketId = 0;
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

        mDatabaseHelper = new DatabaseHelper(this);
        SQLiteDatabase mSqLiteDatabase;
        mSqLiteDatabase = mDatabaseHelper.getReadableDatabase();

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
            while(!cursor.isFirst() && mEventId != 0)
            {
                mEventId--;
                cursor.moveToPrevious();
            }

            //Читаем текущие данные из базы
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
                    mImageViewAlarm.setImageBitmap(BitmapFactory.decodeResource(
                            this.getResources(), R.drawable.in1_alarm));
                    break;
                case "IN2":
                    mTextViewAlarml.setText(mSettings.getString("pref_in2", "Тревога. Зона ШС2"));
                    mTextViewAlarml.setTextColor(getResources().getColor(R.color.red));
                    mImageViewAlarm.setImageBitmap(BitmapFactory.decodeResource(
                            this.getResources(), R.drawable.in2_alarm));
                    break;
                case "NET AKB":
                case "NO BAT":
                case "NOBAT":
                    mTextViewAlarml.setText("Тревога. Отключен аккумулятор");
                    mTextViewAlarml.setTextColor(getResources().getColor(R.color.red));
                    mImageViewAlarm.setImageBitmap(BitmapFactory.decodeResource(
                            this.getResources(), R.drawable.battery_alarm));
                    break;
                case "UDAR":
                case "MOVE":
                    mTextViewAlarml.setText("Тревога. Сработка датчика акселерометра");
                    mTextViewAlarml.setTextColor(getResources().getColor(R.color.red));
                    mImageViewAlarm.setImageBitmap(BitmapFactory.decodeResource(
                            this.getResources(), R.drawable.accelerometer_alarm));
                    break;
                case "UGON":
                    mTextViewAlarml.setText("Тревога. Объект находится вне охраняемого периметра");
                    mTextViewAlarml.setTextColor(getResources().getColor(R.color.red));
                    mImageViewAlarm.setImageBitmap(BitmapFactory.decodeResource(
                            this.getResources(), R.drawable.mover_alarm));
                    break;
                case "ENGINE":
                    mTextViewAlarml.setText("Тревога. Двигатель заведен");
                    mTextViewAlarml.setTextColor(getResources().getColor(R.color.red));
                    mImageViewAlarm.setImageBitmap(BitmapFactory.decodeResource(
                            this.getResources(), R.drawable.engine_working_alarm));
                    break;
                case "ASK OWN1":
                    mTextViewAlarml.setText("Новое состояние. Опрос собственником 1");
                    mTextViewAlarml.setTextColor(getResources().getColor(R.color.white));
                    mImageViewAlarm.setImageBitmap(BitmapFactory.decodeResource(
                            this.getResources(), R.drawable.refresh));
                    break;
                case "ASK OWN2":
                    mTextViewAlarml.setText("Новое состояние. Опрос собственником 2");
                    mTextViewAlarml.setTextColor(getResources().getColor(R.color.white));
                    mImageViewAlarm.setImageBitmap(BitmapFactory.decodeResource(
                            this.getResources(), R.drawable.refresh));
                    break;
                case "JAMM":
                    mTextViewAlarml.setText("Тревога. Зафиксировано глушени GSM-сигнала");
                    mTextViewAlarml.setTextColor(getResources().getColor(R.color.red));
                    mImageViewAlarm.setImageBitmap(BitmapFactory.decodeResource(
                            this.getResources(), R.drawable.gsm_jam_alarm));
                    break;

                case "LOW BAT":
                case "LOBAT":
                case "RAZRYAD AKB":
                    mTextViewAlarml.setText("Внимание. Аккумулятор разряжен");
                    mTextViewAlarml.setTextColor(getResources().getColor(R.color.yellow));
                    mImageViewAlarm.setImageBitmap(BitmapFactory.decodeResource(
                            this.getResources(), R.drawable.battery_low));
                    break;
                case "VZYAT":
                    mTextViewAlarml.setText("Внимание. Объект взят под охрану");
                    mTextViewAlarml.setTextColor(getResources().getColor(R.color.white));
                    mImageViewAlarm.setImageBitmap(BitmapFactory.decodeResource(
                            this.getResources(), R.drawable.locked));
                    break;
                case "SNYAT":
                    mTextViewAlarml.setText("Внимание. Объект снят с охраны");
                    mTextViewAlarml.setTextColor(getResources().getColor(R.color.white));
                    mImageViewAlarm.setImageBitmap(BitmapFactory.decodeResource(
                            this.getResources(), R.drawable.unlocked));
                    break;
                case "SLEEP":
                case "PWR OFF":
                    mTextViewAlarml.setText("Выключение. Встроенный аккумулятор разряжен");
                    mTextViewAlarml.setTextColor(getResources().getColor(R.color.white));
                    mImageViewAlarm.setImageBitmap(BitmapFactory.decodeResource(
                            this.getResources(), R.drawable.sleep));
                    break;
                case "INIT":
                case "PWR ON":
                    mTextViewAlarml.setText("Включение питания. Инициализация");
                    mTextViewAlarml.setTextColor(getResources().getColor(R.color.white));
                    mImageViewAlarm.setImageBitmap(BitmapFactory.decodeResource(
                            this.getResources(), R.drawable.power));
                    break;
                default:
                    mTextViewAlarml.setText("Тип сообщения не опознан");
                    mTextViewAlarml.setTextColor(getResources().getColor(R.color.yellow));
                    mImageViewAlarm.setImageBitmap(BitmapFactory.decodeResource(
                            this.getResources(), R.drawable.nullpic));
            }

            //Закрывает курсор
            if (!cursor.isClosed()) {
                cursor.close();
                cursor = null;
            }

        }
        catch (Exception ex)
        {
            Toast.makeText(this, "Exception: " + ex.toString(), Toast.LENGTH_LONG).show();

        }




        //Отображаем последнее состояние на главной активности



        if (mMaskGuard) mImageViewStateGuard.setImageBitmap(BitmapFactory.decodeResource(
                this.getResources(), R.drawable.locked));
        else mImageViewStateGuard.setImageBitmap(BitmapFactory.decodeResource(
                this.getResources(), R.drawable.unlocked));

        if (mMaskIn1) mImageViewStateIn1.setImageBitmap(BitmapFactory.decodeResource(
                this.getResources(), R.drawable.in1_alarm));
        else mImageViewStateIn1.setImageBitmap(BitmapFactory.decodeResource(
                this.getResources(), R.drawable.in1));

        if (mMaskIn2) mImageViewStateIn2.setImageBitmap(BitmapFactory.decodeResource(
                this.getResources(), R.drawable.in2_alarm));
        else mImageViewStateIn2.setImageBitmap(BitmapFactory.decodeResource(
                this.getResources(), R.drawable.in2));

        if (mMaskMove) mImageViewStateMove.setImageBitmap(BitmapFactory.decodeResource(
                this.getResources(), R.drawable.accelerometer_alarm));
        else mImageViewStateMove.setImageBitmap(BitmapFactory.decodeResource(
                this.getResources(), R.drawable.accelerometer));

        if (mMaskEngine) {
            if (mMaskGuard) mImageViewStateEngine.setImageBitmap(BitmapFactory.decodeResource(
                    this.getResources(), R.drawable.engine_working_alarm));
            else mImageViewStateEngine.setImageBitmap(BitmapFactory.decodeResource(
                    this.getResources(), R.drawable.engine_working));
        } else mImageViewStateEngine.setImageBitmap(BitmapFactory.decodeResource(
                this.getResources(), R.drawable.engine_locked));

        if (mMaskNobat) mImageViewStateBat.setImageBitmap(BitmapFactory.decodeResource(
                this.getResources(), R.drawable.battery_alarm));
        else if (mMaskLobat) mImageViewStateBat.setImageBitmap(BitmapFactory.decodeResource(
                this.getResources(), R.drawable.battery_low));
        else mImageViewStateBat.setImageBitmap(BitmapFactory.decodeResource(
                    this.getResources(), R.drawable.battery));

        if (mMaskJamm) mImageViewStateJamm.setImageBitmap(BitmapFactory.decodeResource(
                this.getResources(), R.drawable.gsm_jam_alarm));
        else mImageViewStateJamm.setImageBitmap(BitmapFactory.decodeResource(
                this.getResources(), R.drawable.gsm_jam));

        if (mMaskHorn) mImageViewStateHorn.setImageBitmap(BitmapFactory.decodeResource(
                this.getResources(), R.drawable.siren_on_small));
        else mImageViewStateHorn.setImageBitmap(BitmapFactory.decodeResource(
                this.getResources(), R.drawable.siren_off_small));

        if (mMaskBlock) mImageViewStateBlock.setImageBitmap(BitmapFactory.decodeResource(
                this.getResources(), R.drawable.engine_locked));
        else mImageViewStateBlock.setImageBitmap(BitmapFactory.decodeResource(
                this.getResources(), R.drawable.engine_working));

        if (mMaskUgon) mImageViewStateUgon.setImageBitmap(BitmapFactory.decodeResource(
                this.getResources(), R.drawable.mover_alarm));
        else mImageViewStateUgon.setImageBitmap(BitmapFactory.decodeResource(
                this.getResources(), R.drawable.mover));



        if (mMaskBtGuard) mImageViewBluetoothMode.setImageBitmap(BitmapFactory.decodeResource(
                this.getResources(),R.drawable.bluetooth_on));
        else mImageViewBluetoothMode.setImageBitmap(BitmapFactory.decodeResource(
                this.getResources(),R.drawable.bluetooth_off));

        if (mMaskValet) mImageViewValetMode.setImageBitmap(BitmapFactory.decodeResource(
                this.getResources(),R.drawable.position_track_on));
        else mImageViewValetMode.setImageBitmap(BitmapFactory.decodeResource(
                this.getResources(),R.drawable.position_track_off));


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


        if (mMask3DFix == 2)
        {
            mTextViewScreenGpsArchieve.setTextColor(getResources().getColor(R.color.green));
        }
        else if (mMask3DFix == 1)
        {
            mTextViewScreenGpsArchieve.setTextColor(getResources().getColor(R.color.yellow));
        }
        else
        {
            mTextViewScreenGpsArchieve.setTextColor(getResources().getColor(R.color.red));
        }



        gpsLatitude = Double.parseDouble(mGpsLat);
        gpsLongitude = Double.parseDouble(mGpsLong);
    }

    public void onBtnShowGpsOnMapArchieve(View view) {
        Intent intent = new Intent(ArchieveActivity.this, MapsActivity.class);
        intent.putExtra(GPS_LONGITUDE,gpsLatitude);
        intent.putExtra(GPS_LATITUDE,gpsLongitude);
        startActivity(intent);
    }

    public void onBtnGpsShowArchieveClick(View view) {
        Intent intent = new Intent(ArchieveActivity.this, MapsActivity.class);
        intent.putExtra(GPS_LONGITUDE,gpsLatitude);
        intent.putExtra(GPS_LATITUDE,gpsLongitude);
        startActivity(intent);
    }

    public void onBackBtnPressed(View view) {
        onBackPressed();
    }
}
