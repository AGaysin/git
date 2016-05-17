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
    TextView mTextViewCathodeTc;

    LinearLayout mLinearLayoutUseti, mLinearLayoutCathodeStabRs, mLinearLayoutCathodeStabUnivers,
    mLinearLayoutCathodeAlarms;


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
        int dbId=0;
        int dbDeviceType=0;

        mTextViewCathodeText = (TextView)findViewById(R.id.textViewCathodeTextVal);
        mTextViewCathodePhone = (TextView) findViewById(R.id.textViewCathodePhoneVal);
        mTextViewCathodeType = (TextView)findViewById(R.id.textViewCathodeDeviceType);
        mTextViewCathodeTitle = (TextView) findViewById(R.id.textViewCathodeTitle);
        mTextViewCathodeTc = (TextView)findViewById(R.id.textViewCathodeTc);

        mLinearLayoutUseti = (LinearLayout) findViewById(R.id.linearLayoutUseti);
        mLinearLayoutCathodeStabRs = (LinearLayout) findViewById(R.id.linearLayoutCathodeStabRs);
        mLinearLayoutCathodeStabUnivers = (LinearLayout) findViewById(R.id.linearLayoutCathodeStabUniver);
        mLinearLayoutCathodeAlarms = (LinearLayout) findViewById(R.id.linearLayoutCathodeAlarms);




        DatabaseHelper mDatabaseHelper = new DatabaseHelper(this, "CathodMon2.db", null, 1);
        SQLiteDatabase cdb = mDatabaseHelper.getReadableDatabase();

        Cursor cursor = cdb.query("Cathodes", null, null, null, null, null, null) ;

        if (cursor.moveToPosition(dbPosition))
        {

            dbId = cursor.getInt(cursor.getColumnIndex(BaseColumns._ID));
            mDevicePhoneNumber = cursor.getString(cursor.getColumnIndex(DatabaseHelper.PHONE_COLUMN));
            mTextViewCathodeTitle.setText(cursor.getString(cursor.getColumnIndex(DatabaseHelper.TEXT_COLUMN)));
            mTextViewCathodePhone.setText(mDevicePhoneNumber);

            dbDeviceType = cursor.getInt(cursor.getColumnIndex(DatabaseHelper.DEVICE_COLUMN));

            if (dbDeviceType==0)
            {
                //Универсальный
                if (cursor.getInt(cursor.getColumnIndex(DatabaseHelper.SIGNAL_COOLUMN))==0)
                    mTextViewCathodeType.setText("Универсальный (4-20 мА)");
                else mTextViewCathodeType.setText("Универсальный (0-5 В)");
                mTextViewCathodeTc.setVisibility(View.VISIBLE);
                mLinearLayoutUseti.setVisibility(View.GONE);
                mLinearLayoutCathodeAlarms.setVisibility(View.GONE);
                mLinearLayoutCathodeStabRs.setVisibility(View.GONE);
                mLinearLayoutCathodeStabUnivers.setVisibility(View.VISIBLE);

            }
            else
            {
                //Интерфейсный
                mTextViewCathodeType.setText("Интерфейсный (RS-485)");
                mTextViewCathodeTc.setVisibility(View.GONE);
                mLinearLayoutUseti.setVisibility(View.VISIBLE);
                mLinearLayoutCathodeAlarms.setVisibility(View.VISIBLE);
                mLinearLayoutCathodeStabRs.setVisibility(View.VISIBLE);
                mLinearLayoutCathodeStabUnivers.setVisibility(View.GONE);
            }






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
//                prog1 = new ProgressDialog(getBaseContext());
//                prog1.setProgressStyle(ProgressDialog.STYLE_SPINNER);
//                prog1.setMessage("Ожидание запроса конфигурации оборудования...");
//                prog1.setIndeterminate(true); // выдать значек ожидания
//                prog1.setCancelable(false);
//                timerSmsCommandWaiting = 60;
//                prog1.show();
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
            AlertDialog.Builder builder = new AlertDialog.Builder(getBaseContext());
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
        AlertDialog.Builder builder = new AlertDialog.Builder(getBaseContext());
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