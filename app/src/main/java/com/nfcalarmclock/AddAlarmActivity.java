package com.nfcalarmclock;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.support.v4.app.Fragment;

import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.ActionBar;
import android.support.design.widget.Snackbar;
import android.widget.Toast;

public class AddAlarmActivity
    extends AppCompatActivity
{

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_alarm);
        ActionBar actionbar = getSupportActionBar();
        if (actionbar == null)
        {
            Toast.makeText(this, "Fuck the first action bar is null :(", 
                           Toast.LENGTH_LONG).show();
        }
        else
        {
            Toast.makeText(this, "It works! :)", 
                           Toast.LENGTH_LONG).show();
            // actionbar.setDisplayHomeAsUpEnabled(true);
            // getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
    }

    // @Override
    // public boolean onSupportNavigateUp()
    // {  
    //     finish();  
    //     return true;  
    // }

    @Override
    protected void onStart()
    {
        super.onStart();
        ActionBar actionbar = getSupportActionBar();
        if (actionbar != null)
        {
            // actionbar.setDisplayHomeAsUpEnabled(true);
        }
        else
        {
            Toast.makeText(this, "Fuck the action bar is null :(", 
                           Toast.LENGTH_LONG).show();
            // View view = getWindow().getDecorView().getRootView();
            // Snackbar.make(view, "Replace with your own action",
            //               Snackbar.LENGTH_LONG)
            //     .setAction("Action", null).show();
        }
    }

}
