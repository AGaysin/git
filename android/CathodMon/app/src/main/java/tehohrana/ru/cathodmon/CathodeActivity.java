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
import android.preference.PreferenceManager;
import android.provider.BaseColumns;
import android.support.v4.content.LocalBroadcastManager;
import android.telephony.SmsManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Base64;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.nio.charset.Charset;
import java.util.regex.Pattern;

/**
 * Created by AG on 19.04.2016.
 */
public class CathodeActivity extends Activity implements SeekBar.OnSeekBarChangeListener{


    TextView mTextViewControlSetValue;
    RadioButton mRadioButtonI, mRadioButtonU, mRadioButtonP;
    EditText mEditTextControlSetValue;
    SeekBar mSeekBarControlSetValue;
    LinearLayout mLinearLayoutControlSetRs, mLinearLayoutControlSet, mLinearLayoutCathodeMain;
    RadioGroup mRadioGroupControlSet;



    ProgressDialog prog1;
    public boolean isSettingsRead = false;
    private int mLastMessageId=0;


    private SharedPreferences mSettings;

    public final static String IS_SMS_AWAITING = "key_IsSmsAwaiting";
    public final static String IS_SMS_RECEIVED = "key_IsSmsReceived";
    public final static String NUMBER_SMS_AWAITING = "key_NumberSmsAwaiting";
    public final static String TEXT_SMS_AWAITING = "key_TextSmsAwaiting";
    public final static String TEXT_TIMERECEIVED = "key_TextSmsTimeReceived";


    private DatabaseHelper mDatabaseHelper;
    private SQLiteDatabase mSqLiteDatabase;


    int mControlSetParam=0;
    boolean isControlSetWindow=false;
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

    int smsProtocolType=0;
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


        mSettings = PreferenceManager.getDefaultSharedPreferences(this);
        isControlSetWindow = false;
        mRadioButtonI = (RadioButton)findViewById(R.id.radioButtonControlSetI);
        mRadioButtonU = (RadioButton)findViewById(R.id.radioButtonControlSetU);
        mRadioButtonP = (RadioButton)findViewById(R.id.radioButtonControlSetP);
        mRadioGroupControlSet = (RadioGroup)findViewById(R.id.radioGroupControlSet);

