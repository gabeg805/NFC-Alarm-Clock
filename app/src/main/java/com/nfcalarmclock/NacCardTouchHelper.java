package com.nfcalarmclock;

import android.graphics.Canvas;
import android.view.View;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.ItemTouchUIUtil;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.RecyclerView.ViewHolder;

/**
 * Touch helper.
 */
public class NacCardTouchHelper
	extends ItemTouchHelper
{

	/**
	 * Adapter for when an alarm card swipe button is selected.
	 */
	public interface Adapter
	{

		/**
		 * Called when an alarm card should be copied.
		 */
		public void onItemCopy(int pos);

		/**
		 * Called when an alarm card should be deleted.
		 */
		public void onItemDelete(int pos);

		///**
		// * Called when an alarm card should be moved.
		// */
		//public void onItemMove(int from, int to);

	}

	/**
	 * RecyclerView.
	 */
	protected RecyclerView mRecyclerView;

	/**
	 */
	public NacCardTouchHelper(Callback callback)
	{
		super(callback);

		this.mRecyclerView = null;
	}

	/**
	 * Set the RecyclerView to help on.
	 */
	public void setRecyclerView(RecyclerView rv)
	{
		this.mRecyclerView = rv;
	}

	/**
	 * Reset the touch helper.
	 */
	public void reset()
	{
		this.attachToRecyclerView(null);
		this.attachToRecyclerView(this.mRecyclerView);
	}

	/**
	 * Handle callback events when swiping card.
	 */
	public static class Callback
		extends ItemTouchHelper.Callback
	{

		/**
		 * The adapter that will implement the event methods.
		 */
		private Adapter mAdapter;

		/**
		 * The current view holder.
		 */
		private ViewHolder mViewHolder;

		/**
		 * Current action that the user is doing (drag/swipe).
		 */
		private int mAction;

		/**
		 * @param  adapter	The object that overrides the event methods.
		 */
		public Callback(Adapter adapter)
		{
			this.mAdapter = adapter;
			this.mViewHolder = null;
			this.mAction = -1;
		}

		/**
		 * Clear the view.
		 * 
		 * @param  rv  The recycler view.
		 * @param  vh  The view holder.
		 */
		@Override
		public void clearView(RecyclerView rv, ViewHolder vh)
		{
			final View fg = this.getCardView(this.mAction);

			//if (this.mAction == ItemTouchHelper.ACTION_STATE_DRAG)
			//{
			//	fg.setAlpha(1.0f);
			//}

			getDefaultUIUtil().clearView(fg);
		}

		/**
		 * Convert the movement of the card to an absolute direction.
		 *
		 * @param  flags  Movement flags.
		 * @param  dir	The direction information.
		 */
		@Override
		public int convertToAbsoluteDirection(int flags, int dir)
		{
			return super.convertToAbsoluteDirection(flags, dir);
		}

		/**
		 * @return The card holder.
		 */
		private NacCardHolder getCardHolder()
		{
			return (NacCardHolder) this.mViewHolder;
		}

		/**
		 * @return The root view of the card.
		 */
		private View getCardRoot()
		{
			NacCardHolder holder = this.getCardHolder();

			return (holder != null) ? holder.getRoot() : null;
		}

		/**
		 * @return The card view.
		 */
		private View getCardView()
		{
			NacCardHolder holder = this.getCardHolder();

			return (holder != null) ? holder.getCardView() : null;
		}

		/**
		 * @return The card view depending on the action being done.
		 */
		private View getCardView(int action)
		{
			return (action == ItemTouchHelper.ACTION_STATE_SWIPE)
				? this.getCardView() : this.getCardRoot();
		}

		/**
		 * @return The copy view, which resides in the background of the view
		 *         holder.
		 */
		private View getCopyView()
		{
			NacCardHolder holder = this.getCardHolder();

			return (holder != null) ? holder.getCopyView() : null;
		}

		/**
		 * @return The background delete view of the view holder.
		 */
		private View getDeleteView()
		{
			NacCardHolder holder = this.getCardHolder();

			return (holder != null) ? holder.getDeleteView() : null;
		}

		/**
		 * Set te movement flags for the card, to allow it to be swiped left and
		 * right.
		 *
		 * @param  rv  The recycler view.
		 * @param  vh  The view holder.
		 */
		@Override
		public int getMovementFlags(RecyclerView rv, ViewHolder vh)
		{
			this.mViewHolder = vh;

			if (!this.isCollapsed())
			{
				return 0;
			}

			//return makeMovementFlags(ItemTouchHelper.UP|ItemTouchHelper.DOWN,
			return makeMovementFlags(0,
				ItemTouchHelper.LEFT|ItemTouchHelper.RIGHT);
		}

		/**
		 * @return The view holder.
		 */
		private ViewHolder getViewHolder()
		{
			return this.mViewHolder;
		}

		/**
		 * Check if card is collapsed.
		 */
		public boolean isCollapsed()
		{
			NacCardHolder holder = this.getCardHolder();

			return (holder != null) ? holder.isCollapsed() : true;
		}

		/**
		 * Allow the card to be swiped.
		 *
		 * @bug When the app is opened up fresh and a card is expanded, swiping
		 *		will work because the ViewHolder is null.
		 */
		@Override
		public boolean isItemViewSwipeEnabled()
		{
			return true;
		}

		/**
		 * Allow the card to be dragged.
		 */
		@Override
		public boolean isLongPressDragEnabled()
		{
			return false;
			//return true;
		}

		/**
		 * Called when the child is drawn.
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
			this.mAction = action;

			if (!this.isCollapsed())
			{
				return;
			}

			final View fg = this.getCardView(action);
			final View copy = this.getCopyView();
			final View delete = this.getDeleteView();

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
				else
				{
					copy.setVisibility(View.GONE);
					delete.setVisibility(View.GONE);
				}
			}

			getDefaultUIUtil().onDraw(c, rv, fg, dx, dy, action, active);
		}

		/**
		 * Called when onChildDraw is over.
		 *
		 * @param  c  The canvas.
		 * @param  rv  The recycler view.
		 * @param  vh  The view holder.
		 * @param  dx  The amount the card has been swiped in the x-direction.
		 * @param  dy  The amount the card has been swiped in the y-direcdtion.
		 * @param  action  The action that was done on the card.
		 * @param  active  Whether the card is being used by the user or not.
		 */
		//@Override
		//public void onChildDrawOver(Canvas c, RecyclerView rv, ViewHolder vh,
		//	float dx, float dy, int action, boolean active)
		//{
		//	final View view = this.getCardView(action);

		//	getDefaultUIUtil().onDrawOver(c, rv, view, dx, dy, action, active);
		//}

		/**
		 * Disallow movement of cards.
		 *
		 * @param  rv  The recycler view.
		 * @param  vh  The view holder.
		 * @param  target  The target view holder.
		 */
		@Override
		public boolean onMove(RecyclerView rv, ViewHolder vh, ViewHolder target)
		{
			return false;

			//this.mAdapter.onItemMove(vh.getAdapterPosition(),
			//	target.getAdapterPosition());

			//return true;
		}

		/**
		 * Called when the item that was selected has changed.
		 * 
		 * @param  vh  The view holder.
		 * @param  action  The action that was taken.
		 */
		@Override
		public void onSelectedChanged(ViewHolder vh, int action)
		{
			if (vh != null)
			{
				if (!this.isCollapsed())
				{
					return;
				}

				final View fg = this.getCardView(action);

				//if (action == ItemTouchHelper.ACTION_STATE_DRAG)
				//{
				//	fg.setAlpha(0.75f);
				//}

				getDefaultUIUtil().onSelected(fg);
			}
		}

		/**
		 * Called when item is swiped.
		 *
		 * @param  vh  The view holder.
		 * @param  dir	The direction that the item was swiped.
		 */
		@Override
		public void onSwiped(ViewHolder vh, int dir)
		{
			// This may be unnecessary
			this.mViewHolder = vh;

			if (!this.isCollapsed())
			{
				return;
			}
			else if (dir == ItemTouchHelper.LEFT)
			{
				mAdapter.onItemDelete(vh.getAdapterPosition());
			}
			else if (dir == ItemTouchHelper.RIGHT)
			{
				mAdapter.onItemCopy(vh.getAdapterPosition());
			}
		}

	}

}
