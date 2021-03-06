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

    EditText mEditTextAddInfo;

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

        mEditTextAddInfo = (EditText)findViewById(R.id.editTextAddInfo);


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
                    mTextViewAddTextOk.setVisibility(View.GONE);
                }
                else
                {
                    mTextViewAddTextOk.setVisibility(View.VISIBLE);
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
                    mTextViewAddPhoneOk.setVisibility(View.GONE);
                } else {
                    mTextViewAddPhoneOk.setVisibility(View.VISIBLE);
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
        int dbPosition = intent.getIntExtra("dbPosition", 0);


        if (isEdit)
        {
            //Чтение данных с базы

            DatabaseHelper mDatabaseHelper = new DatabaseHelper(this, DatabaseHelper.DATABASE_NAME, null, 1);
            SQLiteDatabase cdb = mDatabaseHelper.getReadableDatabase();

            Cursor cursor = cdb.query(DatabaseHelper.DATABASE_TABLE_CATHODES, null, null, null, null, null, null) ;

            if (cursor.moveToPosition(dbPosition))
            {

                dbId = cursor.getInt(cursor.getColumnIndex(BaseColumns._ID));
                mEditTextAddText.setText(cursor.getString(cursor.getColumnIndex(DatabaseHelper.TEXT_COLUMN)));
                mEditTextAddPhone.setText(cursor.getString(cursor.getColumnIndex(DatabaseHelper.PHONE_COLUMN)));

                mSpinnerAddDeviceType.setSelection(cursor.getInt(cursor.getColumnIndex(DatabaseHelper.DEVICE_COLUMN)));

                if (cursor.getInt(cursor.getColumnIndex(DatabaseHelper.SIGNAL_COOLUMN))==0)
                {
                    mRadioButtonAdd05V.setChecked(false);
                    mRadioButtonAdd420mA.setChecked(true);
                }
                else {
                    mRadioButtonAdd420mA.setChecked(false);
                    mRadioButtonAdd05V.setChecked(true);

                }

                mEditTextAddInfo.setText(cursor.getString(cursor.getColumnIndex(DatabaseHelper.INFO_COLUMN)));

                mEditTextAddImax.setText(String.valueOf(cursor.getInt(cursor.getColumnIndex(DatabaseHelper.IMAX_COLUMN))));
                mEditTextAddUmax.setText(String.valueOf(cursor.getInt(cursor.getColumnIndex(DatabaseHelper.UMAX_COLUMN))));
                mEditTextAddFimax.setText(String.valueOf(cursor.getInt(cursor.getColumnIndex(DatabaseHelper.FIMAX_COLUMN))));
                mEditTextAddCntBegin.setText(String.valueOf(cursor.getInt(cursor.getColumnIndex(DatabaseHelper.CNT_BEGIN_COLUMN))));
                mEditTextAddCntScale.setText(String.valueOf(cursor.getInt(cursor.getColumnIndex(DatabaseHelper.CNT_SCALE_COLUMN))));


            }

            cursor.close();
            cdb.close();
            mDatabaseHelper.close();


            mTextViewAddPhoneOk.setVisibility(View.GONE);
            mTextViewAddTextOk.setVisibility(View.GONE);
            mButtonAddApply.setEnabled(true);
            mButtonAddApply.setText("Сохранить");
        }
        else
        {
            mTextViewAddPhoneOk.setVisibility(View.VISIBLE);
            mTextViewAddTextOk.setVisibility(View.VISIBLE);
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

        mDatabaseHelper = new DatabaseHelper(this, DatabaseHelper.DATABASE_NAME, null, 1);

        mSqLiteDatabase = mDatabaseHelper.getWritableDatabase();

        ContentValues values = new ContentValues();
        // Задайте значения для каждого столбца



        values.put(DatabaseHelper.TEXT_COLUMN, mEditTextAddText.getText().toString());
        values.put(DatabaseHelper.PHONE_COLUMN, mEditTextAddPhone.getText().toString());
        values.put(DatabaseHelper.INFO_COLUMN, mEditTextAddInfo.getText().toString());
        values.put(DatabaseHelper.DEVICE_COLUMN, mSpinnerAddDeviceType.getSelectedItemPosition());
        values.put(DatabaseHelper.SIGNAL_COOLUMN, mRadioButtonAdd05V.isChecked() ? 1:0);
        values.put(DatabaseHelper.IMAX_COLUMN, Integer.parseInt(mEditTextAddImax.getText().toString()));
        values.put(DatabaseHelper.UMAX_COLUMN, Integer.parseInt(mEditTextAddUmax.getText().toString()));
        values.put(DatabaseHelper.FIMAX_COLUMN, Integer.parseInt(mEditTextAddFimax.getText().toString()));
        values.put(DatabaseHelper.CNT_BEGIN_COLUMN, Integer.parseInt(mEditTextAddCntBegin.getText().toString()));
        values.put(DatabaseHelper.CNT_SCALE_COLUMN, Integer.parseInt(mEditTextAddCntScale.getText().toString()));


        if (isEdit)
        {

            //Надо ли читать данные?
//            values.put(DatabaseHelper.VAL_DATETIME_COLUMN, "--.--.--/--:--:--");
//            values.put(DatabaseHelper.VAL_U_COLUMN, "--.-");
//            values.put(DatabaseHelper.VAL_I_COLUMN, "--.-");
//            values.put(DatabaseHelper.VAL_P_COLUMN, "-.--");
//            values.put(DatabaseHelper.VAL_DOOR_COLUMN, 0);
//            values.put(DatabaseHelper.VAL_TC_COLUMN, 0);
//            values.put(DatabaseHelper.VAL_SVN1_COLUMN, 0);
//            values.put(DatabaseHelper.VAL_SVN2_COLUMN, 0);
//            values.put(DatabaseHelper.VAL_CNT_COLUMN, 0);
//            values.put(DatabaseHelper.VAL_220_COLUMN, 0);
//            values.put(DatabaseHelper.VAL_TEMP_COLUMN, "--");
//            values.put(DatabaseHelper.VAL_HEATER_COLUMN, 0);
//            values.put(DatabaseHelper.VAL_STAB_PARAM_COLUMN, 0);
//            values.put(DatabaseHelper.VAL_STAB_VAL_COLUMN, "--.-");
//            values.put(DatabaseHelper.VAL_ALARM1_COLUMN, 0);
//            values.put(DatabaseHelper.VAL_ALARM2_COLUMN, 0);
//            values.put(DatabaseHelper.VAL_ALARM3_COLUMN, 0);
//            values.put(DatabaseHelper.VAL_ALARM4_COLUMN, 0);
//            values.put(DatabaseHelper.VAL_ALARM5_COLUMN, 0);
//            values.put(DatabaseHelper.VAL_ALARM6_COLUMN, 0);
//            values.put(DatabaseHelper.VAL_ALARM7_COLUMN, 0);

            mSqLiteDatabase.update(DatabaseHelper.DATABASE_TABLE_CATHODES, values, "_id = ?",
                    new String[] {Integer.toString(dbId)});

        }
        else {

            values.put(DatabaseHelper.VAL_DATETIME_COLUMN, "--.--.--/--:--:--");
            values.put(DatabaseHelper.VAL_U_COLUMN, 0);
            values.put(DatabaseHelper.VAL_I_COLUMN, 0);
            values.put(DatabaseHelper.VAL_P_COLUMN, 0);
            values.put(DatabaseHelper.VAL_DOOR_COLUMN, 0);
            values.put(DatabaseHelper.VAL_TC_COLUMN, 0);
            values.put(DatabaseHelper.VAL_SVN1_COLUMN, 0);
            values.put(DatabaseHelper.VAL_SVN2_COLUMN, 0);
            values.put(DatabaseHelper.VAL_CNT_COLUMN, 0);
            values.put(DatabaseHelper.VAL_220_COLUMN, 0);
            values.put(DatabaseHelper.VAL_TEMP_COLUMN, 0);
            values.put(DatabaseHelper.VAL_HEATER_COLUMN, 0);
            values.put(DatabaseHelper.VAL_STAB_PARAM_COLUMN, 0);
            values.put(DatabaseHelper.VAL_STAB_VAL_COLUMN, 0);
            values.put(DatabaseHelper.VAL_ALARMS_MASK_COLUMN, 0);
            values.put(DatabaseHelper.VAL_STAB_OK_COLUMN, 0);

            // Вставляем данные в таблицу
            mSqLiteDatabase.insert(DatabaseHelper.DATABASE_TABLE_CATHODES, null, values);
        }
        super.onBackPressed();
    }

    public void onBtnCancelClick(View view) {
        super.onBackPressed();
    }
}