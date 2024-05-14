package com.nfcalarmclock.view.dialog

import android.os.Bundle
import android.view.View
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.button.MaterialButton
import com.nfcalarmclock.shared.NacSharedPreferences

/**
 * Generic bottom sheet dialog fragment.
 */
abstract class NacBottomSheetDialogFragment
	: BottomSheetDialogFragment()
{

	/**
	 * Shared preferences.
	 */
	protected lateinit var sharedPreferences: NacSharedPreferences

	/**
	 * Primary button.
	 */
	protected lateinit var primaryButton: MaterialButton

	/**
	 * Called when the fragment is resumed.
	 */
	override fun onResume()
	{
		// Super
		super.onResume()

		// Setup the primary button
		setupPrimaryButton()
	}

	/**
	 * Called when the dialog view is created.
	 */
	override fun onViewCreated(view: View, savedInstanceState: Bundle?)
	{
		// Super
		super.onViewCreated(view, savedInstanceState)

		// Setup the shared preferences
		setupSharedPreferences()
	}

	/**
	 * Setup the primary button.
	 */
	protected fun setupPrimaryButton()
	{
		primaryButton.setBackgroundColor(sharedPreferences.themeColor)
	}

	/**
	 * Setup the shared preferences.
	 */
	protected fun setupSharedPreferences()
	{
		sharedPreferences = NacSharedPreferences(requireContext())
	}

}