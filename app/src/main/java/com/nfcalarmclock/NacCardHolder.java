package com.nfcalarmclock;

import android.app.TimePickerDialog;
import android.content.Context;
import android.view.HapticFeedbackConstants;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TimePicker;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

/**
 * Card view holder.
 */
public class NacCardHolder
	extends RecyclerView.ViewHolder
	implements View.OnClickListener,
		View.OnLongClickListener,
		CompoundButton.OnCheckedChangeListener,
		TimePickerDialog.OnTimeSetListener,
		NacDialog.OnDismissListener,
		NacDayOfWeek.OnClickListener,
		SeekBar.OnSeekBarChangeListener
{

	/**
	 * Shared preferences.
	 */
	private NacSharedPreferences mSharedPreferences;

	/**
	 * Alarm.
	 */
	private NacAlarm mAlarm;

	/**
	 * The root view.
	 */
	private View mRoot;

	/**
	 * Card view.
	 */
	private NacCardView mCard;

	/**
	 * On/off switch for an alarm.
	 */
	 private NacCardSwitch mSwitch;

	/**
	 * Time and meridian.
	 */
	private NacCardTime mTime;

	/**
	 * Summary information.
	 */
	private NacCardSummary mSummary;

	/**
	 * Days and repeat.
	 */
	private NacCardDays mDays;

	/**
	 * Use NFC.
	 */
	private NacCardNfc mUseNfc;

	/**
	 * Sound.
	 */
	 private NacCardSound mSound;

	/**
	 * Vibrate checkbox.
	 */
	private NacCardVibrate mVibrate;

	/**
	 * Name.
	 */
	 private NacCardName mName;

	/**
	 * Delete.
	 */
	private NacCardDelete mDelete;

	/**
	 */
	public NacCardHolder(View root, NacCardMeasure measure)
	{
		super(root);

		Context context = root.getContext();
		this.mSharedPreferences = new NacSharedPreferences(context);
		this.mAlarm = null;
		this.mRoot = root;
		this.mCard = new NacCardView(context, root, measure);
		this.mSwitch = new NacCardSwitch(root);
		this.mTime = new NacCardTime(context, root);
		this.mSummary = new NacCardSummary(context, root, measure);
		this.mDays = new NacCardDays(root, measure);
		this.mUseNfc = new NacCardNfc(root);
		this.mSound = new NacCardSound(context, root, measure);
		this.mVibrate = new NacCardVibrate(root);
		this.mName = new NacCardName(context, root);
		this.mDelete = new NacCardDelete(root);
	}

	/**
	 * @return True if alarm can be modified, and False otherwise.
	 */
	public boolean canModifyAlarm()
	{
		NacSharedPreferences shared = this.getSharedPreferences();
		NacAlarm alarm = this.getAlarm();

		return (!alarm.isSnoozed(shared) || !shared.getPreventAppFromClosing());
	}

	/**
	 * Delete the alarm card.
	 */
	public void delete()
	{
		this.mDelete.delete(getAdapterPosition());
	}

	/**
	 * Respond to the collapse button being clicked.
	 */
	private void respondToCollapseButtonClick(View view)
	{
		this.mCard.collapse();
		view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
	}

	/**
	 * Perform the day button state change.
	 */
	public void respondToDayButtonClick(NacDayButton button, int index)
	{
		NacSharedPreferences shared = this.getSharedPreferences();
		NacAlarm alarm = this.getAlarm();

		if (!this.canModifyAlarm())
		{
			this.toastModifySnoozedAlarmError();
			button.cancelAnimator();
			return;
		}

		button.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
		alarm.toggleIndex(index);

		if (!alarm.areDaysSelected())
		{
			alarm.setRepeat(false);
		}

		alarm.changed();
		this.mDays.set(shared);
		this.mSummary.set(shared);
	}

	/**
	 * Respond to the delete button being clicked.
	 */
	private void respondToDeleteButtonClick(View view)
	{
		if (!this.canModifyAlarm())
		{
			this.toastDeleteSnoozedAlarmError();
			view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
			return;
		}

		this.delete();
	}

	/**
	 * Respond to the header being clicked.
	 */
	private void respondToHeaderClick(View view)
	{
		this.mCard.toggleState();
		view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
	}

	/**
	 * Respond to the name being clicked.
	 */
	private void respondToNameClick(View view)
	{
		this.mName.showDialog(this);
	}

	/**
	 * Respond to the NFC button being clicked.
	 */
	private void respondToNfcButtonClick(View view)
	{
		NacAlarm alarm = this.getAlarm();

		alarm.toggleUseNfc();
		alarm.changed();
		this.mUseNfc.set();
		view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);

		if (alarm.getUseNfc())
		{
			this.toastNfcEnabled();
		}
	}

	/**
	 * Respond to the repeat button being clicked.
	 */
	private void respondToRepeatButtonClick(View view)
	{
		NacAlarm alarm = this.getAlarm();
		alarm.toggleRepeat();
		alarm.changed();
		this.mDays.setRepeat();
		view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
	}

	/**
	 * Respond to the sound view being clicked.
	 */
	private void respondToSoundClick(View view)
	{
		this.mSound.startActivity();
	}

	/**
	 * Respond to the summary being clicked.
	 */
	private void respondToSummaryClick(View view)
	{
		this.mCard.expand();
		view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
	}

	/**
	 * Perform the switch state change.
	 */
	public void respondToSwitchCheckedChanged(CompoundButton button,
		boolean state)
	{
		NacSharedPreferences shared = this.getSharedPreferences();
		NacAlarm alarm = this.getAlarm();

		if (!this.canModifyAlarm())
		{
			this.toastModifySnoozedAlarmError();
			button.setChecked(!state);
			return;
		}

		button.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
		alarm.setEnabled(state);
		alarm.changed();
		this.mSummary.set(shared);

		if (!state)
		{
			shared.editSnoozeCount(alarm.getId(), 0);
		}
	}

	/**
	 * Respond to the time being clicked.
	 */
	private void respondToTimeClick(View view)
	{
		if (!this.canModifyAlarm())
		{
			this.toastModifySnoozedAlarmError();
			view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
			return;
		}

		this.mTime.showDialog(this);
	}

	/**
	 * Respond to the vibrate button being clicked.
	 */
	private void respondToVibrateButtonClick(View view)
	{
		NacAlarm alarm = this.getAlarm();
		alarm.toggleVibrate();
		alarm.changed();
		this.mVibrate.set();
		view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
	}

	/**
	 * Respond to the volume settings button being clicked.
	 */
	private void respondToVolumeSettingsButtonClick(View view)
	{
		this.mSound.showAudioSourceDialog(this);
		view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
	}

	/**
	 * @return The alarm.
	 */
	public NacAlarm getAlarm()
	{
		return this.mAlarm;
	}

	/**
	 * @return The card view.
	 */
	public CardView getCardView()
	{
		return this.mCard.getCardView();
	}

	/**
	 * @return The context.
	 */
	public Context getContext()
	{
		return this.getRoot().getContext();
	}

	/**
	 * @return The copy view, which resides in the background of the card view.
	 */
	public View getCopyView()
	{
		return this.mCard.getCopyView();
	}

	/**
	 * @return The delete view, which resides in the background of the card
	 *		   view.
	 */
	public View getDeleteView()
	{
		return this.mCard.getDeleteView();
	}

	/**
	 * @return The NFC Alarm Clock card view.
	 */
	public NacCardView getNacCardView()
	{
		return this.mCard;
	}

	/**
	 * @return The root view.
	 */
	public View getRoot()
	{
		return this.mRoot;
	}

	/**
	 * @return The shared preferences.
	 */
	private NacSharedPreferences getSharedPreferences()
	{
		return this.mSharedPreferences;
	}

	/**
	 * Initialize the alarm card.
	 *
	 * @param  alarm  The alarm to use to populate data in the alarm card.
	 * @param  wasAdded  Indicator for whether or not the card should be
	 *					 focused.
	 */
	public void init(NacAlarm alarm)
	{
		NacSharedPreferences shared = this.getSharedPreferences();
		this.mAlarm = alarm;

		this.setListeners(null);
		this.mCard.init(shared, alarm);
		this.mSwitch.init(alarm);
		this.mTime.init(shared, alarm);
		this.mSummary.init(shared, alarm);
		this.mDays.init(shared, alarm);
		this.mUseNfc.init(alarm);
		this.mSound.init(shared, alarm);
		this.mVibrate.init(alarm);
		this.mName.init(alarm);
		this.setColors();
		this.setListeners(this);
	}

	/**
	 * Interact with an alarm.
	 *
	 * Should be called when an alarm has been newly added.
	 */
	public void interact()
	{
		NacSharedPreferences shared = this.getSharedPreferences();
		boolean expandCard = shared.getExpandNewAlarm();

		this.mTime.showDialog(this);

		if (expandCard)
		{
			this.mCard.expand();
		}
	}

	/**
	 * @return True if the alarm card is collapsed, and False otherwise.
	 */
	public boolean isCollapsed()
	{
		return !this.isExpanded();
	}

	/**
	 * @return True if the alarm card is expanded, and False otherwise.
	 */
	public boolean isExpanded()
	{
		return this.mCard.isExpanded();
	}

	/**
	 * @return True if the alarm is snoozed and False otherwise.
	 */
	public boolean isSnoozed()
	{
		NacSharedPreferences shared = this.getSharedPreferences();
		NacAlarm alarm = this.getAlarm();

		return alarm.isSnoozed(shared);
	}

	/**
	 * Save the repeat state of the alarm.
	 */
	@Override
	public void onCheckedChanged(CompoundButton button, boolean state)
	{
		int id = button.getId();

		if (id == R.id.nac_switch)
		{
			this.respondToSwitchCheckedChanged(button, state);
		}
	}

	/**
	 * Save which day was selected to be repeated, or deselected so that it is
	 * not repeated.
	 */
	@Override
	public void onClick(NacDayButton button, int index)
	{
		this.respondToDayButtonClick(button, index);
	}

	/**
	 */
	@Override
	public void onClick(View view)
	{
		int id = view.getId();

		if (id == R.id.nac_header)
		{
			this.respondToHeaderClick(view);
		}
		else if (id == R.id.nac_summary)
		{
			this.respondToSummaryClick(view);
		}
		else if (id == R.id.nac_collapse)
		{
			this.respondToCollapseButtonClick(view);
		}
		else if (id == R.id.nac_time_parent)
		{
			this.respondToTimeClick(view);
		}
		else if (id == R.id.nac_repeat)
		{
			this.respondToRepeatButtonClick(view);
		}
		else if (id == R.id.nac_vibrate)
		{
			this.respondToVibrateButtonClick(view);
		}
		else if (id == R.id.nac_nfc)
		{
			this.respondToNfcButtonClick(view);
		}
		else if (id == R.id.nac_sound)
		{
			this.respondToSoundClick(view);
		}
		else if (id == R.id.nac_volume_settings)
		{
			this.respondToVolumeSettingsButtonClick(view);
		}
		else if (id == R.id.nac_name)
		{
			this.respondToNameClick(view);
		}
		else if (id == R.id.nac_delete)
		{
			this.respondToDeleteButtonClick(view);
		}
	}

	/**
	 * Notify alarm listener that the alarm has been modified.
	 */
	@Override
	public boolean onDismissDialog(NacDialog dialog)
	{
		NacAlarm alarm = this.getAlarm();
		int id = (int) dialog.getId();

		if (id == R.layout.dlg_alarm_name)
		{
			String name = dialog.getDataString();

			alarm.setName(name);
			alarm.changed();
			this.mName.set();
			this.mSummary.setName();
		}
		else if (id == R.layout.dlg_alarm_audio_source)
		{
			String source = dialog.getDataString();

			alarm.setAudioSource(source);
			alarm.changed();
		}
		else
		{
		}

		return true;
	}

	/**
	 */
	@Override
	public boolean onLongClick(View view)
	{
		NacSharedPreferences shared = this.getSharedPreferences();
		NacAlarm alarm = this.getAlarm();

		if (shared.getPreventAppFromClosing() && (shared.getSnoozeCount(alarm.getId()) > 0))
		{
			Context context = this.getContext();
			NacUtility.quickToast(context, "Unable to change days on a snoozed alarm");
			return true;
		}

		alarm.setRepeat(false);
		alarm.setDays(0);
		alarm.changed();
		this.mDays.set(shared);
		this.mSummary.set(shared);

		return true;
	}

	/**
	 */
	@Override
	public void onProgressChanged(SeekBar seekBar, int progress,
		boolean fromUser)
	{
		NacAlarm alarm = this.getAlarm();

		alarm.setVolume(progress);
		this.mSound.setVolumeIcon();
	}

	/**
	 */
	@Override
	public void onStartTrackingTouch(SeekBar seekBar)
	{
	}

	/**
	 */
	@Override
	public void onStopTrackingTouch(SeekBar seekBar)
	{
		NacAlarm alarm = this.getAlarm();

		alarm.changed();
	}

	/**
	 */
	@Override
	public void onTimeSet(TimePicker tp, int hr, int min)
	{
		NacSharedPreferences shared = this.getSharedPreferences();
		NacAlarm alarm = this.getAlarm();

		alarm.setHour(hr);
		alarm.setMinute(min);
		alarm.setEnabled(true);
		alarm.changed();
		this.mTime.set(shared);
		this.mSwitch.set();
		this.mSummary.set(shared);
	}

	/**
	 * Set the colors of the various views.
	 */
	public void setColors()
	{
		NacSharedPreferences shared = this.getSharedPreferences();

		this.mCard.setColor(shared);
		this.mSwitch.setColor(shared);
		this.mTime.setColor(shared);
		this.mSummary.setColor(shared);
	}

	/**
	 * Set the listeners of the various views.
	 */
	public void setListeners(Object listener)
	{
		View root = this.getRoot();

		this.mCard.setOnClickListener(root, (View.OnClickListener)listener);
		this.mSwitch.setOnCheckedChangeListener((CompoundButton.OnCheckedChangeListener)listener);
		this.mDays.setListeners(listener);
		this.mUseNfc.setOnClickListener((View.OnClickListener)listener);
		//this.mUseNfc.setOnCheckedChangeListener((CompoundButton.OnCheckedChangeListener)listener);
		this.mSound.setListener(listener);
		//this.mVibrate.setOnCheckedChangeListener((CompoundButton.OnCheckedChangeListener)listener);
		this.mVibrate.setOnClickListener((View.OnClickListener)listener);
		this.mName.setOnClickListener((View.OnClickListener)listener);
		this.mDelete.setOnClickListener((View.OnClickListener)listener);
	}

	/**
	 * Set listener to delete the card.
	 *
	 * @param  listener  The delete listener.
	 */
	public void setOnDeleteListener(NacCardDelete.OnDeleteListener listener)
	{
		this.mDelete.setOnDeleteListener(listener);
	}

	/**
	 * Set listener for when a menu item is clicked.
	 */
	public void setOnCreateContextMenuListener(
		View.OnCreateContextMenuListener listener)
	{
		View root = this.getRoot();

		this.mCard.setOnCreateContextMenuListener(root, listener);
	}

	/**
	 * Set the listener for when the alarm card expands or collapses.
	 */
	public void setOnStateChangeListener(
		NacCardView.OnStateChangeListener listener)
	{
		this.mCard.setOnStateChangeListener(listener);
	}

	/**
	 * Show a toast when a user enables NFC for an alarm.
	 */
	private void toastNfcEnabled()
	{
		Context context = this.getContext();
		NacUtility.quickToast(context, "NFC required to dismiss alarm");
	}

	/**
	 * Show a toast when a user tries to modify an alarm when they are unable
	 * to.
	 */
	private void toastDeleteSnoozedAlarmError()
	{
		Context context = this.getContext();
		NacUtility.quickToast(context, "Unable to delete a snoozed alarm");
	}

	/**
	 * Show a toast when a user tries to modify an alarm when they are unable
	 * to.
	 */
	private void toastModifySnoozedAlarmError()
	{
		Context context = this.getContext();
		NacUtility.quickToast(context, "Unable to modify a snoozed alarm");
	}

}
