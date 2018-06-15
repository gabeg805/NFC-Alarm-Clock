package com.nfcalarmclock;

import android.os.Bundle;
import android.content.Intent;
import android.widget.Toast;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.util.Log;
import java.util.List;

import android.support.v4.content.ContextCompat;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.DividerItemDecoration;

/**
 * @brief The application's main activity.
 */
public class MainActivity
    extends AppCompatActivity
{

    AlarmCardAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_main);
        this.deleteDatabase(NacDatabaseContract.DATABASE_NAME);
        this.mAdapter = new AlarmCardAdapter(this);
        setupAddAlarmButton();
        setupAlarmList(mAdapter);
        // buildAlarmList(mAdapter);
        this.mAdapter.build();
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
        getMenuInflater().inflate(R.menu.app_bar, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        int id = item.getItemId();
        switch (id)
        {
        case android.R.id.home:
            Toast.makeText(this, "Yo this is the home",
                           Toast.LENGTH_LONG).show();
            return true;
        case R.id.menu_settings:
            Intent intent = new Intent(getApplicationContext(),
                                       SettingsActivity.class);
            startActivity(intent);
            return true;
        default:
            Toast.makeText(this, "Yo this is the default thing", 
                           Toast.LENGTH_LONG).show();
            break;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * @brief Setup the Add Alarm button.
     */
    private void setupAddAlarmButton()
    {
        FloatingActionButton button = this.getAddAlarmButton();
        button.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View view)
                {
                    Toast.makeText(MainActivity.this, "Here is a toast!", 
                                   Toast.LENGTH_LONG).show();
                    // List<Alarm> alarmlist = mAdapter.getAlarms();
                    // alarmlist.add(new Alarm());
                    // mAdapter.notifyDataSetChanged();
                    mAdapter.add(new Alarm());

                    // Intent intent = new Intent(getApplicationContext(),
                    //                            AlarmAddActivity.class);
                    // startActivity(intent);

                    // AddAlarmFragment addalarm = new AddAlarmFragment();
                    // AlarmDaysDialogFragment days = new AlarmDaysDialogFragment();
                    // FragmentManager manager = MainActivity.this.getSupportFragmentManager();
                    // FragmentTransaction transaction = manager.beginTransaction();
                    // transaction.add(addalarm, "AddAlarm");
                    // transaction.add(days, "DaysAlarm");
                    // // transaction.add(R.id.fragment_yo, addalarm);
                    // // transaction.replace(R.id.fragment_main, addalarm);
                    // // transaction.addToBackStack(null);
                    // transaction.commit();
                    // // AddAlarmFragment timePicker = new AddAlarmFragment();
                    // // timePicker.show(getSupportFragmentManager(), "time picker");
                }
            });

        RecyclerView alarmlist = this.getAlarmList();
        alarmlist.addOnScrollListener(new RecyclerView.OnScrollListener()
            {
                @Override
                public void onScrolled(RecyclerView recyclerView, int dx,
                                       int dy)
                {
                    FloatingActionButton fab = getAddAlarmButton();
                    if ((dy > 0) && fab.isShown())
                    {
                        fab.hide();
                    }
                    else if ((dy < 0) && !fab.isShown())
                    {
                        fab.show();
                    }
                }
            });
    }

    /**
     * @brief Setup the alarm list content layout.
     */
    private void setupAlarmList(AlarmCardAdapter adapter)
    {
        RecyclerView alarmlist = this.getAlarmList();
        RecyclerView.LayoutManager layoutmanager = new LinearLayoutManager(this);
        alarmlist.setLayoutManager(layoutmanager);
        alarmlist.setItemAnimator(new DefaultItemAnimator());
        alarmlist.setAdapter(adapter);
        Drawable divider = ContextCompat.getDrawable(this, R.drawable.divider);
        DividerItemDecoration itemdecoration = new DividerItemDecoration(
            getApplicationContext(), LinearLayoutManager.VERTICAL);
        itemdecoration.setDrawable(divider);
        alarmlist.addItemDecoration(itemdecoration);
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

// AddAlarmFragment addalarm = new AddAlarmFragment();
// FragmentManager manager = getSupportFragmentManager();
// FragmentTransaction transaction = manager.beginTransaction();
// transaction.replace(R.id.fragment_main, addalarm);
// transaction.addToBackStack(null);
// transaction.commit();

// Snackbar.make(view, "Display About",
//               Snackbar.LENGTH_LONG)
//     .setAction("Action", null).show();
