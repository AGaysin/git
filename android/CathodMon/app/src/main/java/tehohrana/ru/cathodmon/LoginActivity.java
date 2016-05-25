package tehohrana.ru.cathodmon;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

public class LoginActivity extends Activity {

    int mPinPosition;
    int mPinValue;
    TextView mTextViewPass1;
    TextView mTextViewPass2;
    TextView mTextViewPass3;
    TextView mTextViewPass4;
    Vibrator mVibrator;

    int mPassword;
    boolean isAskPassword;
    public SharedPreferences mSettings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        mPinPosition = 0;
        mPinValue = 0;

        mSettings = PreferenceManager.getDefaultSharedPreferences(this);

        mTextViewPass1 = (TextView) findViewById(R.id.textViewPass1);
        mTextViewPass2 = (TextView) findViewById(R.id.textViewPass2);
        mTextViewPass3 = (TextView) findViewById(R.id.textViewPass3);
        mTextViewPass4 = (TextView) findViewById(R.id.textViewPass4);
        mTextViewPass1.setTextColor(getResources().getColor(R.color.light_gray));
        mTextViewPass2.setTextColor(getResources().getColor(R.color.light_gray));
        mTextViewPass3.setTextColor(getResources().getColor(R.color.light_gray));
        mTextViewPass4.setTextColor(getResources().getColor(R.color.light_gray));
        mVibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

        isAskPassword = mSettings.getBoolean("key_pass_enable",false);
        mPassword = Integer.parseInt(mSettings.getString("key_pass_value","1234"));



        if (getIntent().getExtras() != null && getIntent().getExtras().getBoolean("EXIT", false)) {
            finish();
        }
        else if (!isAskPassword)
        {
            Intent intent = new Intent(this, MainActivity.class);
            this.startActivity(intent);
        }


    }



    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    public void onLoginBtn1Click(View view) {
        mVibrator.vibrate(100);
        checkPassword(1);


        //Toast.makeText(this,"Click 1",Toast.LENGTH_SHORT).show();
    }

    public void onLoginBtn2Click(View view) {
        mVibrator.vibrate(100);
        checkPassword(2);
        //Toast.makeText(this,"Click 2",Toast.LENGTH_SHORT).show();
    }

    public void onLoginBtn3Click(View view) {
        mVibrator.vibrate(100);
        checkPassword(3);
        //Toast.makeText(this,"Click 3",Toast.LENGTH_SHORT).show();
    }

    public void onLoginBtn4Click(View view) {
        mVibrator.vibrate(100);
        checkPassword(4);
        //Toast.makeText(this,"Click 4",Toast.LENGTH_SHORT).show();
    }

    public void onLoginBtn5Click(View view) {
        mVibrator.vibrate(100);

        checkPassword(5);
        //Toast.makeText(this,"Click 5",Toast.LENGTH_SHORT).show();
    }

    public void onLoginBtn6Click(View view) {
        mVibrator.vibrate(100);
        checkPassword(6);
        //Toast.makeText(this,"Click 6",Toast.LENGTH_SHORT).show();
    }

    public void onLoginBtn7Click(View view) {
        mVibrator.vibrate(100);
        checkPassword(7);
        //Toast.makeText(this,"Click 7",Toast.LENGTH_SHORT).show();
    }

    public void onLoginBtn8Click(View view) {
        mVibrator.vibrate(100);
        checkPassword(8);

        //Toast.makeText(this,"Click 8",Toast.LENGTH_SHORT).show();
    }

    public void onLoginBtn9Click(View view) {
        mVibrator.vibrate(100);
        checkPassword(9);
        //Toast.makeText(this,"Click 9",Toast.LENGTH_SHORT).show();
    }

    public void onLoginBtn0Click(View view) {
        mVibrator.vibrate(100);
        checkPassword(0);
        //Toast.makeText(this,"Click 0",Toast.LENGTH_SHORT).show();
    }
    public void checkPassword(int key)
    {
        switch (mPinPosition)
        {
            case 0:
                mPinValue += 1000*key;
                mPinPosition = 1;
                mTextViewPass1.setTextColor(getResources().getColor(R.color.light_blue));
                break;
            case 1:
                mPinValue += 100*key;
                mPinPosition = 2;
                mTextViewPass2.setTextColor(getResources().getColor(R.color.light_blue));
                break;
            case 2:
                mPinValue += 10*key;
                mPinPosition = 3;
                mTextViewPass3.setTextColor(getResources().getColor(R.color.light_blue));
                break;
            case 3:
                mPinValue += 1*key;
                mTextViewPass4.setTextColor(getResources().getColor(R.color.light_blue));
                if (mPinValue == mPassword)
                {


                    Intent intent = new Intent(this, MainActivity.class);
                    this.startActivity(intent);

                }
                else
                {
                    mPinPosition = 0;
                    mPinValue = 0;
                    mTextViewPass1.setTextColor(getResources().getColor(R.color.light_gray));
                    mTextViewPass2.setTextColor(getResources().getColor(R.color.light_gray));
                    mTextViewPass3.setTextColor(getResources().getColor(R.color.light_gray));
                    mTextViewPass4.setTextColor(getResources().getColor(R.color.light_gray));
                    mVibrator.vibrate(1000);
                    Toast.makeText(this,"Неверный пароль. Повторите попытку еще раз!",Toast.LENGTH_LONG).show();
                }
                //Check
                break;
                default:

        }
    }
}
