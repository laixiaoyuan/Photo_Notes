package edu.xlaiscu.photonoteslistviewversion;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;

import org.w3c.dom.Text;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

public class AddPhoto extends AppCompatActivity implements View.OnClickListener{
    private int maxRecId;

    final int requestCode = 1234;
    final String albumName = "photonoteslistviewversion";
    String fileName;
    String caption;
    NoteAdapter na;
    NoteDbHelper dbHelper;
    Cursor cursor;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_photo);

        // action bar
        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("Photo Notes");
        actionBar.setSubtitle("Take a photo and write caption.");
        actionBar.setBackgroundDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.action_bar_background));
        actionBar.setIcon(R.mipmap.ic_launcher);
        actionBar.setDisplayShowHomeEnabled(true);

        dbHelper = new NoteDbHelper(this);
        cursor = dbHelper.fetchAll();
        na = new NoteAdapter(this, cursor, 0);

        Button addButton = (Button) findViewById(R.id.addButton);
        addButton.setOnClickListener(this);

        Button saveButton = (Button) findViewById(R.id.saveButton);
        saveButton.setOnClickListener(this);

        acquireRunTimePermissions();

    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {

            case R.id.addButton:

                Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                if (cameraIntent.resolveActivity(getPackageManager()) == null) {
                    Toast.makeText(getApplicationContext(), "Cannot take pictures on this device!", Toast.LENGTH_SHORT).show();
                    return;
                }

                fileName = getOutputFileName()[0];
                cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.parse(fileName));

                startActivityForResult(cameraIntent, 1234);
                break;

            case R.id.saveButton:
                if (fileName == null) {
                    Toast.makeText(getApplicationContext(), "You have to take a picture!", Toast.LENGTH_LONG).show();
                }
                else {
                    NoteInfo ni = new NoteInfo();
                    ni.fileName = fileName;

                    String thumbFile = getOutputFileName()[1];
                    Thumbify.generateThumbnail(fileName, thumbFile);
                    ni.thumbFile = thumbFile;

                    EditText vCaption = (EditText) findViewById(R.id.textCaption);
                    ni.caption = vCaption.getText().toString();

                    dbHelper.add(ni);
                    cursor.requery();
                    na.notifyDataSetChanged();

                    Intent returnIntent = new Intent(AddPhoto.this, MainActivity.class);
                    startActivity(returnIntent);
                }
                break;
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode != 1234 || resultCode != RESULT_OK) return;

        ImageView imageView = (ImageView) findViewById(R.id.addedPhoto);
        imageView.setImageURI(Uri.parse(fileName));
    }


    private String[] getOutputFileName() {
        String[] normalAndThumb = new String[2];
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String filenamePart =
                "file://"
                        + Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
                        + "/JPEG_"
                        + timeStamp;

        String normalFile = filenamePart + ".bmp";
        String thumFile = filenamePart + "_thumb.bmp";

        normalAndThumb[0] = normalFile;
        normalAndThumb[1] = thumFile;

        return normalAndThumb;
    }

    private void acquireRunTimePermissions() {
        int permissionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    111);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        if (requestCode != 111) return;
        if (grantResults.length >= 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(getApplicationContext(), "Great! We have the permission!", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(getApplicationContext(), "Cannot write to external storage! App will not work properly!", Toast.LENGTH_SHORT).show();
        }
    }


}
