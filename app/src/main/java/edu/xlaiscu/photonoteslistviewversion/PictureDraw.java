package edu.xlaiscu.photonoteslistviewversion;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

public class PictureDraw extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.picture_draw);

        Bundle bundle = getIntent().getExtras();
        String photoFileName = bundle.getString("photoFileName");

        final TouchDrawView view = (TouchDrawView) findViewById(R.id.myview);
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        Bitmap bitmap = BitmapFactory.decodeFile(photoFileName.substring(8), options);
        int screenWidth = DeviceDimensionsHelper.getDisplayWidth(this);
        BitmapScaler.scaleToFitWidth(bitmap, screenWidth);
        Drawable drawable = new BitmapDrawable(getResources(), bitmap);

        view.setBackground(drawable);
//        view.setImageURI(Uri.parse(photoFileName));

        if (view == null) {
            Log.e("draw_on_picture", "we have a problem");
        }

        // Clear picture
        ((Button) findViewById(R.id.clearButton)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                view.clear();
            }
        });

    }
}
