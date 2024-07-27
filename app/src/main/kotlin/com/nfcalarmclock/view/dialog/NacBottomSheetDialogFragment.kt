package com.nfcalarmclock.view.dialog

import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.button.MaterialButton
import com.nfcalarmclock.R
import com.nfcalarmclock.shared.NacSharedPreferences
import com.nfcalarmclock.view.setupRippleColor

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
	protected fun setupPrimaryButton(button: MaterialButton, listener: () -> Unit = { })
	{
		// Setup the color
		button.setBackgroundColor(sharedPreferences.themeColor)
		button.setupRippleColor(sharedPreferences)

		// Set the listener
		button.setOnClickListener {
			listener()
		}
	}

	/**
	 * Setup the secondary button.
	 */
	protected fun setupSecondaryButton(button: MaterialButton, listener: () -> Unit = { dismiss() })
	{
		// Setup the color
		button.setupRippleColor(sharedPreferences,
			themeColor = ContextCompat.getColor(requireContext(), R.color.gray_light))

		// Set the listener
		button.setOnClickListener {
			listener()
		}
	}

	/**
	 * Setup the shared preferences.
	 */
	protected fun setupSharedPreferences()
	{
		sharedPreferences = NacSharedPreferences(requireContext())
	}

}