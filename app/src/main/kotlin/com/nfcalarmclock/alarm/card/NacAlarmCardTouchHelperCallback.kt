package com.nfcalarmclock.alarm.card

import android.graphics.Canvas
import androidx.recyclerview.widget.ItemTouchHelper.LEFT
import androidx.recyclerview.widget.ItemTouchHelper.RIGHT
import androidx.recyclerview.widget.RecyclerView
import com.nfcalarmclock.alarm.db.NacAlarm
import com.nfcalarmclock.card.NacBaseCardTouchHelperCallback

class NacAlarmCardTouchHelperCallback(
	onCardSwipedListener: OnCardSwipedListener<NacAlarm>
) : NacBaseCardTouchHelperCallback<NacAlarm>(onCardSwipedListener)
{

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
	 * Check if card is collapsed.
	 */
	private fun isCollapsed(vh: RecyclerView.ViewHolder): Boolean
	{
		// Get the card holder
		val holder = getCardHolder(vh) as NacAlarmCardHolder

		// Check if the card is collapsed or the alarm is not in use
		return holder.isCollapsed && !holder.isAlarmInUse
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
		// Do nothing if the card is expanded
		if (!isCollapsed(vh))
		{
			return
		}

		// Super
		super.onChildDraw(c, rv, vh, dx, dy, action, active)
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

		// Super
		super.onSwiped(vh, direction)
	}

}