package me.hufman.mediabrowserunlocker

import android.service.media.MediaBrowserService
import android.support.v4.media.MediaBrowserCompat
import androidx.media.MediaBrowserServiceCompat
import me.hufman.mediabrowserunlocker.logger.MediaAccessLogger
import me.hufman.mediabrowserunlocker.policy.MediaAccessPolicy
import me.hufman.mediabrowserunlocker.policy.MediaAccessPolicyAction

class UnlockerEngine(
	val browseResultCollation: BrowseResultCollation,
	val policy: MediaAccessPolicy,
	val logger: MediaAccessLogger
) {
	fun onGetRoot(servicePackage: String, clientPackage: String, origRoot: String?): String? {
		val action = policy.getRootAction(servicePackage, clientPackage)
		val newRoot = when (action) {
			is MediaAccessPolicyAction.OVERRIDE_ROOT -> action.newRoot
			is MediaAccessPolicyAction.OVERRIDE_REJECTED_ROOT -> origRoot ?: action.newRoot
			else -> origRoot
		}
		logger.onGetRoot(servicePackage, clientPackage, origRoot, newRoot)
		return newRoot
	}

	fun beforeOnLoadChildren(servicePackage: String, parentId: String, result: MediaBrowserService.Result<*>) {
		browseResultCollation.startResult(result, servicePackage, parentId)
	}
	fun beforeOnLoadChildren(servicePackage: String, parentId: String, result: MediaBrowserServiceCompat.Result<*>) {
		browseResultCollation.startResult(result, servicePackage, parentId)
	}

	fun onCompleteLoadChildren(result: MediaBrowserService.Result<*>, value: List<MediaBrowserCompat.MediaItem>?) {
		browseResultCollation.completeResult(result, value)
	}
	fun onCompleteLoadChildren(result: MediaBrowserServiceCompat.Result<*>, value: List<MediaBrowserCompat.MediaItem>?) {
		browseResultCollation.completeResult(result, value)
	}
}