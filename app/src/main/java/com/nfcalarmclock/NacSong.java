package com.nfcalarmclock;

import android.net.Uri;

/**
 * @brief Song structure.
 */
public class NacSong
{
	public String name = "";
	public String dir = "";
	public Uri uri = null;

	public NacSong(String n, String d)
	{
		this(n, d, null);
	}

	public NacSong(String n, String d, Uri u)
	{
		this.name = n;
		this.dir = d;
		this.uri = u;
	}

}
