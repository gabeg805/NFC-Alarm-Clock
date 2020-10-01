package com.nfcalarmclock;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import androidx.preference.PreferenceManager;
import java.util.Locale;

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
	private final SharedPreferences mInstance;

	/**
	 * Shared preference keys.
	 */
	private final NacSharedKeys mKeys;

	/**
	 * Shared defaults.
	 */
	private final NacSharedDefaults mDefaults;

	/**
	 * Shared constants.
	 */
	private final NacSharedConstants mConstants;

	/**
	 */
	public NacSharedPreferences(Context context)
	{
		this.mContext = context;
		this.mInstance = PreferenceManager.getDefaultSharedPreferences(
			context);
		this.mKeys = new NacSharedKeys(context);
		this.mDefaults = new NacSharedDefaults(context);
		this.mConstants = new NacSharedConstants(context);
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
		int value = this.getDefaults().getAmColor();
		return this.getInstance().getInt(key, value);
	}

	/**
	 * @return The app's first run value.
	 */
	public boolean getAppFirstRun()
	{
		String key = this.getKeys().getAppFirstRun();
		boolean value = this.getDefaults().getAppFirstRun();
		return this.getInstance().getBoolean(key, value);
	}

	/**
	 * @return The audio source.
	 */
	public String getAudioSource()
	{
		String key = this.getKeys().getAudioSource();
		String value = this.getConstants().getAudioSources().get(1);
		return this.getInstance().getString(key, value);
	}

	/**
	 * @return Auto dismiss duration.
	 */
	public int getAutoDismiss()
	{
		String key = this.getKeys().getAutoDismiss();
		int value = this.getDefaults().getAutoDismissIndex();
		return this.getInstance().getInt(key, value);
	}

	/**
	 * @see getAutoDismissSummary
	 */
	public String getAutoDismissSummary()
	{
		Context context = this.getContext();
		int index = this.getAutoDismiss();
		return NacSharedPreferences.getAutoDismissSummary(context, index);
	}

	/**
	 * @return The summary text to use when displaying the auto dismiss widget.
	 */
	public static String getAutoDismissSummary(Context context, int index)
	{
		NacSharedConstants cons = new NacSharedConstants(context);
		int value = NacSharedPreferences.getAutoDismissTime(index);
		String dismiss = String.valueOf(value);

		if (index == 0)
		{
			return cons.getStateOff();
		}
		else
		{
			Locale locale = Locale.getDefault();
			return String.format(locale, "%1$s %2$s", dismiss, cons.getUnitMinute(index));
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
	 * @return The shared constants.
	 */
	public NacSharedConstants getConstants()
	{
		return this.mConstants;
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
		int value = this.getDefaults().getDays();
		return this.getInstance().getInt(key, value);
	}

	/**
	 * @return The days color.
	 */
	public int getDaysColor()
	{
		String key = this.getKeys().getDaysColor();
		int value = this.getDefaults().getDaysColor();
		return this.getInstance().getInt(key, value);
	}

	/**
	 * @return The days summary.
	 */
	public String getDaysSummary()
	{
		Context context = this.getContext();
		int value = this.getDays();
		int start = this.getStartWeekOn();
		String days = NacCalendar.Days.toString(context, value, start);

		return !days.isEmpty() ? days : this.getConstants().getNone();
	}

	/**
	 * @return The shared defaults.
	 */
	public NacSharedDefaults getDefaults()
	{
		return this.mDefaults;
	}

	/**
	 * @return Whether easy snooze is enabled or not.
	 */
	public boolean getEasySnooze()
	{
		String key = this.getKeys().getEasySnooze();

		return this.getInstance().getBoolean(key,
			this.getDefaults().getEasySnooze());
	}

	/**
	 * @return Whether a new alarm card should be expanded or not.
	 */
	public boolean getExpandNewAlarm()
	{
		String key = this.getKeys().getExpandNewAlarm();
		boolean value = this.getDefaults().getExpandNewAlarm();
		return this.getInstance().getBoolean(key, value);
	}

	/**
	 * @return The SharedPreferences instance.
	 */
	public SharedPreferences getInstance()
	{
		return this.mInstance;
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
		return this.getInstance().getInt(key,
			this.getDefaults().getMaxSnoozeIndex());
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
		String defaultValue = "";
		return this.getInstance().getString(key, defaultValue);
	}

	/**
	 * @return The sound message.
	 */
	public static String getMediaMessage(Context context, String path)
	{
		NacSharedConstants cons = new NacSharedConstants(context);
		return (path != null) && !path.isEmpty()
			? NacMedia.getTitle(context, path)
			: cons.getDescriptionMedia();
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
		NacSharedConstants cons = new NacSharedConstants(context);
		String name = NacMedia.getTitle(context, path);
		return !name.isEmpty() ? name : cons.getNone();
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
			return this.getDefaults().getColor();
		}
	}

	/**
	 * @return True to display missed alarm notifications, and False otherwise.
	 */
	public boolean getMissedAlarmNotification()
	{
		String key = this.getKeys().getMissedAlarmNotification();
		boolean value = this.getDefaults().getMissedAlarm();
		return this.getInstance().getBoolean(key, value);
	}

	/**
	 * @return The name of the alarm.
	 */
	public String getName()
	{
		String key = this.getKeys().getName();
		String defaultValue = "";
		return this.getInstance().getString(key, defaultValue);
	}

	/**
	 * @return The name color.
	 */
	public int getNameColor()
	{
		String key = this.getKeys().getNameColor();
		int value = this.getDefaults().getNameColor();
		return this.getInstance().getInt(key, value);
	}

	/**
	 * @return The name message.
	 */
	public static String getNameMessage(Context context, String name)
	{
		NacSharedConstants cons = new NacSharedConstants(context);
		return !name.isEmpty() ? name : cons.getName();
	}

	/**
	 * @return The name summary.
	 */
	public String getNameSummary()
	{
		Context context = getContext();
		String name = this.getName();
		return NacSharedPreferences.getNameSummary(context, name);
	}

	/**
	 * @see getNameSummary
	 */
	public static String getNameSummary(Context context, String name)
	{
		NacSharedConstants cons = new NacSharedConstants(context);
		return (name != null) && !name.isEmpty()
			? name.replace("\n", " ")
			: cons.getNone();
	}

	/**
	 * @return Whether the display next alarm should show time remaining for
	 *         the next alarm or not.
	 */
	public int getNextAlarmFormat()
	{
		String key = this.getKeys().getNextAlarmFormat();
		int value = this.getDefaults().getNextAlarmFormatIndex();
		return this.getInstance().getInt(key, value);
	}

	/**
	 * @return The PM color.
	 */
	public int getPmColor()
	{
		String key = this.getKeys().getPmColor();
		int value = this.getDefaults().getPmColor();
		return this.getInstance().getInt(key, value);
	}

	/**
	 * @return Whether you should prevent app from closing during an alarm
	 *         activity or not.
	 */
	public boolean getPreventAppFromClosing()
	{
		String key = this.getKeys().getPreventAppFromClosing();
		boolean value = this.getDefaults().getPreventAppFromClosing();
		return this.getInstance().getBoolean(key, value);
	}

	/**
	 * @return The previous system volume, before an alarm goes off.
	 */
	public int getPreviousVolume()
	{
		String key = this.getKeys().getPreviousVolume();
		int value = this.getDefaults().getPreviousVolume();
		return this.getInstance().getInt(key, value);
	}

	/**
	 * @return The app's rating counter.
	 */
	public int getRateMyAppCounter()
	{
		String key = this.getKeys().getRateMyAppCounter();
		int value = this.getDefaults().getRateMyAppCounter();
		return this.getInstance().getInt(key, value);
	}

	/**
	 * @return Whether the alarm should be repeated or not.
	 */
	public boolean getRepeat()
	{
		String key = this.getKeys().getRepeat();
		boolean value = this.getDefaults().getRepeat();
		return this.getInstance().getBoolean(key, value);
	}

	/**
	 * @return Whether the alarm information should be shown or not.
	 */
	public boolean getShowAlarmInfo()
	{
		String key = this.getKeys().getShowAlarmInfo();
		boolean value = this.getDefaults().getShowAlarmInfo();
		return this.getInstance().getBoolean(key, value);
	}

	/**
	 * @return The shuffle status.
	 */
	public boolean getShuffle()
	{
		String key = this.getKeys().getShuffle();
		boolean value = this.getDefaults().getShufflePlaylist();
		return this.getInstance().getBoolean(key, value);
	}

	/**
	 * @return The snooze count.
	 */
	public int getSnoozeCount(int id)
	{
		String key = NacSharedKeys.getSnoozeCount(id);
		int value = this.getDefaults().getSnoozeCount();
		return this.getInstance().getInt(key, value);
	}

	/**
	 * @return The snooze duration.
	 */
	public int getSnoozeDuration()
	{
		String key = this.getKeys().getSnoozeDuration();

		return this.getInstance().getInt(key,
			this.getDefaults().getSnoozeDurationIndex());
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
		int value = this.getDefaults().getSpeakFrequencyIndex();
		return this.getInstance().getInt(key, value);
	}

	/**
	 * @see getSpeakFrequencySummary
	 */
	public String getSpeakFrequencySummary()
	{
		Context context = this.getContext();
		int freq = this.getSpeakFrequency();
		return NacSharedPreferences.getSpeakFrequencySummary(context, freq);
	}

	/**
	 * @return The summary text to use when displaying the speak frequency
	 *         widget.
	 */
	public static String getSpeakFrequencySummary(Context context, int freq)
	{
		NacSharedConstants cons = new NacSharedConstants(context);
		String value = String.valueOf(freq);

		if (freq == 0)
		{
			return cons.getFrequencyOnce();
		}
		else
		{
			return String.format("%1$s %2$s %3$s", cons.getFrequencyInterval(),
				value, (freq == 1) ? " minute" : " minutes");
		}
	}

	/**
	 * @return The speak to me value.
	 */
	public boolean getSpeakToMe()
	{
		String key = this.getKeys().getSpeakToMe();
		boolean value = this.getDefaults().getSpeakToMe();
		return this.getInstance().getBoolean(key, value);
	}

	/**
	 * @return The value indicating which day to start on.
	 */
	public int getStartWeekOn()
	{
		String key = this.getKeys().getStartWeekOn();
		int value = this.getDefaults().getStartWeekOnIndex();
		return this.getInstance().getInt(key, value);
	}

	/**
	 * @return The theme color.
	 */
	public int getThemeColor()
	{
		String key = this.getKeys().getThemeColor();
		int value = this.getDefaults().getThemeColor();
		return this.getInstance().getInt(key, value);
	}

	/**
	 * @return The time color.
	 */
	public int getTimeColor()
	{
		String key = this.getKeys().getTimeColor();
		int value = this.getDefaults().getTimeColor();
		return this.getInstance().getInt(key, value);
	}

	/**
	 * @return True to display upcoming alarm notifications, and False otherwise.
	 */
	public boolean getUpcomingAlarmNotification()
	{
		String key = this.getKeys().getUpcomingAlarmNotification();
		boolean value = this.getDefaults().getUpcomingAlarm();
		return this.getInstance().getBoolean(key, value);
	}

	/**
	 * @return Whether NFC is required or not.
	 */
	public boolean getUseNfc()
	{
		String key = this.getKeys().getUseNfc();
		boolean value = this.getDefaults().getUseNfc();
		return this.getInstance().getBoolean(key, value);
	}

	/**
	 * @return Whether the alarm should vibrate the phone or not.
	 */
	public boolean getVibrate()
	{
		String key = this.getKeys().getVibrate();
		boolean value = this.getDefaults().getVibrate();
		return this.getInstance().getBoolean(key, value);
	}

	/**
	 * @return The alarm volume level.
	 */
	public int getVolume()
	{
		String key = this.getKeys().getVolume();
		int value = this.getDefaults().getVolume();
		return this.getInstance().getInt(key, value);
	}

	/**
	 * Increment the rate my app counter.
	 */
	public void incrementRateMyApp()
	{
		int counter = this.getRateMyAppCounter();
		this.editRateMyAppCounter(counter+1);
	}

	/**
	 * @return True if app has reached the counter limit, and False otherwise.
	 */
	public boolean isRateMyAppLimit()
	{
		int counter = this.getRateMyAppCounter();
		int limit = this.getDefaults().getRateMyAppLimit();
		return counter == limit;
	}

	/**
	 * @return True if app has been rated, and False otherwise.
	 */
	public boolean isRateMyAppRated()
	{
		int counter = this.getRateMyAppCounter();
		int rated = this.getDefaults().getRateMyAppRated();
		return counter == rated;
	}

	/**
	 * Reset the rate my app counter.
	 */
	public void resetRateMyApp()
	{
		int reset = this.getDefaults().getRateMyAppCounter();
		this.editRateMyAppCounter(reset);
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

	/**
	 * Set rate my app to the postpone value.
	 */
	public void setRateMyAppPostpone()
	{
		int postpone = -2 * this.getDefaults().getRateMyAppLimit();
		this.editRateMyAppCounter(postpone);
	}

	/**
	 * Set rate my app to the rated value.
	 */
	public void setRateMyAppRated()
	{
		int rated = this.getDefaults().getRateMyAppRated();
		this.editRateMyAppCounter(rated);
	}

}
