package com.nfcalarmclock.onboarding

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResult
import com.google.android.material.button.MaterialButton
import com.nfcalarmclock.R

/**
 * Onboarding welcome page.
 */
class NacOnboardingWelcomePageFragment
	: Fragment()
{

	/**
	 * Create view.
	 */
	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?
	): View?
	{
		return inflater.inflate(R.layout.frg_onboarding_welcome_page, container, false)
	}

	/**
	 * View created.
	 */
	override fun onViewCreated(view: View, savedInstanceState: Bundle?)
	{
		// Super
		super.onViewCreated(view, savedInstanceState)

		// Get the view
		val getStartedButton: MaterialButton = view.findViewById(R.id.onboarding_get_started_button)

		// Set the on click listener
		getStartedButton.setOnClickListener {

			// The bundle is not really used, but whatever
			val bundle = Bundle().apply {
				putBoolean("result", true)
			}

			// Set the result
			setFragmentResult(GET_STARTED_CLICK_REQUEST_KEY, bundle)

		}
	}

	companion object
	{

		/**
		 * Get started click request key for the fragment result.
		 */
		const val GET_STARTED_CLICK_REQUEST_KEY = "GET_STARTED_REQUEST_KEY"

	}

}