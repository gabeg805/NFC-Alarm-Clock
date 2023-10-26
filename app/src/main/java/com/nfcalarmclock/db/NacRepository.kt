package com.nfcalarmclock.db

import com.nfcalarmclock.alarm.db.NacAlarm
import com.nfcalarmclock.util.NacUtility
import java.util.concurrent.CancellationException
import java.util.concurrent.ExecutionException
import java.util.concurrent.Future

/**
 * Generic repository.
 */
abstract class NacRepository
{

	companion object
	{

		/**
		 * Get a NacAlarm from a Future object.
		 *
		 * @param  future  Future object.
		 *
		 * @return A NacAlarm from a Future object.
		 */
		fun getAlarmFromFuture(future: Future<*>?): NacAlarm?
		{
			try
			{
				if (future != null)
				{
					return future.get() as NacAlarm
				}
			}
			catch (e: CancellationException)
			{
				NacUtility.printf("AHHHHHHHHHHHHHHHHHHHH GET_ALARM exception!")
				NacUtility.printf("String  : %s!", e.toString())
				NacUtility.printf("Message : %s!", e.message)
				e.printStackTrace()
			}
			catch (e: ExecutionException)
			{
				NacUtility.printf("AHHHHHHHHHHHHHHHHHHHH GET_ALARM exception!")
				NacUtility.printf("String  : %s!", e.toString())
				NacUtility.printf("Message : %s!", e.message)
				e.printStackTrace()
			}
			catch (e: InterruptedException)
			{
				NacUtility.printf("AHHHHHHHHHHHHHHHHHHHH GET_ALARM exception!")
				NacUtility.printf("String  : %s!", e.toString())
				NacUtility.printf("Message : %s!", e.message)
				e.printStackTrace()
			}

			return null
		}

		/**
		 * Get a list of NacAlarm from a Future object.
		 *
		 * @param  future  Future object.
		 *
		 * @return A list of NacAlarm from a Future object.
		 */
		fun getAlarmListFromFuture(future: Future<*>?): List<NacAlarm>
		{
			try
			{
				if (future != null)
				{
					val list = future.get() as List<*>
					val convlist: MutableList<NacAlarm> = ArrayList()
					for (o in list)
					{
						convlist.add(o as NacAlarm)
					}
					return convlist
				}
			}
			catch (e: CancellationException)
			{
				NacUtility.printf("AHHHHHHHHHHHHHHHHHHHH GET_ALARM_LIST exception!")
				NacUtility.printf("String  : %s!", e.toString())
				NacUtility.printf("Message : %s!", e.message)
				e.printStackTrace()
			}
			catch (e: ExecutionException)
			{
				NacUtility.printf("AHHHHHHHHHHHHHHHHHHHH GET_ALARM_LIST exception!")
				NacUtility.printf("String  : %s!", e.toString())
				NacUtility.printf("Message : %s!", e.message)
				e.printStackTrace()
			}
			catch (e: InterruptedException)
			{
				NacUtility.printf("AHHHHHHHHHHHHHHHHHHHH GET_ALARM_LIST exception!")
				NacUtility.printf("String  : %s!", e.toString())
				NacUtility.printf("Message : %s!", e.message)
				e.printStackTrace()
			}

			return ArrayList()
		}

		/**
		 * Get an integer from a Future object.
		 *
		 * @param  future  Future object.
		 *
		 * @return An integer from a Future object.
		 */
		fun getIntegerFromFuture(future: Future<*>?): Int
		{
			try
			{
				if (future != null)
				{
					return future.get() as Int
				}
			}
			catch (e: CancellationException)
			{
				NacUtility.printf("AHHHHHHHHHHHHHHHHHHHH GET_INT exception!")
				NacUtility.printf("String  : %s!", e.toString())
				NacUtility.printf("Message : %s!", e.message)
				e.printStackTrace()
			}
			catch (e: ExecutionException)
			{
				NacUtility.printf("AHHHHHHHHHHHHHHHHHHHH GET_INT exception!")
				NacUtility.printf("String  : %s!", e.toString())
				NacUtility.printf("Message : %s!", e.message)
				e.printStackTrace()
			}
			catch (e: InterruptedException)
			{
				NacUtility.printf("AHHHHHHHHHHHHHHHHHHHH GET_INT exception!")
				NacUtility.printf("String  : %s!", e.toString())
				NacUtility.printf("Message : %s!", e.message)
				e.printStackTrace()
			}

			return -1
		}

		/**
		 * Get a long from a Future object.
		 *
		 * @param  future  Future object.
		 *
		 * @return A long from a Future object.
		 */
		fun getLongFromFuture(future: Future<*>?): Long
		{
			try
			{
				if (future != null)
				{
					return future.get() as Long
				}
			}
			catch (e: CancellationException)
			{
				NacUtility.printf("AHHHHHHHHHHHHHHHHHHHH GET_LONG exception!")
				NacUtility.printf("String  : %s!", e.toString())
				NacUtility.printf("Message : %s!", e.message)
				e.printStackTrace()
			}
			catch (e: ExecutionException)
			{
				NacUtility.printf("AHHHHHHHHHHHHHHHHHHHH GET_LONG exception!")
				NacUtility.printf("String  : %s!", e.toString())
				NacUtility.printf("Message : %s!", e.message)
				e.printStackTrace()
			}
			catch (e: InterruptedException)
			{
				NacUtility.printf("AHHHHHHHHHHHHHHHHHHHH GET_LONG exception!")
				NacUtility.printf("String  : %s!", e.toString())
				NacUtility.printf("Message : %s!", e.message)
				e.printStackTrace()
			}

			return -1
		}

	}

}