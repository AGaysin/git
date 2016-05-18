package tehohrana.ru.cathodmon;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.app.ProgressDialog;
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
import android.provider.BaseColumns;
import android.telephony.SmsManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.util.regex.Pattern;

/**
 * Created by AG on 19.04.2016.
 */
public class CathodeActivity extends Activity {

    ProgressDialog prog1;
    public boolean isSettingsRead = false;
    private int mLastMessageId=0;




    private DatabaseHelper mDatabaseHelper;
    private SQLiteDatabase mSqLiteDatabase;


    TextView mTextViewCathodeText;
    TextView mTextViewCathodePhone;
    TextView mTextViewCathodeType, mTextViewCathodeTitle;
    public String mDevicePhoneNumber;

    TextView    mTextViewValDateTime, mTextViewValU, mTextViewValI, mTextViewValP,
        mTextViewValDoor, mTextViewValTc, mTextViewValSvn1, mTextViewValSvn2, mTextViewValCnt,
        mTextViewVal220V, mTextViewValHeater, mTextViewValStabRs, mTextViewValStabUniver, mTextViewValtemp;

    ImageView mImageViewAlarm1,mImageViewAlarm2,mImageViewAlarm3,mImageViewAlarm4,
            mImageViewAlarm5,mImageViewAlarm6,mImageViewAlarm7;
    Button mButtonValStabParam;

    LinearLayout mLinearLayoutUseti, mLinearLayoutCathodeStabRs, mLinearLayoutCathodeStabUnivers,
    mLinearLayoutCathodeAlarms;
    int dbId;
    int dbDeviceType;

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
        setContentView(R.layout.activity_cathode);
        int dbPosition = getIntent().getIntExtra("db_cathode_position", 0);
        dbId=0;
        dbDeviceType=0;

        mTextViewCathodeText = (TextView)findViewById(R.id.textViewCathodeTextVal);
        mTextViewCathodePhone = (TextView) findViewById(R.id.textViewCathodePhoneVal);
        mTextViewCathodeType = (TextView)findViewById(R.id.textViewCathodeDeviceType);
        mTextViewCathodeTitle = (TextView) findViewById(R.id.textViewCathodeTitle);

        mLinearLayoutUseti = (LinearLayout) findViewById(R.id.linearLayoutUseti);
        mLinearLayoutCathodeStabRs = (LinearLayout) findViewById(R.id.linearLayoutCathodeStabRs);
        mLinearLayoutCathodeStabUnivers = (LinearLayout) findViewById(R.id.linearLayoutCathodeStabUniver);
        mLinearLayoutCathodeAlarms = (LinearLayout) findViewById(R.id.linearLayoutCathodeAlarms);



        mTextViewValDateTime = (TextView)findViewById(R.id.textViewCathodeDateTime);
        mTextViewValU = (TextView)findViewById(R.id.textViewCathodeUval);
        mTextViewValI = (TextView)findViewById(R.id.textViewCathodeIval);
        mTextViewValP = (TextView)findViewById(R.id.textViewCathodeFival);
        mTextViewValDoor = (TextView)findViewById(R.id.textViewCathodeDoor);
        mTextViewValTc = (TextView)findViewById(R.id.textViewCathodeTc);
        mTextViewValSvn1 = (TextView)findViewById(R.id.textViewCathodeSvn1Val);
        mTextViewValSvn2 = (TextView)findViewById(R.id.textViewCathodeSvn2Val);
        mTextViewValCnt = (TextView)findViewById(R.id.textViewCathodeElCntVal);
        mTextViewVal220V = (TextView)findViewById(R.id.textViewCathodeUsetiVal);
        mTextViewValtemp = (TextView)findViewById(R.id.textViewCathodeTempVal);
        mTextViewValHeater = (TextView)findViewById(R.id.textViewCathodeHeater);
        mTextViewValStabRs = (TextView)findViewById(R.id.textViewCathodeStabRsVal);
        mTextViewValStabUniver = (TextView)findViewById(R.id.textViewCathodeStabUniverVal);

