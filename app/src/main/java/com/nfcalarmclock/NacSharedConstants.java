package com.nfcalarmclock;

import android.content.Context;
import android.content.res.Resources;
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
	 * @return Use any action.
	 */
	public String getActionUseAny()
	{
		return this.getString(R.string.action_use_any);
	}

	/**
	 * @return Active notification.
	 */
	public String getActiveNotification()
	{
		return this.getString(R.string.title_active_alarm);
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
		return this.getStringList(R.array.audio_sources);
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
		return this.getStringList(R.array.days_of_week);
	}

	/**
	 * @return The days of week abbreviated.
	 */
	public List<String> getDaysOfWeekAbbr()
	{
		List<String> dow = this.getDaysOfWeek();

		for (int i=0; i < dow.size(); i++)
		{
			dow.set(i, dow.get(i).substring(0, 3));
		}

		return dow;
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
	 * @return Error message when trying to delete alarm while active.
	 */
	public String getErrorMessageActiveDelete()
	{
		return this.getString(R.string.error_message_active_delete);
	}

	/**
	 * @return Error message when trying to modify alarm while active.
	 */
	public String getErrorMessageActiveModify()
	{
		return this.getString(R.string.error_message_active_modify);
	}

	/**
	 * @return Max alarm error message.
	 */
	public String getErrorMessageMaxAlarms()
	{
		return this.getString(R.string.error_message_max_alarms);
	}

	/**
	 * @return Mismatch between the scanned NFC tag and the saved tag.
	 */
	public String getErrorMessageNfcMismatch()
	{
		return this.getString(R.string.error_message_nfc_mismatch);
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
	 * @return Any NFC tag ID message.
	 */
	public String getMessageAnyNfcTagId()
	{
		return this.getString(R.string.message_any_nfc_tag_id);
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
	 * @return NFC is optional message.
	 */
	public String getMessageNfcOptional()
	{
		return this.getString(R.string.message_nfc_optional);
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
	 * @return Repeat disabled message.
	 */
	public String getMessageRepeatDisabled()
	{
		return this.getString(R.string.message_repeat_disabled);
	}

	/**
	 * @return Repeat enabled message.
	 */
	public String getMessageRepeatEnabled()
	{
		return this.getString(R.string.message_repeat_enabled);
	}

	/**
	 * @return Show NFC tag ID message.
	 */
	public String getMessageShowNfcTagId()
	{
		return this.getString(R.string.message_show_nfc_tag_id);
	}

	/**
	 * @return Vibrate disabled message.
	 */
	public String getMessageVibrateDisabled()
	{
		return this.getString(R.string.message_vibrate_disabled);
	}

	/**
	 * @return Vibrate enabled message.
	 */
	public String getMessageVibrateEnabled()
	{
		return this.getString(R.string.message_vibrate_enabled);
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
		return this.getString(R.string.title_missed_alarm);
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
	 * @return Scan a specific NFC tag title.
	 */
	public String getTitleScanNfcTag()
	{
		return this.getString(R.string.title_scan_nfc_tag);
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
		return this.getString(R.string.title_upcoming_alarm);
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
