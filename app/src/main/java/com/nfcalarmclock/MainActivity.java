package com.nfcalarmclock;

import android.os.Bundle;
import android.content.res.Configuration;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.view.View;
// import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.view.GravityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.support.v7.app.ActionBar;
import android.view.Menu;
import android.view.MenuItem;

import android.content.res.Resources;
import android.graphics.Rect;
// import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
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


public class MainActivity extends AppCompatActivity
    // implements NavigationView.OnNavigationItemSelectedListener
{
    private RecyclerView recyclerView;
    private AlarmAdapter adapter;
    private List<Alarm> alarmList;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = getToolbar();
        setSupportActionBar(toolbar);

        if (getSupportActionBar() == null)
        {
            Toast.makeText(this, "Fuck the first action bar is null :(", 
                           Toast.LENGTH_LONG).show();
        }
        else
        {
            Toast.makeText(this, "It works! :)", 
                           Toast.LENGTH_LONG).show();
        }

        FloatingActionButton newalarm = getNewAlarmButton();
        setupNewAlarmButton(newalarm);
        recyclerView = (RecyclerView) findViewById(R.id.alarm_list);
        alarmList = new ArrayList<>();
        adapter = new AlarmAdapter(this, alarmList);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(adapter);

        prepareAlarms();

        // if (getSupportActionBar() == null)
        // {
        //     Toast.makeText(this, "Fuck the action bar is definitely null :(", 
        //                    Toast.LENGTH_LONG).show();
        // }
        // else
        // {
        //     Toast.makeText(this, "It definitely works! :)", 
        //                    Toast.LENGTH_LONG).show();
        // }

    }

    // @Override
    // public boolean onSupportNavigateUp()
    // {  
    //     finish();  
    //     return true;  
    // }

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
        // Snackbar.make(view, "Display About",
        //               Snackbar.LENGTH_LONG)
        //     .setAction("Action", null).show();
        int id = item.getItemId();

        // Intent intent;
        // intent = new Intent(getApplicationContext(), AddAlarmActivity.class);
        // startActivity(intent);
        // setTitle(item.getTitle());
        switch (id)
        {
        case android.R.id.home:
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // @SuppressWarnings("StatementWithEmptyBody")
    // @Override
    // public boolean onNavigationItemSelected(MenuItem item)
    // {
    //     // Create a new fragment and specify the fragment to show based on nav item clicked
    //     // Fragment fragment;
    //     // FragmentManager manager;
    //     // FragmentTransaction transaction;

    //     // manager = getSupportFragmentManager();
    //     // transaction = manager.beginTransaction();
    //     // transaction.replace(R.id.fragment_new_alarm, fragment);
    //     // transaction.addToBackStack(null);
    //     // transaction.commit();

    //     // item.setChecked(true);
    //     // getNavigationDrawer().closeDrawer(GravityCompat.START);

    //     return true;
    // }


    /**
     * Adding few alarm for testing
     */
    private void prepareAlarms()
    {
        // int[] covers = new int[]
        //     {
        //         R.drawable.album1,
        //         R.drawable.album2,
        //         R.drawable.album3,
        //         R.drawable.album4,
        //         R.drawable.album5,
        //         R.drawable.album6,
        //         R.drawable.album7,
        //         R.drawable.album8,
        //         R.drawable.album9,
        //         R.drawable.album10,
        //         R.drawable.album11
        //     };

        int[] covers = new int[] { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11 };

        Alarm a = new Alarm("True Romance", 13, covers[0]);
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
     * @brief Setup the New Alarm button.
     */
    private void setupNewAlarmButton(FloatingActionButton button)
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

                    // Snackbar.make(view, "Replace with your own action",
                    //               Snackbar.LENGTH_LONG)
                    //     .setAction("Action", null).show();
                }
            });
    }

    /**
     * @brief Return the Toolbar.
     * 
     * @return Toolbar.
     */
    private Toolbar getToolbar()
    {
        return (Toolbar) findViewById(R.id.ab_toolbar);
    }

    /**
     * @brief Return the New Alarm button.
     * 
     * @return Floating action button.
     */
    private FloatingActionButton getNewAlarmButton()
    {
        return (FloatingActionButton) findViewById(R.id.fab_new_alarm);
    }
}
