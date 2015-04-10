package uk.co.threeequals.notepad;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.View;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * Created by michaelwaterworth on 23/03/15.
 */
public class MyListAdapter extends SimpleCursorAdapter {

    public MyListAdapter(Context context, int layout, Cursor cursor, String[] from, int[] to) {
        super(context, layout, cursor, from, to);
    }

    /**
     * For each list item create object from cursor when bound to a view
     * @param view
     * @param context
     * @param cursor
     */
    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        // Create the bodyText textview
        TextView bodyText = (TextView) view.findViewById(R.id.textBody);
        bodyText.setText(cursor.getString(cursor.getColumnIndex(NotesDbAdapter.KEY_BODY)));

        TextView alarmText = (TextView) view.findViewById(R.id.datetime);
        long time = cursor.getLong(cursor.getColumnIndex(NotesDbAdapter.KEY_ALARM));

        if(time != 0) {
            Calendar cal = Calendar.getInstance(); // creates calendar
            cal.setTime(new Date((time))); // sets calendar time/date

            //Update the TextView
            alarmText.setText(formatTime(cal));
        } else {
            alarmText.setText("");
        }

        // create the typeText textview
        TextView typeText = (TextView) view.findViewById(R.id.textSeverity);
        typeText.setText(cursor.getString(cursor.getColumnIndex(NotesDbAdapter.KEY_TYPE)));

        switch (cursor.getString(cursor.getColumnIndex(NotesDbAdapter.KEY_TYPE))){
            case "important":
                typeText.setTextColor(context.getResources().getColor(R.color.orange));
                break;
            case "urgent":
                typeText.setTextColor(context.getResources().getColor(R.color.red));
                break;
            case "normal":
                typeText.setTextColor(context.getResources().getColor(R.color.green));
                break;
        }
    }

    public String formatTime(Calendar cl){
        SimpleDateFormat format = new SimpleDateFormat("h:mm a", Locale.UK);
        return format.format(cl.getTime());
    }
}