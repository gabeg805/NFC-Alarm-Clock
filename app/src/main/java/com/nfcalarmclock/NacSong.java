package com.nfcalarmclock;

import android.net.Uri;

/**
 * @brief Song structure.
 */
public class NacSong
{
	public String name = "";
	public String dir = "";
	public String ringtone = "";
	public String path = "";

	public NacSong(String n, String d)
	{
		this(n, d, "");
	}

	public NacSong(String n, String d, String r)
	{
		this.name = n;
		this.dir = d;
		this.ringtone = r;
		this.path = this.dir + "/" + this.name;
	}

}
