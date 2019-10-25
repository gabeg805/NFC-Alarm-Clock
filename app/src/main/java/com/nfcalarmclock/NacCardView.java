package com.nfcalarmclock;

import android.app.Activity;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.TransitionDrawable;
import android.os.Handler;
import android.view.animation.Animation;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.LinearSmoothScroller;
import androidx.recyclerview.widget.RecyclerView;

/**
 */
public class NacCardView
	implements NacCardSlideAnimation.OnAnimationListener
{

	/**
	 * Context.
	 */
	private Context mContext;

	/**
	 * Alarm.
	 */
	private NacAlarm mAlarm;

	/**
	 * The recycler view.
	 */
	private RecyclerView mRecyclerView;

	/**
	 * Card view.
	 */
	private CardView mCardView;

	/**
	 * Summary region.
	 */
	private RelativeLayout mSummary;

	/**
	 * Extra region.
	 */
	private LinearLayout mExtra;

	/**
	 * Header.
	 */
	private RelativeLayout mHeader;

	/**
	 * Divider.
	 */
	private View mDivider;

	/**
	 * Copy view.
	 */
	private RelativeLayout mCopy;

	/**
	 * Delete view.
	 */
	private RelativeLayout mDelete;

	/**
	 * Card animation.
	 */
	private NacCardSlideAnimation mAnimation;

	/**
	 * Card measurement
	 */
	private NacCardMeasure mMeasure;

	/**
	 * Collapse duration.
	 */
	private static final int COLLAPSE_DURATION = 350;

	/**
	 * Expand duration.
	 */
	private static final int EXPAND_DURATION = 450;

	/**
	 * Collapse color transition duration.
	 */
	private static final int COLLAPSE_COLOR_DURATION = 250;

	/**
	 * Expand color transition duration.
	 */
	private static final int EXPAND_COLOR_DURATION = 350;

	/**
	 * Wait time during init, before collapsing an expanded alarm card.
	 */
	private static final int INIT_WAIT = 200;

	/**
	 */
	public NacCardView(Context context, View root, NacCardMeasure measure)
	{
		this.mContext = context;
		this.mAlarm = null;
		this.mRecyclerView = (RecyclerView) ((Activity)context).findViewById(
			R.id.content_alarm_list);
		this.mCardView = (CardView) root.findViewById(R.id.nac_card);
		this.mSummary = (RelativeLayout) root.findViewById(R.id.nac_summary);
		this.mExtra = (LinearLayout) root.findViewById(R.id.nac_extra);
		this.mHeader = (RelativeLayout) root.findViewById(R.id.nac_header);
		this.mDivider = (View) root.findViewById(R.id.nac_divider);
		this.mCopy = (RelativeLayout) root.findViewById(R.id.nac_swipe_copy);
		this.mDelete = (RelativeLayout) root.findViewById(R.id.nac_swipe_delete);
		this.mAnimation = new NacCardSlideAnimation(this.mCardView,
			this.mSummary, this.mExtra);
		this.mMeasure = measure;

		this.mAnimation.setOnAnimationListener(this);
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
			NacCardSlideAnimation animation = this.getAnimation();
			int expandHeight = this.getExpandHeight();
			int collapseHeight = this.getCollapseHeight();

			animation.setDuration(COLLAPSE_DURATION);
			animation.setHeights(expandHeight, collapseHeight);
			animation.setupForClose();
			card.setAnimation(animation);
			card.startAnimation(animation);
		}
		else
		{
			this.setCollapseBackgroundColor();
			this.showCollapse();
		}
	}

	/**
	 * @see expand
	 */
	public void expand(int position)
	{
		this.expand(position, true);
	}

	/**
	 * Expand the alarm card.
	 */
	public void expand(int position, boolean animate)
	{
		if (animate)
		{
			CardView card = this.getCardView();
			NacCardSlideAnimation animation = this.getAnimation();
			int expandHeight = this.getExpandHeight();
			int collapseHeight = this.getCollapseHeight();

			animation.setDuration(EXPAND_DURATION);
			animation.setHeights(collapseHeight, expandHeight);
			animation.setupForOpen();
			card.setAnimation(animation);
			card.startAnimation(animation);
			this.scroll(position);
		}
		else
		{
			this.setExpandBackgroundColor();
			this.showExpand();
		}
	}

	/**
	 * @return The alarm.
	 */
	private NacAlarm getAlarm()
	{
		return this.mAlarm;
	}

	/**
	 * @return The card animation.
	 */
	private NacCardSlideAnimation getAnimation()
	{
		return this.mAnimation;
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
	 * @return The color transition from the highlight color to the regular
	 *         card background color.
	 */
	private TransitionDrawable getCollapseColorTransition()
	{
		int startId = R.attr.colorCardExpanded;
		int endId = R.attr.colorCard;

		return this.getColorTransition(startId, endId);
	}

	/**
	 * @return The height of the card when it is collapsed.
	 */
	public int getCollapseHeight()
	{
		return this.mMeasure.getCollapseHeight();
	}

	/**
	 * @return A color transition starting and ending on the provided colors.
	 */
	private TransitionDrawable getColorTransition(int startId, int endId)
	{
		Context context = this.getContext();
		int start = NacUtility.getThemeAttrColor(context, startId);
		int end = NacUtility.getThemeAttrColor(context, endId);
		ColorDrawable[] color = {new ColorDrawable(start),
			new ColorDrawable(end)};

		return new TransitionDrawable(color);
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
	 * @return The color transition from the highlight color to the regular
	 *         card background color.
	 */
	private TransitionDrawable getExpandColorTransition()
	{
		int startId = R.attr.colorCard;
		int endId = R.attr.colorCardExpanded;

		return this.getColorTransition(startId, endId);
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
	 * @return The screen height.
	 */
	private int getScreenHeight()
	{
		return this.mMeasure.getScreenHeight();
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
	 * Initialize the view state.
	 */
	public void init(NacAlarm alarm)
	{
		//// Might be easier to just have init be hideSwipeViews?
		//if (this.isExpanded())
		//{
		//	new Handler().postDelayed(new Runnable()
		//	{
		//		@Override
		//		public void run()
		//		{
		//			collapse();
		//		}
		//	}, INIT_WAIT);
		//}

		this.mAlarm = alarm;

		this.hideSwipeViews();
	}

	/**
	 * @return True if the alarm card is collapsed, and False otherwise.
	 */
	public boolean isCollapsed()
	{
		return !this.isExpanded();
	}

	/**
	 * Check if the card is completely visible.
	 *
	 * @return True if it is visible. False otherwise.
	 */
	private boolean isCompletelyVisible(int position)
	{
		if (position == RecyclerView.NO_POSITION)
		{
			return false;
		}

		LinearLayoutManager layoutManager = (LinearLayoutManager)
			this.mRecyclerView.getLayoutManager();

		// To-do: When deleting too many cards, things get weird and I'm not
		// sure how to fix it. Just implementing some bandaids to mitigate the
		// crashes.
		if (layoutManager != null)
		{
			View view = layoutManager.findViewByPosition(position);
			int y = (view != null) ? view.getTop() : -1;
			int expandHeight = this.getExpandHeight();
			int screenHeight = this.getScreenHeight();

			return ((y < 0) || (expandHeight + y) > screenHeight) ? false
				: true;
		}
		else
		{
			return false;
		}
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

	@Override
	public void onAnimationEnd(Animation animation)
	{
		CardView card = this.getCardView();
		TransitionDrawable transition = this.getCollapseColorTransition();

		card.setBackground(transition);
		transition.startTransition(COLLAPSE_COLOR_DURATION);
	}

	@Override
	public void onAnimationStart(Animation animation)
	{
		CardView card = this.getCardView();
		TransitionDrawable transition = this.getExpandColorTransition();

		card.setBackground(transition);
		transition.startTransition(EXPAND_COLOR_DURATION);
	}

	/**
	 * Scroll when the alarm card is partially visible.
	 */
	public void scroll(final int position)
	{
		if (this.isCompletelyVisible(position)
			|| (position == RecyclerView.NO_POSITION))
		{
			return;
		}

		new Handler().postDelayed(new Runnable()
		{
			@Override
			public void run()
			{
				mRecyclerView.smoothScrollToPosition(position);
			}
		}, EXPAND_DURATION+50);
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

		this.mDivider.setBackgroundTintList(ColorStateList.valueOf(themeColor));
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
	 * Show the collapse views.
	 */
	private void showCollapse()
	{
		this.mSummary.setVisibility(View.VISIBLE);
		this.mExtra.setVisibility(View.GONE);
		this.mSummary.setEnabled(true);
		this.mExtra.setEnabled(false);
	}

	/**
	 * Show the expand views.
	 */
	private void showExpand()
	{
		this.mSummary.setVisibility(View.GONE);
		this.mExtra.setVisibility(View.VISIBLE);
		this.mSummary.setEnabled(false);
		this.mExtra.setEnabled(true);
	}

	/**
	 * Toggle expand/collapse view state.
	 */
	public void toggle(int position)
	{
		if (this.isCollapsed())
		{
			this.expand(position);
		}
		else
		{
			this.collapse();
		}
	}

}
