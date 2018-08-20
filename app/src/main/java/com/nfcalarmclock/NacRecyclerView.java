package com.nfcalarmclock;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

/**
 * @brief Recycler view for all alarm cards.
 */
public class NacRecyclerView
{

    /**
     * @brief Context.
     */
    private Context mContext;

    /**
     * @brief Container for displaying the list of alarm cards.
     */
    private RecyclerView mRecyclerView;

    /**
     * @brief The recycler view containing all alarm cards.
	 *
	 * @param  c  The activity context.
     */
    public NacRecyclerView(Context c)
    {
		AppCompatActivity a = (AppCompatActivity) c;
        this.mContext = c;
        this.mRecyclerView = (RecyclerView) a.findViewById(R.id.content_alarm_list);
    }

    /**
     * @brief Initialize the recycler view.
     */
    public void init()
    {
		LinearLayoutManager manager = new LinearLayoutManager(this.mContext);
        DefaultItemAnimator anim = new DefaultItemAnimator();
        Drawable div = ContextCompat.getDrawable(this.mContext,
                                                 R.drawable.divider);
        DividerItemDecoration decor = new DividerItemDecoration(this.mContext,
                                                                LinearLayoutManager.VERTICAL);
        decor.setDrawable(div);
        this.mRecyclerView.setLayoutManager(manager);
        this.mRecyclerView.setItemAnimator(anim);
        this.mRecyclerView.addItemDecoration(decor);
    }

	/**
	 * @brief Set the recycler view adapter and floating action button.
	 *
	 * @param  adapter  The alarm card adapter.
	 */
	public void setAdapter(NacCardAdapter adapter)
	{
		this.mRecyclerView.setAdapter(adapter);
	}

	/**
	 * @brief Set a listener when scrolling to show/hide the floating action
	 * 		  listener.
	 * 
	 * @param  fb  The activity's floating action button.
	 */
	public void setScrollListener(NacFloatingButton fb)
	{
		this.mRecyclerView.addOnScrollListener(new NacScrollListener(fb));
	}

}
