package com.nfcalarmclock.system

import android.os.Bundle
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import com.nfcalarmclock.view.dialog.NacBottomSheetDialogFragment

/**
 * Observe a back stack entry until it is cleaned up.
 */
fun NavController.navigate(
	navId: Int,
	args: Bundle?,
	fragment: NacBottomSheetDialogFragment,
	onBackStackPopulated: () -> Unit = {
		fragment.dismiss()
	}
)
{
	try
	{
		// Navigate to the fragment
		navigate(navId, args)

		// Observe the back stack entry
		observeBackStackEntry(this, fragment, onBackStackPopulated = onBackStackPopulated)
	}
	catch (_: IllegalStateException)
	{
	}
}

/**
 * Create back stack entry lifecycle observer.
 */
fun createBackStackEntryLifecycleObserver(
	navController: NavController,
	fragment: NacBottomSheetDialogFragment,
	onBackStackPopulated: () -> Unit = {
		fragment.dismiss()
	}
): LifecycleEventObserver
{
	return LifecycleEventObserver { _, event ->

		// Check if the destination was started
		if (event == Lifecycle.Event.ON_START)
		{
			// Hide the dialog
			fragment.dialog?.hide()
		}
		// Check if the destination was stopped
		else if (event == Lifecycle.Event.ON_STOP)
		{
			// Check if the current back stack entry contains the updated alarm
			if (navController.currentBackStackEntry?.savedStateHandle?.contains("YOYOYO") == true)
			{
				// Call the listener
				onBackStackPopulated()
			}
			// The ok button was not clicked, so proceed as normal
			else
			{
				// Show the dialog
				fragment.dialog?.show()

			}
		}

	}
}

/**
 * Observe a back stack entry until it is cleaned up.
 */
fun observeBackStackEntry(
	navController: NavController,
	fragment: NacBottomSheetDialogFragment,
	onBackStackPopulated: () -> Unit = {
		fragment.dismiss()
	}
)
{
	// Get the back stack entry
	val navBackStackEntry = navController.currentBackStackEntry ?: return

	// Create an observer for any changes to the lifecycle of the back stack entry
	val observer = createBackStackEntryLifecycleObserver(
		navController, fragment, onBackStackPopulated = onBackStackPopulated)

	// Observe any changes to the lifecycle of the back stack entry
	navBackStackEntry.lifecycle.addObserver(observer)

	// Observe the lifecycle of the current fragment
	waitToCleanupBackStackEntryLifecycleObserver(
		fragment.viewLifecycleOwner, navBackStackEntry, observer)
}

/**
 * Wait to cleanup the back stack entry lifecycle observer.
 */
fun waitToCleanupBackStackEntryLifecycleObserver(
	lifecycleOwner: LifecycleOwner,
	navBackStackEntry: NavBackStackEntry,
	lifecycleEventObserver: LifecycleEventObserver
)
{
	lifecycleOwner.lifecycle.addObserver(LifecycleEventObserver { _, event ->

		// Check if the fragment is going to be destroyed
		if (event == Lifecycle.Event.ON_DESTROY)
		{
			// Remove the observer to the back stack entry
			navBackStackEntry.lifecycle.removeObserver(lifecycleEventObserver)
		}

	})
}