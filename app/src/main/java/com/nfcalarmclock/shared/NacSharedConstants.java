package com.nfcalarmclock.shared;

import android.content.Context;
import android.content.res.Resources;

import com.nfcalarmclock.util.NacCalendar;
import com.nfcalarmclock.R;

import java.util.Calendar;
import java.util.List;
import java.util.Locale;

/**
 * Constants container.
 */
@SuppressWarnings("RedundantSuppression")
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
	 * @return Dismiss action.
	 */
	public String getActionDismiss()
	{
		return this.getString(R.string.action_alarm_dismiss);
	}

	/**
	 * @return Previous folder action.
	 */
	public String getActionPreviousFolder()
	{
		return this.getString(R.string.action_previous_folder);
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
	 * @return AM.
	 */
	public String getAm()
	{
		return this.getString(R.string.am);
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
	 * @return Auto dismiss summaries.
	 */
	public List<String> getAutoDismissSummaries()
	{
		return this.getStringList(R.array.auto_dismiss_summaries);
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
	 * @return The different dismiss early times.
	 */
	public List<String> getDismissEarlyTimes()
	{
		return this.getStringList(R.array.dismiss_early_times);
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
	 * @return Error message when the device volume cannot be restricted.
	 */
	public String getErrorMessageRestrictVolumeChange()
	{
		return this.getString(R.string.error_message_restrict_volume_change);
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
	 * @return The alarm name example.
	 */
	public String getExampleName()
	{
		return this.getString(R.string.example_name);
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
	 * @return Max snooze summaries.
	 */
	public List<String> getMaxSnoozeSummaries()
	{
		return this.getStringList(R.array.max_snooze_summaries);
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
	 * @return Enable NFC request message.
	 */
	public String getMessageNfcRequest()
	{
		return this.getString(R.string.message_nfc_request);
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
	 * @return Statistics started on message.
	 */
	public String getMessageStatisticsStartedOn()
	{
		return this.getString(R.string.message_statistics_started_on);
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
	 * @return PM.
	 */
	public String getPm()
	{
		return this.getString(R.string.pm);
	}

	/**
	 * @return Settings.
	 */
	public String getSettings()
	{
		return this.getString(R.string.settings);
	}

	/**
	 * @return Snooze duration summaries.
	 */
	public List<String> getSnoozeDurationSummaries()
	{
		return this.getStringList(R.array.snooze_duration_summaries);
	}

	/**
	 * @return Speak to me words in the designated language.
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
	 * @see #getSpeakToMe(Context)
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
	 * @see #getSpeakToMe(Context)
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
	 * @return Will run.
	 */
	public String getWillRun()
	{
		return this.getString(R.string.will_run);
	}

}
