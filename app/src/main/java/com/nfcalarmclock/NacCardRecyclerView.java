package com.nfcalarmclock;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

/**
 * @brief Recycler view for all alarm cards.
 */
public class NacCardRecyclerView
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
	 * @brief Floating action button to add new alarms.
	 */
	private NacFloatingButton mFloatingButton;

    /**
     * @brief Constructor.
     */
    public NacCardRecyclerView(Context c)
    {
		AppCompatActivity a = (AppCompatActivity) c;
        this.mContext = c;
        this.mRecyclerView = (RecyclerView) a.findViewById(R.id.content_alarm_list);
    }

    /**
     * @brief Initialize the recycler view
     */
    public void init()
    {
        NacLayoutManager manager = new NacLayoutManager(this.mContext);
        DefaultItemAnimator anim = new DefaultItemAnimator();
        Drawable div = ContextCompat.getDrawable(this.mContext,
                                                 R.drawable.divider);
        DividerItemDecoration decor = new DividerItemDecoration(this.mContext,
                                                                LinearLayoutManager.VERTICAL);
        decor.setDrawable(div);
        this.mRecyclerView.setLayoutManager(manager);
        this.mRecyclerView.setItemAnimator(anim);
        this.mRecyclerView.addItemDecoration(decor);
        this.mRecyclerView.addOnScrollListener(new NacScrollListener());
    }

	/**
	 * @brief Set the recycler view adapter and floating action button.
	 */
	public void setItems(NacCardAdapter adapter, NacFloatingButton fb)
	{
		this.mFloatingButton = fb;
		this.mRecyclerView.setAdapter(adapter);
	}

    /**
     * @brief Layout manager for the recycler view to catch over scrolling so as
     *        to show/hide the floating action button.
     */
    private class NacLayoutManager
        extends LinearLayoutManager
    {

        /**
         * @brief Constructor.
         */
        public NacLayoutManager(Context c)
        {
            super(c);
        }

        /**
         * @brief Detect overscrolls and display or hide the floating action
         *        button accordingly.
         */
        @Override
        public int scrollVerticallyBy(int dx, RecyclerView.Recycler recycler,
                                      RecyclerView.State state)
        {
            int scrollRange = super.scrollVerticallyBy(dx, recycler, state);
            int overscroll = dx - scrollRange;

            if (overscroll > 0)
            {
                if (mFloatingButton.isShown())
                {
                    mFloatingButton.hide();
                }
            }
            else if (overscroll < 0)
            {
                if (!mFloatingButton.isShown())
                {
                    mFloatingButton.show();
                }
            }

            return scrollRange;
        }

    }

    /**
     * @brief A scroll listener for the recycler view because I cannot implement
     *        it in the above class. It needs to be extended due to it not being
     *        an interface.
     */
    private class NacScrollListener
        extends RecyclerView.OnScrollListener
    {

        /**
         * @brief Hide the floating button when scrolling down and display it when
         *        scrolling up.
         */
        @Override
        public void onScrolled(RecyclerView rv, int dx, int dy)
        {
            if ((dy > 0) && mFloatingButton.isShown())
            {
                mFloatingButton.hide();
            }
            else if ((dy < 0) && !mFloatingButton.isShown())
            {
                mFloatingButton.show();
            }
        }

    }

}
