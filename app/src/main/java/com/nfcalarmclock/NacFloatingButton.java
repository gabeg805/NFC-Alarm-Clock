package com.nfcalarmclock;

import android.content.Context;
import android.content.res.ColorStateList;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

/**
 * Recycler view for all alarm cards.
 */
public class NacFloatingButton
{

	/**
	 * Activity.
	 */
	private AppCompatActivity mActivity;

	/**
	 * Floating action button to add new alarms.
	 */
	private FloatingActionButton mFloatingButton;

	/**
	 */
	public NacFloatingButton(AppCompatActivity a)
	{
		this.mActivity = a;
		this.mFloatingButton = (FloatingActionButton)
			a.findViewById(R.id.fab_add_alarm);
	}

	/**
	 * Initialize the floating button.
	 */
	public void init()
	{
		Context context = this.mFloatingButton.getContext();
		NacSharedPreferences shared = new NacSharedPreferences(context);

		this.mFloatingButton.setBackgroundTintList(ColorStateList.valueOf(
			shared.themeColor));
		this.mFloatingButton.setOnClickListener(
			(NacMainActivity)this.mActivity);
	}

	/**
	 * Show the floating button.
	 */
	public void show()
	{
		this.mFloatingButton.show();
	}

	/**
	 * Hide the floating button.
	 */
	public void hide()
	{
		this.mFloatingButton.hide();
	}

	/**
	 * Check if floating button is shown.
	 */
	public boolean isShown()
	{
		return this.mFloatingButton.isShown();
	}

}
