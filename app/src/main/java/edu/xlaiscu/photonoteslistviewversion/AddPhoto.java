package edu.xlaiscu.photonoteslistviewversion;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
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
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;

import org.w3c.dom.Text;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

public class AddPhoto extends AppCompatActivity implements
        MediaPlayer.OnCompletionListener, SensorEventListener,
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
    private int maxRecId;
    final int requestCode = 1234;
    final String albumName = "photonoteslistviewversion";

    String fileName;
    String standardFileName;
    String thumbFileName;
    String newFileName;

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
    TouchDrawView touchDrawView;

    // sensor manager
    private SensorManager mSensorManager;
    private float mAccel; // acceleration apart from gravity
    private float mAccelCurrent; // current acceleration including gravity
    private float mAccelLast; // last acceleration including gravity

    // location recording
    GoogleApiClient mGoogleApiClient = null;
    Location mLastLocation;
    double latLocation;
    double lngLocation;


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

        // sensor manager
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mAccel = 0.00f;
        mAccelCurrent = SensorManager.GRAVITY_EARTH;
        mAccelLast = SensorManager.GRAVITY_EARTH;

        // location
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this).addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();

        }


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

                fileName = getOutputFileName();
                standardFileName = fileName + ".bmp";
                cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.parse(standardFileName));

                startActivityForResult(cameraIntent, 1234);


                // get location
                latLocation = mLastLocation.getLatitude();
                lngLocation = mLastLocation.getLongitude();

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


    }

    // preview picture and draw
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode != 1234 || resultCode != RESULT_OK) return;

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        Bitmap bitmap = BitmapFactory.decodeFile(standardFileName.substring(8), options);
        int screenWidth = DeviceDimensionsHelper.getDisplayWidth(getApplicationContext());
        BitmapScaler.scaleToFitWidth(bitmap, screenWidth);
        Drawable drawable = new BitmapDrawable(getResources(), bitmap);

        touchDrawView = (TouchDrawView) findViewById(R.id.myview);

        touchDrawView.setBackground(drawable);

        final Button drawButton = (Button) findViewById(R.id.drawButton);

        drawButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                touchDrawView.drawable = true;
                if (touchDrawView == null) {
                    Log.e("draw_on_picture", "we have a problem");
                }

                // Clear picture
                ((Button) findViewById(R.id.clearButton)).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        touchDrawView.clear();
                    }
                });
            }
        });

        // Save note
        final Button saveButton = (Button) findViewById(R.id.saveButton);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (standardFileName == null) {
                    Toast.makeText(getApplicationContext(), "You have to take a picture!", Toast.LENGTH_LONG).show();
                }
                else {
                    NoteInfo ni = new NoteInfo();
                    thumbFileName = fileName + "_thumb.bmp";

                    Thumbify.generateThumbnail(standardFileName, thumbFileName);
                    ni.thumbFile = thumbFileName;

                    newFileName = fileName + "_draw.jpg";

                    try {

                        touchDrawView.setDrawingCacheEnabled(true);
                        Bitmap drawingBitmap = touchDrawView.getDrawingCache();
                        FileOutputStream fo = new FileOutputStream(newFileName.substring(7));

                        drawingBitmap.compress(Bitmap.CompressFormat.JPEG, 100, fo);
                        fo.flush();
                        fo.close();
                    }catch (IOException e) {
                        Log.e("fo","fo failed");
                    }
                    ni.photoFileName = newFileName;

                    EditText vCaption = (EditText) findViewById(R.id.textCaption);
                    ni.caption = vCaption.getText().toString();

                    ni.audioFileName = mFileName;

                    ni.lat = latLocation;
                    ni.lng = lngLocation;

                    dbHelper.add(ni);
                    cursor.requery();
                    na.notifyDataSetChanged();

                    Intent returnIntent = new Intent(AddPhoto.this, MainActivity.class);
                    startActivity(returnIntent);
                }
            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    protected void onPause() {
        mSensorManager.unregisterListener(this);
        super.onPause();
    }

    public void onSensorChanged(SensorEvent se) {
        float x = se.values[0];
        float y = se.values[1];
        float z = se.values[2];
        mAccelLast = mAccelCurrent;
        mAccelCurrent = (float) Math.sqrt((double) (x*x + y*y + z*z));
        float delta = mAccelCurrent - mAccelLast;
        mAccel = mAccel * 0.9f + delta * 0.1f; // perform low-cut filter

        displayAcceleration();
    }

    public void onAccuracyChanged(Sensor sensor, int accuracy) {}

    private void displayAcceleration() {
        float accel = Math.abs(mAccel);
        if (accel > 1.5f) {
            touchDrawView.clear();
        }
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        Toast.makeText(getApplicationContext(), "playback is completed", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    @Override
    public void onStop() {
        super.onStop();
        mGoogleApiClient.disconnect();
    }

    @Override
    public void onConnected(Bundle bundle) {
        toast("onConnected() is called");
        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
    }

    @Override
    public void onConnectionSuspended(int i) {
        toast("onConnectionSuspended() is called : i=" + i);
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        toast("onConnectionFailed() is called");
    }

    void toast(String msg) {
        Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
    }



    private String getOutputFileName() {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String filenamePart =
                "file://"
                        + Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
                        + "/JPEG_"
                        + timeStamp;

        return filenamePart;
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
