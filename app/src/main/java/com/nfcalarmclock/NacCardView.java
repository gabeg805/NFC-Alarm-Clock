package com.nfcalarmclock;

import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Context;
import android.content.res.ColorStateList;
import android.view.animation.Animation;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import androidx.cardview.widget.CardView;

/**
 */
public class NacCardView
	implements NacCardSlideAnimation.OnAnimationListener
{

	/**
	 * Card state change listener.
	 */
	public interface OnStateChangeListener
	{
		public void onStateChange(NacCardView card, State state);
	}

	/**
	 * Card state.
	 */
	public enum State
	{
		COLLAPSED,
		EXPANDED
	}

	/**
	 * Context.
	 */
	private Context mContext;

	/**
	 * Alarm.
	 */
	private NacAlarm mAlarm;

	/**
	 * Card view.
	 */
	private CardView mCardView;

	/**
	 * Header.
	 */
	private RelativeLayout mHeader;

	/**
	 * Summary region.
	 */
	private LinearLayout mSummary;

	/**
	 * Extra region.
	 */
	private LinearLayout mExtra;

	/**
	 * First divider.
	 */
	private View mDivider1;

	/**
	 * Second divider.
	 */
	private LinearLayout mDivider2;

	/**
	 * Copy view.
	 */
	private RelativeLayout mCopy;

	/**
	 * Delete view.
	 */
	private RelativeLayout mDelete;

	/**
	 * Icon indicating the alarm has been snoozed.
	 */
	//private Button mSnoozeIcon;
	//private ImageView mSnoozeIcon;

	/**
	 * Card animation.
	 */
	private NacCardSlideAnimation mSlideAnimation;

	/**
	 * Color animator for highlighting the card.
	 */
	private ObjectAnimator mHighlightAnimator;

	/**
	 * Card measurement
	 */
	private NacCardMeasure mMeasure;

	/**
	 * State change listener.
	 */
	private OnStateChangeListener mListener;

	/**
	 * Card expand/collapse state.
	 */
	private State mState;

	/**
	 * Collapse duration.
	 */
	private static final int COLLAPSE_DURATION = 250;

	/**
	 * Expand duration.
	 */
	private static final int EXPAND_DURATION = 250;

	/**
	 * Collapse color transition duration.
	 */
	private static final int COLLAPSE_COLOR_DURATION = 200;

	/**
	 * Expand color transition duration.
	 */
	private static final int EXPAND_COLOR_DURATION = 200;

	/**
	 * Wait time during init, before collapsing an expanded alarm card.
	 */
	private static final int INIT_WAIT = 200;

	/**
	 * Highlight duration.
	 */
	private static final int HIGHLIGHT_DURATION = 600;

	/**
	 */
	public NacCardView(Context context, View root, NacCardMeasure measure)
	{
		this.mContext = context;
		this.mAlarm = null;
		this.mCardView = (CardView) root.findViewById(R.id.nac_card);
		this.mHeader = (RelativeLayout) root.findViewById(R.id.nac_header);
		this.mSummary = (LinearLayout) root.findViewById(R.id.nac_summary);
		this.mExtra = (LinearLayout) root.findViewById(R.id.nac_extra);
		this.mDivider1 = (View) root.findViewById(R.id.nac_divider1);
		this.mDivider2 = (LinearLayout) root.findViewById(R.id.nac_divider2);
		this.mCopy = (RelativeLayout) root.findViewById(R.id.nac_swipe_copy);
		this.mDelete = (RelativeLayout) root.findViewById(R.id.nac_swipe_delete);
		//this.mSnoozeIcon = (Button) root.findViewById(R.id.nac_snooze);
		//this.mSnoozeIcon = (ImageView) root.findViewById(R.id.nac_snooze);
		this.mSlideAnimation = new NacCardSlideAnimation(this.mCardView,
			this.mSummary, this.mExtra);
		this.mHighlightAnimator = null;
		this.mMeasure = measure;
		this.mListener = null;
		this.mState = State.COLLAPSED;

		this.getSlideAnimation().setOnAnimationListener(this);
	}

	/**
	 * Animate the card collapsing.
	 */
	public void animateCollapse()
	{
		CardView card = this.getCardView();
		NacCardSlideAnimation animation = this.getSlideAnimation();
		int expandHeight = this.getExpandHeight();
		int collapseHeight = this.getCollapseHeight();

		this.setState(State.COLLAPSED);
		this.resetHighlight();
		animation.setDuration(COLLAPSE_DURATION);
		animation.setHeights(expandHeight, collapseHeight);
		animation.setupForClose();
		card.setAnimation(animation);
		card.startAnimation(animation);
	}

	/**
	 * Animate the card expanding.
	 */
	public void animateExpand()
	{
		CardView card = this.getCardView();
		NacCardSlideAnimation animation = this.getSlideAnimation();
		int expandHeight = this.getExpandHeight();
		int collapseHeight = this.getCollapseHeight();

		this.setState(State.EXPANDED);
		this.resetHighlight();
		animation.setDuration(EXPAND_DURATION);
		animation.setHeights(collapseHeight, expandHeight);
		animation.setupForOpen();
		card.setAnimation(animation);
		card.startAnimation(animation);
	}

	/**
	 * Call the state change listener.
	 */
	public void callStateChangeListener()
	{
		OnStateChangeListener listener = this.getListener();

		if (listener != null)
		{
			listener.onStateChange(this, this.getState());
		}
	}

	/**
	 * @return The color animator from the expanded color, to the regular
	 *         background color.
	 */
	private ObjectAnimator createCollapseColorAnimator()
	{
		int startId = R.attr.colorCardExpanded;
		int endId = R.attr.colorCard;
		ObjectAnimator animator = this.createColorAnimator(startId, endId);

		animator.setDuration(COLLAPSE_COLOR_DURATION);

		return animator;
	}

	/**
	 * @return An object animator that will change the backgroun color of the
	 *         CardView from the specified colors.
	 */
	private ObjectAnimator createColorAnimator(int startId, int endId)
	{
		Context context = this.getContext();
		CardView card = this.getCardView();
		int start = NacUtility.getThemeAttrColor(context, startId);
		int end = NacUtility.getThemeAttrColor(context, endId);

		return ObjectAnimator.ofArgb(card, "backgroundColor", start, end);
	}

	/**
	 * @return The color animator from the regular background color, to the
	 *         expanded color.
	 */
	private ObjectAnimator createExpandColorAnimator()
	{
		int startId = R.attr.colorCard;
		int endId = R.attr.colorCardExpanded;
		ObjectAnimator animator = this.createColorAnimator(startId, endId);

		animator.setDuration(EXPAND_COLOR_DURATION);

		return animator;
	}

	/**
	 * @return The color animator from the expanded color, to the regular
	 *         background color.
	 */
	private ObjectAnimator createHighlightColorAnimator()
	{
		int startId = R.attr.colorCard;
		int endId = R.attr.colorCardExpanded;
		ObjectAnimator animator = this.createColorAnimator(startId, endId);

		animator.setDuration(HIGHLIGHT_DURATION);
		animator.setRepeatCount(1);
		animator.setRepeatMode(ObjectAnimator.REVERSE);

		return animator;
	}

	/**
	 * Collapse the card without any animations.
	 */
	public void doCollapse()
	{
		this.setState(State.COLLAPSED);
		this.setCollapseBackgroundColor();
		this.mSummary.setVisibility(View.VISIBLE);
		this.mExtra.setVisibility(View.GONE);
		this.mSummary.setEnabled(true);
		this.mExtra.setEnabled(false);
		this.callStateChangeListener();
	}

	/**
	 * Expand the card without any animations.
	 */
	public void doExpand()
	{
		this.setState(State.EXPANDED);
		this.setExpandBackgroundColor();
		this.mSummary.setVisibility(View.GONE);
		this.mExtra.setVisibility(View.VISIBLE);
		this.mSummary.setEnabled(false);
		this.mExtra.setEnabled(true);
		this.callStateChangeListener();
	}

	/**
	 * @see animateCollapse
	 */
	public void collapse()
	{
		this.animateCollapse();
	}

	/**
	 * @see animateExpand
	 */
	public void expand()
	{
		this.animateExpand();
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
		return this.mMeasure.getCollapseHeight();
	}

	/**
	 * @return The context.
	 */
	private Context getContext()
	{
		return this.mContext;
	}

	/**
	 * @return The copy view, which resides in the background of the card view.
	 */
	public View getCopyView()
	{
		return this.mCopy;
	}

	/**
	 * @return The delete view, which resides in the background of the card
	 *		   view.
	 */
	public View getDeleteView()
	{
		return this.mDelete;
	}

	/**
	 * @return The height of the card when it is expanded.
	 */
	public int getExpandHeight()
	{
		NacAlarm alarm = this.getAlarm();
		NacDayOfWeek dayButtons = this.mMeasure.getDayButtons();
		int daysHeight = this.mMeasure.getDayButtonsHeight();
		int expandHeight = this.mMeasure.getExpandHeight();

		return (dayButtons.getVisibility() == View.VISIBLE) ? expandHeight
			: expandHeight-daysHeight;
	}

	/**
	 * @return The card height.
	 */
	public int getHeight()
	{
		return NacUtility.getHeight(this.getCardView());
	}

	/**
	 * @return The highlight color animator.
	 */
	private ObjectAnimator getHighlightAnimator()
	{
		return this.mHighlightAnimator;
	}

	/**
	 * @return The OnStateChange listener.
	 */
	private OnStateChangeListener getListener()
	{
		return this.mListener;
	}

	/**
	 * @return The screen height.
	 */
	private int getScreenHeight()
	{
		return this.mMeasure.getScreenHeight();
	}

	/**
	 * @return The slide animation.
	 */
	private NacCardSlideAnimation getSlideAnimation()
	{
		return this.mSlideAnimation;
	}

	/**
	 * @return The card state.
	 */
	private State getState()
	{
		return this.mState;
	}

	/**
	 * Hide the swipe views.
	 */
	private void hideSwipeViews()
	{
		this.getCopyView().setVisibility(View.GONE);
		this.getDeleteView().setVisibility(View.GONE);
	}

	/**
	 * Highlight card.
	 */
	public void highlight()
	{
		ObjectAnimator animator = this.createHighlightColorAnimator();
		this.mHighlightAnimator = animator;

		animator.start();
	}

	/**
	 * Initialize the view state.
	 */
	public void init(NacSharedPreferences shared, NacAlarm alarm)
	{
		this.mAlarm = alarm;

		this.hideSwipeViews();
		this.setupSnoozeIcon(shared);
	}

	/**
	 * @return True if the alarm card is collapsed, and False otherwise.
	 */
	public boolean isCollapsed()
	{
		return !this.isExpanded();
	}

	/**
	 * @return True if the alarm card is in the collapsed state, and False
	 *         otherwise.
	 */
	public boolean isCollapsedState()
	{
		return (this.getState() == State.COLLAPSED);
	}

	/**
	 * @return True if the alarm card is expanded, and False otherwise.
	 */
	public boolean isExpanded()
	{
		int extraVisible = this.mExtra.getVisibility();
		int collapseHeight = this.getCollapseHeight();
		int height = this.getCardView().getMeasuredHeight();

		return (extraVisible == View.VISIBLE) || (height > collapseHeight);
	}

	/**
	 * @return True if the alarm card is in the expanded state, and False
	 *         otherwise.
	 */
	public boolean isExpandedState()
	{
		return (this.getState() == State.EXPANDED);
	}

	/**
	 */
	@Override
	public void onAnimationEnd(Animation animation)
	{
		ObjectAnimator animator = this.createCollapseColorAnimator();

		this.callStateChangeListener();
		animator.start();
	}

	/**
	 */
	@Override
	public void onAnimationStart(Animation animation)
	{
		ObjectAnimator animator = this.createExpandColorAnimator();

		this.callStateChangeListener();
		animator.start();
	}

	/**
	 * Reset the animator that highlights card.
	 */
	public void resetHighlight()
	{
		ObjectAnimator animator = this.getHighlightAnimator();

		if ((animator != null) && animator.isRunning())
		{
			animator.cancel();
		}
	}

	/**
	 * Set the background color for when the card is collapsed.
	 */
	public void setCollapseBackgroundColor()
	{
		CardView card = this.getCardView();
		int id = R.attr.colorCard;

		NacUtility.setBackground(card, id);
	}

	/**
	 * Set the background color for when the card is expanded.
	 */
	public void setExpandBackgroundColor()
	{
		CardView card = this.getCardView();
		int id = R.attr.colorCardExpanded;

		NacUtility.setBackground(card, id);
	}

	/**
	 * Set the color of the view.
	 */
	public void setColor(NacSharedPreferences shared)
	{
		int themeColor = shared.getThemeColor();
		ColorStateList tint = ColorStateList.valueOf(themeColor);
		int count = this.mDivider2.getChildCount();

		this.mDivider1.setBackgroundTintList(tint);

		for (int i=0; i < count; i++)
		{
			this.mDivider2.getChildAt(i).setBackgroundTintList(tint);
		}
	}

	/**
	 * Set the OnClick listeners.
	 */
	public void setOnClickListener(View root, View.OnClickListener listener)
	{
		RelativeLayout collapse = (RelativeLayout)
			root.findViewById(R.id.nac_collapse);
		RelativeLayout time = (RelativeLayout)
			root.findViewById(R.id.nac_time_parent);

		collapse.setOnClickListener(listener);
		time.setOnClickListener(listener);
		this.mSummary.setOnClickListener(listener);
		this.mHeader.setOnClickListener(listener);
	}

	/**
	 * Set the OnCreateContextMenu listeners.
	 */
	public void setOnCreateContextMenuListener(View root,
		View.OnCreateContextMenuListener listener)
	{
		RelativeLayout time = (RelativeLayout)
			root.findViewById(R.id.nac_time_parent);

		time.setOnCreateContextMenuListener(listener);
		this.mSummary.setOnCreateContextMenuListener(listener);
		this.mHeader.setOnCreateContextMenuListener(listener);
	}

	/**
	 * Set the listener for when the alarm card expands or collapses.
	 */
	public void setOnStateChangeListener(OnStateChangeListener listener)
	{
		this.mListener = listener;
	}

	/**
	 * Set the card state.
	 */
	private void setState(State state)
	{
		this.mState = state;
	}

	/**
	 * Setup the snooze icon.
	 */
	private void setupSnoozeIcon(NacSharedPreferences shared)
	{
		//NacAlarm alarm = this.getAlarm();
		//this.mSnoozeIcon.setVisibility(!alarm.isSnoozed(shared) ? View.VISIBLE
		//	: View.GONE);
	}

	/**
	 * Toggle expand/collapse view state.
	 */
	public void toggleState()
	{
		if (this.isCollapsed())
		{
			this.expand();
		}
		else
		{
			this.collapse();
		}
	}

}
