package com.nfcalarmclock;

import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.view.View.MeasureSpec;

/**
 * Expand and collapse regions in the alarm card.
 */
public class NacCardRegion
{

	/**
	 * Summary region.
	 */
	private RelativeLayout mSummaryRegion;

	/**
	 * Extra region.
	 */
	private RelativeLayout mExtraRegion;

	/**
	 * Expand button.
	 */
	private ImageView mExpandButton;

	/**
	 * Collapse button.
	 */
	private ImageView mCollapseButton;

	/**
	 * The original height of the region when it is collapsed. This corresponds
	 * to the height of the summary region.
	 */
	public int mFromHeight;

	/**
	 * The height once the region is expanded. This corresponds to the height
	 * of the extra region.
	 */
	public int mToHeight;

	/**
	 */
	public NacCardRegion(View root)
	{
		this.mSummaryRegion = (RelativeLayout) root.findViewById(R.id.alarmMinorSummary);
		this.mExtraRegion = (RelativeLayout) root.findViewById(R.id.alarmMinorExpand);
		this.mExpandButton = (ImageView) root.findViewById(R.id.nacExpand);
		this.mCollapseButton = (ImageView) root.findViewById(R.id.nacCollapse);
		this.mFromHeight = 0;
		this.mToHeight = 0;
	}

	/**
	 * Initialize the summary and expandable regions.
	 */
	public void init()
	{
		this.measureFromHeight();
		this.measureToHeight();
		this.collapseNoAnimation();
	}

	/**
	 * Collapse the alarm card and animate the view.
	 */
	public void collapse()
	{
		this.collapseNoAnimation();

		NacSlideAnimation slide = new NacSlideAnimation(this.mSummaryRegion,
			this.mToHeight, this.mFromHeight);

		this.mSummaryRegion.startAnimation(slide);
	}

	/**
	 * @see collapse
	 *
	 * Do not animate the view.
	 */
	public void collapseNoAnimation()
	{
		this.mSummaryRegion.setVisibility(View.VISIBLE);
		this.mSummaryRegion.setEnabled(true);
		this.mExtraRegion.setVisibility(View.GONE);
		this.mExtraRegion.setEnabled(false);
	}

	/**
	 * Expand the alarm card and animate the view.
	 */
	public void expand()
	{
		this.expandNoAnimation();

		NacSlideAnimation slide = new NacSlideAnimation(this.mExtraRegion,
			this.mFromHeight, this.mToHeight);

		this.mExtraRegion.startAnimation(slide);
	}

	/**
	 * @see expand
	 *
	 * Do not animate the view.
	 */
	public void expandNoAnimation()
	{
		this.mSummaryRegion.setVisibility(View.GONE);
		this.mSummaryRegion.setEnabled(false);
		this.mExtraRegion.setVisibility(View.VISIBLE);
		this.mExtraRegion.setEnabled(true);
	}

	/**
	 * Return the height of the view that is visible.
	 */
	public int getHeight()
	{
		View v = this.getView();

		return NacUtility.getHeight(v);
	}

	/**
	 * Return the view that is visible.
	 */
	public View getView()
	{
		if (this.mSummaryRegion.isShown())
		{
			return this.mSummaryRegion;
		}
		else if (this.mExtraRegion.isShown())
		{
			return this.mExtraRegion;
		}
		else
		{
			return null;
		}
	}

	/**
	 * Measure the height of the collapsed region.
	 */
	private void measureFromHeight()
	{
		this.collapseNoAnimation();
		this.mSummaryRegion.measure(MeasureSpec.makeMeasureSpec(0,
			MeasureSpec.UNSPECIFIED), MeasureSpec.makeMeasureSpec(0,
			MeasureSpec.UNSPECIFIED));

		this.mFromHeight = this.mSummaryRegion.getMeasuredHeight();
	}

	/**
	 * Measure the height of the expanded region.
	 */
	private void measureToHeight()
	{
		this.expandNoAnimation();
		this.mExtraRegion.measure(MeasureSpec.makeMeasureSpec(0,
			MeasureSpec.UNSPECIFIED), MeasureSpec.makeMeasureSpec(0,
			MeasureSpec.UNSPECIFIED));

		this.mToHeight = mExtraRegion.getMeasuredHeight();
	}

	/**
	 * Set a listener to collapse the card.
	 *
	 * @param  listener  The on click listener.
	 */
	public void setCollapseListener(View.OnClickListener listener)
	{
		this.mCollapseButton.setOnClickListener(listener);
	}

	/**
	 * Set a listener to expand the card.
	 *
	 * @param  listener  The on click listener.
	 */
	public void setExpandListener(View.OnClickListener listener)
	{
		this.mExpandButton.setOnClickListener(listener);
	}

}
