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
	 * @return A resource boolean.
	 */
	public boolean getBoolean(int id)
	{
		return this.getResources().getBoolean(id);
	}

	/**
	 * @return A resource integer.
	 */
	public int getInteger(int id)
	{
		return this.getResources().getInteger(id);
	}

	/**
	 * @return The resources.
	 */
	public Resources getResources()
	{
		return this.mResources;
	}

	/**
	 * @return A resource string.
	 */
	public String getString(int id)
	{
		return this.getResources().getString(id);
	}

}
