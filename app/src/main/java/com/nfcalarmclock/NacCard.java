package com.nfcalarmclock;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.TransitionDrawable;
import android.os.Handler;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.LinearSmoothScroller;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.MenuInflater;
import android.view.View;
import android.widget.RelativeLayout;

/**
 * @brief Holder of all important views.
 */
public class NacCard
    extends RecyclerView.ViewHolder
    implements View.OnClickListener
{

	/**
	 * @brief Context.
	 */
    public Context mContext;

	/**
	 * @brief The recycler view.
	 */
    public RecyclerView mRecyclerView;

	/**
	 * @brief The alarm card view.
	 */
    public CardView mCard;
	public RelativeLayout mBackgroundCopy;
	public RelativeLayout mBackgroundDelete;

	/**
	 * @brief Summary and extra regions in the alarm card.
	 */
	public NacCardRegion mRegion;

	/**
	 * @brief The on/off switch for the alarm.
	 */
    public NacCardSwitch mSwitch;

	/**
	 * @brief The alarm time.
	 */
    public NacCardTime mTime;

	/**
	 * @brief Repeat checkbox.
	 */
    public NacCardRepeat mRepeat;

	/**
	 * @brief Sound selector.
	 */
    public NacCardSound mSound;

	/**
	 * @brief Vibrate checkbox.
	 */
    public NacCardVibrate mVibrate;

	/**
	 * @brief Name label and selector.
	 */
    public NacCardName mName;

	/**
	 * @brief Button to delete the alarm card.
	 */
    public NacCardDelete mDelete;

	/**
	 * @brief Background color transition.
	 */
	private TransitionDrawable mTransition = null;

	/**
	 * @brief Expand/collapse state.
	 */
	public class CardState
	{
		public final static byte EXPANDED = 1;
		public final static byte COLLAPSED = 2;
	}

	public byte mCardState = CardState.COLLAPSED;

    /**
     * @brief Define all views in the holder.
	 *
	 * @param  c  The activity's context.
	 * @param  r  The root view.
     */
    public NacCard(Context c, View r)
    {
        super(r);
        this.mContext = c;
        this.mCard = (CardView) r.findViewById(R.id.view_card_alarm);
		this.mBackgroundCopy = (RelativeLayout) r.findViewById(R.id.view_background_copy);
		this.mBackgroundDelete = (RelativeLayout) r.findViewById(R.id.view_background_delete);
		this.mRecyclerView = (RecyclerView) ((Activity)c).findViewById(
			R.id.content_alarm_list);

		this.mRegion = new NacCardRegion(r);
        this.mTime = new NacCardTime(c, r);
        this.mSwitch = new NacCardSwitch(r);
        this.mRepeat = new NacCardRepeat(r);
        this.mSound = new NacCardSound(c, r);
        this.mVibrate = new NacCardVibrate(r);
        this.mName = new NacCardName(r);
        this.mDelete = new NacCardDelete(r);
	}

    /**
     * @brief Initialize the alarm card.
	 *
	 * @param  alarm  The alarm to use to populate data in the alarm card.
	 * @param  pos  The position of the alarm card in the recycler view.
     */
    public void init(Alarm alarm)
    {
		this.mBackgroundCopy.setVisibility(View.GONE);
		this.mBackgroundDelete.setVisibility(View.GONE);

		int pos = this.getAdapterPosition();

		this.mRegion.init();
        this.mTime.init(alarm);
        this.mSwitch.init(alarm);
        this.mRepeat.init(alarm);
        this.mSound.init(alarm);
        this.mVibrate.init(alarm);
        this.mName.init(alarm);
		this.mDelete.init(pos);

		this.mCard.setOnClickListener(this);
		this.mRegion.setExpandListener(this);
		this.mRegion.setCollapseListener(this);

		if (this.mCardState == CardState.EXPANDED)
		{
			collapse();
		}
    }

	/**
	 * @brief Focus the alarm card.
	 */
	public void focus(boolean state)
	{
		if (!state)
		{
			return;
		}

		int duration = 2200;
		int bg = NacUtility.getThemeAttrColor(this.mContext, R.attr.colorCard);
		int highlight= NacUtility.getThemeAttrColor(this.mContext,
			R.attr.colorCardExpanded);
		ColorDrawable[] color = {new ColorDrawable(highlight),
			new ColorDrawable(bg)};
		this.mTransition = new TransitionDrawable(color);

		this.mCard.setBackground(this.mTransition);
		this.mTransition.startTransition(duration);
	}

	/**
	 * @brief Unfocus the alarm card.
	 */
	public void unfocus()
	{
		if (this.mTransition != null)
		{
			this.mTransition.resetTransition();
			this.setBackgroundColor(CardState.COLLAPSED);

			this.mTransition = null;
		}
	}

    /**
     * @brief Expand the alarm card.
     */
    public void expand()
    {
		this.mCardState = CardState.EXPANDED;

		this.unfocus();
		mRegion.expandAndAnimate();
		this.setBackgroundColor(this.mCardState);
		this.scrollOnPartiallyVisible();
    }

    /**
     * @brief Collapse the alarm card.
     */
    public void collapse()
    {
		this.mCardState = CardState.COLLAPSED;

		mRegion.collapseAndAnimate();
		this.setBackgroundColor(this.mCardState);
    }

	/**
	 * @brief Scroll when the alarm card is partially visible.
	 */
	public void scrollOnPartiallyVisible()
	{
		if (this.isCompletelyVisible())
		{
			return;
		}

		int delay = 200;

		new Handler().postDelayed(new Runnable()
		{
			@Override
			public void run()
			{
				int pos = getAdapterPosition();

				mRecyclerView.getLayoutManager().startSmoothScroll(
					new NacSmoothScroller(mContext, pos,
						LinearSmoothScroller.SNAP_TO_END));
			}
		}, delay);
	}

	/**
	 * @brief Set the background color.
	 */
	private void setBackgroundColor(byte state)
	{
		if (state == CardState.EXPANDED)
		{
			NacUtility.setBackground(this.mContext, this.mCard,
				R.attr.colorCardExpanded);
		}
		else if (state == CardState.COLLAPSED)
		{
			NacUtility.setBackground(this.mContext, this.mCard,
				R.attr.colorCard);
		}
	}

	/**
	 * @brief Set listener to delete the card.
	 *
	 * @param  listener  The card delete listener.
	 */
	public void setDeleteListener(View.OnClickListener listener)
	{
		this.mDelete.setListener(this);
		this.mDelete.setListener(listener);
	}

	/**
	 * @brief Return the card height.
	 *
	 * @return The card height.
	 */
	private int getCardHeight()
	{
		int regionheight = this.mRegion.getHeight();
		int timeheight = this.mTime.getHeight();
		int switchheight = this.mSwitch.getHeight();

		return (timeheight >= switchheight) ? regionheight + timeheight 
			: regionheight + switchheight;
	}

	/**
	 * @brief Return the screen height.
	 *
	 * @return The screen height.
	 */
	private int getScreenHeight()
	{
		return this.mRecyclerView.getHeight();
	}

	/**
	 * @return True if the card is expanded and false otherwise.
	 */
	public boolean isExpanded()
	{
		return (this.mCardState == CardState.EXPANDED);
	}

	/**
	 * @return True if the card is collapsed and false otherwise.
	 */
	public boolean isCollapsed()
	{
		return (this.mCardState == CardState.COLLAPSED);
	}

	/**
	 * @brief Check if the card is completely visible.
	 *
	 * @return True if it is visible. False otherwise.
	 */
	private boolean isCompletelyVisible()
	{
		LinearLayoutManager lv = (LinearLayoutManager) mRecyclerView.getLayoutManager();
		int pos = getAdapterPosition();
		int ypos = lv.findViewByPosition(pos).getTop();
		int cardheight = this.getCardHeight();
		int screenheight = this.getScreenHeight();

		return ((cardheight+ypos) > screenheight) ? false : true;
	}

	/**
	 * @brief Expand or collapse the card.
	 *
	 * @details When the alarm card is not at the top of the screen, clicking
	 * 			the expand button will scroll the screen so that the alarm card
	 * 			is at the top.
	 *
	 * @param  v  The view that was clicked on.
	 */
	@Override
	public void onClick(View v)
	{
		int id = v.getId();

		switch (id)
		{
			case R.id.view_card_alarm:
				if (this.isExpanded())
				{
					this.collapse();
				}
				else if (this.isCollapsed())
				{
					this.expand();
				}
				break;
			case R.id.nacExpand:
				this.expand();
				break;
			case R.id.nacCollapse:
				this.collapse();
				break;
			default:
				break;
		}
	}

}
