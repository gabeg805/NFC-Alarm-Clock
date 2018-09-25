package com.nfcalarmclock;

import android.graphics.Canvas;
import android.support.v7.widget.helper.ItemTouchUIUtil;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.view.View;

/**
 * @brief Class to handle callback events when swiping card.
 */
public class NacCardTouchHelperCallback
	extends ItemTouchHelper.Callback
{

	/**
	 * @brief The adapter that will implement the event methods.
	 */
    private NacCardTouchHelperAdapter mAdapter = null;

	/**
	 * @brief The current view holder.
	 */
	private ViewHolder mViewHolder = null;

	/**
	 * @param  adapter  The object that overrides the event methods.
	 */
    public NacCardTouchHelperCallback(NacCardTouchHelperAdapter adapter)
	{
        mAdapter = adapter;
    }

	/**
	 * @brief Return the foreground view of the view holder.
	 */
	private View getForegroundView()
	{
		return ((NacCard)this.mViewHolder).mCard;
	}

	/**
	 * @brief Return the background copy view of the view holder.
	 */
	private View getBackgroundCopyView()
	{
		return ((NacCard)this.mViewHolder).mBackgroundCopy;
	}

	/**
	 * @brief Return the background delete view of the view holder.
	 */
	private View getBackgroundDeleteView()
	{
		return ((NacCard)this.mViewHolder).mBackgroundDelete;
	}

	/**
	 * @brief Clear the view.
	 * 
	 * @param  rv  The recycler view.
	 * @param  vh  The view holder.
	 */
    @Override
    public void clearView(RecyclerView rv, ViewHolder vh)
	{
        final View fg = this.getForegroundView();

        getDefaultUIUtil().clearView(fg);
    }
	/**
	 * @brief Convert the movement of the card to an absolute direction.
	 *
	 * @param  flags  Movement flags.
	 * @param  dir  The direction information.
	 */
    @Override
    public int convertToAbsoluteDirection(int flags, int dir)
	{
        return super.convertToAbsoluteDirection(flags, dir);
    }

	/**
	 * @brief Set te movement flags for the card, to allow it to be swiped
	 *        left and right.
	 *
	 * @param  rv  The recycler view.
	 * @param  vh  The view holder.
	 */
    @Override
    public int getMovementFlags(RecyclerView rv, ViewHolder vh)
	{
		this.mViewHolder = vh;

        return makeMovementFlags(0,
			ItemTouchHelper.LEFT|ItemTouchHelper.RIGHT);
    }

	/**
	 * @brief Allow the card to be swiped.
	 */
    @Override
    public boolean isItemViewSwipeEnabled()
	{
        return (this.mViewHolder == null) ? true :
			((NacCard)this.mViewHolder).isCollapsed();
    }

	/**
	 * @brief Called when the child is drawn.
	 *
	 * @param  c  The canvas.
	 * @param  rv  The recycler view.
	 * @param  vh  The view holder.
	 * @param  dx  The amount the card has been swiped in the x-direction.
	 * @param  dy  The amount the card has been swiped in the y-direcdtion.
	 * @param  action  The action that was done on the card.
	 * @param  active  Whether the card is being used by the user or not.
	 */
    @Override
    public void onChildDraw(Canvas c, RecyclerView rv, ViewHolder vh,
		float dx, float dy, int action, boolean active)
	{
		this.mViewHolder = vh;
        final View fg = this.getForegroundView();
		final View copy = this.getBackgroundCopyView();
		final View delete = this.getBackgroundDeleteView();

		if (action == ItemTouchHelper.ACTION_STATE_SWIPE)
		{
			if (dx > 0)
			{
				copy.setVisibility(View.VISIBLE);
				delete.setVisibility(View.GONE);
			}
			else if (dx < 0)
			{
				copy.setVisibility(View.GONE);
				delete.setVisibility(View.VISIBLE);
			}
		}

        getDefaultUIUtil().onDraw(c, rv, fg, dx, dy, action, active);
    }

	/**
	 * @brief Called when onChildDraw is over.
	 *
	 * @param  c  The canvas.
	 * @param  rv  The recycler view.
	 * @param  vh  The view holder.
	 * @param  dx  The amount the card has been swiped in the x-direction.
	 * @param  dy  The amount the card has been swiped in the y-direcdtion.
	 * @param  action  The action that was done on the card.
	 * @param  active  Whether the card is being used by the user or not.
	 */
    @Override
    public void onChildDrawOver(Canvas c, RecyclerView rv, ViewHolder vh,
		float dx, float dy, int action, boolean active)
	{
        final View view = this.getForegroundView();

        getDefaultUIUtil().onDrawOver(c, rv, view, dx, dy, action, active);
    }

	/**
	 * @brief Disallow movement of cards.
	 *
	 * @param  rv  The recycler view.
	 * @param  vh  The view holder.
	 * @param  target  The target view holder.
	 */
    @Override
    public boolean onMove(RecyclerView rv, ViewHolder vh, ViewHolder target)
	{
        return false;
    }

	/**
	 * @brief Called when the item that was selected has changed.
	 * 
	 * @param  vh  The view holder.
	 * @param  action  The action that was taken.
	 */
    @Override
    public void onSelectedChanged(ViewHolder vh, int action)
	{
        if (vh != null)
		{
            final View fg= this.getForegroundView();

            getDefaultUIUtil().onSelected(fg);
        }
    }

	/**
	 * @brief Called when item is swiped.
	 *
	 * @param  vh  The view holder.
	 * @param  dir  The direction that the item was swiped.
	 */
    @Override
    public void onSwiped(ViewHolder vh, int dir)
	{
		if (dir == ItemTouchHelper.LEFT)
		{
        	mAdapter.onItemDelete(vh.getAdapterPosition());
		}
		else if (dir == ItemTouchHelper.RIGHT)
		{
			mAdapter.onItemCopy(vh.getAdapterPosition());
		}
    }

}
