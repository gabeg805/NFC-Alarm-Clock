package com.nfcalarmclock;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.support.v4.app.Fragment;

import android.support.v7.app.AppCompatActivity;

import android.support.v7.app.ActionBar;

public class AddAlarmActivity
    extends AppCompatActivity
{

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_alarm);
        // ActionBar actionbar = getSupportActionBar();
        // if (actionbar != null)
        // {
        //     actionbar.setDisplayHomeAsUpEnabled(true);
        // }
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        // getSupportActionBar().setDisplayShowHomeEnabled(true);
    }

    @Override
    public boolean onSupportNavigateUp()
    {  
        finish();  
        return true;  
    }

}
