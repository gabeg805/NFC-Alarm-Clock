package com.nfcalarmclock;

import android.animation.Animator;
import android.animation.AnimatorInflater;
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
import androidx.appcompat.widget.SwitchCompat;
import androidx.cardview.widget.CardView;
import androidx.core.graphics.ColorUtils;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.button.MaterialButton;
//import com.google.android.material.timepicker.MaterialTimePicker;
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
		NacDialog.OnDismissListener,
		NacDayOfWeek.OnWeekChangedListener,
		SeekBar.OnSeekBarChangeListener,
		NacHeightAnimator.OnAnimateHeightListener
{

	/**
	 * Card collapsed listener.
	 */
	public interface OnCardCollapsedListener
	{
		@SuppressWarnings("unused")
        public void onCardCollapsed(NacCardHolder holder, NacAlarm alarm);
	}

	/**
	 * Card expanded listener.
	 */
	public interface OnCardExpandedListener
	{
		@SuppressWarnings({"unused", "EmptyMethod"})
		public void onCardExpanded(NacCardHolder holder, NacAlarm alarm);
	}

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
	 * Audio source button.
	 */
	private final MaterialButton mAudioSourceButton;

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

	/**
	 * Listener for when the alarm card is collapsed.
	 */
	private OnCardCollapsedListener mOnCardCollapsedListener;

	/**
	 * Listener for when the alarm card is expanded.
	 */
	private OnCardExpandedListener mOnCardExpandedListener;

	/**
	 * Listener for when the delete button is clicked.
	 */
	private OnDeleteClickedListener mOnDeleteClickedListener;

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
		this.mAudioSourceButton = root.findViewById(R.id.nac_audio_source);
		this.mNameButton = root.findViewById(R.id.nac_name);
		this.mDeleteButton = root.findViewById(R.id.nac_delete);
		this.mCardAnimator = new NacHeightAnimator(this.getCardView());
		this.mBackgroundColorAnimator = null;
		this.mHighlightAnimator = null;
		this.mOnCardCollapsedListener = null;
		this.mOnCardExpandedListener = null;
		this.mOnDeleteClickedListener = null;

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
	 * Call the card collapsed listener.
	 */
	private void callOnCardCollapsedListener()
	{
		OnCardCollapsedListener listener = this.getOnCardCollapsedListener();
		NacAlarm alarm = this.getAlarm();

		if ((listener != null) && this.isCollapsed())
		{
			listener.onCardCollapsed(this, alarm);
		}
	}

	/**
	 * Call the card expanded listener.
	 */
	private void callOnCardExpandedListener()
	{
		OnCardExpandedListener listener = this.getOnCardExpandedListener();
		NacAlarm alarm = this.getAlarm();

		if ((listener != null) && this.isExpanded())
		{
			listener.onCardExpanded(this, alarm);
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
		if ((animator != null) && animator.isRunning())
		{
			animator.cancel();
		}

		this.mHighlightAnimator = null;
	}

	/**
	 * @see #checkCanModifyAlarm()
	 *
	 * Same, but for deleting an alarm.
	 */
	public boolean checkCanDeleteAlarm()
	{
		NacSharedPreferences shared = this.getSharedPreferences();
		NacAlarm alarm = this.getAlarm();

		if (alarm.isActive())
		{
			this.toastDeleteActiveAlarmError();
		}
		else if (alarm.isSnoozed(shared))
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
		NacSharedPreferences shared = this.getSharedPreferences();
		NacAlarm alarm = this.getAlarm();

		if (alarm.isActive())
		{
			this.toastModifyActiveAlarmError();
		}
		else if (alarm.isSnoozed(shared))
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
		animator.setHeights(fromHeight, toHeight);
		animator.setDuration(COLLAPSE_DURATION);
		animator.start();
	}

	/**
	 * Create a ColorStateList object that is blended with the theme color.
	 */
	private ColorStateList createThemeColorStateList()
	{
		NacSharedPreferences shared = this.getSharedPreferences();
		int theme = shared.getThemeColor();
		int blendedTheme = ColorUtils.blendARGB(theme, Color.TRANSPARENT, 0.6f);

		return ColorStateList.valueOf(blendedTheme);
	}

	/**
	 * Delete the alarm card.
	 */
	public void delete()
	{
		OnDeleteClickedListener listener = this.getOnDeleteClickedListener();
		int pos = getAbsoluteAdapterPosition();

		if ((listener != null) && (pos >= 0))
		{
			listener.onDeleteClicked(pos);
		}
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
		this.setDismissView();
		//this.setCollapsedBackgroundColor();
	}

	/**
	 * Force the alarm card to be fully collapsed, without any animations.
	 */
	public void doCollapseForce()
	{
		this.doCollapse();
		this.getDismissParentView().setVisibility(View.GONE);
	}

	/**
	 * Act as if the day button was clicked.
	 */
	public void doDayButtonClick(NacCalendar.Day day)
	{
		NacAlarm alarm = this.getAlarm();
		alarm.toggleDay(day);

		if (!alarm.areDaysSelected())
		{
			alarm.setRepeat(false);
		}

		alarm.changed();
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
	 * Act as if the dismiss button was clicked.
	 */
	public void doDismissButtonClick()
	{
		Context context = this.getContext();
		NacAlarm alarm = this.getAlarm();

		NacContext.dismissAlarmActivity(context, alarm);
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
		//this.setExpandedBackgroundColor();
	}

	/**
	 * Act as if the media button was clicked.
	 */
	public void doMediaButtonClick()
	{
		this.startMediaActivity();
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
	}

	/**
	 * Act as if the repeat button was clicked.
	 */
	public void doRepeatButtonClick()
	{
		NacAlarm alarm = this.getAlarm();

		alarm.toggleRepeat();
		alarm.changed();
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
		alarm.changed();
		this.setDayOfWeek();
		this.setRepeatButton();
		this.setSummaryDaysView();
	}

	/**
	 * Act as if the switch was changed.
	 */
	public void doSwitchCheckedChanged(boolean state)
	{
		NacSharedPreferences shared = this.getSharedPreferences();
		NacAlarm alarm = this.getAlarm();

		if (!state && alarm.isInUse(shared))
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
		//this.setVibrateButton();
		this.toastVibrate();
	}

	/**
	 * Act as if the audio source button was clicked.
	 */
	public void doAudioSourceButtonClick()
	{
		this.showAudioSourceDialog();
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
	 * @return The audio source button.
	 */
	private MaterialButton getAudioSourceButton()
	{
		return this.mAudioSourceButton;
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

		return alarm.isSnoozed(shared)
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
	 * @return The listener for when the alarm card is collapsed.
	 */
	private OnCardCollapsedListener getOnCardCollapsedListener()
	{
		return this.mOnCardCollapsedListener;
	}

	/**
	 * @return The listener for when the alarm card is expanded.
	 */
	private OnCardExpandedListener getOnCardExpandedListener()
	{
		return this.mOnCardExpandedListener;
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
		this.mAlarm = alarm;

		this.initListeners(null);
		this.measureCard();
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
		this.setAudioSourceButtonRippleColor();
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
		this.getVolumeSeekBar().setOnSeekBarChangeListener(seek);
		this.getAudioSourceButton().setOnClickListener(click);
		this.getNameButton().setOnClickListener(click);
		this.getDeleteButton().setOnClickListener(click);
	}

	/**
	 * Initialize the various views.
	 */
	public void initViews()
	{
		this.setDismissView();
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
			this.expand();
		}
	}

	/**
	 * @return True if the alarm is in use, and False otherwise.
	 */
	public boolean isAlarmInUse()
	{
		NacSharedPreferences shared = this.getSharedPreferences();
		NacAlarm alarm = this.getAlarm();

		return alarm.isInUse(shared);
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

	/**
	 * Measure the alarm card.
	 */
	private void measureCard()
	{
		NacSharedPreferences shared = this.getSharedPreferences();
		CardView cardView = this.getCardView();

		if (shared.getCardIsMeasured() || this.isExpanded())
		{
			return;
		}

		this.doExpand();
		int expandHeight = NacUtility.getHeight(cardView);

		this.doCollapseForce();
		int collapseHeight = NacUtility.getHeight(cardView);

		this.getDismissParentView().setVisibility(View.VISIBLE);
		int dismissHeight = NacUtility.getHeight(cardView);

		shared.editCardHeightCollapsed(collapseHeight);
		shared.editCardHeightCollapsedDismiss(dismissHeight);
		shared.editCardHeightExpanded(expandHeight);
		shared.editCardIsMeasured(true);
	}

	/**
	 * Called when the card is collapsing.
	 *
	 * Used to set view visibility, animate the background color, and call the
	 * card collapsed listener.
	 */
	public void onAnimateCollapse(NacHeightAnimator animator)
	{
		if (animator.isLastUpdate())
		{
			this.doCollapse();
			this.animateCollapsedBackgroundColor();
			this.callOnCardCollapsedListener();
			this.getAlarm().unlatchChangeTracker();
			this.getAlarm().resetChangeTracker();
		}
	}

	/**
	 * Called when the card is expanding.
	 *
	 * Used to set view visibility, animate the background color, and call the
	 * card collapsed listener.
	 */
	public void onAnimateExpand(NacHeightAnimator animator)
	{
		if (animator.isFirstUpdate())
		{
			this.doExpand();
			this.animateExpandedBackgroundColor();
			this.getAlarm().latchChangeTracker();
			this.callOnCardExpandedListener();
		}
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
	public boolean onWeekChanged(NacDayButton button, NacCalendar.Day day)
	{
		this.respondToDayButtonClick(button, day);
		return true;
	}

	/**
	 */
	@Override
	public void onClick(View view)
	{
		int id = view.getId();
		if ((id == R.id.nac_header)
			|| (id == R.id.nac_summary)
			|| (id == R.id.nac_dismiss_parent)
			|| (id == R.id.nac_expand) || (id == R.id.nac_expand_other)
			|| (id == R.id.nac_collapse) || (id == R.id.nac_collapse_parent))
		{
			this.respondToCardClick(view);
		}
		else if (id == R.id.nac_time_parent)
		{
			this.respondToTimeClick(view);
		}
		else if (id == R.id.nac_dismiss)
		{
			this.respondToDismissButtonClick(view);
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
		else if (id == R.id.nac_media)
		{
			this.respondToMediaButtonClick(view);
		}
		else if (id == R.id.nac_audio_source)
		{
			this.respondToAudioSourceButtonClick(view);
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
		NacAlarm alarm = this.getAlarm();

		alarm.setHour(hr);
		alarm.setMinute(min);
		alarm.setEnabled(true);
		alarm.changed();
		this.setTimeView();
		this.setMeridianView();
		this.setMeridianColor();
		this.setSwitchView();
		this.setSummaryDaysView();
	}

	/**
	 * Perform haptic feedback on a view.
	 */
	private void performHapticFeedback(View view)
	{
		view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
	}

	/**
	 * Respond to the audio source button being clicked.
	 */
	private void respondToAudioSourceButtonClick(View view)
	{
		if (this.checkCanModifyAlarm())
		{
			this.doAudioSourceButtonClick();
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
		if (this.checkCanModifyAlarm())
		{
			this.doDayButtonClick(day);
		}

		this.performHapticFeedback(button);
	}

	/**
	 * Respond to the delete button being clicked.
	 */
	private void respondToDeleteButtonClick(View view)
	{
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
	 * Respond to the media button being clicked.
	 */
	private void respondToMediaButtonClick(View view)
	{
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
		if (this.checkCanModifyAlarm())
		{
			this.doNfcButtonClick();
		}

		this.performHapticFeedback(view);
	}

	/**
	 * Respond to the repeat button being clicked.
	 */
	private void respondToRepeatButtonClick(View view)
	{
		if (this.checkCanModifyAlarm())
		{
			this.doRepeatButtonClick();
		}

		this.performHapticFeedback(view);
	}

	/**
	 * Perform the repeat button long click action.
	 */
	public void respondToRepeatButtonLongClick()
	{
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
		NacSharedPreferences shared = this.getSharedPreferences();

		if (shared.getPreventAppFromClosing() && !this.checkCanModifyAlarm())
		{
			button.setChecked(!state);
		}
		else
		{
			this.doSwitchCheckedChanged(state);
		}

		this.performHapticFeedback(button);
	}

	/**
	 * Respond to the time being clicked.
	 */
	private void respondToTimeClick(View view)
	{
		if (!this.checkCanModifyAlarm())
		{
			this.performHapticFeedback(view);
			return;
		}

		this.doTimeClick();
	}

	/**
	 * Respond to the vibrate button being clicked.
	 */
	private void respondToVibrateButtonClick(View view)
	{
		if (this.checkCanModifyAlarm())
		{
			this.doVibrateButtonClick();
		}

		this.performHapticFeedback(view);
	}

	/**
	 * Set the ripple color of the audio source button.
	 */
	private void setAudioSourceButtonRippleColor()
	{
		ColorStateList ripple = this.createThemeColorStateList();
		this.getAudioSourceButton().setRippleColor(ripple);
	}

	/**
	 * Set the ripple color of the collapse button.
	 */
	private void setCollapseButtonRippleColor()
	{
		ColorStateList ripple = this.createThemeColorStateList();
		this.getCollapseButton().setRippleColor(ripple);
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
	 * Set the ripple color for each day in the day of week view.
	 */
	public void setDayOfWeekRippleColor()
	{
		ColorStateList ripple = this.createThemeColorStateList();
		NacDayOfWeek dow = this.getDayOfWeek();

		for (NacDayButton day : dow.getDayButtons())
		{
			MaterialButton button = day.getButton();
			button.setRippleColor(ripple);
		}
	}

	/**
	 * Set the ripple color of the delete button.
	 */
	private void setDeleteButtonRippleColor()
	{
		ColorStateList ripple = this.createThemeColorStateList();
		this.getDeleteButton().setRippleColor(ripple);
	}

	/**
	 * Set the ripple color for the dismiss button.
	 */
	public void setDismissButtonRippleColor()
	{
		ColorStateList ripple = this.createThemeColorStateList();
		this.getDismissButton().setRippleColor(ripple);
	}

	/**
	 * Set the dismiss view to its proper setting.
	 */
	public void setDismissView()
	{
		NacSharedPreferences shared = this.getSharedPreferences();
		NacAlarm alarm = this.getAlarm();

		int dismissVisibility = alarm.isSnoozed(shared) ? View.VISIBLE : View.GONE;
		int expandVisibility = (dismissVisibility == View.GONE)
			? View.VISIBLE : View.INVISIBLE;

		this.getDismissParentView().setVisibility(dismissVisibility);
		this.getExpandButton().setVisibility(expandVisibility);
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
		int count = headerDivider.getChildCount();
		int themeColor = shared.getThemeColor();
		ColorStateList tint = ColorStateList.valueOf(themeColor);

		for (int i=0; i < count; i++)
		{
			headerDivider.getChildAt(i).setBackgroundTintList(tint);
		}

		deleteDivider.setBackgroundTintList(tint);
	}

	/**
	 * Set the ripple color of the expand button.
	 */
	private void setExpandButtonRippleColor()
	{
		ColorStateList ripple = this.createThemeColorStateList();
		this.getExpandButton().setRippleColor(ripple);
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
	}

	/**
	 * Set the ripple color of the media button.
	 */
	public void setMediaButtonRippleColor()
	{
		ColorStateList ripple = this.createThemeColorStateList();
		this.getMediaButton().setRippleColor(ripple);
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
		int color = shared.getMeridianColor(meridian);

		this.getMeridianView().setTextColor(color);
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
	 * Set the ripple color of the name button.
	 */
	private void setNameButtonRippleColor()
	{
		ColorStateList ripple = this.createThemeColorStateList();
		this.getNameButton().setRippleColor(ripple);
	}

	/**
	 * Set the NFC button to its proper settings.
	 */
	private void setNfcButton()
	{
		MaterialButton button = this.getNfcButton();
		NacAlarm alarm = this.getAlarm();
		boolean useNfc = alarm.getUseNfc();

		button.setChecked(useNfc);
	}

	/**
	 * Set the ripple color of the NFC button.
	 */
	private void setNfcButtonRippleColor()
	{
		ColorStateList ripple = this.createThemeColorStateList();
		this.getNfcButton().setRippleColor(ripple);
	}

	/**
	 * Set the listener for when the alarm card is collapsed.
	 */
	public void setOnCardCollapsedListener(OnCardCollapsedListener listener)
	{
		this.mOnCardCollapsedListener = listener;
	}

	/**
	 * Set the listener for when the alarm card is expanded.
	 */
	public void setOnCardExpandedListener(OnCardExpandedListener listener)
	{
		this.mOnCardExpandedListener = listener;
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
	 * Set listener to delete the card.
	 *
	 * @param  listener  The delete listener.
	 */
	public void setOnDeleteClickedListener(OnDeleteClickedListener listener)
	{
		this.mOnDeleteClickedListener = listener;
	}

	/**
	 * Set the repeat button to its proper setting.
	 */
	private void setRepeatButton()
	{
		MaterialButton button = this.getRepeatButton();
		NacAlarm alarm = this.getAlarm();
		boolean repeat = alarm.getRepeat();

		if (alarm.areDaysSelected())
		{
			button.setEnabled(true);
			button.setChecked(repeat);
		}
		else
		{
			button.setChecked(false);
			button.setEnabled(false);
		}
	}

	/**
	 * Set the ripple color of the repeat button.
	 */
	private void setRepeatButtonRippleColor()
	{
		ColorStateList ripple = this.createThemeColorStateList();
		this.getRepeatButton().setRippleColor(ripple);
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
		MaterialButton button = this.getVibrateButton();
		NacAlarm alarm = this.getAlarm();
		boolean vibrate = alarm.getVibrate();

		button.setChecked(vibrate);
	}

	/**
	 * Set the ripple color of the vibrate button.
	 */
	private void setVibrateButtonRippleColor()
	{
		ColorStateList ripple = this.createThemeColorStateList();
		this.getVibrateButton().setRippleColor(ripple);
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
