package com.nfcalarmclock;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.BlendMode;
import android.graphics.BlendModeColorFilter;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.PorterDuff;
import android.os.Build;
import android.text.TextUtils;
import android.view.HapticFeedbackConstants;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.TimePicker;
import androidx.appcompat.widget.SwitchCompat;
import androidx.cardview.widget.CardView;
import androidx.core.graphics.ColorUtils;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.button.MaterialButton;
import java.util.EnumSet;

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
	 * Listener for when the delete button is clicked.
	 */
	public interface OnDeleteClickedListener
	{
		public void onDeleteClicked(int pos);
	}

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
	 private SwitchCompat mSwitch;

	/**
	 * Time text.
	 */
	private TextView mTimeView;

	/**
	 * Meridian text (AM/PM).
	 */
	private TextView mMeridianView;

	/**
	 * Summary view containing the days to repeat.
	 */
	private TextView mSummaryDaysView;

	/**
	 * Summary view containing the name of the alarm.
	 */
	private TextView mSummaryNameView;

	/**
	 * Day of week.
	 */
	private NacDayOfWeek mDayOfWeek;

	/**
	 * Repeat button.
	 */
	private MaterialButton mRepeatButton;

	/**
	 * Vibrate button.
	 */
	private MaterialButton mVibrateButton;

	/**
	 * NFC button.
	 */
	private MaterialButton mNfcButton;

	/**
	 * Media button.
	 */
	private MaterialButton mMediaButton;

	/**
	 * Volume image view.
	 */
	private ImageView mVolumeImageView;

	/**
	 * Volume seekbar.
	 */
	private SeekBar mVolumeSeekBar;

	/**
	 * Audio source button.
	 */
	private MaterialButton mAudioSourceButton;

	/**
	 * Name button.
	 */
	 private MaterialButton mNameButton;

	/**
	 * Delete button.
	 */
	private MaterialButton mDeleteButton;

	/**
	 * Listener for when the delete button is clicked.
	 */
	private OnDeleteClickedListener mOnDeleteClickedListener;

	/**
	 */
	public NacCardHolder(View root, NacCardMeasure measure)
	{
		super(root);

		Context context = root.getContext();
		LinearLayout dowView = root.findViewById(R.id.nac_days);

		this.mSharedPreferences = new NacSharedPreferences(context);
		this.mAlarm = null;
		this.mRoot = root;
		this.mCard = new NacCardView(context, root, measure);
		this.mTimeView = root.findViewById(R.id.nac_time);
		this.mMeridianView = root.findViewById(R.id.nac_meridian);
		this.mSwitch = root.findViewById(R.id.nac_switch);
		this.mSummaryDaysView = root.findViewById(R.id.nac_summary_days);
		this.mSummaryNameView = root.findViewById(R.id.nac_summary_name);
		this.mDayOfWeek = new NacDayOfWeek(dowView);
		this.mRepeatButton = root.findViewById(R.id.nac_repeat);
		this.mVibrateButton = root.findViewById(R.id.nac_vibrate);
		this.mNfcButton = root.findViewById(R.id.nac_nfc);
		this.mMediaButton = root.findViewById(R.id.nac_sound);
		this.mVolumeImageView = root.findViewById(R.id.nac_volume_icon);
		this.mVolumeSeekBar = root.findViewById(R.id.nac_volume_slider);
		this.mAudioSourceButton = root.findViewById(R.id.nac_volume_settings);
		this.mNameButton = root.findViewById(R.id.nac_name);
		this.mDeleteButton = root.findViewById(R.id.nac_delete);
		this.mOnDeleteClickedListener = null;
	}

	/**
	 * @return True if alarm can be modified, and False otherwise.
	 */
	public boolean canModifyAlarm()
	{
		NacSharedPreferences shared = this.getSharedPreferences();
		NacAlarm alarm = this.getAlarm();

		return !alarm.isSnoozed(shared) && !alarm.isActive();
	}

	/**
	 * Delete the alarm card.
	 */
	public void delete()
	{
		OnDeleteClickedListener listener = this.getOnDeleteClickedListener();
		int pos = getAdapterPosition();

		if (listener != null)
		{
			listener.onDeleteClicked(pos);
		}
	}

	/**
	 * Act as if the collapse button was clicked.
	 */
	public void doCollapseButtonClick()
	{
		this.mCard.collapse();
	}

	/**
	 * Act as if the day button was clicked.
	 */
	public void doDayButtonClick(NacCalendar.Day day)
	{
		NacSharedPreferences shared = this.getSharedPreferences();
		NacAlarm alarm = this.getAlarm();

		alarm.toggleDay(day);

		if (!alarm.areDaysSelected())
		{
			alarm.setRepeat(false);
		}

		alarm.changed();
		this.setDayOfWeek();
		this.setRepeatButton();
		this.setSummaryDaysView();
	}

	/**
	 * Act as if the delete button was clicked.
	 */
	public void doDeleteButtonClick()
	{
		this.delete();
	}

	/**
	 * Act as if the header was clicked.
	 */
	public void doHeaderClick()
	{
		this.mCard.toggleState();
	}

	/**
	 * Act as if the name was clicked.
	 */
	public void doNameClick()
	{
		this.showNameDialog();
	}

	/**
	 * Act as if the NFC button was clicked.
	 */
	public void doNfcButtonClick()
	{
		NacAlarm alarm = this.getAlarm();

		alarm.toggleUseNfc();

		if (!alarm.getUseNfc())
		{
			alarm.setNfcTagId("");
			this.toastNfc();
		}

		alarm.changed();
		this.setNfcButton();
	}

	/**
	 * Act as if the repeat button was clicked.
	 */
	public void doRepeatButtonClick()
	{
		NacAlarm alarm = this.getAlarm();

		alarm.toggleRepeat();
		alarm.changed();
		this.setRepeatButton();
		this.toastRepeat();
	}

	/**
	 * Act as if the repeat button was long clicked.
	 */
	public void doRepeatButtonLongClick()
	{
		NacSharedPreferences shared = this.getSharedPreferences();
		NacAlarm alarm = this.getAlarm();

		alarm.setRepeat(false);
		alarm.setDays(0);
		alarm.changed();
		this.setDayOfWeek();
		this.setRepeatButton();
		this.setSummaryDaysView();
	}

	/**
	 * Act as if the sound was clicked.
	 */
	public void doSoundClick()
	{
		this.startMediaActivity();
	}

	/**
	 * Act as if the summary was clicked.
	 */
	public void doSummaryClick()
	{
		this.mCard.expand();
	}

	/**
	 * Act as if the switch was changed.
	 */
	public void doSwitchCheckedChanged(boolean state)
	{
		NacSharedPreferences shared = this.getSharedPreferences();
		NacAlarm alarm = this.getAlarm();

		if (!state && (alarm.isSnoozed(shared) || alarm.isActive()))
		{
			Context context = this.getContext();
			NacContext.dismissForegroundService(context, alarm);
			alarm.setIsActive(false);
		}

		alarm.setEnabled(state);
		alarm.changed();
		this.setSummaryDaysView();

		if (!state)
		{
			shared.editSnoozeCount(alarm.getId(), 0);
		}
	}

	/**
	 * Act as if the time was clicked.
	 */
	public void doTimeClick()
	{
		this.showTimeDialog();
	}

	/**
	 * Act as if the vibrate button was clicked.
	 */
	public void doVibrateButtonClick()
	{
		NacAlarm alarm = this.getAlarm();

		alarm.toggleVibrate();
		alarm.changed();
		this.setVibrateButton();
		this.toastVibrate();
	}

	/**
	 * Act as if the volume setting button was clicked.
	 */
	public void doVolumeSettingButtonClick()
	{
		this.showAudioSourceDialog();
	}

	/**
	 * @return The alarm.
	 */
	public NacAlarm getAlarm()
	{
		return this.mAlarm;
	}

	/**
	 * @return The audio source button.
	 */
	private MaterialButton getAudioSourceButton()
	{
		return this.mAudioSourceButton;
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
	 * @return The day of week buttons.
	 */
	private NacDayOfWeek getDayOfWeek()
	{
		return this.mDayOfWeek;
	}

	/**
	 * @return The delete button.
	 */
	public MaterialButton getDeleteButton()
	{
		return this.mDeleteButton;
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
	 * @return The media button.
	 */
	private MaterialButton getMediaButton()
	{
		return this.mMediaButton;
	}

	/**
	 * @return The meridian view.
	 */
	public TextView getMeridianView()
	{
		return this.mMeridianView;
	}

	/**
	 * @return The NFC Alarm Clock card view.
	 */
	public NacCardView getNacCardView()
	{
		return this.mCard;
	}

	/**
	 * @return The name button.
	 */
	public MaterialButton getNameButton()
	{
		return this.mNameButton;
	}

	/**
	 * @return The NFC button.
	 */
	public MaterialButton getNfcButton()
	{
		return this.mNfcButton;
	}

	/**
	 * @return The listener for when the delete button is clicked.
	 */
	public OnDeleteClickedListener getOnDeleteClickedListener()
	{
		return this.mOnDeleteClickedListener;
	}

	/**
	 * @return The repeat button.
	 */
	public MaterialButton getRepeatButton()
	{
		return this.mRepeatButton;
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
	 * @return The summary days view.
	 */
	public TextView getSummaryDaysView()
	{
		return this.mSummaryDaysView;
	}

	/**
	 * @return The summary name view.
	 */
	public TextView getSummaryNameView()
	{
		return this.mSummaryNameView;
	}

	/**
	 * @return The switch.
	 */
	public SwitchCompat getSwitch()
	{
		return this.mSwitch;
	}

	/**
	 * @return The time view.
	 */
	public TextView getTimeView()
	{
		return this.mTimeView;
	}

	/**
	 * @return The vibrate button.
	 */
	private MaterialButton getVibrateButton()
	{
		return this.mVibrateButton;
	}

	/**
	 * @return The volume image view.
	 */
	private ImageView getVolumeImageView()
	{
		return this.mVolumeImageView;
	}

	/**
	 * @return The volume seekbar.
	 */
	private SeekBar getVolumeSeekBar()
	{
		return this.mVolumeSeekBar;
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
		this.setTimeView();
		this.setMeridianView();
		this.setSwitchView();
		this.setSummaryDaysView();
		this.setSummaryNameView();
		this.setDayOfWeek();
		this.setRepeatButton();
		this.setVibrateButton();
		this.setNfcButton();
		this.setMediaButton();
		this.setVolumeSeekBar();
		this.setVolumeImageView();
		this.setNameButton();
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

		this.showTimeDialog();

		if (shared.getExpandNewAlarm())
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
	public void onClick(NacDayButton button, NacCalendar.Day day)
	{
		this.respondToDayButtonClick(button, day);
	}

	/**
	 */
	@Override
	public void onClick(View view)
	{
		int id = view.getId();
		Context context = this.getContext();
		NacAlarm alarm = this.getAlarm();


		if (id == R.id.nac_header)
		{
			this.respondToHeaderClick(view);
			this.setDayOfWeek();
		}
		else if (id == R.id.nac_summary)
		{
			this.respondToSummaryClick(view);
			this.setDayOfWeek();
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
			this.setNameButton();
			this.setSummaryNameView();
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
		int id = view.getId();

		if (id == R.id.nac_repeat)
		{
			this.respondToRepeatButtonLongClick();
		}
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
		this.setVolumeImageView();
		//this.mSound.setVolumeIcon();
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
		this.setTimeView();
		this.setMeridianView();
		this.setSwitchView();
		this.setSummaryDaysView();
	}

	/**
	 * Respond to the collapse button being clicked.
	 */
	private void respondToCollapseButtonClick(View view)
	{
		view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
		this.doCollapseButtonClick();
	}

	/**
	 * Perform the day button state change.
	 */
	public void respondToDayButtonClick(NacDayButton button, NacCalendar.Day day)
	{
		if (!this.canModifyAlarm())
		{
			this.toastModifySnoozedAlarmError();
			button.cancelAnimator();
			return;
		}

		button.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
		this.doDayButtonClick(day);
	}

	/**
	 * Respond to the delete button being clicked.
	 */
	private void respondToDeleteButtonClick(View view)
	{
		if (this.canModifyAlarm())
		{
			this.doDeleteButtonClick();
		}
		else
		{
			view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
			this.toastDeleteSnoozedAlarmError();
		}
	}

	/**
	 * Respond to the header being clicked.
	 */
	private void respondToHeaderClick(View view)
	{
		view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
		this.doHeaderClick();
	}

	/**
	 * Respond to the name being clicked.
	 */
	private void respondToNameClick(View view)
	{
		if (this.canModifyAlarm())
		{
			this.doNameClick();
		}
		else
		{
			view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
			this.toastModifySnoozedAlarmError();
		}
	}

	/**
	 * Respond to the NFC button being clicked.
	 */
	private void respondToNfcButtonClick(View view)
	{
		if (this.canModifyAlarm())
		{
			this.doNfcButtonClick();
		}
		else
		{
			this.toastModifySnoozedAlarmError();
		}

		view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
	}

	/**
	 * Respond to the repeat button being clicked.
	 */
	private void respondToRepeatButtonClick(View view)
	{
		if (this.canModifyAlarm())
		{
			this.doRepeatButtonClick();
		}
		else
		{
			this.toastModifySnoozedAlarmError();
		}

		view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
	}

	/**
	 * Perform the repeat button long click action.
	 */
	public void respondToRepeatButtonLongClick()
	{
		if (this.canModifyAlarm())
		{
			this.doRepeatButtonLongClick();
		}
		else
		{
			//Context context = this.getContext();
			//NacSharedConstants cons = new NacSharedConstants(context);
			//NacUtility.quickToast(context, cons.getErrorMessageSnoozedDays());
			this.toastModifySnoozedAlarmError();
		}
	}

	/**
	 * Respond to the sound view being clicked.
	 */
	private void respondToSoundClick(View view)
	{
		if (this.canModifyAlarm())
		{
			this.doSoundClick();
		}
		else
		{
			this.toastModifySnoozedAlarmError();
			view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
		}
	}

	/**
	 * Respond to the summary being clicked.
	 */
	private void respondToSummaryClick(View view)
	{
		this.doSummaryClick();
		view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
	}

	/**
	 * Perform the switch state change.
	 */
	public void respondToSwitchCheckedChanged(CompoundButton button,
		boolean state)
	{
		NacSharedPreferences shared = this.getSharedPreferences();

		if (!this.canModifyAlarm() && shared.getPreventAppFromClosing())
		{
			this.toastModifySnoozedAlarmError();
			button.setChecked(!state);
			return;
		}

		button.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
		this.doSwitchCheckedChanged(state);
	}

	/**
	 * Respond to the time being clicked.
	 */
	private void respondToTimeClick(View view)
	{
		if (this.canModifyAlarm())
		{
			this.doTimeClick();
		}
		else
		{
			this.toastModifySnoozedAlarmError();
			view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
		}
	}

	/**
	 * Respond to the vibrate button being clicked.
	 */
	private void respondToVibrateButtonClick(View view)
	{
		if (this.canModifyAlarm())
		{
			this.doVibrateButtonClick();
		}
		else
		{
			this.toastModifySnoozedAlarmError();
		}

		view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
	}

	/**
	 * Respond to the volume settings button being clicked.
	 */
	private void respondToVolumeSettingsButtonClick(View view)
	{
		if (this.canModifyAlarm())
		{
			this.doVolumeSettingButtonClick();
		}
		else
		{
			this.toastModifySnoozedAlarmError();
		}

		view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
	}

	/**
	 * Set the colors of the various views.
	 */
	public void setColors()
	{
		NacSharedPreferences shared = this.getSharedPreferences();

		this.mCard.setColor(shared);
		this.setTimeColor();
		this.setMeridianColor();
		this.setSwitchColor();
		this.setSummaryDaysColor();
		this.setSummaryNameColor();
		this.setVolumeSeekBarColor();
	}

	/**
	 * Set the day of week to its proper setting.
	 */
	public void setDayOfWeek()
	{
		NacSharedPreferences shared = this.getSharedPreferences();
		NacDayOfWeek dow = this.getDayOfWeek();
		NacAlarm alarm = this.getAlarm();
		EnumSet<NacCalendar.Day> days = alarm.getDays();

		dow.setStartWeekOn(shared.getStartWeekOn());
		dow.setDays(days);
	}

	/**
	 * Set the listeners of the various views.
	 */
	public void setListeners(Object listener)
	{
		View root = this.getRoot();
		View.OnClickListener click = (View.OnClickListener) listener;
		View.OnLongClickListener longClick = (View.OnLongClickListener) listener;
		NacDayOfWeek.OnClickListener dow = (NacDayOfWeek.OnClickListener) listener;
		CompoundButton.OnCheckedChangeListener compound =
			(CompoundButton.OnCheckedChangeListener) listener;
		SeekBar.OnSeekBarChangeListener seek =
			(SeekBar.OnSeekBarChangeListener) listener;

		this.mCard.setOnClickListener(root, (View.OnClickListener)listener);
		this.getSwitch().setOnCheckedChangeListener(compound);
		this.getDayOfWeek().setOnClickListener(dow);
		this.getRepeatButton().setOnClickListener(click);
		this.getRepeatButton().setOnLongClickListener(longClick);
		this.getVibrateButton().setOnClickListener(click);
		this.getNfcButton().setOnClickListener(click);
		this.getMediaButton().setOnClickListener(click);
		this.getVolumeSeekBar().setOnSeekBarChangeListener(seek);
		this.getAudioSourceButton().setOnClickListener(click);
		this.getNameButton().setOnClickListener(click);
		this.getDeleteButton().setOnClickListener(click);
	}

	/**
	 * Set the media button to its proper setting.
	 */
	public void setMediaButton()
	{
		Context context = this.getContext();
		NacAlarm alarm = this.getAlarm();
		String path = alarm.getMediaPath();
		String message = NacSharedPreferences.getMediaMessage(context, path);
		float alpha = ((path != null) && !path.isEmpty()) ? 1.0f : 0.3f;

		this.getMediaButton().setText(message);
		this.getMediaButton().setAlpha(alpha);
		//this.mSound.setText(message);
		//this.mSound.setAlpha(alpha);
	}

	/**
	 * Set the meridian color.
	 */
	public void setMeridianColor()
	{
		NacSharedPreferences shared = this.getSharedPreferences();
		Context context = this.getContext();
		NacAlarm alarm = this.getAlarm();
		String meridian = alarm.getMeridian(context);
		int meridianColor = (meridian == "AM") ? shared.getAmColor()
			: shared.getPmColor();

		this.getMeridianView().setTextColor(meridianColor);
	}

	/**
	 * Set the meridian view to its proper setting.
	 */
	public void setMeridianView()
	{
		Context context = this.getContext();
		NacAlarm alarm = this.getAlarm();
		String meridian = alarm.getMeridian(context);

		this.getMeridianView().setText(meridian);
	}

	/**
	 * Set the name button to its proper settings.
	 */
	private void setNameButton()
	{
		Context context = this.getContext();
		NacAlarm alarm = this.getAlarm();
		MaterialButton button = this.getNameButton();
		String name = alarm.getNameNormalized();
		String message = NacSharedPreferences.getNameMessage(context, name);
		float alpha = !name.isEmpty() ? 1.0f : 0.3f;

		button.setText(message);
		button.setAlpha(alpha);
	}

	/**
	 * Set the NFC button to its proper settings.
	 */
	private void setNfcButton()
	{
		NacAlarm alarm = this.getAlarm();
		View view = this.getNfcButton();

		view.setAlpha(alarm.getUseNfc() ? 1.0f : 0.3f);
	}

	/**
	 * Set listener to delete the card.
	 *
	 * @param  listener  The delete listener.
	 */
	public void setOnDeleteClickedListener(OnDeleteClickedListener listener)
	{
		this.mOnDeleteClickedListener = listener;
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
	 * Set the repeat button to its proper setting.
	 */
	private void setRepeatButton()
	{
		View view = this.getRepeatButton();
		NacAlarm alarm = this.getAlarm();
		boolean repeat = alarm.getRepeat();

		if (!alarm.areDaysSelected())
		{
			view.setEnabled(false);
			view.setAlpha(0.3f);
		}
		else
		{
			view.setEnabled(true);
			view.setAlpha(repeat ? 1.0f : 0.3f);
		}
	}

	/**
	 * Set the color of the summary days.
	 */
	public void setSummaryDaysColor()
	{
		NacSharedPreferences shared = this.getSharedPreferences();
		int daysColor = shared.getDaysColor();

		this.getSummaryDaysView().setTextColor(daysColor);
	}

	/**
	 * Set the summary days view to its proper setting.
	 */
	public void setSummaryDaysView()
	{
		NacSharedPreferences shared = this.getSharedPreferences();
		Context context = this.getContext();
		NacAlarm alarm = this.getAlarm();
		int start = shared.getStartWeekOn();
		String string = NacCalendar.Days.toString(context, alarm, start);

		this.getSummaryDaysView().setText(string);
		this.getSummaryDaysView().requestLayout();
	}

	/**
	 * Set the color of the summary name.
	 */
	public void setSummaryNameColor()
	{
		NacSharedPreferences shared = this.getSharedPreferences();
		int nameColor = shared.getNameColor();

		this.getSummaryNameView().setTextColor(nameColor);
	}

	/**
	 * Set the summary name view to its proper setting.
	 */
	public void setSummaryNameView()
	{
		NacAlarm alarm = this.getAlarm();
		String name = alarm.getNameNormalized();

		this.getSummaryNameView().setText(name);
		//this.mName.setVisibility(name.isEmpty() ? View.GONE : View.VISIBLE);
	}

	/**
	 * Set the color of the switch.
	 */
	public void setSwitchColor()
	{
		NacSharedPreferences shared = this.getSharedPreferences();
		int theme = shared.getThemeColor();
		int themeDark = ColorUtils.blendARGB(theme, Color.BLACK, 0.6f);
		int[] thumbColors = new int[] {theme, Color.GRAY};
		int[] trackColors = new int[] {themeDark, Color.DKGRAY};

		int[][] states = new int[][] {
			new int[] { android.R.attr.state_checked},
			new int[] {-android.R.attr.state_checked}};
		ColorStateList thumbStateList = new ColorStateList(states, thumbColors);
		ColorStateList trackStateList = new ColorStateList(states, trackColors);

		this.getSwitch().setThumbTintList(thumbStateList);
		this.getSwitch().setTrackTintList(trackStateList);
	}

	/**
	 * Set the switch to its proper setting.
	 */
	public void setSwitchView()
	{
		NacAlarm alarm = this.getAlarm();
		boolean enabled = alarm.getEnabled();

		this.getSwitch().setChecked(enabled);
	}

	/**
	 * Set the time color.
	 */
	public void setTimeColor()
	{
		NacSharedPreferences shared = this.getSharedPreferences();
		int timeColor = shared.getTimeColor();

		this.getTimeView().setTextColor(timeColor);
	}

	/**
	 * Set the time view to its proper setting.
	 */
	public void setTimeView()
	{
		Context context = this.getContext();
		NacAlarm alarm = this.getAlarm();
		String time = alarm.getClockTime(context);

		this.getTimeView().setText(time);
	}

	/**
	 * Set the vibrate button to its proper setting.
	 */
	public void setVibrateButton()
	{
		View view = this.getVibrateButton();
		NacAlarm alarm = this.getAlarm();

		view.setAlpha(alarm.getVibrate() ? 1.0f : 0.3f);
	}

	/**
	 * Set the volume image view.
	 */
	public void setVolumeImageView()
	{
		ImageView image = this.getVolumeImageView();
		NacAlarm alarm = this.getAlarm();
		int progress = alarm.getVolume();

		if (progress == 0)
		{
			image.setImageResource(R.mipmap.volume_off);
		}
		else if ((progress > 0) && (progress <= 33))
		{
			image.setImageResource(R.mipmap.volume_low);
		}
		else if ((progress > 33) && (progress <= 66))
		{
			image.setImageResource(R.mipmap.volume_med);
		}
		else
		{
			image.setImageResource(R.mipmap.volume_high);
		}
	}

	/**
	 * Set the volume seekbar.
	 */
	public void setVolumeSeekBar()
	{
		NacAlarm alarm = this.getAlarm();

		//this.mVolume.incrementProgressBy(10);
		this.getVolumeSeekBar().setProgress(alarm.getVolume());
	}

	/**
	 * Set the volume seekbar color.
	 */
	@SuppressWarnings("deprecation")
	@TargetApi(Build.VERSION_CODES.Q)
	@SuppressLint("NewApi")
	public void setVolumeSeekBarColor()
	{
		NacSharedPreferences shared = this.getSharedPreferences();
		SeekBar seekbar = this.getVolumeSeekBar();
		int themeColor = shared.getThemeColor();
		Drawable progressDrawable = seekbar.getProgressDrawable();
		Drawable thumbDrawable = seekbar.getThumb();

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
		{
			BlendModeColorFilter blendFilter = new BlendModeColorFilter(
				themeColor, BlendMode.SRC_IN);

			progressDrawable.setColorFilter(blendFilter);
			thumbDrawable.setColorFilter(blendFilter);
		}
		else
		{
			progressDrawable.setColorFilter(themeColor, PorterDuff.Mode.SRC_IN);
			thumbDrawable.setColorFilter(themeColor, PorterDuff.Mode.SRC_IN);
		}
	}

	/**
	 * Show the audio source dialog.
	 */
	public void showAudioSourceDialog()
	{
		Context context = this.getContext();
		NacAlarm alarm = this.getAlarm();
		NacAudioSourceDialog dialog = new NacAudioSourceDialog();

		dialog.build(context);
		dialog.saveData(alarm.getAudioSource());
		dialog.addOnDismissListener(this);
		dialog.show();
	}

	/**
	 * Show the name dialog.
	 */
	private void showNameDialog()
	{
		Context context = this.getContext();
		NacAlarm alarm = this.getAlarm();
		NacNameDialog dialog = new NacNameDialog();

		dialog.build(context);
		dialog.saveData(alarm.getName());
		dialog.addOnDismissListener(this);
		dialog.show();
	}

	/**
	 * Show the time picker dialog.
	 */
	private void showTimeDialog()
	{
		Context context = this.getContext();
		NacAlarm alarm = this.getAlarm();
		int hour = alarm.getHour();
		int minute = alarm.getMinute();
		boolean format = NacCalendar.Time.is24HourFormat(context);
		TimePickerDialog dialog = new TimePickerDialog(context, this, hour, minute,
			format);

		dialog.show();
	}

	/**
	 * Start the media activity.
	 */
	public void startMediaActivity()
	{
		Context context = this.getContext();
		NacAlarm alarm = this.getAlarm();
		Intent intent = NacIntent.toIntent(context, NacMediaActivity.class, alarm);

		context.startActivity(intent);
	}

	/**
	 * Show a toast when a user tries to modify an alarm when they are unable
	 * to.
	 */
	private void toastDeleteSnoozedAlarmError()
	{
		Context context = this.getContext();
		NacSharedConstants cons = new NacSharedConstants(context);
		NacUtility.quickToast(context, cons.getErrorMessageSnoozedDelete());
	}

	/**
	 * Show a toast when a user tries to modify an alarm when they are unable
	 * to.
	 */
	private void toastModifySnoozedAlarmError()
	{
		Context context = this.getContext();
		NacSharedConstants cons = new NacSharedConstants(context);
		NacUtility.quickToast(context, cons.getErrorMessageSnoozedModify());
	}

	/**
	 * Show a toast when a user clicks the NFC button.
	 */
	private void toastNfc()
	{
		NacAlarm alarm = this.getAlarm();
		Context context = this.getContext();
		NacSharedConstants cons = new NacSharedConstants(context);
		String message = alarm.getUseNfc() ? cons.getMessageNfcRequired()
			: cons.getMessageNfcOptional();

		NacUtility.quickToast(context, message);
	}

	/**
	 * Show a toast when a user clicks the repeat button.
	 */
	private void toastRepeat()
	{
		NacAlarm alarm = this.getAlarm();
		Context context = this.getContext();
		NacSharedConstants cons = new NacSharedConstants(context);
		String message = alarm.getRepeat() ? cons.getMessageRepeatEnabled()
			: cons.getMessageRepeatDisabled();

		NacUtility.quickToast(context, message);
	}

	/**
	 * Show a toast when a user clicks the vibrate button.
	 */
	private void toastVibrate()
	{
		NacAlarm alarm = this.getAlarm();
		Context context = this.getContext();
		NacSharedConstants cons = new NacSharedConstants(context);
		String message = alarm.getVibrate() ? cons.getMessageVibrateEnabled()
			: cons.getMessageVibrateDisabled();

		NacUtility.quickToast(context, message);
	}

}
