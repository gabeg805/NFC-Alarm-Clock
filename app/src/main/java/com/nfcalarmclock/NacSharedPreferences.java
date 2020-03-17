package com.nfcalarmclock;

import android.content.Context;
import android.content.res.Resources;
import android.content.SharedPreferences;
import android.graphics.Color;
import androidx.preference.PreferenceManager;
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
	 * Default app rating counter.
	 */
	public static final int DEFAULT_RATE_MY_APP_COUNTER = 0;

	/**
	 * Default app rating counter limit.
	 */
	public static final int DEFAULT_RATE_MY_APP_LIMIT = 30;

	/**
	 * Counter amount indicating that the app has been rated.
	 */
	public static final int DEFAULT_RATE_MY_APP_RATED = -999;

	/**
	 * Default auto dismiss duration.
	 */
	public static final int DEFAULT_AUTO_DISMISS = 15;

	/**
	 * Default prevent app from closing.
	 */
	public static final boolean DEFAULT_PREVENT_APP_FROM_CLOSING = false;

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
	 * When a new alarm is created with the "+" button, the alarm card is
	 * expanded.
	 */
	public static final boolean DEFAULT_EXPAND_NEW_ALARM = true;

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
	 * Default show missed alarm notification.
	 */
	public static final boolean DEFAULT_MISSED_ALARM_NOTIFICATION = true;

	/**
	 * Default show upcoming alarm notification.
	 */
	public static final boolean DEFAULT_UPCOMING_ALARM_NOTIFICATION = false;

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
	 * Default previous volume.
	 */
	public static final int DEFAULT_PREVIOUS_VOLUME = -1;

	/**
	 * Default audio source.
	 */
	public static final String DEFAULT_AUDIO_SOURCE = "Music";

	/**
	 * Default sound path.
	 */
	public static final String DEFAULT_MEDIA_PATH = "";

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
	public static final String DEFAULT_MEDIA_SUMMARY = "None";

	/**
	 * Default sound message.
	 */
	public static final String DEFAULT_MEDIA_MESSAGE = "Song or ringtone";

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
	 * Default max name length.
	 */
	public static final int DEFAULT_MAX_NAME_LENGTH = 32;

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
	 * @see editAudioSource
	 */
	public void editAudioSource(String source)
	{
		this.editAudioSource(source, false);
	}

	/**
	 * @return Edit the audio source.
	 */
	public void editAudioSource(String source, boolean commit)
	{
		String key = this.getKeys().getAudioSource();

		this.saveString(key, source, commit);
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
	 * @see editPreviousVolume
	 */
	public void editPreviousVolume(int previous)
	{
		this.editPreviousVolume(previous, false);
	}

	/**
	 * Edit the previous system volume, before an alarm goes off.
	 */
	public void editPreviousVolume(int previous, boolean commit)
	{
		String key = this.getKeys().getPreviousVolume();

		this.saveInt(key, previous, commit);
	}

	/**
	 * @see editRateMyAppCounter
	 */
	public void editRateMyAppCounter(int counter)
	{
		this.editRateMyAppCounter(counter, false);
	}

	/**
	 * Edit the app rating counter.
	 */
	public void editRateMyAppCounter(int counter, boolean commit)
	{
		String key = this.getKeys().getRateMyAppCounter();

		this.saveInt(key, counter, commit);
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
		String key = NacSharedKeys.getSnoozeCount(id);
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
	 * @see editMediaPath
	 */
	public void editMediaPath(String path)
	{
		this.editMediaPath(path, false);
	}

	/**
	 * Edit the sound preference value.
	 */
	public void editMediaPath(String path, boolean commit)
	{
		String key = this.getKeys().getMediaPath();

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
	 * @return Whether a new alarm card should be expanded or not.
	 */
	public boolean getExpandNewAlarm()
	{
		String key = this.getKeys().getExpandNewAlarm();

		return this.getSharedPreferences().getBoolean(key,
			DEFAULT_EXPAND_NEW_ALARM);
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
	 * @return The media path.
	 */
	public String getMediaPath()
	{
		String key = this.getKeys().getMediaPath();

		return this.getSharedPreferences().getString(key, DEFAULT_MEDIA_PATH);
	}

	/**
	 * @return The sound message.
	 */
	public static String getMediaMessage(Context context, String path)
	{
		return ((path != null) && !path.isEmpty())
			? NacMedia.getTitle(context, path)
			: NacSharedPreferences.DEFAULT_MEDIA_MESSAGE;
	}

	/**
	 * @return The media summary.
	 */
	public String getMediaSummary()
	{
		Context context = this.getContext();
		String path = this.getMediaPath();

		return NacSharedPreferences.getMediaSummary(context, path);
	}

	/**
	 * @see getMediaSummary
	 */
	public static String getMediaSummary(Context context, String path)
	{
		String name = NacMedia.getTitle(context, path);

		return (!name.isEmpty()) ? name
			: NacSharedPreferences.DEFAULT_MEDIA_SUMMARY;
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
	 * @return True to display missed alarm notifications, and False otherwise.
	 */
	public boolean getMissedAlarmNotification()
	{
		String key = this.getKeys().getMissedAlarmNotification();

		return this.getSharedPreferences().getBoolean(key,
			DEFAULT_MISSED_ALARM_NOTIFICATION);
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
		return (!name.isEmpty()) ? name
			: NacSharedPreferences.DEFAULT_NAME_MESSAGE;
	}

	/**
	 * @return The name summary.
	 */
	public String getNameSummary()
	{
		String name = this.getName();

		return NacSharedPreferences.getNameSummary(name);
	}

	/**
	 * @see getNameSummary
	 */
	public static String getNameSummary(String name)
	{
		return ((name != null) && !name.isEmpty()) ? name.replace("\n", " ")
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
	 * @return Whether you should prevent app from closing during an alarm
	 *         activity or not.
	 */
	public boolean getPreventAppFromClosing()
	{
		String key = this.getKeys().getPreventAppFromClosing();

		return this.getSharedPreferences().getBoolean(key,
			DEFAULT_PREVENT_APP_FROM_CLOSING);
	}

	/**
	 * @return The previous system volume, before an alarm goes off.
	 */
	public int getPreviousVolume()
	{
		String key = this.getKeys().getPreviousVolume();

		return this.getSharedPreferences().getInt(key, DEFAULT_PREVIOUS_VOLUME);
	}

	/**
	 * @return The app's rating counter.
	 */
	public int getRateMyAppCounter()
	{
		String key = this.getKeys().getRateMyAppCounter();

		return this.getSharedPreferences().getInt(key,
			DEFAULT_RATE_MY_APP_COUNTER);
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
		String key = NacSharedKeys.getSnoozeCount(id);

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
	 * @return True to display upcoming alarm notifications, and False otherwise.
	 */
	public boolean getUpcomingAlarmNotification()
	{
		String key = this.getKeys().getUpcomingAlarmNotification();

		return this.getSharedPreferences().getBoolean(key,
			DEFAULT_UPCOMING_ALARM_NOTIFICATION);
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
