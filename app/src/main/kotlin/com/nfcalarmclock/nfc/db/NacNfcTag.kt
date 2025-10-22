package com.nfcalarmclock.nfc.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * An NFC tag.
 */
@Entity(
	tableName = "nfc_tag",
	indices = [Index(value=["nfc_id"], unique=true)]
)
class NacNfcTag()
{

	/**
	 * Unique ID.
	 */
	@PrimaryKey(autoGenerate = true)
	@ColumnInfo(name = "id")
	var id: Long = 0

	/**
	 * Name of the NFC tag.
	 */
	@ColumnInfo(name = "name")
	var name: String = ""

	/**
	 * ID of the NFC tag.
	 */
	@ColumnInfo(name = "nfc_id")
	var nfcId: String = ""

	/**
	 * Constructor.
	 */
	constructor(nfcTagName: String, nfcTagId: String) : this()
	{
		name = nfcTagName
		nfcId = nfcTagId
	}

	/**
	 * Check if two tags are equal, except for the ID.
	 */
	fun equalsExceptId(tag: NacNfcTag): Boolean
	{
		return (name == tag.name)
			&& (nfcId == tag.nfcId)
	}

}