        mRadioGroupControlSet.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener()
        {
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                // checkedId is the RadioButton selected

                if (mRadioButtonI.isChecked())
                {
                    mControlSetParam = 1;
                    mSeekBarControlSetValue.setMax(990);
                    mTextViewControlSetValue.setText("Установите величину выходного тока, А");

                }
                else if (mRadioButtonU.isChecked())
                {
                    mControlSetParam = 2;
                    mSeekBarControlSetValue.setMax(990);
                    mTextViewControlSetValue.setText("Установите величину выхоного напряжения, В");

                }
                else if (mRadioButtonP.isChecked())
                {
                    mControlSetParam = 3;
                    mSeekBarControlSetValue.setMax(500);
                    mTextViewControlSetValue.setText("Установите величину потенциала, В");
                }
            }
        });

        mTextViewCathodeText = (TextView)findViewById(R.id.textViewCathodeTextVal);
        mTextViewCathodePhone = (TextView) findViewById(R.id.textViewCathodePhoneVal);
        mTextViewCathodeType = (TextView)findViewById(R.id.textViewCathodeDeviceType);
        mTextViewCathodeTitle = (TextView) findViewById(R.id.textViewCathodeTitle);

        mLinearLayoutUseti = (LinearLayout) findViewById(R.id.linearLayoutUseti);
        mLinearLayoutCathodeStabRs = (LinearLayout) findViewById(R.id.linearLayoutCathodeStabRs);
        mLinearLayoutCathodeStabUnivers = (LinearLayout) findViewById(R.id.linearLayoutCathodeStabUniver);
        mLinearLayoutCathodeAlarms = (LinearLayout) findViewById(R.id.linearLayoutCathodeAlarms);

        mTextViewControlSetValue = (TextView)findViewById(R.id.textViewConstrolSetValue);
        mEditTextControlSetValue = (EditText)findViewById(R.id.editTextControlSetValue);
        mSeekBarControlSetValue = (SeekBar)findViewById(R.id.seekBarControlSetValue);

        mSeekBarControlSetValue.setOnSeekBarChangeListener(this);


        mLinearLayoutControlSetRs = (LinearLayout)findViewById(R.id.linearLayoutControlSetRs);
        mLinearLayoutControlSet = (LinearLayout)findViewById(R.id.linearLayoutControlSet);
                mLinearLayoutCathodeMain = (LinearLayout)findViewById(R.id.linearLayoutCathodeMain);



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

        mLinearLayoutCathodeMain.setVisibility(View.VISIBLE);
        mLinearLayoutControlSet.setVisibility(View.GONE);

        updateViewFromDatabase();



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
    public void onStop() {


        super.onStop();

    }

    @Override
    public void onBackPressed() {
        if (isControlSetWindow)
        {
            mLinearLayoutCathodeMain.setVisibility(View.VISIBLE);
            mLinearLayoutControlSet.setVisibility(View.GONE);
            isControlSetWindow = false;
        }
        else super.onBackPressed();

    }


    @Override
    public void onProgressChanged(SeekBar seekBar, int progress,
                                  boolean fromUser) {

        if (dbDeviceType==0) mEditTextControlSetValue.setText(String.valueOf(progress));
        else
        {
            switch (mControlSetParam)
            {
                case 1:
                case 2:
                    mEditTextControlSetValue.setText(String.valueOf(progress/10)+","+String.valueOf(progress%10));
                    break;
                case 3:
                    mEditTextControlSetValue.setText(String.valueOf(progress/100)+","+String.valueOf(progress%100));
                    break;
            }
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        //mEditTextControlSetValue.setText(String.valueOf(mSeekBarControlSetValue.getProgress()));
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
                //Send ASK SMS message
                smsProtocolType = 0;
                //sendByteArrayAsSMS(mDevicePhoneNumber, byteArrayToSend);
                //sendByteArrayAsSMS(mDevicePhoneNumber, port, smsBody);
                //sendByteArrayAsSMS(mDevicePhoneNumber, port2, smsBody);
                //sendByteArrayAsSMS(mDevicePhoneNumber, port3, smsBody);
                //sendByteArrayAsSMS("+79051815744", port2, smsBody);
                //String string2 = Base64.encodeToString(byteArrayToSend, Base64.DEFAULT);

                //sendSMS(mDevicePhoneNumber, new String(byteArrayToSend, Charset.forName("UTF-8")));
                //sendSMS(mDevicePhoneNumber, new String(byteArrayToSend, Charset.forName("US-ASCII")));

                if (dbDeviceType==0)
                {
                    sendSMS(mDevicePhoneNumber, new String("\u0100"));
                }
                else
                {
                    sendSMS(mDevicePhoneNumber, new String("\u1100"));
                }


                //Put param to shared preferences to wait answer from host
                SharedPreferences.Editor editor = mSettings.edit();
                editor.putString(CathodeActivity.NUMBER_SMS_AWAITING, mDevicePhoneNumber);
                editor.putBoolean(CathodeActivity.IS_SMS_RECEIVED, false);
                editor.putBoolean(CathodeActivity.IS_SMS_AWAITING, true);
                editor.apply();


                prog1 = new ProgressDialog(CathodeActivity.this);
                prog1.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                prog1.setMessage("Ожидание ответа от станции...");
                prog1.setIndeterminate(true); // выдать значек ожидания
                prog1.setCancelable(false);
                timerSmsCommandWaiting = 120;
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


        if (mSettings.getBoolean(IS_SMS_RECEIVED,false))
        {
            String strSmsData = mSettings.getString(TEXT_SMS_AWAITING,"");

            int smsReceivedProtocolType = readSmsParameters(strSmsData);
            prog1.dismiss();
            //Вывсти сообщение, что параметры успешно обновлены
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            isSettingsRead = true;


            if ((dbDeviceType==0 && smsReceivedProtocolType==2) ||
                    (dbDeviceType==0 && smsReceivedProtocolType==2))
            {
                builder.setTitle("Получено новое состояние станции")
                        .setMessage("Формат протокола не соотвествует типу станции, проведен парсинг")
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
            else {
                //showParameters();
                builder.setTitle("Получено новое состояние станции")
                        .setMessage("Данные успешно обновлены и загружены")
                        .setIcon(R.drawable.ic_action_accept)
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


    public int readSmsParameters(String string){


        String mTimeDate = mSettings.getString(TEXT_TIMERECEIVED,"");
        byte[] byteArray = hexStringToByteArray(string);
        int ArraySize = byteArray.length;
        int[] Array = new int[ArraySize];
        for (int i=0; i<ArraySize; i++)
        {
            if (byteArray[i]>=0) Array[i]=byteArray[i];
            else Array[i] = 256 + byteArray[i];
        }


        //Инициализация переменных // параметров
        int Uout = 0;
        int Iout = 0;
        int Upot = 0;
        int RsU220 = 0;
        int StabParam = 0;
        int RsPwm = 0;
        int StabValue =0;
        int RsInputsOutputs=0;
        int RsStatus=0;
        int RsType=0;
        String RsProgDate = "";

        int RsEnergyCntKoeff = 0;
        int isSkzWorking = 0;
        int isTermostatWorking = 0;
        int isDoorEnable = 0;
        int isControlLocal = 0;
        long EnergyCnt = 0;
        int Temp = 0;
        long Svn1 = 0;
        long Svn2 = 0;
        //Получение данных
        if (Array[0]==0x01) {
            smsProtocolType = 1;
            //Универсальный BTGV
            Uout = Array[1] * 256 + Array[2];
            Iout = Array[3] * 256 + Array[4];
            Upot = Array[5] * 256 + Array[6];
            StabValue = Array[7] * 256 + Array[8];
            isSkzWorking = ((Array[9] & 0x10) != 0) ? 1 : 0;
            isTermostatWorking = ((Array[9] & 0x20) != 0) ? 1 : 0;
            isDoorEnable = ((Array[9] & 0x01) != 0) ? 1 : 0;
            isControlLocal = ((Array[9] & 0x02) != 0) ? 1 : 0;
            EnergyCnt = Array[14] + Array[13] * 0x100 + Array[12] * 0x10000 + Array[11] * 0x1000000; // + Array[10]*0x100000000
            Temp = Array[15];
            if (Temp >= 128) Temp -= 256;
            Svn1 = Array[18] + Array[17] * 0x100 + Array[16] * 0x10000;
            Svn2 = Array[21] + Array[20] * 0x100 + Array[19] * 0x10000;


        }
        else if (Array[0]==0x11)
        {
            smsProtocolType = 2;
            //Интерфейсный BTGSM RS
            Uout = Array[1] * 256 + Array[2];
            Iout = Array[3] * 256 + Array[4];
            Upot = Array[5] * 256 + Array[6];
            RsU220 = Array[7] * 256 + Array[8];
            RsPwm = Array[9] * 256 + Array[10];

            StabParam = Array[11];
            StabValue = Array[12] * 256 + Array[13];
            EnergyCnt = Array[17] + Array[16] * 0x100 + Array[15] * 0x10000 + Array[14] * 0x1000000; // + Array[10]*0x100000000
            RsEnergyCntKoeff = Array[18] * 256 + Array[19];

            Svn1 = Array[23] + Array[22] * 0x100 + Array[21] * 0x10000 + Array[20] * 0x1000000;
            Svn2 = Array[27] + Array[26] * 0x100 + Array[25] * 0x10000 + Array[24] * 0x1000000;

            Temp = Array[28];
            if (Temp >= 128) Temp -= 256;

            RsInputsOutputs = Array[29];
            RsStatus = Array[30];
            RsType = Array[31];
            RsProgDate = String.valueOf(Array[32]) + "." + String.valueOf(Array[33]) + "." + String.valueOf(Array[34]);


            if ((RsStatus & (1<<0)) != 0) StabParam += 0x10;

//            RsAlarm1 = ((RsStatus & (1<<1)) != 0) ? 1 : 0;
//            RsAlarm2 = ((RsStatus & (1<<2)) != 0) ? 1 : 0;
//            RsAlarm3 = ((RsStatus & (1<<3)) != 0) ? 1 : 0;
//            RsAlarm4 = ((RsStatus & (1<<4)) != 0) ? 1 : 0;
//            RsAlarm5 = ((RsStatus & (1<<5)) != 0) ? 1 : 0;
//            RsAlarm6 = ((RsStatus & (1<<6)) != 0) ? 1 : 0;
//            RsAlarm7 = ((RsStatus & (1<<7)) != 0) ? 1 : 0;


            isTermostatWorking = ((RsInputsOutputs & 0x10) != 0) ? 1 : 0;
            isDoorEnable = ((RsInputsOutputs & 0x01) != 0) ? 1 : 0;

        }




        //Записать даные в архив
        mDatabaseHelper = new DatabaseHelper(this, DatabaseHelper.DATABASE_NAME, null, 1);

        mSqLiteDatabase = mDatabaseHelper.getWritableDatabase();

        ContentValues values = new ContentValues();
        // Задайте значения для каждого столбца



        //Надо ли читать данные?
        values.put(DatabaseHelper.VAL_DATETIME_COLUMN, mTimeDate);
        values.put(DatabaseHelper.VAL_U_COLUMN, (Uout));
        values.put(DatabaseHelper.VAL_I_COLUMN, (Iout));
        values.put(DatabaseHelper.VAL_P_COLUMN, (Upot));
        values.put(DatabaseHelper.VAL_DOOR_COLUMN, isDoorEnable);
        values.put(DatabaseHelper.VAL_TC_COLUMN, isControlLocal);
        values.put(DatabaseHelper.VAL_SVN1_COLUMN, (Svn1));
        values.put(DatabaseHelper.VAL_SVN2_COLUMN, (Svn2));
        values.put(DatabaseHelper.VAL_CNT_COLUMN, (EnergyCnt));
        values.put(DatabaseHelper.VAL_220_COLUMN, (RsU220));
        values.put(DatabaseHelper.VAL_TEMP_COLUMN, (Temp));
        values.put(DatabaseHelper.VAL_HEATER_COLUMN, isTermostatWorking);
        values.put(DatabaseHelper.VAL_STAB_PARAM_COLUMN, StabParam);
        values.put(DatabaseHelper.VAL_STAB_VAL_COLUMN, (StabValue));
        values.put(DatabaseHelper.VAL_ALARMS_MASK_COLUMN, RsStatus);

        mSqLiteDatabase.update(DatabaseHelper.DATABASE_TABLE_CATHODES, values, "_id = ?",
                new String[] {Integer.toString(dbId)});



        //Отобразить данные
        updateViewFromDatabase();
        return smsProtocolType;
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


    private void sendByteArrayAsSMS(String phoneNumber, short port, byte[] message)    {
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
        sms.sendDataMessage(phoneNumber, null, port, message, sentPI, deliveredPI);

    }


    public static byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i+1), 16));
        }
        return data;
    }

    void updateViewFromDatabase()
    {
        DatabaseHelper mDatabaseHelper = new DatabaseHelper(this, DatabaseHelper.DATABASE_NAME, null, 1);
        SQLiteDatabase cdb = mDatabaseHelper.getReadableDatabase();

        Cursor cursor = cdb.query(DatabaseHelper.DATABASE_TABLE_CATHODES, null, null, null, null, null, null) ;

        int dbPosition = getIntent().getIntExtra("db_cathode_position", 0);
        dbId=0;
        dbDeviceType=0;

        if (cursor.moveToPosition(dbPosition))
        {

            dbId = cursor.getInt(cursor.getColumnIndex(BaseColumns._ID));
            mDevicePhoneNumber = cursor.getString(cursor.getColumnIndex(DatabaseHelper.PHONE_COLUMN));
            mTextViewCathodeTitle.setText(cursor.getString(cursor.getColumnIndex(DatabaseHelper.TEXT_COLUMN)));
            mTextViewCathodePhone.setText(mDevicePhoneNumber);
            mTextViewCathodeText.setText(cursor.getString(cursor.getColumnIndex(DatabaseHelper.INFO_COLUMN)));
            dbDeviceType = cursor.getInt(cursor.getColumnIndex(DatabaseHelper.DEVICE_COLUMN));


            int uiTemp;
            long ulTemp;

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



                uiTemp = cursor.getInt(cursor.getColumnIndex(DatabaseHelper.VAL_U_COLUMN))*cursor.getInt(cursor.getColumnIndex(DatabaseHelper.UMAX_COLUMN))*10/1024;
                mTextViewValU.setText(String.valueOf(uiTemp/10) + "."+String.valueOf(uiTemp%10));

                uiTemp = cursor.getInt(cursor.getColumnIndex(DatabaseHelper.VAL_I_COLUMN))*cursor.getInt(cursor.getColumnIndex(DatabaseHelper.IMAX_COLUMN))*10/1024;
                mTextViewValI.setText(String.valueOf(uiTemp/10) + "."+String.valueOf(uiTemp%10));

                uiTemp = cursor.getInt(cursor.getColumnIndex(DatabaseHelper.VAL_P_COLUMN))*cursor.getInt(cursor.getColumnIndex(DatabaseHelper.FIMAX_COLUMN))*100/1024;
                mTextViewValP.setText(String.valueOf(uiTemp/100) + "."+String.valueOf(uiTemp%100));

                if (cursor.getInt(cursor.getColumnIndex(DatabaseHelper.CNT_SCALE_COLUMN))>0) ulTemp = cursor.getInt(cursor.getColumnIndex(DatabaseHelper.CNT_BEGIN_COLUMN))*10 + (int)(((float)(cursor.getInt(cursor.getColumnIndex(DatabaseHelper.VAL_CNT_COLUMN))*10))/(float)cursor.getInt(cursor.getColumnIndex(DatabaseHelper.CNT_SCALE_COLUMN)));
                else ulTemp=0;
                mTextViewValCnt.setText(String.valueOf(ulTemp/10) + "."+String.valueOf(ulTemp%10));


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

                uiTemp = cursor.getInt(cursor.getColumnIndex(DatabaseHelper.VAL_U_COLUMN));
                mTextViewValU.setText(String.valueOf(uiTemp/10) + "."+String.valueOf(uiTemp%10));

                uiTemp = cursor.getInt(cursor.getColumnIndex(DatabaseHelper.VAL_I_COLUMN));
                mTextViewValI.setText(String.valueOf(uiTemp/10) + "."+String.valueOf(uiTemp%10));

                uiTemp = cursor.getInt(cursor.getColumnIndex(DatabaseHelper.VAL_P_COLUMN));
                mTextViewValP.setText(String.valueOf(uiTemp/100) + "."+String.valueOf(uiTemp%100));

                ulTemp = cursor.getInt(cursor.getColumnIndex(DatabaseHelper.VAL_CNT_COLUMN));
                mTextViewValCnt.setText(String.valueOf(ulTemp/10) + "."+String.valueOf(ulTemp%10));
            }



            //Последние показания станции
            mTextViewValDateTime.setText(cursor.getString(cursor.getColumnIndex(DatabaseHelper.VAL_DATETIME_COLUMN)));
            if (cursor.getInt(cursor.getColumnIndex(DatabaseHelper.VAL_DOOR_COLUMN))==0)
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

            long SvnTemp = 0;
            SvnTemp = cursor.getInt(cursor.getColumnIndex(DatabaseHelper.VAL_SVN1_COLUMN));
            mTextViewValSvn1.setText(String.valueOf(SvnTemp/10)+"."+String.valueOf(SvnTemp%10));

            SvnTemp = cursor.getInt(cursor.getColumnIndex(DatabaseHelper.VAL_SVN2_COLUMN));
            mTextViewValSvn2.setText(String.valueOf(SvnTemp/10)+"."+String.valueOf(SvnTemp%10));

            mTextViewVal220V.setText(String.valueOf(cursor.getInt(cursor.getColumnIndex(DatabaseHelper.VAL_220_COLUMN))));

            int TempTemp=0;
            TempTemp = cursor.getInt(cursor.getColumnIndex(DatabaseHelper.VAL_TEMP_COLUMN));
            mTextViewValtemp.setText(String.valueOf(TempTemp));

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




            uiTemp = cursor.getInt(cursor.getColumnIndex(DatabaseHelper.VAL_STAB_VAL_COLUMN));
            mTextViewValStabRs.setText(String.valueOf(uiTemp/10) + "."+String.valueOf(uiTemp%10));

            mTextViewValStabUniver.setText(String.valueOf(cursor.getInt(cursor.getColumnIndex(DatabaseHelper.VAL_STAB_VAL_COLUMN))));

            switch (cursor.getInt(cursor.getColumnIndex(DatabaseHelper.VAL_STAB_PARAM_COLUMN)) & 0x0F)
            {
                case 1: mButtonValStabParam.setText("Напряжение, В (Изменить)"); break;
                case 2: mButtonValStabParam.setText("Ток, А (Изменить)"); break;
                case 3: mButtonValStabParam.setText("Потенциал, В (Изменить)"); break;
                default: mButtonValStabParam.setText("Неизвестный параметр (Изменить)");
            }

            byte tsStatus = (byte)cursor.getInt(cursor.getColumnIndex(DatabaseHelper.VAL_ALARMS_MASK_COLUMN));
            //((RsStatus & (1<<1)) != 0)

            // Стабилизация
            if ((tsStatus & (1<<1)) !=0) mTextViewValStabRs.setTextColor(getResources().getColor(R.color.green));
            else mTextViewValStabRs.setTextColor(getResources().getColor(R.color.red));


            if ((tsStatus & (1<<1)) ==0)
                mImageViewAlarm1.setImageBitmap(BitmapFactory.decodeResource(
                        this.getResources(), R.drawable.led_gray));
            else mImageViewAlarm1.setImageBitmap(BitmapFactory.decodeResource(
                    this.getResources(), R.drawable.led_green));

            if ((tsStatus & (1<<2)) ==0)
                mImageViewAlarm2.setImageBitmap(BitmapFactory.decodeResource(
                        this.getResources(), R.drawable.led_gray));
            else mImageViewAlarm2.setImageBitmap(BitmapFactory.decodeResource(
                    this.getResources(), R.drawable.led_green));

            if ((tsStatus & (1<<3)) ==0)
                mImageViewAlarm3.setImageBitmap(BitmapFactory.decodeResource(
                        this.getResources(), R.drawable.led_gray));
            else mImageViewAlarm3.setImageBitmap(BitmapFactory.decodeResource(
                    this.getResources(), R.drawable.led_green));

            if ((tsStatus & (1<<4)) ==0)
                mImageViewAlarm4.setImageBitmap(BitmapFactory.decodeResource(
                        this.getResources(), R.drawable.led_gray));
            else mImageViewAlarm4.setImageBitmap(BitmapFactory.decodeResource(
                    this.getResources(), R.drawable.led_green));

            if ((tsStatus & (1<<5)) ==0)
                mImageViewAlarm5.setImageBitmap(BitmapFactory.decodeResource(
                        this.getResources(), R.drawable.led_gray));
            else mImageViewAlarm5.setImageBitmap(BitmapFactory.decodeResource(
                    this.getResources(), R.drawable.led_green));

            if ((tsStatus & (1<<6)) ==0)
                mImageViewAlarm6.setImageBitmap(BitmapFactory.decodeResource(
                        this.getResources(), R.drawable.led_gray));
            else mImageViewAlarm6.setImageBitmap(BitmapFactory.decodeResource(
                    this.getResources(), R.drawable.led_green));

            if ((tsStatus & (1<<7)) ==0)
                mImageViewAlarm7.setImageBitmap(BitmapFactory.decodeResource(
                        this.getResources(), R.drawable.led_gray));
            else mImageViewAlarm7.setImageBitmap(BitmapFactory.decodeResource(
                    this.getResources(), R.drawable.led_green));



        }

        cursor.close();
        cdb.close();
        mDatabaseHelper.close();
    }

    public void onBtnCathodeControlSetClick(View view) {


        AlertDialog.Builder sendSmsIdGetDialog = new AlertDialog.Builder(this);
        sendSmsIdGetDialog.setTitle("Будет отправлена SMS-команда! Вы уверены?");

        sendSmsIdGetDialog.setPositiveButton("Да", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //Send ASK SMS message
                smsProtocolType = 0;


                int valueSet = mSeekBarControlSetValue.getProgress();
                if (dbDeviceType==0)
                {
                    //03XXXX00
                    String s = "\u0300" + Character.toString((char)(valueSet<<8));
                    sendSMS(mDevicePhoneNumber, s);
                }
                else
                {
                    //12FFGGGG
                    String s = new String();
                    switch (mControlSetParam)
                    {
                        case 1:
                            s = "\u1201" + Character.toString((char)(valueSet));
                            break;
                        case 2:
                            s = "\u1202" + Character.toString((char)(valueSet));
                            break;
                        case 3:
                            s = "\u1203" + Character.toString((char)(valueSet));
                            break;
                    }

                    sendSMS(mDevicePhoneNumber, s);
                }


                //Put param to shared preferences to wait answer from host
                SharedPreferences.Editor editor = mSettings.edit();
                editor.putString(CathodeActivity.NUMBER_SMS_AWAITING, mDevicePhoneNumber);
                editor.putBoolean(CathodeActivity.IS_SMS_RECEIVED, false);
                editor.putBoolean(CathodeActivity.IS_SMS_AWAITING, true);
                editor.apply();


                prog1 = new ProgressDialog(CathodeActivity.this);
                prog1.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                prog1.setMessage("Ожидание ответа от станции...");
                prog1.setIndeterminate(true); // выдать значек ожидания
                prog1.setCancelable(false);
                timerSmsCommandWaiting = 120;
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

        mLinearLayoutCathodeMain.setVisibility(View.VISIBLE);
        mLinearLayoutControlSet.setVisibility(View.GONE);
        isControlSetWindow = false;


        //wait for

    }

    public void onBtnCathodeControlSetRsClick(View view) {
        mLinearLayoutCathodeMain.setVisibility(View.GONE);
        mLinearLayoutControlSet.setVisibility(View.VISIBLE);
        isControlSetWindow = true;
        if (dbDeviceType==0)
        {
            mLinearLayoutControlSetRs.setVisibility(View.GONE);
            mSeekBarControlSetValue.setMax(100);
            mTextViewControlSetValue.setText("Установите величину управления, %");
        }
        else
        {
            mSeekBarControlSetValue.setMax(990);
            mTextViewControlSetValue.setText("Установите величину выходного тока, А");
            mLinearLayoutControlSetRs.setVisibility(View.VISIBLE);
        }
    }
}