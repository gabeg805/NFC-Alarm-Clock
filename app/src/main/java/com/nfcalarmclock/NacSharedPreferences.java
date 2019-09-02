package com.nfcalarmclock;

import android.content.Context;
import android.content.res.Resources;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.preference.PreferenceManager;
import java.util.EnumSet;

/**
 * Container for the values of each preference.
 */
public class NacSharedPreferences
{

	/**
	 * The context application.
	 */
	private final Context mContext;

	/**
	 * Shared preferences instance.
	 */
	private final SharedPreferences mSharedPreferences;

	/**
	 * Shared preference keys.
	 */
	private final NacSharedKeys mKeys;

	/**
	 * Default value to see if this is the app's first run after installing.
	 */
	private static final boolean DEFAULT_APP_FIRST_RUN = true;

	/**
	 * Default auto dismiss message.
	 */
	public static final String DEFAULT_AUTO_DISMISS_MESSAGE = "";

	/**
	 * Default auto dismiss duration.
	 */
	public static final int DEFAULT_AUTO_DISMISS = 15;

	/**
	 * Default format to display the next alarm.
	 *
	 * 0 = Next alarm in 7 hours 30 minutes
	 * 1 = Next alarm on Mon, 7:00 AM
	 */
	public static final int DEFAULT_NEXT_ALARM_FORMAT = 0;

	/**
	 * Default show alarm information value.
	 */
	public static final boolean DEFAULT_SHOW_ALARM_INFO = false;

	/**
	 * Default use NFC.
	 */
	public static final boolean DEFAULT_USE_NFC = false;

	/**
	 * Default max snooze count.
	 */
	public static final int DEFAULT_MAX_SNOOZE = -1;

	/**
	 * Default snooze count.
	 */
	public static final int DEFAULT_SNOOZE_COUNT = 0;

	/**
	 * Default snooze duration.
	 */
	public static final int DEFAULT_SNOOZE_DURATION = 5;

	/**
	 * Default easy snooze.
	 */
	public static final boolean DEFAULT_EASY_SNOOZE = false;

	/**
	 * Default shuffle value.
	 */
	public static final boolean DEFAULT_SHUFFLE = false;

	/**
	 * Default value to start week on.
	 *
	 * Sunday = 0
	 * Monday = 1
	 * Saturday = 6
	 */
	public static final int DEFAULT_START_WEEK_ON = 0;

	/**
	 * Default speak to me value.
	 */
	public static final boolean DEFAULT_SPEAK_TO_ME = false;

	/**
	 * Default speak frequency value.
	 */
	public static final int DEFAULT_SPEAK_FREQUENCY = 1;

	/**
	 * Default auto dismiss index value.
	 */
	public static final int DEFAULT_AUTO_DISMISS_INDEX = 7;

	/**
	 * Default max snooze index value.
	 */
	public static final int DEFAULT_MAX_SNOOZE_INDEX = 11;

	/**
	 * Default snooze duration index value.
	 */
	public static final int DEFAULT_SNOOZE_DURATION_INDEX = 4;

	/**
	 * Default maximum number of alarms.
	 */
	public static final int DEFAULT_MAX_ALARMS = 50;

	/**
	 * Default days.
	 */
	public static final int DEFAULT_DAYS = NacCalendar.Days.daysToValue(
		EnumSet.of(NacCalendar.Day.MONDAY, NacCalendar.Day.TUESDAY,
			NacCalendar.Day.WEDNESDAY, NacCalendar.Day.THURSDAY,
			NacCalendar.Day.FRIDAY));

	/**
	 * Default repeat status.
	 */
	public static final boolean DEFAULT_REPEAT = true;

	/**
	 * Default vibrate.
	 */
	public static final boolean DEFAULT_VIBRATE = true;

	/**
	 * Default volume.
	 */
	public static final int DEFAULT_VOLUME = 75;

	/**
	 * Default audio source.
	 */
	public static final String DEFAULT_AUDIO_SOURCE = "Music";

	/**
	 * Default sound path.
	 */
	public static final String DEFAULT_SOUND = "";

	/**
	 * Default alarm name.
	 */
	public static final String DEFAULT_NAME = "";

	/**
	 * Default color.
	 */
	public static final int DEFAULT_COLOR = Color.WHITE;

