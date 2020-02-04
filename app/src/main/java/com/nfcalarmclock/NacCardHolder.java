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
	 * Delete the alarm card.
	 */
	public void delete()
	{
		this.mDelete.delete(getAdapterPosition());
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
		this.mCard.init(alarm);
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
			this.mCard.expand(getAdapterPosition());
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
	public void onCheckedChanged(CompoundButton v, boolean state)
	{
		NacSharedPreferences shared = this.getSharedPreferences();
		NacAlarm alarm = this.getAlarm();
		int id = v.getId();

		if (alarm.isSnoozed(shared))
		{
			Context context = this.getContext();
			NacUtility.quickToast(context,
				"Unable to modify a snoozed alarm");
			v.setChecked(!state);
			return;
		}

		if (id == R.id.nac_switch)
		{
			v.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
			alarm.setEnabled(state);
			this.mSummary.set(shared);

			if (!state)
			{
				shared.editSnoozeCount(alarm.getId(), 0);
			}
		}
		else if (id == R.id.nac_repeat)
		{
			alarm.setRepeat(state);
			this.mSummary.set(shared);
		}
		else if (id == R.id.nac_nfc)
		{
			alarm.setUseNfc(state);
		}
		else if (id == R.id.nac_vibrate)
		{
			alarm.setVibrate(state);
		}

		alarm.changed();
	}

	/**
	 * Save which day was selected to be repeated, or deselected so that it is
	 * not repeated.
	 */
	@Override
	public void onClick(NacDayButton button, int index)
	{
		NacSharedPreferences shared = this.getSharedPreferences();
		NacAlarm alarm = this.getAlarm();

		if (alarm.isSnoozed(shared))
		{
			Context context = this.getContext();
			NacUtility.quickToast(context, "Unable to modify a snoozed alarm");
			button.cancelAnimator();
			return;
		}

		button.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
		alarm.toggleIndex(index);

		if (!alarm.areDaysSelected())
		{
			alarm.setRepeat(false);
			this.mDays.getRepeat().setEnabled(false);
			this.mDays.getRepeat().setAlpha(0.5f);
			this.mDays.set(shared);
		}
		else
		{
			this.mDays.getRepeat().setEnabled(true);
			this.mDays.getRepeat().setAlpha(1.0f);
		}

		alarm.changed();
		this.mSummary.set(shared);
	}

	/**
	 * When delete button is clicked, call the delete listener to delete the view.
	 *
	 * @param  view  The delete view.
	 */
	@Override
	public void onClick(View view)
	{
		NacAlarm alarm = this.getAlarm();
		int id = view.getId();

		if (id == R.id.nac_header)
		{
			this.mCard.toggle(getAdapterPosition());
			view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);

			if (this.mCard.isCollapseState() && alarm.wasChanged())
			{
				alarm.changed();
			}
		}
		else if (id == R.id.nac_summary)
		{
			this.mCard.expand(getAdapterPosition());
			view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
		}
		else if (id == R.id.nac_collapse)
		{
			this.mCard.collapse();
			view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);

			if (alarm.wasChanged())
			{
				alarm.changed();
			}
		}
		else if (id == R.id.nac_time_parent)
		{
			NacSharedPreferences shared = this.getSharedPreferences();
			Context context = this.getContext();

			if (alarm.isSnoozed(shared))
			{
				NacUtility.quickToast(context, "Unable to modify a snoozed alarm");
				return;
			}

			this.mTime.showDialog(this);
		}
		else if (id == R.id.nac_sound)
		{
			this.mSound.startActivity();
		}
		else if (id == R.id.nac_volume_settings)
		{
			this.mSound.showAudioSourceDialog(this);
			view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
		}
		else if (id == R.id.nac_name)
		{
			this.mName.showDialog(this);
		}
		else if (id == R.id.nac_delete)
		{
			NacSharedPreferences shared = this.getSharedPreferences();
			Context context = this.getContext();

			if (alarm.isSnoozed(shared))
			{
				NacUtility.quickToast(context, "Unable to delete a snoozed alarm");
				return;
			}

			this.delete();
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

		//view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
		alarm.setRepeat(false);
		alarm.setDays(0);
		alarm.changed();
		this.mDays.getRepeat().setEnabled(false);
		this.mDays.getRepeat().setAlpha(0.5f);
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

	@Override
	public void onStartTrackingTouch(SeekBar seekBar)
	{
	}

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
		this.mUseNfc.setOnCheckedChangeListener((CompoundButton.OnCheckedChangeListener)listener);
		this.mSound.setListener(listener);
		this.mVibrate.setOnCheckedChangeListener((CompoundButton.OnCheckedChangeListener)listener);
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

}
