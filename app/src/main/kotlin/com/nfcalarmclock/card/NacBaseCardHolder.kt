package com.nfcalarmclock.card

import android.view.View
import android.widget.RelativeLayout
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.nfcalarmclock.R
import com.nfcalarmclock.alarm.db.NacAlarm
import com.nfcalarmclock.shared.NacSharedPreferences

/**
 * Generic ViewHolder for a CardView.
 *
 * @param root Root view.
 */
abstract class NacBaseCardHolder<T: NacAlarm>(

	val root: View

	// Constructor
) : RecyclerView.ViewHolder(root)
{

	/**
	 * Shared preferences.
	 */
	protected val sharedPreferences: NacSharedPreferences = NacSharedPreferences(root.context)

	/**
	 * Card view.
	 */
	val cardView: CardView = root.findViewById(R.id.nac_card)

	/**
	 * Copy swipe view.
	 */
	val copySwipeView: RelativeLayout? = root.findViewById(R.id.nac_swipe_copy)

	/**
	 * Delete swipe view.
	 */
	val deleteSwipeView: RelativeLayout? = root.findViewById(R.id.nac_swipe_delete)

	///**
	// * Constructor.
	// */
	//init
	//{
	//	// Initialize the colors and listeners
	//	initColors()
	//	initListeners()
	//}

	/**
	 * Hide the swipe views.
	 */
	protected fun hideSwipeViews()
	{
		copySwipeView?.visibility = View.GONE
		deleteSwipeView?.visibility = View.GONE
	}

	/**
	 * Bind the alarm to the card view.
	 */
	open fun bind(item: T)
	{
		// Hide the swipe views
		hideSwipeViews()

		// Setup the views because it is dependent on the item being bound to the card
		// holder
		initViews()
	}

	/**
	 * Initialize the colors of the various views.
	 */
	protected abstract fun initColors()

	/**
	 * Initialize the listeners of the various views.
	 */
	protected abstract fun initListeners()

	/**
	 * Initialize the various views.
	 */
	protected abstract fun initViews()

}