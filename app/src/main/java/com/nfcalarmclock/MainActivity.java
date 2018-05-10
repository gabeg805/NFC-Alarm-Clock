package com.nfcalarmclock;

import android.os.Bundle;
import android.content.res.Configuration;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.view.View;
import android.support.v4.app.Fragment;
import android.support.v4.view.GravityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.ActionBar;
import android.view.Menu;
import android.view.MenuItem;

import android.content.res.Resources;
import android.graphics.Rect;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.TypedValue;
import android.widget.ImageView;

import java.util.ArrayList;
import java.util.List;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;

import android.content.Intent;
import android.widget.Toast;


public class MainActivity
    extends AppCompatActivity
{

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        FloatingActionButton addalarm = this.getAddAlarmButton();
        RecyclerView alarmlist = this.getAlarmList();
        AlarmAdapter adapter = new AlarmAdapter(this);
        setupAddAlarmButton(addalarm);
        setupAlarmList(alarmlist, adapter);
        buildAlarmList(adapter);
    }

    @Override
    public void onBackPressed()
    {
        super.onBackPressed();
    }

    /**
     * @brief Create the options menu in the action bar.
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.menu_settings, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        int id = item.getItemId();
        // Snackbar.make(view, "Display About",
        //               Snackbar.LENGTH_LONG)
        //     .setAction("Action", null).show();
        // Intent intent;
        // intent = new Intent(getApplicationContext(), AddAlarmActivity.class);
        // startActivity(intent);
        // setTitle(item.getTitle());
        switch (id)
        {
        case android.R.id.home:
            Toast.makeText(this, "Yo this is the home", 
                           Toast.LENGTH_LONG).show();
            return true;
        case R.id.menu_settings:
            Toast.makeText(this, "This is the menu setting", 
                           Toast.LENGTH_LONG).show();
            break;
        default:
            Toast.makeText(this, "Yo this is the default thing", 
                           Toast.LENGTH_LONG).show();
            break;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Adding few alarm for testing
     */
    private void buildAlarmList(AlarmAdapter adapter)
    {
        List<Alarm> alarmList = adapter.getAlarms();
        int[] covers = new int[] { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11 };
        Alarm a;

        a = new Alarm("True Romance", 13, covers[0]);
        alarmList.add(a);

        a = new Alarm("Xscpae", 8, covers[1]);
        alarmList.add(a);

        a = new Alarm("Maroon 5", 11, covers[2]);
        alarmList.add(a);

        a = new Alarm("Born to Die", 12, covers[3]);
        alarmList.add(a);

        a = new Alarm("Honeymoon", 14, covers[4]);
        alarmList.add(a);

        a = new Alarm("I Need a Doctor", 1, covers[5]);
        alarmList.add(a);

        a = new Alarm("Loud", 11, covers[6]);
        alarmList.add(a);

        a = new Alarm("Legend", 14, covers[7]);
        alarmList.add(a);

        a = new Alarm("Hello", 11, covers[8]);
        alarmList.add(a);

        a = new Alarm("Greatest Hits", 17, covers[9]);
        alarmList.add(a);

        adapter.notifyDataSetChanged();
    }

    /**
     * @brief Setup the Add Alarm button.
     */
    private void setupAddAlarmButton(FloatingActionButton button)
    {
        button.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View view)
                {
                    Intent intent = new Intent(getApplicationContext(), AddAlarmActivity.class);
                    startActivity(intent);
                    // AddAlarmFragment addalarm = new AddAlarmFragment();
                    // FragmentManager manager = getSupportFragmentManager();
                    // FragmentTransaction transaction = manager.beginTransaction();
                    // transaction.replace(R.id.fragment_main, addalarm);
                    // transaction.addToBackStack(null);
                    // transaction.commit();
                }
            });
    }

    /**
     * @brief Setup the alarm list content layout.
     */
    private void setupAlarmList(RecyclerView alarmlist, AlarmAdapter adapter)
    {
        RecyclerView.LayoutManager layoutmanager = new LinearLayoutManager(this);
        alarmlist.setLayoutManager(layoutmanager);
        alarmlist.setItemAnimator(new DefaultItemAnimator());
        alarmlist.setAdapter(adapter);
    }

    /**
     * @brief Return the Add Alarm button.
     * 
     * @return Floating action button.
     */
    private FloatingActionButton getAddAlarmButton()
    {
        return (FloatingActionButton) findViewById(R.id.fab_add_alarm);
    }

    /**
     * @brief Return the layout containing the list of alarms.
     * 
     * @return RecyclerView.
     */
    private RecyclerView getAlarmList()
    {
        return (RecyclerView) findViewById(R.id.content_alarm_list);
    }


}
