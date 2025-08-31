package com.nfcalarmclock.settings

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toDrawable
import androidx.fragment.app.FragmentManager
import androidx.navigation.fragment.NavHostFragment
import com.nfcalarmclock.R
import dagger.hilt.android.AndroidEntryPoint

/**
 * Display all the configurable settings for the app.
 */
@AndroidEntryPoint
class NacMainSettingActivity
	: AppCompatActivity(),
	FragmentManager.OnBackStackChangedListener
{

	/**
	 * Navigation host fragment.
	 */
	val navController by lazy {
		(supportFragmentManager.findFragmentById(R.id.hello_content) as NavHostFragment).navController
	}

	/**
	 * Called when the back stack is changed.
	 */
	override fun onBackStackChanged()
	{
		// Get the default title
		var title = getString(R.string.title_settings)

		// Check if there are items in the back stack
		if (supportFragmentManager.backStackEntryCount > 0)
		{
			// Get the title of the first entry in the back stack
			val entry = supportFragmentManager.getBackStackEntryAt(0)
			title = entry.name.toString()
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

		// Set the content view
		setContentView(R.layout.act_setting)

		// Add the main setting fragment to the activity
		supportFragmentManager.beginTransaction()
			.replace(android.R.id.content, NacMainSettingFragment())
			.commit()

		// Set a back stack changed listener
		supportFragmentManager.addOnBackStackChangedListener(this)

		// Set the color of the action bar
		val black = ContextCompat.getColor(this, R.color.black)
		val actionBarColor = black.toDrawable()

		supportActionBar?.setBackgroundDrawable(actionBarColor)
	}

	/**
	 * Called when when the activity is navigated up
	 */
	override fun onSupportNavigateUp(): Boolean
	{
		// There are still items in the back stack. Keep popping them
		return if (supportFragmentManager.backStackEntryCount > 0)
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