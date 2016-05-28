package edu.xlaiscu.photonoteslistviewversion;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CursorAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MainActivity extends AppCompatActivity implements AdapterView.OnItemClickListener{

    private int maxRecId;
    NoteAdapter noteAdapter;
    NoteDbHelper dbHelper;
    Cursor cursor;
    ListView list;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // action bar
        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("Photo Notes");
        actionBar.setSubtitle("Take a photo and write caption.");
        actionBar.setBackgroundDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.action_bar_background));
        actionBar.setIcon(R.mipmap.ic_launcher);
        actionBar.setDisplayShowHomeEnabled(true);

        dbHelper = new NoteDbHelper(this);
        cursor = dbHelper.fetchAll();
        noteAdapter = new NoteAdapter(this, cursor, 0);

        list = (ListView)findViewById(R.id.listView);
        list.setAdapter(noteAdapter);
        list.setOnItemClickListener(this);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toast("ADD action ...");
                Intent intent = new Intent(MainActivity.this, AddPhoto.class);
                startActivity(intent);
            }
        });

        maxRecId = dbHelper.getMaxRecID();
        toastShow("MacRecID is " + maxRecId);
    }


    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        toastShow("View Photo Detail : ");
        String photoFileName = cursor.getString(cursor.getColumnIndex("fileName"));
        String photoCaption = cursor.getString(cursor.getColumnIndex("caption"));

        Intent intent = new Intent(MainActivity.this, ViewPhoto.class);
        Bundle bundle = new Bundle();
        bundle.putString("photoFileName", photoFileName);
        bundle.putString("photoCaption", photoCaption);
        intent.putExtras(bundle);
        startActivity(intent);
    }


    private void toastShow(String msg) {
        Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
    }

    // menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.action_ADD:
                toast("ADD action ...");
                Intent intent = new Intent(MainActivity.this, AddPhoto.class);
                startActivity(intent);
                break;
            case R.id.action_uninstall:
                toast("Uninstall action ...");
                Uri packageURI = Uri.parse("package:" + MainActivity.class.getPackage().getName());
                Intent uninstallIntent = new Intent(Intent.ACTION_DELETE, packageURI);
                startActivity(uninstallIntent);
                break;
            default:
                toast("unknown action ...");
        }

        return super.onOptionsItemSelected(item);
    }

    private void toast(String msg) {
        Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
    }

}
