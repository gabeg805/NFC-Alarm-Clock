package com.nfcalarmclock;

import android.app.Activity;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.TransitionDrawable;
import android.os.Handler;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.LinearSmoothScroller;
import android.support.v7.widget.RecyclerView;
import android.view.animation.Animation;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

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
	 * Height of the alarm card when collapsed.
	 */
	private int mCollapseHeight;

	/**
	 * Height of the alarm card when expanded.
	 */
	private int mExpandHeight;

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
	 */
	public NacCardView(Context context, View root)
	{
		this.mContext = context;
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
		this.mCollapseHeight = 0;
		this.mExpandHeight = 0;

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
	 * @return The color transition from the highlight color to the regular
	 *         card background color.
	 */
	private TransitionDrawable getExpandColorTransition()
	{
		//Context context = this.getContext();
		//int bg = NacUtility.getThemeAttrColor(context, R.attr.colorCard);
		//int highlight = NacUtility.getThemeAttrColor(context,
		//	R.attr.colorCardHighlight);
		//ColorDrawable[] color = {new ColorDrawable(highlight),
		//	new ColorDrawable(bg)};

		int startId = R.attr.colorCard;
		int endId = R.attr.colorCardExpanded;

		return this.getColorTransition(startId, endId);
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
	 * @return The height of the card when it is collapsed.
	 */
	public int getCollapseHeight()
	{
		return this.mCollapseHeight;
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
		return this.mExpandHeight;
	}

	/**
	 * @return The card height.
	 */
	public int getHeight()
	{
		//CardView card = this.getCardView();
		//ViewGroup.LayoutParams params = card.getLayoutParams();

		//return (params != null) ? params.height : 0;
		//return this.getCardView().getHeight();
		return NacUtility.getHeight(this.getCardView());
		//return this.getCardView().getMeasuredHeight();
	}

	/**
	 * @return The screen height.
	 */
	private int getScreenHeight()
	{
		Context context = this.getContext();
		int fabHeight = (int) context.getResources()
			.getDimension(R.dimen.pb_for_fab);
		int recyclerHeight = this.mRecyclerView.getHeight();

		return recyclerHeight - fabHeight;
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
	public void init()
	{
		CardView card = this.getCardView();
		int height = this.getHeight();
		int expandHeight = this.getExpandHeight();
		int collapseHeight = this.getCollapseHeight();
		ViewGroup.LayoutParams params = card.getLayoutParams();

		if (!this.isCollapsed())
		{
			this.collapse(false);
		}

		if ((height > 0) && (height == expandHeight) && (collapseHeight != 0)
			&& (params != null))
		{
			params.height = collapseHeight;
			card.setLayoutParams(params);
			card.requestLayout();
		}

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
		int height = this.getHeight();
		int expandHeight = this.getExpandHeight();
		int collapseHeight = this.getCollapseHeight();

		return (!this.isMeasured() || (height != collapseHeight)
			|| (!this.mSummary.isEnabled() && this.mExtra.isEnabled()));
	}

	/**
	 * @return True if the card view is measured, and False otherwise.
	 */
	public boolean isMeasured()
	{
		int expandHeight = this.getExpandHeight();
		int collapseHeight = this.getCollapseHeight();

		return ((expandHeight != 0) || (collapseHeight != 0));
	}

	/**
	 * Measure the alarm card.
	 */
	public void measure()
	{
		boolean collapsed = this.isCollapsed() || !this.isMeasured();

		this.measureExpandedHeight();
		this.measureCollapsedHeight();

		if (collapsed)
		{
			this.collapse(false);
		}
		else
		{
			this.expand(-1, false);
		}
	}

	/**
	 * Measure the height of the alarm card when it is collapsed.
	 */
	private void measureCollapsedHeight()
	{
		this.collapse(false);
		this.mHeader.requestLayout();
		this.mSummary.requestLayout();

		int timeHeight = NacUtility.getHeight(this.mHeader);
		int summaryHeight = NacUtility.getHeight(this.mSummary);
		this.mCollapseHeight = timeHeight + summaryHeight;
	}

	/**
	 * Measure the height of the alarm card when it is expanded.
	 */
	private void measureExpandedHeight()
	{
		this.expand(-1, false);
		this.mHeader.requestLayout();
		this.mExtra.requestLayout();

		int timeHeight = NacUtility.getHeight(this.mHeader);
		int extraHeight = NacUtility.getHeight(this.mExtra);
		this.mExpandHeight = timeHeight + extraHeight;
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
	 * Set the card height when it is expanded and the repeat checkbox has been
	 * pressed.
	 */
	public void setRepeatHeight(NacCardDays days, boolean repeat)
	{
		CardView card = this.getCardView();
		NacCardSlideAnimation animation = this.getAnimation();
		int daysHeight = days.getHeight();

		if (repeat)
		{
			animation.setHeights(this.mExpandHeight,
				this.mExpandHeight+daysHeight);
			this.mExpandHeight += daysHeight;
		}
		else
		{
			animation.setHeights(this.mExpandHeight,
				this.mExpandHeight-daysHeight);
			this.mExpandHeight -= daysHeight;
		}

		animation.setDuration(400);
		animation.setupForSkipListener();
		card.setAnimation(animation);
		card.startAnimation(animation);
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
