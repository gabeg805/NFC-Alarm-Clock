package com.nfcalarmclock.db;

import com.nfcalarmclock.NacUtility;
import com.nfcalarmclock.alarm.NacAlarm;

import java.lang.InterruptedException;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.ArrayList;
import java.util.List;

/**
 * Generic repository.
 */
public abstract class NacRepository
{

	/**
	 * Get a NacAlarm from a Future object.
	 *
	 * @param  future  Future object.
	 *
	 * @return A NacAlarm from a Future object.
	 */
	public static NacAlarm getAlarmFromFuture(Future<?> future)
	{
		try
		{
			if (future != null)
			{
				return (NacAlarm) future.get();
			}
		}
		catch (CancellationException | ExecutionException | InterruptedException e)
		{
			NacUtility.printf("AHHHHHHHHHHHHHHHHHHHH GET_ALARM exception!");
			NacUtility.printf("String  : %s!", e.toString());
			NacUtility.printf("Message : %s!", e.getMessage());
			e.printStackTrace();
		}

		return null;
	}

	/**
	 * Get a list of NacAlarm from a Future object.
	 *
	 * @param  future  Future object.
	 *
	 * @return A list of NacAlarm from a Future object.
	 */
	public static List<NacAlarm> getAlarmListFromFuture(Future<?> future)
	{
		try
		{
			if (future != null)
			{
				List<?> list = (List<?>) future.get();
				List<NacAlarm> convlist = new ArrayList<>();

				for (Object o : list)
				{
					convlist.add((NacAlarm)o);
				}

				return convlist;
			}
		}
		catch (CancellationException | ExecutionException | InterruptedException e)
		{
			NacUtility.printf("AHHHHHHHHHHHHHHHHHHHH GET_ALARM_LIST exception!");
			NacUtility.printf("String  : %s!", e.toString());
			NacUtility.printf("Message : %s!", e.getMessage());
			e.printStackTrace();
		}

		return new ArrayList<>();
	}

	/**
	 * Get an integer from a Future object.
	 *
	 * @param  future  Future object.
	 *
	 * @return An integer from a Future object.
	 */
	public static int getIntegerFromFuture(Future<?> future)
	{
		try
		{
			if (future != null)
			{
				return (Integer) future.get();
			}
		}
		catch (CancellationException | ExecutionException | InterruptedException e)
		{
			NacUtility.printf("AHHHHHHHHHHHHHHHHHHHH GET_INT exception!");
			NacUtility.printf("String  : %s!", e.toString());
			NacUtility.printf("Message : %s!", e.getMessage());
			e.printStackTrace();
		}

		return -1;
	}

	/**
	 * Get a long from a Future object.
	 *
	 * @param  future  Future object.
	 *
	 * @return A long from a Future object.
	 */
	public static long getLongFromFuture(Future<?> future)
	{
		try
		{
			if (future != null)
			{
				return (Long) future.get();
			}
		}
		catch (CancellationException | ExecutionException | InterruptedException e)
		{
			NacUtility.printf("AHHHHHHHHHHHHHHHHHHHH GET_LONG exception!");
			NacUtility.printf("String  : %s!", e.toString());
			NacUtility.printf("Message : %s!", e.getMessage());
			e.printStackTrace();
		}

		return -1;
	}

}
