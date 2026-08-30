package com.nfcalarmclock.onboarding

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.MarginPageTransformer
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.button.MaterialButton
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.nfcalarmclock.BuildConfig
import com.nfcalarmclock.R
import com.nfcalarmclock.onboarding.NacOnboardingWelcomePageFragment.Companion.GET_STARTED_CLICK_REQUEST_KEY
import com.nfcalarmclock.shared.NacSharedPreferences

/**
 * Onboard the user into using the app.
 */
class NacOnboardingFragment : Fragment()
{

	/**
	 * Pager adapter.
	 */
	class NacOnboardingPagerAdapter(fragment: Fragment)
		: FragmentStateAdapter(fragment)
	{

		/**
		 * Create fragment.
		 */
		override fun createFragment(position: Int): Fragment
		{
			return when(position)
			{
				0 -> NacOnboardingWelcomePageFragment()
				1 -> NacOnboardingPermissionsPageFragment()
				2 -> NacOnboardingHowToPageFragment()
				else ->
				{
					throw IllegalArgumentException("Invalid onboarding position: $position")
				}
			}
		}

		/**
		 * Item count.
		 */
		override fun getItemCount(): Int = 3

	}

	/**
	 * View pager.
	 */
	private lateinit var viewPager: ViewPager2

	/**
	 * Fragment attached.
	 */
	override fun onAttach(context: Context)
	{
		// Super
		super.onAttach(context)

		// Get the activity
		val activity = requireActivity()

		// On back pressed
		activity.onBackPressedDispatcher.addCallback(this, object: OnBackPressedCallback(true) {
			override fun handleOnBackPressed()
			{
				// Close the app when back is pressed on the first page
				if (viewPager.currentItem == 0)
				{
					findNavController().popBackStack()
					activity.finish()
				}
				// Go to the previous page
				else
				{
					viewPager.currentItem -= 1
				}
			}

		})
	}

	/**
	 * Create view.
	 */
	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?
	): View?
	{
		return inflater.inflate(R.layout.frg_onboarding, container, false)
	}

	/**
	 * View created.
	 */
	override fun onViewCreated(view: View, savedInstanceState: Bundle?)
	{
		// Super
		super.onViewCreated(view, savedInstanceState)

		// Get views
		viewPager = view.findViewById(R.id.onboarding_viewpager)
		val tabLayout: TabLayout = view.findViewById(R.id.onboarding_tab_layout_indicator)
		val backButton: MaterialButton = view.findViewById(R.id.onboarding_back_button)
		val nextButton: MaterialButton = view.findViewById(R.id.onboarding_next_button)

		// Setup the views
		val adapter = NacOnboardingPagerAdapter(this)
		viewPager.adapter = adapter
		viewPager.setPageTransformer(MarginPageTransformer(50))

		// Link the ViewPager2 and TabLayout together
		TabLayoutMediator(tabLayout, viewPager) { _, _ ->
		}.attach()

		// Fragment result from welcome page
		childFragmentManager.setFragmentResultListener(GET_STARTED_CLICK_REQUEST_KEY, viewLifecycleOwner) { _, _ ->
			viewPager.currentItem = 1
		}

		// Page change callback
		viewPager.registerOnPageChangeCallback(object: ViewPager2.OnPageChangeCallback() {
			override fun onPageSelected(position: Int)
			{

				// Set button and tab layout visibility
				val buttonVisibility = if (position == 0) View.INVISIBLE else View.VISIBLE

				backButton.visibility = buttonVisibility
				nextButton.visibility = buttonVisibility
				tabLayout.visibility = buttonVisibility

				// Set the next button text
				if (position == adapter.itemCount - 1)
				{
					nextButton.setText(R.string.word_finish)
				}
				else
				{
					nextButton.setText(R.string.word_next)
				}
			}

		})

		// Back button click listener
		backButton.setOnClickListener {

			// Current page
			val current = viewPager.currentItem

			// Change the page
			if (current > 0)
			{
				viewPager.currentItem = current - 1
			}

		}

		// Next button click listener
		nextButton.setOnClickListener {

			// Current page
			val current = viewPager.currentItem

			// Change the page
			if (current < adapter.itemCount - 1)
			{
				viewPager.currentItem = current + 1
			}
			// Done with onboarding
			else
			{
				// Set the flag so onboarding is not shown
				val context = requireContext()
				val sharedPreferences = NacSharedPreferences(context)
				sharedPreferences.shouldShowOnboardingScreen = false

				// Set the previous app version as the current version. This way, the What's
				// New dialog does not show up
				sharedPreferences.previousAppVersion = BuildConfig.VERSION_NAME

				// Navigate to show alarms
				val navController = findNavController()

				navController.popBackStack()
				navController.navigate(R.id.action_global_nacShowAlarmsFragment)
			}

		}
	}

}