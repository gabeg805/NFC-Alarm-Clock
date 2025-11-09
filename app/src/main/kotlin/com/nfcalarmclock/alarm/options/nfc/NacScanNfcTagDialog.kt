package com.nfcalarmclock.alarm.options.nfc

import android.nfc.NfcAdapter
import android.nfc.Tag
import android.view.View
import android.view.ViewGroup.OnHierarchyChangeListener
import android.widget.AdapterView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.Space
import android.widget.TextView
import androidx.appcompat.widget.SwitchCompat
import androidx.core.view.children
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavDestination
import androidx.navigation.fragment.findNavController
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.MaterialAutoCompleteTextView
import com.google.android.material.textfield.TextInputLayout
import com.nfcalarmclock.R
import com.nfcalarmclock.alarm.db.NacAlarm
import com.nfcalarmclock.alarm.options.NacGenericAlarmOptionsDialog
import com.nfcalarmclock.nfc.NacNfc
import com.nfcalarmclock.nfc.NacNfcReaderMode
import com.nfcalarmclock.nfc.NacNfcTagViewModel
import com.nfcalarmclock.nfc.SCANNED_NFC_TAG_ALREADY_EXISTS_BUNDLE_NAME
import com.nfcalarmclock.nfc.SCANNED_NFC_TAG_ID_BUNDLE_NAME
import com.nfcalarmclock.nfc.db.NacNfcTag
import com.nfcalarmclock.nfc.setNfcTagIds
import com.nfcalarmclock.system.navigate
import com.nfcalarmclock.view.calcAlpha
import com.nfcalarmclock.view.quickToast
import com.nfcalarmclock.view.setTextFromIndex
import com.nfcalarmclock.view.setupInputLayoutColor
import com.nfcalarmclock.view.setupSwitchColor
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

/**
 * Check if NFC tag matches a name.
 *
 * This is unique to scanning an NFC tag where an NFC tag can be scanned and saved to an
 * alarm/timer, but not saved to the table in the database. It is still valid though and
 * should be matched correctly.
 *
 * @return True if NFC tag matches a name, and False otherwise.
 */
fun NacNfcTag.matchesName(name: String): Boolean
{
	return this.name == name || this.nfcId == name
}

/**
 * Scan an NFC tag that will be used to dismiss the given alarm when it goes
 * off.
 */
