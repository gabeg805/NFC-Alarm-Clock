package com.nfcalarmclock.system.mediaplayer

import androidx.media3.common.AudioAttributes
import androidx.media3.common.DeviceInfo
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.text.CueGroup
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.analytics.AnalyticsListener
import androidx.media3.exoplayer.analytics.AnalyticsListener.EventTime
import androidx.media3.exoplayer.source.LoadEventInfo
import androidx.media3.exoplayer.source.MediaLoadData
import com.nfcalarmclock.log.NacLog
import java.io.IOException
import java.lang.Exception

/**
 * Log Exoplayer events.
 */
@UnstableApi
class NacMediaLogger
	: AnalyticsListener
{

	/**
	 * Audio attributes changed.
	 */
	override fun onAudioAttributesChanged(eventTime: EventTime, attrs: AudioAttributes)
	{
		NacLog.i("Exoplayer audio attributes changed. Stream=${attrs.volumeControlStream} | ContentType=${attrs.contentType} | Flags=${attrs.flags} | Usage=${attrs.usage}")
	}

	/**
	 * Audio codec error.
	 */
	override fun onAudioCodecError(eventTime: EventTime, audioCodecError: Exception)
	{
		NacLog.e("Exoplayer audio codec error. This can be recoverable", throwable = audioCodecError)
	}

	/**
	 * Audio sink error.
	 */
	override fun onAudioSinkError(eventTime: EventTime, audioSinkError: Exception)
	{
		NacLog.e("Exoplayer audio sink error. This can be recoverable", throwable = audioSinkError)
	}

	/**
	 * CueGroup was changed.
	 */
	override fun onCues(eventTime: EventTime, cueGroup: CueGroup)
	{
		NacLog.i("Exoplayer cue group changed. CueGroup=$cueGroup | Cues=${cueGroup.cues}")
	}

	/**
	 * Device info changed.
	 */
	override fun onDeviceInfoChanged(eventTime: EventTime, deviceInfo: DeviceInfo)
	{
		NacLog.i("Exoplayer device info changed. MinV=${deviceInfo.minVolume} | MaxV=${deviceInfo.maxVolume} | PlaybackType=${deviceInfo.playbackType}")
	}

	/**
	 * Device volume changed.
	 */
	override fun onDeviceVolumeChanged(eventTime: EventTime, volume: Int, muted: Boolean)
	{
		NacLog.i("Exoplayer device volume changed. Volume=$volume | Muted=$muted")
	}

	/**
	 * Load error.
	 */
	override fun onLoadError(
		eventTime: EventTime,
		loadEventInfo: LoadEventInfo,
		mediaLoadData: MediaLoadData,
		error: IOException,
		wasCanceled: Boolean
	)
	{
		NacLog.e("Exoplayer load error load event info: Bytes=${loadEventInfo.bytesLoaded} | LoadDurationMs=${loadEventInfo.loadDurationMs} | LoadUri=${loadEventInfo.uri}")
		NacLog.e("Exoplayer load error media load data: Type=${mediaLoadData.dataType} | StartMs=${mediaLoadData.mediaStartTimeMs} | EndMs=${mediaLoadData.mediaEndTimeMs} | TrackFormat=${mediaLoadData.trackFormat} | TrackType=${mediaLoadData.trackType}")
		NacLog.e("Exoplayer load error. This can be recoverable. WasCanceled=$wasCanceled", throwable = error)
	}

	/**
	 * Player error changed.
	 */
	override fun onPlayerErrorChanged(eventTime: EventTime, error: PlaybackException?)
	{
		NacLog.e("Exoplayer player error changed", throwable = error)
	}

	/**
	 * Player error.
	 */
	override fun onPlayerError(eventTime: EventTime, error: PlaybackException)
	{
		NacLog.e("Exoplayer player error: ${error.errorCodeName} - ${error.message}")
	}

	/**
	 * Player is released.
	 */
	override fun onPlayerReleased(eventTime: EventTime)
	{
		NacLog.i("Exoplayer released")
	}

	/**
	 * Playback state changed.
	 */
	override fun onPlaybackStateChanged(eventTime: EventTime, state: Int)
	{
		val stateString = when (state)
		{
			Player.STATE_BUFFERING -> "BUFFERING"
			Player.STATE_ENDED     -> "ENDED"
			Player.STATE_IDLE      -> "IDLE"
			Player.STATE_READY     -> "READY"
			else                   -> "UNKNOWN"
		}

		NacLog.i("Exoplayer playback state changed: $stateString")
	}

	/**
	 * Volume released.
	 */
	override fun onVolumeChanged(eventTime: EventTime, volume: Float)
	{
		NacLog.i("Exoplayer volume changed. Volume=$volume")
	}

}
