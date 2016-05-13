package tehohrana.ru.cathodmon;

import android.app.Activity;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.provider.BaseColumns;
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
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.util.regex.Pattern;

/**
 * Created by AG on 19.04.2016.
 */
public class AddActivity extends Activity {


    private DatabaseHelper mDatabaseHelper;
    private SQLiteDatabase mSqLiteDatabase;


    EditText mEditTextAddText;
    EditText mEditTextAddPhone;
    Spinner mSpinnerAddDeviceType;
    RadioButton mRadioButtonAdd05V,mRadioButtonAdd420mA;
    EditText mEditTextAddImax;
    EditText mEditTextAddUmax;
    EditText mEditTextAddFimax;
    EditText mEditTextAddCntBegin;
    EditText mEditTextAddCntScale;
    TextView mTextViewAddTextOk;
    TextView mTextViewAddPhoneOk;

    Button mButtonAddApply;
    LinearLayout mLinearLayoutDeviceParamUniversal;

    boolean isEdit = false;
    int dbId=0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add);

        mEditTextAddText = (EditText)findViewById(R.id.editTextAddText);
        mEditTextAddPhone = (EditText) findViewById(R.id.editTextAddPhone);

        mSpinnerAddDeviceType = (Spinner)findViewById(R.id.spinnerAddDeviceType);

        mRadioButtonAdd05V = (RadioButton)findViewById(R.id.radioButton05V);
        mRadioButtonAdd420mA = (RadioButton)findViewById(R.id.radioButton420mA);

        mEditTextAddImax = (EditText)findViewById(R.id.editTextImax);
        mEditTextAddUmax = (EditText)findViewById(R.id.editTextUmax);
        mEditTextAddFimax = (EditText) findViewById(R.id.editTextFimax);
        mEditTextAddCntBegin = (EditText) findViewById(R.id.editTextCntBegin);
        mEditTextAddCntScale = (EditText) findViewById(R.id.editTextCntScale);
        mButtonAddApply = (Button) findViewById(R.id.buttonAddApply);
        mTextViewAddPhoneOk = (TextView)findViewById(R.id.textViewAddPhoneOk);
        mTextViewAddTextOk = (TextView)findViewById(R.id.textViewAddTextOk);



        mLinearLayoutDeviceParamUniversal = (LinearLayout)findViewById(R.id.linearLayoutUniversalType);

        mEditTextAddText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {

                if (s.length()>4)
                {
                    if (Pattern.matches("[7]([0-9]{10})", mEditTextAddPhone.getText().toString())) mButtonAddApply.setEnabled(true);
                    mTextViewAddTextOk.setText(" Ok");
                }
                else
                {
                    mTextViewAddTextOk.setText("");
                    mButtonAddApply.setEnabled(false);
                }
            }
        });

        //Обработка номера записи номера телефона
        mEditTextAddPhone.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {


                if (Pattern.matches("[7]([0-9]{10})", s.toString())) {
                    if (mEditTextAddText.getText().length() > 4) mButtonAddApply.setEnabled(true);
                    mTextViewAddPhoneOk.setText(" Ok");
                } else {
                    mTextViewAddPhoneOk.setText("");
                    mButtonAddApply.setEnabled(false);
                }
            }
        });




        String[] widgetModes = {"BT GSM Универсальный", "BT GSM Интерфейсный (RS-485)"};
        ArrayAdapter<String> widgetModeAdapter = new ArrayAdapter<String> (this, android.R.layout.simple_spinner_item, widgetModes);
        widgetModeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);


        mSpinnerAddDeviceType.setAdapter(widgetModeAdapter);
        mSpinnerAddDeviceType.setPrompt("Выберите тип прибора");

        mSpinnerAddDeviceType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                if (position == 0) mLinearLayoutDeviceParamUniversal.setVisibility(View.VISIBLE);
                else mLinearLayoutDeviceParamUniversal.setVisibility(View.GONE);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                mLinearLayoutDeviceParamUniversal.setVisibility(View.GONE);
            }
        });


        Intent intent = getIntent();
        isEdit = intent.getBooleanExtra("isEdit", false);
        dbId = intent.getIntExtra("dbId", 0);
        int dbPosition = intent.getIntExtra("dbPosition", 0);


        if (isEdit)
        {
            //Чтение данных с базы

            DatabaseHelper mDatabaseHelper = new DatabaseHelper(this, "CathodMon2.db", null, 1);
            SQLiteDatabase cdb = mDatabaseHelper.getReadableDatabase();

            Cursor cursor = cdb.query("Cathodes", null, null, null, null, null, null) ;

            if (cursor.moveToPosition(dbPosition))
            {
                int maxCount = cursor.getCount();
                cursor.moveToFirst();

                mEditTextAddText.setText(cursor.getString(cursor.getColumnIndex(DatabaseHelper.TEXT_COLUMN)));
                mEditTextAddPhone.setText(cursor.getString(cursor.getColumnIndex(DatabaseHelper.PHONE_COLUMN)));

                mSpinnerAddDeviceType.setSelection(cursor.getInt(cursor.getColumnIndex(DatabaseHelper.DEVICE_COLUMN)));

                if (cursor.getInt(cursor.getColumnIndex(DatabaseHelper.SIGNAL_COOLUMN))==0)
                {
                    mRadioButtonAdd420mA.setChecked(false);
                    mRadioButtonAdd05V.setChecked(true);
                }
                else {
                    mRadioButtonAdd05V.setChecked(false);
                    mRadioButtonAdd420mA.setChecked(true);
                }


                mEditTextAddImax.setText(String.valueOf(cursor.getInt(cursor.getColumnIndex(DatabaseHelper.IMAX_COLUMN))));
                mEditTextAddUmax.setText(String.valueOf(cursor.getInt(cursor.getColumnIndex(DatabaseHelper.UMAX_COLUMN))));
                mEditTextAddFimax.setText(String.valueOf(cursor.getInt(cursor.getColumnIndex(DatabaseHelper.FIMAX_COLUMN))));
                mEditTextAddCntBegin.setText(String.valueOf(cursor.getInt(cursor.getColumnIndex(DatabaseHelper.CNT_BEGIN_COLUMN))));
                mEditTextAddCntScale.setText(String.valueOf(cursor.getInt(cursor.getColumnIndex(DatabaseHelper.CNT_SCALE_COLUMN))));


            }

            cursor.close();
            cdb.close();
            mDatabaseHelper.close();


            mTextViewAddPhoneOk.setText("Ok");
            mTextViewAddTextOk.setText("Ok");
            mButtonAddApply.setEnabled(true);
            mButtonAddApply.setText("Изменить");
        }
        else
        {
            mTextViewAddPhoneOk.setText("");
            mTextViewAddTextOk.setText("");
            mButtonAddApply.setEnabled(false);
            mButtonAddApply.setText("Добавить");
            mSpinnerAddDeviceType.performClick();
        }


        //Обработка выпадающего списка ТИП ПРИБОРА
