package com.nfcalarmclock;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Alarm card adapter.
 */
public class NacCardAdapter
	extends RecyclerView.Adapter<NacCardHolder>
	implements View.OnClickListener,
		NacAlarm.OnChangeListener,
		NacCardHolder.OnDeleteListener,
		NacCardTouchHelper.Adapter
{

	/**
	 * Main activity root view.
	 */
	private CoordinatorLayout mRoot;

	/**
	 * RecyclerView containing list of alarm cards.
	 */
	private RecyclerView mRecyclerView;

	/**
	 * Alarm scheduler.
	 */
	private NacScheduler mScheduler;

	/**
	 * The database.
	 */
	private NacDatabase mDatabase;

	/**
	 * Handle card swipe events.
	 */
	private NacCardTouchHelper mTouchHelper;

	/**
	 * The alarm to restore, when prompted after deletion.
	 */
	private Undo mUndo;

	/**
	 * List of alarms.
	 */
	private List<NacAlarm> mAlarmList;

	/**
	 * Indicator that the alarm was added through the floating action button.
	 */
	private boolean mWasAdded;

	/**
	 */
	public NacCardAdapter(Context context)
	{
		AppCompatActivity activity = (AppCompatActivity) context;
		NacCardTouchHelper.Callback callback =
			new NacCardTouchHelper.Callback(this);

		this.mRoot = (CoordinatorLayout) activity.findViewById(
			R.id.activity_main);
		this.mRecyclerView = (RecyclerView) this.getRoot().findViewById(
			R.id.content_alarm_list);
		this.mTouchHelper = new NacCardTouchHelper(callback);
		this.mUndo = new Undo();
		this.mDatabase = new NacDatabase(context);
		this.mScheduler = new NacScheduler(context);
		this.mAlarmList = null;
		this.mWasAdded = false;
	}

	/**
	 * Add an alarm.
	 */
	public void add()
	{
		if (this.size() >= NacSharedPreferences.DEFAULT_MAX_ALARMS)
		{
			NacUtility.quickToast(this.getRoot(),
				"Max number of alarms created");
			return;
		}

		Context context = this.getContext();
		boolean format = DateFormat.is24HourFormat(context);
		int id = this.getUniqueId();
		NacSharedPreferences shared = new NacSharedPreferences(context);
		NacAlarm alarm = new NacAlarm.Builder()
			.setId(id)
			.setRepeat(shared.getRepeat())
			.setDays(shared.getDays())
			.setVibrate(shared.getVibrate())
			.setSound(shared.getSound())
			.setName(shared.getName())
			.set24HourFormat(format)
			.build();

		this.add(alarm);
	}

	/**
	 * @see add
	 *
	 * @param  alarm  The alarm to add.
	 */
	public void add(NacAlarm alarm)
	{
		int index = this.size();

		this.add(alarm, index);
		this.getRecyclerView().scrollToPosition(index);
	}

	/**
	 * @see add
	 *
	 * @param  alarm  The alarm to add.
	 * @param  position  The position to add the alarm.
	 */
	public void add(NacAlarm alarm, int position)
	{
		// Using update instead of add for testing. Things should never get
		// canceled in update, only added
		this.mWasAdded = true;

		Intent intent = this.getIntent(alarm, "add");

		//this.getDatabase().add(alarm);
		//this.getScheduler().update(alarm);
		this.getContext().startService(intent);
		this.getAlarms().add(position, alarm);
		notifyItemInserted(position);

		//this.refresh(position);
	}

	/**
	 * Build the alarm list.
	 */
	public void build()
	{
		this.mAlarmList = this.getDatabase().read();

		this.getTouchHelper().setRecyclerView(this.getRecyclerView());
		this.getTouchHelper().reset();
		notifyDataSetChanged();
	}

	/**
	 * Copy the alarm.
	 *
	 * @param  pos	The position of the alarm card to copy.
	 */
	public void copy(int position)
	{
		NacAlarm alarm = this.get(position);
		NacAlarm copy = alarm.copy();
		int newPosition = this.size();

		copy.setId(this.getUniqueId());
		notifyItemChanged(position);
		this.add(copy);
		this.undo(copy, newPosition, Undo.Type.COPY);
		NacUtility.snackbar(this.getRoot(), "Copied alarm.", "UNDO", this);
	}

	/**
	 * Delete the alarm at the given position.
	 *
	 * @param  pos	The position of the alarm card to delete.
	 */
	public void delete(int position)
	{
		NacAlarm alarm = this.get(position);
		int firstVisible = this.getFirstVisible(position);
		int lastVisible = this.getLastVisible(position);

		Intent intent = this.getIntent(alarm, "delete");
		//this.getDatabase().delete(alarm);
		//this.getScheduler().cancel(alarm);
		this.getContext().startService(intent);
		this.getAlarms().remove(position);
		notifyItemRemoved(position);

		if ((firstVisible >= 0) && (lastVisible >= 0))
		{
			notifyItemRangeChanged(firstVisible, lastVisible);
			//this.refresh(position);
		}

		this.undo(alarm, position, Undo.Type.DELETE);
		NacUtility.snackbar(this.getRoot(), "Deleted alarm.", "UNDO", this);
	}

	/**
	 * @return The alarm at the given index.
	 */
	public NacAlarm get(int index)
	{
		return this.getAlarms().get(index);
	}

	/**
	 * @return The list of alarms.
	 */
	private List<NacAlarm> getAlarms()
	{
		return this.mAlarmList;
	}

	/**
	 * @return The context.
	 */
	private Context getContext()
	{
		return this.getRoot().getContext();
	}

	/**
	 * @return The alarm database.
	 */
	private NacDatabase getDatabase()
	{
		return this.mDatabase;
	}

	/**
	 * @return The first visible view holder in the recycler view.
	 */
	private int getFirstVisible(int position)
	{
		int size = this.size();

		for (int i=position-1; i >= 0; i--)
		{
			if (this.getRecyclerView().findViewHolderForAdapterPosition(i)
				== null)
			{
				return i+1;
			}
		}

		return 0;
	}

	/**
	 * @return The intent that will be used when starting the service for
	 *         excecuting schedule and database updates.
	 */
	private Intent getIntent(NacAlarm alarm, String message)
	{
		Intent intent = new Intent(this.getContext(),
			NacService.class);
		Bundle bundle = new Bundle();
		NacAlarmParcel parcel = new NacAlarmParcel(alarm);
		Uri uri = Uri.parse(message);

		bundle.putParcelable("parcel", parcel);
		intent.putExtra("bundle", bundle);
		intent.setData(uri);

		return intent;
	}

	/**
	 * @return The number of items in the recycler view.
	 */
	@Override
	public int getItemCount()
	{
		return this.size();
	}

	/**
	 * @return The last visible view holder in the recycler view.
	 */
	private int getLastVisible(int position)
	{
		int size = this.size();

		for (int i=position; i < size; i++)
		{
			if (this.getRecyclerView().findViewHolderForAdapterPosition(i)
				== null)
			{
				return i-1;
			}
		}

		return size-1;
	}

	/**
	 * @return The RecyclerView.
	 */
	private RecyclerView getRecyclerView()
	{
		return this.mRecyclerView;
	}

	/**
	 * @return The root view.
	 */
	private View getRoot()
	{
		return this.mRoot;
	}

	/**
	 * @return The alarm scheduler.
	 */
	private NacScheduler getScheduler()
	{
		return this.mScheduler;
	}

	/**
	 * @return The touch helper.
	 */
	private NacCardTouchHelper getTouchHelper()
	{
		return this.mTouchHelper;
	}

	/**
	 * @return The undo object.
	 */
	private Undo getUndo()
	{
		return this.mUndo;
	}

	/**
	 * Determine a unique integer ID number to use for newly created alarms.
	 */
	private int getUniqueId()
	{
		List<Integer> used = new ArrayList<>();

		for (NacAlarm a : this.getAlarms())
		{
			used.add(a.getId());
		}

		for (int i=1; i < Integer.MAX_VALUE; i+=7)
		{
			if (!used.contains(i))
			{
				return i;
			}
		}

		return -1;
	}

	/**
	 * Refresh cards on screen, around the given position.
	 */
	private void refresh(int position)
	{
		int size = this.size();
		int start = 0;
		int end = size;

		for (int i=0; i < position; i++)
		{
			if (this.getRecyclerView().findViewHolderForAdapterPosition(i)
				!= null)
			{
				start = i;
				break;
			}
		}

		for (int i=position; i < size; i++)
		{
			if (this.getRecyclerView().findViewHolderForAdapterPosition(i)
				== null)
			{
				end = i-1;
				break;
			}
		}

		notifyItemRangeChanged(start, end-start);
	}

	/**
	 * Setup the alarm card.
	 *
	 * @param  card  The alarm card.
	 * @param  position  The position of the alarm card.
	 */
	@Override
	public void onBindViewHolder(final NacCardHolder card, int position)
	{
		NacAlarm alarm = this.get(position);

		alarm.setOnChangeListener(this);
		card.init(alarm, this.mWasAdded);
		card.setOnDeleteListener(this);

		this.mWasAdded = false;
	}

	/**
	 * Update the database when alarm data is changed.
	 *
	 * @param  a  The alarm that was changed.
	 */
	@Override
	public void onChange(NacAlarm alarm)
	{
		Intent intent = this.getIntent(alarm, "change");

		this.getContext().startService(intent);
		//this.getDatabase().update(alarm);
		//this.getScheduler().update(alarm);
	}

	/**
	 * Capture the click event on the delete button, and delete the card it
	 * belongs to.
	 *
	 * @param  v  The view that was clicked.
	 */
	@Override
	public void onClick(View v)
	{
		Undo undo = this.getUndo();
		NacAlarm alarm = undo.getAlarm();
		int position = undo.getPosition();
		Undo.Type type = undo.getType();

		undo.reset();

		if (type == Undo.Type.COPY)
		{
			this.delete(position);
		}
		else if (type == Undo.Type.DELETE)
		{
			this.restore(alarm, position);
		}
		else if (type == Undo.Type.RESTORE)
		{
			this.delete(position);
		}
	}

	/**
	 * Create the view holder.
	 *
	 * @param  parent  The parent view.
	 * @param  viewType  The type of view.
	 */
	@Override
	public NacCardHolder onCreateViewHolder(ViewGroup parent, int viewType)
	{
		Context context = parent.getContext();
		View root = LayoutInflater.from(context).inflate(R.layout.card_frame,
			parent, false);

		return new NacCardHolder(root);
	}

	/**
	 * Delete the alarm.
	 */
	@Override
	public void onDelete(int pos)
	{
		this.delete(pos);
	}

	/**
	 * Copy the alarm.
	 *
	 * @param  pos	The position of the alarm to copy.
	 */
	@Override
	public void onItemCopy(int position)
	{
		//this.mTouchHelper.reset();
		//notifyItemChanged(pos);
		//this.refresh(position);
		this.copy(position);
	}

	/**
	 * Delete the alarm.
	 * 
	 * @param  pos	The position of the alarm to delete.
	 */
	@Override
	public void onItemDelete(int position)
	{
		//notifyItemChanged(pos);
		//this.refresh(position);
		this.delete(position);
	}

	/**
	 * Drag and reorder the alarm.
	 *
	 * @param  from  The position that the alarm is moving from.
	 * @param  to  The position the alarm is moving to.
	 */
	@Override
	public void onItemMove(int fromIndex, int toIndex)
	{
		NacAlarm fromAlarm = this.get(fromIndex);
		NacAlarm toAlarm = this.get(toIndex);

		Collections.swap(this.getAlarms(), fromIndex, toIndex);
		this.getScheduler().cancel(fromAlarm);
		this.getScheduler().cancel(toAlarm);
		this.getDatabase().swap(fromAlarm, toAlarm);
		this.getScheduler().add(fromAlarm);
		this.getScheduler().add(toAlarm);
		notifyItemMoved(fromIndex, toIndex);
	}

	/**
	 * Measure the expanded and collapsed sizes of the alarm card.
	 */
	@Override
	public void onViewAttachedToWindow(NacCardHolder card)
	{
		card.measure();
	}

	/**
	 * Clear any animation that is occuring.
	 */
	@Override
	public void onViewDetachedFromWindow(NacCardHolder card)
	{
		card.unfocus();
	}

	/**
	 * Restore a previously deleted alarm.
	 * 
	 * @param  alarm  The alarm to restore.
	 * @param  position  The position to insert the alarm.
	 */
	public void restore(NacAlarm alarm, int position)
	{
		this.add(alarm, position);
		this.undo(alarm, position, Undo.Type.RESTORE);
		NacUtility.snackbar(this.getRoot(), "Restored alarm.", "UNDO", this);
	}

	/**
	 * @return The number of elements in the adapter.
	 */
	public int size()
	{
		return this.getAlarms().size();
	}

	/**
	 * Save undo parameters.
	 */
	public void undo(NacAlarm alarm, int position, Undo.Type type)
	{
		this.getUndo().set(alarm, position, type);
	}

	/**
	 * Undo an alarm card.
	 */
	public static class Undo
	{

		/**
		 * Type of undo operation.
		 */
		public enum Type
		{
			NONE,
			COPY,
			DELETE,
			RESTORE
		}

		/**
		 * Alarm.
		 */
		public NacAlarm mAlarm;

		/**
		 * Position of the alarm card in the RecyclerView.
		 */
		public int mPosition;

		/**
		 * Type of undo.
		 */
		public Type mType;

		/**
		 */
		public Undo()
		{
			this.reset();
		}

		/**
		 * Reset the member variables.
		 */
		public void reset()
		{
			this.set(null, -1, Type.NONE);
		}

		/**
		 * Set the member variables.
		 *
		 * @param  alarm  The alarm info.
		 * @param  position  Position of the alarm card.
		 * @param  type  Type of undo.
		 */
		public void set(NacAlarm alarm, int position, Type type)
		{
			this.mAlarm = alarm;
			this.mPosition = position;
			this.mType = type;
		}

		/**
		 * @return The alarm.
		 */
		public NacAlarm getAlarm()
		{
			return this.mAlarm;
		}

		/**
		 * @return The position.
		 */
		public int getPosition()
		{
			return this.mPosition;
		}

		/**
		 * @return The undo type.
		 */
		public Type getType()
		{
			return this.mType;
		}

	}

}
