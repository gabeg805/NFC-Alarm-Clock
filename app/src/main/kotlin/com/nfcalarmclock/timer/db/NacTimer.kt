package com.nfcalarmclock.timer.db

import android.os.Parcel
import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import com.nfcalarmclock.alarm.db.NacAlarm
import com.nfcalarmclock.shared.NacSharedPreferences
import com.nfcalarmclock.system.removeToday
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

// TODO: Fragments:
//       * List of timers
// TODO: Compare by duration?
// TODO: When calculating size of numpads: screen size - (padding normal + height of timer_hour + height of timer_start (make sure heights include margin and if not, add them)) / 4
/**
 * Timer.
 *
 * Note: Class declaration has "()" with NacTimer because of "supertype initialization
 *       is impossible without primary constructor" error that was happening without it.
 */
@Entity(tableName = "timer",
	ignoredColumns = [
		"time_active", "snooze_count", "is_enabled", "hour", "minute", "days", "date",
		"repeat_frequency_days_to_run_before_starting", "can_dismiss_early",
		"dismiss_early_time", "should_show_dismiss_early_notification",
		"time_of_dismiss_early_alarm",
		"should_auto_snooze", "auto_snooze_time", "max_snooze", "snooze_duration",
		"should_easy_snooze", "should_volume_snooze", "should_show_reminder",
		"time_to_show_reminder", "reminder_frequency", "should_use_tts_for_reminder",
		"should_skip_next_alarm"
	])
