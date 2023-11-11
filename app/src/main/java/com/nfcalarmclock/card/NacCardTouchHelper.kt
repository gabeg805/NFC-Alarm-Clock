package com.nfcalarmclock.card

import android.graphics.Canvas
import android.view.View
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.nfcalarmclock.alarm.db.NacAlarm

/**
 * Touch helper.
 */
class NacCardTouchHelper(

	/**
	 * Callback.
	 */
	callback: Callback

	// Constructor
) : ItemTouchHelper(callback)
{

	/**
	 * Listener for when an alarm card is swiped.
	 */
	interface OnSwipedListener
	{

		/**
		 * Called when an alarm card is swiped left.
		 */
		fun onCopySwipe(alarm: NacAlarm, index: Int)

		/**
		 * Called when an alarm card is swiped right.
		 */
		fun onDeleteSwipe(alarm: NacAlarm, index: Int)

	}

	/**
	 */
	constructor(listener: OnSwipedListener) : this(Callback(listener))

	/**
	 * Handle callback events when swiping card.
	 */
	class Callback(

		/**
		 * The listener to call when a swiped event occurs.
		 */
		private val onSwipedListener: OnSwipedListener

		// Constructor
	) : ItemTouchHelper.Callback()
	{

		/**
		 * Clear the view.
		 *
		 * @param  rv  The recycler view.
		 * @param  vh  The view holder.
		 */
		override fun clearView(rv: RecyclerView, vh: RecyclerView.ViewHolder)
		{
			val fg = getCardView(vh)
			val copy = getCopySwipeView(vh)
			val delete = getDeleteSwipeView(vh)
			getDefaultUIUtil().clearView(fg)
			copy!!.visibility = View.GONE
			delete!!.visibility = View.GONE
		}

		/**
		 * Get the card holder.
		 *
		 * @return The card holder.
		 */
		private fun getCardHolder(vh: RecyclerView.ViewHolder): NacCardHolder
		{
			return vh as NacCardHolder
		}

		/**
		 * Get the card view.
		 *
		 * @return The card view.
		 */
		private fun getCardView(vh: RecyclerView.ViewHolder): View?
		{
			// Get the card holder
			val holder = getCardHolder(vh)

			// Return the card view
			return holder.cardView
		}

		/**
		 * @return The copy view, which resides in the background of the view
		 * holder.
		 */
		private fun getCopySwipeView(vh: RecyclerView.ViewHolder): View?
		{
			// Get the card holder
			val holder = getCardHolder(vh)

			// Return the copy view
			return holder.copySwipeView
		}

		/**
		 * @return The background delete view of the view holder.
		 */
		private fun getDeleteSwipeView(vh: RecyclerView.ViewHolder): View?
		{
			// Get the card holder
			val holder = getCardHolder(vh)

			// Return the delete view
			return holder.deleteSwipeView
		}

		/**
		 * Set te movement flags for the card, to allow it to be swiped left and
		 * right.
		 *
		 * @param  rv  The recycler view.
		 * @param  vh  The view holder.
		 */
		override fun getMovementFlags(rv: RecyclerView, vh: RecyclerView.ViewHolder): Int
		{
			// Check if collapsed
			return if (isCollapsed(vh))
			{
				makeMovementFlags(0, LEFT or RIGHT)
			}
			// Expanded
			else
			{
				0
			}
		}

		/**
		 * Define the minimum velocity which will be considered as a swipe
		 * action by the user.
		 */
		override fun getSwipeEscapeVelocity(defaultValue: Float): Float
		{
			return 6 * defaultValue
		}

		/**
		 * The fraction that the user should move the view to be considered
		 * as swiped.
		 */
		override fun getSwipeThreshold(vh: RecyclerView.ViewHolder): Float
		{
			return 0.5f
		}

		/**
		 * Check if card is collapsed.
		 *
		 * TODO: Should this be isExpanded() instead? Every check is not this method
		 */
		private fun isCollapsed(vh: RecyclerView.ViewHolder): Boolean
		{
			// Get the card holder
			val holder = getCardHolder(vh)

			// Check if the card is collapsed or the alarm is not in use
			return holder.isCollapsed && !holder.isAlarmInUse
		}

		/**
		 * Allow the card to be swiped.
		 *
		 *
		 * Note: When the app is opened up fresh and a card is expanded, swiping
		 * will work because the ViewHolder is null.
		 */
		override fun isItemViewSwipeEnabled(): Boolean
		{
			return true
		}

		/**
		 * Do not allow the card to be dragged.
		 */
		override fun isLongPressDragEnabled(): Boolean
		{
			return false
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
		override fun onChildDraw(c: Canvas, rv: RecyclerView,
			vh: RecyclerView.ViewHolder, dx: Float, dy: Float, action: Int, active: Boolean)
		{
			// Do nothing if the card is expanded
			if (!isCollapsed(vh))
			{
				return
			}

			// Get the foreground view
			val fg = getCardView(vh)

			// A swipe occurred
			if (action == ACTION_STATE_SWIPE)
			{
				// Get the copy and delete views
				val copy = getCopySwipeView(vh)
				val delete = getDeleteSwipeView(vh)

				// Moved to the right
				if (dx > 0)
				{
					copy!!.visibility = View.VISIBLE
					delete!!.visibility = View.GONE
				}
				// Moved to the left
				else if (dx < 0)
				{
					copy!!.visibility = View.GONE
					delete!!.visibility = View.VISIBLE
				}
				// No movement
				else
				{
					copy!!.visibility = View.GONE
					delete!!.visibility = View.GONE
				}
			}

			// Draw
			getDefaultUIUtil().onDraw(c, rv, fg, dx, dy, action, active)
		}

		/**
		 * Do not allow movement of cards.
		 *
		 * @param  rv  The recycler view.
		 * @param  vh  The view holder.
		 * @param  target  The target view holder.
		 */
		override fun onMove(
			rv: RecyclerView,
			vh: RecyclerView.ViewHolder,
			target: RecyclerView.ViewHolder
		): Boolean
		{
			return false
		}

		/**
		 * Called when item is swiped.
		 *
		 * @param  vh  The view holder.
		 * @param  direction  The direction that the item was swiped.
		 */
		override fun onSwiped(vh: RecyclerView.ViewHolder, direction: Int)
		{
			// Do nothing if the card is expanded
			if (!isCollapsed(vh))
			{
				return
			}

			val adapter = vh.bindingAdapter as NacCardAdapter
			val index = vh.bindingAdapterPosition
			val alarm = adapter.getAlarmAt(index)

			// Swiped to the left
			if (direction == LEFT)
			{
				// Call the delete listener
				onSwipedListener.onDeleteSwipe(alarm, index)
			}
			// Swiped to the right
			else if (direction == RIGHT)
			{
				// Call the copy listener
				onSwipedListener.onCopySwipe(alarm, index)
			}
		}

	}
}