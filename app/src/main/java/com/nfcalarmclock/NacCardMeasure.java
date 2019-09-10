package com.nfcalarmclock;

import android.app.Activity;
import android.content.Context;
//import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import androidx.recyclerview.widget.RecyclerView;

/**
 * Measurements of an alarm card.
 */
public class NacCardMeasure
{

	/**
	 * Context.
	 */
	private Context mContext;

	/**
	 * Screen width.
	 */
	private int mScreenWidth;

	/**
	 * Screen height.
	 */
	private int mScreenHeight;

	/**
	 * Header height.
	 */
	private int mHeaderHeight;

	/**
	 * Collapsed height.
	 */
	private int mCollapseHeight;

	/**
	 * Expanded height.
	 */
	private int mExpandHeight;

	/**
	 * Alarm card padding.
	 */
	private int mCardPadding;

	/**
	 * Day buttons.
	 */
	private NacDayOfWeek mDayButtons;

	/**
	 * Day buttons height.
	 */
	private int mDayButtonsHeight;

	/**
	 * Vibrate width.
	 */
	private int mVibrateWidth;

	/**
	 * Check if card has been measured.
	 */
	private boolean mIsMeasured;

	/**
	 */
	public NacCardMeasure(Context context)
	{
		this.mContext = context;
		this.mScreenWidth = 0;
		this.mScreenHeight= 0;
		this.mHeaderHeight = 0;
		this.mCollapseHeight = 0;
		this.mExpandHeight = 0;
		this.mCardPadding = 0;
		this.mDayButtons = null;
		this.mDayButtonsHeight = 0;
		this.mVibrateWidth = 0;
		this.mIsMeasured = false;
	}

	/**
	 * @return The card padding.
	 */
	public int getCardPadding()
	{
		return this.mCardPadding;
	}

	/**
	 * @return The collapse height.
	 */
	public int getCollapseHeight()
	{
		return this.mCollapseHeight;
	}

	/**
	 * @return The context.
	 */
	private Context getContext()
	{
		return this.mContext;
	}

	/**
	 * @return The day buttons.
	 */
	public NacDayOfWeek getDayButtons()
	{
		return this.mDayButtons;
	}

	/**
	 * @return The height of the day buttons.
	 */
	public int getDayButtonsHeight()
	{
		return this.mDayButtonsHeight;
	}

	/**
	 * @return The expand height.
	 */
	public int getExpandHeight()
	{
		return this.mExpandHeight;
	}

	/**
	 * @return The header height.
	 */
	public int getHeaderHeight()
	{
		return this.mHeaderHeight;
	}

	/**
	 * @return The screen height.
	 */
	public int getScreenHeight()
	{
		return this.mScreenHeight;
	}

	/**
	 * @return The screen width.
	 */
	public int getScreenWidth()
	{
		return this.mScreenWidth;
	}

	/**
	 * @return The vibrate width.
	 */
	public int getVibrateWidth()
	{
		return this.mVibrateWidth;
	}

	/**
	 * @return True if alarm card is measured, and False otherwise.
	 */
	public boolean isMeasured()
	{
		return this.mIsMeasured;
	}

	/**
	 * Measure the alarm card.
	 */
	public void measure(RecyclerView rv)
	{
		NacCardHolder holder = (NacCardHolder) rv.findViewHolderForAdapterPosition(0);

		if (holder != null)
		{
			this.measure(holder);
		}
	}

	/**
	 * Measure the alarm card.
	 */
	public void measure(NacCardHolder holder)
	{
		//NacUtility.printf("NacCardMeasure MEASURING!");
		View root = holder.getRoot();

		this.setScreenWidth();
		this.setScreenHeight();
		this.setCardPadding(root);
		this.setDayButtons(root);
		this.setDayButtonsHeight(root);
		this.setVibrateWidth(root);
		this.setHeaderHeight(root);
		this.setExpandHeight(root);
		this.setCollapseHeight(root);

		this.mIsMeasured = true;
	}

