package com.nfcalarmclock.settings

import android.R
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentManager
import com.nfcalarmclock.shared.NacSharedConstants

/**
 * Display all the configurable settings for the app.
 */
class NacMainSettingActivity
	: AppCompatActivity(),
	FragmentManager.OnBackStackChangedListener
{

	/**
	 * The back stack count.
	 */
	private val stackCount: Int
		get() = supportFragmentManager.backStackEntryCount

	/**
	 * Called when the back stack is changed.
	 */
	override fun onBackStackChanged()
	{
		// Get the default title
		val cons = NacSharedConstants(this)
		var title = cons.settings

		// Check if there are items in the back stack
		if (stackCount > 0)
		{
			// Get the title of the first entry in the back stack
			val entry = supportFragmentManager.getBackStackEntryAt(0)
			title = entry.name
		}

		// Set the title of the activity
		setTitle(title)
	}

	/**
	 * Called when the activity is created.
	 */
	override fun onCreate(savedInstanceState: Bundle?)
	{
		// Super
		super.onCreate(savedInstanceState)

		// Add the main setting fragment to the activity
		supportFragmentManager.beginTransaction()
			.replace(R.id.content, NacMainSettingFragment())
			.commit()

		// Set a back stack changed listener
		supportFragmentManager.addOnBackStackChangedListener(this)
	}

	/**
	 * Called when when the activity is navigated up
	 */
	override fun onSupportNavigateUp(): Boolean
	{
		// There are still items in the back stack. Keep popping them
		return if (stackCount > 0)
			{
				supportFragmentManager.popBackStack()
				false
			}
			// No more items in the back stack. Finish the activity
			else
			{
				finish()
				true
			}
	}

}