package com.nfcalarmclock.ratemyapp;

import androidx.appcompat.app.AppCompatActivity;
import com.google.android.play.core.review.ReviewInfo;
import com.google.android.play.core.review.ReviewManager;
import com.google.android.play.core.review.ReviewManagerFactory;
import com.google.android.play.core.tasks.Task;

import com.nfcalarmclock.shared.NacSharedPreferences;

/**
 * Handle when to prompt the user to rate my app.
 */
public final class NacRateMyApp
{

	/**
	 */
	private NacRateMyApp()
	{
	}

	/**
	 * Request to rate my app.
	 *
	 * If the app is already rated, or the counter has not reached the threshold
	 * to launch the request, then nothing is shown to the user.
	 */
	public static void request(NacSharedPreferences shared)
	{
		// App is already rated
		if (shared.isRateMyAppRated())
		{
			return;
		}

		// Time to show the Google rating dialog
		else if (shared.isRateMyAppLimit())
		{
			AppCompatActivity activity = (AppCompatActivity) shared.getContext();
			ReviewManager manager = ReviewManagerFactory.create(activity);
			Task<ReviewInfo> request = manager.requestReviewFlow();

			// Launch the review flow
			request.addOnCompleteListener(task -> {
				if (task.isSuccessful())
				{
					ReviewInfo reviewInfo = task.getResult();
					Task<Void> flow = manager.launchReviewFlow(activity, reviewInfo);

					// The flow has finished. The API does not indicate whether the user
					// reviewed or not, or even whether the review dialog was shown.
					// 
					// No matter the result, just treat it as rated.
					flow.addOnCompleteListener(ignored -> {
						shared.ratedRateMyApp();
					});
				}
			});

			//NacRateMyAppDialog dialog = new NacRateMyAppDialog();
			//dialog.build(this);
			//dialog.show();
		}

		// Increment the counter until it is time to show the Google rating dialog
		else
		{
			shared.incrementRateMyApp();
		}
	}

}
