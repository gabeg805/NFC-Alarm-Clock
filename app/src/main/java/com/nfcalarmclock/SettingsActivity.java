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

public class SettingsActivity
    extends AppCompatActivity
{

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
    }

    // @Override
    // public boolean onSupportNavigateUp()
    // {  
    //     finish();  
    //     return true;  
    // }

}