	/**
	 * Default theme color.
	 */
	public static final int DEFAULT_THEME_COLOR = 0xfffb8c00;

	/**
	 * Default name color.
	 */
	public static final int DEFAULT_NAME_COLOR = 0xff00c0fb;

	/**
	 * Default days color.
	 */
	public static final int DEFAULT_DAYS_COLOR = 0xfffb8c00;

	/**
	 * Default time color.
	 */
	public static final int DEFAULT_TIME_COLOR = Color.WHITE;

	/**
	 * Default AM color.
	 */
	public static final int DEFAULT_AM_COLOR = Color.WHITE;

	/**
	 * Default PM color.
	 */
	public static final int DEFAULT_PM_COLOR = Color.WHITE;

	/**
	 * Default sound summary text.
	 */
	public static final String DEFAULT_SOUND_SUMMARY = "None";

	/**
	 * Default sound message.
	 */
	public static final String DEFAULT_SOUND_MESSAGE = "Song or ringtone";

	/**
	 * Default name summary text.
	 */
	public static final String DEFAULT_NAME_SUMMARY = "None";

	/**
	 * Default name message.
	 */
	public static final String DEFAULT_NAME_MESSAGE = "Alarm name";

	/**
	 * Default days summary text.
	 */
	public static final String DEFAULT_DAYS_SUMMARY = "None";

	/**
	 * Default days message.
	 */
	public static final String DEFAULT_DAYS_MESSAGE = "";

	/**
	 */
	public NacSharedPreferences(Context context)
	{
		this.mSharedPreferences = PreferenceManager
			.getDefaultSharedPreferences(context);
		this.mKeys = new NacSharedKeys(context);
		this.mContext = context;
	}

	/**
	 * @see editAmColor
	 */
	public void editAmColor(int color)
	{
		this.editAmColor(color, false);
	}

	/**
	 * Edit the AM preference color.
	 */
	public void editAmColor(int color, boolean commit)
	{
		String key = this.getKeys().getAmColor();

		this.saveInt(key, color, commit);
	}

	/**
	 * @see editAppFirstRun
	 */
	public void editAppFirstRun(boolean first)
	{
		this.editAppFirstRun(first, false);
	}

	/**
	 * Edit the app first run value.
	 */
	public void editAppFirstRun(boolean first, boolean commit)
	{
		String key = this.getKeys().getAppFirstRun();

		this.saveBoolean(key, first, commit);
	}

	/**
	 * @see editAutoDismiss
	 */
	public void editAutoDismiss(int index)
	{
		this.editAutoDismiss(index, false);
	}

	/**
	 * Edit the auto dismiss preference value.
	 */
	public void editAutoDismiss(int index, boolean commit)
	{
		String key = this.getKeys().getAutoDismiss();

		this.saveInt(key, index, commit);
	}

	/**
	 * @see editAutoDismissMessage
	 */
	public void editAutoDismissMessage(String message)
	{
		this.editAutoDismissMessage(message, false);
	}

	/**
	 * Edit the auto dismiss message.
	 */
	public void editAutoDismissMessage(String message, boolean commit)
	{
		String key = this.getKeys().getAutoDismissMessage();

		this.saveString(key, message, commit);
	}

	/**
	 * @see editDays
	 */
	public void editDays(int days)
	{
		this.editDays(days, false);
	}

	/**
	 * Edit the days preference value.
	 */
	public void editDays(int days, boolean commit)
	{
		String key = this.getKeys().getDays();

		this.saveInt(key, days, commit);
	}

	/**
	 * @see editDaysColor
	 */
	public void editDaysColor(int color)
	{
		this.editDaysColor(color, false);
	}

	/**
	 * Edit the days preference color.
	 */
	public void editDaysColor(int color, boolean commit)
	{
		String key = this.getKeys().getDaysColor();

		this.saveInt(key, color, commit);
	}

	/**
	 * @see editEasySnooze
	 */
	public void editEasySnooze(boolean easy)
	{
		this.editEasySnooze(easy, false);
	}

	/**
	 * Edit the easy snooze preference value.
	 */
	public void editEasySnooze(boolean easy, boolean commit)
	{
		String key = this.getKeys().getEasySnooze();

		this.saveBoolean(key, easy, commit);
	}

