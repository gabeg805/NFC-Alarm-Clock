package com.nfcalarmclock;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

/**
 * Alarm card adapter.
 */
@SuppressWarnings("UnnecessaryInterfaceModifier")
public class NacAlarmCardAdapter
	extends ListAdapter<NacAlarm, NacCardHolder>
{

	/**
	 * Listener for when an alarm card is bound.
	 */
	public interface OnViewHolderBoundListener
	{
		public void onViewHolderBound(NacCardHolder holder, int index);
	}

	/**
	 * Listener for when an alarm card is created.
	 */
	public interface OnViewHolderCreatedListener
	{
		public void onViewHolderCreated(NacCardHolder holder);
	}

	/**
	 * Listener for when an alarm card is bound.
	 */
	private OnViewHolderBoundListener mOnViewHolderBoundListener;

	/**
	 * Listener for when an alarm card is created.
	 */
	private OnViewHolderCreatedListener mOnViewHolderCreatedListener;

	/**
	 * Indicator that the alarm was added through the floating action button.
	 */
	private boolean mWasAddedWithFloatingActionButton;

	/**
	 */
	public static final DiffUtil.ItemCallback<NacAlarm> DIFF_CALLBACK =
		new DiffUtil.ItemCallback<NacAlarm>() {

			/**
			 */
			@Override
			public boolean areItemsTheSame(@NonNull NacAlarm oldAlarm,
				@NonNull NacAlarm newAlarm)
			{
				NacUtility.printf("Are items same? %d | %d | %b",
					oldAlarm.getId(), newAlarm.getId(), oldAlarm.getId() == newAlarm.getId());
				return oldAlarm.equalsId(newAlarm);
				//return oldAlarm.getId() == newAlarm.getId();
			}

			/**
			 */
			@Override
			public boolean areContentsTheSame(@NonNull NacAlarm oldAlarm,
				@NonNull NacAlarm newAlarm)
			{
				// NOTE: if you use equals, your object must properly override Object#equals()
				// Incorrectly returning false here will result in too many animations.
				NacUtility.printf("Are contents same? %d | %d | %b",
					oldAlarm.getId(), newAlarm.getId(), oldAlarm.equals(newAlarm));
				return oldAlarm.equals(newAlarm);
			}
		};

	/**
	 */
	public NacAlarmCardAdapter()
	{
		super(DIFF_CALLBACK);

		this.mWasAddedWithFloatingActionButton = false;

		setHasStableIds(true);
	}

	///**
	// * @return True if the maximum number of alarms has been created, and False
	// *     otherwise.
	// */
	//public boolean atMaxAlarmCapacity()
	//{
	//	NacSharedConstants cons = this.getSharedConstants();
	//	int size = getItemCount();
	//	//int size = this.size();

	//	return ((size+1) > cons.getMaxAlarms());
	//	//return ((size+1) <= cons.getMaxAlarms());
	//}

	/**
	 * Call the listener for when an alarm card is bound.
	 */
	public void callOnViewHolderBoundListener(NacCardHolder holder, int index)
	{
		OnViewHolderBoundListener listener = this.getOnViewHolderBoundListener();

		if (listener != null)
		{
			listener.onViewHolderBound(holder, index);
		}
	}

	/**
	 * Call the listener for when an alarm card is created.
	 */
	public void callOnViewHolderCreatedListener(NacCardHolder holder)
	{
		OnViewHolderCreatedListener listener = this.getOnViewHolderCreatedListener();

		if (listener != null)
		{
			listener.onViewHolderCreated(holder);
		}
	}

	/**
	 * @return The alarm at the given index.
	 */
	public NacAlarm getAlarmAt(int index)
	{
		return getItem(index);
	}

	/**
	 * Count the number of cards that are collapsed.
	 *
	 * @param  rv  The recyclerview containing the view holders.
	 *
	 * @return Number of cards that are collapsed.
	 */
	public int getCardsCollapsedCount(RecyclerView rv)
	{
		int size = getItemCount();
		int count = 0;

		for (int i=0; i < size; i++)
		{
			NacCardHolder card = (NacCardHolder) rv.findViewHolderForAdapterPosition(i);
			if (card.isCollapsed())
			{
				count++;
			}
		}

		return count;
	}

	/**
	 * Count the number of cards that are expanded.
	 *
	 * @param  rv  The recyclerview containing the view holders.
	 *
	 * @return Number of cards that are expanded.
	 */
	public int getCardsExpandedCount(RecyclerView rv)
	{
		int size = getItemCount();
		int count = 0;

		for (int i=0; i < size; i++)
		{
			NacCardHolder card = (NacCardHolder) rv.findViewHolderForAdapterPosition(i);
			if (card.isExpanded())
			{
				count++;
			}
		}

		return count;
	}

	/**
	 * @return The unique ID of an alarm. Used alongside setHasStableIds().
	 */
	@Override
	public long getItemId(int index)
	{
		NacAlarm alarm = getItem(index);
		return (alarm != null) ? alarm.getId() : RecyclerView.NO_ID;
	}

	/**
	 * @return The listener for when an alarm card is bound.
	 */
	public OnViewHolderBoundListener getOnViewHolderBoundListener()
	{
		return this.mOnViewHolderBoundListener;
	}

	/**
	 * @return The listener for when an alarm card is created.
	 */
	public OnViewHolderCreatedListener getOnViewHolderCreatedListener()
	{
		return this.mOnViewHolderCreatedListener;
	}

	/**
	 * Setup the alarm card.
	 *
	 * @param  card  The alarm card.
	 * @param  index  The position of the alarm card.
	 */
	@Override
	public void onBindViewHolder(final NacCardHolder card, int index)
	{
		NacAlarm alarm = getItem(index);
		NacUtility.printf("onBindViewHolder()! Id: %d | Index: %d", alarm.getId(), index);

		card.init(alarm);

		if (this.wasAddedWithFloatingActionButton())
		{
			card.interact();
		}

		this.setWasAddedWithFloatingActionButton(false);

		this.callOnViewHolderBoundListener(card, index);
	}

	/**
	 * Create the view holder.
	 *
	 * @param  parent  The parent view.
	 * @param  viewType  The type of view.
	 */
	@NonNull
    @Override
	public NacCardHolder onCreateViewHolder(ViewGroup parent, int viewType)
	{
		Context context = parent.getContext();
		LayoutInflater inflater = LayoutInflater.from(context);
		View root = inflater.inflate(R.layout.card_frame, parent, false);
		NacCardHolder card = new NacCardHolder(root);

		this.callOnViewHolderCreatedListener(card);

		return card;
	}

	/**
	 * Set the listener for when an alarm card is bound.
	 */
	public void setOnViewHolderBoundListener(OnViewHolderBoundListener listener)
	{
		this.mOnViewHolderBoundListener = listener;
	}

	/**
	 * Set the listener for when an alarm card is created.
	 */
	public void setOnViewHolderCreatedListener(OnViewHolderCreatedListener listener)
	{
		this.mOnViewHolderCreatedListener = listener;
	}

	/**
	 * Set whether an alarm was added with the floating button.
	 */
	public void setWasAddedWithFloatingActionButton(boolean added)
	{
		this.mWasAddedWithFloatingActionButton = added;
	}

	///**
	// * Toast for when the maximum number of alarms has been created.
	// */
	//public void toastMaxAlarmsError()
	//{
	//	Context context = this.getContext();
	//	NacSharedConstants cons = this.getSharedConstants();

	//	NacUtility.quickToast(context, cons.getErrorMessageMaxAlarms());
	//}

	/**
	 * @return True if the alarm was added, and False otherwise.
	 */
	public boolean wasAddedWithFloatingActionButton()
	{
		return this.mWasAddedWithFloatingActionButton;
	}

}
