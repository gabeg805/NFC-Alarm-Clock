package com.nfcalarmclock;

/**
 * @brief Song structure.
 */
public class NacSound
{
	public String path = "";
	public String name = "";

	public NacSound(String n, String d)
	{
		this(n, d, "");
	}

	public NacSound(String n, String d, String r)
	{
		this.path = d+"/"+n;
		this.name = n;

		if (!r.isEmpty())
		{
			this.name = r;
		}
	}

}
