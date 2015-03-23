package uk.co.threeequals.notepad;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.View;
import android.widget.TextView;

/**
 * Created by michaelwaterworth on 23/03/15.
 */
public class MyListAdapter extends SimpleCursorAdapter {

    public MyListAdapter(Context context, int layout, Cursor cursor, String[] from, int[] to) {
        super(context, layout, cursor, from, to);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        // Create the bodyText textview
        TextView bodyText = (TextView) view.findViewById(R.id.textBody);
        bodyText.setText(cursor.getString(cursor.getColumnIndex(NotesDbAdapter.KEY_BODY)));

        // create the typeText textview
        TextView typeText = (TextView) view.findViewById(R.id.textSeverity);
        typeText.setText(cursor.getString(cursor.getColumnIndex(NotesDbAdapter.KEY_TYPE)));
    }
}