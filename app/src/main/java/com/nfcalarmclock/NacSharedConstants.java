package com.nfcalarmclock;

import android.content.Context;
import android.content.res.Resources;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.EnumSet;
import java.util.List;
import java.util.Locale;

/**
 * Constants container.
 */
public class NacSharedConstants
	extends NacSharedResource
{

	/**
	 */
	public NacSharedConstants(Context context)
	{
		super(context);
	}

	/**
	 */
	public NacSharedConstants(Resources res)
	{
		super(res);
	}

	/**
	 * @return The app name.
	 */
	public String getAppName()
	{
		return this.getString(R.string.app_name);
	}

	/**
	 * @return The audio sources.
	 */
	public List<String> getAudioSources()
	{
		List<String> sources = new ArrayList<>();
		sources.add(this.getString(R.string.audio_source_alarm));
		sources.add(this.getString(R.string.audio_source_media));
		sources.add(this.getString(R.string.audio_source_notification));
		sources.add(this.getString(R.string.audio_source_ringtone));
		sources.add(this.getString(R.string.audio_source_system));
		return sources;
	}

	/**
	 * @return Auto dismiss.
	 */
	public String getAutoDismiss()
	{
		return this.getString(R.string.auto_dismiss);
	}

	/**
	 * @return Browse.
	 */
	public String getBrowse()
	{
		return this.getString(R.string.browse);
	}

	/**
	 * @return Cancel.
	 */
	public String getCancel()
	{
		return this.getString(R.string.prompt_cancel);
	}

	/**
	 * @return Clear.
	 */
	public String getClear()
	{
		return this.getString(R.string.prompt_clear);
	}

	/**
	 * @return The color hint.
	 */
	public String getColorHint()
	{
		return this.getString(R.string.color_hint);
	}

	/**
	 * @return Copied alarm.
	 */
	public String getCopiedAlarm()
	{
		return this.getString(R.string.copied_alarm);
	}

	/**
	 * @return The days of week.
	 */
	public List<String> getDaysOfWeek()
	{
		String sun = this.getString(R.string.dow_sunday);
		String mon = this.getString(R.string.dow_monday);
		String tue = this.getString(R.string.dow_tuesday);
		String wed = this.getString(R.string.dow_wednesday);
		String thu = this.getString(R.string.dow_thursday);
		String fri = this.getString(R.string.dow_friday);
		String sat = this.getString(R.string.dow_saturday);

		return Arrays.asList(sun, mon, tue, wed, thu, fri, sat);
	}

	/**
	 * @return The days of week abbreviated.
	 */
	public List<String> getDaysOfWeekAbbr()
	{
		String sun = this.getString(R.string.dow_sun);
		String mon = this.getString(R.string.dow_mon);
		String tue = this.getString(R.string.dow_tue);
		String wed = this.getString(R.string.dow_wed);
		String thu = this.getString(R.string.dow_thu);
		String fri = this.getString(R.string.dow_fri);
		String sat = this.getString(R.string.dow_sat);

		return Arrays.asList(sun, mon, tue, wed, thu, fri, sat);
	}

	/**
	 * @return Default.
	 */
	public String getDefault()
	{
		return this.getString(R.string.prompt_default);
	}

	/**
	 * @return Deleted alarm.
	 */
	public String getDeletedAlarm()
	{
		return this.getString(R.string.deleted_alarm);
	}

	/**
	 * @return Dismiss.
	 */
	public String getDismiss()
	{
		return this.getString(R.string.dismiss_alarm);
	}

	/**
	 * @return Dismissed alarm.
	 */
	public String getDismissedAlarm()
	{
		return this.getString(R.string.dismissed_alarm);
	}

	/**
	 * @return Everyday.
	 */
	public String getEveryday()
	{
		return this.getString(R.string.dow_everyday);
	}

	/**
	 * @return Folder selected.
	 */
	public String getFolderSelected()
	{
		return this.getString(R.string.folder_selected);
	}

	/**
	 * @return Frequency interval.
	 */
	public String getFrequencyInterval()
	{
		return this.getString(R.string.frequency_interval);
	}

	/**
	 * @return Frequency once.
	 */
	public String getFrequencyOnce()
	{
		return this.getString(R.string.frequency_once);
	}

	/**
	 * @return Max alarm error.
	 */
	public String getMaxAlarmsError()
	{
		return this.getString(R.string.max_alarms_error);
	}

	/**
	 * @return Max snooze.
	 */
	public String getMaxSnooze()
	{
		return this.getString(R.string.max_snooze);
	}

	/**
	 * @return The media hint.
	 */
	public String getMediaHint()
	{
		return this.getString(R.string.media_hint);
	}

	/**
	 * @return Monday.
	 */
	public String getMonday()
	{
		return this.getString(R.string.dow_monday);
	}

	/**
	 * @return The alarm name example.
	 */
	public String getNameExample()
	{
		return this.getString(R.string.name_example);
	}

	/**
	 * @return The name hint.
	 */
	public String getNameHint()
	{
		return this.getString(R.string.name_hint);
	}

	/**
	 * @return NFC request.
	 */
	public String getNfcRequest()
	{
		return this.getString(R.string.nfc_request);
	}

	/**
	 * @return NFC required message.
	 */
	public String getNfcRequired()
	{
		return this.getString(R.string.nfc_required);
	}

	/**
	 * @return NFC unsupported message.
	 */
	public String getNfcUnsupported()
	{
		return this.getString(R.string.nfc_unsupported);
	}

	/**
	 * @return None.
	 */
	public String getNone()
	{
		return this.getString(R.string.none);
	}

	/**
	 * @return Ok.
	 */
	public String getOk()
	{
		return this.getString(R.string.prompt_ok);
	}

	/**
	 * @return Play audio error.
	 */
	public String getPlayAudioError()
	{
		return this.getString(R.string.play_audio_error);
	}

	/**
	 * @return Play file error.
	 */
	public String getPlayFileError()
	{
		return this.getString(R.string.play_file_error);
	}

	/**
	 * @return Previous folder.
	 */
	public String getPreviousFolder()
	{
		return this.getString(R.string.previous_folder);
	}

	/**
	 * @return Rate later.
	 */
	public String getRateLater()
	{
		return this.getString(R.string.rate_later);
	}

	/**
	 * @return Rate never.
	 */
	public String getRateNever()
	{
		return this.getString(R.string.rate_never);
	}

	/**
	 * @return Rate now.
	 */
	public String getRateNow()
	{
		return this.getString(R.string.rate_now);
	}

	/**
	 * @return Rate title.
	 */
	public String getRateTitle()
	{
		return this.getString(R.string.rate_title);
	}

	/**
	 * @return Restored alarm.
	 */
	public String getRestoredAlarm()
	{
		return this.getString(R.string.restored_alarm);
	}

	/**
	 * @return Select audio source.
	 */
	public String getSelectAudioSource()
	{
		return this.getString(R.string.select_audio_source);
	}

	/**
	 * @return Select color.
	 */
	public String getSelectColor()
	{
		return this.getString(R.string.select_color);
	}

	/**
	 * @return Select color error message.
	 */
	public String getSelectColorError()
	{
		return this.getString(R.string.select_color_error);
	}

	/**
	 * @return Select days.
	 */
	public String getSelectDays()
	{
		return this.getString(R.string.select_days);
	}

	/**
	 * @return Select format.
	 */
	public String getSelectFormat()
	{
		return this.getString(R.string.select_format);
	}

	/**
	 * @return Select speak frequency.
	 */
	public String getSelectSpeakFrequency()
	{
		return this.getString(R.string.select_speak_frequency);
	}

	/**
	 * @return Set alarm name.
	 */
	public String getSetAlarmName()
	{
		return this.getString(R.string.set_alarm_name);
	}

	/**
	 * @return Settings.
	 */
	public String getSettings()
	{
		return this.getString(R.string.settings);
	}

	/**
	 * @return Snooze.
	 */
	public String getSnooze()
	{
		return this.getString(R.string.snooze_alarm);
	}

	/**
	 * @return Snoozed alarm.
	 */
	public String getSnoozedAlarm()
	{
		return this.getString(R.string.snoozed_alarm);
	}

	/**
	 * @return Snooze duration.
	 */
	public String getSnoozeDuration()
	{
		return this.getString(R.string.snooze_duration);
	}

	/**
	 * @return Snooze error message.
	 */
	public String getSnoozeError()
	{
		return this.getString(R.string.snooze_error);
	}

	/**
	 * @return Snoozed days error message.
	 */
	public String getSnoozedDaysError()
	{
		return this.getString(R.string.snoozed_days_error);
	}

	/**
	 * @return Snoozed delete error message.
	 */
	public String getSnoozedDeleteError()
	{
		return this.getString(R.string.snoozed_delete_error);
	}

	/**
	 * @return Snoozed modify error message.
	 */
	public String getSnoozedModifyError()
	{
		return this.getString(R.string.snoozed_modify_error);
	}

	/**
	 * @return Speak frequency.
	 */
	public String getSpeakFrequency()
	{
		return this.getString(R.string.speak_frequency);
	}

	/**
	 * @return Speak to me words.
	 */
	public String getSpeakToMe(Context context)
	{
		Locale locale = Locale.getDefault();
		String lang = locale.getLanguage();
		Calendar calendar = Calendar.getInstance();
		int hour = calendar.get(Calendar.HOUR_OF_DAY);
		int minute = calendar.get(Calendar.MINUTE);
		String meridian = NacCalendar.Time.getMeridian(context, hour);

		if (lang.equals("es"))
		{
			return this.getSpeakToMeEs(hour, minute, meridian);
		}
		else
		{
			return this.getSpeakToMeEn(hour, minute, meridian);
		}
	}

	/**
	 * @see getSpeakToMe
	 */
	public String getSpeakToMeEn(int hour, int minute, String meridian)
	{
		Locale locale = Locale.getDefault();
		String oh = (minute > 0) && (minute < 10) ? "O" : "";
		String showMinute = minute == 0 ? "" : String.valueOf(minute);

		if ((meridian != null) && !meridian.isEmpty())
		{
			hour = NacCalendar.Time.to12HourFormat(hour);
		}

		return String.format(locale,
			", , The time, is, %1$d, %2$s, %3$s, %4$s",
			hour, oh, showMinute, meridian);
	}

	/**
	 * @see getSpeakToMe
	 */
	public String getSpeakToMeEs(int hour, int minute, String meridian)
	{
		Locale locale = Locale.getDefault();

		if ((meridian == null) || meridian.isEmpty())
		{
			String plural = minute != 1 ? "s" : "";
			return String.format(locale,
				", , Es, la hora, %1$d, con, %2$d, minuto%3$s",
				hour, minute, plural);
		}
		else
		{
			hour = NacCalendar.Time.to12HourFormat(hour);
			String theTimeIs = (hour == 1) ? "Es, la," : "Son, las,";
			String showMinute = minute == 0 ? "" : String.valueOf(minute);

			return String.format(locale,
				", , %1$s %2$d, %3$s, %4$s",
				theTimeIs, hour, showMinute, meridian);
		}
	}

	/**
	 * @return Start week on title.
	 */
	public String getStartWeekOnTitle()
	{
		return this.getString(R.string.start_week_on);
	}

	/**
	 * @return Sunday.
	 */
	public String getSunday()
	{
		return this.getString(R.string.dow_sunday);
	}

	/**
	 * @return Today.
	 */
	public String getToday()
	{
		return this.getString(R.string.dow_today);
	}

	/**
	 * @return Tomorrow.
	 */
	public String getTomorrow()
	{
		return this.getString(R.string.dow_tomorrow);
	}

	/**
	 * @return Undo.
	 */
	public String getUndo()
	{
		return this.getString(R.string.undo);
	}

	/**
	 * @return Unknown.
	 */
	public String getUnknown()
	{
		return this.getString(R.string.unknown);
	}

	/**
	 * @return Weekdays.
	 */
	public String getWeekdays()
	{
		return this.getString(R.string.dow_weekdays);
	}

	/**
	 * @return Weekend.
	 */
	public String getWeekend()
	{
		return this.getString(R.string.dow_weekend);
	}

	/**
	 * @return Will run.
	 */
	public String getWillRun()
	{
		return this.getString(R.string.will_run);
	}

}
