package tehohrana.ru.cathodmon;

import android.app.Activity;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
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


    private DatabaseHelper mDatabaseHelper;
    private SQLiteDatabase mSqLiteDatabase;


    TextView mTextViewCathodeText;
    TextView mTextViewCathodePhone;
    TextView mTextViewCathodeType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cathode);
        int database_position = getIntent().getIntExtra("db_cathode_position", 0);

        mTextViewCathodeText = (TextView)findViewById(R.id.textViewCathodeText);
        mTextViewCathodePhone = (TextView) findViewById(R.id.textViewCathodePhone);
        mTextViewCathodeType = (TextView)findViewById(R.id.textViewCathodeType);




    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();

    }

}