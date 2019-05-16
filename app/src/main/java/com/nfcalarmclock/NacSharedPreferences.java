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
	private final NacPreferenceKeys mKeys;

	/**
	 * Default auto dismiss duration.
	 */
	public static final int DEFAULT_AUTO_DISMISS = 15;

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
	 * Default require NFC.
	 */
	public static final boolean DEFAULT_REQUIRE_NFC = true;

	/**
	 * Default easy snooze.
	 */
	public static final boolean DEFAULT_EASY_SNOOZE = false;

	/**
	 * Default shuffle value.
	 */
	public static final boolean DEFAULT_SHUFFLE = false;

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
	public static final int DEFAULT_DAYS = NacCalendar.daysToValue(
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
		this.mKeys = new NacPreferenceKeys(context);
		this.mContext = context;
	}

	/**
	 * Edit the AM preference color.
	 */
	public void editAmColor(int color)
	{
		String key = this.getKeys().getAmColor();

		this.getInstance().edit().putInt(key, color).apply();
	}

	/**
	 * Edit the auto dismiss preference value.
	 */
	public void editAutoDismiss(int index)
	{
		String key = this.getKeys().getAutoDismiss();

		this.getInstance().edit().putInt(key, index).apply();
	}

	/**
	 * Edit the days preference value.
	 */
	public void editDays(int days)
	{
		String key = this.getKeys().getDays();

		this.getInstance().edit().putInt(key, days).apply();
	}

	/**
	 * Edit the days preference color.
	 */
	public void editDaysColor(int color)
	{
		String key = this.getKeys().getDaysColor();

		this.getInstance().edit().putInt(key, color).apply();
	}

	/**
	 * Edit the easy snooze preference value.
	 */
	public void editEasySnooze(boolean easy)
	{
		String key = this.getKeys().getEasySnooze();

		this.getInstance().edit().putBoolean(key, easy).apply();
	}

	/**
	 * Edit the max snooze preference value.
	 */
	public void editMaxSnooze(int max)
	{
		String key = this.getKeys().getMaxSnooze();

		this.getInstance().edit().putInt(key, max).apply();
	}

	/**
	 * Edit the alarm name preference value.
	 */
	public void editName(String name)
	{
		String key = this.getKeys().getName();

		this.getInstance().edit().putString(key, name).apply();
	}

	/**
	 * Edit the alarm name preference color.
	 */
	public void editNameColor(int color)
	{
		String key = this.getKeys().getNameColor();

		this.getInstance().edit().putInt(key, color).apply();
	}

	/**
	 * Edit the PM preference color.
	 */
	public void editPmColor(int color)
	{
		String key = this.getKeys().getPmColor();

		this.getInstance().edit().putInt(key, color).apply();
	}

	/**
	 * Edit the repeat preference value.
	 */
	public void editRepeat(boolean repeat)
	{
		String key = this.getKeys().getRepeat();

		this.getInstance().edit().putBoolean(key, repeat).apply();
	}

	/**
	 * Edit the require NFC preference value.
	 */
	public void editRequireNfc(boolean require)
	{
		String key = this.getKeys().getRequireNfc();

		this.getInstance().edit().putBoolean(key, require).apply();
	}

	/**
	 * Edit the snooze count value.
	 */
	public void editSnoozeCount(int id, int count)
	{
		String key = this.getKeys().getSnoozeCount(id);

		this.getSharedPreferences().edit().putInt(key, count).apply();
	}

	/**
	 * Edit the snooze duration preference value.
	 */
	public void editSnoozeDuration(int duration)
	{
		String key = this.getKeys().getSnoozeDuration();

		this.getInstance().edit().putInt(key, duration).apply();
	}

	/**
	 * Edit the sound preference value.
	 */
	public void editSound(String path)
	{
		String key = this.getKeys().getSound();

		this.getInstance().edit().putString(key, path).apply();
	}

	/**
	 * Edit the theme color preference value.
	 */
	public void editThemeColor(int color)
	{
		String key = this.getKeys().getThemeColor();

		this.getInstance().edit().putInt(key, color).apply();
	}

	/**
	 * Edit the time color preference value.
	 */
	public void editTimeColor(int color)
	{
		String key = this.getKeys().getTimeColor();

		this.getInstance().edit().putInt(key, color).apply();
	}

	/**
	 * Edit the vibrate preference value.
	 */
	public void editVibrate(boolean vibrate)
	{
		String key = this.getKeys().getVibrate();

		this.getInstance().edit().putBoolean(key, vibrate).apply();
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
		String days = NacCalendar.toString(value);

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
	public NacPreferenceKeys getKeys()
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
	 * @return Whether NFC is required or not.
	 */
	public boolean getRequireNfc()
	{
		String key = this.getKeys().getRequireNfc();

		return this.getSharedPreferences().getBoolean(key, DEFAULT_REQUIRE_NFC);
	}

	/**
	 * @return The SharedPreferences object.
	 */
	public SharedPreferences getSharedPreferences()
	{
		return this.mSharedPreferences;
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
		return (!path.isEmpty())
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
			name += " Playlist";
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
	 * @return Whether the alarm should vibrate the phone or not.
	 */
	public boolean getVibrate()
	{
		String key = this.getKeys().getVibrate();

		return this.getSharedPreferences().getBoolean(key, DEFAULT_VIBRATE);
	}

}
