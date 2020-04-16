package me.hufman.mediabrowserunlocker

import android.service.media.MediaBrowserService
import android.support.v4.media.MediaBrowserCompat
import androidx.media.MediaBrowserServiceCompat
import me.hufman.mediabrowserunlocker.logger.MediaAccessLogger
import org.apache.commons.collections4.map.LRUMap

data class BrowseResultContext(val servicePackage: String, val parentId: String)

class BrowseResultCollation(val logger: MediaAccessLogger) {
	private val resultMetadata = LRUMap<Any, BrowseResultContext>(16)

	fun startResult(
		result: MediaBrowserService.Result<*>,
		servicePackage: String,
		parentId: String
	) = _startResult(result, servicePackage, parentId)

	fun startResult(
		result: MediaBrowserServiceCompat.Result<*>,
		servicePackage: String,
		parentId: String
	) = _startResult(result, servicePackage, parentId)

	private fun _startResult(result: Any, servicePackage: String, parentId: String) {
		val context = BrowseResultContext(servicePackage, parentId)
		synchronized(this) {
			resultMetadata[result] = context
		}
	}

	fun completeResult(
		result: MediaBrowserService.Result<*>,
		value: List<MediaBrowserCompat.MediaItem>?
	) = _completeResult(result, value)

	fun completeResult(
		result: MediaBrowserServiceCompat.Result<*>,
		value: List<MediaBrowserCompat.MediaItem>?
	) = _completeResult(result, value)

	private fun _completeResult(result: Any, value: List<MediaBrowserCompat.MediaItem>?) {
		val context = synchronized(this) {
			resultMetadata.remove(result)
		} ?: return
		logger.onLoadChildren(context.servicePackage, context.parentId, value)
	}
}