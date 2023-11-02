package com.nfcalarmclock.shared;

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
	private final Resources mResources;

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
	 * @return An integer.
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
	 * @return A string.
	 */
	public String getString(int id)
	{
		return this.getResources().getString(id);
	}

}
