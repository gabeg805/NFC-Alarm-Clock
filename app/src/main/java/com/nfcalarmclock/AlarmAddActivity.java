package com.nfcalarmclock;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import android.app.Activity;
import android.widget.TimePicker;
// import java.util.Calendar;
// import java.util.Date;
// import java.util.Random;
// import android.content.ContentValues;
// import android.database.sqlite.SQLiteDatabase;

/**
 * @brief Add an alarm.
 */
public class AlarmAddActivity
    extends AppCompatActivity
    implements View.OnClickListener
{

    private static final String NAME = "NFCAlarmClock";
    private static int STEP = 1;
    private static int MAXSTEP = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_alarm_add);
        initButtons();
        runFragment();
    }

    @Override
    public void onClick(View v)
    {
        int id = v.getId();
        switch (id)
        {
        case R.id.alarm_add_positive_button:
            this.nextFragment();
            break;
        case R.id.alarm_add_negative_button:
            this.previousFragment();
            break;
        default:
            return;
        }

    }

    private void setupFragment(int step)
    {
        int posvisible = View.VISIBLE;
        int negvisible = View.VISIBLE;
        String postext = "Next";
        String negtext = "Previous";
        if (step == 1)
        {
            negvisible = View.GONE;
        }
        if (step == MAXSTEP)
        {
            postext = "Done";
        }
        getPositiveButton().setVisibility(posvisible);
        getNegativeButton().setVisibility(negvisible);
        getPositiveButton().setText(postext);
        getNegativeButton().setText(negtext);
    }

    private void runFragment()
    {
        setupFragment(STEP);
        displayFragment(STEP);
    }

    private void nextFragment()
    {
        STEP++;
        runFragment();
    }

    private void previousFragment()
    {
        STEP--;
        runFragment();
    }

    private void displayFragment(int step)
    {
        Bundle args = new Bundle();
        Fragment fragment = null;
        String tag;

        Toast.makeText(this, "Step: "+String.valueOf(step), Toast.LENGTH_SHORT).show();

        switch (step)
        {
        case 1:
            fragment = new AlarmAddTimeFragment();
            break;
        case 2:
            fragment = new AlarmAddDaysFragment();
            break;
        default:
            return;
        }

        tag = fragment.getClass().getSimpleName();
        args.putString("tag", tag);
        fragment.setArguments(args);

        FragmentManager manager = getSupportFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();
        // transaction.add(fragment, tag);
        transaction.replace(R.id.alarm_add_fragment_container, fragment, tag);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    private void initButtons()
    {
        Button positive = getPositiveButton();
        Button negative = getNegativeButton();
        positive.setOnClickListener(this);
        negative.setOnClickListener(this);
    }

    private Button getPositiveButton()
    {
        return (Button) findViewById(R.id.alarm_add_positive_button);
    }

    private Button getNegativeButton()
    {
        return (Button) findViewById(R.id.alarm_add_negative_button);
    }

}
