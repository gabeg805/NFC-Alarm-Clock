package com.nfcalarmclock;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import androidx.recyclerview.widget.RecyclerView;

//import androidx.cardview.widget.CardView;

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
	 * Check if card has been measured.
	 */
	private boolean mIsMeasured;

	/**
	 */
	public NacCardMeasure(Context context)
	{
		this.mContext = context;
		this.mHeaderHeight = 0;
		this.mCollapseHeight = 0;
		this.mExpandHeight = 0;
		this.mIsMeasured = false;
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
		View root = holder.getRoot();

		this.setHeaderHeight(root);
		this.setExpandHeight(root);
		this.setCollapseHeight(root);

		this.mIsMeasured = true;
	}

	/**
	 * Set the collapse height.
	 */
	private void setCollapseHeight(View root)
	{
		View summary = (View) root.findViewById(R.id.nac_summary);

		int headerHeight = this.getHeaderHeight();
		int summaryHeight = NacUtility.getHeight(summary);
		this.mCollapseHeight = headerHeight + summaryHeight;
		NacUtility.printf("Collapse height : %d", this.mCollapseHeight);
	}

	/**
	 * Set the expand height.
	 */
	private void setExpandHeight(View root)
	{
		View extra = (View) root.findViewById(R.id.nac_extra);

		int headerHeight = this.getHeaderHeight();
		int extraHeight = NacUtility.getHeight(extra);
		this.mExpandHeight = headerHeight + extraHeight;
		NacUtility.printf("Expand height : %d", this.mExpandHeight);
	}

	/**
	 * Set the header height.
	 */
	private void setHeaderHeight(View root)
	{
		View header = (View) root.findViewById(R.id.nac_header);
		this.mHeaderHeight = NacUtility.getHeight(header);
	}

}