        mImageViewAlarm1 = (ImageView)findViewById(R.id.imageViewCathodeAlarm1);
        mImageViewAlarm2 = (ImageView)findViewById(R.id.imageViewCathodeAlarm2);
        mImageViewAlarm3 = (ImageView)findViewById(R.id.imageViewCathodeAlarm3);
        mImageViewAlarm4 = (ImageView)findViewById(R.id.imageViewCathodeAlarm4);
        mImageViewAlarm5 = (ImageView)findViewById(R.id.imageViewCathodeAlarm5);
        mImageViewAlarm6 = (ImageView)findViewById(R.id.imageViewCathodeAlarm6);
        mImageViewAlarm7 = (ImageView)findViewById(R.id.imageViewCathodeAlarm7);

        mButtonValStabParam = (Button)findViewById(R.id.buttonCathodeStabRsSet);


        DatabaseHelper mDatabaseHelper = new DatabaseHelper(this, DatabaseHelper.DATABASE_NAME, null, 1);
        SQLiteDatabase cdb = mDatabaseHelper.getReadableDatabase();

        Cursor cursor = cdb.query(DatabaseHelper.DATABASE_TABLE_CATHODES, null, null, null, null, null, null) ;

        if (cursor.moveToPosition(dbPosition))
        {

            dbId = cursor.getInt(cursor.getColumnIndex(BaseColumns._ID));
            mDevicePhoneNumber = cursor.getString(cursor.getColumnIndex(DatabaseHelper.PHONE_COLUMN));
            mTextViewCathodeTitle.setText(cursor.getString(cursor.getColumnIndex(DatabaseHelper.TEXT_COLUMN)));
            mTextViewCathodePhone.setText(mDevicePhoneNumber);
            mTextViewCathodeText.setText(cursor.getString(cursor.getColumnIndex(DatabaseHelper.INFO_COLUMN)));
            dbDeviceType = cursor.getInt(cursor.getColumnIndex(DatabaseHelper.DEVICE_COLUMN));

            if (dbDeviceType==0)
            {
                //Универсальный
                if (cursor.getInt(cursor.getColumnIndex(DatabaseHelper.SIGNAL_COOLUMN))==0)
                    mTextViewCathodeType.setText("Универсальный (4-20 мА)");
                else mTextViewCathodeType.setText("Универсальный (0-5 В)");
                mTextViewValTc.setVisibility(View.VISIBLE);
                mLinearLayoutUseti.setVisibility(View.GONE);
                mLinearLayoutCathodeAlarms.setVisibility(View.GONE);
                mLinearLayoutCathodeStabRs.setVisibility(View.GONE);
                mLinearLayoutCathodeStabUnivers.setVisibility(View.VISIBLE);

            }
            else
            {
                //Интерфейсный
                mTextViewCathodeType.setText("Интерфейсный (RS-485)");
                mTextViewValTc.setVisibility(View.GONE);
                mLinearLayoutUseti.setVisibility(View.VISIBLE);
                mLinearLayoutCathodeAlarms.setVisibility(View.VISIBLE);
                mLinearLayoutCathodeStabRs.setVisibility(View.VISIBLE);
                mLinearLayoutCathodeStabUnivers.setVisibility(View.GONE);
            }



            //Последние показания станции
            mTextViewValDateTime.setText(cursor.getString(cursor.getColumnIndex(DatabaseHelper.VAL_DATETIME_COLUMN)));
            mTextViewValU.setText(cursor.getString(cursor.getColumnIndex(DatabaseHelper.VAL_U_COLUMN )));
            mTextViewValI.setText(cursor.getString(cursor.getColumnIndex(DatabaseHelper.VAL_I_COLUMN )));
            mTextViewValP.setText(cursor.getString(cursor.getColumnIndex(DatabaseHelper.VAL_P_COLUMN )));
            if (cursor.getInt(cursor.getColumnIndex(DatabaseHelper.VAL_DOOR_COLUMN  ))==0)
            {
                //дверь закрыта
                mTextViewValDoor.setText("ДВЕРЬ ЗАКРЫТА");
                mTextViewValDoor.setTextColor(getResources().getColor(R.color.green));
            }
            else
            {
                //Дверь открыта
                mTextViewValDoor.setText("ДВЕРЬ ОТКРЫТА");
                mTextViewValDoor.setTextColor(getResources().getColor(R.color.red));
            }

            if (cursor.getInt(cursor.getColumnIndex(DatabaseHelper.VAL_TC_COLUMN))==0)
            {
                //TC-резерв = 0
                mTextViewValTc.setText("TC-резерв = 0 (замкнут)");
                mTextViewValTc.setTextColor(getResources().getColor(R.color.green));
            }
            else
            {
                //TC-резерв = 1
                mTextViewValTc.setText("TC-резерв = 1 (разомкнут)");
                mTextViewValTc.setTextColor(getResources().getColor(R.color.red));
            }

            mTextViewValSvn1.setText(String.valueOf(cursor.getInt(cursor.getColumnIndex(DatabaseHelper.VAL_SVN1_COLUMN))));
            mTextViewValSvn2.setText(String.valueOf(cursor.getInt(cursor.getColumnIndex(DatabaseHelper.VAL_SVN2_COLUMN))));
            mTextViewValCnt.setText(String.valueOf(cursor.getInt(cursor.getColumnIndex(DatabaseHelper.VAL_CNT_COLUMN))));
            mTextViewVal220V.setText(String.valueOf(cursor.getInt(cursor.getColumnIndex(DatabaseHelper.VAL_220_COLUMN))));
            mTextViewValtemp.setText(String.valueOf(cursor.getInt(cursor.getColumnIndex(DatabaseHelper.VAL_TEMP_COLUMN))));

            if (cursor.getInt(cursor.getColumnIndex(DatabaseHelper.VAL_HEATER_COLUMN ))==0)
            {
                //ТЕРМОСТАТ ОТКЛЮЧЕН
                mTextViewValHeater.setText("ТЕРМОСТАТ ОТКЛЮЧЕН");
                mTextViewValHeater.setTextColor(getResources().getColor(R.color.gray_time));
            }
            else
            {
                //ТЕРМОСТАТ ВКЛЮЧЕН
                mTextViewValHeater.setText("ТЕРМОСТАТ ВКЛЮЧЕН");
                mTextViewValHeater.setTextColor(getResources().getColor(R.color.red));
            }




            mTextViewValStabRs.setText(cursor.getString(cursor.getColumnIndex(DatabaseHelper.VAL_STAB_VAL_COLUMN )));
            mTextViewValStabUniver.setText(cursor.getString(cursor.getColumnIndex(DatabaseHelper.VAL_STAB_VAL_COLUMN )));

            switch (cursor.getInt(cursor.getColumnIndex(DatabaseHelper.VAL_STAB_PARAM_COLUMN)))
            {
                case 1: mButtonValStabParam.setText("Напряжение, В (Изменить)"); break;
                case 2: mButtonValStabParam.setText("Ток, А (Изменить)"); break;
                case 3: mButtonValStabParam.setText("Потенциал, В (Изменить)"); break;
                default: mButtonValStabParam.setText("Неизвестный параметр (Изменить)");
            }

            if (cursor.getInt(cursor.getColumnIndex(DatabaseHelper.VAL_ALARM1_COLUMN))==0)
                mImageViewAlarm1.setImageBitmap(BitmapFactory.decodeResource(
                    this.getResources(), R.drawable.led_gray));
            else mImageViewAlarm1.setImageBitmap(BitmapFactory.decodeResource(
                    this.getResources(), R.drawable.led_green));

            if (cursor.getInt(cursor.getColumnIndex(DatabaseHelper.VAL_ALARM2_COLUMN))==0)
                mImageViewAlarm2.setImageBitmap(BitmapFactory.decodeResource(
                        this.getResources(), R.drawable.led_gray));
            else mImageViewAlarm2.setImageBitmap(BitmapFactory.decodeResource(
                    this.getResources(), R.drawable.led_green));

            if (cursor.getInt(cursor.getColumnIndex(DatabaseHelper.VAL_ALARM3_COLUMN))==0)
                mImageViewAlarm3.setImageBitmap(BitmapFactory.decodeResource(
                        this.getResources(), R.drawable.led_gray));
            else mImageViewAlarm3.setImageBitmap(BitmapFactory.decodeResource(
                    this.getResources(), R.drawable.led_green));

            if (cursor.getInt(cursor.getColumnIndex(DatabaseHelper.VAL_ALARM4_COLUMN))==0)
                mImageViewAlarm4.setImageBitmap(BitmapFactory.decodeResource(
                        this.getResources(), R.drawable.led_gray));
            else mImageViewAlarm4.setImageBitmap(BitmapFactory.decodeResource(
                    this.getResources(), R.drawable.led_green));

            if (cursor.getInt(cursor.getColumnIndex(DatabaseHelper.VAL_ALARM5_COLUMN))==0)
                mImageViewAlarm5.setImageBitmap(BitmapFactory.decodeResource(
                        this.getResources(), R.drawable.led_gray));
            else mImageViewAlarm5.setImageBitmap(BitmapFactory.decodeResource(
                    this.getResources(), R.drawable.led_green));

            if (cursor.getInt(cursor.getColumnIndex(DatabaseHelper.VAL_ALARM6_COLUMN))==0)
                mImageViewAlarm6.setImageBitmap(BitmapFactory.decodeResource(
                        this.getResources(), R.drawable.led_gray));
            else mImageViewAlarm6.setImageBitmap(BitmapFactory.decodeResource(
                    this.getResources(), R.drawable.led_green));

            if (cursor.getInt(cursor.getColumnIndex(DatabaseHelper.VAL_ALARM7_COLUMN))==0)
                mImageViewAlarm7.setImageBitmap(BitmapFactory.decodeResource(
                        this.getResources(), R.drawable.led_gray));
            else mImageViewAlarm7.setImageBitmap(BitmapFactory.decodeResource(
                    this.getResources(), R.drawable.led_green));



        }

