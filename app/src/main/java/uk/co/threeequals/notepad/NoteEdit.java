/*
 * Copyright (C) 2008 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package uk.co.threeequals.notepad;

import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class NoteEdit extends FragmentActivity implements TimePickerFragment.OnTimePickedListener {

    private EditText mBodyText;
    private LinearLayout mAlarmTimeout;
    private TextView mAlarmTimeoutText;
    private Calendar cal;

    private RadioGroup mTypeRadio;
    private Long mRowId;
    private NotesDbAdapter mDbHelper;

    // This is a handle so that we can call methods on our service
    private ScheduleClient scheduleClient;

    /**
     * On time picked event, converts hour and minutes values to milliseconds
     * milliseconds and sets a new value for the layout in the activity.
     * @param hour          Hour value.
     * @param minute        Minutes value.
     */
    @Override

    public void onTimePicked(int hour, int minute) {
        cal = Calendar.getInstance(); // creates calendar
        cal.setTime(new Date()); // sets calendar time/date
        cal.set(Calendar.MINUTE, minute);
        cal.set(Calendar.HOUR_OF_DAY, hour);

        SimpleDateFormat format = new SimpleDateFormat("h:mm a");
        mAlarmTimeoutText.setText(format.format(cal.getTime()));
    }

    /**
     * This is the onClick called from the XML to set a new notification
     */
    public void onDateSelectedButtonClick(View v){
        // Get the date from our datepicker
        // Create a new calendar set to the date chosen
        // we set the time to midnight (i.e. the first minute of that day)
        // Ask our service to set an alarm for that date, this activity talks to the client that talks to the service
        Log.d("Time", cal.getTimeInMillis() + "");
        scheduleClient.setAlarmForNotification(cal);
        // Notify the user what they just did
        Toast.makeText(this, "Notification set", Toast.LENGTH_SHORT).show();
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mDbHelper = new NotesDbAdapter(this);
        mDbHelper.open();

        setContentView(R.layout.note_edit);

        mBodyText = (EditText) findViewById(R.id.body);
        mAlarmTimeout = (LinearLayout) findViewById(R.id.alarm);
        mAlarmTimeoutText = (TextView) findViewById(R.id.alarmText);
        mTypeRadio = (RadioGroup) findViewById(R.id.type);

        Button confirmButton = (Button) findViewById(R.id.confirm);

        mRowId = (savedInstanceState == null) ? null :
            (Long) savedInstanceState.getSerializable(NotesDbAdapter.KEY_ROWID);
		if (mRowId == null) {
			Bundle extras = getIntent().getExtras();
			mRowId = extras != null ? extras.getLong(NotesDbAdapter.KEY_ROWID)
									: null;
		}

        mTypeRadio.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener(){
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int checkedId){
                RadioButton checkedButton = (RadioButton) findViewById(checkedId);
                if(checkedButton.getText().toString().compareTo("urgent") == 0){
                    mAlarmTimeout.setVisibility(View.VISIBLE);
                } else {
                    mAlarmTimeout.setVisibility(View.GONE);
                }
            }
        });

		populateFields();

        confirmButton.setOnClickListener(new View.OnClickListener() {

            public void onClick(View view) {
                setResult(RESULT_OK);
                finish();
            }

        });

        scheduleClient = new ScheduleClient(this);
        scheduleClient.doBindService();
    }

    private void populateFields() {
        if (mRowId != null) {
            Cursor note = mDbHelper.fetchNote(mRowId);
            startManagingCursor(note);

            mBodyText.setText(note.getString(
                    note.getColumnIndexOrThrow(NotesDbAdapter.KEY_BODY)));

            if(note.getString(note.getColumnIndexOrThrow(NotesDbAdapter.KEY_TYPE)).compareTo("normal") == 0){
                mTypeRadio.check(R.id.radio_normal);
            }
            if(note.getString(note.getColumnIndexOrThrow(NotesDbAdapter.KEY_TYPE)).compareTo("urgent") == 0){
                mTypeRadio.check(R.id.radio_urgent);
            }
            if(note.getString(note.getColumnIndexOrThrow(NotesDbAdapter.KEY_TYPE)).compareTo("important") == 0){
                mTypeRadio.check(R.id.radio_important);
            }
        } else {
            mTypeRadio.check(R.id.radio_normal);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        saveState();
        outState.putSerializable(NotesDbAdapter.KEY_ROWID, mRowId);
    }

    @Override
    protected void onStop() {
        // When our activity is stopped ensure we also stop the connection to the service
        // this stops us leaking our activity into the system *bad*
        if(scheduleClient != null)
            scheduleClient.doUnbindService();
        super.onStop();
    }

    @Override
    protected void onPause() {
        super.onPause();
        saveState();
    }


    public void showTimePickerDialog(View v) {
        //DialogFragment newFragment = new TimePickerFragment();
        DialogFragment newFragment = TimePickerFragment.newInstance(0, 11, 43);
        newFragment.show(getSupportFragmentManager(), "timePicker");
    }

    @Override
    protected void onResume() {
        super.onResume();
        populateFields();
    }

    private void saveState() {
        String body = mBodyText.getText().toString();
        String type = "";

        if(mTypeRadio.getCheckedRadioButtonId()!=-1){
            int id= mTypeRadio.getCheckedRadioButtonId();
            View radioButton = mTypeRadio.findViewById(id);
            int radioId = mTypeRadio.indexOfChild(radioButton);
            RadioButton btn = (RadioButton) mTypeRadio.getChildAt(radioId);
            type = (String) btn.getText();
        }

        Date alarm = null;
        //TimeOffset in minutes

        String timeOffsetStr = "";//mAlarmTimeout.getText().toString();
        try {
            if (timeOffsetStr.equals("")) {
                int timeOffset = Integer.parseInt(timeOffsetStr);

                Calendar cal = Calendar.getInstance(); // creates calendar
                cal.setTime(new Date()); // sets calendar time/date
                cal.add(Calendar.MINUTE, timeOffset); // adds one hour
                //cal.getTime(); // returns new date object, one hour in the future

                alarm = cal.getTime();
            }
        } catch (NumberFormatException e){}

        if (mRowId == null) {
            long id = mDbHelper.createNote(body, type, alarm);
            if (id > 0) {
                mRowId = id;
            }
        } else {
            mDbHelper.updateNote(mRowId, body, type, alarm);
        }
    }

}
