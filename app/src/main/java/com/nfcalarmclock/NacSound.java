package com.nfcalarmclock;

/**
 * Song structure.
 */
public class NacSound
{

	/**
	 * Path to the song.
	 */
	public String path = "";

	/**
	 * Name of the song.
	 */
	public String name = "";

	/**
	 */
	public NacSound(String p, String n)
	{
		this.path = p;
		this.name = n;
	}

	/**
	 */
	public NacSound(String d, String n, String r)
	{
		this.path = d+"/"+n;
		this.name = n;

		if (!r.isEmpty())
		{
			this.name = r;
		}
	}

	/**
	 * Contains path of song.
	 */
	public boolean containsPath(String path)
	{
		return this.path.equals(path);
	}

	/**
	 * Contains name of song.
	 */
	public boolean containsName(String name)
	{
		return this.name.equals(name);
	}

}
