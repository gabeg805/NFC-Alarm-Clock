package com.nfcalarmclock.settings

import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toDrawable
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import com.nfcalarmclock.R
import dagger.hilt.android.AndroidEntryPoint

/**
 * Display all the configurable settings for the app.
 */
@AndroidEntryPoint
class NacMainSettingActivity
	: AppCompatActivity()
{

	/**
	 * Navigation host fragment.
	 */
	val navController by lazy {
		(supportFragmentManager.findFragmentById(R.id.hello_content) as NavHostFragment).navController
	}

	/**
	 * App bar configuration.
	 */
	private lateinit var appBarConfiguration: AppBarConfiguration

	/**
	 * RecyclerView top margin.
	 */
	//var rvTopMargin: Int = 0

	/**
	 * Activity is created.
	 */
	override fun onCreate(savedInstanceState: Bundle?)
	{
		// Super
		super.onCreate(savedInstanceState)

		// Set the content view
		setContentView(R.layout.act_setting)

		// Set the color of the action bar
		val black = ContextCompat.getColor(this, R.color.black)
		val actionBarColor = black.toDrawable()

		supportActionBar?.setBackgroundDrawable(actionBarColor)

		// Setup the action bar
		appBarConfiguration = AppBarConfiguration(navController.graph)
		setupActionBarWithNavController(navController, appBarConfiguration)

		// Setup edge to edge
		setupEdgeToEdge()
	}

	/**
	 * Activity is navigated up
	 */
	override fun onSupportNavigateUp(): Boolean
	{
		return navController.navigateUp(appBarConfiguration)
			|| super.onSupportNavigateUp()
	}

	/**
	 * Setup any views that need changing due to API 35+ edge-to-edge.
	 */
	private fun setupEdgeToEdge()
	{
		// Check if API < 35, then edge-to-edge is not enforced and do not need to do
		// anything
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.VANILLA_ICE_CREAM)
		{
			return
		}

		// TODO: Can maybe customize this more when going up to API 36, but for now opting out
		//// Set edge to edge color of top status bar
		//findViewById<ProtectionLayout>(R.id.protection_layout)
		//	.setProtections(
		//		listOf(
		//			ColorProtection(WindowInsetsCompat.Side.TOP, Color.BLACK)
		//		)
		//	)
	}

}