package me.hufman.mediabrowserunlocker.policy

import android.util.Log
import me.hufman.mediabrowserunlocker.data.MediaRootDao

class MediaAccessPolicyDatabase(val mediaRootDao: MediaRootDao): MediaAccessPolicy {
	companion object {
		val TAG = "MediaBrowserUnlPolicy"
	}
	override fun getRootAction(
		servicePackage: String,
		clientPackage: String,
		originalRoot: String?
	): MediaAccessPolicyAction {
		val roots = mediaRootDao.getRootsForService(servicePackage)
		val successfulRoots = roots.filter { it.success }
		return if (successfulRoots.isNotEmpty()) {
			Log.d(TAG, "Using verified root from ${successfulRoots.first().clientPackage}")
			MediaAccessPolicyAction.OVERRIDE_ROOT(successfulRoots.first().rootId)
		} else {
			val gearheadRoots = roots.filter { it.clientPackage.contains("gearhead") }
			if (gearheadRoots.isNotEmpty()) {
				Log.d(TAG, "Using gearhead root from ${gearheadRoots.first().clientPackage}")
				MediaAccessPolicyAction.OVERRIDE_ROOT(gearheadRoots.first().rootId)
			} else {
				Log.d(TAG, "Providing fallback root")
				MediaAccessPolicyAction.OVERRIDE_REJECTED_ROOT("_ROOT_")
			}
		}
	}
}