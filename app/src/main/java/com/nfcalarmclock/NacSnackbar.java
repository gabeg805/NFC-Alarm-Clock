package com.nfcalarmclock;

import android.content.Context;
import android.view.View;
import java.util.LinkedList;
import java.util.Queue;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;

/**
 */
public class NacSnackbar
	implements View.OnClickListener
{

	/**
	 * Snackbar data holder.
	 */
	private static class SnackHolder
	{

		/**
		 * Message.
		 */
		private String mMessage;

		/**
		 * Action button text.
		 */
		private String mAction;

		/**
		 * Click listener.
		 */
		private View.OnClickListener mListener;

		/**
		 */
		public SnackHolder(String message, String action,
			View.OnClickListener listener)
		{
			this.mMessage = message;
			this.mAction = action;
			this.mListener = listener;
		}

		/**
		 * @return The action button text.
		 */
		public String getAction()
		{
			return this.mAction;
		}

		/**
		 * @return The OnClickListener.
		 */
		public View.OnClickListener getListener()
		{
			return this.mListener;
		}

		/**
		 * @return The message.
		 */
		public String getMessage()
		{
			return this.mMessage;
		}

	}

	/**
	 * Response to snackbar onDismissed and onShown callbacks.
	 */
	private class NacSnackbarCallback
		extends BaseTransientBottomBar.BaseCallback<Snackbar>
	{

		@Override
		public void onDismissed(Snackbar transientBottomBar, int event)
		{
			Queue<SnackHolder> queue = getQueue();
			mSnackbar = null;

			if (queue.size() > 0)
			{
				SnackHolder holder = queue.remove();
				String message = holder.getMessage();
				String action = holder.getAction();
				View.OnClickListener listener = holder.getListener();

				show(message, action, listener, false);
			}
		}

		@Override
		public void onShown(Snackbar transientBottomBar)
		{
		}

	}

	/**
	 * Snackbar.
	 */
	private Snackbar mSnackbar;

	/**
	 * Root view.
	 */
	private View mRoot;

	/**
	 * Message queue.
	 */
	private Queue<SnackHolder> mQueue;

	/**
	 * Check if the snackbar can be dismissed early.
	 */
	private boolean mCanDismiss;

	/**
	 */
	public NacSnackbar(View root)
	{
		this.mSnackbar = null;
		this.mRoot = root;
		this.mQueue = new LinkedList<SnackHolder>();
		this.mCanDismiss = false;
	}

	/**
	 * Check if the snackbar can be dismissed early.
	 */
	public boolean canDismiss()
	{
		return this.mCanDismiss;
	}

	/**
	 * Dismiss the snackbar.
	 */
	public void dismiss()
	{
		Snackbar snackbar = this.getSnackbar();

		if (snackbar != null)
		{
			snackbar.dismiss();
		}
	}

	/**
	 * @return The context.
	 */
	public Context getContext()
	{
		View root = this.getRoot();
		return (root != null) ? root.getContext() : null;
	}

	/**
	 * @return The root view.
	 */
	public View getRoot()
	{
		return this.mRoot;
	}

	/**
	 * @return The queue.
	 */
	private Queue<SnackHolder> getQueue()
	{
		return this.mQueue;
	}

	/**
	 * @return The snackbar.
	 */
	public Snackbar getSnackbar()
	{
		return this.mSnackbar;
	}

	/**
	 * @return True if the snackbar is shown, and False otherwise.
	 */
	public boolean isShown()
	{
		Snackbar snackbar = this.getSnackbar();
		return ((snackbar != null) && snackbar.isShown());
	}

	/**
	 * Default listener when the action button is clicked.
	 */
	@Override
	public void onClick(View view)
	{
	}

	/**
	 * Queue the snackbar data.
	 */
	private void queue(String message, String action,
		View.OnClickListener listener)
	{
		SnackHolder holder = new SnackHolder(message, action, listener);
		this.mQueue.add(holder);
	}

	/**
	 * Set the snackbar action listener.
	 */
	public void setAction(String action, View.OnClickListener listener)
	{
		if (action.isEmpty())
		{
			return;
		}

		if (listener == null)
		{
			listener = this;
		}

		Snackbar snackbar = this.getSnackbar();
		snackbar.setAction(action, listener);
	}

	/**
	 * Set the action text color to the theme color.
	 */
	public void setActionTextThemeColor()
	{
		Snackbar snackbar = this.getSnackbar();
		Context context = this.getContext();
		NacSharedPreferences shared = new NacSharedPreferences(context);
		int themeColor = shared.getThemeColor();

		snackbar.setActionTextColor(themeColor);
	}

	/**
	 * Set the snackbar.
	 */
	protected void setSnackbar(String message)
	{
		View root = this.getRoot();
		Snackbar snackbar = Snackbar.make(root, message, Snackbar.LENGTH_LONG);
		this.mSnackbar = snackbar;

		snackbar.addCallback(new NacSnackbarCallback());
	}

	/**
	 * @see show
	 */
	public void show()
	{
		this.getSnackbar().show();
	}

	/**
	 * @see show
	 */
	public void show(String message, String action,
		View.OnClickListener listener)
	{
		this.show(message, action, listener, false);
	}

	/**
	 * Show the snackbar.
	 */
	public void show(String message, String action,
		View.OnClickListener listener, boolean dismiss)
	{
		this.mCanDismiss = dismiss;

		if (this.isShown())
		{
			this.dismiss();
			this.queue(message, action, listener);
			return;
		}

		this.setSnackbar(message);
		this.setAction(action, listener);
		this.setActionTextThemeColor();
		this.show();
	}

}
