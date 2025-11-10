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
	 * Text containing either the name or the ID of the NFC tag, in the event that the
	 * name is empty.
	 */
	val text: String
		get() = name.takeIf { it.isNotEmpty() } ?: nfcId

	/**
	 * Whether the NFC tag is empty or not.
	 *
	 * This is when the name and NFC ID have not been set.
	 */
	val isEmpty: Boolean
		get() = name.isEmpty() && nfcId.isEmpty()

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

	/**
	 * Text containing either the name or the ID of the NFC tag, in the event that the
	 * name is empty.
	 *
	 * If the ID is returned, can optionally have a prefix in front of it.
	 *
	 * @return Text containing either the name or the ID of the NFC tag, in the event
	 *         that the name is empty.
	 */
	fun getTextWithPrefix(prefix: String): String
	{
		return name.takeIf { it.isNotEmpty() } ?: "$prefix$nfcId"
	}

}