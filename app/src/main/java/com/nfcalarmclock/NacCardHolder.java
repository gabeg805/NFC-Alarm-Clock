package com.nfcalarmclock;

import android.app.Activity;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.res.ColorStateList;
import android.os.Handler;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.TransitionDrawable;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.LinearSmoothScroller;
import android.transition.ChangeBounds;
import android.transition.Transition;
import android.transition.TransitionManager;
import android.util.DisplayMetrics;
import android.view.animation.AlphaAnimation;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.TimePicker;
import java.util.EnumSet;

import android.os.Bundle;
import android.content.Intent;

/**
 * Card view holder.
 */
public class NacCardHolder
	extends RecyclerView.ViewHolder
	implements View.OnClickListener,
		CompoundButton.OnCheckedChangeListener,
		TimePickerDialog.OnTimeSetListener,
		NacDialog.OnDismissListener,
		NacDayOfWeek.OnClickListener
{

	/**
	 * Delete listener.
	 */
	public interface OnDeleteListener
	{
		public void onDelete(int pos);
	}

	/**
	 * Expand/collapse view states.
	 */
	public enum State
	{
		NONE,
		EXPANDED,
		COLLAPSED
	}

	/**
	 * Alarm.
	 */
	private NacAlarm mAlarm;

	/**
	 * The root view.
	 */
	private View mRoot;

	/**
	 * The recycler view.
	 */
	public RecyclerView mRecyclerView;

	/**
	 * Card view.
	 */
	private CardView mCardView;

	/**
	 * On/off switch for an alarm.
	 */
	 private Switch mSwitch;

	/**
	 * Time text.
	 */
	private TextView mTime;

	/**
	 * Meridian text (AM/PM).
	 */
	private TextView mMeridian;

	/**
	 * Text of days to repeat.
	 */
	private TextView mSummaryDays;

	/**
	 * Text of days to repeat.
	 */
	private TextView mSummaryName;

	/**
	 * Buttons to select which days to repeat the alarm on.
	 */
	private NacDayOfWeek mDayButtons;

	/**
	 * Repeat checkbox.
	 */
	private CheckBox mRepeat;

	/**
	 * Vibrate checkbox.
	 */
	private CheckBox mVibrate;

	/**
	 * Sound.
	 */
	 private NacImageTextButton mSound;

	/**
	 * Name view.
	 */
	 private NacImageTextButton mName;

	/**
	 * Delete button.
	 */
	 private NacImageTextButton mDelete;

	/**
	 * Background color transition.
	 */
	private TransitionDrawable mTransition;

	/**
	 * The expand/collapse state of the card.
	 */
	private State mState;

	/**
	 * Height of the alarm card when collapsed.
	 */
	private int mCollapseHeight;

	/**
	 * Height of the alarm card when expanded.
	 */
	private int mExpandHeight;

	/**
	 * Delete listener.
	 */
	public OnDeleteListener mDeleteListener;

	/**
	 */
	public NacCardHolder(View root)
	{
		super(root);

		Activity activity = (Activity) root.getContext();
		this.mAlarm = null;
		this.mRoot = root;
		this.mRecyclerView = (RecyclerView) activity.findViewById(
			R.id.content_alarm_list);
		this.mCardView = (CardView) root.findViewById(R.id.nac_card);
		this.mSwitch = (Switch) root.findViewById(R.id.nac_switch);
		this.mTime = (TextView) root.findViewById(R.id.nac_time);
		this.mMeridian = (TextView) root.findViewById(R.id.nac_meridian);
		this.mSummaryDays = (TextView) root.findViewById(R.id.nac_summary_days);
		this.mSummaryName = (TextView) root.findViewById(R.id.nac_summary_name);
		this.mDayButtons = (NacDayOfWeek) root.findViewById(R.id.nac_days);
		this.mRepeat = (CheckBox) root.findViewById(R.id.nac_repeat);
		this.mVibrate = (CheckBox) root.findViewById(R.id.nac_vibrate);
		this.mSound = (NacImageTextButton) root.findViewById(R.id.nac_sound);
		this.mName = (NacImageTextButton) root.findViewById(R.id.nac_name);
		this.mDelete = (NacImageTextButton) root.findViewById(R.id.nac_delete);
		this.mTransition = null;
		this.mState = State.NONE;
		this.mCollapseHeight = 0;
		this.mExpandHeight = 0;
	}

	/**
	 * @see collapse
	 */
	public void collapse()
	{
		this.collapse(true);
	}

	/**
	 * Collapse the alarm card.
	 */
	public void collapse(boolean animate)
	{
		if (animate)
		{
			CardView card = this.getCardView();
			TransitionDrawable transition = this.getColorTransition();

			card.setBackground(transition);
			transition.startTransition(500);
		}
		else
		{
			this.setBackgroundColor(this.getState());
		}

		this.setState(State.COLLAPSED);
		this.setRegions(this.getState());
	}

	/**
	 * Delete the alarm card.
	 */
	public void delete()
	{
		if (this.mDeleteListener != null)
		{
			int pos = getAdapterPosition();

			this.mDeleteListener.onDelete(pos);
		}
	}

	/**
	 * @see expand
	 */
	public void expand()
	{
		this.expand(true);
	}

	/**
	 * Expand the alarm card.
	 */
	public void expand(boolean animate)
	{
		if (animate)
		{
			CardView card = this.getCardView();
			Transition transition = new ChangeBounds().setDuration(400);

			TransitionManager.beginDelayedTransition(card, transition);
		}

		this.setState(State.EXPANDED);
		this.setRegions(this.getState());
		this.setBackgroundColor(this.getState());

		if (animate)
		{
			this.scrollOnPartiallyVisible();
		}
	}

	/**
	 * Focus the alarm card.
	 */
	public void focus(boolean state)
	{
		if (!state)
		{
			return;
		}

		CardView card = this.getCardView();
		AlphaAnimation animation = new AlphaAnimation(0.0f, 1.0f);

		animation.setDuration(1000);
		card.startAnimation(animation);
	}

	/**
	 * @return The alarm.
	 */
	public NacAlarm getAlarm()
	{
		return this.mAlarm;
	}

	/**
	 * @return The card height.
	 */
	private int getCardHeight()
	{
		return (this.isCollapsed()) ? this.getCollapseHeight()
			: this.getExpandHeight();
	}

	/**
	 * @return The card view.
	 */
	public CardView getCardView()
	{
		return this.mCardView;
	}

	/**
	 * @return The height of the card when it is collapsed.
	 */
	public int getCollapseHeight()
	{
		return this.mCollapseHeight;
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
		View root = this.getRoot();

		return (RelativeLayout) root.findViewById(R.id.nac_swipe_copy);
	}

	/**
	 * @return The day of week buttons.
	 */
	public NacDayOfWeek getDayButtons()
	{
		return this.mDayButtons;
	}

	/**
	 * @return The delete view, which resides in the background of the card
	 *		   view.
	 */
	public View getDeleteView()
	{
		View root = this.getRoot();

		return (RelativeLayout) root.findViewById(R.id.nac_swipe_delete);
	}

	/**
	 * @return The height of the card when it is expanded.
	 */
	public int getExpandHeight()
	{
		return this.mExpandHeight;
	}

	/**
	 * @return The repeat checkbox.
	 */
	public CheckBox getRepeat()
	{
		return this.mRepeat;
	}

	/**
	 * @return The root view.
	 */
	public View getRoot()
	{
		return this.mRoot;
	}

	/**
	 * @return The screen height.
	 */
	private int getScreenHeight()
	{
		return this.mRecyclerView.getHeight();
	}

	/**
	 * @return The expand/collapse state.
	 */
	private State getState()
	{
		return this.mState;
	}

	/**
	 * @return The color transition from the highlight color to the regular
	 *         card background color.
	 */
	private TransitionDrawable getColorTransition()
	{
		Context context = this.getContext();
		int bg = NacUtility.getThemeAttrColor(context, R.attr.colorCard);
		int highlight = NacUtility.getThemeAttrColor(context,
			R.attr.colorCardHighlight);
		ColorDrawable[] color = {new ColorDrawable(highlight),
			new ColorDrawable(bg)};

		return new TransitionDrawable(color);
	}

	/**
	 * Initialize the alarm card.
	 *
	 * @param  alarm  The alarm to use to populate data in the alarm card.
	 * @param  wasAdded  Indicator for whether or not the card should be
	 *					 focused.
	 */
	public void init(NacAlarm alarm, boolean wasAdded)
	{
		this.mAlarm = alarm;

		if (!this.isCollapsed())
		{
			this.collapse(false);
		}

		this.focus(wasAdded);
		this.setSwipeViews();
		this.setSwitch();
		this.setTime();
		this.setSummaryDays();
		this.setRepeat();
		this.setDays();
		this.setVibrate();
		this.setSound();
		this.setName();
		this.setColors();
		this.setListeners();
	}

	/**
	 * @return True if the card is collapsed and false otherwise.
	 */
	public boolean isCollapsed()
	{
		return this.isCollapsed(this.mState);
	}

	/**
	 * @see isCollapsed
	 */
	public boolean isCollapsed(State state)
	{
		return (state == State.COLLAPSED);
	}

	/**
	 * Check if the card is completely visible.
	 *
	 * @return True if it is visible. False otherwise.
	 */
	private boolean isCompletelyVisible()
	{
		int position = getAdapterPosition();

		if (position == RecyclerView.NO_POSITION)
		{
			return false;
		}

		LinearLayoutManager layoutManager = (LinearLayoutManager)
			this.mRecyclerView.getLayoutManager();

		// When deleting too many cards, things get weird and I'm not sure how
		// to fix it. Just implementing some bandaids to mitigate the crashes.
		if (layoutManager != null)
		{
			int y = layoutManager.findViewByPosition(position).getTop();
			int cardHeight = this.getCardHeight();
			int screenHeight = this.getScreenHeight();

			return ((y < 0) || (cardHeight + y) > screenHeight) ? false
				: true;
		}
		else
		{
			return false;
		}
	}

	/**
	 * @return True if the card is expanded and false otherwise.
	 */
	public boolean isExpanded()
	{
		return this.isExpanded(this.mState);
	}

	/**
	 * @see isExpanded
	 */
	public boolean isExpanded(State state)
	{
		return (state == State.EXPANDED);
	}

	/**
	 * Measure the alarm card.
	 */
	public void measure()
	{
		State state = this.getState();

		this.measureExpandedHeight();
		this.measureCollapsedHeight();

		if (this.isExpanded(state))
		{
			this.expand(false);
		}
		else
		{
			this.collapse(false);
		}
	}

	/**
	 * Measure the height of the alarm card when it is collapsed.
	 */
	private void measureCollapsedHeight()
	{
		CardView card = this.getCardView();

		this.collapse(false);
		card.invalidate();
		card.requestLayout();
		this.mCollapseHeight = NacUtility.getHeight(card);
	}

	/**
	 * Measure the height of the alarm card when it is expanded.
	 */
	private void measureExpandedHeight()
	{
		CardView card = this.getCardView();

		this.expand(false);
		card.invalidate();
		card.requestLayout();
		this.mExpandHeight = NacUtility.getHeight(card);
	}

	/**
	 * Save the repeat state of the alarm.
	 */
	@Override
	public void onCheckedChanged(CompoundButton v, boolean state)
	{
		NacAlarm alarm = this.getAlarm();
		int id = v.getId();

		if (id == R.id.nac_switch)
		{
			alarm.setEnabled(state);
			this.setSummaryDays();
		}
		else if (id == R.id.nac_repeat)
		{
			NacDayOfWeek dayButtons = this.getDayButtons();
			alarm.setRepeat(state);

			if (state && alarm.getDays().isEmpty())
			{
				alarm.setDays(NacSharedPreferences.DEFAULT_DAYS);
			}

			this.setDays();
			this.setSummaryDays();
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
		NacAlarm alarm = this.getAlarm();
		CheckBox repeat = this.getRepeat();
		NacDayOfWeek dayButtons = this.getDayButtons();

		alarm.toggleIndex(index);

		if (alarm.getDays().isEmpty() && repeat.isChecked())
		{
			this.setDays();
			repeat.setChecked(false);
		}

		alarm.changed();
		this.setSummaryDays();
	}

	/**
	 * When delete button is clicked, call the delete listener to delete the view.
	 *
	 * @param  view  The delete view.
	 */
	@Override
	public void onClick(View view)
	{
		int id = view.getId();

		if (id == R.id.nac_header)
		{
			this.toggleViewState();
		}
		else if (id == R.id.nac_summary)
		{
			this.expand();
		}
		else if (id == R.id.nac_collapse)
		{
			this.collapse();
		}
		else if (id == R.id.nac_time_parent)
		{
			this.showTimePickerDialog();
		}
		else if (id == R.id.nac_sound)
		{
			this.showSoundDialog();
		}
		else if (id == R.id.nac_name)
		{
			this.showNameDialog();
		}
		else if (id == R.id.nac_delete)
		{
			this.delete();
		}
	}

	/**
	 * Notify alarm listener that the alarm has been modified.
	 */
	@Override
	public boolean onDismissDialog(NacDialog dialog)
	{
		Object data = dialog.getData();
		String name = (data != null) ? (String) data : "";
		NacAlarm alarm = this.getAlarm();

		alarm.setName(name);
		alarm.changed();
		this.setName();

		return true;
	}

	@Override
	public void onTimeSet(TimePicker tp, int hr, int min)
	{
		NacAlarm alarm = this.getAlarm();

		alarm.setHour(hr);
		alarm.setMinute(min);
		alarm.setEnabled(true);
		alarm.changed();
		this.setTime();
		this.setSwitch();

		if (!alarm.getRepeat() || alarm.getDays().isEmpty())
		{
			this.setSummaryDays();
		}
	}

	/**
	 * Scroll when the alarm card is partially visible.
	 */
	public void scrollOnPartiallyVisible()
	{
		int delay = 200;

		if (this.isCompletelyVisible())
		{
			return;
		}

		new Handler().postDelayed(new Runnable()
		{
			@Override
			public void run()
			{
				int position = getAdapterPosition();

				if (position == RecyclerView.NO_POSITION)
				{
					return;
				}

				LinearLayoutManager layoutManager = (LinearLayoutManager)
					mRecyclerView.getLayoutManager();

				if ((layoutManager == null))
				{
					return;
				}

				Context context = getContext();
				int y = layoutManager.findViewByPosition(position).getTop();
				int snap = (y >= 0) ? LinearSmoothScroller.SNAP_TO_END
					: LinearSmoothScroller.SNAP_TO_START;
				SmoothScroller scroller = new SmoothScroller(context, position, snap);

				layoutManager.startSmoothScroll(scroller);
			}
		}, delay);
	}

	/**
	 * Set the background color.
	 */
	public void setBackgroundColor(State state)
	{
		CardView card = this.getCardView();
		int id = (this.isExpanded(state)) ? R.attr.colorCardExpanded
			: R.attr.colorCard;

		NacUtility.setBackground(card, id);
	}

	/**
	 * Set the colors of the various views.
	 */
	public void setColors()
	{
		Context context = this.getContext();
		View root = this.getRoot();
		View divider = (View) root.findViewById(R.id.nac_divider);
		NacSharedPreferences shared = new NacSharedPreferences(context);

		int[][] states = new int[][] {
			new int[] { android.R.attr.state_checked},
			new int[] {-android.R.attr.state_checked}};
		int[] thumbColors = new int[] {shared.getThemeColor(), Color.LTGRAY};
		int[] trackColors = new int[] {shared.getThemeColor(), Color.GRAY};
		ColorStateList thumbStateList = new ColorStateList(states, thumbColors);
		ColorStateList trackStateList = new ColorStateList(states, trackColors);
		String meridian = this.getAlarm().getMeridian(context);
		int timeColor = shared.getTimeColor();
		int meridianColor = (meridian == "AM") ? shared.getAmColor()
			: shared.getPmColor();

		this.mSwitch.getThumbDrawable().setTintList(thumbStateList);
		this.mSwitch.getTrackDrawable().setTintList(trackStateList);
		this.mTime.setTextColor(timeColor);
		this.mMeridian.setTextColor(meridianColor);
		this.mSummaryDays.setTextColor(shared.getDaysColor());
		this.mSummaryName.setTextColor(shared.getNameColor());
		divider.setBackgroundTintList(ColorStateList.valueOf(
			shared.getThemeColor()));
	}

	/**
	 * Set which day buttons are enabled.
	 */
	public void setDays()
	{
		NacAlarm alarm = this.getAlarm();
		NacDayOfWeek dayButtons = this.getDayButtons();
		CheckBox repeat = this.getRepeat();
		EnumSet<NacCalendar.Day> days = alarm.getDays();

		dayButtons.setDays(days);
		dayButtons.setVisibility((days.isEmpty() || !repeat.isChecked())
			? View.GONE : View.VISIBLE);
	}

	/**
	 * Set the listeners of the various views.
	 */
	public void setListeners()
	{
		View root = this.getRoot();
		RelativeLayout header = (RelativeLayout) root.findViewById(
			R.id.nac_header);
		RelativeLayout summary = (RelativeLayout) root.findViewById(
			R.id.nac_summary);
		RelativeLayout collapse = (RelativeLayout) root.findViewById(
			R.id.nac_collapse);
		RelativeLayout time = (RelativeLayout) root.findViewById(
			R.id.nac_time_parent);

		time.setOnClickListener(this);
		header.setOnClickListener(this);
		summary.setOnClickListener(this);
		collapse.setOnClickListener(this);
		this.mSwitch.setOnCheckedChangeListener(this);
		this.mDayButtons.setOnClickListener((NacDayOfWeek.OnClickListener)this);
		this.mRepeat.setOnCheckedChangeListener(this);
		this.mVibrate.setOnCheckedChangeListener(this);
		this.mSound.setOnClickListener(this);
		this.mName.setOnClickListener(this);
		this.mDelete.setOnClickListener(this);
	}

	/**
	 * Set the name of the alarm.
	 */
	public void setName()
	{
		NacAlarm alarm = this.getAlarm();
		View root = this.getRoot();
		String name = alarm.getName();
		String text = name + "  ";
		boolean focus = true;
		RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
			RelativeLayout.LayoutParams.WRAP_CONTENT,
			RelativeLayout.LayoutParams.WRAP_CONTENT);
		int margin = root.getResources().getDimensionPixelSize(
			R.dimen.sp_text);

		if (name.isEmpty())
		{
			name = NacSharedPreferences.DEFAULT_NAME_MESSAGE;
			text = "";
			focus = false;
			margin = 0;
		}

		params.setMarginStart(margin);
		this.mSummaryName.setLayoutParams(params);
		this.mSummaryName.setText(text);
		this.mName.setText(name);
		this.mName.setFocus(focus);
	}

	/**
	 * Set listener to delete the card.
	 *
	 * @param  listener  The delete listener.
	 */
	public void setOnDeleteListener(OnDeleteListener listener)
	{
		this.mDeleteListener = listener;
	}

	/**
	 * Set the expand/collapse state for the two regions on the alarm card.
	 */
	private void setRegions(State state)
	{
		View root = this.getRoot();
		RelativeLayout summary = (RelativeLayout) root.findViewById(
			R.id.nac_summary);
		LinearLayout extra = (LinearLayout) root.findViewById(R.id.nac_extra);
		boolean enabled = this.isCollapsed(state);

		summary.setVisibility((enabled) ? View.VISIBLE : View.GONE);
		extra.setVisibility((enabled) ? View.GONE : View.VISIBLE);
		summary.setEnabled(enabled);
		extra.setEnabled(!enabled);
	}

	/**
	 * Set the repeat checked status.
	 */
	public void setRepeat()
	{
		NacAlarm alarm = this.getAlarm();
		CheckBox repeat = this.getRepeat();

		repeat.setChecked(alarm.getRepeat());
	}

	/**
	 * Set the repeat days text.
	 */
	public void setSummaryDays()
	{
		NacAlarm alarm = this.getAlarm();
		String string = NacCalendar.toString(alarm);

		this.mSummaryDays.setText(string);
	}

	/**
	 * Set the sound.
	 */
	public void setSound()
	{
		NacAlarm alarm = this.getAlarm();
		String path = alarm.getSoundPath();
		String name = (!path.isEmpty()) ? alarm.getSoundName()
			: NacSharedPreferences.DEFAULT_SOUND_MESSAGE;
		boolean focus = (!path.isEmpty());

		this.mSound.setText(name);
		this.mSound.setFocus(focus);
	}

	/**
	 * Set the expand/collapse state.
	 */
	private void setState(State state)
	{
		this.mState = state;
	}

	/**
	 * Setup the swipe views.
	 */
	private void setSwipeViews()
	{
		this.getCopyView().setVisibility(View.GONE);
		this.getDeleteView().setVisibility(View.GONE);
	}

	/**
	 * Set the switch enabled status.
	 */
	public void setSwitch()
	{
		NacAlarm alarm = this.getAlarm();

		this.mSwitch.setChecked(alarm.getEnabled());
	}

	/**
	 * Set the time.
	 */
	public void setTime()
	{
		Context context = this.getContext();
		NacAlarm alarm = this.getAlarm();
		String time = alarm.getTime(context);
		String meridian = alarm.getMeridian(context);

		this.mTime.setText(time);
		this.mMeridian.setText(meridian);
	}

	/**
	 * Set the vibration checked status.
	 */
	public void setVibrate()
	{
		NacAlarm alarm = this.getAlarm();

		this.mVibrate.setChecked(alarm.getVibrate());
	}

	/**
	 * Show name dialog.
	 */
	public void showNameDialog()
	{
		Context context = this.getContext();
		NacAlarm alarm = this.getAlarm();
		NacNameDialog dialog = new NacNameDialog();

		dialog.build(context, R.layout.dlg_alarm_name);
		dialog.saveData(alarm.getName());
		dialog.addOnDismissListener(this);
		dialog.show();
	}

	/**
	 * Show sound dialog.
	 */
	public void showSoundDialog()
	{
		Context context = this.getContext();
		NacAlarm alarm = this.getAlarm();
		Intent intent = NacAlarmParcel.toIntent(context,
			NacPagerFragment.class, alarm);

		context.startActivity(intent);
	}

	/**
	 * Show time picker dialog.
	 */
	public void showTimePickerDialog()
	{
		Context context = this.getContext();
		NacAlarm alarm = this.getAlarm();
		int hour = alarm.getHour();
		int minute = alarm.getMinute();
		boolean format = alarm.is24HourFormat(context);
		TimePickerDialog dialog = new TimePickerDialog(context, this, hour,
			minute, format);

		dialog.show();
	}

	/**
	 * Toggle expand/collapse view state.
	 */
	public void toggleViewState()
	{
		if (this.isExpanded())
		{
			this.collapse();
		}
		else if (this.isCollapsed())
		{
			this.expand();
		}
	}

	/**
	 * Unfocus the alarm card.
	 */
	public void unfocus()
	{
		this.getRoot().clearAnimation();
	}

	/**
	 * Smooth scroller
	 */
	public static class SmoothScroller
		extends LinearSmoothScroller
	{

		/**
		 * Scrolling snap preference.
		 */
		private final int mSnap;

		/**
		 * Speed to scroll in millimeters per pixel.
		 */
		private final float mSpeed = 100;

		/**
		 */
		public SmoothScroller(Context context, int position, int snap)
		{
			super(context);

			this.mSnap = snap;

			setTargetPosition(position);
		}

		/**
		 */
		@Override
		protected float calculateSpeedPerPixel(DisplayMetrics dm)
		{
			return mSpeed / dm.densityDpi;
		}

		/**
		 */
		@Override
		protected int getVerticalSnapPreference()
		{
			return mSnap;
		}

	}

}
