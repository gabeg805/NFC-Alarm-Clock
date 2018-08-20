package com.nfcalarmclock;

import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.TransitionDrawable;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.MenuInflater;
import android.view.View;

/**
 * @brief Holder of all important views.
 */
public class NacCard
    extends RecyclerView.ViewHolder
    implements View.OnCreateContextMenuListener,View.OnClickListener
{

	/**
	 * @brief Activity.
	 */
    public AppCompatActivity mActivity;

	/**
	 * @brief Context.
	 */
    public Context mContext;

	/**
	 * @brief The alarm card view.
	 */
    public CardView mCard;

	/**
	 * @brief Summary and extra regions in the alarm card.
	 */
	public NacCardRegion mRegion;

	/**
	 * @brief The on/off switch for the alarm.
	 */
    public NacCardSwitch mSwitch;

	/**
	 * @brief The alarm time.
	 */
    public NacCardTime mTime;

	/**
	 * @brief Repeat checkbox.
	 */
    public NacCardRepeat mRepeat;

	/**
	 * @brief Sound selector.
	 */
    public NacCardSound mSound;

	/**
	 * @brief Vibrate checkbox.
	 */
    public NacCardVibrate mVibrate;

	/**
	 * @brief Name label and selector.
	 */
    public NacCardName mName;

	/**
	 * @brief Button to delete the alarm card.
	 */
    public NacCardDelete mDelete;

    /**
     * @brief The alarm data used in populating this alarm card.
     */
    private Alarm mAlarm;

    /**
     * @brief Define all views in the holder.
	 *
	 * @param  c  The activity's context.
	 * @param  r  The root view.
     */
    public NacCard(Context c, View r)
    {
        super(r);
        this.mActivity = (AppCompatActivity) c;
        this.mContext = c;
        this.mCard = (CardView) r.findViewById(R.id.view_card_alarm);

		this.mRegion = new NacCardRegion(r);
        this.mTime = new NacCardTime(c, r);
        this.mSwitch = new NacCardSwitch(r);
        this.mRepeat = new NacCardRepeat(r);
        this.mSound = new NacCardSound(c, r);
        this.mVibrate = new NacCardVibrate(r);
        this.mName = new NacCardName(c, r);
        this.mDelete = new NacCardDelete(r);

	}

    /**
     * @brief Initialize the alarm card.
	 *
	 * @param  alarm  The alarm to use to populate data in the alarm card.
	 * @param  pos  The position of the alarm card in the recycler view.
     */
    public void init(Alarm alarm, int pos)
    {
        this.mAlarm = alarm;

		this.mRegion.init();
        this.mTime.init(alarm);
        this.mSwitch.init(alarm);
        this.mRepeat.init(alarm);
        this.mSound.init(alarm);
        this.mVibrate.init(alarm);
        this.mName.init(alarm);
		this.mDelete.init(pos);

        this.mActivity.registerForContextMenu(this.mCard);
        this.mCard.setOnCreateContextMenuListener(this);
		this.mRegion.setExpandListener(this);
		this.mRegion.setCollapseListener(this);
    }

	TransitionDrawable mTransition = null;

	/**
	 * @brief Focus the alarm card.
	 */
	public void focus()
	{
		int duration = 2200;
		int bg = NacUtility.getThemeAttrColor(this.mContext, R.attr.colorCard);
		int highlight= NacUtility.getThemeAttrColor(this.mContext,
			R.attr.colorCardExpanded);
		ColorDrawable[] color = {new ColorDrawable(highlight),
			new ColorDrawable(bg)};
		this.mTransition = new TransitionDrawable(color);

		this.mCard.setBackgroundDrawable(this.mTransition);
		this.mTransition.startTransition(duration);

		NacUtility.print("~~~~~~~~~~~~~~~~~~ CHANGING FPOCUS");
		//new Handler().postDelayed(new Runnable()
		//{
		//	@Override
		//	public void run()
		//	{
		//		if (mTransition == null)
		//		{
		//			return;
		//		}

		//		//NacUtility.print("Resetting background.");
		//		//mTransition.reverseTransition(2200);
		//		backgroundReset();
		//		mTransition = null;
		//	}
		//}, duration);
	}

    /**
     * @brief Expand the alarm card.
     */
    public void expand()
    {
		if (this.mTransition != null)
		{
			mTransition.resetTransition();
			backgroundReset();
			this.mTransition = null;
		}

		scrollToTop();
		mRegion.expandAndAnimate();
		backgroundExpanded();
    }

    /**
     * @brief Collapse the alarm card.
     */
    public void collapse()
    {
		mRegion.collapseAndAnimate();
		backgroundReset();
    }

	/**
	 * @brief Scroll card view to the top of the screen.
	 */
	public void scrollToTop()
	{
		int pos = getAdapterPosition();
        RecyclerView rv = (RecyclerView) mActivity.findViewById(R.id.content_alarm_list);
        rv.getLayoutManager().startSmoothScroll(new NacSmoothScroller(mContext, pos));
		//rv.scrollToPosition(pos);
	}

	/**
	 * @brief Reset background color of the alarm card.
	 */
	public void backgroundReset()
	{
		int bg = NacUtility.getThemeAttrColor(this.mContext, R.attr.colorCard);
		this.mCard.setBackground(null);
		this.mCard.setBackgroundColor(bg);
	}

	/**
	 * @brief Set background color of the alarm card to the expanded color.
	 */
	public void backgroundExpanded()
	{
		int bg = NacUtility.getThemeAttrColor(this.mContext,
											R.attr.colorCardExpanded);
		this.mCard.setBackground(null);
		this.mCard.setBackgroundColor(bg);
	}

	/**
	 * @brief Set background color of the alarm card to the highlight color.
	 */
	public void backgroundHighlight()
	{
		int bg = NacUtility.getThemeAttrColor(this.mContext,
											R.attr.colorCardHighlight);
		this.mCard.setBackground(null);
		this.mCard.setBackgroundColor(bg);
	}

	/**
	 * @brief Set listener to delete the card.
	 */
	public void setDeleteListener(View.OnClickListener listener)
	{
		this.mDelete.setListener(listener);
	}

    /**
     * @brief Display the context menu for the selected alarm card.
     */
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
                                    ContextMenuInfo menuInfo)
    {
        MenuInflater inflater = mActivity.getMenuInflater();
        inflater.inflate(R.menu.alarm_card, menu);
    }

	/**
	 * @brief Expand or collapse the card.
	 *
	 * @details When the alarm card is not at the top of the screen, clicking
	 * 			the expand button will scroll the screen so that the alarm card
	 * 			is at the top.
	 */
	@Override
	public void onClick(View v)
	{
		int id = v.getId();

		switch (id)
		{
			case R.id.nacExpand:
				expand();
				break;
			case R.id.nacCollapse:
				collapse();
				break;
			default:
				break;
		}
	}

}
