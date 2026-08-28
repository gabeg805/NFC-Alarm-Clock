package com.nfcalarmclock.onboarding

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.google.android.material.button.MaterialButton
import com.nfcalarmclock.R
import com.nfcalarmclock.system.permission.postnotifications.NacPostNotificationsPermission
import com.nfcalarmclock.system.permission.scheduleexactalarm.NacScheduleExactAlarmPermission
import com.nfcalarmclock.system.permission.systemalertwindow.NacSystemAlertWindowPermission

/**
 * Onboarding permissions page.
 */
class NacOnboardingPermissionsPageFragment
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
		return inflater.inflate(R.layout.frg_onboarding_permissions_page, container, false)
	}

	/**
	 * View created.
	 */
	override fun onViewCreated(view: View, savedInstanceState: Bundle?)
	{
		// Super
		super.onViewCreated(view, savedInstanceState)

		// Show notification views and visibility
		val showNotificationsTitle: TextView = view.findViewById(R.id.onboarding_notification_title)
		val showNotificationsDescription: TextView = view.findViewById(R.id.onboarding_notification_description)
		val showNotificationsAllowButton: MaterialButton = view.findViewById(R.id.onboarding_notification_allow_button)
		val showNotificationsVisibility = if (NacPostNotificationsPermission.isCorrectAndroidVersion)
		{
			View.VISIBLE
		}
		else
		{
			View.GONE
		}

		// Schedule exact alarm views and visibilty
		val scheduleExactAlarmsTitle: TextView = view.findViewById(R.id.onboarding_schedule_exact_alarm_title)
		val scheduleExactAlarmsDescription: TextView = view.findViewById(R.id.onboarding_schedule_exact_alarm_description)
		val scheduleExactAlarmsAllowButton: MaterialButton = view.findViewById(R.id.onboarding_schedule_exact_alarm_allow_button)
		val scheduleExactAlarmsVisibility = if (NacScheduleExactAlarmPermission.isCorrectAndroidVersion)
		{
			View.VISIBLE
		}
		else
		{
			View.GONE
		}

		// System alert (display over other apps) views and visibilty
		val systemAlertTitle: TextView = view.findViewById(R.id.onboarding_system_alert_title)
		val systemAlertDescription: TextView = view.findViewById(R.id.onboarding_system_alert_description)
		val systemAlertAllowButton: MaterialButton = view.findViewById(R.id.onboarding_system_alert_allow_button)
		val systemAlertVisibility = if (NacSystemAlertWindowPermission.isCorrectAndroidVersion)
		{
			View.VISIBLE
		}
		else
		{
			View.GONE
		}

		// Set the visibility of everything
		showNotificationsTitle.visibility = showNotificationsVisibility
		showNotificationsDescription.visibility = showNotificationsVisibility
		showNotificationsAllowButton.visibility = showNotificationsVisibility
		scheduleExactAlarmsTitle.visibility = scheduleExactAlarmsVisibility
		scheduleExactAlarmsDescription.visibility = scheduleExactAlarmsVisibility
		scheduleExactAlarmsAllowButton.visibility = scheduleExactAlarmsVisibility
		systemAlertTitle.visibility = systemAlertVisibility
		systemAlertDescription.visibility = systemAlertVisibility
		systemAlertAllowButton.visibility = systemAlertVisibility

		// Get the activity to request permissions
		val activity = requireActivity()

		// Set the allow button on click listeners
		showNotificationsAllowButton.setOnClickListener {
			println("Request notification permission")
			NacPostNotificationsPermission.requestPermission(activity, 0)
		}

		scheduleExactAlarmsAllowButton.setOnClickListener {
			println("Request schedule exact alarm permission")
			NacScheduleExactAlarmPermission.requestPermission(activity)
		}

		systemAlertAllowButton.setOnClickListener {
			println("System alert window permission")
			NacSystemAlertWindowPermission.requestPermission(activity)
		}
	}

}