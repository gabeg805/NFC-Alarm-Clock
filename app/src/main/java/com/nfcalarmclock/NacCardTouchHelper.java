package com.nfcalarmclock;

import android.graphics.Canvas;
import android.view.View;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.ItemTouchHelper;
//import androidx.recyclerview.widget.ItemTouchUIUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.RecyclerView.ViewHolder;

/**
 * Touch helper.
 */
@SuppressWarnings({"BooleanMethodIsAlwaysInverted", "UnnecessaryInterfaceModifier"})
public class NacCardTouchHelper
	extends ItemTouchHelper
{

	/**
	 * Listener for when an alarm card is swiped.
	 */
	public interface OnSwipedListener
	{

		/**
		 * Called when an alarm card is swiped left.
		 */
		public void onCopySwipe(NacAlarm alarm, int index);

		/**
		 * Called when an alarm card is swiped right.
		 */
		public void onDeleteSwipe(NacAlarm alarm, int index);

	}

	/**
	 * Callback.
	 */
	private Callback mCallback;

	/**
	 */
	public NacCardTouchHelper(OnSwipedListener listener)
	{
		this(new Callback(listener));
	}

	/**
	 */
	public NacCardTouchHelper(Callback callback)
	{
		super(callback);

		this.mCallback = callback;
	}

	/**
	 * @return The callback.
	 */
	public Callback getCallback()
	{
		return this.mCallback;
	}

	/**
	 * Handle callback events when swiping card.
	 */
	public static class Callback
		extends ItemTouchHelper.Callback
	{

		/**
		 * The current view holder.
		 */
		private ViewHolder mViewHolder;

		/**
		 * The listener to call when a swiped event occurs.
		 */
		private OnSwipedListener mOnSwipedListener;

		/**
		 * @param  adapter	The object that overrides the event methods.
		 */
		public Callback(OnSwipedListener listener)
		{
			this.mOnSwipedListener = listener;
			this.mViewHolder = null;
		}

		/**
		 * Clear the view.
		 * 
		 * @param  rv  The recycler view.
		 * @param  vh  The view holder.
		 */
		@Override
		public void clearView(@NonNull RecyclerView rv, @NonNull ViewHolder vh)
		{
			final View fg = this.getCardView();
			final View copy = this.getCopySwipeView();
			final View delete = this.getDeleteSwipeView();

			NacUtility.printf("CLEARING VIEW!");
			copy.setVisibility(View.GONE);
			delete.setVisibility(View.GONE);
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
			return (NacCardHolder) this.getViewHolder();
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
		 * @return The copy view, which resides in the background of the view
		 *         holder.
		 */
		private View getCopySwipeView()
		{
			NacCardHolder holder = this.getCardHolder();
			return (holder != null) ? holder.getCopySwipeView() : null;
		}

		/**
		 * @return The background delete view of the view holder.
		 */
		private View getDeleteSwipeView()
		{
			NacCardHolder holder = this.getCardHolder();
			return (holder != null) ? holder.getDeleteSwipeView() : null;
		}

		/**
		 * Set te movement flags for the card, to allow it to be swiped left and
		 * right.
		 *
		 * @param  rv  The recycler view.
		 * @param  vh  The view holder.
		 */
		@Override
		public int getMovementFlags(@NonNull RecyclerView rv, @NonNull ViewHolder vh)
		{
			this.mViewHolder = vh;

			if (!this.isCollapsed())
			{
				return 0;
			}

			return makeMovementFlags(0,
				ItemTouchHelper.LEFT|ItemTouchHelper.RIGHT);
		}

		/**
		 * The listener to call when a swiped event occurs.
		 */
		private OnSwipedListener getOnSwipedListener()
		{
			return this.mOnSwipedListener;
		}

		/**
		 */
		@Override
		public float getSwipeEscapeVelocity(float defaultValue)
		{
			return 6*defaultValue;
		}

		/**
		 */
		@Override
		public float getSwipeThreshold(@NonNull ViewHolder vh)
		{
			return 0.5f;
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

			if (holder != null)
			{
				return holder.isCollapsed() && !holder.isAlarmInUse();
			}
			else
			{
				return true;
			}
		}

		/**
		 * Allow the card to be swiped.
		 *
		 * Note: When the app is opened up fresh and a card is expanded, swiping
		 * will work because the ViewHolder is null.
		 */
		@Override
		public boolean isItemViewSwipeEnabled()
		{
			return true;
		}

		/**
		 * Do not allow the card to be dragged.
		 */
		@Override
		public boolean isLongPressDragEnabled()
		{
			return false;
		}

		/**
		 * Called when the child is drawn.
		 *
		 * @param  c  The canvas.
		 * @param  rv  The recycler view.
		 * @param  vh  The view holder.
		 * @param  dx  The amount the card has been swiped in the x-direction.
		 * @param  dy  The amount the card has been swiped in the y-direction.
		 * @param  action  The action that was done on the card.
		 * @param  active  Whether the card is being used by the user or not.
		 */
		@Override
		public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView rv,
			@NonNull ViewHolder vh, float dx, float dy, int action, boolean active)
		{
			this.mViewHolder = vh;

			if (!this.isCollapsed())
			{
				return;
			}

			final View fg = this.getCardView();

			if (action == ItemTouchHelper.ACTION_STATE_SWIPE)
			{
				final View copy = this.getCopySwipeView();
				final View delete = this.getDeleteSwipeView();

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
		 * Do not allow movement of cards.
		 *
		 * @param  rv  The recycler view.
		 * @param  vh  The view holder.
		 * @param  target  The target view holder.
		 */
		@Override
		public boolean onMove(@NonNull RecyclerView rv, @NonNull ViewHolder vh,
			@NonNull ViewHolder target)
		{
			return false;
		}

		/**
		 * Called when item is swiped.
		 *
		 * @param  vh  The view holder.
		 * @param  direction  The direction that the item was swiped.
		 */
		@Override
		public void onSwiped(@NonNull ViewHolder vh, int direction)
		{
			// This may be unnecessary
			this.mViewHolder = vh;

			if (!this.isCollapsed())
			{
				return;
			}

			NacUtility.printf("View holder was swiped!");
			NacAlarmCardAdapter adapter = (NacAlarmCardAdapter) vh.getBindingAdapter();
			int index = vh.getBindingAdapterPosition();
			NacAlarm alarm = adapter.getAlarmAt(index);

			if (direction == ItemTouchHelper.LEFT)
			{
				this.getOnSwipedListener().onDeleteSwipe(alarm, index);
			}
			else if (direction == ItemTouchHelper.RIGHT)
			{
				this.getCardView().setX(0);
				this.getOnSwipedListener().onCopySwipe(alarm, index);
			}
		}

	}

}
