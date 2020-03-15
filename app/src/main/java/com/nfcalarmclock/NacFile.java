package com.nfcalarmclock;

import android.net.Uri;

/**
 */
public class NacFile
{

	/**
	 * File type.
	 */
	public enum Type
	{
		NONE,
		DIRECTORY,
		FILE
	}

	/**
	 * The directory path.
	 */
	private String mPath;

	/**
	 * The file name.
	 */
	private String mName;

	/**
	 * Content Uri.
	 */
	private Uri mUri;

	/**
	 * Extra data.
	 */
	private Object mData;

	/**
	 * File type.
	 */
	private Type mType;

	/**
	 */
	public NacFile()
	{
		//this.mPath
	}

}
