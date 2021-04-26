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

	///**
	// * Adapter for when an alarm card swipe button is selected.
	// */
	//public interface Adapter
	//{

	//	/**
	//	 * Called when an alarm card should be copied.
	//	 */
	//	public void onItemCopy(int pos);

	//	/**
	//	 * Called when an alarm card should be deleted.
	//	 */
	//	public void onItemDelete(int pos);

	//}

	/**
	 * Listener for when an alarm card is swiped.
	 */
	public interface OnSwipedListener
	{

		/**
		 * Called when an alarm card is swiped left.
		 */
		public void onSwipedLeft(NacAlarm alarm, int pos);

		/**
		 * Called when an alarm card is swiped right.
		 */
		public void onSwipedRight(NacAlarm alarm, int pos);

	}

	/**
	 * Track previous swipe activity.
	 */
	public static class PreviousSwipe
	{

		/**
		 * Type of swipe operation.
		 */
		public enum Type
		{
			NONE,
			LEFT,
			RIGHT,
			OTHER
		}

		/**
		 * Position of card.
		 */
		private int mPosition;

		/**
		 * Direction of swipe.
		 */
		private Type mDirection;

		/**
		 * Extra data.
		 */
		private Object mData;

		/**
		 */
		public PreviousSwipe()
		{
			this(-1, Type.NONE);
		}

		/**
		 */
		public PreviousSwipe(int position, Type direction)
		{
			this.mPosition = position;
			this.mDirection = direction;
			this.mData = null;
		}

		/**
		 * @return The extra data.
		 */
		public Object getData()
		{
			return this.mData;
		}

		/**
		 * @return The direction that was swiped.
		 */
		public Type getDirection()
		{
			return this.mDirection;
		}

		/**
		 * @return The position of the card that was swiped.
		 */
		public int getPosition()
		{
			return this.mPosition;
		}

		/**
		 * Set the position and direction of the previous swipe..
		 */
		public void set(int position, Type direction)
		{
			Object data = this.getData();
			this.set(position, direction, data);
		}

		/**
		 * Set the position and direction of the previous swipe..
		 */
		public void set(int position, Type direction, Object data)
		{
			this.setPosition(position);
			this.setDirection(direction);
			this.setData(data);
		}

		/**
		 * Set the extra data.
		 */
		public void setData(Object data)
		{
			this.mData = data;
		}

		/**
		 * Set direction.
		 */
		public void setDirection(Type direction)
		{
			this.mDirection = direction;
		}

		/**
		 * Set position.
		 */
		public void setPosition(int position)
		{
			this.mPosition = position;
		}

		/**
		 * @return True if was not swiped (NONE), and False otherwise.
		 */
		public boolean wasNotSwiped()
		{
			return this.getDirection() == Type.NONE;
		}

		/**
		 * @return True if was swiped to the LEFT, and False otherwise.
		 */
		public boolean wasSwipedLeft()
		{
			return this.getDirection() == Type.LEFT;
		}

		/**
		 * @return True if was swiped OTHER, and False otherwise.
		 */
		public boolean wasSwipedOther()
		{
			return this.getDirection() == Type.OTHER;
		}

		/**
		 * @return True if was swiped to the RIGHT, and False otherwise.
		 */
		public boolean wasSwipedRight()
		{
			return this.getDirection() == Type.RIGHT;
		}

	}

	///**
	// * RecyclerView.
	// */
	//protected RecyclerView mRecyclerView;

	/**
	 */
	//public NacCardTouchHelper(Callback callback)
	//{
	//	super(callback);

	//	this.mRecyclerView = null;
	//}

	/**
	 * Callback.
	 */
	private Callback mCallback;

	/**
	 */
	public NacCardTouchHelper(OnSwipedListener listener)
	{
		super(new Callback(listener));
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
	 * @return The previous swipe.
	 */
	public PreviousSwipe getPreviousSwipe()
	{
		return this.getCallback().getPreviousSwipe();
	}

	///**
	// * Set the RecyclerView to help on.
	// */
	//public void setRecyclerView(RecyclerView rv)
	//{
	//	this.mRecyclerView = rv;
	//}

	/**
	 * Reset the touch helper.
	 */
	//public void reset()
	//{
	//	this.attachToRecyclerView(null);
	//	this.attachToRecyclerView(this.mRecyclerView);
	//}

	/**
	 * Handle callback events when swiping card.
	 */
	public static class Callback
		extends ItemTouchHelper.Callback
	{

		/**
		 * The adapter that will implement the event methods.
		 */
		private final OnSwipedListener mOnSwipedListener;
		//private final Adapter mAdapter;

		/**
		 * Track previous swipe activity.
		 */
		private PreviousSwipe mPreviousSwipe;

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
		//public Callback(Adapter adapter)
		public Callback(OnSwipedListener listener)
		{
			//this.mAdapter = adapter;
			this.mOnSwipedListener = listener;
			this.mPreviousSwipe = new PreviousSwipe();
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
		public void clearView(@NonNull RecyclerView rv, @NonNull ViewHolder vh)
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
			return (NacCardHolder) this.getViewHolder();
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
		 * @return The previous swipe activity.
		 */
		public PreviousSwipe getPreviousSwipe()
		{
			return this.mPreviousSwipe;
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
			this.mAction = action;

			if (!this.isCollapsed())
			{
				return;
			}

			final View fg = this.getCardView(action);
			final View copy = this.getCopySwipeView();
			final View delete = this.getDeleteSwipeView();

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


			//ListAdapter<NacAlarm, NacCardHolder> adapter = (ListAdapter)
			//	vh.getBindingAdapter();
			NacAlarmCardAdapter adapter = (NacAlarmCardAdapter) vh.getBindingAdapter();
			int position = vh.getBindingAdapterPosition();
			NacAlarm alarm = adapter.getAlarmAt(position);
			//int position = vh.getAdapterPosition();

			if (direction == ItemTouchHelper.LEFT)
			{
				//mAdapter.onItemDelete(vh.getAdapterPosition());
				mPreviousSwipe.set(position, PreviousSwipe.Type.LEFT, alarm);
				mOnSwipedListener.onSwipedLeft(alarm, position);
			}
			else if (direction == ItemTouchHelper.RIGHT)
			{
				//mAdapter.onItemCopy(vh.getAdapterPosition());
				mPreviousSwipe.set(position, PreviousSwipe.Type.RIGHT, alarm);
				mOnSwipedListener.onSwipedRight(alarm, position);
			}
		}

	}

}
