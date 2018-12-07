package com.nfcalarmclock;

/**
 * Undo an alarm card.
 */
public class NacCardUndo
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
	public NacAlarm alarm;

	/**
	 * Position of the alarm card in the RecyclerView.
	 */
	public int position;

	/**
	 * Type of undo.
	 */
	public Type type;

	/**
	 */
	public NacCardUndo()
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
		this.alarm = alarm;
		this.position = position;
		this.type = type;
	}

}
