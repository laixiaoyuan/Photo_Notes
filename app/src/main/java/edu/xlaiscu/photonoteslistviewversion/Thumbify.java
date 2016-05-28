package edu.xlaiscu.photonoteslistviewversion;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ThumbnailUtils;

import java.io.File;
import java.io.FileOutputStream;

/**
 * Created by Lexie on 5/17/16.
 */
public class Thumbify {
    static public void generateThumbnail(String imgFile, String thumbFile) {
        try {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inSampleSize = 8;
            Bitmap picture = BitmapFactory.decodeFile(imgFile.substring(8), options);

            Bitmap resized = ThumbnailUtils.extractThumbnail(picture, 120, 120);
            FileOutputStream fos = new FileOutputStream(thumbFile.substring(8));
            resized.compress(Bitmap.CompressFormat.JPEG, 90, fos);
            fos.flush();
            fos.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
}
