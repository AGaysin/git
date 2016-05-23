package tehohrana.ru.cathodmon;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.telephony.SmsMessage;
import android.util.Log;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by AG on 19.05.2016.
 */
public class SmsReceiver extends BroadcastReceiver {
    private String TAG = SmsReceiver.class.getSimpleName();

    public SmsReceiver() {
    }
    private SharedPreferences mSettings;


    @Override
    public void onReceive(Context context, Intent intent) {
        // Get the data (SMS data) bound to intent
        Bundle bundle = intent.getExtras();

        SmsMessage[] msgs = null;

        String str = "";

        if (bundle != null) {
            // Retrieve the SMS Messages received
            Object[] pdus = (Object[]) bundle.get("pdus");
            msgs = new SmsMessage[pdus.length];
            msgs[0] = SmsMessage.createFromPdu((byte[]) pdus[0]);

            mSettings = PreferenceManager.getDefaultSharedPreferences(context);
            boolean isSmsAwaiting = mSettings.getBoolean(CathodeActivity.IS_SMS_AWAITING, false);
            String numberSmsFrom = "+" + mSettings.getString(CathodeActivity.NUMBER_SMS_AWAITING, "");
            //Ждем ли сообщения?

            if (isSmsAwaiting) {
                //Проверка номера телефона
                if (msgs[0].getOriginatingAddress().equals(numberSmsFrom)) {
                    //Получаем строку





            String ts = new SimpleDateFormat("dd.MM.yyyy  HH:mm:ss").format(new Date());

            byte[] bytePdu = msgs[0].getPdu();
            String string = bytesToHex(bytePdu).substring(54);



//            int[] Array = new int[bytePdu.length-26];
//
//            for (int i=27; i<bytePdu.length; i++)
//            {
//                if (bytePdu[i]<0) Array[i-27] = 256+bytePdu[i];
//                else Array[i-27] = bytePdu[i];
//            }


            //Передаем ее в активити
            SharedPreferences.Editor editor = mSettings.edit();
            editor.putString(CathodeActivity.TEXT_SMS_AWAITING, string);
            editor.putString(CathodeActivity.TEXT_TIMERECEIVED, ts);

            editor.putBoolean(CathodeActivity.IS_SMS_RECEIVED, true);
            editor.putBoolean(CathodeActivity.IS_SMS_AWAITING, false);
            editor.apply();
                }
            }


        }
    }

    final protected static char[] hexArray = "0123456789ABCDEF".toCharArray();
    public static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for ( int j = 0; j < bytes.length; j++ ) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }
}