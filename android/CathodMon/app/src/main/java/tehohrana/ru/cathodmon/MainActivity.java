package tehohrana.ru.cathodmon;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.provider.BaseColumns;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.view.ContextMenu;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewDebug;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private static final int CM_ADD_ID = 1;
    private static final int CM_EDIT_ID = 2;
    private static final int CM_DELETE_ID = 3;




    CathodesListAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);



        //updateListView();


        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        if (fab != null) {
            fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(getApplicationContext(), AddActivity.class);
                    startActivity(intent);

//                    new AlertDialog.Builder(getApplicationContext())
//                    .setIcon(android.R.drawable.ic_dialog_alert)
//                    .setTitle("Приложение будет закрыто")
//                    .setMessage("Вы действительно хотите закрыть приложение?")
//                    .setPositiveButton("Да", new DialogInterface.OnClickListener()
//                    {
//                        @Override
//                        public void onClick(DialogInterface dialog, int which) {
//
//
//                        }
//
//                    })
//                    .setNegativeButton("Нет", null)
//                    .show();


    //                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
    //                        .setAction("Action", null).show();
                }
            });
        }



        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);




    }

    @Override
    public void onResume(){
        updateListView();
        super.onResume();
        // put your code here...
        //Read database for Cathodes

        //Show database as columns








    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {

//            new AlertDialog.Builder(this)
//                    .setIcon(android.R.drawable.ic_dialog_alert)
//                    .setTitle("Приложение будет закрыто")
//                    .setMessage("Вы действительно хотите закрыть приложение?")
//                    .setPositiveButton("Да", new DialogInterface.OnClickListener()
//                    {
//                        @Override
//                        public void onClick(DialogInterface dialog, int which) {
//                            finish();
//                        }
//
//                    })
//                    .setNegativeButton("Нет", null)
//                    .show();


            super.finish();
        }
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
                                    ContextMenu.ContextMenuInfo menuInfo) {

        menu.add(0, CM_EDIT_ID, 0, "Редактировать");
        menu.add(0, CM_DELETE_ID, 0, "Удалить");
        super.onCreateContextMenu(menu, v, menuInfo);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo acmi = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();

        String id = String.valueOf(acmi.position);



        switch (item.getItemId())
        {
            case CM_DELETE_ID:
                // получаем инфу о пункте списка




                try{
                    DatabaseHelper mDatabaseHelper = new DatabaseHelper(getApplicationContext(), DatabaseHelper.DATABASE_NAME, null, 1);
                    SQLiteDatabase db = mDatabaseHelper.getWritableDatabase();

                    Cursor cursor = db.query(DatabaseHelper.DATABASE_TABLE_CATHODES, null, null, null, null, null, null) ;
                    int itemId;
                    if (cursor.moveToPosition(acmi.position))
                    {
                        itemId = cursor.getInt(cursor.getColumnIndex(BaseColumns._ID));
                        db.delete(DatabaseHelper.DATABASE_TABLE_CATHODES, "_id = " + itemId, null);
                        Toast.makeText(getApplicationContext(), "Запись с ID: " + itemId + " удалена из базы", Toast.LENGTH_LONG).show();
                    }

                    cursor.close();
                    db.close();
                    mDatabaseHelper.close();
                    updateListView();
                }
                catch (Exception e)
                {
                    Toast.makeText(getBaseContext(), "Ошибка удаления записи из базы данных: " + e.toString(), Toast.LENGTH_LONG).show();
                }





                // уведомляем, что данные изменились
                //adapter.notifyDataSetChanged();
                return true;
            case CM_EDIT_ID:



                Intent intent2 = new Intent(this, AddActivity.class);
                intent2.putExtra("isEdit",true);
                intent2.putExtra("dbPosition",acmi.position);
                startActivity(intent2);
                break;
        }

        return super.onContextItemSelected(item);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_gallery) {
            // Handle the camera action
        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_manage) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public void onBtnFabAddClick(View view) {
        Intent intent = new Intent(this, AddActivity.class);
        intent.putExtra("isEdit",false);
        intent.putExtra("dbPosition",0);
        startActivity(intent);
    }

    public void updateListView()
    {
        DatabaseHelper mDatabaseHelper = new DatabaseHelper(this, DatabaseHelper.DATABASE_NAME, null, 1);
        SQLiteDatabase cdb = mDatabaseHelper.getReadableDatabase();




        Cursor cursor = cdb.query(DatabaseHelper.DATABASE_TABLE_CATHODES, null, null, null,
                null, null, null) ;


        if (cursor.getCount() >0 )
        {
            int maxCount = cursor.getCount();
            cursor.moveToFirst();

            String[] dataBaseEvents = new String[maxCount];
            String[] dataBaseDates = new String[maxCount];
            Integer[] dataBaseIcons = new Integer[maxCount];

            int itemId;
            int i=0;
            while (i<maxCount && !cursor.isAfterLast())
            {
                dataBaseEvents[i]=cursor.getString(cursor.getColumnIndex(DatabaseHelper.TEXT_COLUMN));
                itemId = cursor.getInt(cursor.getColumnIndex(BaseColumns._ID));
                dataBaseDates[i]="id: " + itemId;
                dataBaseIcons[i]=R.drawable.add_icon;
                i++;
                cursor.moveToNext();
            }


            adapter=new CathodesListAdapter(this, dataBaseEvents, dataBaseDates, dataBaseIcons);
            final ListView list=(ListView)findViewById(R.id.listViewCathodes);
            list.setAdapter(adapter);

            list.setOnItemClickListener(new AdapterView.OnItemClickListener() {

                @Override
                public void onItemClick(AdapterView<?> parent, View view,
                                        int position, long id) {
                    // TODO Auto-generated method stub

                    Intent intent = new Intent(getApplicationContext(), CathodeActivity.class);
                    intent.putExtra("db_cathode_position", position);
                    startActivity(intent);

                }
            });


            registerForContextMenu(list);

        }

        cursor.close();
        cdb.close();
        mDatabaseHelper.close();
    }

}
