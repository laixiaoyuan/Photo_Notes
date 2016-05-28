package edu.xlaiscu.photonoteslistviewversion;

import android.content.Context;
import android.database.Cursor;
import android.media.Image;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Created by Lexie on 5/12/16.
 */
public class NoteAdapter extends CursorAdapter {

    public NoteAdapter(Context context, Cursor cursor, int flags) {
        super(context, cursor, flags);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.namecard_layout, parent, false);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        String fileName = cursor.getString(cursor.getColumnIndex("thumbFile"));
        String note = cursor.getString(cursor.getColumnIndex("caption"));

        ((TextView)view.findViewById(R.id.txtNote)).setText(note);
        ((ImageView)view.findViewById(R.id.icon)).setImageURI(Uri.parse(fileName));

    }
}
