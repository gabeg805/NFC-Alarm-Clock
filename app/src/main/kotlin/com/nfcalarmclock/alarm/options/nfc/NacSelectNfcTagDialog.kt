package com.nfcalarmclock.alarm.options.nfc

import android.view.View
import android.view.ViewGroup.OnHierarchyChangeListener
import android.widget.AdapterView
import android.widget.LinearLayout
import androidx.core.view.children
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.MaterialAutoCompleteTextView
import com.google.android.material.textfield.TextInputLayout
import com.nfcalarmclock.R
import com.nfcalarmclock.alarm.db.NacAlarm
import com.nfcalarmclock.alarm.options.NacGenericAlarmOptionsDialog
import com.nfcalarmclock.alarm.options.nfc.db.NacNfcTag
import com.nfcalarmclock.view.setupInputLayoutColor
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

/**
 * Select an NFC tag that has been previously saved.
 */
@AndroidEntryPoint
class NacSelectNfcTagDialog
	: NacGenericAlarmOptionsDialog()
{

	/**
	 * Layout resource ID.
	 */
	override val layoutId = R.layout.dlg_select_nfc_tag

	/**
	 * Container for all NFC tag dropdowns and add/remove buttons.
	 */
	private lateinit var container: LinearLayout

	/**
	 * NFC tag view model.
	 */
	private val nfcTagViewModel: NacNfcTagViewModel by viewModels()

	/**
	 * List of NFC tags.
	 */
	private var allNfcTags: List<NacNfcTag> = emptyList()

	/**
	 * Name of the selected NFC tag.
	 */
	private var selectedNfcTags: MutableList<NacNfcTag> = mutableListOf()

	/**
	 * Unused (unselected) NFC tag names and is updated as the selected NFC tags list is
	 * updated.
	 */
	private val unusedNfcTagNames: List<String>
		get() = allNfcTags.filter { !selectedNfcTags.contains(it) }
			.map { it.name }

	/**
	 * Update the alarm with selected options.
	 */
	override fun onOkClicked(alarm: NacAlarm?)
	{
		alarm?.nfcTagId = selectedNfcTags.joinToString(" || ") { it.nfcId }
	}

	/**
	 * Set add button usability.
	 */
	private fun setAddButtonUsability(button: MaterialButton)
	{
		setButtonUsability(button, (container.childCount == allNfcTags.size))
	}

	/**
	 * Set add button usability for all add buttons.
	 */
	private fun setAllAddButtonUsability()
	{
		container.children.forEach { root ->
			val addButton: MaterialButton = root.findViewById(R.id.add_nfc_tag)
			setAddButtonUsability(addButton)
		}
	}

	/**
	 * Set the dropdown items for all dropdowns.
	 */
	private fun setAllDropdownItems()
	{
		// Get the list of unused NFC tag names
		val unused = unusedNfcTagNames.toTypedArray()

		// Update each dropdown
		container.children.forEach { root ->
			val textView: MaterialAutoCompleteTextView = root.findViewById(R.id.nfc_tag_dropdown_menu)
			textView.setSimpleItems(unused)
		}
	}

	/**
	 * Set button usability.
	 */
	private fun setButtonUsability(button: MaterialButton, notState: Boolean)
	{
		// Not usable
		if (notState)
		{
			button.alpha = 0.25f
			button.isEnabled = false
		}
		// Usable
		else
		{
			button.alpha = 1f
			button.isEnabled = true
		}
	}

	/**
	 * Set remove button usability.
	 */
	private fun setRemoveButtonUsability(button: MaterialButton)
	{
		setButtonUsability(button, (container.childCount == 1))
	}

	/**
	 * Setup all alarm options.
	 */
	override fun setupAlarmOptions(alarm: NacAlarm?)
	{
		// Get the container for all the NFC tags
		container = dialog!!.findViewById(R.id.nfc_tag_container)

		// Set the listener for when children are added/removed
		container.setOnHierarchyChangeListener(object: OnHierarchyChangeListener
		{

			/**
			 * Called when a child is added.
			 */
			override fun onChildViewAdded(parent: View, child: View)
			{
				// Set the usability of all add buttons
				setAllAddButtonUsability()
			}

			/**
			 * Called when a child is reomved.
			 */
			override fun onChildViewRemoved(parent: View, child: View)
			{
				// Set the usability of all add buttons
				setAllAddButtonUsability()
			}

		})

		lifecycleScope.launch {

			// Get the alarm NFC IDs and all the NFC tags
			val alarmNfcTagIdList = alarm?.nfcTagIdList ?: emptyList()
			allNfcTags = nfcTagViewModel.getAllNfcTags()

			// Set the selected NFC tags based on the alarm
			selectedNfcTags = alarmNfcTagIdList
				.mapNotNull { id ->
					allNfcTags.find { it.nfcId == id }
				}
				.toMutableList()

			// If not tags have been selected for this alarm, then default to selecting
			// the first NFC tag in the list
			if (selectedNfcTags.isEmpty())
			{
				selectedNfcTags.add(allNfcTags[0])
			}

			// Set the list of items in the textview dropdown menu
			val nfcTagNames = allNfcTags.map { it.name }.toTypedArray()

			// Setup the input layout and textview
			selectedNfcTags.forEach {
				setupInputLayoutAndTextView(it.name, nfcTagNames)
			}

		}
	}

	/**
	 * Setup the input layout and textview.
	 */
	private fun setupInputLayoutAndTextView(defaultName: String, nfcTagNames: Array<String>)
	{
		// Inflate the layout and add it to the container
		val root = dialog!!.layoutInflater.inflate(R.layout.nac_select_add_remove_nfc_tag,
			container, false)

		container.addView(root)

		// Get the views
		val inputLayout: TextInputLayout = root.findViewById(R.id.nfc_tag_input_layout)
		val textView: MaterialAutoCompleteTextView = root.findViewById(R.id.nfc_tag_dropdown_menu)
		val addButton: MaterialButton = root.findViewById(R.id.add_nfc_tag)
		val removeButton: MaterialButton = root.findViewById(R.id.remove_nfc_tag)

		// Setup the views
		inputLayout.setupInputLayoutColor(requireContext(), sharedPreferences)
		setAddButtonUsability(addButton)
		setRemoveButtonUsability(removeButton)

		// Set the items in the dropdown list
		textView.setSimpleItems(nfcTagNames)
		textView.setText(defaultName, false)

		// Dropdown item click listener
		textView.onItemClickListener = AdapterView.OnItemClickListener { _, _, _, _ ->

			// Get the name of the selected NFC tag and the index of this view in
			// relation to the container. This will help determine the correct index in
			// the selected NFC tags list
			val name = textView.text.toString()
			val index = container.indexOfChild(root)

			// Find the matching tag in the list of all NFC tags and add/replace it in
			// the selected NFC tags list
			allNfcTags.firstOrNull { it.name == name }
				?.let { match ->

					// Add the selected NFC tag
					if (index+1 > selectedNfcTags.size)
					{
						selectedNfcTags.add(match)
					}
					// Replace the selected NFC tag
					else
					{
						selectedNfcTags[index] = match
					}

				}

			// Update the dropdown items
			setAllDropdownItems()
		}

		// Add button click listener
		addButton.setOnClickListener {

			// Find the first NFC tag that is not being used
			val unusedName = unusedNfcTagNames.firstOrNull()
				?.also { name ->

					// Add the unused tag to the selected NFC tags
					allNfcTags.firstOrNull { it.name == name }
						?.let { selectedNfcTags.add(it) }

				}

			// Add a new NFC tag and update all the dropdown items
			setupInputLayoutAndTextView(unusedName ?: nfcTagNames[0], nfcTagNames)
			setAllDropdownItems()
		}

		// Remove button click listener
		removeButton.setOnClickListener {

			// Remove the view
			container.removeView(root)

			// Remove the tag from the list
			selectedNfcTags.indexOfFirst { it.name == textView.text.toString() }
				.takeIf { it >= 0 }
				?.let { index ->
					selectedNfcTags.removeAt(index)
				}

			// Update all the dropdown items
			setAllDropdownItems()
		}
	}

}