	/**
	 * @see editMaxSnooze
	 */
	public void editMaxSnooze(int max)
	{
		this.editMaxSnooze(max, false);
	}

	/**
	 * Edit the max snooze preference value.
	 */
	public void editMaxSnooze(int max, boolean commit)
	{
		String key = this.getKeys().getMaxSnooze();
		NacUtility.printf("Edit Max Snooze : %d", max);

		this.saveInt(key, max, commit);
	}

	/**
	 * @see editName
	 */
	public void editName(String name)
	{
		this.editName(name, false);
	}

	/**
	 * Edit the alarm name preference value.
	 */
	public void editName(String name, boolean commit)
	{
		String key = this.getKeys().getName();

		this.saveString(key, name, commit);
	}

	/**
	 * @see editNameColor
	 */
	public void editNameColor(int color)
	{
		this.editNameColor(color, false);
	}

	/**
	 * Edit the alarm name preference color.
	 */
	public void editNameColor(int color, boolean commit)
	{
		String key = this.getKeys().getNameColor();

		this.saveInt(key, color, commit);
	}

	/**
	 * @see editNextAlarmFormat
	 */
	public void editNextAlarmFormat(int format)
	{
		this.editNextAlarmFormat(format, false);
	}

	/**
	 * Edit the display time remaining preference.
	 */
	public void editNextAlarmFormat(int format, boolean commit)
	{
		String key = this.getKeys().getNextAlarmFormat();

		this.saveInt(key, format, commit);
	}

	/**
	 * @see editPmColor
	 */
	public void editPmColor(int color)
	{
		this.editPmColor(color, false);
	}

	/**
	 * Edit the PM preference color.
	 */
	public void editPmColor(int color, boolean commit)
	{
		String key = this.getKeys().getPmColor();

		this.saveInt(key, color, commit);
	}

	/**
	 * @see editRepeat
	 */
	public void editRepeat(boolean repeat)
	{
		this.editRepeat(repeat, false);
	}

	/**
	 * Edit the repeat preference value.
	 */
	public void editRepeat(boolean repeat, boolean commit)
	{
		String key = this.getKeys().getRepeat();

		this.saveBoolean(key, repeat, commit);
	}

	/**
	 * Edit the show alarm information value.
	 */
	public void editShowAlarmInfo(boolean show, boolean commit)
	{
		String key = this.getKeys().getShowAlarmInfo();

		this.saveBoolean(key, show, commit);
	}

	/**
	 * @see editSnoozeCount
	 */
	public void editSnoozeCount(int id, int count)
	{
		this.editSnoozeCount(id, count, false);
	}

	/**
	 * Edit the snooze count value.
	 */
	public void editSnoozeCount(int id, int count, boolean commit)
	{
		String key = this.getKeys().getSnoozeCount(id);
		NacUtility.printf("Edit Snooze Count : %d", count);

		this.saveInt(key, count, commit);
	}

	/**
	 * @see editSnoozeDuration
	 */
	public void editSnoozeDuration(int duration)
	{
		this.editSnoozeDuration(duration, false);
	}

	/**
	 * Edit the snooze duration preference value.
	 */
	public void editSnoozeDuration(int duration, boolean commit)
	{
		String key = this.getKeys().getSnoozeDuration();
		NacUtility.printf("Edit Snooze Duration : %d", duration);

		this.saveInt(key, duration, commit);
	}

	/**
	 * @see editSound
	 */
	public void editSound(String path)
	{
		this.editSound(path, false);
	}

	/**
	 * Edit the sound preference value.
	 */
	public void editSound(String path, boolean commit)
	{
		String key = this.getKeys().getSound();

		this.saveString(key, path, commit);
	}

	/**
	 * @see editSpeakFrequency
	 */
	public void editSpeakFrequency(int freq)
	{
		this.editSpeakFrequency(freq, false);
	}

	/**
	 * Edit the speak frequency preference value.
	 */
	public void editSpeakFrequency(int freq, boolean commit)
	{
		String key = this.getKeys().getSpeakFrequency();

		this.saveInt(key, freq, commit);
	}

	/**
	 * @see editSpeakToMe
	 */
	public void editSpeakToMe(boolean speak)
	{
		this.editSpeakToMe(speak, false);
	}

