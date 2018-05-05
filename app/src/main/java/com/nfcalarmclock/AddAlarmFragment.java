package com.nfcalarmclock;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.support.v4.app.Fragment;

public class AddAlarmFragment
    extends Fragment
{

    public static AddAlarmFragment newInstance()
    {
        return new AddAlarmFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        return inflater.inflate(R.layout.fragment_new_alarm, container, false);
    }
}