class NacTimer()
	: NacAlarm()
{

	/**
	 * Duration of the timer, in seconds.
	 */
	@ColumnInfo(name = "duration", defaultValue = "0")
	var duration: Long = 0

	/**
	 * ID(s) of the NFC tag(s) that can be used to start the timer.
	 */
	@ColumnInfo(name = "scan_nfc_tag_id_to_start", defaultValue = "")
	var scanNfcTagIdToStart: String = ""

	/**
	 * Whether stopping with a volume button is allowed or not.
	 */
	@ColumnInfo(name = "should_volume_stop", defaultValue = "0")
	var shouldVolumeStop: Boolean = false

	/**
	 * ID(s) of the NFC tag(s) that can be used to start the timer.
	 */
	val scanNfcTagIdToStartList: List<String>
		get()
		{
			// Create the regex
			val regex = Regex(" \\|\\| ")

			return if (scanNfcTagIdToStart.isEmpty())
			{
				// No NFC ID
				emptyList()
			}
			else
			{
				// Try to split the NFC IDs
				scanNfcTagIdToStart.split(regex)
			}
		}

	/**
	 * Populate values with input parcel.
	 */
	private constructor(input: Parcel) : this()
	{
		// ID
		id = input.readLong()

		// Active timer
		isActive = input.readInt() != 0
		duration = input.readLong()

		// Repeat
		shouldRepeat = input.readInt() != 0
		repeatFrequency = input.readInt()
		repeatFrequencyUnits = input.readInt()

		// Vibrate
		shouldVibrate = input.readInt() != 0
		vibrateDuration = input.readLong()
		vibrateWaitTime = input.readLong()
		shouldVibratePattern = input.readInt() != 0
		vibrateRepeatPattern= input.readInt()
		vibrateWaitTimeAfterPattern= input.readLong()

		// NFC
		shouldUseNfc = input.readInt() != 0
		nfcTagId = input.readString() ?: ""
		nfcTagDismissOrder = input.readInt()
		scanNfcTagIdToStart = input.readString() ?: ""

		// Flashlight
		shouldUseFlashlight = input.readInt() != 0
		flashlightStrengthLevel = input.readInt()
		graduallyIncreaseFlashlightStrengthLevelWaitTime = input.readInt()
		shouldBlinkFlashlight = input.readInt() != 0
		flashlightOnDuration = input.readString() ?: ""
		flashlightOffDuration = input.readString() ?: ""

		// Media
		mediaPath = input.readString() ?: ""
		mediaArtist = input.readString() ?: ""
		mediaTitle = input.readString() ?: ""
		mediaType = input.readInt()
		localMediaPath = input.readString() ?: ""
		shouldShuffleMedia = input.readInt() != 0
		shouldRecursivelyPlayMedia = input.readInt() != 0

		// Volume and audio source
		volume = input.readInt()
		shouldGraduallyIncreaseVolume = input.readInt() != 0
		graduallyIncreaseVolumeWaitTime = input.readInt()
		shouldRestrictVolume = input.readInt() != 0
		audioSource = input.readString() ?: ""

		// Name
		name = input.readString() ?: ""

		// Text-to-speech
		shouldSayCurrentTime = input.readInt() != 0
		shouldSayName = input.readInt() != 0
		ttsFrequency = input.readInt()
		ttsSpeechRate = input.readFloat()
		ttsVoice = input.readString() ?: ""

		// Dismiss/stop
		shouldAutoDismiss = input.readInt() != 0
		autoDismissTime = input.readInt()
		shouldVolumeStop = input.readInt() != 0
	}

	/**
	 * Check if this alarm equals another alarm.
	 *
	 * @param timer An alarm.
	 *
	 * @return True if both alarms are the same, and false otherwise.
	 */
	@Suppress("CovariantEquals")
	fun equals(timer: NacTimer?): Boolean
	{
		return (timer != null)
				&& (this.equalsId(timer))
				&& (isActive == timer.isActive)
				&& (localMediaPath == timer.localMediaPath)
				&& fuzzyEquals(timer)
	}

	/**
	 * Fuzzy equals to compare most of the important timer attributes, but not all.
	 */
	fun fuzzyEquals(timer: NacTimer): Boolean
	{
		return (duration == timer.duration)
			&& (shouldRepeat == timer.shouldRepeat)
			&& (repeatFrequency == timer.repeatFrequency)
			&& (repeatFrequencyUnits == timer.repeatFrequencyUnits)
			&& (repeatFrequencyDaysToRunBeforeStarting == timer.repeatFrequencyDaysToRunBeforeStarting)
			&& (shouldVibrate == timer.shouldVibrate)
			&& (vibrateDuration == timer.vibrateDuration)
			&& (vibrateWaitTime == timer.vibrateWaitTime)
			&& (shouldVibratePattern == timer.shouldVibratePattern)
			&& (vibrateRepeatPattern == timer.vibrateRepeatPattern)
			&& (vibrateWaitTimeAfterPattern == timer.vibrateWaitTimeAfterPattern)
			&& (shouldUseNfc == timer.shouldUseNfc)
			&& (nfcTagId == timer.nfcTagId)
			&& (nfcTagDismissOrder == timer.nfcTagDismissOrder)
			&& (shouldUseFlashlight == timer.shouldUseFlashlight)
			&& (flashlightStrengthLevel == timer.flashlightStrengthLevel)
			&& (graduallyIncreaseFlashlightStrengthLevelWaitTime == timer.graduallyIncreaseFlashlightStrengthLevelWaitTime)
			&& (shouldBlinkFlashlight == timer.shouldBlinkFlashlight)
			&& (flashlightOnDuration == timer.flashlightOnDuration)
			&& (flashlightOffDuration == timer.flashlightOffDuration)
			&& (mediaPath == timer.mediaPath)
			&& (mediaArtist == timer.mediaArtist)
			&& (mediaTitle == timer.mediaTitle)
			&& (mediaType == timer.mediaType)
			&& (shouldShuffleMedia == timer.shouldShuffleMedia)
			&& (shouldRecursivelyPlayMedia == timer.shouldRecursivelyPlayMedia)
			&& (volume == timer.volume)
			&& (audioSource == timer.audioSource)
			&& (name == timer.name)
			&& (shouldSayCurrentTime == timer.shouldSayCurrentTime)
			&& (shouldSayName == timer.shouldSayName)
			&& (ttsFrequency == timer.ttsFrequency)
			&& (ttsSpeechRate == timer.ttsSpeechRate)
			&& (ttsVoice == timer.ttsVoice)
			&& (shouldGraduallyIncreaseVolume == timer.shouldGraduallyIncreaseVolume)
			&& (graduallyIncreaseVolumeWaitTime == timer.graduallyIncreaseVolumeWaitTime)
			&& (shouldRestrictVolume == timer.shouldRestrictVolume)
			&& (shouldAutoDismiss == timer.shouldAutoDismiss)
			&& (autoDismissTime == timer.autoDismissTime)
			&& (shouldDeleteAfterDismissed == timer.shouldDeleteAfterDismissed)
	}

	/**
	 * Print all values in the alarm object.
	 */
	@Suppress("unused")
	override fun print()
	{
		println("Alarm Information")
		println("Id                    : $id")
		println("Is Active             : $isActive")
		println("Duration              : $duration")
		println("Repeat                : $shouldRepeat")
		println("Repeat Freq           : $repeatFrequency")
		println("Repeat Freq Units     : $repeatFrequencyUnits")
		println("Vibrate               : $shouldVibrate")
		println("Vibrate duration      : $vibrateDuration")
		println("Vibrate wait time     : $vibrateWaitTime")
		println("Vibrate pattern       : $shouldVibratePattern")
		println("Vibrate repeat pat.   : $vibrateRepeatPattern")
		println("Vibrate wait after    : $vibrateWaitTimeAfterPattern")
		println("Use NFC               : $shouldUseNfc")
		println("Nfc Tag Id            : $nfcTagId")
		println("Nfc Tag Dismiss Order : $nfcTagDismissOrder")
		println("Sacn NFC tag to start : $scanNfcTagIdToStart")
		println("Use Flashlight        : $shouldUseFlashlight")
		println("Flashlight Strength   : $flashlightStrengthLevel")
		println("Grad Inc Flash        : $graduallyIncreaseFlashlightStrengthLevelWaitTime")
		println("Should Blink Flash    : $shouldBlinkFlashlight")
		println("Flashlight On         : $flashlightOnDuration")
		println("Flashlight Off        : $flashlightOffDuration")
		println("Media Path            : $mediaPath")
		println("Media Artist          : $mediaArtist")
		println("Media Name            : $mediaTitle")
		println("Media Type            : $mediaType")
		println("Local media Path      : $localMediaPath")
		println("Shuffle media         : $shouldShuffleMedia")
		println("Recusively Play       : $shouldRecursivelyPlayMedia")
		println("Volume                : $volume")
		println("Audio Source          : $audioSource")
		println("Name                  : $name")
		println("Tts say time          : $shouldSayCurrentTime")
		println("Tts say name          : $shouldSayName")
		println("Tts Freq              : $ttsFrequency")
		println("Tts Speech Rate       : $ttsSpeechRate")
		println("Tts Voice             : $ttsVoice")
		println("Grad Inc Vol          : $shouldGraduallyIncreaseVolume")
		println("Grad Inc Vol Wait T   : $graduallyIncreaseVolumeWaitTime")
		println("Restrict Vol          : $shouldRestrictVolume")
		println("Should auto dismiss   : $shouldAutoDismiss")
		println("Auto Dismiss          : $autoDismissTime")
		println("Should delete after   : $shouldDeleteAfterDismissed")
		println("Should Volume Stop    : $shouldVolumeStop")
	}

	/**
	 * Write data into parcel (required for Parcelable).
	 *
	 * Update this when adding/removing an element.
	 */
	override fun writeToParcel(output: Parcel, flags: Int)
	{
		// ID
		output.writeLong(id)

		// Active timer
		output.writeInt(if (isActive) 1 else 0)
		output.writeLong(duration)

		// Repeat
		output.writeInt(if (shouldRepeat) 1 else 0)
		output.writeInt(repeatFrequency)
		output.writeInt(repeatFrequencyUnits)

		// Vibrate
		output.writeInt(if (shouldVibrate) 1 else 0)
		output.writeLong(vibrateDuration)
		output.writeLong(vibrateWaitTime)
		output.writeInt(if (shouldVibratePattern) 1 else 0)
		output.writeInt(vibrateRepeatPattern)
		output.writeLong(vibrateWaitTimeAfterPattern)

		// NFC
		output.writeInt(if (shouldUseNfc) 1 else 0)
		output.writeString(nfcTagId)
		output.writeInt(nfcTagDismissOrder)
		output.writeString(scanNfcTagIdToStart)

		// Flashlight
		output.writeInt(if (shouldUseFlashlight) 1 else 0)
		output.writeInt(flashlightStrengthLevel)
		output.writeInt(graduallyIncreaseFlashlightStrengthLevelWaitTime)
		output.writeInt(if (shouldBlinkFlashlight) 1 else 0)
		output.writeString(flashlightOnDuration)
		output.writeString(flashlightOffDuration)

		// Media
		output.writeString(mediaPath)
		output.writeString(mediaArtist)
		output.writeString(mediaTitle)
		output.writeInt(mediaType)
		output.writeString(localMediaPath)
		output.writeInt(if (shouldShuffleMedia) 1 else 0)
		output.writeInt(if (shouldRecursivelyPlayMedia) 1 else 0)

		// Volume and audio source
		output.writeInt(volume)
		output.writeInt(if (shouldGraduallyIncreaseVolume) 1 else 0)
		output.writeInt(graduallyIncreaseVolumeWaitTime)
		output.writeInt(if (shouldRestrictVolume) 1 else 0)
		output.writeString(audioSource)

		// Name
		output.writeString(name)

		// Text-to-speech
		output.writeInt(if (shouldSayCurrentTime) 1 else 0)
		output.writeInt(if (shouldSayName) 1 else 0)
		output.writeInt(ttsFrequency)
		output.writeFloat(ttsSpeechRate)
		output.writeString(ttsVoice)

		// Dismiss/stop
		output.writeInt(if (shouldAutoDismiss) 1 else 0)
		output.writeInt(autoDismissTime)
		output.writeInt(if (shouldVolumeStop) 1 else 0)
	}

	companion object {

		/**
		 * Generate parcel (required for Parcelable).
		 */
		@Suppress("unused")
		@JvmField
		val CREATOR: Parcelable.Creator<NacTimer> = object : Parcelable.Creator<NacTimer>
		{
			override fun createFromParcel(input: Parcel): NacTimer
			{
				return NacTimer(input)
			}

			override fun newArray(size: Int): Array<NacTimer?>
			{
				return arrayOfNulls(size)
			}
		}

		/**
		 * Build an alarm.
		 */
		fun build(shared: NacSharedPreferences? = null): NacTimer
		{
			// Create a timer
			val timer = NacTimer()

			// Unable to access defaults in shared preferences because null
			if (shared == null)
			{
				return timer
			}

			// Repeat
			timer.shouldRepeat = shared.shouldRepeatTimer
			//timer.repeatFrequency = shared.repeatFrequency
			//timer.repeatFrequencyUnits = shared.repeatFrequencyUnits

			// Vibrate
			timer.shouldVibrate = shared.shouldVibrateTimer
			timer.vibrateDuration = shared.vibrateDurationTimer
			timer.vibrateWaitTime = shared.vibrateWaitTimeTimer
			timer.shouldVibratePattern = shared.shouldVibratePatternTimer
			timer.vibrateRepeatPattern = shared.vibrateRepeatPatternTimer
			timer.vibrateWaitTimeAfterPattern = shared.vibrateWaitTimeAfterPatternTimer

			// NFC
			timer.shouldUseNfc = shared.shouldUseNfcTimer
			timer.nfcTagId = shared.nfcTagIdTimer
			timer.nfcTagDismissOrder = shared.nfcTagDismissOrderTimer

			// Flashlight
			timer.shouldUseFlashlight = shared.shouldUseFlashlightTimer
			timer.flashlightStrengthLevel = shared.flashlightStrengthLevelTimer
			timer.graduallyIncreaseFlashlightStrengthLevelWaitTime = shared.graduallyIncreaseFlashlightStrengthLevelWaitTimeTimer
			timer.shouldBlinkFlashlight = shared.shouldBlinkFlashlightTimer
			timer.flashlightOnDuration = shared.flashlightOnDurationTimer
			timer.flashlightOffDuration = shared.flashlightOffDurationTimer

			// Media
			timer.mediaPath = shared.mediaPathTimer
			timer.mediaArtist = shared.mediaArtistTimer
			timer.mediaTitle = shared.mediaTitleTimer
			timer.mediaType = shared.mediaTypeTimer
			timer.localMediaPath = shared.localMediaPathTimer
			timer.shouldShuffleMedia = shared.shouldShuffleMediaTimer
			timer.shouldRecursivelyPlayMedia = shared.recursivelyPlayMediaTimer

			// Volume and audio source
			timer.volume = shared.volumeTimer
			timer.shouldGraduallyIncreaseVolume = shared.shouldGraduallyIncreaseVolumeTimer
			timer.graduallyIncreaseVolumeWaitTime = shared.graduallyIncreaseVolumeWaitTimeTimer
			timer.shouldRestrictVolume = shared.shouldRestrictVolumeTimer
			timer.audioSource = shared.audioSourceTimer

			// Name
			timer.name = shared.nameTimer

			// Text-to-speech
			timer.shouldSayCurrentTime = shared.shouldSayCurrentTimeTimer
			timer.shouldSayName = shared.shouldSayAlarmNameTimer
			timer.ttsFrequency = shared.ttsFrequencyTimer
			timer.ttsSpeechRate = shared.ttsSpeechRateTimer
			timer.ttsVoice = shared.ttsVoiceTimer

			// Dismiss
			timer.shouldAutoDismiss = shared.shouldAutoDismissTimer
			timer.autoDismissTime = shared.autoDismissTimeTimer
			timer.shouldDeleteAfterDismissed = shared.shouldDeleteAfterDismissedTimer

			return timer
		}

	}

}

/**
 * Hilt module to provide an instance of a timer.
 */
@Suppress("unused")
@InstallIn(SingletonComponent::class)
@Module
class NacTimerModule
{

	/**
	 * Provide an instance of a timer.
	 */
	@Provides
	fun provideTimer() : NacTimer
	{
		return NacTimer.build()
	}

}
