package me.hufman.mediabrowserunlocker.logger

/**
 * Reports Media app access to the main UI
 */
interface MediaAccessLogger {
	fun onGetRoot(
		servicePackage: String,
		clientPackage: String,
		originalRoot: String?,
		replacementRoot: String?
	)

	fun onLoadChildren(
		servicePackage: String,
		parentId: String,
		results: List<*>?
	)
}