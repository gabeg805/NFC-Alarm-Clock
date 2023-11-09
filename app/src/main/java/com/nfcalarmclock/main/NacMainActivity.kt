package com.nfcalarmclock.main

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.graphics.drawable.InsetDrawable
import android.os.Bundle
import android.view.ContextMenu
import android.view.ContextMenu.ContextMenuInfo
import android.view.HapticFeedbackConstants
import android.view.Menu
import android.view.MenuItem
import android.view.MotionEvent
import android.view.View
import android.view.View.OnCreateContextMenuListener
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.OnItemTouchListener
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.textview.MaterialTextView
import com.nfcalarmclock.BuildConfig
import com.nfcalarmclock.R
import com.nfcalarmclock.activealarm.NacActiveAlarmService
import com.nfcalarmclock.alarm.NacAlarmViewModel
import com.nfcalarmclock.alarm.db.NacAlarm
import com.nfcalarmclock.audiooptions.NacAlarmAudioOptionsDialog
import com.nfcalarmclock.audiooptions.NacAlarmAudioOptionsDialog.OnAudioOptionClickedListener
import com.nfcalarmclock.audiosource.NacAudioSourceDialog
import com.nfcalarmclock.audiosource.NacAudioSourceDialog.OnAudioSourceSelectedListener
import com.nfcalarmclock.card.NacCardAdapter
import com.nfcalarmclock.card.NacCardAdapter.OnViewHolderBoundListener
import com.nfcalarmclock.card.NacCardAdapter.OnViewHolderCreatedListener
import com.nfcalarmclock.card.NacCardAdapterLiveData
import com.nfcalarmclock.card.NacCardHolder
import com.nfcalarmclock.card.NacCardHolder.OnCardAudioOptionsClickedListener
import com.nfcalarmclock.card.NacCardHolder.OnCardCollapsedListener
import com.nfcalarmclock.card.NacCardHolder.OnCardDeleteClickedListener
import com.nfcalarmclock.card.NacCardHolder.OnCardExpandedListener
import com.nfcalarmclock.card.NacCardHolder.OnCardMediaClickedListener
import com.nfcalarmclock.card.NacCardHolder.OnCardUpdatedListener
import com.nfcalarmclock.card.NacCardHolder.OnCardUseNfcChangedListener
import com.nfcalarmclock.card.NacCardTouchHelper
import com.nfcalarmclock.card.NacCardTouchHelper.OnSwipedListener
import com.nfcalarmclock.dismissearly.NacDismissEarlyDialog
import com.nfcalarmclock.dismissearly.NacDismissEarlyDialog.OnDismissEarlyOptionSelectedListener
import com.nfcalarmclock.graduallyincreasevolume.NacGraduallyIncreaseVolumeDialog
import com.nfcalarmclock.graduallyincreasevolume.NacGraduallyIncreaseVolumeDialog.OnGraduallyIncreaseVolumeListener
import com.nfcalarmclock.mediapicker.NacMediaActivity
import com.nfcalarmclock.nfc.NacNfc.getTag
import com.nfcalarmclock.nfc.NacNfc.parseId
import com.nfcalarmclock.nfc.NacNfc.start
import com.nfcalarmclock.nfc.NacNfc.stop
import com.nfcalarmclock.nfc.NacNfc.wasScanned
import com.nfcalarmclock.nfc.NacNfcTag
import com.nfcalarmclock.nfc.NacScanNfcTagDialog
import com.nfcalarmclock.nfc.NacScanNfcTagDialog.OnScanNfcTagListener
import com.nfcalarmclock.permission.NacPermissionRequestManager
import com.nfcalarmclock.ratemyapp.NacRateMyApp.request
import com.nfcalarmclock.restrictvolume.NacRestrictVolumeDialog
import com.nfcalarmclock.restrictvolume.NacRestrictVolumeDialog.OnRestrictVolumeListener
import com.nfcalarmclock.settings.NacMainSettingActivity
import com.nfcalarmclock.shared.NacSharedPreferences
import com.nfcalarmclock.shutdown.NacShutdownBroadcastReceiver
import com.nfcalarmclock.statistics.NacAlarmStatisticRepository
import com.nfcalarmclock.tts.NacTextToSpeechDialog
import com.nfcalarmclock.tts.NacTextToSpeechDialog.OnTextToSpeechOptionsSelectedListener
import com.nfcalarmclock.upcomingalarm.NacUpcomingAlarmNotification
import com.nfcalarmclock.util.NacCalendar
import com.nfcalarmclock.util.NacContext.dismissAlarmActivityWithNfc
import com.nfcalarmclock.util.NacContext.startAlarm
import com.nfcalarmclock.util.NacContext.startAlarmActivity
import com.nfcalarmclock.util.NacIntent.getSetAlarm
import com.nfcalarmclock.util.NacIntent.toIntent
import com.nfcalarmclock.util.NacUtility.printf
import com.nfcalarmclock.util.NacUtility.quickToast
import com.nfcalarmclock.view.snackbar.NacSnackbar
import com.nfcalarmclock.whatsnew.NacWhatsNewDialog
import com.nfcalarmclock.whatsnew.NacWhatsNewDialog.OnReadWhatsNewListener
import java.util.Locale

/**
 * The application's main activity.
 */
