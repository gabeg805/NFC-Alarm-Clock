package com.nfcalarmclock;

import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

/**
 * Linear layout manager for a recyclerview that is primarily used because it
 * handles the smooth scrolling.
 */
public class NacLayoutManager
	extends LinearLayoutManager
{

	/**
	 * Context.
	 */
	private Context mContext;

	/**
	 */
	public NacLayoutManager(Context context)
	{
		super(context);

		this.mContext = context;
	}

	/**
	 * @return The context.
	 */
	private Context getContext()
	{
		return this.mContext;
	}

	/**
	 */
    @Override
    public void smoothScrollToPosition(RecyclerView recyclerView,
		RecyclerView.State state, int position)
	{
		Context context = this.getContext();
		NacSmoothScroller smoothScroller = new NacSmoothScroller(context,
			position);

        startSmoothScroll(smoothScroller);
    }

}
