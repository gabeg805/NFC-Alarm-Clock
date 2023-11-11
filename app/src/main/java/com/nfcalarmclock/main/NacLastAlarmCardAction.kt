package com.nfcalarmclock.main

import com.nfcalarmclock.alarm.db.NacAlarm

/**
 * The last action (Copy, Delete, Restore) that was executed.
 */
class NacLastAlarmCardAction
{

	/**
	 * Type of action.
	 */
	enum class Type
	{
		NONE,
		COPY,
		DELETE,
		RESTORE
	}

	/**
	 * Alarm.
	 */
	var alarm: NacAlarm? = null

	/**
	 * Index of the alarm in the adapter.
	 */
	var index: Int

	/**
	 * Type of action.
	 */
	var type: Type

	/**
	 * Constructor.
	 */
	init
	{
		index = -1
		type = Type.NONE
	}

	/**
	 * Set the last action data.
	 */
	operator fun set(alarm: NacAlarm?, type: Type)
	{
		this.alarm = alarm
		this.type = type
	}

	/**
	 * Set the last action data.
	 */
	operator fun set(alarm: NacAlarm?, index: Int, type: Type)
	{
		this.alarm = alarm
		this.index = index
		this.type = type
	}

	/**
	 * Check if the action was NONE.
	 *
	 * @return True if the action was NONE, and False otherwise.
	 */
	fun wasNone(): Boolean
	{
		return type == Type.NONE
	}

	/**
	 * Check if the last action was COPY.
	 *
	 * @return True if the last action was COPY, and False otherwise.
	 */
	fun wasCopy(): Boolean
	{
		return type == Type.COPY
	}

	/**
	 * Check if the last action was DELETE.
	 *
	 * @return True if the last action was DELETE, and False otherwise.
	 */
	fun wasDelete(): Boolean
	{
		return type == Type.DELETE
	}

	/**
	 * Check if the last action was RESTORE.
	 * @return True if the last action was RESTORE, and False otherwise.
	 */
	fun wasRestore(): Boolean
	{
		return type == Type.RESTORE
	}

}