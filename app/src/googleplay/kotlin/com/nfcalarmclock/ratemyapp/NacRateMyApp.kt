package com.nfcalarmclock.ratemyapp

import androidx.appcompat.app.AppCompatActivity
import com.google.android.play.core.review.ReviewInfo
import com.google.android.play.core.review.ReviewManagerFactory
import com.google.android.gms.tasks.Task
import com.nfcalarmclock.R
import com.nfcalarmclock.shared.NacSharedPreferences

/**
 * Handle when to prompt the user to rate my app.
 */
object NacRateMyApp
{

	/**
	 * Check if the rate my app Google Play flow should be shown or not.
	 *
	 * Note: If the flow should not be shown and the user has never been shown
	 *       the flow before, then the rate my app counter will be incremented.
	 *
	 * @return True if the rate my app Google Play flow should be shown, and
	 *         False otherwise.
	 */
	fun shouldRequest(shared: NacSharedPreferences): Boolean
	{
		// App is already rated
		return if (shared.isRateMyAppRated)
		{
			false
		}
		// Reached the counter limit so it is time to ask again
		else if (shared.isRateMyAppLimit)
		{
			true
		}
		// Increment the counter
		else
		{
			shared.rateMyAppCounter = shared.rateMyAppCounter + 1
			false
		}
	}

	/**
	 * Request to rate my app.
	 *
	 * This will NOT check if the review flow should be launched or not. It
	 * will simply launch it.
	 */
	fun request(activity:AppCompatActivity, shared: NacSharedPreferences)
	{
		// Create a review manager
		val manager = ReviewManagerFactory.create(activity)

		// Retrieves all the needed information to launch the review flow
		val request = manager.requestReviewFlow()

		// Launch the review flow
		request.addOnCompleteListener { task: Task<ReviewInfo> ->

			// Task was not successful
			if (!task.isSuccessful)
			{
				return@addOnCompleteListener
			}

			// Launch the review flow
			val flow = manager.launchReviewFlow(activity, task.result)

			// The flow has finished. The API does not indicate whether the user
			// reviewed or not, or even whether the review dialog was shown.
			//
			// No matter the result, just treat it as rated.
			flow.addOnCompleteListener { _ ->

				// Get the value indicating that it has been rated
				val rated = shared.resources.getInteger(R.integer.default_rate_my_app_rated)

				// Set the rated counter
				shared.rateMyAppCounter = rated

			}

		}
	}

}
