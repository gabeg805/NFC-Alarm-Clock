package com.nfcalarmclock.settings.importexport

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.button.MaterialButton
import com.nfcalarmclock.R
import com.nfcalarmclock.view.dialog.NacBottomSheetDialogFragment

class NacImportExportDialog
	: NacBottomSheetDialogFragment()
{

	/**
	 * Listener for importing the database and shared preferences.
	 */
	fun interface OnImportListener
	{
		fun onImport()
	}

	/**
	 * Listener for exporting the database and shared preferences.
	 */
	fun interface OnExportListener
	{
		fun onExport()
	}

	/**
	 * Listener for importing the database and shared preferences.
	 */
	var onImportListener: OnImportListener? = null

	/**
	 * Listener for exporting the database and shared preferences.
	 */
	var onExportListener: OnExportListener? = null

	/**
	 * Called when the dialog view is created.
	 */
	override fun onCreateView(inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?): View?
	{
		return inflater.inflate(R.layout.dlg_import_export, container, false)
	}

	/**
	 * Called when the dialog view is created.
	 */
	override fun onViewCreated(view: View, savedInstanceState: Bundle?)
	{
		// Super
		super.onViewCreated(view, savedInstanceState)

		// Get the views
		val exportButton = view.findViewById(R.id.export_data) as MaterialButton
		val importButton = view.findViewById(R.id.import_data) as MaterialButton

		// Setup the export button
		setupPrimaryButton(exportButton, listener = {

			// Call the listener
			onExportListener?.onExport()

			// Dismiss the dialog
			dismiss()

		})

		// Setup the import button
		setupSecondaryButton(importButton, listener = {

			// Call the listener
			onImportListener?.onImport()

			// Dismiss the dialog
			dismiss()

		})

	}

	companion object
	{

		/**
		 * Tag for the class.
		 */
		const val TAG = "NacImportExportDialog"

	}

}