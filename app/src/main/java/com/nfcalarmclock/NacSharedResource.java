package com.nfcalarmclock;

import android.content.Context;
import android.content.res.Resources;

/**
 * Resource container.
 */
public class NacSharedResource
{

	/**
	 * Resource.
	 */
	private Resources mResources;

	/**
	 */
	public NacSharedResource(Context context)
	{
		this(context.getResources());
	}

	/**
	 */
	public NacSharedResource(Resources res)
	{
		this.mResources = res;
	}

	/**
	 * @return A boolean.
	 */
	public boolean getBoolean(int id)
	{
		return this.getResources().getBoolean(id);
	}

	/**
	 * @return An integer.
	 */
	public int getInteger(int id)
	{
		return this.getResources().getInteger(id);
	}

	/**
	 * @return A plural string.
	 */
	public String getPluralString(int id, int quantity)
	{
		return this.getResources().getQuantityString(id, quantity);
	}

	/**
	 * @return The resources.
	 */
	public Resources getResources()
	{
		return this.mResources;
	}

	/**
	 * @return A string.
	 */
	public String getString(int id)
	{
		return this.getResources().getString(id);
	}

}
