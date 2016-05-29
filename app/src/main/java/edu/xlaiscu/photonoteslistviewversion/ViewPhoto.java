package edu.xlaiscu.photonoteslistviewversion;

import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;

public class ViewPhoto extends AppCompatActivity implements MediaPlayer.OnCompletionListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.view_photo);

        // action bar
        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("Photo Notes");
        actionBar.setSubtitle("Take a photo and write caption.");
        actionBar.setBackgroundDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.action_bar_background));
        actionBar.setIcon(R.mipmap.ic_launcher);
        actionBar.setDisplayShowHomeEnabled(true);

        Bundle bundle = getIntent().getExtras();
        String photoFileName = bundle.getString("photoFileName");
        String caption = bundle.getString("photoCaption");
        final String audioFileName = bundle.getString("audioFileName");

        TextView captionText = (TextView) findViewById(R.id.Caption);
        ImageView photoFileNameText = (ImageView) findViewById(R.id.photo);

        captionText.setText(caption);
        photoFileNameText.setImageURI(Uri.parse(photoFileName));

        ImageButton audioPlay = (ImageButton) findViewById(R.id.playBtnIcon);
        audioPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MediaPlayer mediaPlayer = new MediaPlayer();
                mediaPlayer.setOnCompletionListener(ViewPhoto.this);
                try {
                    mediaPlayer.setDataSource(audioFileName);
                    mediaPlayer.prepare();
                    mediaPlayer.start();
                }
                catch (IOException e) {
                    Log.e("AudioRecordTest", "prepare() failed");
                }
            }
        });



        Button returnButton = (Button) findViewById(R.id.returnButton);
        returnButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick (View v) {
                Intent intent = new Intent(ViewPhoto.this, MainActivity.class);
                startActivity(intent);

            }
        });
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        Toast.makeText(getApplicationContext(), "playback is completed", Toast.LENGTH_SHORT).show();
    }
}
