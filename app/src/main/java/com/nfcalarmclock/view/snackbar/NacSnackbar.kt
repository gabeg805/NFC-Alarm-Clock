package com.nfcalarmclock.view.snackbar

import android.content.Context
import android.view.View
import com.google.android.material.snackbar.BaseTransientBottomBar.BaseCallback
import com.google.android.material.snackbar.Snackbar
import com.nfcalarmclock.shared.NacSharedPreferences
import java.util.LinkedList
import java.util.Queue

/**
 * Snackbar handler to show, swipe away, etc.
 *
 * TODO: Can I get rid of this?
 */
class NacSnackbar(

	/**
	 * Root view.
	 */
	val root: View

) : View.OnClickListener
{

	/**
	 * Snackbar data holder.
	 */
	private class SnackHolder(

		/**
		 * Message.
		 */
		val message: String,

		/**
		 * Action button text.
		 */
		val action: String,

		/**
		 * Click listener.
		 */
		val onClickListener: View.OnClickListener?)

	/**
	 * Response to snackbar onDismissed and onShown callbacks.
	 */
	private inner class NacSnackbarCallback : BaseCallback<Snackbar?>()
	{

		/**
		 * Called when the snackbar is dismissed.
		 */
		override fun onDismissed(transientBottomBar: Snackbar?, event: Int)
		{
			val queue: Queue<SnackHolder> = queue
			snackbar = null

			// Check if there is a snackbar to show
			if (queue.size > 0)
			{
				val holder = queue.remove()
				val message = holder.message
				val action = holder.action
				val onClickListener = holder.onClickListener

				// Show the snackbar
				show(message, action, onClickListener, false)
			}
		}

		/**
		 * Called when the snackbar is shown.
		 */
		override fun onShown(transientBottomBar: Snackbar?)
		{
		}

	}

	/**
	 * Context.
	 */
	val context: Context
		get() = root.context

	/**
	 * Snackbar.
	 */
	var snackbar: Snackbar? = null
		private set

	/**
	 * Message queue.
	 */
	private val queue: Queue<SnackHolder> = LinkedList()

	/**
	 * Check if the snackbar can be dismissed early.
	 */
	var canDismiss = false

	/**
	 * Check if the snackbar is shown.
	 */
	val isShown: Boolean
		get() = snackbar?.isShown == true

	/**
	 * Dismiss the snackbar.
	 */
	fun dismiss()
	{
		snackbar?.dismiss()
	}

	/**
	 * Default listener when the action button is clicked.
	 */
	override fun onClick(view: View)
	{
	}

	/**
	 * Queue the snackbar data.
	 */
	private fun queue(message: String, action: String,
		listener: View.OnClickListener?)
	{
		val holder = SnackHolder(message, action, listener)

		queue.add(holder)
	}

	/**
	 * Set the snackbar action listener.
	 */
	fun setAction(action: String, listener: View.OnClickListener?)
	{
		if (action.isEmpty())
		{
			return
		}

		// Get the snackbar and listener
		val snackbar = snackbar
		val actionListener = listener ?: this

		// Set the action
		snackbar!!.setAction(action, actionListener)
	}

	/**
	 * Set the action text color to the theme color.
	 */
	fun setActionTextThemeColor()
	{
		val shared = NacSharedPreferences(context)
		val themeColor = shared.themeColor

		// Set the text color
		snackbar!!.setActionTextColor(themeColor)
	}

	/**
	 * Set the snackbar.
	 */
	protected fun setSnackbar(message: String?)
	{
		// Set the snackbar
		snackbar = Snackbar.make(root, message!!, Snackbar.LENGTH_LONG)

		// Add a callback
		snackbar!!.addCallback(NacSnackbarCallback())
	}

	/**
	 * Show the snackbar now.
	 */
	fun show()
	{
		snackbar!!.show()
	}

	/**
	 * @see .show
	 */
	@Suppress("unused")
	fun show(message: String, action: String,
		listener: View.OnClickListener?)
	{
		this.show(message, action, listener, false)
	}

	/**
	 * Setup the snackbar and show it.
	 *
	 * @see .show
	 */
	fun show(message: String, action: String,
		listener: View.OnClickListener?, dismiss: Boolean)
	{
		// Set whether the snackbar can be dismissed or not
		canDismiss = dismiss

		// Check if a snackbar is currently shown
		if (isShown)
		{
			// Dismiss the shown snackbar
			dismiss()

			// Queue the next one
			queue(message, action, listener)
			return
		}

		// Set the snackbar
		setSnackbar(message)

		// Setup the snackbar
		setAction(action, listener)
		setActionTextThemeColor()

		// Show the snackbar
		this.show()
	}

}