@AndroidEntryPoint
open class NacScanNfcTagDialog
	: NacGenericAlarmOptionsDialog(),
	NfcAdapter.ReaderCallback
{

	/**
	 * NFC tag view model.
	 */
	private val nfcTagViewModel: NacNfcTagViewModel by viewModels()

	/**
	 * Layout resource ID.
	 */
	override val layoutId = R.layout.dlg_scan_nfc_tag

	/**
	 * Max scroll view height as a percentage of the total height of the screen.
	 */
	override val maxScrollViewHeightPercent: Int = 75

	/**
	 * Select NFC tag description.
	 */
	private val selectNfcTagDescription: TextView by lazy {
		requireView().findViewById(R.id.select_nfc_tag_description)
	}

	/**
	 * Container for all NFC tag dropdowns and add/remove buttons.
	 */
	private val selectNfcTagListContainer: LinearLayout by lazy {
		requireView().findViewById(R.id.nfc_tag_container)
	}

	/**
	 * Container for the dismiss order
	 */
	private val dismissOrderContainer: RelativeLayout by lazy {
		requireView().findViewById(R.id.nfc_tag_dismiss_order_container)
	}

	/**
	 * Dismiss order switch.
	 */
	private val dismissOrderSwitch: SwitchCompat by lazy {
		requireView().findViewById(R.id.nfc_tag_dismiss_order_switch)
	}

	/**
	 * Input layout (dropdown) for the dismiss order.
	 */
	private val dismissOrderInputLayout: TextInputLayout by lazy {
		requireView().findViewById(R.id.nfc_tag_dismiss_order_input_layout)
	}

	/**
	 * List of NFC tags.
	 */
	private var allNfcTags: List<NacNfcTag> = emptyList()

	/**
	 * Name of the selected NFC tag.
	 */
	private var selectedNfcTags: MutableList<NacNfcTag> = mutableListOf()

	/**
	 * Selected dismiss order.
	 */
	private var selectedDismissOrder: Int = 0

	/**
	 * Currently selected NFC tag IDs.
	 */
	private var currentlySelectedNfcTagIds: List<String> = emptyList()

	/**
	 * Unused (unselected) NFC tag names and is updated as the selected NFC tags list is
	 * updated.
	 */
	private val unusedNfcTagNames: List<String>
		get() = allNfcTags.filter { !selectedNfcTags.contains(it) }
			.map { it.text }

	/**
	 * The visibility of the select NFC tag and dismiss order views (as an example).
	 */
	private val viewVisibility: Int
		get() = if (allNfcTags.isNotEmpty()) View.VISIBLE else View.GONE

	/**
	 * Get the navigation destination ID for the Save NFC Tag dialog.
	 *
	 * @return The navigation destination ID for the Save NFC Tag dialog.
	 */
	open fun getSaveNfcTagDialogId(currentDestination: NavDestination?): Int
	{
		// Normal option
		return if (currentDestination?.id == R.id.nacScanNfcTagDialog)
		{
			R.id.nacSaveNfcTagDialog
		}
		// Quick option
		else
		{
			R.id.nacSaveNfcTagDialog2
		}
	}

	/**
	 * Get the navigation destination ID for the Select NFC Tag dialog.
	 *
	 * @return The navigation destination ID for the Select NFC Tag dialog.
	 */
	open fun getSelectNfcTagDialogId(currentDestination: NavDestination?): Int
	{
		// Normal option
		return if (currentDestination?.id == R.id.nacScanNfcTagDialog)
		{
			R.id.nacSelectNfcTagDialog
		}
		// Quick option
		else
		{
			R.id.nacSelectNfcTagDialog2
		}
	}

	/**
	 * OK buton is clicked.
	 */
	override fun onOkClicked(alarm: NacAlarm?)
	{
		// TODO: Added from select nfc
		selectedNfcTags.filter { it.text.isNotEmpty() }.forEach { println("USING THESE : ${it.text}") }

		alarm?.setNfcTagIds(selectedNfcTags.filter { it.text.isNotEmpty() })
		alarm?.shouldUseNfcTagDismissOrder = dismissOrderSwitch.isChecked
		alarm?.nfcTagDismissOrder = NacAlarm.calcNfcTagDismissOrderFromIndex(selectedDismissOrder)
	}

	/**
	 * Fragment is started.
	 */
	override fun onStart()
	{
		// Super
		super.onStart()

		// Check if NFC is not on the device or not enabled
		if (!NacNfc.isEnabled(activity))
		{
			return
		}

		// Enable NFC reader mode
		NacNfc.disableReaderMode(requireActivity())
		NacNfc.enableReaderMode(requireActivity(), this)
		NacNfcReaderMode.update(true)
	}

	/**
	 * Fragment is stopped.
	 */
	override fun onStop()
	{
		// Super
		super.onStop()

		// Check if NFC is not on the device or not enabled
		if (!NacNfc.isEnabled(activity))
		{
			return
		}

		// Disable NFC reader mode
		NacNfc.disableReaderMode(requireActivity())
		NacNfcReaderMode.update(false)
	}

	/**
	 * NFC tag is discovered.
	 */
	override fun onTagDiscovered(tag: Tag?)
	{
		// Get the current destination ID
		val context = requireContext()
		val currentDestinationId = findNavController().currentDestination?.id

		// Destination is not one of the valid expected destinations
		if ((currentDestinationId != R.id.nacScanNfcTagDialog)
			&& (currentDestinationId != R.id.nacScanNfcTagDialog2)
			&& (currentDestinationId != R.id.nacScanNfcTagDialog3))
		{
			// Do nothing
			return
		}

		// Get the fragment item and parse the ID from the NFC tag
		val item = getFragmentArgument()
		val nfcId = NacNfc.parseId(tag)

		// Unable to parse the NFC ID
		if (nfcId == null)
		{
			quickToast(context, R.string.error_message_unable_to_parse_nfc_id)
			return
		}
		// The NFC ID is already being used by the alarm/timer
		else if (item!!.nfcTagIdList.contains(nfcId))
		{
			quickToast(context, R.string.error_message_nfc_id_exists_new)
			return
		}

		lifecycleScope.launch {

			// Get if the NFC tag already exists
			val doesNfcTagAlreadyExist = (nfcTagViewModel.findNfcTag(nfcId) != null)

			// Set the current options on the item
			onOkClicked(item)

			// TODO: When NFC tag already exists and this alarm has no NFC tags set on it,
			//  do not need to do all this. It should automatically be added

			// Alarm/timer has no NFC tag set and the NFC tag already exists, so no need
			// to save. Add the NFC tag to the alarm/timer and dismiss the dialog
			if (item.nfcTagId.isEmpty() && doesNfcTagAlreadyExist)
			{
				println("SAVING STRAIGHT AWAY! $nfcId")
				item.nfcTagId = nfcId
				onSaveAlarm(item)
				dismiss()
				return@launch
			}

			// Prepare to navigate to save the NFC tag
			val navController = findNavController()
			val destinationId = getSaveNfcTagDialogId(navController.currentDestination)
			val newArgs = addFragmentArgument(item)
				.apply {
					putString(SCANNED_NFC_TAG_ID_BUNDLE_NAME, nfcId)
					putBoolean(SCANNED_NFC_TAG_ALREADY_EXISTS_BUNDLE_NAME, doesNfcTagAlreadyExist)
				}

			println("Putting NFC tag in the bundle : $nfcId | ${NacNfc.parseId(tag).toString()} | $doesNfcTagAlreadyExist")

			// Navigate to the save NFC tag dialog
			navController.navigate(destinationId, newArgs, this@NacScanNfcTagDialog,
				onBackStackPopulated = {

					// Get the item from the save NFC tag dialog and disable the NFC
					// tag dismiss order, just in case
					val newItem = navController.currentBackStackEntry?.savedStateHandle?.get<NacAlarm>("YOYOYO")
					println("Back STACK Populated!  ${newItem?.nfcTagId}")
					// TODO: Should maybe not do this?
					//newItem?.shouldUseNfcTagDismissOrder = false

					// Save the item and dismiss
					onSaveAlarm(newItem)
					dismiss()

				})
		}

	}

	/**
	 * Use any NFC tag was clicked.
	 */
	open fun onUseAnyNfcTagClicked(alarm: NacAlarm?)
	{
		// TODO: See if I need this anymore?
		// Clear the NFC tag ID and NFC tag dismiss order
		alarm?.nfcTagId = ""
		alarm?.shouldUseNfcTagDismissOrder = false

		// Save the alarm and dismiss
		onSaveAlarm(alarm)
		dismiss()
	}

	/**
	 * Set add button usability.
	 */
	private fun setAddButtonUsability(button: MaterialButton)
	{
		setButtonUsability(button, (selectNfcTagListContainer.childCount == allNfcTags.size))
	}

	/**
	 * Set add button usability for all add buttons.
	 */
	private fun setAllAddButtonUsability()
	{
		selectNfcTagListContainer.children.forEach { root ->
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
		val unused = unusedNfcTagNames.toMutableList()
			.apply {

				// None of the selected NFC tags are the empty placeholder so add it to
				// the list of possible unused
				if (selectedNfcTags.none { it.name.isEmpty() && it.nfcId.isEmpty() })
				{
					println("ADDING EMPTY THING TO UNUSED")
					add(0, "")
				}

			}.toTypedArray()

		unused.forEach { println("UNUSED : $it") }

		// Update each dropdown
		selectNfcTagListContainer.children.forEach { root ->
			val textView: MaterialAutoCompleteTextView = root.findViewById(R.id.nfc_tag_dropdown_menu)
			textView.setSimpleItems(unused)
		}
	}

	/**
	 * Set button usability.
	 */
	private fun setButtonUsability(button: MaterialButton, notState: Boolean)
	{
		button.alpha = calcAlpha(!notState, alpha = 0.25f)
		button.isEnabled = !notState
	}

	/**
	 * Set dismiss order views usability.
	 */
	private fun setDismissOrderUsability()
	{
		// Count how many NFC tags are actually selected
		val emptyCount = selectedNfcTags.count { it.name.isEmpty() && it.nfcId.isEmpty() }
		val childCount = selectNfcTagListContainer.childCount
		val finalCount = childCount - emptyCount

		// Determine the usability
		val state = (finalCount > 1)
		val alpha = calcAlpha(state)

		// Container
		dismissOrderContainer.alpha = alpha
		dismissOrderContainer.isEnabled = state

		// Dropdown menu
		dismissOrderInputLayout.alpha = alpha
		dismissOrderInputLayout.isEnabled = state && dismissOrderSwitch.isChecked
	}

	/**
	 * Set OK button visibility.
	 */
	private fun setOkButtonVisibility()
	{
		// Get the ok button
		val okButton: MaterialButton = dialog!!.findViewById(R.id.ok_button)

		// Set the visibility. If the select NFC tags and dismiss order views are
		// not shown, then the OK button does not need to be shown either since all the
		// buttons are basically going to do the same thing
		okButton.visibility = viewVisibility
	}

	/**
	 * Set remove button usability.
	 */
	private fun setRemoveButtonUsability(button: MaterialButton)
	{
		setButtonUsability(button, (selectNfcTagListContainer.childCount == 1))
	}

	/**
	 * Setup all alarm options.
	 */
	override fun setupAlarmOptions(alarm: NacAlarm?)
	{
		// Get the alarm, or build a new one, to get default values
		val a = alarm ?: NacAlarm.build(sharedPreferences)

		// Set the default selected values
		selectedDismissOrder = NacAlarm.calcNfcTagDismissOrderIndex(a.nfcTagDismissOrder)
		currentlySelectedNfcTagIds = a.nfcTagIdList
		currentlySelectedNfcTagIds.forEach { println("CURRENT : $it") }

		// Setup the views
		lifecycleScope.launch {

			setupAllAndSelectedNfcTags()
			setupCurrentlySelectedInfo()
			setupStartTimerOnScan(alarm)
			setupSelectNfcTag()
			// TODO: Need to add thing for timer in here. Maybe override setupCurrentlySelected or allandselected?
			setupDismissOrder(a.shouldUseNfcTagDismissOrder, a.nfcTagDismissOrder)
			setOkButtonVisibility()

		}
	}

	/**
	 * Setup the start timer on scan.
	 */
	open fun setupStartTimerOnScan(alarm: NacAlarm?) {}

	/**
	 * Setup all and selected NFC tags.
	 */
	private suspend fun setupAllAndSelectedNfcTags()
	{
		// Get the alarm NFC IDs and all the NFC tags
		allNfcTags = nfcTagViewModel.getAllNfcTags()
			.toMutableList()
			.apply {

				// Add any NFC IDs that are currently selected, but are not saved to the
				// table
				currentlySelectedNfcTagIds.reversed().forEach { id ->
					if (this.none { it.nfcId == id })
					{
						println("ADDING : $id")
						add(0, NacNfcTag("", id))
					}
				}

			}

		// Set the selected NFC tags based on the alarm
		selectedNfcTags = currentlySelectedNfcTagIds
			.mapNotNull { id ->
				allNfcTags.find { it.nfcId == id }
			}
			.toMutableList()

		// No tags have been selected for this alarm. Default to having an empty NFC tag
		// shown
		if (selectedNfcTags.isEmpty())
		{
			selectedNfcTags.add(NacNfcTag("", ""))
		}
	}

	/**
	 * Setup the currently selected information.
	 */
	private fun setupCurrentlySelectedInfo()
	{
		// No NFC IDs are set
		if (currentlySelectedNfcTagIds.isEmpty())
		{
			return
		}

		// Get the views and NFC tag name
		val title: TextView = dialog!!.findViewById(R.id.title_currently_selected)
		val description: TextView = dialog!!.findViewById(R.id.description_nfc_tag_name)

		lifecycleScope.launch {

			// Get the name of each NFC tag selected
			var names = ""

			currentlySelectedNfcTagIds.forEachIndexed { index, id ->

				// Find the NFC tag
				val tag = allNfcTags.firstOrNull { it.nfcId == id }

				// Add a newline if multiple NFC tags have been found
				if (index > 0)
				{
					names += "\n"
				}

				// Add the tag name or show the ID
				names += tag?.name?.takeIf { it.isNotEmpty() }
					?: "(${resources.getString(R.string.message_show_nfc_tag_id)}) $id"

			}

			// Set the description
			description.text = names

			// Change the visibility of the views
			title.visibility = View.VISIBLE
			description.visibility = View.VISIBLE

		}
	}

	/**
	 * Setup the input layout and textview.
	 */
	private fun setupDismissOrder(
		defaultUseNfcDismissOrder: Boolean,
		defaultDismissOrder: Int)
	{
		// Get the views
		val autoCompleteTextView: MaterialAutoCompleteTextView = dialog!!.findViewById(R.id.nfc_tag_dismiss_order_dropdown_menu)
		val dismissOrderSeparator: Space = dialog!!.findViewById(R.id.nfc_tag_dismiss_order_separator)

		// Determine the visibility
		val visibility = viewVisibility

		// Set the visibility
		dismissOrderContainer.visibility = visibility
		dismissOrderInputLayout.visibility = visibility
		dismissOrderSeparator.visibility = visibility

		// Do not proceed if the views are not shown
		if (visibility == View.GONE)
		{
			return
		}

		// Get the index of dismiss order
		val index = NacAlarm.calcNfcTagDismissOrderIndex(defaultDismissOrder)

		// Setup the views
		dismissOrderSwitch.isChecked = defaultUseNfcDismissOrder
		dismissOrderSwitch.setupSwitchColor(sharedPreferences)
		dismissOrderInputLayout.setupInputLayoutColor(requireContext(), sharedPreferences)
		autoCompleteTextView.setTextFromIndex(index)

		// Set the relative layout listener
		dismissOrderContainer.setOnClickListener {

			// Toggle the switch and set the usability of the dropdown
			dismissOrderSwitch.toggle()
			setDismissOrderUsability()

		}

		// Set the textview listeners
		autoCompleteTextView.onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
			selectedDismissOrder = position
		}

		// Set the usability
		setDismissOrderUsability()
		// TODO: The dropdown shows all janks still available even though it should only show a blank spot
	}

	/**
	 * Setup any extra buttons.
	 */
	override fun setupExtraButtons(alarm: NacAlarm?)
	{
		// Get the views
		val useAnyButton: MaterialButton = dialog!!.findViewById(R.id.use_any_nfc_tag_button)
		val cancelButton: MaterialButton = dialog!!.findViewById(R.id.cancel_button)
		val parentView: LinearLayout = useAnyButton.parent as LinearLayout

		// Get the visibility. If the alarm/timer does not have any NFC tags set, then it
		// does not need to be shown
		val visibility = if (alarm?.nfcTagId?.isNotEmpty() == true) View.VISIBLE else View.GONE

		// Set the visibility
		useAnyButton.visibility = visibility

		// Do not proceed if the button is not shown
		if (visibility == View.GONE)
		{
			return
		}

		// Swap views
		parentView.removeView(cancelButton)
		parentView.removeView(useAnyButton)
		parentView.addView(useAnyButton)
		parentView.addView(cancelButton)

		// Setup the button
		setupSecondaryButton(useAnyButton, listener = { onUseAnyNfcTagClicked(alarm) })
	}

	/**
	 * Setup the input layout and textview.
	 */
	private fun setupInputLayoutAndTextView(defaultName: String, nfcTagNames: Array<String>)
	{
		// Inflate the layout and add it to the container
		val root = dialog!!.layoutInflater.inflate(R.layout.nac_select_add_remove_nfc_tag,
			selectNfcTagListContainer, false)

		selectNfcTagListContainer.addView(root)

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
			val index = selectNfcTagListContainer.indexOfChild(root)

			// Empty NFC tag selected
			if (name.isEmpty())
			{
				// Replace the selected NFC tag
				selectedNfcTags[index] = NacNfcTag("", "")
			}
			// Normal NFC tag selected
			else
			{
				// Find the matching tag in the list of all NFC tags and add/replace it in
				// the selected NFC tags list
				allNfcTags.firstOrNull { it.matchesName(name) }
					?.let { match ->

						// Add the selected NFC tag
						if (index + 1 > selectedNfcTags.size)
						{
							selectedNfcTags.add(match)
						}
						// Replace the selected NFC tag
						else
						{
							selectedNfcTags[index] = match
						}

					}
			}

			// Update the dropdown items and set the dismiss order usability
			setAllDropdownItems()
			setDismissOrderUsability()

			selectedNfcTags.forEach { println("Current selected : ${it.name} | ${it.nfcId}") }
		}

		// Add button click listener
		addButton.setOnClickListener {

			// Find the first NFC tag that is not being used
			val unusedName = unusedNfcTagNames.firstOrNull()
				?.also { name ->

					// Add the unused tag to the selected NFC tags
					allNfcTags.firstOrNull { it.matchesName(name) }
						?.let { selectedNfcTags.add(it) }

				}
			println("unused name ::::: $unusedName")

			// Add a new NFC tag and update all the dropdown items
			setupInputLayoutAndTextView(unusedName ?: nfcTagNames[0], nfcTagNames)
			setAllDropdownItems()
			selectedNfcTags.forEach { println("Current selected : ${it.name} | ${it.nfcId}") }
		}

		// Remove button click listener
		removeButton.setOnClickListener {

			// Remove the view
			selectNfcTagListContainer.removeView(root)

			// Remove the tag from the list
			selectedNfcTags.indexOfFirst { it.matchesName(textView.text.toString()) }
				.takeIf { it >= 0 }
				?.let { index ->
					println("Removing index : $index")
					selectedNfcTags.removeAt(index)
				}

			// Update all the dropdown items
			setAllDropdownItems()
			selectedNfcTags.forEach { println("Current selected : ${it.name} | ${it.nfcId}") }
		}
	}

	/**
	 * Setup the select NFC tag section.
	 */
	private fun setupSelectNfcTag()
	{
		// Get the views
		val selectNfcTagTitle: TextView = dialog!!.findViewById(R.id.select_nfc_tag_title)
		val selectNfcTagSeparator: Space = dialog!!.findViewById(R.id.select_nfc_tag_separator)

		// Determine the visibility
		val visibility = viewVisibility

		// Set the visibility
		selectNfcTagTitle.visibility = visibility
		selectNfcTagDescription.visibility = visibility
		selectNfcTagListContainer.visibility = visibility
		selectNfcTagSeparator.visibility = visibility

		// Do not proceed if the views are not shown
		if (visibility == View.GONE)
		{
			return
		}

		// Set the listener for when children are added/removed
		selectNfcTagListContainer.setOnHierarchyChangeListener(object: OnHierarchyChangeListener
		{

			/**
			 * A child is added.
			 */
			override fun onChildViewAdded(parent: View, child: View)
			{
				// Set the usability of all add buttons and dismiss order views
				setAllAddButtonUsability()
				setDismissOrderUsability()
			}

			/**
			 * A child is reomved.
			 */
			override fun onChildViewRemoved(parent: View, child: View)
			{
				// Set the usability of all add buttons and dismiss order views
				setAllAddButtonUsability()
				setDismissOrderUsability()
			}

		})

		// Set the list of items in the textview dropdown menu. If the name is empty then
		// use the NFC ID
		val nfcTagNames = allNfcTags.map { it.text }
			.toMutableList()
			.apply { add(0, "") }
			.toTypedArray()

		// Create input layouts for select NFC tags
		selectedNfcTags.forEach {
			setupInputLayoutAndTextView(it.text, nfcTagNames)
		}

		// Update all the dropdown items
		setAllDropdownItems()
	}

}