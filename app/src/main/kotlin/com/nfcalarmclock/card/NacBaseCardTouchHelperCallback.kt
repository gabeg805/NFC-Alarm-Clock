package com.nfcalarmclock.card

import android.graphics.Canvas
import android.view.View
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.ItemTouchHelper.ACTION_STATE_SWIPE
import androidx.recyclerview.widget.ItemTouchHelper.LEFT
import androidx.recyclerview.widget.ItemTouchHelper.RIGHT
import androidx.recyclerview.widget.RecyclerView
import com.nfcalarmclock.alarm.db.NacAlarm

/**
 * Handle callback events when swiping card.
 *
 * @param onCardSwipedListener Listener for when the card is swiped.
 */
class NacBaseCardTouchHelperCallback<T: NacAlarm>(

	private val onCardSwipedListener: OnCardSwipedListener<T>

	// Constructor
) : ItemTouchHelper.Callback()
{

	/**
	 * Listener for when a card is swiped.
	 */
	interface OnCardSwipedListener<T: NacAlarm>
	{

		/**
		 * Called when a card is swiped left.
		 */
		fun onCopySwipe(item: T, index: Int)

		/**
		 * Called when a card is swiped right.
		 */
		fun onDeleteSwipe(item: T, index: Int)

	}

	/**
	 * Clear the view.
	 *
	 * @param  rv  The recycler view.
	 * @param  vh  The view holder.
	 */
	override fun clearView(rv: RecyclerView, vh: RecyclerView.ViewHolder)
	{
		// Get the views
		val fg = getCardView(vh)
		val copy = getCopySwipeView(vh)
		val delete = getDeleteSwipeView(vh)

		// Clear the view
		getDefaultUIUtil().clearView(fg)

		// Hide the copy and delete views
		copy.visibility = View.GONE
		delete.visibility = View.GONE
	}

	/**
	 * Get the card holder.
	 *
	 * @return The card holder.
	 */
	private fun getCardHolder(vh: RecyclerView.ViewHolder): NacBaseCardHolder<T>
	{
		return vh as NacBaseCardHolder<T>
	}

	/**
	 * Get the card view.
	 *
	 * @return The card view.
	 */
	private fun getCardView(vh: RecyclerView.ViewHolder): View
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
	private fun getCopySwipeView(vh: RecyclerView.ViewHolder): View
	{
		// Get the card holder
		val holder = getCardHolder(vh)

		// Return the copy view
		return holder.copySwipeView!!
	}

	/**
	 * @return The background delete view of the view holder.
	 */
	private fun getDeleteSwipeView(vh: RecyclerView.ViewHolder): View
	{
		// Get the card holder
		val holder = getCardHolder(vh)

		// Return the delete view
		return holder.deleteSwipeView!!
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
		return makeMovementFlags(0, LEFT or RIGHT)
		// TODO: Only alarm needs this
		//// Check if collapsed
		//return if (isCollapsed(vh))
		//{
		//	makeMovementFlags(0, LEFT or RIGHT)
		//}
		//// Expanded
		//else
		//{
		//	0
		//}
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

	// TODO: Only alarm needs this
	///**
	// * Check if card is collapsed.
	// */
	//private fun isCollapsed(vh: RecyclerView.ViewHolder): Boolean
	//{
	//	// Get the card holder
	//	val holder = getCardHolder(vh)

	//	// Check if the card is collapsed or the alarm is not in use
	//	return holder.isCollapsed && !holder.isAlarmInUse
	//}

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
	override fun onChildDraw(
		c: Canvas,
		rv: RecyclerView,
		vh: RecyclerView.ViewHolder,
		dx: Float,
		dy: Float,
		action: Int,
		active: Boolean)
	{
		// TODO: Only alarm needs this
		//// Do nothing if the card is expanded
		//if (!isCollapsed(vh))
		//{
		//	return
		//}

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
				copy.visibility = View.VISIBLE
				delete.visibility = View.GONE
			}
			// Moved to the left
			else if (dx < 0)
			{
				copy.visibility = View.GONE
				delete.visibility = View.VISIBLE
			}
			// No movement
			else
			{
				copy.visibility = View.GONE
				delete.visibility = View.GONE
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
		// TODO: Only alarm needs this
		//// Do nothing if the card is expanded
		//if (!isCollapsed(vh))
		//{
		//	return
		//}

		val adapter = vh.bindingAdapter as NacBaseCardAdapter<*, *>
		val index = vh.bindingAdapterPosition
		val item = adapter.getItemAt(index) as T

		// Swiped to the left. Call delete listener
		if (direction == LEFT)
		{
			onCardSwipedListener.onDeleteSwipe(item, index)
		}
		// Swiped to the right. Call copy listener
		else if (direction == RIGHT)
		{
			onCardSwipedListener.onCopySwipe(item, index)
		}
	}

}