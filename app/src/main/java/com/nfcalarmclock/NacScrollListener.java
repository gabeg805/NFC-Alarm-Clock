package com.nfcalarmclock;

import android.support.v7.widget.RecyclerView;

/**
 * @brief A scroll listener for the recycler view.
 */
public class NacScrollListener
	extends RecyclerView.OnScrollListener
{

	/**
	 * @brief Floating action button.
	 */
	private NacFloatingButton mFloatingButton;

	/**
	 * @brief Set the floating action button that will be shown/hid when
	 *		  scrolling.
	 *
	 * @param  fb  The activity's floating action button.
	 */
	public NacScrollListener(NacFloatingButton fb)
	{
		this.mFloatingButton = fb;
	}

	/**
	 * @brief Show the floating action button when scrolling up and hide it
	 *		  when scrolling down.
	 *
	 * @param  rv  The recycler view.
	 * @param  dx  The change in scrolling in the x-direction.
	 * @param  dy  The change in scrolling in the y-direction.
	 */
	@Override
	public void onScrolled(RecyclerView rv, int dx, int dy)
	{
		super.onScrolled(rv, dx, dy);

		if ((dy < 0) && !mFloatingButton.isShown())
		{
			mFloatingButton.show();
		}
		else if ((dy > 0) && mFloatingButton.isShown())
		{
			mFloatingButton.hide();
		}
	}

	/**
	 * @brief Display the floating button when at the bottom of the list.
	 *
	 * @param  rv  The recycler view.
	 * @param  state The scroll state (Idle, Dragging, or Settling).
	 */
	@Override
	public void onScrollStateChanged(RecyclerView rv, int state)
	{
		super.onScrollStateChanged(rv, state);

		if ((state == 0) && !rv.canScrollVertically(1))
		{
			if (!mFloatingButton.isShown())
			{
				mFloatingButton.show();
			}
		}
	}

}
