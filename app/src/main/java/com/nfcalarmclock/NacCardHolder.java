package com.nfcalarmclock;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.LinearSmoothScroller;
import android.view.View;

/**
 * Card view holder.
 */
public class NacCardHolder
	extends RecyclerView.ViewHolder
	implements View.OnClickListener,NacCard.OnExpandListener
{

	/**
	 * Delete listener.
	 */
	public interface OnDeleteListener
	{
		public void onDelete(int pos);
	}

	/**
	 * Delete listener.
	 */
	public OnDeleteListener mDeleteListener;

	/**
	 * The recycler view.
	 */
	public RecyclerView mRecyclerView;

	/**
	 * Alarm card view.
	 */
	public NacCard mCard;

	/**
	 * The on/off switch for the alarm.
	 */
	public NacCardSwitch mSwitch;

	/**
	 * The alarm time.
	 */
	public NacCardTime mTime;

	/**
	 * Repeat checkbox.
	 */
	public NacCardRepeat mRepeat;

	/**
	 * Sound selector.
	 */
	public NacCardSound mSound;

	/**
	 * Vibrate checkbox.
	 */
	public NacCardVibrate mVibrate;

	/**
	 * Alarm name.
	 */
	public NacCardName mName;

	/**
	 * Button to delete the alarm card.
	 */
	public NacCardDelete mDelete;

	/**
	 */
	public NacCardHolder(View root)
	{
		super(root);

		Activity activity = (Activity) root.getContext();

		this.mRecyclerView = (RecyclerView) activity.findViewById(
			R.id.content_alarm_list);
		this.mCard = new NacCard(root);
		this.mTime = new NacCardTime(root);
		this.mSwitch = new NacCardSwitch(root);
		this.mRepeat = new NacCardRepeat(root);
		this.mSound = new NacCardSound(root);
		this.mVibrate = new NacCardVibrate(root);
		this.mName = new NacCardName(root);
		this.mDelete = new NacCardDelete(root);
	}

	/**
	 * @return The card height.
	 */
	private int getCardHeight()
	{
		int regionheight = this.mCard.getRegionHeight();
		int timeheight = this.mTime.getHeight();
		int switchheight = this.mSwitch.getHeight();

		return (timeheight >= switchheight) ? regionheight + timeheight 
			: regionheight + switchheight;
	}

	/**
	 * @return The screen height.
	 */
	private int getScreenHeight()
	{
		return this.mRecyclerView.getHeight();
	}

	/**
	 * Initialize the alarm card.
	 *
	 * @param  alarm  The alarm to use to populate data in the alarm card.
	 * @param  wasAdded  Indicator for whether or not the card should be
	 *					 focused.
	 */
	public void init(NacAlarm alarm, boolean wasAdded)
	{
		this.mCard.init(wasAdded);
		this.mTime.init(alarm);
		this.mSwitch.init(alarm);
		this.mRepeat.init(alarm);
		this.mSound.init(alarm);
		this.mVibrate.init(alarm);
		this.mName.init(alarm);
		this.mCard.setOnExpandListener(this);
		this.mDelete.setOnClickListener(this);
	}

	/**
	 * Check if the card is completely visible.
	 *
	 * @return True if it is visible. False otherwise.
	 */
	private boolean isCompletelyVisible()
	{
		LinearLayoutManager lv = (LinearLayoutManager)
			this.mRecyclerView.getLayoutManager();
		int pos = getAdapterPosition();
		int ypos = lv.findViewByPosition(pos).getTop();
		int cardheight = this.getCardHeight();
		int screenheight = this.getScreenHeight();

		return ((cardheight+ypos) > screenheight) ? false : true;
	}

	/**
	 * When delete button is clicked, call the delete listener to delete the view.
	 *
	 * @param  view  The delete view.
	 */
	@Override
	public void onClick(View view)
	{
		if (this.mDeleteListener != null)
		{
			int pos = this.getAdapterPosition();

			this.mDeleteListener.onDelete(pos);
		}
	}

	/**
	 * Scroll when the alarm card is partially visible.
	 */
	@Override
	public void onExpand(NacCard card)
	{
		this.scrollOnPartiallyVisible();
	}

	/**
	 * Scroll when the alarm card is partially visible.
	 */
	public void scrollOnPartiallyVisible()
	{
		if (this.isCompletelyVisible())
		{
			return;
		}

		int delay = 200;

		new Handler().postDelayed(new Runnable()
		{
			@Override
			public void run()
			{
				Context context = mRecyclerView.getContext();
				int pos = getAdapterPosition();

				mRecyclerView.getLayoutManager().startSmoothScroll(
					new NacSmoothScroller(context, pos,
						LinearSmoothScroller.SNAP_TO_END));
			}
		}, delay);
	}

	/**
	 * Set listener to delete the card.
	 *
	 * @param  listener  The delete listener.
	 */
	public void setOnDeleteListener(OnDeleteListener listener)
	{
		this.mDeleteListener = listener;
	}

}