	/**
	 * Set alarm card padding.
	 */
	private void setCardPadding(View root)
	{
		RelativeLayout header = root.findViewById(R.id.nac_header);
		this.mCardPadding = header.getPaddingStart() + header.getPaddingEnd();
		//NacUtility.printf("CARD PADDING : %d", mCardPadding);
	}

	/**
	 * Set the collapse height.
	 */
	private void setCollapseHeight(View root)
	{
		RelativeLayout summary = (RelativeLayout) root.findViewById(R.id.nac_summary);
		//LinearLayout extra = (LinearLayout) root.findViewById(R.id.nac_extra);
		//summary.setVisibility(View.VISIBLE);
		//extra.setVisibility(View.GONE);
		//summary.setEnabled(true);
		//extra.setEnabled(false);
		//summary.requestLayout();

		int headerHeight = this.getHeaderHeight();
		int summaryHeight = NacUtility.getHeight(summary);
		this.mCollapseHeight = headerHeight + summaryHeight;
		//NacUtility.printf("COLLAPSE HEIGHT : %d", mCollapseHeight);
	}

	/**
	 * Set the day buttons.
	 */
	private void setDayButtons(View root)
	{
		this.mDayButtons = (NacDayOfWeek)
			root.findViewById(R.id.nac_days);
	}

	/**
	 * Set the height of the day buttons.
	 */
	private void setDayButtonsHeight(View root)
	{
		NacDayOfWeek dayButtons = (NacDayOfWeek)
			root.findViewById(R.id.nac_days);
		this.mDayButtonsHeight = NacUtility.getHeight(dayButtons);
		//NacUtility.printf("DAY BUTTONS : %d", mDayButtonsHeight);
	}

	/**
	 * Set the expand height.
	 */
	private void setExpandHeight(View root)
	{
		//RelativeLayout summary = (RelativeLayout) root.findViewById(R.id.nac_summary);
		LinearLayout extra = (LinearLayout) root.findViewById(R.id.nac_extra);
		//summary.setVisibility(View.GONE);
		//extra.setVisibility(View.VISIBLE);
		//summary.setEnabled(false);
		//extra.setEnabled(true);
		//extra.requestLayout();

		int headerHeight = this.getHeaderHeight();
		int extraHeight = NacUtility.getHeight(extra);
		this.mExpandHeight = headerHeight + extraHeight;
		//NacUtility.printf("EXPAND HEIGHT : %d", mExpandHeight);
	}

	/**
	 * Set the header height.
	 */
	private void setHeaderHeight(View root)
	{
		RelativeLayout header = (RelativeLayout) root.findViewById(R.id.nac_header);
		//header.requestLayout();
		this.mHeaderHeight = NacUtility.getHeight(header);
		//NacUtility.printf("HEADER HEIGHT : %d", this.mHeaderHeight);
	}

	/**
	 * Set the screen height.
	 */
	private void setScreenHeight()
	{
		Context context = this.getContext();
		RecyclerView rv = (RecyclerView) ((Activity)context).findViewById(
			R.id.content_alarm_list);
		int fabHeight = (int) context.getResources()
			.getDimension(R.dimen.pb_for_fab);
		int recyclerHeight = rv.getHeight();
		this.mScreenHeight = recyclerHeight - fabHeight;
		//NacUtility.printf("SCREEN HEIGHT : %d", mScreenHeight);
	}

	/**
	 * Set the screen width.
	 */
	private void setScreenWidth()
	{
		this.mScreenWidth = this.getContext().getResources()
			.getDisplayMetrics().widthPixels;
		//NacUtility.printf("SCREEN WIDTH : %d", mScreenWidth);
	}

	/**
	 * Set the vibrate width.
	 */
	private void setVibrateWidth(View root)
	{
		CheckBox vibrate = (CheckBox) root.findViewById(R.id.nac_vibrate);
		this.mVibrateWidth = NacUtility.getWidth(vibrate);
		//NacUtility.printf("VIBRATE WIDTH : %d", mVibrateWidth);
	}

}
