package com.nfcalarmclock.card;

import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.view.animation.AccelerateInterpolator;
import android.view.HapticFeedbackConstants;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.TimePicker;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.ColorUtils;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.color.MaterialColors;
import com.google.android.material.button.MaterialButton;
//import com.google.android.material.timepicker.MaterialTimePicker;

import com.nfcalarmclock.activealarm.NacActiveAlarmService;
import com.nfcalarmclock.alarm.db.NacAlarm;
import com.nfcalarmclock.util.NacCalendar;
import com.nfcalarmclock.util.NacContext;
import com.nfcalarmclock.view.dayofweek.NacDayButton;
import com.nfcalarmclock.view.dayofweek.NacDayOfWeek;
import com.nfcalarmclock.view.dialog.NacDialog;
import com.nfcalarmclock.name.NacNameDialog;
import com.nfcalarmclock.shared.NacSharedConstants;
import com.nfcalarmclock.shared.NacSharedPreferences;
import com.nfcalarmclock.util.NacUtility;
import com.nfcalarmclock.R;

import java.lang.Float;
import java.util.EnumSet;

/**
 * Card view holder.
 */
@SuppressWarnings({"RedundantSuppression", "UnnecessaryInterfaceModifier"})
public class NacCardHolder
	extends RecyclerView.ViewHolder
	implements View.OnClickListener,
		View.OnLongClickListener,
		CompoundButton.OnCheckedChangeListener,
		TimePickerDialog.OnTimeSetListener,
        NacNameDialog.OnNameEnteredListener,
        NacDayOfWeek.OnWeekChangedListener,
		SeekBar.OnSeekBarChangeListener,
        NacHeightAnimator.OnAnimateHeightListener
{

	/**
	 * Listener for when the audio options button is clicked.
	 */
	public interface OnCardAudioOptionsClickedListener
	{
        public void onCardAudioOptionsClicked(NacCardHolder holder, NacAlarm alarm);
	}

	/**
	 * Listener for when a card is collapsed.
	 */
	public interface OnCardCollapsedListener
	{
		@SuppressWarnings("unused")
        public void onCardCollapsed(NacCardHolder holder, NacAlarm alarm);
	}

	/**
	 * Listener for when the delete button is clicked.
	 */
	public interface OnCardDeleteClickedListener
	{
        public void onCardDeleteClicked(NacCardHolder holder, NacAlarm alarm);
	}

	/**
	 * Listener for when a card is expanded.
	 */
	public interface OnCardExpandedListener
	{
		@SuppressWarnings({"unused", "EmptyMethod"})
		public void onCardExpanded(NacCardHolder holder, NacAlarm alarm);
	}

	/**
	 * Listener for when the media button is clicked.
	 */
	public interface OnCardMediaClickedListener
	{
        public void onCardMediaClicked(NacCardHolder holder, NacAlarm alarm);
	}

	/**
	 * Listener for when a card is updated.
	 */
	public interface OnCardUpdatedListener
	{
        public void onCardUpdated(NacCardHolder holder, NacAlarm alarm);
	}

	/**
	 * Listener for when a card will use NFC or not is changed.
	 */
	public interface OnCardUseNfcChangedListener
	{
        public void onCardUseNfcChanged(NacCardHolder holder, NacAlarm alarm);
	}

	/**
	 * Shared preferences.
	 */
	private final NacSharedPreferences mSharedPreferences;

	/**
	 * Alarm.
	 */
	private NacAlarm mAlarm;

	/**
	 * The root view.
	 */
	private final View mRoot;

	/**
	 * Card view.
	 */
	private final CardView mCardView;

	/**
	 * Copy swipe view.
	 */
	private final RelativeLayout mCopySwipeView;

	/**
	 * Delete swipe view.
	 */
	private final RelativeLayout mDeleteSwipeView;

	/**
	 * Header view.
	 */
	private final LinearLayout mHeaderView;

	/**
	 * Summary view.
	 */
	private final LinearLayout mSummaryView;

	/**
	 * Dismiss snoozed alarm parent view.
	 */
	private final LinearLayout mDismissParentView;

	/**
	 * Dismiss snoozed alarm button.
	 */
	private final MaterialButton mDismissButton;

	/**
	 * Dismiss early alarm button.
	 */
	private final MaterialButton mDismissEarlyButton;

	/**
	 * Extra view.
	 */
	private final LinearLayout mExtraView;

	/**
	 * On/off switch for an alarm.
	 */
	 private final SwitchCompat mSwitch;

	/**
	 * Time parent view.
	 */
	private final LinearLayout mTimeParentView;

	/**
	 * Time text.
	 */
	private final TextView mTimeView;

	/**
	 * Meridian text (AM/PM).
	 */
	private final TextView mMeridianView;

	/**
	 * Summary view containing the days to repeat.
	 */
	private final TextView mSummaryDaysView;

	/**
	 * Summary view containing the name of the alarm.
	 */
	private final TextView mSummaryNameView;

	/**
	 * Day of week.
	 */
	private final NacDayOfWeek mDayOfWeek;

	/**
	 * Repeat button.
	 */
	private final MaterialButton mRepeatButton;

	/**
	 * Vibrate button.
	 */
	private final MaterialButton mVibrateButton;

	/**
	 * NFC button.
	 */
	private final MaterialButton mNfcButton;

	/**
	 * Media button.
	 */
	private final MaterialButton mMediaButton;

	/**
	 * Volume image view.
	 */
	private final ImageView mVolumeImageView;

	/**
	 * Volume seekbar.
	 */
	private final SeekBar mVolumeSeekBar;

	/**
	 * Audio options button.
	 */
	private final MaterialButton mAudioOptionsButton;

	/**
	 * Name button.
	 */
	 private final MaterialButton mNameButton;

	/**
	 * Delete button.
	 */
	private final MaterialButton mDeleteButton;

	/**
	 * Card animator for collapsing and expanding.
	 */
	private final NacHeightAnimator mCardAnimator;

	/**
	 * Color animator for animating the background color of the card.
	 */
	private Animator mBackgroundColorAnimator;

	/**
	 * Color animator for highlighting the card.
	 */
	private Animator mHighlightAnimator;

	///**
	// * Time picker dialog.
	// */
	//private MaterialTimePicker mTimePicker;

	/**
	 * Listener for when the audio options button is clicked.
	 */
	private OnCardAudioOptionsClickedListener mOnCardAudioOptionsClickedListener;

	/**
	 * Listener for when the alarm card is collapsed.
	 */
	private OnCardCollapsedListener mOnCardCollapsedListener;


	/**
	 * Listener for when the delete button is clicked.
	 */
	private OnCardDeleteClickedListener mOnCardDeleteClickedListener;
	/**
	 * Listener for when the alarm card is expanded.
	 */
	private OnCardExpandedListener mOnCardExpandedListener;

	/**
	 * Listener for when the media button is clicked.
	 */
	private OnCardMediaClickedListener mOnCardMediaClickedListener;

	/**
	 * Listener for when the alarm card is updated.
	 */
	private OnCardUpdatedListener mOnCardUpdatedListener;

	/**
	 * Listener for when a card will use NFC or not is changed.
	 */
	private OnCardUseNfcChangedListener mOnCardUseNfcChangedListener;

	/**
	 * Collapse duration.
	 */
	private static final int COLLAPSE_DURATION = 250;

	/**
	 * Expand duration.
	 */
	private static final int EXPAND_DURATION = 250;

	/**
	 */
	public NacCardHolder(View root)
	{
		super(root);

		Context context = root.getContext();
		LinearLayout dowView = root.findViewById(R.id.nac_days);

		this.mSharedPreferences = new NacSharedPreferences(context);
		this.mAlarm = null;
		this.mRoot = root;

		this.mCardView = root.findViewById(R.id.nac_card);
		this.mCopySwipeView = root.findViewById(R.id.nac_swipe_copy);
		this.mDeleteSwipeView = root.findViewById(R.id.nac_swipe_delete);
		this.mHeaderView = root.findViewById(R.id.nac_header);
		this.mSummaryView = root.findViewById(R.id.nac_summary);
		this.mDismissParentView = root.findViewById(R.id.nac_dismiss_parent);
		this.mDismissButton = root.findViewById(R.id.nac_dismiss);
		this.mDismissEarlyButton = root.findViewById(R.id.nac_dismiss_early);
		this.mExtraView = root.findViewById(R.id.nac_extra);
		this.mTimeParentView = root.findViewById(R.id.nac_time_parent);
		this.mTimeView = root.findViewById(R.id.nac_time);
		this.mMeridianView = root.findViewById(R.id.nac_meridian);
		this.mSwitch = root.findViewById(R.id.nac_switch);
		this.mSummaryDaysView = root.findViewById(R.id.nac_summary_days);
		this.mSummaryNameView = root.findViewById(R.id.nac_summary_name);
		this.mDayOfWeek = new NacDayOfWeek(dowView);
		this.mRepeatButton = root.findViewById(R.id.nac_repeat);
		this.mVibrateButton = root.findViewById(R.id.nac_vibrate);
		this.mNfcButton = root.findViewById(R.id.nac_nfc);
		this.mMediaButton = root.findViewById(R.id.nac_media);
		this.mVolumeImageView = root.findViewById(R.id.nac_volume_icon);
		this.mVolumeSeekBar = root.findViewById(R.id.nac_volume_slider);
		this.mAudioOptionsButton = root.findViewById(R.id.nac_audio_options);
		this.mNameButton = root.findViewById(R.id.nac_name);
		this.mDeleteButton = root.findViewById(R.id.nac_delete);
		this.mCardAnimator = new NacHeightAnimator(this.getCardView());
		this.mBackgroundColorAnimator = null;
		this.mHighlightAnimator = null;
		//this.mTimePicker = null;
		this.mOnCardAudioOptionsClickedListener = null;
		this.mOnCardCollapsedListener = null;
		this.mOnCardDeleteClickedListener = null;
		this.mOnCardExpandedListener = null;
		this.mOnCardMediaClickedListener = null;
		this.mOnCardUpdatedListener = null;
		this.mOnCardUseNfcChangedListener = null;

		this.mCardAnimator.setInterpolator(new AccelerateInterpolator());
	}

	/**
	 * Animate the background color of the card changing to the collapsed color.
	 */
	private void animateCollapsedBackgroundColor()
	{
		Context context = this.getContext();
		CardView card = this.getCardView();
		Animator animator = AnimatorInflater.loadAnimator(context,
			R.animator.card_color_collapse);
		this.mBackgroundColorAnimator = animator;

		animator.setTarget(card);
		animator.start();
	}

	/**
	 * Animate the background color of the card changing to the expanded color.
	 */
	private void animateExpandedBackgroundColor()
	{
		Context context = this.getContext();
		CardView card = this.getCardView();
		Animator animator = AnimatorInflater.loadAnimator(
			context, R.animator.card_color_expand);
		this.mBackgroundColorAnimator = animator;

		animator.setTarget(card);
		animator.start();
	}

	/**
	 * Call the audio options button clicked listener.
	 */
	private void callOnCardAudioOptionsClickedListener()
	{
		OnCardAudioOptionsClickedListener listener = this.getOnCardAudioOptionsClickedListener();
		NacAlarm alarm = this.getAlarm();

		if (listener != null)
		{
			listener.onCardAudioOptionsClicked(this, alarm);
		}
	}

	/**
	 * Call the card collapsed listener.
	 * <p>
	 * This listener will not get called if the card has not been measured yet.
	 */
	private void callOnCardCollapsedListener()
	{
		NacSharedPreferences shared = this.getSharedPreferences();
		OnCardCollapsedListener listener = this.getOnCardCollapsedListener();

		if (!shared.getCardIsMeasured())
		{
			return;
		}

		if ((listener != null) && this.isCollapsed())
		{
			NacAlarm alarm = this.getAlarm();
			listener.onCardCollapsed(this, alarm);
		}
	}

	/**
	 * Call the delete clicked listener.
	 */
	private void callOnCardDeleteClickedListener()
	{
		OnCardDeleteClickedListener listener = this.getOnCardDeleteClickedListener();
		NacAlarm alarm = this.getAlarm();

		if (listener != null)
		{
			listener.onCardDeleteClicked(this, alarm);
		}
	}

	/**
	 * Call the card expanded listener.
	 * <p>
	 * This listener will not get called if the card has not been measured yet.
	 */
	private void callOnCardExpandedListener()
	{
		NacSharedPreferences shared = this.getSharedPreferences();
		OnCardExpandedListener listener = this.getOnCardExpandedListener();
		NacAlarm alarm = this.getAlarm();

		if (!shared.getCardIsMeasured())
		{
			return;
		}

		if ((listener != null) && this.isExpanded())
		{
			listener.onCardExpanded(this, alarm);
		}
	}

	/**
	 * Call the media button clicked listener.
	 */
	private void callOnCardMediaClickedListener()
	{
		OnCardMediaClickedListener listener = this.getOnCardMediaClickedListener();
		NacAlarm alarm = this.getAlarm();

		if (listener != null)
		{
			listener.onCardMediaClicked(this, alarm);
		}
	}

	/**
	 * Call the card updated listener.
	 */
	private void callOnCardUpdatedListener()
	{
		OnCardUpdatedListener listener = this.getOnCardUpdatedListener();
		NacAlarm alarm = this.getAlarm();

		if (listener != null)
		{
			listener.onCardUpdated(this, alarm);
		}
	}

	/**
	 * Call the card use NFC changed listener.
	 */
	private void callOnCardUseNfcChangedListener()
	{
		OnCardUseNfcChangedListener listener = this.getOnCardUseNfcChangedListener();
		NacAlarm alarm = this.getAlarm();

		if (listener != null)
		{
			listener.onCardUseNfcChanged(this, alarm);
		}
	}

	/**
	 * Reset the animator that changes the background color of the alarm card.
	 */
	public void cancelBackgroundColor()
	{
		Animator animator = this.getBackgroundColorAnimator();
		if ((animator != null) && animator.isRunning())
		{
			animator.cancel();
		}

		this.mBackgroundColorAnimator = null;
	}

	/**
	 * Cancel the animator that highlights the alarm card.
	 */
	public void cancelHighlight()
	{
		Animator animator = this.getHighlightAnimator();

		// Animator is currently running
		if ((animator != null) && animator.isRunning())
		{
			// Cancel the animation
			animator.cancel();
		}

		// Reset the highlight animator to null
		this.mHighlightAnimator = null;
	}

	/**
	 * @see #checkCanModifyAlarm()
	 *
	 * Same, but for deleting an alarm.
	 */
	public boolean checkCanDeleteAlarm()
	{
		//NacSharedPreferences shared = this.getSharedPreferences();
		NacAlarm alarm = this.getAlarm();

		if (alarm.isActive())
		{
			this.toastDeleteActiveAlarmError();
		}
		//else if (alarm.isSnoozed(shared))
		else if (alarm.isSnoozed())
		{
			this.toastDeleteSnoozedAlarmError();
		}
		else
		{
			return true;
		}

		return false;
	}

	/**
	 * Check if the alarm can be modified, and if it cannot, display toasts to the
	 * user indicating as such.
	 *
	 * @return True if the check passed successfully, and the alarm can be
	 *         modified, and False otherwise.
	 */
	public boolean checkCanModifyAlarm()
	{
		//NacSharedPreferences shared = this.getSharedPreferences();
		NacAlarm alarm = this.getAlarm();

		if (alarm.isActive())
		{
			this.toastModifyActiveAlarmError();
		}
		else if (alarm.isSnoozed())
		{
			this.toastModifySnoozedAlarmError();
		}
		else
		{
			return true;
		}

		return false;
	}

	/**
	 * Collapse the alarm card.
	 */
	public void collapse()
	{
		NacHeightAnimator animator = this.getCardAnimator();
		int fromHeight = this.getHeightExpanded();
		int toHeight = this.getHeightCollapsed();

		this.cancelHighlight();
		animator.setAnimationType(NacHeightAnimator.AnimationType.COLLAPSE);
		animator.setHeights(fromHeight, toHeight);
		animator.setDuration(COLLAPSE_DURATION);
		animator.start();
	}

	/**
	 * Collapse the alarm card after a refresh.
	 */
	public void collapseRefresh()
	{
		// Card is not collapsed
		if (!this.isCollapsed())
		{
			return;
		}

		NacSharedPreferences shared = this.getSharedPreferences();
		View dismissView = this.getDismissParentView();
		int visibility = dismissView.getVisibility();
		int fromHeight;
		int toHeight;

		// Set the from/to heights that the collapse will act on
		if (visibility == View.VISIBLE)
		{
			fromHeight = shared.getCardHeightCollapsed();
			toHeight = shared.getCardHeightCollapsedDismiss();
		}
		else
		{
			fromHeight = shared.getCardHeightCollapsedDismiss();
			toHeight = shared.getCardHeightCollapsed();
		}

		// Animate the collapse
		NacHeightAnimator animator = this.getCardAnimator();

		this.cancelHighlight();
		animator.setAnimationType(NacHeightAnimator.AnimationType.COLLAPSE);
		animator.setHeights(fromHeight, toHeight);
		animator.setDuration(COLLAPSE_DURATION);
		animator.start();
	}

	/**
	 * Compare the default color of two ColorStateList objects.
	 *
	 * @return True if the default color is the same, and False otherwise.
	 */
	@SuppressWarnings("BooleanMethodIsAlwaysInverted")
	public boolean compareColorStateList(ColorStateList oldColor,
		ColorStateList newColor)
	{
		if ((oldColor == null) || (newColor == null))
		{
			return false;
		}

		return oldColor.getDefaultColor() == newColor.getDefaultColor();
	}

	/**
	 * Compare the default color of a ColorStateList object with a color.
	 *
	 * @return True if the default color of the ColorStateList object is the same
	 *     as the color, and False otherwise.
	 */
	public boolean compareColorStateList(ColorStateList oldColor, int newColor)
	{
		if (oldColor == null)
		{
			return false;
		}

		return oldColor.getDefaultColor() == newColor;
	}

	/**
	 * Create a ColorStateList object that is blended with the theme color.
	 */
	private ColorStateList createBlendedThemeColorStateList()
	{
		NacSharedPreferences shared = this.getSharedPreferences();
		int themeColor = shared.getThemeColor();
		int blendedColor = ColorUtils.blendARGB(themeColor, Color.TRANSPARENT, 0.6f);

		return ColorStateList.valueOf(blendedColor);
	}

	/**
	 * Create a ColorStateList object from the theme color.
	 */
	private ColorStateList createThemeColorStateList()
	{
		NacSharedPreferences shared = this.getSharedPreferences();
		int themeColor = shared.getThemeColor();

		return ColorStateList.valueOf(themeColor);
	}

	/**
	 * Delete the alarm card.
	 */
	public void delete()
	{
		this.callOnCardDeleteClickedListener();
	}

	/**
	 * Act as if the audio options button was clicked.
	 */
	public void doAudioOptionsButtonClick()
	{
		this.callOnCardAudioOptionsClickedListener();
	}

	/**
	 * Act as if the alarm card was clicked.
	 */
	public void doCardClick()
	{
		if (this.isCollapsed())
		{
			this.expand();
		}
		else if (this.isExpanded())
		{
			this.collapse();
		}
		else
		{
			this.collapse();
		}
	}

	/**
	 * Collapse the alarm card without any animations.
	 */
	public void doCollapse()
	{
		View summary = this.getSummaryView();
		View extra = this.getExtraView();

		summary.setVisibility(View.VISIBLE);
		summary.setEnabled(true);
		extra.setVisibility(View.GONE);
		extra.setEnabled(false);
		this.refreshDismissAndDismissEarlyButtons();
	}

	/**
	 * Changes the color of the card, in addition to collapsing it.
	 *
	 * @see #doCollapse
	 */
	public void doCollapseWithColor()
	{
		this.doCollapse();
		//this.setCollapsedBackgroundColor();
	}

	/**
	 * Act as if the day button was clicked.
	 */
	public void doDayButtonClick(NacCalendar.Day day)
	{
		NacAlarm alarm = this.getAlarm();
		alarm.toggleDay(day);

		if (!alarm.getAreDaysSelected())
		{
			alarm.setRepeat(false);
		}

		//alarm.changed();
		this.setRepeatButton();
		this.setSummaryDaysView();
		this.callOnCardUpdatedListener();
	}

	/**
	 * Act as if the delete button was clicked.
	 */
	public void doDeleteButtonClick()
	{
		this.delete();
	}

	/**
	 * Act as if the dismiss button was clicked.
	 */
	public void doDismissButtonClick()
	{
		Context context = this.getContext();
		NacAlarm alarm = this.getAlarm();

		NacContext.dismissAlarmActivity(context, alarm);
	}

	/**
	 * Act as if the dismiss early button was clicked.
	 */
	public void doDismissEarlyButtonClick()
	{
		Context context = this.getContext();
		NacAlarm alarm = this.getAlarm();

		alarm.dismissEarly();
		this.refreshDismissAndDismissEarlyButtons();
		this.collapseRefresh();
		this.callOnCardUpdatedListener();
	}

	/**
	 * Expand the alarm card without any animations.
	 */
	public void doExpand()
	{
		View summary = this.getSummaryView();
		View extra = this.getExtraView();

		summary.setVisibility(View.GONE);
		summary.setEnabled(false);
		extra.setVisibility(View.VISIBLE);
		extra.setEnabled(true);
	}

	/**
	 * Changes the color of the card, in addition to expanding it.
	 *
	 * @see #doExpand
	 */
	public void doExpandWithColor()
	{
		this.doExpand();
		this.setExpandedBackgroundColor();
	}

	/**
	 * Act as if the media button was clicked.
	 */
	public void doMediaButtonClick()
	{
		this.callOnCardMediaClickedListener();
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

		if (!alarm.getShouldUseNfc())
		{
			alarm.setNfcTagId("");
			this.toastNfc();
		}

		//alarm.changed();
		this.callOnCardUpdatedListener();
		this.callOnCardUseNfcChangedListener();
	}

	/**
	 * Act as if the repeat button was clicked.
	 */
	public void doRepeatButtonClick()
	{
		NacAlarm alarm = this.getAlarm();

		alarm.toggleRepeat();
		//alarm.changed();
		this.callOnCardUpdatedListener();
		this.toastRepeat();
	}

	/**
	 * Act as if the repeat button was long clicked.
	 */
	public void doRepeatButtonLongClick()
	{
		NacAlarm alarm = this.getAlarm();

		alarm.setRepeat(false);
		alarm.setDays(0);
		//alarm.changed();
		this.setDayOfWeek();
		this.setRepeatButton();
		this.setSummaryDaysView();
		this.callOnCardUpdatedListener();
	}

	/**
	 * Act as if the switch was changed.
	 */
	public void doSwitchCheckedChanged(boolean state)
	{
		//NacSharedPreferences shared = this.getSharedPreferences();
		NacAlarm alarm = this.getAlarm();

		// Dismiss the alarm if it is currently active
		//if (!state && alarm.isInUse(shared))
		if (!state && alarm.isInUse())
		{
			Context context = this.getContext();

			NacActiveAlarmService.dismissService(context, alarm);
			alarm.setActive(false);
		}

		// Reset the snooze counter
		if (!state)
		{
			alarm.setSnoozeCount(0);
			//shared.editSnoozeCount(alarm.getId(), 0);
		}

		alarm.setEnabled(state);
		//alarm.changed();
		this.setSummaryDaysView();
		this.callOnCardUpdatedListener();
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
		//alarm.changed();
		this.callOnCardUpdatedListener();
		//this.setVibrateButton();
		this.toastVibrate();
	}

	/**
	 * Expand the alarm card.
	 */
	public void expand()
	{
		NacHeightAnimator animator = this.getCardAnimator();
		int fromHeight = this.getHeightCollapsed();
		int toHeight = this.getHeightExpanded();

		this.cancelHighlight();
		animator.setAnimationType(NacHeightAnimator.AnimationType.EXPAND);
		animator.setHeights(fromHeight, toHeight);
		animator.setDuration(EXPAND_DURATION);
		animator.start();
	}

	/**
	 * @return The alarm.
	 */
	public NacAlarm getAlarm()
	{
		return this.mAlarm;
	}

	/**
	 * @return The audio options button.
	 */
	public MaterialButton getAudioOptionsButton()
	{
		return this.mAudioOptionsButton;
	}

	/**
	 * @return The card expand/collapse animator.
	 */
	private NacHeightAnimator getCardAnimator()
	{
		return this.mCardAnimator;
	}

	/**
	 * @return The background color animator.
	 */
	private Animator getBackgroundColorAnimator()
	{
		return this.mBackgroundColorAnimator;
	}

	/**
	 * @return The card view.
	 */
	public CardView getCardView()
	{
		return this.mCardView;
		//return this.mCard.getCardView();
	}

	/**
	 * @return The button to collapse the alarm card.
	 */
	private MaterialButton getCollapseButton()
	{
		View root = this.getRoot();
		return root.findViewById(R.id.nac_collapse);
	}

	/**
	 * @return The parent view that contains the collapse button.
	 */
	private LinearLayout getCollapseParentView()
	{
		View root = this.getRoot();
		return root.findViewById(R.id.nac_collapse_parent);
	}

	/**
	 * @return The context.
	 */
	public Context getContext()
	{
		return this.getRoot().getContext();
	}

	/**
	 * @return The copy swipe view, which resides in the background of the card
	 *         view.
	 */
	public RelativeLayout getCopySwipeView()
	{
		return this.mCopySwipeView;
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
	 * @return The delete swipe view, which resides in the background of the card
	 *		   view.
	 */
	public RelativeLayout getDeleteSwipeView()
	{
		return this.mDeleteSwipeView;
	}

	/**
	 * @return The dismiss button.
	 */
	public MaterialButton getDismissButton()
	{
		return this.mDismissButton;
	}

	/**
	 * @return The dismiss early button.
	 */
	public MaterialButton getDismissEarlyButton()
	{
		return this.mDismissEarlyButton;
	}

	/**
	 * @return The dismiss parent view.
	 */
	public LinearLayout getDismissParentView()
	{
		return this.mDismissParentView;
	}

	/**
	 * @return The button to expand the alarm card.
	 */
	private MaterialButton getExpandButton()
	{
		View root = this.getRoot();
		return root.findViewById(R.id.nac_expand);
	}

	/**
	 * @return The other button to expand the alarm card (there are 2).
	 */
	private MaterialButton getExpandOtherButton()
	{
		View root = this.getRoot();
		return root.findViewById(R.id.nac_expand_other);
	}

	/**
	 * @return The extra view.
	 */
	public LinearLayout getExtraView()
	{
		return this.mExtraView;
	}

	/**
	 * @return The header view.
	 */
	private LinearLayout getHeaderView()
	{
		return this.mHeaderView;
	}

	/**
	 * @return The height when the card is collapsed.
	 */
	private int getHeightCollapsed()
	{
		NacSharedPreferences shared = this.getSharedPreferences();
		NacAlarm alarm = this.getAlarm();

		return alarm.isSnoozed() || alarm.willAlarmSoon()
			? shared.getCardHeightCollapsedDismiss()
			: shared.getCardHeightCollapsed();
	}

	/**
	 * @return The height when the card is expanded.
	 */
	private int getHeightExpanded()
	{
		NacSharedPreferences shared = this.getSharedPreferences();
		return shared.getCardHeightExpanded();
	}

	/**
	 * @return The highlight color animator.
	 */
	private Animator getHighlightAnimator()
	{
		return this.mHighlightAnimator;
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
	 * @return The listener for when the media button is clicked.
	 */
	public OnCardAudioOptionsClickedListener getOnCardAudioOptionsClickedListener()
	{
		return this.mOnCardAudioOptionsClickedListener;
	}

	/**
	 * @return The listener for when the alarm card is collapsed.
	 */
	private OnCardCollapsedListener getOnCardCollapsedListener()
	{
		return this.mOnCardCollapsedListener;
	}

	/**
	 * @return The listener for when the delete button is clicked.
	 */
	public OnCardDeleteClickedListener getOnCardDeleteClickedListener()
	{
		return this.mOnCardDeleteClickedListener;
	}

	/**
	 * @return The listener for when the alarm card is expanded.
	 */
	private OnCardExpandedListener getOnCardExpandedListener()
	{
		return this.mOnCardExpandedListener;
	}

	/**
	 * @return The listener for when the media button is clicked.
	 */
	public OnCardMediaClickedListener getOnCardMediaClickedListener()
	{
		return this.mOnCardMediaClickedListener;
	}

	/**
	 * @return The listener for when the alarm card is updated.
	 */
	private OnCardUpdatedListener getOnCardUpdatedListener()
	{
		return this.mOnCardUpdatedListener;
	}

	/**
	 * @return The listener for when the alarm card should use NFC or not is
	 *     changed.
	 */
	private OnCardUseNfcChangedListener getOnCardUseNfcChangedListener()
	{
		return this.mOnCardUseNfcChangedListener;
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
	 * @return The shared constants.
	 */
	private NacSharedConstants getSharedConstants()
	{
		return this.getSharedPreferences().getConstants();
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
	 * @return The summary view.
	 */
	private LinearLayout getSummaryView()
	{
		return this.mSummaryView;
	}

	/**
	 * @return The switch.
	 */
	public SwitchCompat getSwitch()
	{
		return this.mSwitch;
	}

	///**
	// * @return The time picker.
	// */
	//public MaterialTimePicker getTimePicker()
	//{
	//	return this.mTimePicker;
	//}

	/**
	 * @return The time parent view.
	 */
	public LinearLayout getTimeParentView()
	{
		return this.mTimeParentView;
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
	 * Hide the swipe views.
	 */
	private void hideSwipeViews()
	{
		this.getCopySwipeView().setVisibility(View.GONE);
		this.getDeleteSwipeView().setVisibility(View.GONE);
	}

	/**
	 * Highlight the alarm card.
	 */
	public void highlight()
	{
		this.cancelBackgroundColor();

		Context context = this.getContext();
		CardView card = this.getCardView();
		Animator animator = AnimatorInflater.loadAnimator(context,
			R.animator.card_color_highlight);
		this.mHighlightAnimator = animator;

		animator.setTarget(card);
		animator.start();
	}

	/**
	 * Initialize the alarm card.
	 */
	public void init(NacAlarm alarm)
	{
		this.setAlarm(alarm);
		this.initListeners(null);
		this.initViews();
		this.initColors();
		this.initListeners(this);
	}

	/**
	 * Initialize the colors of the various views.
	 */
	public void initColors()
	{
		this.setDividerColor();
		this.setTimeColor();
		this.setMeridianColor();
		this.setSwitchColor();
		this.setSummaryDaysColor();
		this.setSummaryNameColor();
		this.setDismissButtonRippleColor();
		this.setDayOfWeekRippleColor();
		this.setRepeatButtonRippleColor();
		this.setVibrateButtonRippleColor();
		this.setNfcButtonRippleColor();
		this.setMediaButtonRippleColor();
		this.setVolumeSeekBarColor();
		this.setAudioOptionsButtonRippleColor();
		this.setNameButtonRippleColor();
		this.setDeleteButtonRippleColor();
		this.setCollapseButtonRippleColor();
		this.setExpandButtonRippleColor();
	}

	/**
	 * Initialize the listeners of the various views.
	 */
	public void initListeners(Object listener)
	{
		View.OnClickListener click = (View.OnClickListener) listener;
		View.OnLongClickListener longClick = (View.OnLongClickListener) listener;
		NacDayOfWeek.OnWeekChangedListener dow =
			(NacDayOfWeek.OnWeekChangedListener) listener;
		CompoundButton.OnCheckedChangeListener compound =
			(CompoundButton.OnCheckedChangeListener) listener;
		SeekBar.OnSeekBarChangeListener seek =
			(SeekBar.OnSeekBarChangeListener) listener;
		NacHeightAnimator.OnAnimateHeightListener height =
			(NacHeightAnimator.OnAnimateHeightListener) listener;

		this.hideSwipeViews();
		this.mCardAnimator.setOnAnimateHeightListener(height);
		this.getHeaderView().setOnClickListener(click);
		this.getSummaryView().setOnClickListener(click);
		this.getTimeParentView().setOnClickListener(click);
		this.getSwitch().setOnCheckedChangeListener(compound);
		this.getDismissParentView().setOnClickListener(click);
		this.getDismissButton().setOnClickListener(click);
		this.getDismissEarlyButton().setOnClickListener(click);
		this.getExpandButton().setOnClickListener(click);
		this.getExpandOtherButton().setOnClickListener(click);
		this.getCollapseButton().setOnClickListener(click);
		this.getCollapseParentView().setOnClickListener(click);
		this.getDayOfWeek().setOnWeekChangedListener(dow);
		this.getRepeatButton().setOnClickListener(click);
		this.getRepeatButton().setOnLongClickListener(longClick);
		this.getVibrateButton().setOnClickListener(click);
		this.getNfcButton().setOnClickListener(click);
		this.getMediaButton().setOnClickListener(click);
		this.getAudioOptionsButton().setOnClickListener(click);
		this.getVolumeSeekBar().setOnSeekBarChangeListener(seek);
		this.getNameButton().setOnClickListener(click);
		this.getDeleteButton().setOnClickListener(click);
	}

	/**
	 * Initialize the various views.
	 */
	public void initViews()
	{
		this.refreshDismissAndDismissEarlyButtons();
		this.setTimeView();
		this.setMeridianView();
		this.setSwitchView();
		this.setSummaryDaysView();
		this.setSummaryNameView();
		this.setDayOfWeek();
		this.setStartWeekOn();
		this.setRepeatButton();
		this.setVibrateButton();
		this.setNfcButton();
		this.setMediaButton();
		this.setVolumeSeekBar();
		this.setVolumeImageView();
		this.setNameButton();
	}

	/**
	 * Interact with an alarm.
	 * <p>
	 * Should be called when an alarm has been newly added.
	 */
	public void interact()
	{
		NacSharedPreferences shared = this.getSharedPreferences();

		this.showTimeDialog();

		if (shared.getExpandNewAlarm())
		{
			this.expand();
		}
	}

	/**
	 * @return True if the alarm is in use, and False otherwise.
	 */
	public boolean isAlarmInUse()
	{
		//NacSharedPreferences shared = this.getSharedPreferences();
		NacAlarm alarm = this.getAlarm();

		return alarm.isInUse();
		//return alarm.isInUse(shared);
	}

	/**
	 * @return True if the alarm card is collapsed, and False otherwise.
	 */
	public boolean isCollapsed()
	{
		NacSharedPreferences shared = this.getSharedPreferences();
		View cardView = this.getCardView();
		View extraView = this.getExtraView();
		int currentHeight = cardView.getMeasuredHeight();

		return (extraView.getVisibility() == View.GONE)
			|| (currentHeight == shared.getCardHeightCollapsed())
			|| (currentHeight == shared.getCardHeightCollapsedDismiss());
	}

	/**
	 * @return True if the alarm card is expanded, and False otherwise.
	 */
	public boolean isExpanded()
	{
		NacSharedPreferences shared = this.getSharedPreferences();
		View cardView = this.getCardView();
		View extraView = this.getExtraView();
		int currentHeight = cardView.getMeasuredHeight();

		return (extraView.getVisibility() == View.VISIBLE)
			|| (currentHeight == shared.getCardHeightExpanded());
	}

	///**
	// * @return True if showing the time picker, and false otherwise.
	// */
	//public boolean isShowingTimePicker()
	//{
	//	MaterialTimePicker timepicker = this.getTimePicker();
	//	return (timepicker != null);
	//}

	/**
	 * Measure the different alarm card heights.
	 * <p>
	 * This will populate the array that is passed in with the corresponding
	 * heights:
	 * <p>
	 *     i=0: Collapsed height.
	 *     i=1: Collapsed height with the dismiss button shown.
	 *     i=2: Expanded height.
	 *
	 * @param  heights  An integer array of 3 elements.
	 */
	public void measureCard(int[] heights)
	{
		CardView cardView = this.getCardView();

		if ((heights == null) || (heights.length != 3))
		{
			return;
		}

		this.doExpand();
		heights[2] = NacUtility.getHeight(cardView);

		this.doCollapse();
		this.getDismissParentView().setVisibility(View.GONE);
		heights[0] = NacUtility.getHeight(cardView);

		this.getDismissParentView().setVisibility(View.VISIBLE);
		heights[1] = NacUtility.getHeight(cardView);

		// Refresh the dismiss and dismiss early buttons
		this.refreshDismissAndDismissEarlyButtons();
	}

	/**
	 * Called when the card is collapsing.
	 * <p>
	 * Used to set view visibility, animate the background color, and call the
	 * card collapsed listener.
	 */
	public void onAnimateCollapse(NacHeightAnimator animator)
	{
		if (animator.isLastUpdate())
		{
			// Quickly change view visibility, no animations
			this.doCollapse();

			// Check if the card was already collapsed, in which this would not need
			// to be run
			NacSharedPreferences shared = this.getSharedPreferences();
			int fromHeight = animator.getFromHeight();

			if ((fromHeight != shared.getCardHeightCollapsed())
				&& (fromHeight != shared.getCardHeightCollapsedDismiss()))
			{
				// Card was not already collapsed. Animate the background color
				this.animateCollapsedBackgroundColor();
			}

			// Call the listener
			this.callOnCardCollapsedListener();
		}
	}

	/**
	 * Called when the card is expanding.
	 * <p>
	 * Used to set view visibility, animate the background color, and call the
	 * card collapsed listener.
	 */
	public void onAnimateExpand(NacHeightAnimator animator)
	{
		if (animator.isFirstUpdate())
		{
			this.doExpand();
			this.animateExpandedBackgroundColor();
			this.callOnCardExpandedListener();
		}
	}

	/**
	 * A switch has changed state.
	 */
	@Override
	public void onCheckedChanged(CompoundButton button, boolean state)
	{
		int id = button.getId();

		// Switch that enables/disables an alarm
		if (id == R.id.nac_switch)
		{
			this.respondToSwitchCheckedChanged(button, state);
		}
	}

	/**
	 * A day button was selected.
	 */
	@Override
	public boolean onWeekChanged(NacDayButton button, NacCalendar.Day day)
	{
		this.respondToDayButtonClick(button, day);
		return true;
	}

	/**
	 * A view was clicked.
	 */
	@Override
	public void onClick(View view)
	{
		int id = view.getId();

		// Expand/collapse the alarm
		if ((id == R.id.nac_header)
			|| (id == R.id.nac_summary)
			|| (id == R.id.nac_dismiss_parent)
			|| (id == R.id.nac_expand) || (id == R.id.nac_expand_other)
			|| (id == R.id.nac_collapse) || (id == R.id.nac_collapse_parent))
		{
			this.respondToCardClick(view);
		}
		// Time
		else if (id == R.id.nac_time_parent)
		{
			this.respondToTimeClick(view);
		}
		// Repeat
		else if (id == R.id.nac_repeat)
		{
			this.respondToRepeatButtonClick(view);
		}
		// Vibrate
		else if (id == R.id.nac_vibrate)
		{
			this.respondToVibrateButtonClick(view);
		}
		// NFC
		else if (id == R.id.nac_nfc)
		{
			this.respondToNfcButtonClick(view);
		}
		// Music/ringtone
		else if (id == R.id.nac_media)
		{
			this.respondToMediaButtonClick(view);
		}
		// Audio options
		else if (id == R.id.nac_audio_options)
		{
			this.respondToAudioOptionsButtonClick(view);
		}
		// Name
		else if (id == R.id.nac_name)
		{
			this.respondToNameClick(view);
		}
		// Delete
		else if (id == R.id.nac_delete)
		{
			this.respondToDeleteButtonClick(view);
		}
		// Dismiss button
		else if (id == R.id.nac_dismiss)
		{
			this.respondToDismissButtonClick(view);
		}
		// Dismiss early button
		else if (id == R.id.nac_dismiss_early)
		{
			this.respondToDismissEarlyButtonClick(view);
		}
		//else if (this.isShowingTimePicker())
		//{
		//	this.setTime();
		//	this.mTimePicker = null;
		//}
	}

	/**
	 * Notify alarm listener that the alarm has been modified.
	 */
	@Override
	public void onNameEntered(String name)
	{
		NacAlarm alarm = this.getAlarm();

		alarm.setName(name);
		//alarm.changed();
		this.setNameButton();
		this.setSummaryNameView();
		this.callOnCardUpdatedListener();
	}

	/**
	 */
	@Override
	public boolean onLongClick(View view)
	{
		int id = view.getId();

		// Repeat button
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
		int alarmVolume = alarm.getVolume();

		// Do nothing if the volumes are already the same
		if (alarmVolume == progress)
		{
			return;
		}

		// Volume can be changed since the alarm can be modified
		if (this.checkCanModifyAlarm())
		{
			alarm.setVolume(progress);
			this.setVolumeImageView();
		}
		else
		{
			seekBar.setProgress(alarm.getVolume());
		}
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

		// Unable to update the alarm. It is currently in use (active or snoozed)
		if (alarm.isInUse())
		{
			return;
		}

		// Update the card
		this.callOnCardUpdatedListener();
	}

	/**
	 */
	@Override
	public void onTimeSet(TimePicker timepicker, int hr, int min)
	{
		NacAlarm alarm = this.getAlarm();

		alarm.setHour(hr);
		alarm.setMinute(min);
		alarm.setEnabled(true);
		this.setTimeView();
		this.setMeridianView();
		this.setMeridianColor();
		this.setSwitchView();
		this.setSummaryDaysView();

		// Get the visiblity before refreshing the dismiss buttons
		View dismissView = this.getDismissParentView();
		int beforeVisibility = dismissView.getVisibility();

		// Refresh dismiss buttons
		this.refreshDismissAndDismissEarlyButtons();

		// Get the visiblity after refreshing the dismiss buttons
		int afterVisibility = dismissView.getVisibility();

		// Determine if the card is already collapsed and the visibility of the
		// dismiss buttons has changed after the new time was set. If so, there
		// is or should be new space because of the dismiss buttons, so do a collapse
		// due to the refresh
		if (this.isCollapsed() && (beforeVisibility != afterVisibility))
		{
			this.collapseRefresh();
		}

		// Call the card updated listener
		this.callOnCardUpdatedListener();
	}

	/**
	 * Perform haptic feedback on a view.
	 */
	public void performHapticFeedback(View view)
	{
		view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
	}

	/**
	 * Check if the dismiss view should be refreshed or not.
	 *
	 * @return True if the dismiss view should be refreshed, and False otherwise.
	 */
	public boolean shouldRefreshDismissView()
	{
		NacAlarm alarm = this.getAlarm();
		View dismissView = this.getDismissParentView();
		View expandView = this.getExpandButton();

		// Alarm is in use, or will alarm soon so the "Dismiss" or "Dismiss early"
		// button should be shown
		int dismissVis = alarm.isInUse() || alarm.willAlarmSoon() ? View.VISIBLE
				: View.GONE;

		// The dismiss view is being shown so do not show this view
		int expandVis = (dismissVis == View.GONE) ? View.VISIBLE : View.INVISIBLE;

		// The "Dismiss"/"Dismiss early" button OR the "Expand" down-arrow button
		// are NOT the correct and expected visibilities
		return (dismissView.getVisibility() != dismissVis)
			|| (expandView.getVisibility() != expandVis);
	}

	/**
	 * Set the dismiss view parent, which contains both "Dismiss" and
	 * "Dismiss early" to its proper setting.
	 */
	public void refreshDismissAndDismissEarlyButtons()
	{
		NacAlarm alarm = this.getAlarm();
		View dismissView = this.getDismissParentView();
		View expandView = this.getExpandButton();

		// Alarm is in use, or will alarm soon so the "Dismiss" or "Dismiss early"
		// button should be shown
		int dismissVis = alarm.isInUse() || alarm.willAlarmSoon() ? View.VISIBLE
				: View.GONE;

		// The dismiss view is being shown so do not show this view
		int expandVis = (dismissVis == View.GONE) ? View.VISIBLE : View.INVISIBLE;

		// Set the "Dismiss" button visibility
		if (dismissView.getVisibility() != dismissVis)
		{
			dismissView.setVisibility(dismissVis);
		}

		// Set the "Expand" down-arrow button visibility
		if (expandView.getVisibility() != expandVis)
		{
			expandView.setVisibility(expandVis);
		}

		// Set the "Dismiss" and "Dismiss early" visibilities
		if (dismissVis == View.VISIBLE)
		{
			MaterialButton dismissButton = this.getDismissButton();
			MaterialButton dismissEarlyButton = this.getDismissEarlyButton();

			// Alarm is in use so "Dismiss" should be shown
			if (alarm.isInUse())
			{
				dismissButton.setVisibility(View.VISIBLE);
				dismissEarlyButton.setVisibility(View.GONE);
			}
			// Alarm will alarm soon so "Dismiss early" should be shown
			else if (alarm.willAlarmSoon())
			{
				dismissButton.setVisibility(View.GONE);
				dismissEarlyButton.setVisibility(View.VISIBLE);
			}
		}
	}

	/**
	 * Respond to the audio options button being clicked.
	 */
	private void respondToAudioOptionsButtonClick(View view)
	{
		// Respond to the audio options button since the alarm can be modified
		if (this.checkCanModifyAlarm())
		{
			this.doAudioOptionsButtonClick();
		}

		this.performHapticFeedback(view);
	}

	/**
	 * Respond to the alarm card being clicked.
	 */
	private void respondToCardClick(View view)
	{
		this.doCardClick();
		this.performHapticFeedback(view);
	}

	/**
	 * Perform the day button state change.
	 */
	public void respondToDayButtonClick(NacDayButton button, NacCalendar.Day day)
	{
		// Change the state of the day button since the alarm can be modified
		if (this.checkCanModifyAlarm())
		{
			this.doDayButtonClick(day);
		}
		// Unable to change the day. Reset the state of the day
		else
		{
			button.toggle();
		}

		this.performHapticFeedback(button);
	}

	/**
	 * Respond to the delete button being clicked.
	 */
	private void respondToDeleteButtonClick(View view)
	{
		// Delete the alarm, since the alarm can be deleted
		if (this.checkCanDeleteAlarm())
		{
			this.doDeleteButtonClick();
		}

		this.performHapticFeedback(view);
	}

	/**
	 * Respond to the dismiss button being clicked.
	 */
	private void respondToDismissButtonClick(View view)
	{
		this.doDismissButtonClick();
		this.performHapticFeedback(view);
	}

	/**
	 * Respond to the dismiss early button being clicked.
	 */
	private void respondToDismissEarlyButtonClick(View view)
	{
		this.doDismissEarlyButtonClick();
		this.performHapticFeedback(view);
	}

	/**
	 * Respond to the media button being clicked.
	 */
	private void respondToMediaButtonClick(View view)
	{
		// Respond to the media button since the alarm can be modified
		if (this.checkCanModifyAlarm())
		{
			this.doMediaButtonClick();
		}

		this.performHapticFeedback(view);
	}

	/**
	 * Respond to the name being clicked.
	 */
	private void respondToNameClick(View view)
	{
		// Name of an alarm can be clicked since the alarm can be modified
		if (this.checkCanModifyAlarm())
		{
			this.doNameClick();
		}

		this.performHapticFeedback(view);
	}

	/**
	 * Respond to the NFC button being clicked.
	 */
	private void respondToNfcButtonClick(View view)
	{
		// NFC button can be clicked since the alarm can be modified
		if (this.checkCanModifyAlarm())
		{
			this.doNfcButtonClick();
		}
		// Unable to modify the alarm. Reset the state of the button
		else
		{
			MaterialButton nfcButton = (MaterialButton) view;
			boolean state = nfcButton.isChecked();

			nfcButton.setChecked(!state);
		}

		this.performHapticFeedback(view);
	}

	/**
	 * Respond to the repeat button being clicked.
	 */
	private void respondToRepeatButtonClick(View view)
	{
		// Repeat button can be clicked since the alarm can be modified
		if (this.checkCanModifyAlarm())
		{
			this.doRepeatButtonClick();
		}
		// Unable to modify the alarm. Reset the state of the button
		else
		{
			MaterialButton repeatButton = (MaterialButton) view;
			boolean state = repeatButton.isChecked();

			repeatButton.setChecked(!state);
		}

		this.performHapticFeedback(view);
	}

	/**
	 * Perform the repeat button long click action.
	 */
	public void respondToRepeatButtonLongClick()
	{
		// Repeat button can be long clicked since the alarm can be modified
		if (this.checkCanModifyAlarm())
		{
			this.doRepeatButtonLongClick();
		}
	}

	/**
	 * Perform the switch state change.
	 */
	public void respondToSwitchCheckedChanged(CompoundButton button,
		boolean state)
	{
		// Change the state of the switch since the alarm can be modified
		if (this.checkCanModifyAlarm())
		{
			this.doSwitchCheckedChanged(state);
		}
		// Unable to modify the alarm. Reset the state of the switch
		else
		{
			button.setChecked(!state);
		}

		this.performHapticFeedback(button);
	}

	/**
	 * Respond to the time being clicked.
	 */
	private void respondToTimeClick(View view)
	{
		// Unable to modify the alarm
		if (!this.checkCanModifyAlarm())
		{
			this.performHapticFeedback(view);
			return;
		}

		// Time can be clicked
		this.doTimeClick();
	}

	/**
	 * Respond to the vibrate button being clicked.
	 */
	private void respondToVibrateButtonClick(View view)
	{
		// Vibrate can be clicked since the alarm can be modified
		if (this.checkCanModifyAlarm())
		{
			this.doVibrateButtonClick();
		}
		// Unable to modify the alarm. Reset the state of the button
		else
		{
			MaterialButton vibrateButton = (MaterialButton) view;
			boolean state = vibrateButton.isChecked();

			vibrateButton.setChecked(!state);
		}

		this.performHapticFeedback(view);
	}

	/**
	 * Set the alarm.
	 */
	public void setAlarm(NacAlarm alarm)
	{
		this.mAlarm = alarm;
	}

	/**
	 * Set the ripple color of the audio options button.
	 */
	private void setAudioOptionsButtonRippleColor()
	{
		MaterialButton button = this.getAudioOptionsButton();
		this.setMaterialButtonColor(button);
	}

	/**
	 * Set the ripple color of the collapse button.
	 */
	private void setCollapseButtonRippleColor()
	{
		MaterialButton button = this.getCollapseButton();
		this.setMaterialButtonColor(button);
	}

	/**
	 * Set the background color for when the card is collapsed.
	 */
	public void setCollapsedBackgroundColor()
	{
		Context context = this.getContext();
		CardView card = this.getCardView();
		int grayDark = ContextCompat.getColor(context, R.color.gray_dark);
		int color = MaterialColors.getColor(context, R.attr.colorCard, grayDark);
		//int color = NacUtility.getThemeAttrColor(context, R.attr.colorCardExpanded);

		//Context context = view.getContext();
		//int bg = NacUtility.getThemeAttrColor(context, id);

		//view.setBackground(null);
		//view.setBackgroundColor(bg);
		//NacUtility.setBackground(card, id);
		card.setBackgroundColor(color);
	}

	/**
	 * Set the day of week to its proper setting.
	 */
	public void setDayOfWeek()
	{
		//NacSharedPreferences shared = this.getSharedPreferences();
		NacDayOfWeek dow = this.getDayOfWeek();
		NacAlarm alarm = this.getAlarm();
		EnumSet<NacCalendar.Day> days = alarm.getDays();

		if (!dow.getDays().equals(days))
		{
			//dow.setStartWeekOn(shared.getStartWeekOn());
			dow.setDays(days);
		}
	}

	/**
	 * Set the day of week to start on.
	 */
	public void setStartWeekOn()
	{
		NacSharedPreferences shared = this.getSharedPreferences();
		NacDayOfWeek dow = this.getDayOfWeek();

		dow.setStartWeekOn(shared.getStartWeekOn());
	}

	/**
	 * Set the ripple color for each day in the day of week view.
	 */
	public void setDayOfWeekRippleColor()
	{
		ColorStateList newColor = this.createBlendedThemeColorStateList();
		NacDayOfWeek dow = this.getDayOfWeek();

		for (NacDayButton day : dow.getDayButtons())
		{
			MaterialButton button = day.getButton();
			this.setMaterialButtonColor(button, newColor);
		}
	}

	/**
	 * Set the ripple color of the delete button.
	 */
	private void setDeleteButtonRippleColor()
	{
		MaterialButton button = this.getDeleteButton();
		this.setMaterialButtonColor(button);
	}

	/**
	 * Set the ripple color for the dismiss button.
	 */
	public void setDismissButtonRippleColor()
	{
		MaterialButton button = this.getDismissButton();
		this.setMaterialButtonColor(button);
	}

	/**
	 * Set the divider color.
	 */
	public void setDividerColor()
	{
		NacSharedPreferences shared = this.getSharedPreferences();
		View root = this.getRoot();
		ViewGroup headerDivider = root.findViewById(R.id.nac_divider_header);
		View deleteDivider = root.findViewById(R.id.nac_divider_delete);
		ColorStateList themeColor = ColorStateList.valueOf(shared.getThemeColor());

		// Header divider
		for (int i=0; i < headerDivider.getChildCount(); i++)
		{
			View view = headerDivider.getChildAt(i);
			ColorStateList color = view.getBackgroundTintList();

			if (!this.compareColorStateList(color, themeColor))
			{
				view.setBackgroundTintList(themeColor);
			}
		}

		// Delete divider
		ColorStateList color = deleteDivider.getBackgroundTintList();

		if (!this.compareColorStateList(color, themeColor))
		{
			deleteDivider.setBackgroundTintList(themeColor);
		}
	}

	/**
	 * Set the ripple color of the expand button.
	 */
	private void setExpandButtonRippleColor()
	{
		MaterialButton button = this.getExpandButton();
		this.setMaterialButtonColor(button);
	}

	/**
	 * Set the background color for when the card is expanded.
	 */
	public void setExpandedBackgroundColor()
	{
		Context context = this.getContext();
		CardView card = this.getCardView();
		int gray = ContextCompat.getColor(context, R.color.gray);
		int color = MaterialColors.getColor(context, R.attr.colorCardExpanded, gray);
		//int color = NacUtility.getThemeAttrColor(context, R.attr.colorCardExpanded);

		//Context context = view.getContext();
		//int bg = NacUtility.getThemeAttrColor(context, id);

		//view.setBackground(null);
		//view.setBackgroundColor(bg);
		//NacUtility.setBackground(card, id);
		card.setBackgroundColor(color);
	}

	/**
	 * Set the color of a MaterialButton.
	 *
	 * @param  button  The button to color.
	 * @param  newColor  The new color of the button.
	 */
	protected void setMaterialButtonColor(MaterialButton button, ColorStateList newColor)
	{
		if (button == null)
		{
			return;
		}

		ColorStateList currentColor = button.getRippleColor();

		if (!this.compareColorStateList(currentColor, newColor))
		{
			button.setRippleColor(newColor);
		}
	}

	/**
	 * @see #setMaterialButtonColor(MaterialButton, ColorStateList)
	 */
	protected void setMaterialButtonColor(MaterialButton button)
	{
		if (button == null)
		{
			return;
		}

		ColorStateList newColor = this.createBlendedThemeColorStateList();
		this.setMaterialButtonColor(button, newColor);
	}

	/**
	 * Set the media button to its proper setting.
	 */
	public void setMediaButton()
	{
		Context context = this.getContext();
		NacAlarm alarm = this.getAlarm();
		MaterialButton button = this.getMediaButton();

		String path = alarm.getMediaPath();
		String message = NacSharedPreferences.getMediaMessage(context, path);
		String text = button.getText().toString();
		float alpha = !path.isEmpty() ? 1.0f : 0.3f;

		if (!text.equals(message))
		{
			button.setText(message);
		}

		if (Float.compare(button.getAlpha(), alpha) != 0)
		{
			button.setAlpha(alpha);
		}
	}

	/**
	 * Set the ripple color of the media button.
	 */
	public void setMediaButtonRippleColor()
	{
		MaterialButton button = this.getMediaButton();
		this.setMaterialButtonColor(button);
	}

	/**
	 * Set the meridian color.
	 */
	public void setMeridianColor()
	{
		Context context = this.getContext();
		NacSharedPreferences shared = this.getSharedPreferences();
		TextView tv = this.getMeridianView();
		NacAlarm alarm = this.getAlarm();
		String meridian = alarm.getMeridian(context);
		int color = shared.getMeridianColor(meridian);

		this.setTextViewColor(tv, color);
	}

	/**
	 * Set the meridian view to its proper setting.
	 */
	public void setMeridianView()
	{
		Context context = this.getContext();
		NacAlarm alarm = this.getAlarm();
		TextView tv = this.getMeridianView();
		String meridian = alarm.getMeridian(context);
		String text = tv.getText().toString();

		if (!text.equals(meridian))
		{
			tv.setText(meridian);
		}
	}

	/**
	 * Set the name button to its proper settings.
	 */
	private void setNameButton()
	{
		NacSharedConstants cons = this.getSharedConstants();
		NacAlarm alarm = this.getAlarm();
		MaterialButton button = this.getNameButton();

		String name = alarm.getNameNormalized();
		String message = NacSharedPreferences.getNameMessage(cons, name);
		String text = button.getText().toString();
		float alpha = !name.isEmpty() ? 1.0f : 0.3f;

		if (!text.equals(message))
		{
			button.setText(message);
		}

		if (Float.compare(button.getAlpha(), alpha) != 0)
		{
			button.setAlpha(alpha);
		}
	}

	/**
	 * Set the ripple color of the name button.
	 */
	private void setNameButtonRippleColor()
	{
		MaterialButton button = this.getNameButton();
		this.setMaterialButtonColor(button);
	}

	/**
	 * Set the NFC button to its proper settings.
	 */
	private void setNfcButton()
	{
		MaterialButton button = this.getNfcButton();
		NacAlarm alarm = this.getAlarm();
		boolean shouldUseNfc = alarm.getShouldUseNfc();

		if (button.isChecked() != shouldUseNfc)
		{
			button.setChecked(shouldUseNfc);
		}
	}

	/**
	 * Set the ripple color of the NFC button.
	 */
	private void setNfcButtonRippleColor()
	{
		MaterialButton button = this.getNfcButton();
		this.setMaterialButtonColor(button);
	}

	/**
	 * Set listener for when the audio options button in the alarm card is clicked.
	 *
	 * @param  listener  The media clicked listener.
	 */
	public void setOnCardAudioOptionsClickedListener(OnCardAudioOptionsClickedListener listener)
	{
		this.mOnCardAudioOptionsClickedListener = listener;
	}

	/**
	 * Set the listener for when the alarm card is collapsed.
	 */
	public void setOnCardCollapsedListener(OnCardCollapsedListener listener)
	{
		this.mOnCardCollapsedListener = listener;
	}

	/**
	 * Set listener to delete the card.
	 *
	 * @param  listener  The delete listener.
	 */
	public void setOnCardDeleteClickedListener(OnCardDeleteClickedListener listener)
	{
		this.mOnCardDeleteClickedListener = listener;
	}

	/**
	 * Set the listener for when the alarm card is expanded.
	 */
	public void setOnCardExpandedListener(OnCardExpandedListener listener)
	{
		this.mOnCardExpandedListener = listener;
	}

	/**
	 * Set listener for when the media button in the alarm card is clicked.
	 *
	 * @param  listener  The media clicked listener.
	 */
	public void setOnCardMediaClickedListener(OnCardMediaClickedListener listener)
	{
		this.mOnCardMediaClickedListener = listener;
	}

	/**
	 * Set the listener for when the alarm card is updated.
	 */
	public void setOnCardUpdatedListener(OnCardUpdatedListener listener)
	{
		this.mOnCardUpdatedListener = listener;
	}

	/**
	 * Set the listener for when the alarm card should use NFC or not is changed.
	 */
	public void setOnCardUseNfcChangedListener(OnCardUseNfcChangedListener listener)
	{
		this.mOnCardUseNfcChangedListener = listener;
	}

	/**
	 * Set listener for when a menu item is clicked.
	 */
	public void setOnCreateContextMenuListener(
		View.OnCreateContextMenuListener listener)
	{
		this.getHeaderView().setOnCreateContextMenuListener(listener);
		this.getSummaryView().setOnCreateContextMenuListener(listener);
		this.getTimeParentView().setOnCreateContextMenuListener(listener);
	}

	/**
	 * Set the repeat button to its proper setting.
	 */
	private void setRepeatButton()
	{
		MaterialButton button = this.getRepeatButton();
		NacAlarm alarm = this.getAlarm();
		boolean isEnabled = alarm.getAreDaysSelected();
		boolean shouldRepeat = alarm.getAreDaysSelected() && alarm.getShouldRepeat();

		if (button.isChecked() != shouldRepeat)
		{
			button.setChecked(shouldRepeat);
		}

		if (button.isEnabled() != isEnabled)
		{
			button.setEnabled(isEnabled);
		}
	}

	/**
	 * Set the ripple color of the repeat button.
	 */
	private void setRepeatButtonRippleColor()
	{
		MaterialButton button = this.getRepeatButton();
		this.setMaterialButtonColor(button);
	}

	/**
	 * Set the progress and thumb color of a seekbar.
	 *
	 * @param  seekbar  A seekbar.
	 */
	protected void setSeekBarColor(SeekBar seekbar)
	{
		if (seekbar == null)
		{
			return;
		}

		ColorStateList currentProgress = seekbar.getProgressTintList();
		ColorStateList currentThumb = seekbar.getThumbTintList();
		ColorStateList newColor = this.createThemeColorStateList();

		if (!this.compareColorStateList(currentProgress, newColor))
		{
			seekbar.setProgressTintList(newColor);
		}

		if (!this.compareColorStateList(currentThumb, newColor))
		{
			seekbar.setThumbTintList(newColor);
		}
	}

	/**
	 * Set the color of the summary days.
	 */
	public void setSummaryDaysColor()
	{
		NacSharedPreferences shared = this.getSharedPreferences();
		TextView tv = this.getSummaryDaysView();
		int color = shared.getDaysColor();

		this.setTextViewColor(tv, color);
	}

	/**
	 * Set the summary days view to its proper setting.
	 */
	public void setSummaryDaysView()
	{
		NacSharedPreferences shared = this.getSharedPreferences();
		NacSharedConstants cons = this.getSharedConstants();
		NacAlarm alarm = this.getAlarm();
		TextView tv = this.getSummaryDaysView();

		int start = shared.getStartWeekOn();
		String string = NacCalendar.Days.toString(cons, alarm, start);
		String text = tv.getText().toString();

		if (!text.equals(string))
		{
			tv.setText(string);
			//tv.requestLayout();
		}
	}

	/**
	 * Set the color of the summary name.
	 */
	public void setSummaryNameColor()
	{
		NacSharedPreferences shared = this.getSharedPreferences();
		TextView tv = this.getSummaryNameView();
		int color = shared.getNameColor();

		this.setTextViewColor(tv, color);
	}

	/**
	 * Set the summary name view to its proper setting.
	 */
	public void setSummaryNameView()
	{
		NacAlarm alarm = this.getAlarm();
		TextView tv = this.getSummaryNameView();
		String name = alarm.getNameNormalized();
		String text = tv.getText().toString();

		if (!text.equals(name))
		{
			tv.setText(name);
		}
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
		SwitchCompat switchView = this.getSwitch();

		this.setSwitchColor(switchView, thumbStateList, trackStateList);
	}

	/**
	 * Set the thumb and track color of a switch.
	 *
	 * @param  switchView  The switch.
	 * @param  thumbColor  The color of the thumb.
	 * @param  trackColor  The color of the track.
	 */
	protected void setSwitchColor(SwitchCompat switchView,
		ColorStateList thumbColor, ColorStateList trackColor)
	{
		if (switchView == null)
		{
			return;
		}

		ColorStateList currentThumb = switchView.getThumbTintList();
		ColorStateList currentTrack = switchView.getTrackTintList();

		// Thumb color
		if (!this.compareColorStateList(currentThumb, thumbColor))
		{
			switchView.setThumbTintList(thumbColor);
		}

		// Track color
		if (!this.compareColorStateList(currentTrack, trackColor))
		{
			switchView.setTrackTintList(trackColor);
		}
	}

	/**
	 * Set the switch to its proper setting.
	 */
	public void setSwitchView()
	{
		NacAlarm alarm = this.getAlarm();
		SwitchCompat view = this.getSwitch();
		boolean enabled = alarm.isEnabled();

		if (view.isChecked() != enabled)
		{
			view.setChecked(enabled);
		}
	}

	/**
	 * Set the color of a TextView.
	 *
	 * @param  tv  The text view to color.
	 * @param  newColor  The new color of the text view.
	 */
	protected void setTextViewColor(TextView tv, int newColor)
	{
		if (tv == null)
		{
			return;
		}

		if (tv.getCurrentTextColor() != newColor)
		{
			tv.setTextColor(newColor);
		}
	}

	///**
	// * Set the time.
	// */
	//public void setTime()
	//{
	//	//if (!this.isShowingTimePicker())
	//	//{
	//	//	return;
	//	//}

	//	MaterialTimePicker timepicker = this.getTimePicker();
	//	NacAlarm alarm = this.getAlarm();
	//	int hr = timepicker.getHour();
	//	int min = timepicker.getMinute();

	//	alarm.setHour(hr);
	//	alarm.setMinute(min);
	//	alarm.setIsEnabled(true);
	//	//alarm.changed();
	//	this.setTimeView();
	//	this.setMeridianView();
	//	this.setMeridianColor();
	//	this.setSwitchView();
	//	this.setSummaryDaysView();
	//	this.callOnCardUpdatedListener();
	//}

	/**
	 * Set the time color.
	 */
	public void setTimeColor()
	{
		NacSharedPreferences shared = this.getSharedPreferences();
		TextView tv = this.getTimeView();
		int color = shared.getTimeColor();

		this.setTextViewColor(tv, color);
	}

	/**
	 * Set the time view to its proper setting.
	 */
	public void setTimeView()
	{
		Context context = this.getContext();
		NacAlarm alarm = this.getAlarm();
		TextView tv = this.getTimeView();
		String time = alarm.getClockTime(context);
		String text = tv.getText().toString();

		if (!text.equals(time))
		{
			tv.setText(time);
		}
	}

	/**
	 * Set the vibrate button to its proper setting.
	 */
	public void setVibrateButton()
	{
		MaterialButton button = this.getVibrateButton();
		NacAlarm alarm = this.getAlarm();
		boolean shouldVibrate = alarm.getShouldVibrate();

		if (button.isChecked() != shouldVibrate)
		{
			button.setChecked(shouldVibrate);
		}
	}

	/**
	 * Set the ripple color of the vibrate button.
	 */
	private void setVibrateButtonRippleColor()
	{
		MaterialButton button = this.getVibrateButton();
		this.setMaterialButtonColor(button);
	}

	/**
	 * Set the volume image view.
	 */
	public void setVolumeImageView()
	{
		ImageView image = this.getVolumeImageView();
		NacAlarm alarm = this.getAlarm();
		int progress = alarm.getVolume();
		int resId;

		if (progress == 0)
		{
			resId = R.mipmap.volume_off;
		}
		else if ((progress > 0) && (progress <= 33))
		{
			resId = R.mipmap.volume_low;
		}
		else if ((progress > 33) && (progress <= 66))
		{
			resId = R.mipmap.volume_med;
		}
		else
		{
			resId = R.mipmap.volume_high;
		}

		Object tag = image.getTag();

		if ((tag == null) || (((int)tag) != resId))
		{
			image.setImageResource(resId);
			image.setTag(resId);
		}
	}

	/**
	 * Set the volume seekbar.
	 */
	public void setVolumeSeekBar()
	{
		NacAlarm alarm = this.getAlarm();
		SeekBar bar = this.getVolumeSeekBar();
		int volume = alarm.getVolume();

		//this.mVolume.incrementProgressBy(10);
		if (bar.getProgress() != volume)
		{
			bar.setProgress(volume);
		}
	}

	/**
	 * Set the volume seekbar color.
	 */
	public void setVolumeSeekBarColor()
	{
		SeekBar seekbar = this.getVolumeSeekBar();
		this.setSeekBarColor(seekbar);
	}

	/**
	 * Show the name dialog.
	 */
	private void showNameDialog()
	{
		Context context = this.getContext();
		NacAlarm alarm = this.getAlarm();

		// Get the fragment manager
		FragmentManager manager = ((AppCompatActivity)context).getSupportFragmentManager();

		// Create the dialog
		NacNameDialog dialog = new NacNameDialog();

		// Setup the dialog
		dialog.setDefaultName(alarm.getName());
		dialog.setOnNameEnteredListener(this);
		dialog.show(manager, NacNameDialog.TAG);
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
		boolean is24HourFormat = NacCalendar.Time.is24HourFormat(context);

		//FragmentManager fragmentManager = ((AppCompatActivity)context)
		//	.getSupportFragmentManager();
		//MaterialTimePicker timepicker = new MaterialTimePicker.Builder()
		//	.setHour(hour)
		//	.setMinute(minute)
		//	.setTimeFormat(is24HourFormat ? TimeFormat.CLOCK_24H : TimeFormat.CLOCK_12H)
		//	.build();

		//timepicker.addOnPositiveButtonClickListener(this);
		//timepicker.show(fragmentManager, "TimePicker");

		//this.mTimePicker = timepicker;

		TimePickerDialog dialog = new TimePickerDialog(context, this, hour, minute,
			is24HourFormat);
		dialog.show();
	}

	/**
	 * Show a toast saying that a user cannot delete an active alarm.
	 */
	private void toastDeleteActiveAlarmError()
	{
		Context context = this.getContext();
		NacSharedConstants cons = new NacSharedConstants(context);
		NacUtility.quickToast(context, cons.getErrorMessageActiveDelete());
	}

	/**
	 * Show a toast saying that a user cannot delete a snoozed alarm.
	 */
	private void toastDeleteSnoozedAlarmError()
	{
		Context context = this.getContext();
		NacSharedConstants cons = new NacSharedConstants(context);
		NacUtility.quickToast(context, cons.getErrorMessageSnoozedDelete());
	}

	/**
	 * Show a toast saying that a user cannot modify an active alarm.
	 */
	private void toastModifyActiveAlarmError()
	{
		Context context = this.getContext();
		NacSharedConstants cons = new NacSharedConstants(context);
		NacUtility.quickToast(context, cons.getErrorMessageActiveModify());
	}

	/**
	 * Show a toast saying that a user cannot modify a snoozed alarm.
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

		// Determine which message to show
		String requiredMessage = context.getString(R.string.message_nfc_required);
		String optionalMessage = context.getString(R.string.message_nfc_optional);
		String message = alarm.getShouldUseNfc() ? requiredMessage
			: optionalMessage;

		// Toast
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
		String message = alarm.getShouldRepeat() ? cons.getMessageRepeatEnabled()
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
		String message = alarm.getShouldVibrate() ? cons.getMessageVibrateEnabled()
			: cons.getMessageVibrateDisabled();

		NacUtility.quickToast(context, message);
	}

}
