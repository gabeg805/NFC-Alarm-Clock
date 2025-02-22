package com.nfcalarmclock.card

import android.content.Context
import android.util.AttributeSet
import androidx.preference.Preference
import androidx.preference.PreferenceViewHolder
import com.nfcalarmclock.R
import com.nfcalarmclock.alarm.db.NacAlarm
import com.nfcalarmclock.alarm.options.nfc.db.NacNfcTag
import com.nfcalarmclock.shared.NacSharedPreferences
import com.nfcalarmclock.util.NacCalendar
import com.nfcalarmclock.view.calcAlpha

/**
 * An alarm card to use in a preference.
 */
class NacCardPreference @JvmOverloads constructor(

	/**
	 * Context.
	 */
	context: Context,

	/**
	 * Attribute set.
	 */
	attrs: AttributeSet? = null,

	/**
	 * Default style.
	 */
	style: Int = 0

	// Constructor
) : Preference(context, attrs, style)
{

	/**
	 * Listener for when the alarm options button is clicked.
	 */
	fun interface OnCardAlarmOptionsClickedListener
	{
		fun onCardAlarmOptionsClicked(alarm: NacAlarm)
	}

	/**
	 * Listener for when the dismiss options button is clicked.
	 */
	fun interface OnCardDismissOptionsClickedListener
	{
		fun onCardDismissOptionsClicked(alarm: NacAlarm)
	}

	/**
	 * Listener for when the media button is clicked.
	 */
	fun interface OnCardMediaClickedListener
	{
		fun onCardMediaClicked(alarm: NacAlarm)
	}

	/**
	 * Listener for when the name button is clicked.
	 */
	fun interface OnCardNameClickedListener
	{
		fun onCardNameClicked(alarm: NacAlarm)
	}

	/**
	 * Listener for when the snooze options button is clicked.
	 */
	fun interface OnCardSnoozeOptionsClickedListener
	{
		fun onCardSnoozeOptionsClicked(alarm: NacAlarm)
	}

	/**
	 * List of NFC tags.
	 */
	var allNfcTags: List<NacNfcTag> = emptyList()

	/**
	 * Listener for when the alarm options button is clicked.
	 */
	var onCardAlarmOptionsClickedListener: OnCardAlarmOptionsClickedListener? = null

	/**
	 * Listener for when the dismiss options button is clicked.
	 */
	var onCardDismissOptionsClickedListener: OnCardDismissOptionsClickedListener? = null

	/**
	 * Listener for when the media button is clicked.
	 */
	var onCardMediaClickedListener: OnCardMediaClickedListener? = null

	/**
	 * Listener for when the name button is clicked.
	 */
	var onCardNameClickedListener: OnCardNameClickedListener? = null

	/**
	 * Listener for when the snooze options button is clicked.
	 */
	var onCardSnoozeOptionsClickedListener: OnCardSnoozeOptionsClickedListener? = null

	/**
	 * Alarm card.
	 */
	lateinit var card: NacCardHolder

	// Constructor
	init
	{
		layoutResource = R.layout.nac_preference_alarm_card
	}

	/**
	 * Called when binding the view.
	 *
	 * Note: No need to do Time and Switch as these are not modifiable here.
	 */
	override fun onBindViewHolder(holder: PreferenceViewHolder)
	{
		super.onBindViewHolder(holder)

		// Create the alarm card holder object
		val sharedPreferences = NacSharedPreferences(context)
		val alarm = NacAlarm.build(sharedPreferences)
		card = NacCardHolder(holder.itemView)

		// Bind the alarm to the card and expand the card
		card.bind(alarm)
		card.doExpandWithColor()

		// Do not allow changing the time and is enabled switch
		card.timeParentView.isEnabled = false
		card.timeParentView.alpha = calcAlpha(false)
		card.switch.isEnabled = false
		card.switch.alpha = calcAlpha(false)

		// Days
		card.onCardDaysChangedListener = NacCardHolder.OnCardDaysChangedListener { _, a ->
			sharedPreferences.days = NacCalendar.Day.daysToValue(a.days)
		}

		// Repeat
		card.onCardUseRepeatChangedListener = NacCardHolder.OnCardUseRepeatChangedListener { _, a ->
			sharedPreferences.shouldRepeat = a.shouldRepeat
			card.toastRepeat(context)
		}

		// Vibrate
		card.onCardUseVibrateChangedListener = NacCardHolder.OnCardUseVibrateChangedListener { _, a ->
			sharedPreferences.shouldVibrate = a.shouldVibrate
			card.toastVibrate(context)
		}

		// NFC
		card.onCardUseNfcChangedListener = NacCardHolder.OnCardUseNfcChangedListener { _, a ->
			sharedPreferences.shouldUseNfc = a.shouldUseNfc
			card.toastNfc(context, allNfcTags)
		}

		// Flashlight
		card.onCardUseFlashlightChangedListener = NacCardHolder.OnCardUseFlashlightChangedListener { _, a ->
			sharedPreferences.shouldUseFlashlight = a.shouldUseFlashlight
			card.toastFlashlight(context)
		}

		// Media
		card.onCardMediaClickedListener = NacCardHolder.OnCardMediaClickedListener { _, a ->
			onCardMediaClickedListener?.onCardMediaClicked(a)
		}

		// Volume
		card.onCardVolumeChangedListener = NacCardHolder.OnCardVolumeChangedListener { _, a ->
			sharedPreferences.volume = a.volume
		}

		// Name
		card.onCardNameClickedListener = NacCardHolder.OnCardNameClickedListener { _, a ->
			onCardNameClickedListener?.onCardNameClicked(a)
		}

		// Dismiss options
		card.onCardDismissOptionsClickedListener = NacCardHolder.OnCardDismissOptionsClickedListener { _, a ->
			onCardDismissOptionsClickedListener?.onCardDismissOptionsClicked(a)
		}

		// Snooze options
		card.onCardSnoozeOptionsClickedListener = NacCardHolder.OnCardSnoozeOptionsClickedListener { _, a ->
			onCardSnoozeOptionsClickedListener?.onCardSnoozeOptionsClicked(a)
		}

		// Alarm options
		card.onCardAlarmOptionsClickedListener = NacCardHolder.OnCardAlarmOptionsClickedListener { _, a ->
			onCardAlarmOptionsClickedListener?.onCardAlarmOptionsClicked(a)
		}

	}
}