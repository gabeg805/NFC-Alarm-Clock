package com.nfcalarmclock;

import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.TransitionDrawable;
import android.support.v7.widget.CardView;
import android.view.View;
import android.widget.RelativeLayout;

/**
 * @brief Holder of all important views.
 */
public class NacCard
    implements View.OnClickListener
{

	/**
	 * Collapse listener for another class to implement.
	 */
	public interface OnCollapseListener
	{
		public void onCollapse(NacCard card);
	}

	/**
	 * Expand listener for another class to implement.
	 */
	public interface OnExpandListener
	{
		public void onExpand(NacCard card);
	}

	/**
	 * Expand/collapse states.
	 */
	public static class State
	{
		public static final byte EXPANDED = 1;
		public static final byte COLLAPSED = 2;
	}

	/**
	 * Collapse listener for the callback.
	 */
	public OnCollapseListener mCollapseListener;

	/**
	 * Expand listener for the callback.
	 */
	public OnExpandListener mExpandListener;

	/**
	 * Card view.
	 */
    public CardView mCardView;

	/**
	 * Summary and extra regions in the alarm card.
	 */
	private NacCardRegion mRegion;

	/**
	 * View that is displayed when copying the alarm (swiping right).
	 */
	public RelativeLayout mBackgroundCopy;

	/**
	 * View that is displayed when deleting the alarm (swiping left).
	 */
	public RelativeLayout mBackgroundDelete;

	/**
	 * Background color transition.
	 */
	private TransitionDrawable mTransition;

	/**
	 * The expand/collapse state of the card.
	 */
	private byte mState;

    /**
     */
    public NacCard(View root)
    {
        this.mCardView = (CardView) root.findViewById(R.id.view_card_alarm);
		this.mBackgroundCopy = (RelativeLayout) root.findViewById(
			R.id.view_background_copy);
		this.mBackgroundDelete = (RelativeLayout) root.findViewById(
			R.id.view_background_delete);
		this.mRegion = new NacCardRegion(root);
		this.mTransition = null;
		this.mExpandListener = null;
		this.mCollapseListener = null;
		this.mState = State.COLLAPSED;
	}

    /**
     * Collapse the alarm card.
     */
    public void collapse()
    {
		this.mState = State.COLLAPSED;

		this.mRegion.collapse();
		this.setBackgroundColor(this.mState);

		if (this.mCollapseListener != null)
		{
			this.mCollapseListener.onCollapse(this);
		}
    }

    /**
     * Expand the alarm card.
     */
    public void expand()
    {
		this.mState = State.EXPANDED;

		this.unfocus();
		this.mRegion.expand();
		this.setBackgroundColor(this.mState);

		if (this.mExpandListener != null)
		{
			this.mExpandListener.onExpand(this);
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

		Context context = this.mCardView.getContext();
		int duration = 2200;
		int bg = NacUtility.getThemeAttrColor(context, R.attr.colorCard);
		int highlight = NacUtility.getThemeAttrColor(context,
			R.attr.colorCardExpanded);
		ColorDrawable[] color = {new ColorDrawable(highlight),
			new ColorDrawable(bg)};
		this.mTransition = new TransitionDrawable(color);

		this.mCardView.setBackground(this.mTransition);
		this.mTransition.startTransition(duration);
	}

	/**
	 * @return The region height.
	 */
	public int getRegionHeight()
	{
		return this.mRegion.getHeight();
	}

    /**
     * Initialize the alarm card.
	 *
	 * @param  wasAdded  Indicator for whether or not the card should be
	 * 					 focused.
     */
    public void init(boolean wasAdded)
    {
		this.mBackgroundCopy.setVisibility(View.GONE);
		this.mBackgroundDelete.setVisibility(View.GONE);

		this.mRegion.init();

		this.mCardView.setOnClickListener(this);
		this.mRegion.setExpandListener(this);
		this.mRegion.setCollapseListener(this);

		if (this.isExpanded())
		{
			this.collapse();
		}

		this.focus(wasAdded);
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
	public boolean isCollapsed(byte state)
	{
		return (state == State.COLLAPSED);
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
	public boolean isExpanded(byte state)
	{
		return (state == State.EXPANDED);
	}

	/**
	 * Expand or collapse the card.
	 *
	 * @param  view  The view that was clicked on.
	 */
	@Override
	public void onClick(View view)
	{
		int id = view.getId();

		if (id == R.id.view_card_alarm)
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
		else if (id == R.id.nacExpand)
		{
			this.expand();
		}
		else if (id == R.id.nacCollapse)
		{
			this.collapse();
		}
	}

	/**
	 * Set the background color.
	 */
	public void setBackgroundColor(byte state)
	{
		if (this.isExpanded(state))
		{
			NacUtility.setBackground(this.mCardView, R.attr.colorCardExpanded);
		}
		else if (this.isCollapsed(state))
		{
			NacUtility.setBackground(this.mCardView, R.attr.colorCard);
		}
	}

	/**
	 * Set the OnCollapse listener.
	 */
	public void setOnCollapseListener(OnCollapseListener listener)
	{
		this.mCollapseListener = listener;
	}

	/**
	 * Set the OnExpand listener.
	 */
	public void setOnExpandListener(OnExpandListener listener)
	{
		this.mExpandListener = listener;
	}

	/**
	 * Unfocus the alarm card.
	 */
	public void unfocus()
	{
		if (this.mTransition != null)
		{
			this.mTransition.resetTransition();
			this.setBackgroundColor(State.COLLAPSED);

			this.mTransition = null;
		}
	}

}
