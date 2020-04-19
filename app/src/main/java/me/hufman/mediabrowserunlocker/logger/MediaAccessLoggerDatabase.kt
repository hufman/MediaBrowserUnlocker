package me.hufman.mediabrowserunlocker.logger

import me.hufman.mediabrowserunlocker.data.MediaRootDao

class MediaAccessLoggerDatabase(val mediaRootDao: MediaRootDao): MediaAccessLoggerLogcat() {
	override fun onGetRoot(
		servicePackage: String,
		clientPackage: String,
		originalRoot: String?,
		replacementRoot: String?
	) {
		super.onGetRoot(servicePackage, clientPackage, originalRoot, replacementRoot)
		mediaRootDao.upsert(servicePackage, clientPackage, originalRoot)
	}

	override fun onLoadChildren(servicePackage: String, parentId: String, results: List<*>?) {
		super.onLoadChildren(servicePackage, parentId, results)
		val rootId = mediaRootDao.getRootForService(servicePackage, rootId = parentId)
		val success = (results?.isNotEmpty() == true)
		if (rootId != null) {
			mediaRootDao.updateSuccess(rootId.id, success)
		}
	}
}