package com.nfcalarmclock;

import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

/**
 * @brief Recycler view for all alarm cards.
 */
public class NacFloatingButton
{

    /**
     * @brief Activity.
     */
    private AppCompatActivity mActivity;

    /**
     * @brief Floating action button to add new alarms.
     */
    private FloatingActionButton mFloatingButton;

    /**
     * @brief Define the floating button view.
	 * 
	 * @param  a  The main activity.
     */
    public NacFloatingButton(AppCompatActivity a)
    {
        this.mActivity = a;
        this.mFloatingButton = (FloatingActionButton) a.findViewById(R.id.fab_add_alarm);
    }

    /**
     * @brief Initialize the floating button.
     */
    public void init()
    {
        this.mFloatingButton.setOnClickListener((MainActivity)this.mActivity);
    }

    /**
     * @brief Show the floating button.
     */
    public void show()
    {
        this.mFloatingButton.show();
    }

    /**
     * @brief Hide the floating button.
     */
    public void hide()
    {
        this.mFloatingButton.hide();
    }

    /**
     * @brief Check if floating button is shown.
     */
    public boolean isShown()
    {
        return this.mFloatingButton.isShown();
    }

}