class NacMainActivity

	// Constructor
	: AppCompatActivity(),

	// Interface
	OnCreateContextMenuListener,
	Toolbar.OnMenuItemClickListener,
	OnItemTouchListener,
	OnSwipedListener,
	OnViewHolderBoundListener,
	OnViewHolderCreatedListener,
	OnCardCollapsedListener,
	OnCardDeleteClickedListener,
	OnCardExpandedListener,
	OnCardMediaClickedListener,
	OnCardAudioOptionsClickedListener,
	OnCardUpdatedListener,
	OnCardUseNfcChangedListener,
	OnScanNfcTagListener,
	OnAudioOptionClickedListener,
	OnAudioSourceSelectedListener,
	OnDismissEarlyOptionSelectedListener,
	OnGraduallyIncreaseVolumeListener,
	OnRestrictVolumeListener,
	OnTextToSpeechOptionsSelectedListener,
	OnReadWhatsNewListener
{

	/**
	 * Shared preferences.
	 */
	private var sharedPreferences: NacSharedPreferences? = null

	/**
	 * Root view.
	 */
	private var root: View? = null

	/**
	 * Top toolbar.
	 */
	private var toolbar: MaterialToolbar? = null

	/**
	 * Next alarm text view.
	 */
	private var nextAlarmTextView: MaterialTextView? = null

	/**
	 * Recycler view containing the alarm cards.
	 */
	private var recyclerView: RecyclerView? = null

	/**
	 * Floating action button to add new alarms.
	 */
	private var floatingActionButton: FloatingActionButton? = null

	/**
	 * Alarm card adapter.
	 */
	private var alarmCardAdapter: NacCardAdapter? = null

	/**
	 * The snackbar.
	 */
	private var snackbar: NacSnackbar? = null

	/**
	 * Alarm statistic repository.
	 */
	private var alarmStatisticRepository: NacAlarmStatisticRepository? = null

	/**
	 * Alarm view model.
	 */
	private var alarmViewModel: NacAlarmViewModel? = null

	/**
	 * Mutable live data for the alarm card that can be modified and sorted, or
	 * not sorted, depending on the circumstance.
	 *
	 * Live data from the view model cannot be sorted, hence the need for this.
	 */
	private var alarmCardAdapterLiveData: NacCardAdapterLiveData? = null

	/**
	 * Alarm card touch helper.
	 */
	private var alarmCardTouchHelper: NacCardTouchHelper? = null

	/**
	 * Shutdown broadcast receiver.
	 *
	 * TODO: Should this be final like mTimeTickReceiver?
	 */
	private var shutdownBroadcastReceiver: NacShutdownBroadcastReceiver? = null

	/**
	 * The IDs of alarms that were recently added.
	 */
	private var recentlyAddedAlarmIds: MutableList<Long> = ArrayList()

	/**
	 * The IDs of alarms that were recently updated.
	 */
	private var recentlyUpdatedAlarmIds: MutableList<Long> = ArrayList()

	/**
	 * Last action on an alarm card.
	 */
	private var lastAlarmCardAction = NacLastAlarmCardAction()

	/**
	 * Permission request manager, handles requesting permissions from the user.
	 */
	private var permissionRequestManager: NacPermissionRequestManager? = null

	/**
	 * Indicator of whether the activity is shown or not.
	 */
	private var isActivityShown = false

	/**
	 * View of the last card clicked.
	 */
	private var mLastCardClicked: View? = null

	/**
	 * Scan an NFC tag dialog.
	 */
	private var scanNfcTagDialog: NacScanNfcTagDialog? = null

	/**
	 * Alarm that is being used by an open audio options dialog.
	 */
	private var audioOptionsAlarm: NacAlarm? = null

	/**
	 * The NFC tag that was scanned for an active alarm.
	 */
	private var nfcTag: NacNfcTag? = null

	/**
	 * Listener for when the floating action button is clicked.
	 */
	private val mFloatingActionButtonListener = View.OnClickListener { view: View ->

		// Get the current and max counts
		val currentSize = alarmCardAdapter!!.itemCount
		val maxAlarms = resources.getInteger(R.integer.max_alarms)

		// Haptic feedback so that the user knows the action was received
		view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)

		// Max number of alarms reached
		if (currentSize+1 > maxAlarms)
		{
			val message = getString(R.string.error_message_max_alarms)

			// Show a toast that the max number of alarms was created
			quickToast(this@NacMainActivity, message)
			return@OnClickListener
		}

		// Create the alarm
		val alarm = NacAlarm.Builder(sharedPreferences)
			.build()

		// Add the alarm
		addAlarm(alarm)
	}

	/**
	 * Capture the click event on the Snackbar button.
	 */
	private val mOnSwipeSnackbarActionListener = View.OnClickListener {

		// Get the alarm
		val alarm = lastAlarmCardAction.alarm

		// Undo copy
		if (lastAlarmCardAction.wasCopy())
		{
			// Delete the alarm
			deleteAlarm(alarm)
		}
		// Undo delete
		else if (lastAlarmCardAction.wasDelete())
		{
			// Restore the alarm
			restoreAlarm(alarm)
		}
		// Undo restore
		else if (lastAlarmCardAction.wasRestore())
		{
			// Delete the alarm
			deleteAlarm(alarm)
		}
	}

	/**
	 * Receiver for the time tick intent. This is called when the time increments
	 * every minute.
	 *
	 * TODO: Should this be its own class like NacShutdownBroadcastReceiver?
	 */
	private val timeTickReceiver = object : BroadcastReceiver()
	{
		override fun onReceive(context: Context, intent: Intent)
		{
			// Set the message for when the next alarm will be run
			setNextAlarmMessage()

			// Refresh alarms that will run soon
			refreshAlarmsThatWillAlarmSoon()
		}
	}

	/**
	 * Add an alarm to the database.
	 *
	 * @param  alarm  An alarm.
	 */
	private fun addAlarm(alarm: NacAlarm?)
	{
		// Check if the alarm is null
		if (alarm == null)
		{
			return
		}

		// Insert alarm
		val id = alarmViewModel!!.insert(this, alarm)

		// Set the ID of the alarm
		if (alarm.id <= 0)
		{
			alarm.id = id
		}

		// Save the recently added alarm ID
		recentlyAddedAlarmIds.add(id)

		// Save the statistics
		alarmStatisticRepository!!.insertCreated()
	}

	/**
	 * Add the first alarm, when the app is first run.
	 */
	private fun addFirstAlarm()
	{
		// Get the shared prefereneces
		val shared = sharedPreferences

		// Create the alarm
		val alarm = NacAlarm.Builder(shared)
			.setId(0)
			.setHour(8)
			.setMinute(0)
			.setName("Work")
			.build()

		// Add the alarm
		addAlarm(alarm)

		// Avoid having interact() called for the alarm card, that way it does not
		// get expanded and show the time dialog
		val id = alarm.id
		recentlyAddedAlarmIds.remove(id)
	}

	/**
	 * Add an alarm that was created from the SET_ALARM intent.
	 */
	private fun addSetAlarmFromIntent()
	{
		val intent = intent
		val alarm = getSetAlarm(this, intent)
		addAlarm(alarm)
	}

	/**
	 * Cleanup the show activity delay handler.
	 */
	private fun cleanupScanNfcTagDialog()
	{
		val dialog = scanNfcTagDialog

		// Check that the dialog is not null
		if (dialog != null)
		{
			// Dismiss the dialog
			dialog.dismissAllowingStateLoss()

			// Cleanup the dialog
			scanNfcTagDialog = null
		}
	}

	/**
	 * Cleanup the shutdown broadcast receiver.
	 */
	private fun cleanupShutdownBroadcastReceiver()
	{
		val receiver = shutdownBroadcastReceiver
		if (receiver != null)
		{
			try
			{
				// Unregister the receiver
				unregisterReceiver(receiver)
			}
			catch (e: IllegalArgumentException)
			{
				//e.printStackTrace();
			}
		}
	}

	/**
	 * Cleanup the time tick receiver.
	 */
	private fun cleanupTimeTickReceiver()
	{
		val receiver = timeTickReceiver
		try
		{
			// Unregister the receiver
			unregisterReceiver(receiver)
		}
		catch (e: IllegalArgumentException)
		{
			//e.printStackTrace();
		}
	}

	/**
	 * TODO: Catch exceptions properly
	 */
	fun copyAlarm(alarm: NacAlarm?)
	{
		val message = getString(R.string.message_alarm_copy)
		val action = getString(R.string.action_undo)
		val copiedAlarm = alarm!!.copy()
		addAlarm(copiedAlarm)
		lastAlarmCardAction[copiedAlarm] = NacLastAlarmCardAction.Type.COPY
		showSnackbar(message, action, mOnSwipeSnackbarActionListener)
	}

	/**
	 * Delete an alarm from the database.
	 *
	 * @param  alarm  An alarm.
	 */
	fun deleteAlarm(alarm: NacAlarm?)
	{
		if (alarm == null)
		{
			return
		}
		val message = getString(R.string.message_alarm_delete)
		val action = getString(R.string.action_undo)
		alarmViewModel!!.delete(this, alarm)
		alarmStatisticRepository!!.insertDeleted(alarm)
		lastAlarmCardAction[alarm] = NacLastAlarmCardAction.Type.DELETE
		showSnackbar(message, action, mOnSwipeSnackbarActionListener)
	}

	/**
	 * Disable the alias for the main activity so that tapping an NFC tag
	 * DOES NOT open the main activity.
	 */
	private fun disableActivityAlias()
	{
		val packageManager = packageManager
		val packageName = packageName
		val aliasName = "$packageName.main.NacMainAliasActivity"
		val componentName = ComponentName(this, aliasName)
		packageManager.setComponentEnabledSetting(componentName,
			PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
			PackageManager.DONT_KILL_APP)
	}

	/**
	 * Attempt to dismiss the first active alarm found.
	 *
	 *
	 * If unable to dismiss the alarm, the alarm activity is shown.
	 */
	private fun dismissActiveAlarm()
	{
		val tag = nfcTag
		if (tag == null)
		{
			return
		}
		else if (tag.check(this))
		{
			dismissAlarmActivityWithNfc(this, tag)
		}
		else
		{
			startAlarm(this, tag.activeAlarm)
		}
		nfcTag = null
	}

	/**
	 * @return The alarm card at the given index.
	 */
	private fun getAlarmCardAt(index: Int): NacCardHolder?
	{
		val rv = recyclerView
		return rv!!.findViewHolderForAdapterPosition(index) as NacCardHolder?
	}

	private val cardsExpandedCount: Int
		/**
		 * @return The number of alarm cards that are expanded.
		 */
		get()
		{
			val rv = recyclerView
			val adapter = alarmCardAdapter
			return adapter!!.getCardsExpandedCount(rv!!)
		}

	/**
	 * Get the message to show for the next alarm.
	 *
	 * @param  alarms  List of alarms.
	 *
	 * @return The message to show for the next alarm.
	 */
	fun getNextAlarmMessage(alarms: List<NacAlarm>?): String
	{
		val shared = sharedPreferences
		val nextAlarm = NacCalendar.getNextAlarm(alarms)
		return NacCalendar.getMessageNextAlarm(shared, nextAlarm)
	}

	private val isNfcTagReady: Boolean
		/**
		 * @return True if an NFC tag was scanned to dismiss an alarm and is ready
		 * for the active alarm activity, and False otherwise.
		 */
		get()
		{
			val tag = nfcTag
			return tag != null && tag.isReady
		}

	/**
	 * Called when an item in the audio options dialog is clicked.
	 */
	override fun onAudioOptionClicked(alarmId: Long, which: Int)
	{
		audioOptionsAlarm = alarmViewModel!!.findAlarm(alarmId)
		when (which)
		{
			0 -> showAudioSourceDialog()
			1 -> showDismissEarlyDialog()
			2 -> showGraduallyIncreaseVolumeDialog()
			3 -> showRestrictVolumeDialog()
			4 -> showTextToSpeechDialog()
			else ->
			{
			}
		}
	}

	/**
	 * Called when an audio source is selected.
	 */
	override fun onAudioSourceSelected(audioSource: String?)
	{
		val alarm = audioOptionsAlarm
		alarm!!.audioSource = audioSource!!
		alarmViewModel!!.update(this, alarm)
	}

	/**
	 * Called when the user cancels the scan NFC tag dialog.
	 */
	override fun onCancelNfcTagScan(alarm: NacAlarm?)
	{
		val rv = recyclerView

		// Check that the alarm is not null
		if (alarm != null)
		{

			// Get the card that corresponds to the alarm
			val id = alarm.id
			val cardHolder = rv!!.findViewHolderForItemId(id) as NacCardHolder

			// Uncheck the NFC button when the dialog is canceled.
			cardHolder.nfcButton.isChecked = false
			cardHolder.doNfcButtonClick()
		}

		// Cleanup the dialog
		cleanupScanNfcTagDialog()
	}

	/**
	 * Called when the audio options button is clicked in an alarm card.
	 */
	override fun onCardAudioOptionsClicked(holder: NacCardHolder?, alarm: NacAlarm?)
	{
		showAudioOptionsDialog(alarm)
	}

	/**
	 * Called when the alarm card is collapsed.
	 */
	override fun onCardCollapsed(holder: NacCardHolder?, alarm: NacAlarm?)
	{
		val updatedAlarms = recentlyUpdatedAlarmIds
		val id = alarm!!.id

		// Sort the list when no cards are expanded
		if (cardsExpandedCount == 0)
		{
			alarmCardAdapterLiveData!!.sort()
		}

		// Show the next time the alarm will go off, as well as highlight the card
		// that was just collapsed
		if (updatedAlarms.contains(id))
		{
			showUpdatedAlarmSnackbar(alarm)
			holder!!.highlight()
			updatedAlarms.remove(id)
		}
	}

	/**
	 * Called when the delete button is clicked in an alarm card.
	 */
	override fun onCardDeleteClicked(holder: NacCardHolder?, alarm: NacAlarm?)
	{
		deleteAlarm(alarm)
	}

	/**
	 * Called when the alarm card is expanded.
	 */
	override fun onCardExpanded(holder: NacCardHolder?, alarm: NacAlarm?)
	{
	}

	/**
	 * Called when the media button is clicked in an alarm card.
	 */
	override fun onCardMediaClicked(holder: NacCardHolder?, alarm: NacAlarm?)
	{
		val intent = toIntent(this, NacMediaActivity::class.java, alarm)
		startActivity(intent)
	}

	/**
	 * Called when the alarm has been changed.
	 *
	 * @param  alarm  The alarm that was changed.
	 */
	override fun onCardUpdated(holder: NacCardHolder?, alarm: NacAlarm?)
	{
		// Set the next alarm message
		this.setNextAlarmMessage()

		// Card is collapsed
		if (holder!!.isCollapsed)
		{
			showUpdatedAlarmSnackbar(alarm)
			holder.highlight()
		}
		else
		{
			val id = alarm!!.id
			recentlyUpdatedAlarmIds.add(id)
		}

		// Update the view model
		alarmViewModel!!.update(this, alarm)
	}

	/**
	 */
	override fun onCardUseNfcChanged(holder: NacCardHolder?, alarm: NacAlarm?)
	{
		if (!alarm!!.shouldUseNfc)
		{
			return
		}

		// Get the fragment manager
		val manager = supportFragmentManager

		// Create the dialog
		val dialog = NacScanNfcTagDialog()
		scanNfcTagDialog = dialog

		// Setup the dialog
		dialog.alarm = alarm
		dialog.onScanNfcTagListener = this

		// Show the dialog
		dialog.show(manager, NacScanNfcTagDialog.TAG)
	}

	/**
	 * Called when an alarm card was swiped to copy.
	 *
	 * @param  index  The index of the alarm card.
	 */
	override fun onCopySwipe(alarm: NacAlarm?, index: Int)
	{
		val adapter = alarmCardAdapter
		val card = getAlarmCardAt(index)
		val size = adapter!!.itemCount
		val maxAlarms = resources.getInteger(R.integer.max_alarms)

		// Haptic feedback so that the user knows the action was received
		card?.root?.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)

		// Reset the view on the alarm that was swiped
		adapter.notifyItemChanged(index)

		// Max number of alarms reached
		if (size + 1 > maxAlarms)
		{
			val message = getString(R.string.error_message_max_alarms)
			quickToast(this, message)
			return
		}

		// Set the index of the new alarm that will be created. This way, the
		// the snackbar can undo any action on that alarm
		lastAlarmCardAction.index = size

		// Copy the alarm
		copyAlarm(alarm)
	}

	/**
	 * Called when the activity is created.
	 */
	@SuppressLint("NewApi")
	override fun onCreate(savedInstanceState: Bundle?)
	{
		// Setup
		super.onCreate(savedInstanceState)

		// Set the content view
		setContentView(R.layout.act_main)

		// Set member variables
		sharedPreferences = NacSharedPreferences(this)
		root = findViewById(R.id.activity_main)
		toolbar = findViewById(R.id.tb_top_bar)
		nextAlarmTextView = findViewById(R.id.tv_next_alarm)
		recyclerView = findViewById(R.id.rv_alarm_list)
		floatingActionButton = findViewById(R.id.fab_add_alarm)
		alarmCardAdapter = NacCardAdapter()
		snackbar = NacSnackbar(root!!)
		alarmStatisticRepository = NacAlarmStatisticRepository(this)
		alarmViewModel = ViewModelProvider(this).get(NacAlarmViewModel::class.java)
		alarmCardAdapterLiveData = NacCardAdapterLiveData()
		alarmCardTouchHelper = NacCardTouchHelper(this)
		shutdownBroadcastReceiver = NacShutdownBroadcastReceiver()
		permissionRequestManager = NacPermissionRequestManager(this)

		// Setup
		sharedPreferences!!.editCardIsMeasured(false)
		setupLiveDataObservers()
		setupToolbar()
		setupAlarmCardAdapter()
		setupRecyclerView()

		// NFC tag was scanned for an active alarm
		if (wasNfcScannedForActiveAlarm(intent))
		{
			setNfcTagIntent(intent)
		}

		// Disable the activity alias so that tapping an NFC tag will NOT open
		// the main activity
		disableActivityAlias()
	}

	/**
	 * Create the context menu.
	 */
	override fun onCreateContextMenu(menu: ContextMenu, view: View,
		menuInfo: ContextMenuInfo)
	{
		// Set the last card that was clicked
		mLastCardClicked = view

		// Inflate the context menu
		menuInflater.inflate(R.menu.menu_card, menu)

		// Iterate over each menu item
		for (i in 0 until menu.size())
		{
			val item = menu.getItem(i)

			// Set the listener for a menu item
			item.setOnMenuItemClickListener { menuItem: MenuItem ->
				val rv = recyclerView
				val holder = rv!!.findContainingViewHolder(mLastCardClicked!!) as NacCardHolder?
				val id = menuItem.itemId

				// Check to make sure the card holder is not null
				if (holder != null)
				{
					val alarm = holder.alarm

					// Show the next time the alarm is scheduled to go off
					if (id == R.id.menu_show_next_alarm)
					{
						showAlarmSnackbar(alarm)
					}
					else if (id == R.id.menu_show_nfc_tag_id)
					{
						showNfcTagId(alarm)
					}
				}

				// Reset the last clicked card to null
				mLastCardClicked = null
				true
			}
		}
	}

	/**
	 */
	override fun onCreateOptionsMenu(menu: Menu): Boolean
	{
		menuInflater.inflate(R.menu.menu_action_bar, menu)
		return true
	}

	/**
	 * Called when an alarm card was swiped to delete.
	 *
	 * @param  index  The index of the alarm card.
	 */
	override fun onDeleteSwipe(alarm: NacAlarm?, index: Int)
	{
		val card = getAlarmCardAt(index)

		// Haptic feedback so that the user knows the action was received
		card?.root?.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)

		// Set the index of the new alarm that will be created. This way, the
		// the snackbar can undo any action on that alarm
		lastAlarmCardAction.index = index

		// Delete the alarm
		deleteAlarm(alarm)
	}

	/**
	 * Called when the dismiss early alarm option is selected.
	 */
	override fun onDismissEarlyOptionSelected(useDismissEarly: Boolean, index: Int)
	{
		val alarm = audioOptionsAlarm
		alarm!!.useDismissEarly = useDismissEarly
		alarm.setDismissEarlyTimeFromIndex(index)
		alarmViewModel!!.update(this, alarm)
	}

	/**
	 * Called when the alarm volume should/should not be gradually increased when
	 * an alarm goes off.
	 */
	override fun onGraduallyIncreaseVolume(shouldIncrease: Boolean)
	{
		val alarm = audioOptionsAlarm
		alarm!!.shouldGraduallyIncreaseVolume = shouldIncrease
		alarmViewModel!!.update(this, alarm)
	}

	/**
	 * Needed for RecyclerView.OnItemTouchListener
	 */
	override fun onInterceptTouchEvent(rv: RecyclerView, ev: MotionEvent): Boolean
	{
		val action = ev.action
		if (action == MotionEvent.ACTION_UP)
		{
			val snackbar = snackbar
			if (snackbar!!.canDismiss)
			{
				snackbar.dismiss()
			}
		}
		return false
	}

	/**
	 * Catch when a menu item is clicked.
	 */
	override fun onMenuItemClick(item: MenuItem): Boolean
	{
		val id = item.itemId

		// Settings clicked
		return if (id == R.id.menu_settings)
		{
			val settingsIntent = Intent(this, NacMainSettingActivity::class.java)
			startActivity(settingsIntent)
			true
		}
		else
		{
			false
		}
	}

	/**
	 */
	override fun onNewIntent(intent: Intent)
	{
		super.onNewIntent(intent)

		// NFC tag was scanned for the NFC dialog
		if (wasNfcScannedForDialog(intent))
		{
			val dialog = scanNfcTagDialog
			val message = getString(R.string.message_nfc_required)

			// Save the NFC tag ID and show a toast
			saveNfcTagId(intent)
			quickToast(this, message)

			// Close the dialog
			if (dialog != null)
			{
				// Set the listener to null so that it does not get called
				dialog.onScanNfcTagListener = null

				// Dismiss the dialog
				dialog.dismiss()
			}
		}
		else if (wasNfcScannedForActiveAlarm(intent))
		{
			setNfcTagIntent(intent)
			dismissActiveAlarm()
		}
	}

	/**
	 */
	override fun onPause()
	{
		super.onPause()
		isActivityShown = false
		cleanupTimeTickReceiver()
		cleanupShutdownBroadcastReceiver()
		stop(this)
	}

	/**
	 * Called when the What's New dialog has been read.
	 */
	override fun onReadWhatsNew()
	{
		val shared = sharedPreferences
		val version = BuildConfig.VERSION_NAME

		// Set the previous app version as the current version. This way, the What's
		// New dialog does not show again
		shared!!.editPreviousAppVersion(version)
	}

	/**
	 * Note: Needed for RecyclerView.OnItemTouchListener
	 */
	override fun onRequestDisallowInterceptTouchEvent(disallowIntercept: Boolean)
	{
	}

	/**
	 */
	override fun onRestrictVolume(shouldRestrict: Boolean)
	{
		val alarm = audioOptionsAlarm
		alarm!!.shouldRestrictVolume = shouldRestrict
		alarmViewModel!!.update(this, alarm)
	}

	/**
	 */
	override fun onResume()
	{
		super.onResume()
		val shared = sharedPreferences

		// Check if the main activity should be refreshed and if so, refresh it
		if (shared!!.shouldRefreshMainActivity)
		{
			refreshMainActivity()
			return
		}

		// Set flag that the activity is being shown
		isActivityShown = true

		// Set the next alarm text
		this.setNextAlarmMessage()

		// Setup UI
		setupFloatingActionButton()
		setupInitialDialogToShow()

		// Setup broadcast receivers
		setupTimeTickReceiver()
		setupShutdownBroadcastReceiver()

		// Setup NFC scanning detection
		start(this)

		// Add alarm from SET_ALARM intent (if it is present in intent)
		addSetAlarmFromIntent()
	}

	/**
	 * Called when a text-to-speech option is selected.
	 */
	override fun onTextToSpeechOptionsSelected(useTts: Boolean, freq: Int)
	{
		val alarm = audioOptionsAlarm
		alarm!!.useTts = useTts
		alarm.ttsFrequency = freq
		alarmViewModel!!.update(this, alarm)
	}

	/**
	 * Note: Needed for RecyclerView.OnItemTouchListener
	 */
	override fun onTouchEvent(rv: RecyclerView, e: MotionEvent)
	{
	}

	/**
	 * Called when theh user wants to use any NFC tag.
	 */
	override fun onUseAnyNfcTag(alarm: NacAlarm?)
	{
		// Check that the alarm is null
		if (alarm == null)
		{
			return
		}

		// Set the default (empty) NFC tag ID.
		alarm.nfcTagId = ""

		// Update the alarm
		alarmViewModel!!.update(this, alarm)

		// Cleanup the dialog
		cleanupScanNfcTagDialog()

		// Toast to the user
		val message = getString(R.string.message_nfc_required)
		quickToast(this, message)
	}

	/**
	 * Needed for NacAlarmCardAdapter.OnViewHolderBoundListener.
	 */
	override fun onViewHolderBound(holder: NacCardHolder?, index: Int)
	{
		// Verify that the alarm card is measured. If it is not, it will be measured
		verifyCardIsMeasured(holder)
		val adapter = alarmCardAdapter
		val alarm = adapter!!.getAlarmAt(index)
		val addedAlarms = recentlyAddedAlarmIds
		val id = alarm.id

		// Interact with recently added alarms, expanding them and showing the time
		// dialog
		if (addedAlarms.contains(id))
		{
			holder!!.interact()
			addedAlarms.remove(id)
		}
	}

	/**
	 * Needed for NacAlarmCardAdapter.OnViewHolderCreatedListener.
	 */
	override fun onViewHolderCreated(holder: NacCardHolder?)
	{
		holder!!.onCardCollapsedListener = this
		holder.onCardDeleteClickedListener = this
		holder.onCardExpandedListener = this
		holder.onCardMediaClickedListener = this
		holder.onCardAudioOptionsClickedListener = this
		holder.onCardUpdatedListener = this
		holder.onCardUseNfcChangedListener = this
		holder.setOnCreateContextMenuListener(this)
	}

	/**
	 * Prepare an active alarm to be shown to the user.
	 *
	 *
	 * If the alarm is null, or the activity is not shown, it will not be run.
	 *
	 * @param  alarm  An active alarm.
	 */
	private fun prepareActiveAlarm(alarm: NacAlarm?)
	{
		if (alarm != null)
		{
			setNfcTagAlarm(alarm)
		}
		if (isNfcTagReady)
		{
			dismissActiveAlarm()
		}
		else if (isActivityShown && shouldShowAlarmActivity(alarm))
		{
			// TODO: This caused the active alarm to show up a million times!
			//NacSharedPreferences shared = this.getSharedPreferences();
			//Remove this setting: shared.getPreventAppFromClosing()?

			// Run the service only if it is not already running
			if (!NacActiveAlarmService.isRunning(this))
			{
				NacActiveAlarmService.startService(this, alarm)
			}

			// Start the alarm activity
			startAlarmActivity(this, alarm)
		}
	}

	/**
	 * Refresh alarms that will alarm soon.
	 */
	private fun refreshAlarmsThatWillAlarmSoon()
	{
		val adapter = alarmCardAdapter
		val length = adapter!!.itemCount

		// Iterate over each alarm card in the adapter
		for (i in 0 until length)
		{
			val card = getAlarmCardAt(i)
			val a = adapter.getAlarmAt(i)

			// Alarm will alarm soon and the card needs to be updated
			if (card != null && a.willAlarmSoon() && card.shouldRefreshDismissView())
			{
				// Refresh the alarm
				adapter.notifyItemChanged(i)
			}
		}
	}

	/**
	 * Refresh the main activity.
	 */
	private fun refreshMainActivity()
	{
		val shared = sharedPreferences
		shared!!.editShouldRefreshMainActivity(false)
		recreate()
	}

	/**
	 * Restore an alarm and add it back to the database.
	 *
	 * @param  alarm  An alarm.
	 */
	fun restoreAlarm(alarm: NacAlarm?)
	{
		if (alarm == null)
		{
			return
		}
		val message = getString(R.string.message_alarm_restore)
		val action = getString(R.string.action_undo)
		lastAlarmCardAction[alarm] = NacLastAlarmCardAction.Type.RESTORE
		alarmViewModel!!.insert(this, alarm)
		showSnackbar(message, action, mOnSwipeSnackbarActionListener)
	}

	/**
	 * Save the scanned NFC tag ID.
	 */
	private fun saveNfcTagId(intent: Intent)
	{
		// Check if the intent corresponds with the scan NFC tag dialog
		if (!wasNfcScannedForDialog(intent))
		{
			return
		}
		val nfcTag = getTag(intent) ?: return

		// Check that the intent tag is null

		// Get the dialog
		val dialog = scanNfcTagDialog

		// Get the alarm and NFC tag ID
		val alarm = dialog!!.alarm
		val id = parseId(nfcTag)

		// Check to make sure the alarm is not null before proceeding
		if (alarm == null)
		{
			return
		}

		// Set the NFC tag ID
		alarm.nfcTagId = id!!

		// Update the alarm
		alarmViewModel!!.update(this, alarm)
	}

	/**
	 * Set the next alarm message in the text view.
	 */
	private fun setNextAlarmMessage()
	{
		val cardAdapter = alarmCardAdapter
		val alarms = cardAdapter!!.currentList
		this.setNextAlarmMessage(alarms)
	}

	/**
	 * Set the next alarm message in the text view.
	 */
	private fun setNextAlarmMessage(alarms: List<NacAlarm>)
	{
		val nextAlarm = nextAlarmTextView
		val message = getNextAlarmMessage(alarms)
		nextAlarm!!.text = message
	}

	/**
	 * Set the NFC tag active alarm.
	 *
	 * @param  activeAlarm  The active alarm to use in conjunction with the NFC tag.
	 */
	private fun setNfcTagAlarm(activeAlarm: NacAlarm)
	{
		val tag = nfcTag
		if (tag == null)
		{
			nfcTag = NacNfcTag(activeAlarm)
		}
		else
		{
			tag.activeAlarm = activeAlarm
		}
	}

	/**
	 * Set the NFC tag action and ID from an Intent.
	 *
	 * @param  nfcIntent  The intent received when scanning an NFC tag.
	 */
	private fun setNfcTagIntent(nfcIntent: Intent)
	{
		val tag = nfcTag
		if (tag == null)
		{
			nfcTag = NacNfcTag(nfcIntent)
		}
		else
		{
			tag.setNfcId(nfcIntent)
			tag.setNfcAction(nfcIntent)
		}
	}

	/**
	 * Setup the alarm card adapter.
	 */
	private fun setupAlarmCardAdapter()
	{
		val rv = recyclerView
		val adapter = alarmCardAdapter
		val touchHelper = alarmCardTouchHelper
		adapter!!.onViewHolderBoundListener = this
		adapter.onViewHolderCreatedListener = this
		touchHelper!!.attachToRecyclerView(rv)
	}

	/**
	 * Setup the floating action button.
	 */
	private fun setupFloatingActionButton()
	{
		val shared = sharedPreferences
		val floatingButton = floatingActionButton
		val color = ColorStateList.valueOf(shared!!.themeColor)

		//floatingButton.setOnClickListener(this);
		floatingButton!!.setOnClickListener(mFloatingActionButtonListener)
		floatingButton.backgroundTintList = color
	}

	/**
	 * Run the setup when it is the app's first time running.
	 *
	 *
	 * If it is not the app's first time running, this does nothing.
	 */
	private fun setupForAppFirstRun()
	{
		val shared = sharedPreferences
		shared!!.editAppFirstRun(this, false)
		shared.editAppStartStatistics(false)
		addFirstAlarm()
	}

	/**
	 * Setup the Google rating dialog.
	 */
	private fun setupGoogleRatingDialog(): Boolean
	{
		val shared = sharedPreferences
		return request(this, shared!!)
	}

	/**
	 * Setup an initial dialog, if any, that need to be shown.
	 */
	private fun setupInitialDialogToShow()
	{
		// Get the shared preferences
		val manager = permissionRequestManager

		// Request permissions
		if (manager!!.count() > 0)
		{
			manager.requestPermissions(this)
		}
		else if (setupWhatsNewDialog())
		{
		}
		else if (setupGoogleRatingDialog())
		{
		}
	}

	/**
	 * Setup LiveData observers.
	 */
	private fun setupLiveDataObservers()
	{
		// Observer is called when list of all alarms changes. Including when the app
		// starts and the list is initially empty
		alarmViewModel!!.allAlarms.observe(this
		) { alarms: List<NacAlarm> ->
			// Setup statistics
			setupStatistics(alarms)

			// Merge and sort the alarms if there are none expanded
			if (cardsExpandedCount == 0)
			{
				alarmCardAdapterLiveData!!.mergeSort(alarms)
			}
			else
			{
				alarmCardAdapterLiveData!!.merge(alarms)
			}

			// Set the next alarm message
			this.setNextAlarmMessage(alarms)
		}
		alarmViewModel!!.activeAlarm.observe(this) { alarm: NacAlarm? -> prepareActiveAlarm(alarm) }
		alarmCardAdapterLiveData!!.observe(this) { alarms ->

			 // Alarm list has changed.
			 // TODO: There is a race condition between snoozing an alarm, writing to the
			 // database, and refreshing the main activity.

			// If this is the first time the app is running, set the flags accordingly
			if (sharedPreferences!!.getAppFirstRun(this))
			{
				setupForAppFirstRun()
			}

			// Update the notification if a user uses upcoming alarm notifications
			this.updateUpcomingNotification(alarms)

			// Update the alarm adapter
			alarmCardAdapter!!.storeIndicesOfExpandedCards(recyclerView!!)
			alarmCardAdapter!!.submitList(alarms)

			// Check if the main activity should be refreshed and if so, refresh it
			// TODO: Why is this here?
			if (sharedPreferences!!.shouldRefreshMainActivity)
			{
				refreshMainActivity()
			}

		}
	}

	/**
	 * Setup the recycler view.
	 */
	private fun setupRecyclerView()
	{
		val rv = recyclerView
		val adapter = alarmCardAdapter
		val padding = resources.getDimensionPixelSize(R.dimen.normal)
		val drawable = ContextCompat.getDrawable(this,
			R.drawable.card_divider)
		val insetDrawable = InsetDrawable(drawable, padding, 0,
			padding, 0)
		val divider = DividerItemDecoration(this,
			LinearLayoutManager.VERTICAL)
		val layoutManager = NacLayoutManager(this)

		//divider.setDrawable(drawable);
		divider.setDrawable(insetDrawable)
		rv!!.addItemDecoration(divider)
		rv.adapter = adapter
		rv.layoutManager = layoutManager
		rv.addOnItemTouchListener(this)
		rv.setHasFixedSize(true)
	}

	/**
	 * Setup the shutdown broadcast receiver.
	 */
	private fun setupShutdownBroadcastReceiver()
	{
		val receiver = shutdownBroadcastReceiver
		val filter = IntentFilter(Intent.ACTION_SHUTDOWN)
		receiver?.let { registerReceiver(it, filter) }
	}

	/**
	 * Setup statistics, and start collecting the data.
	 *
	 *
	 * This is only done if this is not the app's first time running and
	 * statistics should be started.
	 *
	 * @param  alarms  List of alarms.
	 */
	private fun setupStatistics(alarms: List<NacAlarm>)
	{
		val shared = sharedPreferences
		if (shared!!.appStartStatistics)
		{
			val repo = alarmStatisticRepository
			val numCreated = repo!!.createdCount
			if (numCreated == 0L)
			{
				for (a in alarms)
				{
					repo.insertCreated()
				}
			}
			shared.editAppStartStatistics(false)
		}
	}

	/**
	 * Setup the time tick receiver.
	 */
	private fun setupTimeTickReceiver()
	{
		val receiver = timeTickReceiver
		val filter = IntentFilter(Intent.ACTION_TIME_TICK)

		// Register the receiver
		registerReceiver(receiver, filter)
	}

	/**
	 * Setup the toolbar
	 */
	private fun setupToolbar()
	{
		val toolbar = toolbar
		toolbar!!.setOnMenuItemClickListener(this)
	}

	/**
	 * Setup showing the What's New dialog.
	 */
	private fun setupWhatsNewDialog(): Boolean
	{
		val shared = sharedPreferences
		val version = BuildConfig.VERSION_NAME
		val prevVersion = shared!!.previousAppVersion

		// The current version and previously saved version match. This means there
		// is no update that has occurred. Alternatively, something is wrong with the
		// current version (if it is empty)
		if (version == prevVersion)
		{
			return false
		}

		// Show the What's New dialog
		showWhatsNewDialog()
		return true
	}

	/**
	 * @return True if the alarm activity should be shown, and False otherwise.
	 */
	private fun shouldShowAlarmActivity(alarm: NacAlarm?): Boolean
	{
		val intent = intent
		return alarm != null && !wasScanned(intent)
	}

	/**
	 * Show a snackbar for the alarm.
	 */
	fun showAlarmSnackbar(alarm: NacAlarm?)
	{
		val shared = sharedPreferences
		val message = NacCalendar.getMessageWillRun(shared, alarm)
		val action = getString(R.string.action_alarm_dismiss)
		showSnackbar(message, action)
	}

	/**
	 * Show the audio options dialog.
	 */
	fun showAudioOptionsDialog(alarm: NacAlarm?)
	{
		val dialog = NacAlarmAudioOptionsDialog()
		dialog.alarmId = alarm!!.id
		dialog.onAudioOptionClickedListener = this@NacMainActivity
		dialog.show(supportFragmentManager, NacAlarmAudioOptionsDialog.TAG)
	}

	/**
	 * Show the audio source dialog.
	 */
	fun showAudioSourceDialog()
	{
		val dialog = NacAudioSourceDialog()
		val alarm = audioOptionsAlarm
		val audioSource = alarm!!.audioSource
		dialog.defaultAudioSource = audioSource
		dialog.onAudioSourceSelectedListener = this
		dialog.show(supportFragmentManager, NacAudioSourceDialog.TAG)
	}

	/**
	 * Show the dismiss early dialog.
	 */
	fun showDismissEarlyDialog()
	{
		val dialog = NacDismissEarlyDialog()
		val alarm = audioOptionsAlarm
		val useDismissEarly = alarm!!.shouldUseDismissEarly
		val index = alarm.dismissEarlyIndex
		printf("Index : %d", index)
		dialog.defaultShouldDismissEarly = useDismissEarly
		dialog.defaultShouldDismissEarlyIndex = index
		dialog.onDismissEarlyOptionSelectedListener = this
		dialog.show(supportFragmentManager, NacGraduallyIncreaseVolumeDialog.TAG)
	}

	/**
	 * Show the gradually increase volume dialog.
	 */
	fun showGraduallyIncreaseVolumeDialog()
	{
		val dialog = NacGraduallyIncreaseVolumeDialog()
		val alarm = audioOptionsAlarm
		val shouldIncrease = alarm!!.shouldGraduallyIncreaseVolume
		dialog.defaultShouldGraduallyIncreaseVolume = shouldIncrease
		dialog.onGraduallyIncreaseVolumeListener = this
		dialog.show(supportFragmentManager, NacGraduallyIncreaseVolumeDialog.TAG)
	}

	/**
	 * Show a snackbar for the next alarm that will run.
	 */
	fun showNextAlarmSnackbar()
	{
		val shared = sharedPreferences
		val cardAdapter = alarmCardAdapter
		val alarms = cardAdapter!!.currentList
		val alarm = NacCalendar.getNextAlarm(alarms)
		val message = NacCalendar.getMessageNextAlarm(shared, alarm)
		val action = getString(R.string.action_alarm_dismiss)
		showSnackbar(message, action)
	}

	/**
	 * Show the saved NFC tag ID of the given alarm.
	 */
	fun showNfcTagId(alarm: NacAlarm?)
	{
		if (alarm == null)
		{
			return
		}
		val locale = Locale.getDefault()
		val id = alarm.nfcTagId
		val message: String
		message = if (!id.isEmpty())
		{
			val nfcId = getString(R.string.message_show_nfc_tag_id)
			String.format(locale, "%1\$s %2\$s", nfcId, id)
		}
		else
		{
			val anyNfc = getString(R.string.message_any_nfc_tag_id)
			String.format(locale, "%1\$s", anyNfc)
		}
		quickToast(this, message)
	}

	/**
	 * Show the restrict volume dialog.
	 */
	fun showRestrictVolumeDialog()
	{
		val dialog = NacRestrictVolumeDialog()
		val alarm = audioOptionsAlarm
		val shouldRestrict = alarm!!.shouldRestrictVolume
		dialog.defaultShouldRestrictVolume = shouldRestrict
		dialog.onRestrictVolumeListener = this
		dialog.show(supportFragmentManager, NacRestrictVolumeDialog.TAG)
	}
	/**
	 * Create a snackbar message.
	 */
	/**
	 * @see .showSnackbar
	 */
	private fun showSnackbar(message: String, action: String,
		listener: View.OnClickListener? = null)
	{
		val snackbar = snackbar
		snackbar!!.show(message, action, listener, true)
	}

	/**
	 * Show the text-to-speech dialog.
	 */
	fun showTextToSpeechDialog()
	{
		val dialog = NacTextToSpeechDialog()
		val alarm = audioOptionsAlarm
		val useTts = alarm!!.shouldUseTts
		val freq = alarm.ttsFrequency
		dialog.defaultUseTts = useTts
		dialog.defaultTtsFrequency = freq
		dialog.onTextToSpeechOptionsSelectedListener = this
		dialog.show(supportFragmentManager, NacTextToSpeechDialog.TAG)
	}

	/**
	 * Show a snackbar for the updated alarm.
	 *
	 *
	 * If this alarm is disabled, a snackbar for the next alarm will be shown.
	 */
	fun showUpdatedAlarmSnackbar(alarm: NacAlarm?)
	{
		if (alarm!!.isEnabled)
		{
			showAlarmSnackbar(alarm)
		}
		else
		{
			showNextAlarmSnackbar()
		}
	}

	/**
	 * Show the What's New dialog.
	 */
	fun showWhatsNewDialog()
	{
		val dialog = NacWhatsNewDialog()
		dialog.onReadWhatsNewListener = this
		dialog.show(supportFragmentManager, NacWhatsNewDialog.TAG)
	}

	/**
	 * Update the notification.
	 *
	 *
	 * TODO: Check if race condition with this being called after submitList?
	 * Should I just pass a list of alarms to this method?
	 */
	fun updateUpcomingNotification(alarms: List<NacAlarm>)
	{
		val shared = sharedPreferences
		if (shared!!.upcomingAlarmNotification)
		{
			val notification = NacUpcomingAlarmNotification(this)
			notification.alarmList = alarms
			notification.show()
		}
	}

	/**
	 * @see .updateUpcomingNotification
	 */
	fun updateUpcomingNotification()
	{
		val cardAdapter = alarmCardAdapter
		val alarms = cardAdapter!!.currentList
		this.updateUpcomingNotification(alarms)
	}

	/**
	 * Verify that the card is measured.
	 *
	 *
	 * If a card has already been measured, this does nothing.
	 */
	private fun verifyCardIsMeasured(card: NacCardHolder?)
	{
		val shared = sharedPreferences
		if (shared!!.cardIsMeasured)
		{
			return
		}
		val heights = IntArray(3)
		card!!.measureCard(heights)
		shared.editCardHeightCollapsed(heights[0])
		shared.editCardHeightCollapsedDismiss(heights[1])
		shared.editCardHeightExpanded(heights[2])
		shared.editCardIsMeasured(true)
	}

	/**
	 * @return True if an NFC tag was scanned to dismiss an alarm, and False
	 * otherwise. This is to say that if an NFC tag was scanned for the
	 * dialog, this would return False.
	 */
	private fun wasNfcScannedForActiveAlarm(intent: Intent): Boolean
	{
		return wasScanned(intent) && !wasNfcScannedForDialog(intent)
	}

	/**
	 * @return True if an NFC tag was scanned while the Scan NFC Tag dialog was
	 * open, and False otherwise.
	 */
	private fun wasNfcScannedForDialog(intent: Intent): Boolean
	{
		val dialog = scanNfcTagDialog
		return dialog != null && wasScanned(intent)
	}
}