package uk.co.threeequals.notepad;

import android.app.Activity;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.text.format.DateFormat;
import android.widget.TimePicker;

import java.util.Calendar;

/**
 * Created by michaelwaterworth on 23/03/15.
 */
public class TimePickerFragment extends DialogFragment
        implements TimePickerDialog.OnTimeSetListener {

    OnTimePickedListener mCallback;

    public interface OnTimePickedListener {
        public void onTimePicked(int hour, int minute);
    }

    static TimePickerFragment newInstance(int id, int hour, int minute)
    {
        Bundle args = new Bundle();
        args.putInt("picker_id", id);
        args.putInt("hour", hour);
        args.putInt("minute", minute);
        TimePickerFragment fragment = new TimePickerFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        try {
            mCallback = (OnTimePickedListener)activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement OnTimePickedListener.");
        }
    }


    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Bundle args = getArguments();

        final Calendar c = Calendar.getInstance();

        //The second input is a default value in case hour or minute are empty
        int hour = args.getInt("hour", c.get(Calendar.HOUR_OF_DAY));
        int minute = args.getInt("minute", c.get(Calendar.MINUTE));

        // create a new instance of TimePickerDialog and return it
        return new TimePickerDialog(getActivity(), this, hour, minute,
                DateFormat.is24HourFormat(getActivity()));
    }

    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        if(mCallback != null)
        {
            mCallback.onTimePicked(hourOfDay, minute);
        }
    }
}