        cursor.close();
        cdb.close();
        mDatabaseHelper.close();



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
        super.onBackPressed();

    }

    public void onBackBtnPressed(View view) {
        super.onBackPressed();
    }

    public void onBtnCathodeCallClick(View view) {
    }

    public void onBtnCathodeAskSmsClick(View view) {
        AlertDialog.Builder sendSmsIdGetDialog = new AlertDialog.Builder(this);
        sendSmsIdGetDialog.setTitle("Будет отправлена SMS-команда! Вы уверены?");

        sendSmsIdGetDialog.setPositiveButton("Да", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                mLastMessageId = getLastMessageId();
                sendSMS(mDevicePhoneNumber, "&SET?");
                prog1 = new ProgressDialog(CathodeActivity.this);
                prog1.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                prog1.setMessage("Ожидание запроса конфигурации оборудования...");
                prog1.setIndeterminate(true); // выдать значек ожидания
                prog1.setCancelable(false);
                timerSmsCommandWaiting = 90;
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



    public void checkNewSms(){

        if (readSmsParameters(mLastMessageId))
        {
            prog1.dismiss();
            //Вывсти сообщение, что параметры успешно обновлены
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            isSettingsRead = true;


            //showParameters();
            builder.setTitle("Получено новое состояние станции")
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

        prog1.setMessage("Ожидание ответа от станции ...(" + timerSmsCommandWaiting + ")");
        //Toast.makeText(getApplicationContext(), "check " + timerSmsCommandWaiting, Toast.LENGTH_SHORT).show();
        //prog1.cancel();
    }

    public void cancelWaiting(){
        prog1.dismiss();
        //Вывести сообщение о таймауте
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Время вышло!")
                .setMessage("Время ожидания SMS-сообщения истекло!")
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


    public boolean readSmsParameters(int mId){
        final String SMS_URI_INBOX = "content://sms/inbox";
        if (!mDevicePhoneNumber.isEmpty()) {
            try {
                String mDevicePhoneNumberRequest = "address='+" + mDevicePhoneNumber + "'";
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

                        //проверка протокола
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


                            }
                            cur.close();
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
                String mDevicePhoneNumberRequest = "address='+" + mDevicePhoneNumber + "'";
                String[] projection = new String[]{"_id", "address", "person", "body", "date", "type"};
                Cursor cur = getContentResolver().query(Uri.parse(SMS_URI_INBOX),
                        projection,
                        mDevicePhoneNumberRequest,
                        null,
                        "date desc");
                //startManagingCursor(cur);

                if (cur.getCount()>0)
                {
                    cur.moveToFirst();
                    int getLastIndexId=cur.getInt(cur.getColumnIndex("_id"));
                    cur.close();
                    return getLastIndexId;
                }
                cur.close();
            }
            catch (Exception ex) {
                Log.d("SQLiteException", ex.getMessage());
            }
        }
        return 0;
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

}