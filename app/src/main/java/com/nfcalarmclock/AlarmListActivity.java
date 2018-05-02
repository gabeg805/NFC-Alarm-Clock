package com.nfcalarmclock;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

public class AlarmListActivity extends AppCompatActivity
{

    /* When you're ready, set this back to false. You need to add a timing
     * thing, where if you don't hit back before the toast finishes, then it
     * resets back to false. */
    private static boolean quitApp = true;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.content_alarm_list);

        // /* Toolbar */
        // Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        // setSupportActionBar(toolbar);

        // /* Add a new alarm */
        // FloatingActionButton newalarm = (FloatingActionButton) findViewById(R.id.button_new_alarm);
        // newalarm.setOnClickListener(new View.OnClickListener()
        //     {
        //         @Override
        //         public void onClick(View view)
        //         {
        //             Snackbar.make(view, "Replace with your own action",
        //                           Snackbar.LENGTH_LONG)
        //                 .setAction("Action", null).show();
        //         }
        //     });

        // /* Or is this the navigation drawer */
        // DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        // ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
        //     this, drawer, toolbar, R.string.navigation_drawer_open,
        //     R.string.navigation_drawer_close);
        // drawer.addDrawerListener(toggle);
        // toggle.syncState();

        // /* Navigation drawer, I think */
        // NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        // navigationView.setNavigationItemSelectedListener(this);
    }

    @Override
    public void onBackPressed()
    {
        if (!quitApp)
        {
            Toast.makeText(getApplicationContext(), R.string.quit_message,
                           Toast.LENGTH_SHORT).show();
            quitApp = true;
        }
        else
        {
            quitApp = false;
            moveTaskToBack(true);
        }
    }
}
