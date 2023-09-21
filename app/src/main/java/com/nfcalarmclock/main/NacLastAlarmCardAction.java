package com.nfcalarmclock.main;

import com.nfcalarmclock.alarm.db.NacAlarm;

/**
 * The last action (Copy, Delete, Restore) that was executed.
 */
public class NacLastAlarmCardAction
{

	/**
	 * Type of action.
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
	private NacAlarm mAlarm;

	/**
	 * Index of the alarm in the adapter.
	 */
	private int mIndex;

	/**
	 * Type of action.
	 */
	private Type mType;

	/**
	 */
	public NacLastAlarmCardAction()
	{
		this.mAlarm = null;
		this.mIndex = -1;
		this.mType = Type.NONE;
	}

	/**
	 * @return The alarm.
	 */
	public NacAlarm getAlarm()
	{
		return this.mAlarm;
	}

	/**
	 * @return The index of the alarm in the adapter.
	 */
	public int getIndex()
	{
		return this.mIndex;
	}

	/**
	 * @return The type of action.
	 */
	public Type getType()
	{
		return this.mType;
	}

	/**
	 * Set the last action data.
	 */
	public void set(NacAlarm alarm, Type type)
	{
		this.setAlarm(alarm);
		this.setType(type);
	}

	/**
	 * Set the last action data.
	 */
	public void set(NacAlarm alarm, int index, Type type)
	{
		this.setAlarm(alarm);
		this.setIndex(index);
		this.setType(type);
	}

	/**
	 * Set the alarm.
	 */
	public void setAlarm(NacAlarm alarm)
	{
		this.mAlarm = alarm;
	}

	/**
	 * Set the index.
	 */
	public void setIndex(int index)
	{
		this.mIndex = index;
	}

	/**
	 * Set the type.
	 */
	public void setType(Type type)
	{
		this.mType = type;
	}

	/**
	 * @return True if the action was NONE, and False otherwise.
	 */
	public boolean wasNone()
	{
		return this.getType() == Type.NONE;
	}

	/**
	 * @return True if the last action was COPY, and False otherwise.
	 */
	public boolean wasCopy()
	{
		return this.getType() == Type.COPY;
	}

	/**
	 * @return True if the last action was DELETE, and False otherwise.
	 */
	public boolean wasDelete()
	{
		return this.getType() == Type.DELETE;
	}

	/**
	 * @return True if the last action was RESTORE, and False otherwise.
	 */
	public boolean wasRestore()
	{
		return this.getType() == Type.RESTORE;
	}

}