	/**
	 * Edit the speak to me preference value.
	 */
	public void editSpeakToMe(boolean speak, boolean commit)
	{
		String key = this.getKeys().getSpeakToMe();

		this.saveBoolean(key, speak, commit);
	}

	/**
	 * @see editStartWeekOn
	 */
	public void editStartWeekOn(int start)
	{
		this.editStartWeekOn(start, false);
	}

	/**
	 * Edit the preference value indicating which day to start on.
	 */
	public void editStartWeekOn(int start, boolean commit)
	{
		String key = this.getKeys().getStartWeekOn();

		this.saveInt(key, start, commit);
	}

	/**
	 * @see editThemeColor
	 */
	public void editThemeColor(int color)
	{
		this.editThemeColor(color, false);
	}

	/**
	 * Edit the theme color preference value.
	 */
	public void editThemeColor(int color, boolean commit)
	{
		String key = this.getKeys().getThemeColor();

		this.saveInt(key, color, commit);
	}

	/**
	 * @see editTimeColor
	 */
	public void editTimeColor(int color)
	{
		this.editTimeColor(color, false);
	}

	/**
	 * Edit the time color preference value.
	 */
	public void editTimeColor(int color, boolean commit)
	{
		String key = this.getKeys().getTimeColor();

		this.saveInt(key, color, commit);
	}

	/**
	 * @see editVibrate
	 */
	public void editVibrate(boolean vibrate)
	{
		this.editVibrate(vibrate, false);
	}

	/**
	 * Edit the vibrate preference value.
	 */
	public void editVibrate(boolean vibrate, boolean commit)
	{
		String key = this.getKeys().getVibrate();

		this.saveBoolean(key, vibrate, commit);

	}

	/**
	 * @return The AM color.
	 */
	public int getAmColor()
	{
		String key = this.getKeys().getAmColor();

		return this.getSharedPreferences().getInt(key, DEFAULT_AM_COLOR);
	}

	/**
	 * @return The app's first run value.
	 */
	public boolean getAppFirstRun()
	{
		String key = this.getKeys().getAppFirstRun();

		return this.getSharedPreferences().getBoolean(key, DEFAULT_APP_FIRST_RUN);
	}

	/**
	 * @return The audio source.
	 */
	public String getAudioSource()
	{
		String key = this.getKeys().getAudioSource();

		return this.getSharedPreferences().getString(key,
			DEFAULT_AUDIO_SOURCE);
	}

	/**
	 * @return Auto dismiss duration.
	 */
	public int getAutoDismiss()
	{
		String key = this.getKeys().getAutoDismiss();

		return this.getSharedPreferences().getInt(key,
			DEFAULT_AUTO_DISMISS_INDEX);
	}

	/**
	 * @return Whether the alarm was auto-dismissed or not.
	 */
	public String getAutoDismissMessage()
	{
		String key = this.getKeys().getAutoDismissMessage();

		return this.getSharedPreferences().getString(key,
			DEFAULT_AUTO_DISMISS_MESSAGE);
	}

	/**
	 * @see getAutoDismissSummary
	 */
	public String getAutoDismissSummary()
	{
		int index = this.getAutoDismiss();

		return NacSharedPreferences.getAutoDismissSummary(index);
	}

	/**
	 * @return The summary text to use when displaying the auto dismiss widget.
	 */
	public static String getAutoDismissSummary(int index)
	{
		int value = NacSharedPreferences.getAutoDismissTime(index);
		String dismiss = String.valueOf(value);

		if (index == 0)
		{
			return "Off";
		}
		else if (index == 1)
		{
			return dismiss + " minute";

		}
		else
		{
			return dismiss + " minutes";
		}
	}

	/**
	 * @see getAutoDismissTime
	 */
	public int getAutoDismissTime()
	{
		int index = this.getAutoDismiss();

		return NacSharedPreferences.getAutoDismissTime(index);
	}

	/**
	 * @return Calculate the auto dismiss duration from an index value,
	 *         corresponding to a location in the spainner widget.
	 */
	public static int getAutoDismissTime(int index)
	{
		return (index < 5) ? index : (index-4)*5;
	}

	/**
	 * @return The application context.
	 */
	public Context getContext()
	{
		return this.mContext;
	}

