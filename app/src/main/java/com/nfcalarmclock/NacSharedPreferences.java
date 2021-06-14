package com.nfcalarmclock;

import android.content.Context;
import android.content.SharedPreferences;
import androidx.preference.PreferenceManager;
import java.util.List;
import java.util.Locale;

/**
 * Container for the values of each preference.
 */
@SuppressWarnings("RedundantSuppression")
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
	 * Keys.
	 */
	private final NacSharedKeys mKeys;

	/**
	 * Defaults.
	 */
	private final NacSharedDefaults mDefaults;

	/**
	 * Constants.
	 */
	private final NacSharedConstants mConstants;

	/**
	 */
	public NacSharedPreferences(Context context)
	{
		this.mContext = context;
		this.mInstance = PreferenceManager.getDefaultSharedPreferences(context);
		this.mKeys = new NacSharedKeys(context);
		this.mDefaults = new NacSharedDefaults(context);
		this.mConstants = new NacSharedConstants(context);
	}

	/**
	 * Edit the AM color, in the time, of all alarm cards.
	 */
	@SuppressWarnings("unused")
	public void editAmColor(int color)
	{
		String key = this.getKeys().getAmColor();
		this.saveInt(key, color, false);
	}

	/**
	 * Edit whether this is the app's first run or not.
	 */
	public void editAppFirstRun(boolean first)
	{
		String key = this.getKeys().getAppFirstRun();
		this.saveBoolean(key, first, false);
	}

	/**
	 * Edit the default audio source to use when a new alarm card is created.
	 *
	 * This can be changed for an alarm by clicking the audio settings button.
	 */
	public void editAudioSource(String source)
	{
		String key = this.getKeys().getAudioSource();
		this.saveString(key, source, false);
	}

	/**
	 * Edit the index corresponding to a time in which to auto dismiss the alarm.
	 */
	@SuppressWarnings("unused")
	public void editAutoDismiss(int index)
	{
		String key = this.getKeys().getAutoDismiss();
		this.saveInt(key, index, false);
	}

	/**
	 * Edit the height of the alarm card when it is collapsed.
	 */
	public void editCardHeightCollapsed(int height)
	{
		String key = this.getKeys().getCardHeightCollapsed();
		this.saveInt(key, height, false);
	}

	/**
	 * Edit the height of the alarm card height it is collapsed, but the dismiss
	 * button is showing.
	 */
	public void editCardHeightCollapsedDismiss(int height)
	{
		String key = this.getKeys().getCardHeightCollapsedDismiss();
		this.saveInt(key, height, false);
	}

	/**
	 * Edit the height of the alarm card when it is expanded.
	 */
	public void editCardHeightExpanded(int height)
	{
		String key = this.getKeys().getCardHeightExpanded();
		this.saveInt(key, height, false);
	}

	/**
	 * Edit the flag indicating if the alarm card has been measured or not.
	 */
	public void editCardIsMeasured(boolean isMeasured)
	{
		String key = this.getKeys().getCardIsMeasured();
		this.saveBoolean(key, isMeasured, false);
	}

	/**
	 * Edit the value indicating which day button style to use.
	 */
	@SuppressWarnings("unused")
	public void editDayButtonStyle(int style)
	{
		String key = this.getKeys().getDayButtonStyle();
		this.saveInt(key, style, false);
	}

	/**
	 * Edit the default days to use when a new alarm card is created.
	 */
	@SuppressWarnings("unused")
	public void editDays(int days)
	{
		String key = this.getKeys().getDays();
		this.saveInt(key, days, false);
	}

	/**
	 * Edit the color of the days an alarm will go off on.
	 * 
	 * This is displayed when an alarm card is collapsed.
	 */
	@SuppressWarnings("unused")
	public void editDaysColor(int color)
	{
		String key = this.getKeys().getDaysColor();
		this.saveInt(key, color, false);
	}

	/**
	 * Edit the flag indicating whether the user wants to snooze easily or not.
	 */
	@SuppressWarnings("unused")
	public void editEasySnooze(boolean easy)
	{
		String key = this.getKeys().getEasySnooze();
		this.saveBoolean(key, easy, false);
	}

	/**
	 * Edit the max number of times a user is able to snooze an alarm.
	 */
	@SuppressWarnings("unused")
	public void editMaxSnooze(int max)
	{
		String key = this.getKeys().getMaxSnooze();
		this.saveInt(key, max, false);
	}

	/**
	 * Edit the default media path to use when a new alarm card is created.
	 *
	 * This is the path to the media that should play when an alarm goes off.
	 */
	@SuppressWarnings("unused")
	public void editMediaPath(String path)
	{
		String key = this.getKeys().getMediaPath();
		this.saveString(key, path, false);
	}

	/**
	 * Edit the default name to use when a new alarm card is created.
	 */
	@SuppressWarnings("unused")
	public void editName(String name)
	{
		String key = this.getKeys().getName();
		this.saveString(key, name, false);
	}

	/**
	 * Edit the color of the name of an alarm.
	 * 
	 * This is displayed when an alarm card is collapsed.
	 */
	@SuppressWarnings("unused")
	public void editNameColor(int color)
	{
		String key = this.getKeys().getNameColor();
		this.saveInt(key, color, false);
	}

	/**
	 * Edit the time format to display the next alarm in.
	 */
	@SuppressWarnings("unused")
	public void editNextAlarmFormat(int format)
	{
		String key = this.getKeys().getNextAlarmFormat();
		this.saveInt(key, format, false);
	}

	/**
	 * Edit the PM color, in the time, of all alarm cards.
	 */
	@SuppressWarnings("unused")
	public void editPmColor(int color)
	{
		String key = this.getKeys().getPmColor();
		this.saveInt(key, color, false);
	}

	/**
	 * Edit the previous system volume, before an alarm goes off.
	 */
	public void editPreviousVolume(int previous)
	{
		String key = this.getKeys().getPreviousVolume();
		this.saveInt(key, previous, false);
	}

	/**
	 * Edit the counter that will indicate whether it is time to show dialog to
	 * Rate My App.
	 */
	public void editRateMyAppCounter(int counter)
	{
		String key = this.getKeys().getRateMyAppCounter();
		this.saveInt(key, counter, false);
	}

	/**
	 * Edit the default repeat value when a new alarm card is created.
	 */
	@SuppressWarnings("unused")
	public void editRepeat(boolean repeat)
	{
		String key = this.getKeys().getRepeat();
		this.saveBoolean(key, repeat, false);
	}

	/**
	 * Edit the current snooze count for an alarm with the given ID.
	 */
	public void editSnoozeCount(long id, int count)
	{
		String key = NacSharedKeys.getSnoozeCount(id);
		this.saveInt(key, count, false);
	}

	/**
	 * Edit the snooze duration preference value.
	 */
	@SuppressWarnings("unused")
	public void editSnoozeDuration(int duration)
	{
		String key = this.getKeys().getSnoozeDuration();
		this.saveInt(key, duration, false);
	}

	/**
	 * Edit the value indicating whether the main activity should be refreshed or
	 * not.
	 */
	public void editShouldRefreshMainActivity(boolean shouldRefresh)
	{
		String key = this.getKeys().getShouldRefreshMainActivity();
		this.saveBoolean(key, shouldRefresh, false);
	}

	/**
	 * Edit the frequency at which the text-to-speech should go off when an alarm
	 * is going off.
	 */
	@SuppressWarnings("unused")
	public void editSpeakFrequency(int freq)
	{
		String key = this.getKeys().getSpeakFrequency();
		this.saveInt(key, freq, false);
	}

	/**
	 * Edit the flag indicating whether text-to-speech should be used when an
	 * alarm goes off.
	 */
	@SuppressWarnings("unused")
	public void editSpeakToMe(boolean speak)
	{
		String key = this.getKeys().getSpeakToMe();
		this.saveBoolean(key, speak, false);
	}

	/**
	 * Edit the index indicating which day to start the week on.
	 */
	@SuppressWarnings("unused")
	public void editStartWeekOn(int start)
	{
		String key = this.getKeys().getStartWeekOn();
		this.saveInt(key, start, false);
	}

	/**
	 * Edit the theme color.
	 */
	@SuppressWarnings("unused")
	public void editThemeColor(int color)
	{
		String key = this.getKeys().getThemeColor();
		this.saveInt(key, color, false);
	}

	/**
	 * Edit the color of the time for all alarm cards.
	 */
	@SuppressWarnings("unused")
	public void editTimeColor(int color)
	{
		String key = this.getKeys().getTimeColor();
		this.saveInt(key, color, false);
	}

	/**
	 * Edit the default use NFC value when an alarm is created.
	 */
	@SuppressWarnings("unused")
	public void editUseNfc(boolean useNfc)
	{
		String key = this.getKeys().getUseNfc();
		this.saveBoolean(key, useNfc, false);

	}

	/**
	 * Edit the default vibrate value when an alarm is created.
	 */
	@SuppressWarnings("unused")
	public void editVibrate(boolean vibrate)
	{
		String key = this.getKeys().getVibrate();
		this.saveBoolean(key, vibrate, false);

	}

	/**
	 * @return The AM color.
	 */
	public int getAmColor()
	{
		String key = this.getKeys().getAmColor();
		int value = this.getDefaults().getAmColor();
		return this.getInt(key, value);
	}

	/**
	 * @return The app's first run value.
	 */
	public boolean getAppFirstRun()
	{
		String key = this.getKeys().getAppFirstRun();
		boolean value = this.getDefaults().getAppFirstRun();
		return this.getBoolean(key, value);
	}

	/**
	 * @return The audio source.
	 */
	public String getAudioSource()
	{
		String key = this.getKeys().getAudioSource();
		String value = this.getConstants().getAudioSources().get(1);
		return this.getString(key, value);
	}

	/**
	 * @return Auto dismiss duration.
	 */
	public int getAutoDismiss()
	{
		String key = this.getKeys().getAutoDismiss();
		int value = this.getDefaults().getAutoDismissIndex();

		return this.getInt(key, value);
	}

	/**
	 * @see #getAutoDismissSummary(Context, int)
	 */
	@SuppressWarnings("unused")
	public String getAutoDismissSummary()
	{
		NacSharedConstants cons = this.getConstants();
		int index = this.getAutoDismiss();

		return NacSharedPreferences.getAutoDismissSummary(cons, index);
	}

	/**
	 * @return The summary text to use when displaying the auto dismiss widget.
	 */
	public static String getAutoDismissSummary(NacSharedConstants cons, int index)
	{
		List<String> summaries = cons.getAutoDismissSummaries();
		return summaries.get(index);
	}

	/**
	 * @see #getAutoDismissTime(int)
	 */
	public int getAutoDismissTime()
	{
		int index = this.getAutoDismiss();
		return NacSharedPreferences.getAutoDismissTime(index);
	}

	/**
	 * @return Calculate the auto dismiss duration from an index value,
	 *     corresponding to a location in the spainner widget.
	 */
	public static int getAutoDismissTime(int index)
	{
		return (index < 5) ? index : (index-4)*5;
	}

	/**
	 * @return A boolean value from the SharedPreferences instance.
	 */
	public boolean getBoolean(String key, boolean defValue)
	{
		return this.getInstance().getBoolean(key, defValue);
	}

	/**
	 * @return The alarm card height when it is collapsed.
	 */
	public int getCardHeightCollapsed()
	{
		String key = this.getKeys().getCardHeightCollapsed();
		int value = this.getDefaults().getCardHeightCollapsed();
		return this.getInt(key, value);
	}

	/**
	 * @return The alarm card height when it is collapsed, with dismiss showing.
	 */
	public int getCardHeightCollapsedDismiss()
	{
		String key = this.getKeys().getCardHeightCollapsedDismiss();
		int value = this.getDefaults().getCardHeightCollapsedDismiss();
		return this.getInt(key, value);
	}

	/**
	 * @return The alarm card height when it is expanded.
	 */
	public int getCardHeightExpanded()
	{
		String key = this.getKeys().getCardHeightExpanded();
		int value = this.getDefaults().getCardHeightExpanded();
		return this.getInt(key, value);
	}

	/**
	 * @return True if the alarm card has been measured, and False otherwise.
	 */
	public boolean getCardIsMeasured()
	{
		String key = this.getKeys().getCardIsMeasured();
		boolean value = this.getDefaults().getCardIsMeasured();
		return this.getBoolean(key, value);
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
	 * @return Which style to use for the day buttons.
	 *
	 *         1: Represents using the filled-in buttons (Default)
	 *         2: Represents the outlined button style
	 */
	public int getDayButtonStyle()
	{
		String key = this.getKeys().getDayButtonStyle();
		int value = this.getDefaults().getDayButtonStyle();
		return this.getInt(key, value);
	}

	/**
	 * @return The alarm days.
	 */
	public int getDays()
	{
		String key = this.getKeys().getDays();
		int value = this.getDefaults().getDays();
		return this.getInt(key, value);
	}

	/**
	 * @return The days color.
	 */
	public int getDaysColor()
	{
		String key = this.getKeys().getDaysColor();
		int value = this.getDefaults().getDaysColor();
		return this.getInt(key, value);
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
		boolean value = this.getDefaults().getEasySnooze();
		return this.getBoolean(key, value);
	}

	/**
	 * @return Whether a new alarm card should be expanded or not.
	 */
	public boolean getExpandNewAlarm()
	{
		String key = this.getKeys().getExpandNewAlarm();
		boolean value = this.getDefaults().getExpandNewAlarm();
		return this.getBoolean(key, value);
	}

	/**
	 * @return The SharedPreferences instance.
	 */
	public SharedPreferences getInstance()
	{
		return this.mInstance;
	}

	/**
	 * @return An integer value from the SharedPreferences instance.
	 */
	public int getInt(String key, int defValue)
	{
		return this.getInstance().getInt(key, defValue);
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
		int value = this.getDefaults().getMaxSnoozeIndex();

		return this.getInt(key, value);
	}

	/**
	 * @see #getMaxSnoozeSummary(Context, int)
	 */
	@SuppressWarnings("unused")
	public String getMaxSnoozeSummary()
	{
		NacSharedConstants cons = this.getConstants();
		int index = this.getMaxSnooze();

		return NacSharedPreferences.getMaxSnoozeSummary(cons, index);
	}

	/**
	 * @return The summary text to use when displaying the max snooze widget.
	 */
	public static String getMaxSnoozeSummary(NacSharedConstants cons, int index)
	{
		List<String> summaries = cons.getMaxSnoozeSummaries();
		return summaries.get(index);
	}

	/**
	 * @see #getMaxSnoozeValue(int)
	 */
	public int getMaxSnoozeValue()
	{
		int index = this.getMaxSnooze();
		return NacSharedPreferences.getMaxSnoozeValue(index);
	}

	/**
	 * @return Calculate the max snooze duration from an index corresponding
	 *     to a location in the spinner widget.
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
		return this.getString(key, "");
	}

	/**
	 * @return The sound message.
	 */
	public static String getMediaMessage(Context context, String path)
	{
		NacSharedConstants cons = new NacSharedConstants(context);
		//return (path != null) && !path.isEmpty()
		return !NacFile.isEmpty(path)
			? NacMedia.getTitle(context, path)
			: cons.getDescriptionMedia();
	}

	/**
	 * @see #getMediaSummary(Context, String)
	 */
	@SuppressWarnings("unused")
	public String getMediaSummary()
	{
		Context context = this.getContext();
		String path = this.getMediaPath();
		return NacSharedPreferences.getMediaSummary(context, path);
	}

	/**
	 * @return The media summary.
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
		NacSharedConstants cons = this.getConstants();

		if (meridian.equals(cons.getAm()))
		{
			return this.getAmColor();
		}
		else if (meridian.equals(cons.getPm()))
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
		return this.getBoolean(key, value);
	}

	/**
	 * @return The name of the alarm.
	 */
	public String getName()
	{
		String key = this.getKeys().getName();
		return this.getString(key, "");
	}

	/**
	 * @return The name color.
	 */
	public int getNameColor()
	{
		String key = this.getKeys().getNameColor();
		int value = this.getDefaults().getNameColor();
		return this.getInt(key, value);
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
	 * @see #getNameSummary(Context, String)
	 */
	@SuppressWarnings("unused")
	public String getNameSummary()
	{
		Context context = getContext();
		String name = this.getName();
		return NacSharedPreferences.getNameSummary(context, name);
	}

	/**
	 * @return The name summary.
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
		return this.getInt(key, value);
	}

	/**
	 * @return The PM color.
	 */
	public int getPmColor()
	{
		String key = this.getKeys().getPmColor();
		int value = this.getDefaults().getPmColor();
		return this.getInt(key, value);
	}

	/**
	 * @return Whether you should prevent app from closing during an alarm
	 *         activity or not.
	 */
	public boolean getPreventAppFromClosing()
	{
		String key = this.getKeys().getPreventAppFromClosing();
		boolean value = this.getDefaults().getPreventAppFromClosing();
		return this.getBoolean(key, value);
	}

	/**
	 * @return The previous system volume, before an alarm goes off.
	 */
	public int getPreviousVolume()
	{
		String key = this.getKeys().getPreviousVolume();
		int value = this.getDefaults().getPreviousVolume();
		return this.getInt(key, value);
	}

	/**
	 * @return The app's rating counter.
	 */
	public int getRateMyAppCounter()
	{
		String key = this.getKeys().getRateMyAppCounter();
		int value = this.getDefaults().getRateMyAppCounter();
		return this.getInt(key, value);
	}

	/**
	 * @return Whether the alarm should be repeated or not.
	 */
	public boolean getRepeat()
	{
		String key = this.getKeys().getRepeat();
		boolean value = this.getDefaults().getRepeat();
		return this.getBoolean(key, value);
	}

	/**
	 * @return Whether the main activity should be refreshed or not.
	 */
	public boolean getShouldRefreshMainActivity()
	{
		String key = this.getKeys().getShouldRefreshMainActivity();
		boolean value = this.getDefaults().getShouldRefreshMainActivity();
		return this.getBoolean(key, value);
	}

	/**
	 * @return Whether the alarm information should be shown or not.
	 */
	public boolean getShowAlarmInfo()
	{
		String key = this.getKeys().getShowAlarmInfo();
		boolean value = this.getDefaults().getShowAlarmInfo();
		return this.getBoolean(key, value);
	}

	/**
	 * @return The shuffle status.
	 */
	public boolean getShuffle()
	{
		String key = this.getKeys().getShuffle();
		boolean value = this.getDefaults().getShufflePlaylist();

		return this.getBoolean(key, value);
	}

	/**
	 * @return The snooze count.
	 */
	public int getSnoozeCount(long id)
	{
		String key = NacSharedKeys.getSnoozeCount(id);
		int value = this.getDefaults().getSnoozeCount();

		return this.getInt(key, value);
	}

	/**
	 * @return The snooze duration.
	 */
	public int getSnoozeDuration()
	{
		String key = this.getKeys().getSnoozeDuration();
		int value = this.getDefaults().getSnoozeDurationIndex();

		return this.getInt(key, value);
	}

	/**
	 * @see #getSnoozeDurationSummary(Context, int)
	 */
	@SuppressWarnings("unused")
	public String getSnoozeDurationSummary()
	{
		NacSharedConstants cons = this.getConstants();
		int index = this.getSnoozeDuration();

		return NacSharedPreferences.getSnoozeDurationSummary(cons, index);
	}

	/**
	 * @return The summary text for the snooze duration widget.
	 */
	public static String getSnoozeDurationSummary(NacSharedConstants cons, int index)
	{
		List<String> summaries = cons.getSnoozeDurationSummaries();
		return summaries.get(index);
	}

	/**
	 * @see #getSnoozeDurationValue(int)
	 */
	public int getSnoozeDurationValue()
	{
		int index = this.getSnoozeDuration();
		return NacSharedPreferences.getSnoozeDurationValue(index);
	}

	/**
	 * @return Calculate the snooze duration from an index value, corresponding
	 *     to a location in the spainner widget.
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
		return this.getInt(key, value);
	}

	/**
	 * @return The speak to me value.
	 */
	public boolean getSpeakToMe()
	{
		String key = this.getKeys().getSpeakToMe();
		boolean value = this.getDefaults().getSpeakToMe();
		return this.getBoolean(key, value);
	}

	/**
	 * @return The value indicating which day to start on.
	 */
	public int getStartWeekOn()
	{
		String key = this.getKeys().getStartWeekOn();
		int value = this.getDefaults().getStartWeekOnIndex();
		return this.getInt(key, value);
	}

	/**
	 * @return A string value from the SharedPreferences instance.
	 */
	public String getString(String key, String defValue)
	{
		return this.getInstance().getString(key, defValue);
	}

	/**
	 * @return The theme color.
	 */
	public int getThemeColor()
	{
		String key = this.getKeys().getThemeColor();
		int value = this.getDefaults().getThemeColor();
		return this.getInt(key, value);
	}

	/**
	 * @return The time color.
	 */
	public int getTimeColor()
	{
		String key = this.getKeys().getTimeColor();
		int value = this.getDefaults().getTimeColor();
		return this.getInt(key, value);
	}

	/**
	 * @return True to display upcoming alarm notifications, and False otherwise.
	 */
	public boolean getUpcomingAlarmNotification()
	{
		String key = this.getKeys().getUpcomingAlarmNotification();
		boolean value = this.getDefaults().getUpcomingAlarm();
		return this.getBoolean(key, value);
	}

	/**
	 * @return Whether NFC is required or not.
	 */
	public boolean getUseNfc()
	{
		String key = this.getKeys().getUseNfc();
		boolean value = this.getDefaults().getUseNfc();
		return this.getBoolean(key, value);
	}

	/**
	 * @return Whether the alarm should vibrate the phone or not.
	 */
	public boolean getVibrate()
	{
		String key = this.getKeys().getVibrate();
		boolean value = this.getDefaults().getVibrate();
		return this.getBoolean(key, value);
	}

	/**
	 * @return The alarm volume level.
	 */
	public int getVolume()
	{
		String key = this.getKeys().getVolume();
		int value = this.getDefaults().getVolume();
		return this.getInt(key, value);
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
	 * Set the rate my app counter to the postpone value.
	 */
	public void postponeRateMyApp()
	{
		int postpone = -2 * this.getDefaults().getRateMyAppLimit();
		this.editRateMyAppCounter(postpone);
	}

	/**
	 * Set the rate my app counter to the rated value.
	 */
	public void ratedRateMyApp()
	{
		int rated = this.getDefaults().getRateMyAppRated();
		this.editRateMyAppCounter(rated);
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
	 * @see #save(SharedPreferences.Editor, boolean)
	 */
	@SuppressWarnings("unused")
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
	 * Save a boolean to the shared preference.
	 */
	public void saveBoolean(String key, boolean value, boolean commit)
	{
		SharedPreferences.Editor editor = this.getInstance().edit()
			.putBoolean(key, value);

		this.save(editor, commit);
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
	 * Save a string to the shared preference.
	 */
	public void saveString(String key, String value, boolean commit)
	{
		SharedPreferences.Editor editor = this.getInstance().edit()
			.putString(key, value);

		this.save(editor, commit);
	}

}
