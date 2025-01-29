package com.nfcalarmclock.view.dialog

import android.os.Bundle
import android.os.Handler
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.updateLayoutParams
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.button.MaterialButton
import com.nfcalarmclock.R
import com.nfcalarmclock.shared.NacSharedPreferences
import com.nfcalarmclock.view.setupThemeColor

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
	 * Set the state of the bottom sheet dialog.
	 */
	@Suppress("SameParameterValue")
	protected fun setBehaviorState(behavior: Int)
	{
		(dialog as BottomSheetDialog).behavior.state = behavior
	}

	/**
	 * Setup the primary button.
	 */
	protected fun setupPrimaryButton(button: MaterialButton, listener: () -> Unit = { })
	{
		// Setup the color
		button.setupThemeColor(sharedPreferences)

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
		val color = ContextCompat.getColor(requireContext(), R.color.gray_light)
		button.setupThemeColor(sharedPreferences, themeColor=color)

		// Set the listener
		button.setOnClickListener {
			listener()
		}
	}

	/**
	 * Setup the height of a scrollable view.
	 *
	 * @param viewGroup The scrollable view's height to change.
	 * @param maxHeightPercent Percentage of the max screen height that the scrollable
	 *  view should take up. This should be a value from 0-100.
	 * @param nbuttons The number of buttons that are in the dialog, to take into account
	 *  any extra space that should NOT be used by the scrollable view.
	 */
	open fun setupScrollableViewHeight(
		viewGroup: ViewGroup,
		maxHeightPercent: Int,
		nbuttons: Int = 1
	)
	{
		// Compute the amount of space (in pixels) is needed for the buttons
		val touchDimen = resources.getDimension(R.dimen.touch)
		val marginsDimen = resources.getDimension(R.dimen.medium) + resources.getDimension(R.dimen.large)
		val buttonsHeight = nbuttons*touchDimen.toInt() + marginsDimen.toInt()

		// Get the max height that the scroll view can take up
		val screenHeight = requireContext().resources.displayMetrics.heightPixels
		val maxScrollHeight = screenHeight * maxHeightPercent / 100 - buttonsHeight
		val handler = Handler(requireContext().mainLooper)

		// Layout listener
		viewGroup.addOnLayoutChangeListener { v, _, _, _, _, _, _, _, _ ->

			// Set the height of the scroll view if it exceeds the max
			if (v.height > maxScrollHeight)
			{
				// Update the height in a handler so that it executes outside of the
				// layout change listener
				handler.post {
					viewGroup.updateLayoutParams {
						height = maxScrollHeight
					}
				}
			}

		}
	}

	/**
	 * Setup the shared preferences.
	 */
	private fun setupSharedPreferences()
	{
		sharedPreferences = NacSharedPreferences(requireContext())
	}

}