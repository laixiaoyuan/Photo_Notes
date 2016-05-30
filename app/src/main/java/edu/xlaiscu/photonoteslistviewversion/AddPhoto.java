package edu.xlaiscu.photonoteslistviewversion;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
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
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

public class AddPhoto extends AppCompatActivity implements MediaPlayer.OnCompletionListener{
    private int maxRecId;
    final int requestCode = 1234;
    final String albumName = "photonoteslistviewversion";
    String fileName;
    String caption;
    NoteAdapter na;
    NoteDbHelper dbHelper;
    Cursor cursor;

    // Audio Recording part
    private static final String LOG_TAG = "AudioRecordTest";
    private static String mFileName = null;
    private MediaRecorder mRecorder = null;
    private MediaPlayer mPlayer = null;

    // Draw on top of picture
    Canvas canvas;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_photo);

        acquireRunTimePermissions();

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


        // photo taking
        final Button addButton = (Button) findViewById(R.id.addButton);
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                if (cameraIntent.resolveActivity(getPackageManager()) == null) {
                    Toast.makeText(getApplicationContext(), "Cannot take pictures on this device!", Toast.LENGTH_SHORT).show();
                    return;
                }

                fileName = getOutputFileName()[0];
                cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.parse(fileName));

                startActivityForResult(cameraIntent, 1234);

            }

        });

        // Audio Recording
        mFileName = Environment.getExternalStorageDirectory().getAbsolutePath();
        mFileName += "/audiorecordtest.3gp";

        final Button recordButton = (Button) findViewById(R.id.recordButton);
        recordButton.setOnClickListener(new View.OnClickListener() {
            boolean mStartRecording = true;
            @Override
            public void onClick(View v) {
                if (mStartRecording) {
                    recordButton.setText("Stop");
                    startRecording();
                }
                else {
                    recordButton.setText("Re-record");
                    stopRecording();
                }
                mStartRecording = !mStartRecording;
            }
        });

        // Audio playback
        final Button playButton = (Button) findViewById(R.id.playbackButton);
        playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPlayer = new MediaPlayer();
                mPlayer.setOnCompletionListener(AddPhoto.this);
                try {
                    mPlayer.setDataSource(mFileName);
                    mPlayer.prepare();
                    mPlayer.start();
                }
                catch (IOException e) {
                    Log.e(LOG_TAG, "prepare() failed");
                }
            }
        });




        // Save note
        final Button saveButton = (Button) findViewById(R.id.saveButton);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (fileName == null) {
                    Toast.makeText(getApplicationContext(), "You have to take a picture!", Toast.LENGTH_LONG).show();
                }
                else {
                    NoteInfo ni = new NoteInfo();
                    ni.photoFileName = fileName;

                    String thumbFile = getOutputFileName()[1];
                    Thumbify.generateThumbnail(fileName, thumbFile);
                    ni.thumbFile = thumbFile;

                    EditText vCaption = (EditText) findViewById(R.id.textCaption);
                    ni.caption = vCaption.getText().toString();

                    ni.audioFileName = mFileName;

                    dbHelper.add(ni);
                    cursor.requery();
                    na.notifyDataSetChanged();

                    Intent returnIntent = new Intent(AddPhoto.this, MainActivity.class);
                    startActivity(returnIntent);
                }
            }
        });

        final Button drawButton = (Button) findViewById(R.id.drawButton);
        drawButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent drawPicIntent = new Intent(AddPhoto.this, PictureDraw.class);
                Bundle bundle = new Bundle();
                bundle.putString("photoFileName", fileName);
                drawPicIntent.putExtras(bundle);
                startActivity(drawPicIntent);
            }
        });

    }

    // preview picture and draw
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode != 1234 || resultCode != RESULT_OK) return;

        ImageView imageView = (ImageView) findViewById(R.id.addedPhoto);
        imageView.setImageURI(Uri.parse(fileName));


    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        Toast.makeText(getApplicationContext(), "playback is completed", Toast.LENGTH_SHORT).show();
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

    private void startRecording() {
        mRecorder = new MediaRecorder();
        mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        mRecorder.setOutputFile(mFileName);
        mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);

        try {
            mRecorder.prepare();
        }
        catch (IOException e) {
            Log.e(LOG_TAG, "prepare() failed");
        }
        mRecorder.start();
    }

    private void stopRecording() {
        mRecorder.stop();
        mRecorder.release();
        mRecorder = null;
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
