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
	 * @return Browse action.
	 */
	public String getActionBrowse()
	{
		return this.getString(R.string.action_browse);
	}

	/**
	 * @return Cancel action.
	 */
	public String getActionCancel()
	{
		return this.getString(R.string.action_cancel);
	}

	/**
	 * @return Clear action.
	 */
	public String getActionClear()
	{
		return this.getString(R.string.action_clear);
	}

	/**
	 * @return Default action.
	 */
	public String getActionDefault()
	{
		return this.getString(R.string.action_default);
	}

	/**
	 * @return Dismiss action.
	 */
	public String getActionDismiss()
	{
		return this.getString(R.string.action_alarm_dismiss);
	}

	/**
	 * @return Ok action.
	 */
	public String getActionOk()
	{
		return this.getString(R.string.action_ok);
	}

	/**
	 * @return Previous folder action.
	 */
	public String getActionPreviousFolder()
	{
		return this.getString(R.string.action_previous_folder);
	}

	/**
	 * @return Rate later action.
	 */
	public String getActionRateLater()
	{
		return this.getString(R.string.action_rate_later);
	}

	/**
	 * @return Rate never action.
	 */
	public String getActionRateNever()
	{
		return this.getString(R.string.action_rate_never);
	}

	/**
	 * @return Rate now action.
	 */
	public String getActionRateNow()
	{
		return this.getString(R.string.action_rate_now);
	}

	/**
	 * @return Snooze action.
	 */
	public String getActionSnooze()
	{
		return this.getString(R.string.action_alarm_snooze);
	}

	/**
	 * @return Undo action.
	 */
	public String getActionUndo()
	{
		return this.getString(R.string.action_undo);
	}

	/**
	 * @return Active notification.
	 */
	public String getActiveNotification()
	{
		return this.getString(R.string.active_alarm_plural);
	}

	/**
	 * @return Alarm word.
	 */
	public String getAlarm(int quantity)
	{
		return this.getPluralString(R.plurals.alarm, quantity);
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
	 * @return Active notification description.
	 */
	public String getDescriptionActiveNotification()
	{
		return this.getString(R.string.description_active_alarm);
	}

	/**
	 * @return The media description.
	 */
	public String getDescriptionMedia()
	{
		return this.getString(R.string.description_media);
	}

	/**
	 * @return Missed notification description.
	 */
	public String getDescriptionMissedNotification()
	{
		return this.getString(R.string.description_missed_alarm);
	}

	/**
	 * @return Upcoming notification description.
	 */
	public String getDescriptionUpcomingNotification()
	{
		return this.getString(R.string.description_upcoming_alarm);
	}

	/**
	 * @return Max alarm error message.
	 */
	public String getErrorMessageMaxAlarms()
	{
		return this.getString(R.string.error_message_max_alarms);
	}

	/**
	 * @return NFC is unsupported error message.
	 */
	public String getErrorMessageNfcUnsupported()
	{
		return this.getString(R.string.error_message_nfc_unsupported);
	}

	/**
	 * @return Play audio error message.
	 */
	public String getErrorMessagePlayAudio()
	{
		return this.getString(R.string.error_message_play_audio);
	}

	/**
	 * @return Play file error message.
	 */
	public String getErrorMessagePlayFile()
	{
		return this.getString(R.string.error_message_play_file);
	}

	/**
	 * @return Select color error message.
	 */
	public String getErrorMessageSelectColor()
	{
		return this.getString(R.string.error_message_select_color);
	}

	/**
	 * @return Snooze error message.
	 */
	public String getErrorMessageSnooze()
	{
		return this.getString(R.string.error_message_snooze);
	}

	/**
	 * @return Error message when modifying days while snoozed.
	 */
	public String getErrorMessageSnoozedDays()
	{
		return this.getString(R.string.error_message_snoozed_days);
	}

	/**
	 * @return Error message when trying to delete alarm while snoozed.
	 */
	public String getErrorMessageSnoozedDelete()
	{
		return this.getString(R.string.error_message_snoozed_delete);
	}

	/**
	 * @return Error message when trying to modify alarm while snoozed.
	 */
	public String getErrorMessageSnoozedModify()
	{
		return this.getString(R.string.error_message_snoozed_modify);
	}

	/**
	 * @return Everyday.
	 */
	public String getEveryday()
	{
		return this.getString(R.string.dow_everyday);
	}

	/**
	 * @return The alarm name example.
	 */
	public String getExampleName()
	{
		return this.getString(R.string.example_name);
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
	 * @return Is disabled.
	 */
	public String getIsDisabled()
	{
		return this.getString(R.string.is_disabled);
	}

	/**
	 * @return Max alarms.
	 */
	public int getMaxAlarms()
	{
		return this.getInteger(R.integer.max_alarms);
	}

	/**
	 * @return Max snooze.
	 */
	public String getMaxSnooze()
	{
		return this.getString(R.string.max_snooze);
	}

	/**
	 * @return Message when an alarm is copied.
	 */
	public String getMessageAlarmCopy()
	{
		return this.getString(R.string.message_alarm_copy);
	}

	/**
	 * @return Message when an alarm is deleted.
	 */
	public String getMessageAlarmDelete()
	{
		return this.getString(R.string.message_alarm_delete);
	}

	/**
	 * @return Message when an alarm is dismissed.
	 */
	public String getMessageAlarmDismiss()
	{
		return this.getString(R.string.message_alarm_dismiss);
	}

	/**
	 * @return Message when an alarm is restored.
	 */
	public String getMessageAlarmRestore()
	{
		return this.getString(R.string.message_alarm_restore);
	}

	/**
	 * @return Message when an alarm is snoozed.
	 */
	public String getMessageAlarmSnooze()
	{
		return this.getString(R.string.message_alarm_snooze);
	}

	/**
	 * @return Enable NFC request message.
	 */
	public String getMessageNfcRequest()
	{
		return this.getString(R.string.message_nfc_request);
	}

	/**
	 * @return NFC is required message.
	 */
	public String getMessageNfcRequired()
	{
		return this.getString(R.string.message_nfc_required);
	}

	/**
	 * @return No alarms scheduled message.
	 */
	public String getMessageNoAlarmsScheduled()
	{
		return this.getString(R.string.message_no_alarms_scheduled);
	}

	/**
	 * @return Name length.
	 */
	public int getMessageNameLength()
	{
		return this.getInteger(R.integer.max_message_name_length);
	}

	/**
	 * @return Missed alarm.
	 */
	public String getMissedAlarm(int quantity)
	{
		return this.getPluralString(R.plurals.missed_alarm, quantity);
	}

	/**
	 * @return Missed notification.
	 */
	public String getMissedNotification()
	{
		return this.getString(R.string.missed_alarm_plural);
	}

	/**
	 * @return Monday.
	 */
	public String getMonday()
	{
		return this.getString(R.string.dow_monday);
	}

	/**
	 * @return Name.
	 */
	public String getName()
	{
		return this.getString(R.string.alarm_name);
	}
	
	/**
	 * @return Next alarm.
	 */
	public String getNextAlarm()
	{
		return this.getString(R.string.next_alarm);
	}

	/**
	 * @return None.
	 */
	public String getNone()
	{
		return this.getString(R.string.none);
	}

	/**
	 * @return Settings.
	 */
	public String getSettings()
	{
		return this.getString(R.string.settings);
	}

	/**
	 * @return Snooze duration.
	 */
	public String getSnoozeDuration()
	{
		return this.getString(R.string.snooze_duration);
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
	 * @return Off state.
	 */
	public String getStateOff()
	{
		return this.getString(R.string.state_off);
	}

	/**
	 * @return Unknown state.
	 */
	public String getStateUnknown()
	{
		return this.getString(R.string.state_unknown);
	}

	/**
	 * @return Sunday.
	 */
	public String getSunday()
	{
		return this.getString(R.string.dow_sunday);
	}

	/**
	 * @return Time in.
	 */
	public String getTimeIn()
	{
		return this.getString(R.string.time_in);
	}

	/**
	 * @return Time on.
	 */
	public String getTimeOn()
	{
		return this.getString(R.string.time_on);
	}

	/**
	 * @return Select audio source title.
	 */
	public String getTitleAudioSource()
	{
		return this.getString(R.string.title_audio_source);
	}

	/**
	 * @return Select color title.
	 */
	public String getTitleColor()
	{
		return this.getString(R.string.title_color);
	}

	/**
	 * @return Select days title.
	 */
	public String getTitleDays()
	{
		return this.getString(R.string.title_days);
	}

	/**
	 * @return Folder selected title.
	 */
	public String getTitleFolderSelected()
	{
		return this.getString(R.string.title_folder_selected);
	}

	/**
	 * @return Set alarm name title.
	 */
	public String getTitleName()
	{
		return this.getString(R.string.title_name);
	}

	/**
	 * @return Select next alarm format title.
	 */
	public String getTitleNextAlarmFormat()
	{
		return this.getString(R.string.title_next_alarm_format);
	}

	/**
	 * @return Rate my app title.
	 */
	public String getTitleRateMyApp()
	{
		return this.getString(R.string.title_rate_my_app);
	}

	/**
	 * @return Select speak frequency title.
	 */
	public String getTitleSpeakFrequency()
	{
		return this.getString(R.string.title_speak_frequency);
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
	 * @return Unit day.
	 */
	public String getUnitDay(int quantity)
	{
		return this.getPluralString(R.plurals.unit_day, quantity);
	}

	/**
	 * @return Unit hour.
	 */
	public String getUnitHour(int quantity)
	{
		return this.getPluralString(R.plurals.unit_hour, quantity);
	}

	/**
	 * @return Unit minute.
	 */
	public String getUnitMinute(int quantity)
	{
		return this.getPluralString(R.plurals.unit_minute, quantity);
	}

	/**
	 * @return Unit second.
	 */
	public String getUnitSecond(int quantity)
	{
		return this.getPluralString(R.plurals.unit_second, quantity);
	}

	/**
	 * @return Upcoming alarm.
	 */
	public String getUpcomingAlarm(int quantity)
	{
		return this.getPluralString(R.plurals.upcoming_alarm, quantity);
	}

	/**
	 * @return Upcoming notification.
	 */
	public String getUpcomingNotification()
	{
		return this.getString(R.string.upcoming_alarm_plural);
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