	/**
	 * @return The alarm days.
	 */
	public int getDays()
	{
		String key = this.getKeys().getDays();

		return this.getSharedPreferences().getInt(key, DEFAULT_DAYS);
	}

	/**
	 * @return The days color.
	 */
	public int getDaysColor()
	{
		String key = this.getKeys().getDaysColor();

		return this.getSharedPreferences().getInt(key, DEFAULT_DAYS_COLOR);
	}

	/**
	 * @return The days summary.
	 */
	public String getDaysSummary()
	{
		int value = this.getDays();

		return NacSharedPreferences.getDaysSummary(value);
	}

	/**
	 * @see getDaysSummary
	 */
	public static String getDaysSummary(int value)
	{
		String days = NacCalendar.Days.toString(value);

		return (!days.isEmpty()) ? days
			: NacSharedPreferences.DEFAULT_DAYS_SUMMARY;
	}

	/**
	 * @return Whether easy snooze is enabled or not.
	 */
	public boolean getEasySnooze()
	{
		String key = this.getKeys().getEasySnooze();

		return this.getSharedPreferences().getBoolean(key, DEFAULT_EASY_SNOOZE);
	}

	/**
	 * @return The SharedPreferences instance. To-do: Change this.
	 */
	public SharedPreferences getInstance()
	{
		return this.mSharedPreferences;
	}

	/**
	 * @return The preference keys.
	 */
	public NacSharedKeys getKeys()
	{
		return this.mKeys;
	}

	/**
	 * @return The max number of snoozes.
	 */
	public int getMaxSnooze()
	{
		String key = this.getKeys().getMaxSnooze();

		return this.getSharedPreferences().getInt(key,
			DEFAULT_MAX_SNOOZE_INDEX);
	}

	/**
	 * @see getMaxSnoozeSummary
	 */
	public String getMaxSnoozeSummary()
	{
		int index = this.getMaxSnooze();

		return NacSharedPreferences.getMaxSnoozeSummary(index);
	}

	/**
	 * @return The summary text to use when displaying the max snooze widget.
	 */
	public static String getMaxSnoozeSummary(int index)
	{
		int value = NacSharedPreferences.getMaxSnoozeValue(index);

		if (index == 0)
		{
			return "None";
		}
		else if (index == 11)
		{
			return "Unlimited";
		}
		else
		{
			return String.valueOf(value);
		}
	}

	/**
	 * @see getMaxSnoozeValue
	 */
	public int getMaxSnoozeValue()
	{
		int index = this.getMaxSnooze();

		return NacSharedPreferences.getMaxSnoozeValue(index);
	}

	/**
	 * @return Calculate the max snooze duration from an index corresponding
	 *         to a location in the spinner widget.
	 */
	public static int getMaxSnoozeValue(int index)
	{
		return (index == 11) ? -1 : index;
	}

	/**
	 * @return The meridian color.
	 */
	public int getMeridianColor(String meridian)
	{
		if (meridian.equals("AM"))
		{
			return this.getAmColor();
		}
		else if (meridian.equals("PM"))
		{
			return this.getPmColor();
		}
		else
		{
			return DEFAULT_COLOR;
		}
	}

	/**
	 * @return The name of the alarm.
	 */
	public String getName()
	{
		String key = this.getKeys().getName();

		return this.getSharedPreferences().getString(key, DEFAULT_NAME);
	}

	/**
	 * @return The name color.
	 */
	public int getNameColor()
	{
		String key = this.getKeys().getNameColor();

		return this.getSharedPreferences().getInt(key, DEFAULT_NAME_COLOR);
	}

	/**
	 * @return The name message.
	 */
	public static String getNameMessage(String name)
	{
		return ((name != null) && !name.isEmpty()) ? name
			: NacSharedPreferences.DEFAULT_NAME_MESSAGE;
	}

	/**
	 * @return The name summary.
	 */
	public String getNameSummary()
	{
		String value = this.getName();

		return NacSharedPreferences.getNameSummary(value);
	}

	/**
	 * @see getNameSummary
	 */
	public static String getNameSummary(String value)
	{
		return ((value != null) && !value.isEmpty()) ? value
			: NacSharedPreferences.DEFAULT_NAME_SUMMARY;
	}

