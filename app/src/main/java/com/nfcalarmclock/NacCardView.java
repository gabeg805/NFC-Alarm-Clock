package com.nfcalarmclock;

//import android.view.animation.Animation;
import android.view.View;
import android.widget.LinearLayout;
import androidx.cardview.widget.CardView;

/**
 */
public class NacCardView
{

	/**
	 * Card view.
	 */
	private CardView mCardView;

	/**
	 * Extra region.
	 */
	private LinearLayout mExtra;

	/**
	 * Summary region.
	 */
	private LinearLayout mSummary;

	/**
	 * Card animation.
	 */
	private NacCardSlideAnimation mSlideAnimation;

	/**
	 */
	public NacCardView(View root)
	{
		this.mCardView = root.findViewById(R.id.nac_card);
		this.mSummary = root.findViewById(R.id.nac_summary);
		this.mExtra = root.findViewById(R.id.nac_extra);
		this.mSlideAnimation = new NacCardSlideAnimation(this.mCardView,
			this.mSummary, this.mExtra);

		//this.getSlideAnimation().setOnAnimationListener(this);
	}

	/**
	 * Animate the card sliding.
	 */
	public void animate(int fromHeight, int toHeight, int duration)
	{
		CardView card = this.getCardView();
		NacCardSlideAnimation animation = this.getSlideAnimation();
		NacUtility.printf("Animate : %d -> %d", fromHeight, toHeight);

		animation.setDuration(duration);
		animation.setHeights(fromHeight, toHeight);
		//animation.setupForClose();
		//card.setAnimation(animation);
		card.startAnimation(animation);
	}

	/**
	 * @return The card view.
	 */
	public CardView getCardView()
	{
		return this.mCardView;
	}

	/**
	 * @return The slide animation.
	 */
	private NacCardSlideAnimation getSlideAnimation()
	{
		return this.mSlideAnimation;
	}

	/**
	 * Set the animation listener.
	 */
	public void setOnAnimationListener(
		NacCardSlideAnimation.OnAnimationListener listener)
	{
		this.getSlideAnimation().setOnAnimationListener(listener);
	}

}
