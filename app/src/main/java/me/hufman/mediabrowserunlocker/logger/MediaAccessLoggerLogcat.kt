package me.hufman.mediabrowserunlocker.logger

import android.util.Log

open class MediaAccessLoggerLogcat :
	MediaAccessLogger {
	override fun onGetRoot(
		servicePackage: String,
		clientPackage: String,
		originalRoot: String?,
		replacementRoot: String?
	) {
		if (originalRoot != replacementRoot)
			Log.d(
				"MediaBrowser",
				"onGetRoot from $clientPackage -> $servicePackage, redirected root $originalRoot to $replacementRoot"
			)
		else
			Log.d(
				"MediaBrowser",
				"onGetRoot from $clientPackage -> $servicePackage, received root $originalRoot"
			)
	}

	override fun onLoadChildren(
		servicePackage: String,
		parentId: String,
		results: List<*>?
	) {
		Log.d(
			"MediaBrowser",
			"onLoadChildren to $servicePackage:$parentId -> ${results?.joinToString(", ")}"
		)
	}
}