//        mSpinnerAddDeviceType.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//            @Override
//            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//                if (position==0)
//                {
//                    mLinearLayoutDeviceParamUniversal.setVisibility(View.INVISIBLE);
//                }
//                else mLinearLayoutDeviceParamUniversal.setVisibility(View.VISIBLE);
//
//            }
//        });

    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();

    }

    public void onBtnAddToDatabase(View view) {

        mDatabaseHelper = new DatabaseHelper(this, "CathodMon2.db", null, 1);

        mSqLiteDatabase = mDatabaseHelper.getWritableDatabase();

        ContentValues values = new ContentValues();
        // Задайте значения для каждого столбца



        values.put(DatabaseHelper.TEXT_COLUMN, mEditTextAddText.getText().toString());
        values.put(DatabaseHelper.PHONE_COLUMN, mEditTextAddPhone.getText().toString());
        values.put(DatabaseHelper.DEVICE_COLUMN, mSpinnerAddDeviceType.getSelectedItemPosition());
        values.put(DatabaseHelper.SIGNAL_COOLUMN, mRadioButtonAdd05V.isChecked() ? 1:0);
        values.put(DatabaseHelper.IMAX_COLUMN, mEditTextAddImax.getText().toString());
        values.put(DatabaseHelper.UMAX_COLUMN, mEditTextAddUmax.getText().toString());
        values.put(DatabaseHelper.FIMAX_COLUMN, mEditTextAddFimax.getText().toString());
        values.put(DatabaseHelper.CNT_BEGIN_COLUMN, mEditTextAddCntBegin.getText().toString());
        values.put(DatabaseHelper.CNT_SCALE_COLUMN, mEditTextAddCntScale.getText().toString());
        if (isEdit)
        {
            mSqLiteDatabase.update("Cathodes",values,"id ='" + dbId + "'",null);

        }
        else {
            // Вставляем данные в таблицу
            mSqLiteDatabase.insert("Cathodes", null, values);
        }
        super.onBackPressed();
    }

    public void onBtnCancelClick(View view) {
        super.onBackPressed();
    }
}