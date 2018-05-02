package com.nfcalarmclock;

import android.os.Bundle;
import android.content.res.Configuration;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.support.v7.app.ActionBar;
import android.view.Menu;
import android.view.MenuItem;

public class MainActivity extends AppCompatActivity
    implements NavigationView.OnNavigationItemSelectedListener
{

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = getToolbar();
        setSupportActionBar(toolbar);
        DrawerLayout drawer = getNavigationDrawer();
        FloatingActionButton newalarm = getNewAlarmButton();
        setupNavigationDrawer(drawer, toolbar);
        setupNewAlarmButton(newalarm);
        getNavigationDrawerView().setNavigationItemSelectedListener(this);
    }

    @Override
    public void onBackPressed()
    {
        DrawerLayout drawer = getNavigationDrawer();
        if (drawer.isDrawerOpen(GravityCompat.START))
        {
            drawer.closeDrawer(GravityCompat.START);
        }
        else
        {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_settings, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        int id = item.getItemId();
        switch (id)
        {
        case android.R.id.home:
            getNavigationDrawer().openDrawer(GravityCompat.START);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item)
    {
        // Create a new fragment and specify the fragment to show based on nav item clicked
        Fragment fragment = null;
        Class fragmentClass;
        int id = item.getItemId();

        // switch(id)
        // {
        // case R.id.nav_first_fragment:
        //     fragmentClass = FirstFragment.class;
        //     break;
        // case R.id.nav_second_fragment:
        //     fragmentClass = SecondFragment.class;
        //     break;
        // case R.id.nav_third_fragment:
        //     fragmentClass = ThirdFragment.class;
        //     break;
        // default:
        //     fragmentClass = FirstFragment.class;
        // }

        // try {
        //     fragment = (Fragment) fragmentClass.newInstance();
        // } catch (Exception e) {
        //     e.printStackTrace();
        // }

        // // Insert the fragment by replacing any existing fragment
        // FragmentManager fragmentManager = getSupportFragmentManager();
        // fragmentManager.beginTransaction().replace(R.id.flContent, fragment).commit();

        // Highlight the selected item has been done by NavigationView
        item.setChecked(true);
        // Set action bar title
        setTitle(item.getTitle());
        // Close the navigation drawer
        // mDrawer.closeDrawers();
        getNavigationDrawer().closeDrawer(GravityCompat.START);


        // DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        // drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    /**
     * @brief Setup the Navigation Drawer.
     */
    private void setupNavigationDrawer(DrawerLayout drawer, Toolbar toolbar)
    {
        // ActionBar actionbar = getSupportActionBar();
        // actionbar.setDisplayHomeAsUpEnabled(true);
        // actionbar.setHomeAsUpIndicator(R.mipmap.ic_menu_white_24dp);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
            this, drawer, toolbar, R.string.drawer_open, R.string.drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
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
                    Snackbar.make(view, "Replace with your own action",
                                  Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
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
        return (Toolbar) findViewById(R.id.toolbar);
    }

    /**
     * @brief Return the Navigation Drawer layout.
     * 
     * @return DrawerLayout.
     */
    private DrawerLayout getNavigationDrawer()
    {
        return (DrawerLayout) findViewById(R.id.drawer_layout);
    }

    /**
     * @brief Return the Navigation View of the Navigation Drawer.
     * 
     * @return NavigationView.
     */
    private NavigationView getNavigationDrawerView()
    {
        return (NavigationView) findViewById(R.id.nvView);
    }

    /**
     * @brief Return the New Alarm button.
     * 
     * @return Floating action button.
     */
    private FloatingActionButton getNewAlarmButton()
    {
        return (FloatingActionButton) findViewById(R.id.button_new_alarm);
    }
}