	/**
	 * @return Whether the display next alarm should show time remaining for
	 *         the next alarm or not.
	 */
	public int getNextAlarmFormat()
	{
		String key = this.getKeys().getNextAlarmFormat();

		return this.getSharedPreferences().getInt(key,
			DEFAULT_NEXT_ALARM_FORMAT);
	}

	/**
	 * @return The PM color.
	 */
	public int getPmColor()
	{
		String key = this.getKeys().getPmColor();

		return this.getSharedPreferences().getInt(key, DEFAULT_PM_COLOR);
	}

	/**
	 * @return Whether the alarm should be repeated or not.
	 */
	public boolean getRepeat()
	{
		String key = this.getKeys().getRepeat();

		return this.getSharedPreferences().getBoolean(key, DEFAULT_REPEAT);
	}

	/**
	 * @return The SharedPreferences object.
	 */
	public SharedPreferences getSharedPreferences()
	{
		return this.mSharedPreferences;
	}

	/**
	 * @return Whether the alarm information should be shown or not.
	 */
	public boolean getShowAlarmInfo()
	{
		String key = this.getKeys().getShowAlarmInfo();

		return this.getSharedPreferences().getBoolean(key, DEFAULT_SHOW_ALARM_INFO);
	}

	/**
	 * @return The shuffle status.
	 */
	public boolean getShuffle()
	{
		String key = this.getKeys().getShuffle();

		return this.getSharedPreferences().getBoolean(key, DEFAULT_SHUFFLE);
	}

	/**
	 * @return The snooze count.
	 */
	public int getSnoozeCount(int id)
	{
		String key = this.getKeys().getSnoozeCount(id);

		return this.getSharedPreferences().getInt(key,
			DEFAULT_SNOOZE_COUNT);
	}

	/**
	 * @return The snooze duration.
	 */
	public int getSnoozeDuration()
	{
		String key = this.getKeys().getSnoozeDuration();

		return this.getSharedPreferences().getInt(key,
			DEFAULT_SNOOZE_DURATION_INDEX);
	}

	/**
	 * @see getSnoozeDurationSummary
	 */
	public String getSnoozeDurationSummary()
	{
		int index = this.getSnoozeDuration();

		return NacSharedPreferences.getSnoozeDurationSummary(index);
	}

	/**
	 * @return The summary text for the snooze duration widget.
	 */
	public static String getSnoozeDurationSummary(int index)
	{
		int value = NacSharedPreferences.getSnoozeDurationValue(index);
		String snooze = String.valueOf(value);

		if (index == 0)
		{
			return snooze + " minute";
		}
		else
		{
			return snooze + " minutes";
		}
	}

	/**
	 * @see getSnoozeDurationValue
	 */
	public int getSnoozeDurationValue()
	{
		int index = this.getSnoozeDuration();

		return NacSharedPreferences.getSnoozeDurationValue(index);
	}

	/**
	 * @return Calculate the snooze duration from an index value, corresponding
	 *         to a location in the spainner widget.
	 */
	public static int getSnoozeDurationValue(int index)
	{
		return (index < 4) ? index+1 : (index-3)*5;
	}

	/**
	 * @return The sound path.
	 */
	public String getSound()
	{
		String key = this.getKeys().getSound();

		return this.getSharedPreferences().getString(key, DEFAULT_SOUND);
	}

	/**
	 * @return The sound message.
	 */
	public static String getSoundMessage(Context context, String path)
	{
		return ((path != null) && !path.isEmpty())
			? NacSharedPreferences.getSoundName(context, path)
			: NacSharedPreferences.DEFAULT_SOUND_MESSAGE;
	}

	/**
	 * @return The sound name.
	 */
	public static String getSoundName(Context context, String path)
	{
		NacSound sound = new NacSound(context, path);
		int type = sound.getType();
		String name = sound.getName();

		if (NacSound.isFilePlaylist(type))
		{
			//name += " Playlist";
		}

		return name;
	}

	/**
	 * @return The sound summary.
	 */
	public String getSoundSummary()
	{
		Context context = this.getContext();
		String path = this.getSound();

		return NacSharedPreferences.getSoundSummary(context, path);
	}

	/**
	 * @see getSoundSummary
	 */
	public static String getSoundSummary(Context context, String path)
	{
		String name = NacSharedPreferences.getSoundName(context, path);

		return (!name.isEmpty()) ? name
			: NacSharedPreferences.DEFAULT_SOUND_SUMMARY;
	}

