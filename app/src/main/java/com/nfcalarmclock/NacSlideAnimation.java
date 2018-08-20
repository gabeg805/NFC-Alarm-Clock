package com.nfcalarmclock;

import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.view.View;
import android.view.ViewGroup.LayoutParams;

/**
 * @brief NFC Alarm Clock Slide Animation.
 */
public class NacSlideAnimation
	extends Animation
{

	/**
	 * @brief The view to animate.
	 */
    private View mView;

	/**
	 * @brief The original height of the view.
	 */
    private int mFromHeight;

	/**
	 * @brief The height to set the view to, once the animation is complete.
	 */
    private int mToHeight;

	/**
	 * @brief Constructor where the animation duraiton is calculated for the
	 * 		  user.
	 *
	 * @note See NacSlideAnimation for information on the params.
	 */
    public NacSlideAnimation(View v, int from, int to)
	{
		this(v, from, to, -1);
    }

	/**
	 * @brief Constructor setting all aspects of the slide animation object.
	 *
	 * @param  v  The view to animate.
	 * @param  from  The original height of the view. The view will be set to
	 * 				 this size even if it is not this size to begin with.
	 * @param  to  The height of the view once the animation is finished.
	 * @param  duration   The duration of the animation.
	 */
	public NacSlideAnimation(View v, int from, int to, int duration)
	{
        this.mView = v;
        this.mFromHeight = from;
        this.mToHeight = to;

		if (duration < 0)
		{
			float density = v.getContext().getResources().getDisplayMetrics().density;
			duration = (int)(((to > from) ? to : from)/density);
		}

		this.mView.getLayoutParams().height = this.mFromHeight;
		this.setDuration(duration);
	}

	/**
	 * @brief Apply the transformation to the view to animate it.
	 */
    @Override
    protected void applyTransformation(float time, Transformation trans)
	{
        int newHeight;

		//NacUtility.printf("View Height : %d", mView.getHeight());
		//NacUtility.printf("To Height   : %d", mToHeight);

        if (mView.getHeight() != mToHeight)
		{
            newHeight = (int) (mFromHeight + ((mToHeight - mFromHeight) * time));
            mView.getLayoutParams().height = newHeight;
			//NacUtility.printf("Transform Height : %d", mView.getLayoutParams().height);
            mView.requestLayout();
        }
    }

	/**
	 * @brief Initialize the animation.
	 */
    @Override
    public void initialize(int width, int height,
						   int parentWidth, int parentHeight)
	{
        super.initialize(width, height, parentWidth, parentHeight);
    }

	/**
	 * @brief Change the bounds of the animation.
	 */
    @Override
    public boolean willChangeBounds()
	{
        return true;
    }

}
