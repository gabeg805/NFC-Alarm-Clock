package com.nfcalarmclock.onboarding

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.nfcalarmclock.R

/**
 * Onboarding How-To page.
 */
class NacOnboardingHowToPageFragment
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
		return inflater.inflate(R.layout.frg_onboarding_how_to_page, container, false)
	}

}