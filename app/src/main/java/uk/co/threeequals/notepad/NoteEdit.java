/**
 * Michael 2015
 * Main Activity for editing Notes
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
import java.util.Locale;

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
        showTime(cal);
    }

    /**
     * Format and show time in view
     * @param cl Calendar time to show
     */
    public void showTime(Calendar cl){
        SimpleDateFormat format = new SimpleDateFormat("h:mm a", Locale.UK);
        mAlarmTimeoutText.setText(format.format(cl.getTime()));
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

    /**
     * Using cursor populate data entry fields with data from Db row
     */
    private void populateFields() {
        if (mRowId != null) {
            Cursor note = mDbHelper.fetchNote(mRowId);
            startManagingCursor(note);//Using deprecated as the other option is a lot more code and overly complicated

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

            String time = note.getString(
                    note.getColumnIndexOrThrow(NotesDbAdapter.KEY_ALARM));
            if(time!= null && !time.equals("")) {
                cal = Calendar.getInstance(); // creates calendar
                cal.setTime(new Date((Long.parseLong(time)))); // sets calendar time/date
                //Update the TextView
                showTime(cal);
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

    /**
     * Create a new date time picker dialog
     * @param v
     */
    public void showTimePickerDialog(View v) {
        DialogFragment newFragment;
        if(cal != null){
            int minutes = cal.get(Calendar.MINUTE);
            int hours = cal.get(Calendar.HOUR_OF_DAY);
            newFragment = TimePickerFragment.newInstance(0, hours, minutes);
        } else {
            newFragment = TimePickerFragment.newInstance(0, 11, 43);
        }
        newFragment.show(getSupportFragmentManager(), "timePicker");
    }

    @Override
    protected void onResume() {
        super.onResume();
        populateFields();
    }

    /**
     * Save the activity state to the db
     */
    private void saveState() {
        String body = mBodyText.getText().toString();
        String type = "";
        Date alarm = null;

        if(mTypeRadio.getCheckedRadioButtonId()!=-1){
            int id= mTypeRadio.getCheckedRadioButtonId();
            View radioButton = mTypeRadio.findViewById(id);
            int radioId = mTypeRadio.indexOfChild(radioButton);
            RadioButton btn = (RadioButton) mTypeRadio.getChildAt(radioId);
            type = (String) btn.getText();
        }

        if(cal != null && type.equals("urgent")) {
            alarm = cal.getTime();
        }

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