	/**
	 * @return The speak frequency value.
	 */
	public int getSpeakFrequency()
	{
		String key = this.getKeys().getSpeakFrequency();

		return this.getSharedPreferences().getInt(key,
			DEFAULT_SPEAK_FREQUENCY);
	}

	/**
	 * @see getSpeakFrequencySummary
	 */
	public String getSpeakFrequencySummary()
	{
		int freq = this.getSpeakFrequency();

		return NacSharedPreferences.getSpeakFrequencySummary(freq);
	}

	/**
	 * @return The summary text to use when displaying the speak frequency
	 *         widget.
	 */
	public static String getSpeakFrequencySummary(int freq)
	{
		String value = String.valueOf(freq);

		if (freq == 0)
		{
			return "Once";
		}
		else
		{
			return "Every " + value + ((freq == 1) ? " minute" : " minutes");
		}
	}

	/**
	 * @return The speak to me value.
	 */
	public boolean getSpeakToMe()
	{
		String key = this.getKeys().getSpeakToMe();

		return this.getSharedPreferences().getBoolean(key,
			DEFAULT_SPEAK_TO_ME);
	}

	/**
	 * @return The value indicating which day to start on.
	 */
	public int getStartWeekOn()
	{
		String key = this.getKeys().getStartWeekOn();

		return this.getSharedPreferences().getInt(key,
			DEFAULT_START_WEEK_ON);
	}

	/**
	 * @return The theme color.
	 */
	public int getThemeColor()
	{
		String key = this.getKeys().getThemeColor();

		return this.getSharedPreferences().getInt(key, DEFAULT_THEME_COLOR);
	}

	/**
	 * @return The time color.
	 */
	public int getTimeColor()
	{
		String key = this.getKeys().getTimeColor();

		return this.getSharedPreferences().getInt(key, DEFAULT_TIME_COLOR);
	}

	/**
	 * @return Whether NFC is required or not.
	 */
	public boolean getUseNfc()
	{
		String key = this.getKeys().getUseNfc();

		return this.getSharedPreferences().getBoolean(key, DEFAULT_USE_NFC);
	}

	/**
	 * @return Whether the alarm should vibrate the phone or not.
	 */
	public boolean getVibrate()
	{
		String key = this.getKeys().getVibrate();

		return this.getSharedPreferences().getBoolean(key, DEFAULT_VIBRATE);
	}

	/**
	 * @return The alarm volume level.
	 */
	public int getVolume()
	{
		String key = this.getKeys().getVolume();

		return this.getSharedPreferences().getInt(key, DEFAULT_VOLUME);
	}

	/**
	 * @see save
	 */
	public void save(SharedPreferences.Editor editor)
	{
		this.save(editor, false);
	}

	/**
	 * Save the changes that were made to the shared preference.
	 */
	public void save(SharedPreferences.Editor editor, boolean commit)
	{
		if (commit)
		{
			editor.commit();
		}
		else
		{
			editor.apply();
		}
	}

	/**
	 * @see saveBoolean
	 */
	public void saveBoolean(String key, boolean value)
	{
		this.saveBoolean(key, value, false);
	}

	/**
	 * Save a boolean to the shared preference.
	 */
	public void saveBoolean(String key, boolean value, boolean commit)
	{
		SharedPreferences.Editor editor = this.getInstance().edit()
			.putBoolean(key, value);

		this.save(editor, commit);
	}

	/**
	 * @see saveInt
	 */
	public void saveInt(String key, int value)
	{
		this.saveInt(key, value, false);
	}

	/**
	 * Save an int to the shared preference.
	 */
	public void saveInt(String key, int value, boolean commit)
	{
		SharedPreferences.Editor editor = this.getInstance().edit()
			.putInt(key, value);

		this.save(editor, commit);
	}

	/**
	 * @see saveString
	 */
	public void saveString(String key, String value)
	{
		this.saveString(key, value, false);
	}

	/**
	 * Save a string to the shared preference.
	 */
	public void saveString(String key, String value, boolean commit)
	{
		SharedPreferences.Editor editor = this.getInstance().edit()
			.putString(key, value);

		this.save(editor, commit);
	}

}
