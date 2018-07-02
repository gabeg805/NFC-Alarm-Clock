package com.nfcalarmclock;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.v7.app.AppCompatActivity;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

/**
 * @brief Recycler view for all alarm cards.
 */
public class NacCardRecyclerView
{

    /**
     * @brief Activity.
     */
    private AppCompatActivity mActivity;

    /**
     * @brief Container for displaying the list of alarm cards.
     */
    private RecyclerView mRecyclerView;

    /**
     * @brief Alarm card adapter.
     */
    private AlarmCardAdapter mAdapter;

    /**
     * @brief Constructor.
     */
    public NacCardRecyclerView(AppCompatActivity activity)
    {
        this.mActivity = activity;
        this.mRecyclerView = (RecyclerView) activity.findViewById(R.id.content_alarm_list);
        this.mAdapter = new AlarmCardAdapter(activity);
    }

    /**
     * @brief Add an alarm to the adapter.
     */
    public void addAlarm()
    {
        this.mAdapter.add(new Alarm());
    }

    /**
     * @brief Initialize the recycler view and alarm adapter.
     */
    public void init(NacFloatingButton fb)
    {
        this.initRecyclerView(fb);
        this.initAdapter();
    }

    /**
     * @brief Initialize the recycler view
     */
    private void initRecyclerView(NacFloatingButton fb)
    {
        NacLayoutManager manager = new NacLayoutManager(this.mActivity, fb);
        DefaultItemAnimator anim = new DefaultItemAnimator();
        Drawable div = ContextCompat.getDrawable(this.mActivity,
                                                 R.drawable.divider);
        DividerItemDecoration decor = new DividerItemDecoration(this.mActivity,
                                                                LinearLayoutManager.VERTICAL);
        decor.setDrawable(div);
        this.mRecyclerView.setAdapter(this.mAdapter);
        this.mRecyclerView.setLayoutManager(manager);
        this.mRecyclerView.setItemAnimator(anim);
        this.mRecyclerView.addItemDecoration(decor);
        this.mRecyclerView.addOnScrollListener(new NacScrollListener(fb));
    }

    /**
     * @brief Initialize the alarm adapter
     */
    private void initAdapter()
    {
        this.mAdapter.build();
    }

    /**
     * @brief Layout manager for the recycler view to catch over scrolling so as
     *        to show/hide the floating action button.
     */
    private class NacLayoutManager
        extends LinearLayoutManager
    {

        /**
         * @brief Floating action button.
         */
        private NacFloatingButton mFloatingButton;

        /**
         * @brief Constructor.
         */
        public NacLayoutManager(Context c, NacFloatingButton fb)
        {
            super(c);
            this.mFloatingButton = fb;
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
         * @brief The floating button.
         */
        private NacFloatingButton mFloatingButton;

        /**
         * @brief Constructor.
         */
        public NacScrollListener(NacFloatingButton fb)
        {
            this.mFloatingButton = fb;
        }

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
