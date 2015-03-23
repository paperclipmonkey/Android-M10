package com.android.demo.notepad3;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.View;
import android.widget.TextView;

import com.android.demo.notepad3.ItemFragment;
import com.android.demo.notepad3.R;

/**
 * Created by michaelwaterworth on 23/03/15.
 */
public class MyListAdapter extends SimpleCursorAdapter {

    public MyListAdapter(Context context, int layout, Cursor cursor, String[] from, int[] to) {
        super(context, layout, cursor, from, to);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        // Create the bodyText textview with background image
        TextView bodyText = (TextView) view.findViewById(R.id.textBody);
        bodyText.setText(cursor.getString(cursor.getColumnIndex(NotesDbAdapter.KEY_BODY)));

        // create the typeText textview
        TextView typeText = (TextView) view.findViewById(R.id.textSeverity);
        typeText.setText(cursor.getString(cursor.getColumnIndex(NotesDbAdapter.KEY_TYPE)));
    }

//    // TODO: Rename and change types of parameters
//    public static ItemFragment newInstance(String param1, String param2) {
//        ItemFragment fragment = new ItemFragment();
//        Bundle args = new Bundle();
//        args.putString("one", param1);
//        args.putString("two", param2);
//        fragment.setArguments(args);
//        return fragment;
//    }
}